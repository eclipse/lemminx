/**
 *  Copyright (c) 2020 Red Hat, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lemminx.extensions.entities;

import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;

/**
 * Utilities class to generate entities documentation.
 *
 */
public class EntitiesDocumentationUtils {

	/**
	 * Predefined entities.
	 * 
	 * @See https://www.w3.org/TR/xml/#sec-predefined-ent
	 *
	 */
	public static enum PredefinedEntity {

		lt("&#60;"), gt("&#62;"), amp("&#38;"), apos("&#39;"), quot("&#34;");

		private final String value;

		private PredefinedEntity(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	private EntitiesDocumentationUtils() {
	}

	/**
	 * Returns the entity documentation.
	 * 
	 * @param entityName  the entity name.
	 * @param entityValue the entity value.
	 * @param external    true if it's an external entity and false otherwise.
	 * @param predefined  true if it's an predefined entity and false otherwise.
	 * @param markdown    true if the documentation can be formatted as markdown and
	 *                    false otherwise.
	 * @return the entity documentation.
	 */
	public static MarkupContent getDocumentation(String entityName, String entityValue, boolean external,
			boolean predefined, boolean markdown) {
		StringBuilder documentation = new StringBuilder();

		// Title
		if (markdown) {
			documentation.append("**");
		}
		documentation.append("Entity ");
		documentation.append(entityName);
		if (markdown) {
			documentation.append("**");
		}

		if (entityValue != null && !entityValue.isEmpty()) {
			addParameter("Value", entityValue, documentation, markdown);
		}
		addParameter("External", String.valueOf(external), documentation, markdown);
		addParameter("Predefined", String.valueOf(predefined), documentation, markdown);

		documentation.append(System.lineSeparator());
		return new MarkupContent(markdown ? MarkupKind.MARKDOWN : MarkupKind.PLAINTEXT, documentation.toString());
	}

	private static void addParameter(String name, String value, StringBuilder documentation, boolean markdown) {
		if (value != null) {
			documentation.append(System.lineSeparator());
			if (markdown) {
				documentation.append(" * ");
			}
			documentation.append(name);
			documentation.append(": ");
			if (markdown) {
				documentation.append("`");
			}
			documentation.append(value);
			if (markdown) {
				documentation.append("`");
			}
		}
	}

}
