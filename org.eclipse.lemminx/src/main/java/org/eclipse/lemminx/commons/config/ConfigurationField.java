/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.commons.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

/**
 * Represents a configuration field discovered by scanning a settings class
 * hierarchy. Each instance pairs a reflective {@link Field} path (parent +
 * leaf) with a configuration section name (e.g.
 * {@code "xml.format.maxLineWidth"}). Used by the scoped
 * {@code workspace/configuration} pull mechanism.
 *
 * <p>
 * The scan is driven by {@link Scope} annotations: only leaf fields annotated
 * with {@code @Scope(ConfigurationScope.RESOURCE)} are included. Object fields
 * without {@code @Scope} are recursed into automatically.
 * </p>
 */
public class ConfigurationField {

	private static final Logger LOGGER = Logger.getLogger(ConfigurationField.class.getName());

	private final String parentFieldName;
	private final Field leafField;
	private final String section;

	private ConfigurationField(String parentFieldName, Field leafField, String section) {
		this.parentFieldName = parentFieldName;
		this.leafField = leafField;
		this.section = section;
	}

	/**
	 * Returns the configuration section name (e.g.
	 * {@code "xml.format.splitAttributes"}).
	 */
	public String getSection() {
		return section;
	}

	/**
	 * Recursively scan the given class hierarchy for fields annotated with
	 * {@link Scope}. Object fields without {@code @Scope} are recursed into;
	 * leaf fields (primitives, String, List, enums) with {@code @Scope} are
	 * collected.
	 *
	 * @param clazz  the root class to scan (e.g.
	 *               {@code XMLGeneralClientSettings.class}).
	 * @param prefix the configuration section prefix (e.g. {@code "xml."}).
	 * @return an unmodifiable list of {@link ConfigurationField} instances.
	 */
	public static List<ConfigurationField> computeConfigurationFields(Class<?> clazz, String prefix) {
		List<ConfigurationField> fields = new ArrayList<>();
		collectFields(clazz, prefix, null, fields);
		return Collections.unmodifiableList(fields);
	}

