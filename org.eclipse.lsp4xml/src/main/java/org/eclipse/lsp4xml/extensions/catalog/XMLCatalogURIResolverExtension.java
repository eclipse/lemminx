/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.catalog;

import java.io.IOException;

import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtension;

/**
 * Resolve the XML Schema of XML/Catalog
 *
 */
public class XMLCatalogURIResolverExtension implements URIResolverExtension {

	/**
	 * The XSL namespace URI (=
	 * http://www.oasis-open.org/committees/entity/release/1.1/catalog.xsd)
	 */
	private static final String CATALOG_NAMESPACE_URI = "urn:oasis:names:tc:entity:xmlns:xml:catalog"; //$NON-NLS-1$

	private static final String CATALOG_SYSTEM = "http://www.oasis-open.org/committees/entity/release/1.1/catalog.xsd";

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLCatalogURIResolverExtension(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	@Override
	public String resolve(String baseLocation, String publicId, String systemId) {
		if (!CATALOG_NAMESPACE_URI.equals(publicId)) {
			return null;
		}
		if (hasDTDorXMLSchema(baseLocation)) {
			return null;
		}
		return CATALOG_SYSTEM;
	}

	@Override
	public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException {
		if (hasDTDorXMLSchema(resourceIdentifier.getBaseSystemId())) {
			return null;
		}
		String publicId = resourceIdentifier.getNamespace();
		if (CATALOG_NAMESPACE_URI.equals(publicId)) {
			return new XMLInputSource(publicId, CATALOG_SYSTEM, CATALOG_SYSTEM);
		}
		return null;
	}

	private boolean hasDTDorXMLSchema(String uri) {
		XMLDocument document = extensionsRegistry.getDocumentProvider().getDocument(uri);
		if (document == null) {
			return false;
		}
		return document.hasDTD() || document.hasSchemaLocation() || document.hasNoNamespaceSchemaLocation();
	}
}
