/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package org.eclipse.lemminx.extensions.catalog;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.utils.DOMUtils;

/**
 * Utility functions for working with XML catalog documents
 */
public class CatalogUtils {

	/**
	 * The catalog entries that have a 'uri' attribute
	 */
	private static final Collection<String> CATALOG_NAMES = Arrays.asList("public", "system", "uri", "systemSuffix",
			"uriSuffix");

	private static final String CATALOG_ENTITY_NAME = "catalog";

	private static final String URI_ATTRIBUTE_NAME = "uri";

	/**
	 * Returns a list of all the catalog entries that have the attribute 'uri', or
	 * an empty list if the document is not a catalog.
	 *
	 * @param document The document to collect the catalog entries from
	 * @return A list of all the catalog entries that have the attribute 'uri', or
	 *         an empty list if the document is not a catalog.
	 */
	public static List<DOMElement> getCatalogEntries(DOMDocument document) {
		if (!DOMUtils.isCatalog(document)) {
			return Collections.emptyList();
		}
		for (DOMNode n : document.getChildren()) {
			if (CATALOG_ENTITY_NAME.equals(n.getNodeName())) {
				return n.getChildren().stream().filter(CatalogUtils::isCatalogURIEntry).map((el) -> {
					return (DOMElement) el;
				}).collect(Collectors.toList());
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Returns the uri attribute node of the given catalog entry or null if there is
	 * no uri attribute
	 *
	 * @param element The catalog entry to get the uri attribute of
	 * @return the uri attribute node of the given catalog entry or null if there is
	 *         no uri attribute
	 */
	public static DOMAttr getCatalogEntryURI(DOMElement element) {
		return element.getAttributeNode(URI_ATTRIBUTE_NAME);
	}

	/**
	 * Checks if this node is a catalog entry that is required to have the 'uri'
	 * attribute
	 *
	 * @param node The node to check
	 * @return true if this node is an catalog entry that is required to have the
	 *         'uri' attribute and false otherwise
	 */
	private static boolean isCatalogURIEntry(DOMNode node) {
		return node.isElement() && CATALOG_NAMES.contains(node.getNodeName());
	}

}