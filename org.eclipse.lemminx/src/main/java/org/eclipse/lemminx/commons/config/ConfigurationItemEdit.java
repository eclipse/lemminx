/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.commons.config;

import org.eclipse.lsp4j.ConfigurationItem;

/**
 * Class representing a change to a client's config.
 */
public class ConfigurationItemEdit extends ConfigurationItem {
	private Object value;
	private ConfigurationItemEditType editType;
	private ConfigurationItemValueKind valueKind;

	/**
	 *
	 * @param section   config section to change
	 * @param value     the value for the change
	 * @param editType  the type of edit operation being made
	 * @param valueKind the kind of value for the configuration item
	 */
	public ConfigurationItemEdit(String section, Object value, ConfigurationItemEditType editType,
			ConfigurationItemValueKind valueKind) {
		super.setSection(section);
		this.value = value;
		this.editType = editType;
		this.valueKind = valueKind;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConfigurationItemEdit other = (ConfigurationItemEdit) obj;
		if (editType == null) {
			if (other.editType != null)
				return false;
		} else if (!editType.equals(other.editType))
			return false;
		if (valueKind == null) {
			if (other.valueKind != null)
				return false;
		} else if (!valueKind.equals(other.valueKind))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((editType == null) ? 0 : editType.hashCode());
		result = prime * result + ((valueKind == null) ? 0 : valueKind.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}
}