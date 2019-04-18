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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMElementDeclaration;
import org.eclipse.lsp4xml.utils.StringUtils;

/**
 * XSD document implementation.
 *
 */
public class CMXSDDocument implements CMDocument {

	private final XSModel model;

	private final Map<XSElementDeclaration, CMXSDElementDeclaration> elementMappings;

	private List<CMElementDeclaration> elements;

	public CMXSDDocument(XSModel model) {
		this.model = model;
		this.elementMappings = new HashMap<>();
	}

	@Override
	public List<CMElementDeclaration> getElements() {
		if (elements == null) {
			elements = new ArrayList<>();
			XSNamedMap map = model.getComponents(XSConstants.ELEMENT_DECLARATION);
			for (int j = 0; j < map.getLength(); j++) {
				XSElementDeclaration elementDeclaration = (XSElementDeclaration) map.item(j);
				collectElement(elementDeclaration, elements);
			}
		}
		return elements;
	}

	/**
	 * https://stackoverflow.com/questions/8854144/xml-schema-root-element#answer-8854292
	 * @return
	 */
	public List<CMElementDeclaration> getTopLevelElements() {
		return null;
	}
 
	/**
	 * Fill the given elements list from the given Xerces elementDeclaration
	 * 
	 * @param elementDeclaration
	 * @param elements
	 */
	public void collectElement(XSElementDeclaration elementDeclaration, Collection<CMElementDeclaration> elements) {
		
		// if element declaration is marked as abstract
		// ex with xsl: <xs:element name="declaration" type="xsl:generic-element-type"
		// abstract="true"/>
		// then dont consider this as an option
		// From: https://www.w3schools.com/xml/schema_complex_subst.asp#midcontentadcontainer
		// it looks like an element with a substitution group is also considered
		// if abstract == false
		if (!elementDeclaration.getAbstract()) {
			CMElementDeclaration cmElement = getXSDElement(elementDeclaration);
			// check element declaration is not already added (ex: xs:annotation)
			if (!elements.contains(cmElement)) {
				elements.add(cmElement);
			}
		}
		
		XSObjectList list = model.getSubstitutionGroup(elementDeclaration);
		if (list != null) {
			// it exists elements list bind with this abstract declaration with
			// substitutionGroup
			// ex xsl : <xs:element name="template" substitutionGroup="xsl:declaration">
			for (int i = 0; i < list.getLength(); i++) {
				XSObject object = list.item(i);
				if (object.getType() == XSConstants.ELEMENT_DECLARATION) {
					XSElementDeclaration subElementDeclaration = (XSElementDeclaration) object;
					collectElement(subElementDeclaration, elements);
				}
			}
		}
		
	}

	@Override
	public CMElementDeclaration findCMElement(DOMElement element, String namespace) {
		List<DOMElement> paths = new ArrayList<>();
		while (element != null && (namespace == null || namespace.equals(element.getNamespaceURI()))) {
			paths.add(0, element);
			element = element.getParentNode() instanceof DOMElement ? (DOMElement) element.getParentNode() : null;
		}
		CMElementDeclaration declaration = null;
		for (int i = 0; i < paths.size(); i++) {
			DOMElement elt = paths.get(i);
			if (i == 0) {
				declaration = findElementDeclaration(elt.getLocalName(), namespace);
			} else {
				declaration = declaration.findCMElement(elt.getLocalName(), namespace);
			}
			if (declaration == null) {
				break;
			}
		}
		return declaration;
	}

	private CMElementDeclaration findElementDeclaration(String tag, String namespace) {
		for (CMElementDeclaration cmElement : getElements()) {
			if (cmElement.getName().equals(tag)) {
				return cmElement;
			}
		}
		return null;
	}

	CMElementDeclaration getXSDElement(XSElementDeclaration elementDeclaration) {
		CMXSDElementDeclaration element = elementMappings.get(elementDeclaration);
		if (element == null) {
			element = new CMXSDElementDeclaration(this, elementDeclaration);
			elementMappings.put(elementDeclaration, element);
		}
		return element;
	}

	static Collection<String> getEnumerationValues(XSSimpleTypeDefinition typeDefinition) {
		if (typeDefinition != null) {
			if (isBooleanType(typeDefinition)) {
				return StringUtils.TRUE_FALSE_ARRAY;
			}
			StringList enumerations = typeDefinition.getLexicalEnumeration();
			if (enumerations != null) {
				return enumerations;
			}
		}
		return Collections.emptyList();
	}

	static boolean isBooleanType(XSSimpleTypeDefinition typeDefinition) {
		if (typeDefinition instanceof XSSimpleType) {
			return ((XSSimpleType) typeDefinition).getPrimitiveKind() == XSSimpleType.PRIMITIVE_BOOLEAN;
		}
		return false;
	}
}
