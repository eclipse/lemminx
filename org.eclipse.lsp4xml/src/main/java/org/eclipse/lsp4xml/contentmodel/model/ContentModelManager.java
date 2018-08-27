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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.xerces.impl.xs.XSLoaderImpl;
import org.apache.xerces.xs.XSModel;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
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

	private final XSLoaderImpl loader;

	private Map<String, CMDocument> cmDocumentCache;

	private CatalogResolver catalogResolver;

	public ContentModelManager() {
		loader = new XSLoaderImpl();
		cmDocumentCache = new HashMap<>();
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

		CMDocument cmDocument = getCMDocument(null, schemaURI);
		return cmDocument != null ? cmDocument.findCMElement(element) : null;
	}

	/**
	 * Returns the content model document loaded by the given uri and null
	 * otherwise.
	 * 
	 * @param publicId the public identifier.
	 * @param systemId the expanded system identifier.
	 * @return the content model document loaded by the given uri and null
	 *         otherwise.
	 */
	private CMDocument getCMDocument(String publicId, String systemId) {
		String key = publicId + systemId;
		CMDocument cmDocument = cmDocumentCache.get(key);
		if (cmDocument == null) {
			CatalogResolver resolver = getCatalogResolver();
			String uri = resolver != null ? resolver.getResolvedEntity(publicId, systemId) : systemId;
			XSModel model = loader.loadURI(uri);
			if (model != null) {
				// XML Schema can be loaded
				cmDocument = new XSDDocument(model);
				cmDocumentCache.put(key, cmDocument);
			}
		}
		return cmDocument;
	}

	/**
	 * Set up XML catalogs.
	 * 
	 * @param catalogs list of XML catalog files.
	 */
	public void setCatalogs(String[] catalogs) {
		if (catalogs != null) {
			String xmlCatalogFiles = Stream.of(catalogs).filter(ContentModelManager::isXMLCatalogFileValid)
					.map(Object::toString).collect(Collectors.joining(";"));
			if (!xmlCatalogFiles.isEmpty()) {
				CatalogManager catalogManager = new CatalogManager();
				catalogManager.setUseStaticCatalog(false);
				catalogManager.setIgnoreMissingProperties(true);
				catalogManager.setCatalogFiles(xmlCatalogFiles);
				catalogResolver = new CatalogResolver(catalogManager);
			} else {
				catalogResolver = null;
			}
		}

	}

	/**
	 * Returns true if the XML catalog file exists and false otherwise.
	 * 
	 * @param catalogFile catalog file to check.
	 * @return true if the XML catalog file exists and false otherwise.
	 */
	public static boolean isXMLCatalogFileValid(String catalogFile) {
		File file = new File(catalogFile);
		return (file.exists());
	}

	/**
	 * Returns the catalog resolver to use and null otherwise.
	 * 
	 * @return the catalog resolver to use and null otherwise.
	 */
	public CatalogResolver getCatalogResolver() {
		return catalogResolver;
	}

}