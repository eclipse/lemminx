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
import org.apache.xerces.util.XMLCatalogResolver;
import org.apache.xerces.xs.XSModel;
import org.eclipse.lsp4xml.contentmodel.settings.XMLFileAssociation;
import org.eclipse.lsp4xml.contentmodel.uriresolver.XMLCatalogResolverExtension;
import org.eclipse.lsp4xml.contentmodel.uriresolver.XMLFileAssociationResolverExtension;
import org.eclipse.lsp4xml.contentmodel.xsd.XSDDocument;
import org.eclipse.lsp4xml.dom.Element;
import org.eclipse.lsp4xml.dom.NoNamespaceSchemaLocation;
import org.eclipse.lsp4xml.dom.SchemaLocation;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtensionManager;

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

	private final XMLCatalogResolverExtension catalogResolverExtension;
	private final XMLFileAssociationResolverExtension fileAssociationResolver;

	public ContentModelManager() {
		loader = new XSLoaderImpl();
		cmDocumentCache = new HashMap<>();
		fileAssociationResolver = new XMLFileAssociationResolverExtension();
		URIResolverExtensionManager.getInstance().registerResolver(fileAssociationResolver);
		catalogResolverExtension = new XMLCatalogResolverExtension();
		URIResolverExtensionManager.getInstance().registerResolver(catalogResolverExtension);
	}

	public CMElementDeclaration findCMElement(Element element) throws Exception {
		return findCMElement(element, element.getNamespaceURI());
	}
	/**
	 * Returns the declared element which matches the given XML element and null
	 * otherwise.
	 * 
	 * @param element the XML element
	 * @return the declared element which matches the given XML element and null
	 *         otherwise.
	 */
	public CMElementDeclaration findCMElement(Element element, String namespaceURI) throws Exception {
		CMDocument cmDocument = findCMDocument(element, namespaceURI);
		return cmDocument != null ? cmDocument.findCMElement(element, namespaceURI) : null;
	}

	public CMDocument findCMDocument(Element element, String namespaceURI) {
		String systemId = null;
		XMLDocument xmlDocument = element.getOwnerDocument();
		SchemaLocation schemaLocation = xmlDocument.getSchemaLocation();
		if (schemaLocation != null) {
			systemId = schemaLocation.getLocationHint(namespaceURI);
		} else {
			NoNamespaceSchemaLocation noNamespaceSchemaLocation =  xmlDocument.getNoNamespaceSchemaLocation();
			if (noNamespaceSchemaLocation != null) {
				systemId = noNamespaceSchemaLocation.getLocation();
			} else {
				// TODO : implement with DTD
			}
		}
		return findCMDocument(xmlDocument.getUri(), namespaceURI, systemId);
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
	private CMDocument findCMDocument(String uri, String publicId, String systemId) {
		String key = publicId + systemId;
		CMDocument cmDocument = cmDocumentCache.get(key);
		if (cmDocument == null) {
			String xmlSchemaURI = URIResolverExtensionManager.getInstance().resolve(uri, publicId, systemId);
			if (xmlSchemaURI == null) {
				xmlSchemaURI = systemId;
			}
			if (xmlSchemaURI != null) {
				XSModel model = loader.loadURI(xmlSchemaURI);
				if (model != null) {
					// XML Schema can be loaded
					cmDocument = new XSDDocument(model);
					cmDocumentCache.put(key, cmDocument);
				}
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
			String[] xmlCatalogFiles = Stream.of(catalogs).filter(ContentModelManager::isXMLCatalogFileValid)
					.collect(Collectors.toList()).toArray(new String[0]);
			if (xmlCatalogFiles.length > 0) {
				XMLCatalogResolver catalogResolver = new XMLCatalogResolver(xmlCatalogFiles);
				catalogResolverExtension.setCatalogResolver(catalogResolver);
			} else {
				catalogResolverExtension.setCatalogResolver(null);
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
	 * Set file associations.
	 * 
	 * @param fileAssociations
	 */
	public void setFileAssociations(XMLFileAssociation[] fileAssociations) {
		this.fileAssociationResolver.setFileAssociations(fileAssociations);
	}

}