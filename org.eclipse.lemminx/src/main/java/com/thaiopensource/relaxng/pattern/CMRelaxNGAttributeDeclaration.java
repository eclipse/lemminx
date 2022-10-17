/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.thaiopensource.relaxng.pattern;

import java.util.Collection;
import java.util.List;

import org.eclipse.lemminx.extensions.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lemminx.extensions.contentmodel.model.CMElementDeclaration;
import org.eclipse.lemminx.services.extensions.ISharedSettingsRequest;

import com.thaiopensource.xml.util.Name;

/**
 * RelaxNG content attribute implementation.
 * 
 * <p>
 * NOTE : this class is hosted in 'com.thaiopensource.relaxng.pattern' because
 * {@link Pattern} implementation like {@link ElementPattern} are not public.
 * Once https://github.com/relaxng/jing-trang/issues/271 will be fixed we could
 * move this class in 'org.eclipse.lemminx.extensions.relaxng.contentmodel'
 * package.
 * </p>
 * 
 * @author Angelo ZERR
 *
 */
public class CMRelaxNGAttributeDeclaration implements CMAttributeDeclaration {

	private final CMRelaxNGElementDeclaration cmElement;
	private final AttributePattern pattern;
	private boolean required;
	private List<String> values;

	public CMRelaxNGAttributeDeclaration(CMRelaxNGElementDeclaration element, AttributePattern pattern) {
		this.cmElement = element;
		this.pattern = pattern;
	}

	@Override
	public String getLocalName() {
		return getJingName().getLocalName();
	}

	@Override
	public String getNamespace() {
		return getJingName().getNamespaceUri();
	}

	@Override
	public CMElementDeclaration getOwnerElementDeclaration() {
		return cmElement;
	}
	
	Name getJingName() {
		NameClass nameClass = pattern.getNameClass();
		if (nameClass instanceof SimpleNameClass) {
			return ((SimpleNameClass) nameClass).getName();
		}
		return null;
	}

	@Override
	public String getDefaultValue() {
		return pattern.getDefaultValue();
	}

	@Override
	public Collection<String> getEnumerationValues() {
		if (values == null) {
			values = new CMRelaxNGAttributeValuesCollector(pattern.getContent()).getValues();
		}
		return values;
	}

	@Override
	public String getAttributeNameDocumentation(ISharedSettingsRequest request) {
		return cmElement.getCMDocument().getDocumentation(pattern.getLocator());
	}

	@Override
	public String getAttributeValueDocumentation(String value, ISharedSettingsRequest request) {
		if (!getEnumerationValues().isEmpty()) {
			String documentation = cmElement.getCMDocument().getDocumentation(pattern.getLocator(), value);
			if (documentation != null) {
				return documentation;
			}
		}
		// There was no specific documentation for the value, so use the general
		// attribute documentation
		return getAttributeNameDocumentation(request);
	}

	@Override
	public boolean isRequired() {
		return required;
	}

	void setRequired(boolean required) {
		this.required = required;
	}

	public AttributePattern getPattern() {
		return pattern;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("<attribute");
		result.append(" name=\"");
		result.append(getJingName().getLocalName());
		result.append("\"");
		result.append(" namespaceUri=\"");
		result.append(getJingName().getNamespaceUri());
		result.append("\"");
		result.append(" />");
		return result.toString();
	}
}
