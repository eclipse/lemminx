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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.eclipse.lemminx.extensions.contentmodel.model.CMAttributeDeclaration;

import com.thaiopensource.util.VoidValue;
import com.thaiopensource.xml.util.Name;

/**
 * RelaxNG class used to collect content model attributes for a given
 * {@link ElementPattern}.
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
public class CMRelaxNGAttributeDeclarationCollector extends AbstractCMRelaxNGCollector {

	private final CMRelaxNGElementDeclaration elementDeclaration;

	private final Collection<CMAttributeDeclaration> attributes;

	public CMRelaxNGAttributeDeclarationCollector(CMRelaxNGElementDeclaration elementDeclaration, Pattern pattern) {
		this.elementDeclaration = elementDeclaration;
		this.attributes = new ArrayList<>();
		pattern.apply(this);
		if (!attributes.isEmpty()) {
			RequiredAttributesFunction attributesFunction = new RequiredAttributesFunction();
			Set<Name> requiredAttributeNames = pattern.apply(attributesFunction);
			for (Name requiredAttributeName : requiredAttributeNames) {
				for (CMAttributeDeclaration attribute : attributes) {
					CMRelaxNGAttributeDeclaration rngAttribute = (CMRelaxNGAttributeDeclaration) attribute;
					if (requiredAttributeName.equals(rngAttribute.getJingName())) {
						rngAttribute.setRequired(true);
					}
				}
			}
		}
	}

	@Override
	public VoidValue caseAttribute(AttributePattern p) {
		NameClass nameClass = p.getNameClass();
		if (nameClass instanceof SimpleNameClass) {
			CMRelaxNGAttributeDeclaration attributeDeclaration = new CMRelaxNGAttributeDeclaration(elementDeclaration,
					p);
			attributes.add(attributeDeclaration);
		}
		return VoidValue.VOID;
	}

	public Collection<CMAttributeDeclaration> getAttributes() {
		return attributes;
	}
}
