/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.xsd.contentmodel;

import java.util.Collection;
import java.util.Collections;

import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xs.XSValue;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMAttributeDeclaration;

/**
 * XSD attribute declaration implementation.
 *
 */
public class CMXSDAttributeDeclaration implements CMAttributeDeclaration {

	private final XSAttributeUse attributeUse;
	private String documentation;

	public CMXSDAttributeDeclaration(XSAttributeUse attributeUse) {
		this.attributeUse = attributeUse;
	}

	@Override
	public String getName() {
		return getAttrDeclaration().getName();
	}

	@Override
	public String getDefaultValue() {
		XSValue xsValue = attributeUse.getValueConstraintValue();
		if (xsValue == null) {
			if (CMXSDDocument.isBooleanType(getAttrDeclaration().getTypeDefinition())) {
				return "false";
			}
		}
		return xsValue != null ? xsValue.getNormalizedValue().toString() : null;
	}

	@Override
	public String getDocumentation() {
		if (documentation != null) {
			return documentation;
		}
		// Try get xs:annotation from the element declaration or type
		XSObjectList annotations = getAnnotations();
		documentation = XSDAnnotationModel.getDocumentation(annotations);
		return documentation;
	}

	/**
	 * Returns list of xs:annotation from the element declaration or type
	 * declaration.
	 * 
	 * @return list of xs:annotation from the element declaration or type
	 *         declaration.
	 */
	private XSObjectList getAnnotations() {
		// Try get xs:annotation from the element declaration
		XSAttributeDeclaration attributeDeclaration = getAttrDeclaration();
		XSObjectList annotation = attributeDeclaration.getAnnotations();
		if (annotation != null && annotation.getLength() > 0) {
			return annotation;
		}
		// Try get xs:annotation from the type of element declaration
		XSTypeDefinition typeDefinition = attributeDeclaration.getTypeDefinition();
		if (typeDefinition == null) {
			return null;
		}
		if (typeDefinition.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
			return ((XSComplexTypeDecl) typeDefinition).getAnnotations();
		} else if (typeDefinition.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
			return ((XSSimpleTypeDecl) typeDefinition).getAnnotations();
		}
		return null;
	}

	@Override
	public boolean isRequired() {
		return attributeUse.getRequired();
	}

	private XSAttributeDeclaration getAttrDeclaration() {
		return attributeUse.getAttrDeclaration();
	}

	@Override
	public Collection<String> getEnumerationValues() {
		XSAttributeDeclaration attributeDeclaration = getAttrDeclaration();
		if (attributeDeclaration != null) {
			XSSimpleTypeDefinition typeDefinition = attributeDeclaration.getTypeDefinition();
			return CMXSDDocument.getEnumerationValues(typeDefinition);
		}
		return Collections.emptyList();
	}

}
