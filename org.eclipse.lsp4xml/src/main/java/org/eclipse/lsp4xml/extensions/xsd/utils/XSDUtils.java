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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.utils.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
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
	 * Returns the binding type of the origin attribute which bounds an another
	 * target attribute.
	 * 
	 * @param originAttr the origin attribute
	 * @return the binding type of the origin attribute which bounds an another
	 *         target attribute.
	 */
	public static BindingType getBindingType(DOMAttr originAttr) {
		if (originAttr != null) {
			String name = originAttr.getName();
			if ("type".equals(name)) {
				if ("attribute".equals(originAttr.getOwnerElement().getLocalName())) {
					// - <xs:attribute type="
					return BindingType.SIMPLE;
				}
				// - <xs:element type="
				return BindingType.COMPLEX_AND_SIMPLE;
			}
			if ("base".equals(name)) {
				// - <xs:restriction base="
				// - <xs:extension base="
				DOMElement element = originAttr.getOwnerElement();
				DOMElement parent = element.getParentElement();
				if (parent != null) {
					if (parent.getLocalName().equals("complexContent") || isXSComplexType(parent)) {
						// parent element is complexContent or complexType -> bounded type is complex
						return BindingType.COMPLEX;
					}
					if (parent.getLocalName().equals("simpleContent") || isXSSimpleType(parent)) {
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
		}
		return BindingType.NONE;
	}

	/**
	 * Collect XSD target attributes declared in the XML Schema according the given
	 * attribute and binding type.
	 * 
	 * @param originAttr the origin attribute.
	 * @param matchAttr  true if the attribute value must match the value of target
	 *                   attribute value and false otherwise.
	 * @param collector  collector to collect XSD target attributes.
	 */
	public static void searchXSTargetAttributes(DOMAttr originAttr, BindingType bindingType, boolean matchAttr,
			BiConsumer<String, DOMAttr> collector) {
		if (bindingType == BindingType.NONE) {
			return;
		}

		DOMDocument document = originAttr.getOwnerDocument();
		DOMElement documentElement = document != null ? document.getDocumentElement() : null;
		if (documentElement == null) {
			return;
		}
		String originAttrValue = originAttr.getValue();
		if (matchAttr && StringUtils.isEmpty(originAttrValue)) {
			return;
		}

		// <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
		// xmlns:tns="http://camel.apache.org/schema/spring"
		// targetNamespace="http://camel.apache.org/schema/spring" version="1.0">
		String targetNamespace = documentElement.getAttribute("targetNamespace"); // ->
																					// http://camel.apache.org/schema/spring
		String targetNamespacePrefix = documentElement.getPrefix(targetNamespace); // -> tns

		String originName = null;
		if (matchAttr) {
			originName = getOriginName(originAttrValue, targetNamespacePrefix);
		}

		// Loop for element complexType.
		NodeList children = documentElement.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element targetElement = (Element) node;
				if (isBounded(originAttr.getOwnerElement(), bindingType, targetElement)) {
					// node is a xs:complexType, xs:simpleType element, xsl:element, xs:group which
					// matches the binding type of the originAttr
					DOMAttr targetAttr = (DOMAttr) targetElement.getAttributeNode("name");
					if (targetAttr != null && (!matchAttr || Objects.equal(originName, targetAttr.getValue()))) {
						collector.accept(targetNamespacePrefix, targetAttr);
					}
				}
			}
		}
	}

	private static String getOriginName(String originAttrValue, String targetNamespacePrefix) {
		int index = originAttrValue.indexOf(":");
		if (index != -1) {
			String prefix = originAttrValue.substring(0, index);
			if (!Objects.equal(prefix, targetNamespacePrefix)) {
				return null;
			}
			return originAttrValue.substring(index + 1, originAttrValue.length());
		}
		return originAttrValue;
	}

	private static boolean isBounded(Element originElement, BindingType originBinding, Element targetElement) {
		if (isXSComplexType(targetElement)) {
			return originBinding.isComplex();
		} else if (isXSSimpleType(targetElement)) {
			return originBinding.isSimple();
		} else if (originBinding == BindingType.REF) {
			// - xs:element/@name attributes if originAttr is xs:element/@ref
			// - xs:group/@name attributes if originAttr is xs:group/@ref
			return (originElement.getLocalName().equals(targetElement.getLocalName()));
		} else if (originBinding == BindingType.ELEMENT) {
			return isXSElement(targetElement);
		}
		return false;
	}

	/**
	 * Search origin attributes from the given target node..
	 * 
	 * @param targetNode the referenced node
	 * @param collector  the collector to collect reference between an origin and
	 *                   target attribute.
	 */
	public static void searchXSOriginAttributes(DOMNode targetNode, BiConsumer<DOMAttr, DOMAttr> collector,
			CancelChecker cancelChecker) {
		// get referenced attribute nodes from the given referenced node
		List<DOMAttr> targetAttrs = getTargetAttrs(targetNode);
		if (targetAttrs.isEmpty()) {
			// None referenced nodes, stop the search of references
			return;
		}

		// Here referencedNodes is filled with a list of attributes
		// xs:complexType/@name,
		// xs:simpleType/@name, xs:element/@name, xs:group/@name

		DOMDocument document = targetNode.getOwnerDocument();
		DOMElement documentElement = document.getDocumentElement();

		// <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
		// xmlns:tns="http://camel.apache.org/schema/spring"
		// targetNamespace="http://camel.apache.org/schema/spring" version="1.0">
		String targetNamespace = documentElement.getAttribute("targetNamespace"); // ->
																					// http://camel.apache.org/schema/spring
		String targetNamespacePrefix = documentElement.getPrefix(targetNamespace); // -> tns

		// Collect references for each references nodes

		NodeList nodes = documentElement.getChildNodes();
		searchXSOriginAttributes(nodes, targetAttrs, targetNamespacePrefix, collector, cancelChecker);
	}

	/**
	 * Returns the referenced attributes list from the given referenced node.
	 * 
	 * @param referencedNode the referenced node.
	 * @return the referenced attributes list from the given referenced node.
	 */
	private static List<DOMAttr> getTargetAttrs(DOMNode referencedNode) {
		List<DOMAttr> referencedNodes = new ArrayList<>();
		Document document = referencedNode.getOwnerDocument();
		switch (referencedNode.getNodeType()) {
		case Node.ATTRIBUTE_NODE:
			// The referenced node is an attribute, add it to search references from it.
		case Node.ELEMENT_NODE:
			// The referenced node is an element, get the attribute name) and add it to
			// search references from it.
			addTargetNode(referencedNode, referencedNodes);
			break;
		case Node.DOCUMENT_NODE:
			// The referenced node is the DOM document, collect all attributes
			// xs:complexType/@name, xs:simpleType/@name, xs:element/@name, xs:group/@name
			// which can be referenced
			NodeList nodes = document.getDocumentElement().getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node n = nodes.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					DOMElement element = (DOMElement) n;
					if (isXSTargetElement(element)) {
						addTargetNode(element, referencedNodes);
					}
				}
			}
		}
		return referencedNodes;
	}

	/**
	 * Add the given node as reference node if it is applicable.
	 * 
	 * @param node        the node to add.
	 * @param targetAttrs the list of referenced nodes.
	 */
	private static void addTargetNode(DOMNode node, List<DOMAttr> targetAttrs) {
		DOMAttr attr = null;
		switch (node.getNodeType()) {
		case Node.ATTRIBUTE_NODE:
			attr = (DOMAttr) node;
			break;
		case Node.ELEMENT_NODE:
			attr = ((DOMElement) node).getAttributeNode("name");
			break;
		}
		// Attribute must exists and her value must be not empty.
		if (attr != null && !StringUtils.isEmpty(attr.getValue())) {
			targetAttrs.add(attr);
		}
	}

	private static void searchXSOriginAttributes(NodeList nodes, List<DOMAttr> targetAttrs,
			String targetNamespacePrefix, BiConsumer<DOMAttr, DOMAttr> collector, CancelChecker cancelChecker) {
		for (int i = 0; i < nodes.getLength(); i++) {
			if (cancelChecker != null) {
				cancelChecker.checkCanceled();
			}
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				DOMElement originElement = (DOMElement) node;
				NamedNodeMap originAttributes = originElement.getAttributes();
				if (originAttributes != null) {
					for (int j = 0; j < originAttributes.getLength(); j++) {
						DOMAttr originAttr = (DOMAttr) originAttributes.item(j);
						BindingType originBnding = XSDUtils.getBindingType(originAttr);
						if (originBnding != BindingType.NONE) {
							String originName = getOriginName(originAttr.getValue(), targetNamespacePrefix);
							for (DOMAttr targetAttr : targetAttrs) {
								Element targetElement = targetAttr.getOwnerElement();
								if (isBounded(originAttr.getOwnerElement(), originBnding, targetElement)) {
									// node is a xs:complexType, xs:simpleType element, xsl:element, xs:group which
									// matches the binding type of the originAttr
									if (targetAttr != null && (Objects.equal(originName, targetAttr.getValue()))) {
										collector.accept(originAttr, targetAttr);
									}
								}
							}
						}
					}
				}
			}
			if (node.hasChildNodes()) {
				searchXSOriginAttributes(node.getChildNodes(), targetAttrs, targetNamespacePrefix, collector,
						cancelChecker);
			}
		}

	}

	public static boolean isXSComplexType(Element element) {
		return "complexType".equals(element.getLocalName());
	}

	public static boolean isXSSimpleType(Element element) {
		return "simpleType".equals(element.getLocalName());
	}

	public static boolean isXSElement(Element element) {
		return "element".equals(element.getLocalName());
	}

	public static boolean isXSGroup(Element element) {
		return "group".equals(element.getLocalName());
	}

	public static boolean isXSTargetElement(Element element) {
		return isXSComplexType(element) || isXSSimpleType(element) || isXSElement(element) || isXSGroup(element);
	}
}
