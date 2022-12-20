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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
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
	 * {@link XMLReferenceExpression}.
	 *
	 * @param fromAttr               the from attribute.
	 * @param matchAttr              true if the attribute value must match the
	 *                               value of to attribute value and false
	 *                               otherwise.
	 * @param searchInExternalSchema true if search must be done in included XML
	 *                               Schema (include) and false otherwise.
	 * @param collector              collector to collect to attributes.
	 */
	public static void searchToAttributes(DOMAttr fromAttr, List<XMLReferenceExpression> referenceExpressions,
			boolean matchAttr,
			boolean searchInExternalSchema, IXMLReferenceTosCollector collector) {

		DOMDocument document = fromAttr.getOwnerDocument();
		DOMElement documentElement = document != null ? document.getDocumentElement() : null;
		if (documentElement == null) {
			return;
		}
		String fromAttrValue = fromAttr.getValue();
		if (matchAttr && StringUtils.isEmpty(fromAttrValue)) {
			return;
		}
		String namespacePrefix = null;
		int index = fromAttrValue.indexOf(':');
		if (index != -1) {
			// ex : jakartaee:applicationType
			namespacePrefix = fromAttrValue.substring(0, index);
		}

		String fromName = null;
		if (matchAttr) {
			fromName = getFromName(fromAttrValue, namespacePrefix);
		}

		// Collect attributes to for document element
		collectToAttribute(fromAttr, referenceExpressions, matchAttr, collector, documentElement,
				namespacePrefix);

		// Collect attributes to for children of document element
		searchToAttributes(fromAttr, referenceExpressions, matchAttr, collector, documentElement,
				namespacePrefix,
				fromName, new HashSet<>(), searchInExternalSchema);
	}

	private static void searchToAttributes(DOMAttr fromAttr, List<XMLReferenceExpression> referenceExpressions,
			boolean matchAttr,
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
				collectToAttribute(fromAttr, referenceExpressions, matchAttr, collector, toElement,
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
					searchToAttributes(fromAttr, referenceExpressions, matchAttr, collector, toElement,
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
						searchToAttributes(fromAttr, referenceExpressions, matchAttr, collector,
								externalDocument.getDocumentElement(), toNamespacePrefix, fromName, visitedURIs,
								searchInExternalDocument);
					}
				}
			}
		}
	}

	private static void collectToAttribute(DOMAttr fromAttr, List<XMLReferenceExpression> referenceExpressions,
			boolean matchAttr, IXMLReferenceTosCollector collector, DOMElement toElement,
			String toNamespacePrefix) {
		if (toElement.hasAttributes()) {
			NamedNodeMap toAttributes = toElement.getAttributes();
			if (toAttributes != null) {
				for (int j = 0; j < toAttributes.getLength(); j++) {
					DOMAttr toAttr = (DOMAttr) toAttributes.item(j);
					XMLReferenceExpression expression = findExpressionWhichMatchesTo(fromAttr, toAttr,
							referenceExpressions,
							matchAttr);
					if (expression != null) {
						collector.collect(toNamespacePrefix, toAttr, expression);
					}
				}
			}
		}
	}

	/**
	 * Returns the reference expression where the given attributes fromAttr and
	 * toAttr matches an expression from the given referenceExpressions.
	 * 
	 * @param fromAttr             the from attribute.
	 * @param toAttr               the to attribute.
	 * @param referenceExpressions reference expressions which matches the from
	 *                             attribute.
	 * @param matchAttr            true if the test of the value of from and to
	 *                             attribute must be checked and false otherwise.
	 * @return the reference expression where the given attributes fromAttr and
	 *         toAttr matches an expression from the given referenceExpressions.
	 */
	private static XMLReferenceExpression findExpressionWhichMatchesTo(DOMAttr fromAttr, DOMAttr toAttr,
			List<XMLReferenceExpression> referenceExpressions, boolean matchAttr) {
		for (XMLReferenceExpression expression : referenceExpressions) {
			if (toAttr.getValue() != null && expression.matchTo(toAttr)) {
				// The current expression can be applied for the to attribute.
				if (!matchAttr) {
					// No need to match the attribute value
					return expression;
				}
				// The expression is returned only if the from attribute value is equals to to
				// attribute value.
				String fromValue = fromAttr.getValue();
				String prefix = expression.getPrefix();
				if (prefix != null) {
					if (!fromValue.startsWith(prefix)) {
						continue;
					}
					fromValue = fromValue.substring(prefix.length(), fromValue.length());
				}
				if (fromValue.equals(toAttr.getValue())) {
					return expression;
				}
			}
		}
		return null;
	}

	public static boolean isInclude(Element element) {
		return element != null && INCLUDE_TAG.equals(element.getLocalName());
	}

	private static String getFromName(String fromAttrValue, String toNamespacePrefix) {
		int index = fromAttrValue.indexOf(":");
		if (index != -1) {
			String prefix = fromAttrValue.substring(0, index);
			if (!Objects.equal(prefix, toNamespacePrefix)) {
				return null;
			}
			return fromAttrValue.substring(index + 1, fromAttrValue.length());
		}
		return fromAttrValue;
	}

	/**
	 * Returns all XML reference expressions where the given attribute matches the
	 * from
	 * expression and an empty list otherwise.
	 * 
	 * @param attr                  the DOM attribute.
	 * 
	 * @param xmlReferencesSettings the XML references settings.
	 * @return all XML reference expressions where the given attribute matches the
	 *         from
	 *         expression and an empty list otherwise.
	 */
	public static List<XMLReferenceExpression> findExpressionsWhichMatcheFrom(DOMAttr attr,
			XMLReferencesSettings xmlReferencesSettings) {
		List<XMLReferences> allReferences = xmlReferencesSettings != null ? xmlReferencesSettings.getReferences()
				: null;
		if (allReferences == null) {
			return Collections.emptyList();
		}
		String uri = attr.getOwnerDocument().getDocumentURI();
		List<XMLReferenceExpression> matchedExpressions = new ArrayList<>();
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
						if (expression.matchFrom(attr)) {
							// here the attribute matches xref/@linkend
							matchedExpressions.add(expression);
						}
					}
				}
			}
		}
		return matchedExpressions;
	}
}
