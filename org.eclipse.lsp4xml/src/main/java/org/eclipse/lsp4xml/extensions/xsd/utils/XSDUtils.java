/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.extensions.xsd.utils;

import java.util.function.BiConsumer;

import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.utils.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Objects;

/**
 * XSD utilities.
 * 
 * @author Angelo ZERR
 *
 */
public class XSDUtils {

	/**
	 * Binding type of xs attribute.
	 *
	 */
	public enum BindingType {

		COMPLEX, SIMPLE, COMPLEX_AND_SIMPLE, NONE, REF, ELEMENT;

		public boolean isSimple() {
			return BindingType.COMPLEX_AND_SIMPLE.equals(this) || BindingType.SIMPLE.equals(this);
		}

		public boolean isComplex() {
			return BindingType.COMPLEX_AND_SIMPLE.equals(this) || BindingType.COMPLEX.equals(this);
		}
	}

	/**
	 * Returns the binding type of the given attribute.
	 * 
	 * @param attr the attribute
	 * @return the binding type of the given attribute.
	 */
	public static BindingType getBindingType(DOMAttr attr) {
		String name = attr.getName();
		if ("type".equals(name)) {
			if ("attribute".equals(attr.getOwnerElement().getLocalName())) {
				// - <xs:attribute type="
				return BindingType.SIMPLE;
			}
			// - <xs:element type="
			return BindingType.COMPLEX_AND_SIMPLE;
		}
		if ("base".equals(name)) {
			// - <xs:restriction base="
			// - <xs:extension base="
			DOMElement element = attr.getOwnerElement();
			DOMElement parent = element.getParentElement();
			if (parent != null) {
				if (parent.getLocalName().equals("complexContent") | isComplexType(parent)) {
					// parent element is complexContent or complexType -> bounded type is complex
					return BindingType.COMPLEX;
				}
				if (parent.getLocalName().equals("simpleContent") || isSimpleType(parent)) {
					// parent element is simpleContent or simpleType -> bounded type is simple
					return BindingType.SIMPLE;
				}
			}
			return BindingType.NONE;
		}
		if ("ref".equals(name)) {
			// - <xs:element ref="
			// - <xs:group ref="
			return BindingType.REF;
		}
		if ("itemType".equals(name)) {
			// - <xs:list itemType="
			return BindingType.COMPLEX_AND_SIMPLE;
		}
		if ("memberTypes".equals(name)) {
			// - <xs:union memberTypes="
			return BindingType.COMPLEX_AND_SIMPLE;
		}
		if ("substitutionGroup".equals(name)) {
			// - <xs:element substitutionGroup
			return BindingType.ELEMENT;
		}
		return BindingType.NONE;
	}

	/**
	 * Collect XSD types declared in the XML Schema according the given attribute
	 * and binding type.
	 * 
	 * - xs:complexType/@name attributes - xs:simpleType/@name attributes -
	 * xs:element/@name attributes if attribute is xs:element/@ref - xs:group/@name
	 * attributes if attribute is xs:group/@ref
	 * 
	 * @param originAttr the origin attribute.
	 * @param matchAttr  true if the attribute value must match the value of
	 *                   complexType/@name and false otherwise.
	 * @param collector  collector to collect XSD types attributes.
	 */
	public static void collectXSTypes(DOMAttr originAttr, BindingType bindingType, boolean matchAttr,
			BiConsumer<String, DOMAttr> collector) {
		if (bindingType == BindingType.NONE) {
			return;
		}

		DOMDocument document = originAttr.getOwnerDocument();
		DOMElement documentElement = document != null ? document.getDocumentElement() : null;
		if (documentElement == null) {
			return;
		}
		String attrValue = originAttr.getValue();
		if (matchAttr && StringUtils.isEmpty(attrValue)) {
			return;
		}

		// <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
		// xmlns:tns="http://camel.apache.org/schema/spring"
		// targetNamespace="http://camel.apache.org/schema/spring" version="1.0">
		String targetNamespace = documentElement.getAttribute("targetNamespace"); // ->
																					// http://camel.apache.org/schema/spring
		String targetNamespacePrefix = documentElement.getPrefix(targetNamespace); // -> tns

		String matchAttrName = null;
		if (matchAttr) {
			matchAttrName = attrValue;
			String prefix = null;
			int index = attrValue.indexOf(":");
			if (index != -1) {
				prefix = attrValue.substring(0, index);
				if (!Objects.equal(prefix, targetNamespacePrefix)) {
					return;
				}
				matchAttrName = attrValue.substring(index + 1, attrValue.length());
			}
		}

		// Loop for element complexType.
		NodeList children = documentElement.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element targetElement = (Element) node;
				if (canCollectElement(originAttr, targetElement, bindingType)) {
					// node is a xs:complexType, xs:simpleType element, xsl:element, xs:group which
					// matches the binding type of the originAttr
					DOMAttr targetAttr = (DOMAttr) targetElement.getAttributeNode("name");
					if (targetAttr != null && (!matchAttr || matchAttrName.equals(targetAttr.getValue()))) {
						collector.accept(targetNamespacePrefix, targetAttr);
					}
				}
			}
		}
	}

	private static boolean canCollectElement(DOMAttr originAttr, Element targetElement, BindingType bindingType) {
		if (isComplexType(targetElement)) {
			return bindingType.isComplex();
		} else if (isSimpleType(targetElement)) {
			return bindingType.isSimple();
		} else if (bindingType == BindingType.REF) {
			// - xs:element/@name attributes if originAttr is xs:element/@ref
			// - xs:group/@name attributes if originAttr is xs:group/@ref
			return (originAttr.getOwnerElement().getLocalName().equals(targetElement.getLocalName()));
		} else if (bindingType == BindingType.ELEMENT) {
			return "element".equals(targetElement.getLocalName());
		}
		return false;
	}

	public static boolean isComplexType(Element element) {
		return "complexType".equals(element.getLocalName());
	}

	public static boolean isSimpleType(Element element) {
		return "simpleType".equals(element.getLocalName());
	}

}
