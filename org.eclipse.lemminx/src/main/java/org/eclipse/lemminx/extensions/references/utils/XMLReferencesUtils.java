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
package org.eclipse.lemminx.extensions.references.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.dom.DOMRange;
import org.eclipse.lemminx.dom.DOMText;
import org.eclipse.lemminx.extensions.references.settings.XMLReferenceExpression;
import org.eclipse.lemminx.extensions.references.settings.XMLReferences;
import org.eclipse.lemminx.extensions.references.settings.XMLReferencesSettings;
import org.eclipse.lemminx.uriresolver.URIResolverExtensionManager;
import org.eclipse.lemminx.utils.DOMUtils;
import org.eclipse.lemminx.utils.StringUtils;
import org.eclipse.lemminx.utils.URIUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Objects;

/**
 * XML references utilities.
 * 
 * @author Angelo ZERR
 *
 */
public class XMLReferencesUtils {

	private static final String INCLUDE_TAG = "include";

	private static final String HREF_ATTR = "href";

	/**
	 * Collect 'to' attributes which matches 'to' declared in
	 * {@link XMLReferencesSearchContext} which contains a list of
	 * {@link XMLReferenceExpression}.
	 *
	 * @param fromNode             the from attribute, text node.
	 * @param searchContext        the references search context.
	 * @param matchNode            true if the value of the from and to nodes must
	 *                             be checked, and false otherwise.
	 * @param searchInIncludedFile true if search must be done in included XML
	 *                             file (ex : xi:include) and false otherwise.
	 * @param collector            collector to collect to attribute, text nodes.
	 */
	public static void searchToNodes(DOMNode fromNode, XMLReferencesSearchContext searchContext,
			boolean matchNode, boolean searchInIncludedFile, IXMLReferenceTosCollector collector) {

		DOMDocument document = fromNode.getOwnerDocument();
		DOMElement documentElement = document != null ? document.getDocumentElement() : null;
		if (documentElement == null) {
			return;
		}
		String fromValue = getNodeValue(fromNode);
		if (matchNode && StringUtils.isEmpty(fromValue)) {
			return;
		}
		String namespacePrefix = null;
		int index = fromValue.indexOf(':');
		if (index != -1) {
			// ex : jakartaee:applicationType
			namespacePrefix = fromValue.substring(0, index);
		}

		String fromName = null;
		if (matchNode) {
			fromName = getFromName(fromValue, namespacePrefix);
		}

		// Collect attribute, text nodes to for document element
		collectToNode(fromNode, searchContext, matchNode, collector, documentElement,
				namespacePrefix);

		// Collect attribute, text nodes to for children of document element
		searchToNodes(fromNode, searchContext, matchNode, collector, documentElement,
				namespacePrefix,
				fromName, new HashSet<>(), searchInIncludedFile);
	}

