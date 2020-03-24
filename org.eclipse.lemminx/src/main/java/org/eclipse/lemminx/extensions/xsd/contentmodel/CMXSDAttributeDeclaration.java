/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.extensions.xsd.contentmodel;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSMultiValueFacet;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xs.XSValue;
import org.eclipse.lemminx.extensions.contentmodel.model.CMAttributeDeclaration;

/**
 * XSD attribute declaration implementation.
 *
 */
public class CMXSDAttributeDeclaration implements CMAttributeDeclaration {

	private final XSAttributeUse attributeUse;
	private String documentation;

	private Map<String, String> valuesDocumentation;
	
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

	@Override
	public String getValueDocumentation(String value) {
		if (valuesDocumentation == null) {
			valuesDocumentation = new HashMap<>();
		}
		String documentation = valuesDocumentation.get(value);
		if (documentation != null) {
			return documentation;
		}
		// Try get xs:annotation from the element declaration or type
		XSObjectList annotations = getValueAnnotations();
		documentation = XSDAnnotationModel.getDocumentation(annotations, value);
		valuesDocumentation.put(value, documentation);
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

	/**
	 * Returns list of xs:annotation from the element declaration or type
	 * declaration.
	 * 
	 * Indicated by:
	 * https://msdn.microsoft.com/en-us/library/ms256143(v=vs.110).aspx
	 * xs:attribute tags have content of either an xs:annotation or xs:simpleType
	 * 
	 * @return list of xs:annotation from the element declaration or type
	 *         declaration.
	 */
	private XSObjectList getValueAnnotations() {
		// Try get xs:annotation from the element declaration
		XSAttributeDeclaration attributeDeclaration = getAttrDeclaration();
		XSSimpleTypeDefinition simpleTypeDefinition = attributeDeclaration.getTypeDefinition();
		XSSimpleTypeDecl simpleTypeDecl;
		
	
		XSObjectList annotation = null; // The XSD tag that holds the documentation tag

		if(simpleTypeDefinition instanceof XSSimpleTypeDecl) {
			simpleTypeDecl = (XSSimpleTypeDecl) simpleTypeDefinition;
			XSObjectList multiFacets = simpleTypeDecl.getMultiValueFacets();
			if(!multiFacets.isEmpty()) {
				XSMultiValueFacet facet = (XSMultiValueFacet) multiFacets.get(0);
				multiFacets = facet.getAnnotations();
				Object[] annotationArray = multiFacets.toArray();
				if(!onlyContainsNull(annotationArray)) { // if multiValueFacets has annotations
					annotation = simpleTypeDecl.getMultiValueFacets();
				}
			}
		}
		if(annotation == null){ // There was no specific documentation for the value, so use the general attribute documentation
			annotation = attributeDeclaration.getAnnotations();
		}
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

	private boolean onlyContainsNull(Object[] arr) {
		if(arr == null || arr.length == 0) {
			return true;
		}
		for (Object o : arr) {
			if(o != null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isRequired() {
		return attributeUse.getRequired();
	}

	XSAttributeDeclaration getAttrDeclaration() {
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
