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
package org.eclipse.lsp4xml.extensions.contentmodel.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xs.XSLoaderImpl;
import org.apache.xerces.util.XMLCatalogResolver;
import org.apache.xerces.xs.XSModel;
import org.eclipse.lsp4xml.dom.Element;
import org.eclipse.lsp4xml.dom.NoNamespaceSchemaLocation;
import org.eclipse.lsp4xml.dom.SchemaLocation;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.XMLFileAssociation;
import org.eclipse.lsp4xml.extensions.contentmodel.uriresolver.XMLCacheResolverExtension;
import org.eclipse.lsp4xml.extensions.contentmodel.uriresolver.XMLCatalogResolverExtension;
import org.eclipse.lsp4xml.extensions.contentmodel.uriresolver.XMLFileAssociationResolverExtension;
import org.eclipse.lsp4xml.extensions.contentmodel.xsd.XSDDocument;
import org.eclipse.lsp4xml.uriresolver.CacheResourceLoadingException;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtensionManager;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;

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

	private final Map<String, CMDocument> cmDocumentCache;

	private final XMLCacheResolverExtension cacheResolverExtension;
	private final XMLCatalogResolverExtension catalogResolverExtension;
	private final XMLFileAssociationResolverExtension fileAssociationResolver;

	public ContentModelManager() {
		cmDocumentCache = new HashMap<>();		
		URIResolverExtensionManager resolverManager = URIResolverExtensionManager.getInstance();
		loader = new XSLoaderImpl();
		loader.setParameter("http://apache.org/xml/properties/internal/entity-resolver", resolverManager);
		loader.setParameter(Constants.DOM_ERROR_HANDLER, new DOMErrorHandler() {
			
			@Override
			public boolean handleError(DOMError error) {
				if (error.getRelatedException() instanceof CacheResourceLoadingException) {
					throw ((CacheResourceLoadingException) error.getRelatedException());
				}
				return false;
			}
		});
		cacheResolverExtension = new XMLCacheResolverExtension();
		resolverManager.registerResolver(cacheResolverExtension);
		fileAssociationResolver = new XMLFileAssociationResolverExtension();
		resolverManager.registerResolver(fileAssociationResolver);
		catalogResolverExtension = new XMLCatalogResolverExtension();
		resolverManager.registerResolver(catalogResolverExtension);
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
		return findCMDocument(element.getOwnerDocument(), namespaceURI);
	}

	public CMDocument findCMDocument(XMLDocument xmlDocument, String namespaceURI) {
		String systemId = null;
		SchemaLocation schemaLocation = xmlDocument.getSchemaLocation();
		if (schemaLocation != null) {
			systemId = schemaLocation.getLocationHint(namespaceURI);
		} else {
			NoNamespaceSchemaLocation noNamespaceSchemaLocation = xmlDocument.getNoNamespaceSchemaLocation();
			if (noNamespaceSchemaLocation != null) {
				if (namespaceURI != null) {
					// xsi:noNamespaceSchemaLocation doesn't define namespaces
					return null;
				}
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
		String key = URIResolverExtensionManager.getInstance().resolve(uri, publicId, systemId);
		if (key == null) {
			return null;
		}
		CMDocument cmDocument = cmDocumentCache.get(key);
		if (cmDocument == null) {
			XSModel model = loader.loadURI(key);
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

	public void setRootURI(String rootUri) {
		fileAssociationResolver.setRootUri(rootUri);
	}

	public void setUseCache(boolean useCache) {
		cacheResolverExtension.setUseCache(useCache);
	}

}