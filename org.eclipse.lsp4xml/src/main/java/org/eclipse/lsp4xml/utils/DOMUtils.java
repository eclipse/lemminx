package org.eclipse.lsp4xml.utils;

import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMElement;

/**
 * DOM Utilities.
 *
 */
public class DOMUtils {

	private static final String XSD_EXTENSION = ".xsd";

	// DTD file extensions
	private static final String DTD_EXTENSION = ".dtd";

	private static final String ENT_EXTENSION = ".ent";

	private static final String MOD_EXTENSION = ".mod";

	private static final String HTTP_WWW_W3_ORG_2001_XML_SCHEMA_NS = "http://www.w3.org/2001/XMLSchema";

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
}
