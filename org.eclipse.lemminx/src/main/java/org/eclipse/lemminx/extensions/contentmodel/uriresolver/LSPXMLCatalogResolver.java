/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.contentmodel.uriresolver;

import java.io.IOException;

import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.impl.xs.XSDDescription;
import org.apache.xerces.util.URI;
import org.apache.xerces.util.XMLCatalogResolver;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Extension of Xerces XML catalog resolver to support include of XSD.
 * 
 * @author Angelo ZERR
 *
 */
class LSPXMLCatalogResolver extends XMLCatalogResolver {

	public LSPXMLCatalogResolver(String[] catalogs) {
		super(catalogs);
	}

	/**
	 * <p>
	 * Resolves a resource using the catalog. This method interprets that the
	 * namespace URI corresponds to uri entries in the catalog. Where both a
	 * namespace and an external identifier exist, the namespace takes precedence.
	 * </p>
	 * 
	 * @param type         the type of the resource being resolved
	 * @param namespaceURI the namespace of the resource being resolved, or
	 *                     <code>null</code> if none was supplied
	 * @param publicId     the public identifier of the resource being resolved, or
	 *                     <code>null</code> if none was supplied
	 * @param systemId     the system identifier of the resource being resolved, or
	 *                     <code>null</code> if none was supplied
	 * @param baseURI      the absolute base URI of the resource being parsed, or
	 *                     <code>null</code> if there is no base URI
	 */
	public String resolveIdentifier(String namespaceURI, String publicId, String systemId, String baseURI) {

		String resolvedId = null;

		try {
			// The namespace is useful for resolving namespace aware
			// grammars such as XML schema. Let it take precedence over
			// the external identifier if one exists.
			if (namespaceURI != null) {
				resolvedId = resolveURI(namespaceURI);
				if (resolvedId != null) {
					return resolvedId;
				}
			}

			if (!getUseLiteralSystemId() && baseURI != null) {
				// Attempt to resolve the system identifier against the base URI.
				try {
					URI uri = new URI(new URI(baseURI), systemId);
					systemId = uri.toString();
				}
				// Ignore the exception. Fallback to the literal system identifier.
				catch (URI.MalformedURIException ex) {
				}
			}

			// Resolve against an external identifier if one exists. This
			// is useful for resolving DTD external subsets and other
			// external entities. For XML schemas if there was no namespace
			// mapping we might be able to resolve a system identifier
			// specified as a location hint.
			if (resolvedId == null) {
				if (publicId != null && systemId != null) {
					resolvedId = resolvePublic(publicId, systemId);
				} else if (systemId != null) {
					resolvedId = resolveSystem(systemId);
				} else if (publicId != null) {
					return resolvePublic(publicId, null);
				}
				if (resolvedId == null && systemId != null) {
					// ex systemId = http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd
					//
					// XML
					// <web-app xmlns="http://java.sun.com/xml/ns/j2ee"
					// xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
					// xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee
					// http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd"

					// XML catalog
					// <uri name="http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd"
					// uri="file:///...web-app_2_4.xsd"/>
					resolvedId = resolveURI(systemId);
				}
			}
		}
		// Ignore IOException. It cannot be thrown from this method.
		catch (IOException ex) {
		}
		return resolvedId;
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		String resolvedId = resolveIdentifier(null, publicId, systemId, null);
		if (resolvedId != null) {
			InputSource source = new InputSource(resolvedId);
			source.setPublicId(publicId);
			return source;
		}
		return null;
	}

	@Override
	public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId)
			throws SAXException, IOException {
		String resolvedId = resolveIdentifier(null, publicId, systemId, baseURI);
		if (resolvedId != null) {
			InputSource source = new InputSource(resolvedId);
			source.setPublicId(publicId);
			return source;
		}
		return null;
	}

	@Override
	public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
		String resolvedId = resolveIdentifier(namespaceURI, publicId, systemId, baseURI);
		if (resolvedId != null) {
			return new DOMInputImpl(publicId, resolvedId, baseURI);
		}
		return null;
	}

	@Override
	public String resolveIdentifier(XMLResourceIdentifier resourceIdentifier) throws IOException, XNIException {
		// The default Xerces XML catalog implementation promote the namespace
		// information from the resource identifier
		// See
		// https://github.com/apache/xerces2-j/blob/cf0c517a41b31b0242b96ab1af9627a3ab07fcd2/src/org/apache/xerces/util/XMLCatalogResolver.java#L418
		// For XSD include the resolve identifier is not computed correctly.
		if (isXSDIncludeWithNamespace(resourceIdentifier)) {
			return resourceIdentifier.getExpandedSystemId();
		}

		String publicId = resourceIdentifier.getPublicId();
		String namespaceURI = resourceIdentifier.getNamespace();
		String systemId = getUseLiteralSystemId() ? resourceIdentifier.getLiteralSystemId()
				: resourceIdentifier.getExpandedSystemId();
		String baseURI = resourceIdentifier.getBaseSystemId();
		return resolveIdentifier(namespaceURI, publicId, systemId, baseURI);
	}

	/**
	 * Returns true if it's an XSD include/import with namespace and false
	 * otherwise.
	 * 
	 * @param resourceIdentifier the resource identifier.
	 * 
	 * @return true if it's an XSD include/import with namespace and false
	 *         otherwise.
	 */
	private static boolean isXSDIncludeWithNamespace(XMLResourceIdentifier resourceIdentifier) {
		String namespaceURI = resourceIdentifier.getNamespace();
		if (resourceIdentifier != null && resourceIdentifier instanceof XSDDescription && namespaceURI != null
				&& (resourceIdentifier.getLiteralSystemId() != null
						&& !resourceIdentifier.getLiteralSystemId().contains(":"))) {
			// ex : <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
			// elementFormDefault="qualified" targetNamespace="http://foobar.com/test"
			// xmlns:test="http://foobar.com/test">
			// <xs:include schemaLocation="test-include.xsd"/>
			// </xs:schema>
			XSDDescription description = (XSDDescription) resourceIdentifier;
			int contextType = description.getContextType();
			return contextType == XSDDescription.CONTEXT_INCLUDE;
		}
		return false;
	}
}