	private static void collectFields(Class<?> clazz, String prefix, String parentFieldName,
			List<ConfigurationField> result) {
		for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
			for (Field field : c.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
					continue;
				}
				field.setAccessible(true);
				if (field.isAnnotationPresent(Scope.class)) {
					result.add(new ConfigurationField(parentFieldName, field, prefix + field.getName()));
				} else if (!isLeafType(field.getType())) {
					collectFields(field.getType(), prefix + field.getName() + ".", field.getName(), result);
				}
			}
		}
	}

	private static boolean isLeafType(Class<?> type) {
		return type.isPrimitive() || type == String.class || type == Boolean.class
				|| type == Integer.class || type == List.class || type.isEnum();
	}

	/**
	 * Create a {@link ConfigurationParams} for pulling scoped configuration
	 * values from the client. Each {@link ConfigurationField} produces one
	 * {@link ConfigurationItem} with the given {@code scopeUri}.
	 *
	 * @param scopeUri the document URI to scope the configuration to.
	 * @param fields   the configuration fields to request.
	 * @return the configuration params ready to send via
	 *         {@code workspace/configuration}.
	 */
	public static ConfigurationParams createConfigurationParams(String scopeUri, List<ConfigurationField> fields) {
		List<ConfigurationItem> items = new ArrayList<>(fields.size());
		for (ConfigurationField cf : fields) {
			ConfigurationItem item = new ConfigurationItem();
			item.setScopeUri(scopeUri);
			item.setSection(cf.section);
			items.add(item);
		}
		return new ConfigurationParams(items);
	}

	/**
	 * Apply the results of a {@code workspace/configuration} response to the
	 * given root object. For each result, the method navigates to the parent
	 * field (by name) on the root object, then sets the leaf field value.
	 * Null results are skipped (the field retains its current value).
	 *
	 * <p>
	 * The root object's field names must match the parent field names
	 * discovered during scanning (e.g. both the scanned class and the root
	 * must have a field named {@code "format"}).
	 * </p>
	 *
	 * @param root    the root object (e.g. a {@code SharedSettings} instance).
	 * @param fields  the configuration fields, in the same order as the
	 *                configuration request items.
	 * @param results the configuration response values, one per field.
	 */
	public static void applyConfigurationResults(Object root, List<ConfigurationField> fields, List<Object> results) {
		Map<String, Object> targetCache = new HashMap<>();
		for (int i = 0; i < fields.size() && i < results.size(); i++) {
			Object value = results.get(i);
			if (value == null) {
				continue;
			}
			ConfigurationField cf = fields.get(i);
			try {
				Object target = root;
				if (cf.parentFieldName != null) {
					target = targetCache.computeIfAbsent(cf.parentFieldName, name -> {
						try {
							Field f = findField(root.getClass(), name);
							f.setAccessible(true);
							return f.get(root);
						} catch (Exception e) {
							LOGGER.log(Level.WARNING, "Cannot find parent field '" + name + "'.", e);
							return null;
						}
					});
					if (target == null) {
						continue;
					}
				}
				setFieldValue(target, cf.leafField, value);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Error setting configuration field '" + cf.section + "'.", e);
			}
		}
	}

	/**
	 * Compare the scoped field values between two root objects. Returns
	 * {@code true} if all {@link Scope}-annotated fields have the same
	 * values in both objects.
	 *
	 * @param a      the first root object.
	 * @param b      the second root object.
	 * @param fields the configuration fields to compare.
	 * @return {@code true} if all scoped values are equal.
	 */
	public static boolean haveSameValues(Object a, Object b, List<ConfigurationField> fields) {
		Map<String, Object[]> targetCache = new HashMap<>();
		for (ConfigurationField cf : fields) {
			try {
				Object targetA = a;
				Object targetB = b;
				if (cf.parentFieldName != null) {
					Object[] cached = targetCache.computeIfAbsent(cf.parentFieldName, name -> {
						try {
							Field fa = findField(a.getClass(), name);
							fa.setAccessible(true);
							Field fb = findField(b.getClass(), name);
							fb.setAccessible(true);
							return new Object[] { fa.get(a), fb.get(b) };
						} catch (Exception e) {
							return null;
						}
					});
					if (cached == null) {
						return false;
					}
					targetA = cached[0];
					targetB = cached[1];
				}
				Object v1 = cf.leafField.get(targetA);
				Object v2 = cf.leafField.get(targetB);
				if (!Objects.equals(v1, v2)) {
					return false;
				}
			} catch (Exception e) {
				return false;
			}
		}
		return true;
	}

	private static Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
		for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
			try {
				return c.getDeclaredField(name);
			} catch (NoSuchFieldException e) {
				// Try superclass
			}
		}
		throw new NoSuchFieldException(name);
	}

	private static void setFieldValue(Object target, Field field, Object value) throws Exception {
		Class<?> type = field.getType();
		if (value instanceof JsonPrimitive) {
			JsonPrimitive jp = (JsonPrimitive) value;
			if (type == boolean.class) {
				field.setBoolean(target, jp.getAsBoolean());
			} else if (type == Boolean.class) {
				field.set(target, jp.getAsBoolean());
			} else if (type == int.class) {
				field.setInt(target, jp.getAsInt());
			} else if (type == Integer.class) {
				field.set(target, jp.getAsInt());
			} else if (type == String.class) {
				field.set(target, jp.getAsString());
			}
		} else if (value instanceof JsonArray && type == List.class) {
			JsonArray arr = (JsonArray) value;
			List<String> list = new ArrayList<>(arr.size());
			for (int j = 0; j < arr.size(); j++) {
				list.add(arr.get(j).getAsString());
			}
			field.set(target, list);
		} else if (value instanceof Boolean && (type == boolean.class || type == Boolean.class)) {
			field.set(target, value);
		} else if (value instanceof Number && (type == int.class || type == Integer.class)) {
			field.set(target, ((Number) value).intValue());
		} else if (value instanceof String && type == String.class) {
			field.set(target, value);
		} else if (value instanceof List && type == List.class) {
			field.set(target, value);
		}
	}
}