	private static void searchToNodes(DOMNode fromNode, XMLReferencesSearchContext referenceSearcher,
			boolean matchNode,
			IXMLReferenceTosCollector collector, DOMElement documentElement, String toNamespacePrefix,
			String fromName, Set<String> visitedURIs, boolean searchInExternalDocument) {
		if (visitedURIs != null) {
			DOMDocument document = documentElement.getOwnerDocument();
			String documentURI = document.getDocumentURI();
			if (visitedURIs.contains(documentURI)) {
				return;
			}
			visitedURIs.add(documentURI);
		}
		Set<String> externalURIS = null;
		Node parent = documentElement;
		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				DOMElement toElement = (DOMElement) node;
				collectToNode(fromNode, referenceSearcher, matchNode, collector, toElement,
						toNamespacePrefix);

				if (isInclude(toElement)) {
					// collect xi:include
					String schemaLocation = toElement.getAttribute(HREF_ATTR);
					if (schemaLocation != null) {
						if (externalURIS == null) {
							externalURIS = new HashSet<>();
						}
						externalURIS.add(schemaLocation);
					}
				} else {
					searchToNodes(fromNode, referenceSearcher, matchNode, collector, toElement,
							toNamespacePrefix, fromName, null, false);
				}
			}
		}
		if (searchInExternalDocument && externalURIS != null) {
			// Search in include location
			DOMDocument document = documentElement.getOwnerDocument();
			String documentURI = document.getDocumentURI();
			URIResolverExtensionManager resolverExtensionManager = document.getResolverExtensionManager();
			for (String externalURI : externalURIS) {
				String resourceURI = resolverExtensionManager.resolve(documentURI, null, externalURI);
				if (URIUtils.isFileResource(resourceURI)) {
					DOMDocument externalDocument = DOMUtils.loadDocument(resourceURI,
							document.getResolverExtensionManager());
					if (externalDocument != null) {
						searchToNodes(fromNode, referenceSearcher, matchNode, collector,
								externalDocument.getDocumentElement(), toNamespacePrefix, fromName, visitedURIs,
								searchInExternalDocument);
					}
				}
			}
		}
	}

	private static void collectToNode(DOMNode fromNode, XMLReferencesSearchContext searchContext,
			boolean matchNode, IXMLReferenceTosCollector collector, DOMElement toElement,
			String toNamespacePrefix) {
		if (searchContext.isSearchInAttribute()) {
			// Search to reference in attribute nodes
			if (toElement.hasAttributes()) {
				NamedNodeMap toAttributes = toElement.getAttributes();
				if (toAttributes != null) {
					for (int j = 0; j < toAttributes.getLength(); j++) {
						DOMAttr toAttr = (DOMAttr) toAttributes.item(j);
						XMLReferenceExpression expression = findExpressionWhichMatchesTo(fromNode, toAttr,
								searchContext,
								matchNode);
						if (expression != null) {
							collector.collect(toNamespacePrefix, toAttr, expression);
						}
					}
				}
			}
		}
		if (searchContext.isSearchInText()) {
			// Search to reference in text node.
			DOMNode firstChild = toElement.getFirstChild();
			if (firstChild != null && firstChild.isText()) {
				XMLReferenceExpression expression = findExpressionWhichMatchesTo(fromNode, firstChild,
						searchContext,
						matchNode);
				if (expression != null) {
					collector.collect(toNamespacePrefix, firstChild, expression);
				}
			}
		}
	}

	/**
	 * Returns the reference expression where the given attributes
	 * <code>fromNode</code> and
	 * <code>toNode</code> matches an expression from the given
	 * referenceSearcher.
	 * 
	 * @param fromNode      the from attribute, text node.
	 * @param toNode        the to attribute, text node.
	 * @param searchContext reference searcher which matches the from
	 *                      node.
	 * @param matchNode     true if the test of the value of from and to
	 *                      attribute, text must be checked and false
	 *                      otherwise.
	 * @return the reference expression where the given attributes
	 *         <code>fromNode</code> and
	 *         <code>toNode</code> matches an expression from the given
	 *         referenceSearcher.
	 */
	private static XMLReferenceExpression findExpressionWhichMatchesTo(DOMNode fromNode, DOMNode toNode,
			XMLReferencesSearchContext searchContext, boolean matchNode) {
		for (XMLReferenceExpression expression : searchContext.getExpressions()) {
			if (expression.matchTo(toNode)) {
				// The current expression can be applied for the to attribute.
				if (!matchNode) {
					// No need to match the attribute value
					return expression;
				}
				// The expression is returned only if the from attribute value, text content is
				// equals to to
				// attribute value, text content.
				String fromValue = getNodeValue(fromNode);
				if (fromValue != null) {
					String prefix = expression.getPrefix();
					if (prefix != null) {
						if (!fromValue.startsWith(prefix)) {
							continue;
						}
						fromValue = fromValue.substring(prefix.length(), fromValue.length());
					}
					if (fromValue.equals(getNodeValue(toNode))) {
						return expression;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the value of the given DOM node and null otherwise.
	 * 
	 * @param node the DOM node.
	 * 
	 * @return the value of the given DOM node and null otherwise.
	 */
	public static String getNodeValue(DOMNode node) {
		if (node.isAttribute()) {
			return ((DOMAttr) node).getValue();
		}
		if (node.isText()) {
			return ((DOMText) node).getData();
		}
		return null;
	}

	/**
	 * Returns the range of the given DOM node.
	 * 
	 * @param node the DOM node.
	 * 
	 * @return the range of the given DOM node.
	 */
	public static DOMRange getNodeRange(DOMNode node) {
		if (node.isAttribute()) {
			return ((DOMAttr) node).getNodeAttrValue();
		}
		return node;
	}

	/**
	 * Returns true if the given element is an include element (ex : xi:include) and
	 * false otherwise.
	 * 
	 * @param element the DOM element.
	 * 
	 * @return true if the given element is an include element (ex : xi:include) and
	 *         false otherwise.
	 */
	private static boolean isInclude(Element element) {
		return element != null && INCLUDE_TAG.equals(element.getLocalName());
	}

	private static String getFromName(String fromValue, String toNamespacePrefix) {
		int index = fromValue.indexOf(":");
		if (index != -1) {
			String prefix = fromValue.substring(0, index);
			if (!Objects.equal(prefix, toNamespacePrefix)) {
				return null;
			}
			return fromValue.substring(index + 1, fromValue.length());
		}
		return fromValue;
	}

	/**
	 * Returns all XML reference expressions where the given DOM <code>node</code>
	 * matches the
	 * from expression and null otherwise.
	 * 
	 * @param node                  the DOM attribute.
	 * 
	 * @param xmlReferencesSettings the XML references settings.
	 * @return all XML reference expressions where the given attribute matches the
	 *         from
	 *         expression and an empty list otherwise.
	 */
	public static XMLReferencesSearchContext findExpressionsWhichMatchFrom(DOMNode node,
			XMLReferencesSettings xmlReferencesSettings) {
		List<XMLReferences> allReferences = xmlReferencesSettings != null ? xmlReferencesSettings.getReferences()
				: null;
		if (allReferences == null) {
			return null;
		}
		if (node == null || (!node.isAttribute() && !node.isText())) {
			return null;
		}
		String uri = node.getOwnerDocument().getDocumentURI();
		List<XMLReferenceExpression> matchedExpressions = null;
		for (XMLReferences references : allReferences) {
			// Given this XML references sample

			/**
			 * <code>
			 * "xml.references": [
			 * // references for docbook.xml files
			 * {
			 *   "pattern": "*.xml",
			 *   "expressions": [
			 *     {
			 *       "from": "xref/@linkend",
			 *       "to": "@id"
			 *     }
			 *   ]
			 * }
			 *]
			 * 
			 * </code>
			 *
			 */
			if (references.matches(uri)) {
				// here uri matches the "*.xml" pattern
				List<XMLReferenceExpression> expressions = references.getExpressions();
				if (expressions != null) {
					for (XMLReferenceExpression expression : expressions) {
						// Given this XML reference expression sample
						/**
						 * <code>
						 * "expressions": [
						 *   {
						 *     "from": "xref/@linkend",
						 *     "to": "@id"
						 *   }
						 * ]
						 * 
						 * </code>
						 *
						 */
						if (expression.matchFrom(node)) {
							// here the attribute matches xref/@linkend
							if (matchedExpressions == null) {
								matchedExpressions = new ArrayList<>();
							}
							matchedExpressions.add(expression);
						}
					}
				}
			}
		}
		if (matchedExpressions == null) {
			return null;
		}
		return new XMLReferencesSearchContext(matchedExpressions, true);
	}
}
