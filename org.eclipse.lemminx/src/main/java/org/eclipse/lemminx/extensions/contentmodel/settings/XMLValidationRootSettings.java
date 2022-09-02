/**
 *  Copyright (c) 2022 Red Hat Inc. and others.
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
package org.eclipse.lemminx.extensions.contentmodel.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * XML validation settings root which defines validation rules for all files.
 * 
 * The {@link XMLValidationRootSettings}{@link #filters} can be used to define
 * custom validation rule for a given file pattern:
 * 
 * <code>
[
  {
    "pattern": "**.exsd",
    "enabled": false
  },
  {
    "pattern": "**{.project,.classpath,plugin.xml,feature.xml,category.xml,.target,.product}",
    "noGrammar": "ignore"
  }
]
 * </code>
 * 
 */
public class XMLValidationRootSettings extends XMLValidationSettings {

	private static final XMLValidationFilter[] DEFAULT_FILTERS;

	private XMLValidationFilter[] filters;

	static {
		DEFAULT_FILTERS = createDefaultFilters();
	}

	public XMLValidationRootSettings() {
		super();
		setFilters(DEFAULT_FILTERS);
	}

	/**
	 * Returns validation filters to define custom validation rule for a given file
	 * pattern and null otherwise.
	 * 
	 * @return validation filters to define custom validation rule for a given file
	 *         pattern and null otherwise.
	 */
	public XMLValidationFilter[] getFilters() {
		return filters;
	}

	public void setFilters(XMLValidationFilter[] filters) {
		this.filters = filters;
	}

	/**
	 * Returns the validation settings for the given uri and the global validation
	 * settings otherwise.
	 * 
	 * @param uri the XML document to validate.
	 * 
	 * @returnthe validation settings for the given uri and the global validation
	 *            settings otherwise.
	 */
	public XMLValidationSettings getValidationSettings(String uri) {
		if (filters != null) {
			for (XMLValidationFilter filter : filters) {
				if (filter.matches(uri)) {
					return filter;
				}
			}
		}
		return this;
	}

	public XMLValidationRootSettings merge(XMLValidationRootSettings settings) {
		if (settings != null) {
			this.filters = settings.getFilters();
		}
		super.merge(settings);
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(filters);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		XMLValidationRootSettings other = (XMLValidationRootSettings) obj;
		return Arrays.equals(filters, other.filters);
	}

	private static XMLValidationFilter[] createDefaultFilters() {
		List<XMLValidationFilter> filters = new ArrayList<>();
		// Ignore validation for Eclipse '*.exsd' files
		XMLValidationFilter filter = new XMLValidationFilter();
		filter.setEnabled(false);
		filter.setPattern("**.exsd");
		filters.add(filter);
		// Don't warn that XML file have no grammar for Eclipse '.project',
		// '.classpath',
		// 'plugin.xml', 'feature.xml' files
		filter = new XMLValidationFilter();
		filter.setNoGrammar("ignore");
		filter.setPattern("**{.project,.classpath,plugin.xml,feature.xml,category.xml,.target,.product}");
		filters.add(filter);
		return filters.toArray(new XMLValidationFilter[filters.size()]);
	}
}