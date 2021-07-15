/*******************************************************************************
* Copyright (c) 2018 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.utils;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMParser;
import org.eclipse.lemminx.uriresolver.URIResolverExtensionManager;

/**
 * DOM Utilities.
 *
 */
public class DOMUtils {

	private static final Logger LOGGER = Logger.getLogger(DOMUtils.class.getName());

	private static final String XSD_EXTENSION = ".xsd";

	// DTD file extensions
	private static final String DTD_EXTENSION = ".dtd";

	private static final String ENT_EXTENSION = ".ent";

	private static final String MOD_EXTENSION = ".mod";

	private static final String XSL_EXTENSION = ".xsl";

	private static final String HTTP_WWW_W3_ORG_2001_XML_SCHEMA_NS = "http://www.w3.org/2001/XMLSchema";

	private static final  String HTTP_WWW_W3_ORG_1999_XSL_STYLESHEET_NS = "http://www.w3.org/1999/XSL/Transform";

	private static final String URN_OASIS_NAMES_TC_ENTITY_XMLNS_XML_CATALOG_NS = "urn:oasis:names:tc:entity:xmlns:xml:catalog";

	private DOMUtils() {

	}

	/**
	 * Returns true if the XML document is a XML Schema and false otherwise.
	 * 
	 * @return true if the XML document is a XML Schema and false otherwise.
	 */
	public static boolean isXSD(DOMDocument document) {
		if (document == null) {
			return false;
		}
		String uri = document.getDocumentURI();
		if (isXSD(uri)) {
			return true;
		}
		// check root element is bound with XML Schema namespace
		// (http://www.w3.org/2001/XMLSchema)
		return checkRootNamespace(document, HTTP_WWW_W3_ORG_2001_XML_SCHEMA_NS);
	}


	/**
	 * Returns true if the given URI is a XML Schema and false otherwise.
	 * 
	 * @param uri the URI to check
	 * @return true if the given URI is a XML Schema and false otherwise.
	 */
	public static boolean isXSD(String uri) {
		return uri != null && uri.endsWith(XSD_EXTENSION);
	}

	/**
	 * Returns true if the XML document is a XSL Stylesheet and false otherwise.
	 * 
	 * @return true if the XML document is a XSL Stylesheet and false otherwise.
	 */
	public static boolean isXSL(DOMDocument document) {
		if (document == null) {
			return false;
		}
		String uri = document.getDocumentURI();
		if (isXSL(uri)) {
			return true;
		}
		// check root element is bound with XML Schema namespace
		// (http://www.w3.org/1999/XSL/Transform)
		return checkRootNamespace(document, HTTP_WWW_W3_ORG_1999_XSL_STYLESHEET_NS);
	}
	/**
	 * Returns true if the given URI is a XSL Stylesheet and false otherwise.
	 * 
	 * @param uri the URI to check
	 * @return true if the given URI is a XSL Stylesheet and false otherwise.
	 */
	public static boolean isXSL(String uri) {
		return uri != null && uri.endsWith(XSL_EXTENSION);
	}

	/**
	 * Returns true if the XML document is a XML Catalog and false otherwise.
	 * 
	 * @return true if the XML document is a XML Catalog and false otherwise.
	 */
	public static boolean isCatalog(DOMDocument document) {
		// check root element is bound with XML Catalog namespace
		// (urn:oasis:names:tc:entity:xmlns:xml:catalog)
		return checkRootNamespace(document, URN_OASIS_NAMES_TC_ENTITY_XMLNS_XML_CATALOG_NS);
	}

	/**
	 * Returns true if the document element root is bound to the given namespace and
	 * false otherwise.
	 * 
	 * @param document
	 * @param namespace
	 * @return true if the document element root is bound to the given namespace and
	 *         false otherwise.
	 */
	private static boolean checkRootNamespace(DOMDocument document, String namespace) {
		DOMElement documentElement = document.getDocumentElement();
		return documentElement != null && namespace.equals(documentElement.getNamespaceURI());
	}

	/**
	 * Returns true if the given URI is a DTD and false otherwise.
	 * 
	 * @param uri the URI to check
	 * @return true if the given URI is a DTD and false otherwise.
	 */
	public static boolean isDTD(String uri) {
		return uri != null
				&& (uri.endsWith(DTD_EXTENSION) || uri.endsWith(ENT_EXTENSION) || uri.endsWith(MOD_EXTENSION));
	}

	/**
	 * Returns true if element contains only DOMText and false otherwise.
	 * 
	 * @return true if element contains only DOMText and false otherwise.
	 */
	public static boolean containsTextOnly(DOMElement element) {
		return element.getChildNodes().getLength() == 1 && element.getFirstChild().isText();
	}

	/**
	 * Returns the DOM document from the given XML Schema uri.
	 * 
	 * @param documentURI              the schema URI
	 * @param resolverExtensionManager
	 * @return the DOM document from the given XML Schema uri.
	 */
	public static DOMDocument loadDocument(String documentURI, URIResolverExtensionManager resolverExtensionManager) {
		try {
			return DOMParser.getInstance().parse(IOUtils.convertStreamToString(new URL(documentURI).openStream()),
					documentURI, resolverExtensionManager);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while loading XML Schema '" + documentURI + "'.", e);
			return null;
		}
	}
}
