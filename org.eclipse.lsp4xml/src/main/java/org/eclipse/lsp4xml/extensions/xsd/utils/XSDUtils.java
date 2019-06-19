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
	 * Returns true if the given attribute is bound to complexType/@name attribute
	 * and false otherwise.
	 * 
	 * @param attr the attribute
	 * @return true if the given attribute is bound to complexType/@name attribute
	 *         and false otherwise.
	 */
	public static boolean isBoundToComplexTypes(DOMAttr attr) {
		if (attr == null) {
			return false;
		}
		if ("type".equals(attr.getName()) && "element".equals(attr.getOwnerElement().getLocalName())) {
			// - xs:element/@type -> xs:complexType/@name
			return true;
		}
		if ("base".equals(attr.getName()) && "extension".equals(attr.getOwnerElement().getLocalName())) {
			// - xs:extension/@base -> xs:complexType/@name
			return true;
		}
		return false;
	}

	/**
	 * Collect complexType/@name attributes declared inside the document of the
	 * given attribute.
	 * 
	 * @param originAttr the origin attribute.
	 * @param matchAttr  true if the attribute value must match the value of
	 *                   complexType/@name and false otherwise.
	 * @param collector  collector to collect complexType/@name attributes.
	 */
	public static void collectComplexTypes(DOMAttr originAttr, boolean matchAttr,
			BiConsumer<String, DOMAttr> collector) {
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

		String complexTypeName = attrValue;
		String prefix = null;
		int index = attrValue.indexOf(":");
		if (index != -1) {
			prefix = attrValue.substring(0, index);
			if (!Objects.equal(prefix, targetNamespacePrefix)) {
				return;
			}
			complexTypeName = attrValue.substring(index + 1, attrValue.length());
		}

		// Loop for element complexType.
		NodeList children = documentElement.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				if ("complexType".equals(element.getLocalName())) {
					// node is a complexType element
					// get the attribute complexType/@name
					DOMAttr targetAttr = (DOMAttr) element.getAttributeNode("name");
					if (!matchAttr || complexTypeName.equals(targetAttr.getValue())) {
						collector.accept(targetNamespacePrefix, targetAttr);
					}
				}
			}
		}
	}

}
