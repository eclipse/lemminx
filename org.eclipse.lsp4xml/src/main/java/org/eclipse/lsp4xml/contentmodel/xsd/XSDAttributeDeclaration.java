/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.contentmodel.xsd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSValue;
import org.eclipse.lsp4xml.contentmodel.model.CMAttributeDeclaration;

/**
 * XSD attribute declaration implementation.
 *
 */
public class XSDAttributeDeclaration implements CMAttributeDeclaration {

	private final XSAttributeUse attributeUse;
	private XSDAnnotationModel annotationModel;

	public XSDAttributeDeclaration(XSAttributeUse attributeUse) {
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
			if (isBooleanType(getAttrDeclaration().getTypeDefinition())) {
				return "false";
			}
		}
		return xsValue != null ? xsValue.getNormalizedValue().toString() : null;
	}

	@Override
	public String getDocumentation() {
		if (annotationModel == null && getAttrDeclaration().getAnnotation() != null) {
			annotationModel = XSDAnnotationModel.load(getAttrDeclaration().getAnnotation());
		}
		return annotationModel != null ? annotationModel.getDocumentation() : null;
	}

	@Override
	public boolean isRequired() {
		return attributeUse.getRequired();
	}

	private XSAttributeDeclaration getAttrDeclaration() {
		return attributeUse.getAttrDeclaration();
	}

	private static boolean isBooleanType(XSSimpleTypeDefinition typeDefinition) {
		if (typeDefinition instanceof XSSimpleType) {
			return ((XSSimpleType) typeDefinition).getPrimitiveKind() == XSSimpleType.PRIMITIVE_BOOLEAN;
		}
		return false;
	}

	@Override
	public Collection<String> getEnumerationValues() {
		XSAttributeDeclaration attributeDeclaration = getAttrDeclaration();
		if (attributeDeclaration != null) {
			XSSimpleTypeDefinition typeDefinition = attributeDeclaration.getTypeDefinition();
			if (typeDefinition != null) {
				if (isBooleanType(typeDefinition)) {
					Collection<String> values = new ArrayList<>();
					values.add("true");
					values.add("false");
					return values;
				}
				StringList enumerations = typeDefinition.getLexicalEnumeration();
				if (enumerations != null) {
					return enumerations;
				}
			}
		}
		return Collections.emptyList();
	}

}
