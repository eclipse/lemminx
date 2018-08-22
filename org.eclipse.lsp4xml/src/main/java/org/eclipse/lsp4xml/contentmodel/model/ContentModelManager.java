/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.contentmodel.model;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.xs.XSModel;
import org.eclipse.lsp4xml.contentmodel.xsd.XSDDocument;
import org.eclipse.lsp4xml.model.Node;
import org.eclipse.lsp4xml.model.SchemaLocation;
import org.eclipse.lsp4xml.model.XMLDocument;

/**
 * Content model manager used to load XML Schema, DTD.
 *
 */
public class ContentModelManager {

	private static final ContentModelManager INSTANCE = new ContentModelManager();

	public static ContentModelManager getInstance() {
		return INSTANCE;
	}

	private final XMLSchemaLoader loader;
	private Map<URI, CMDocument> cmDocumentCache;

	public ContentModelManager() {
		loader = new XMLSchemaLoader();
		cmDocumentCache = new HashMap<>();
	}

	/**
	 * Returns the content model document loaded by the given uri and null
	 * otherwise.
	 * 
	 * @param uri of the DTD, XML Schema grammar to load.
	 * @return the content model document loaded by the given uri and null
	 *         otherwise.
	 */
	public CMDocument getCMDocument(URI uri) {
		CMDocument cmDocument = cmDocumentCache.get(uri);
		if (cmDocument == null) {
			XSModel model = loader.loadURI(uri.toString());
			if (model != null) {
				// XML Schema can be loaded
				cmDocument = new XSDDocument(model);
				cmDocumentCache.put(uri, cmDocument);
			}
		}
		return cmDocument;
	}

	/**
	 * Returns the declared element which matches the given XML element and null
	 * otherwise.
	 * 
	 * @param element the XML element
	 * @return the declared element which matches the given XML element and null
	 *         otherwise.
	 */
	public CMElementDeclaration findCMElement(Node element) throws Exception {
		XMLDocument xmlDocument = element.getOwnerDocument();
		SchemaLocation schemaLocation = xmlDocument.getSchemaLocation();
		if (schemaLocation == null) {
			// TODO: implement CMDocument with DTD
			return null;
		}
		String namespaceURI = xmlDocument.getNamespaceURI();
		String schemaURI = schemaLocation.getLocationHint(namespaceURI);
		if (schemaURI == null) {
			return null;
		}

		URI uri = /* new File("maven-4.0.0.xsd").toURI(); // */ new URI(schemaURI);
		CMDocument cmDocument = getCMDocument(uri);
		return cmDocument != null ? cmDocument.findCMElement(element) : null;
	}

}