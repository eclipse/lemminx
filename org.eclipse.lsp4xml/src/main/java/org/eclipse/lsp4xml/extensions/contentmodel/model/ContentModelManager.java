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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xs.XSLoaderImpl;
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
import org.eclipse.lsp4xml.uriresolver.CacheResourceDownloadingException;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtensionManager;
import org.eclipse.lsp4xml.utils.URIUtils;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;

/**
 * Content model manager used to load XML Schema, DTD.
 *
 */
public class ContentModelManager {

//	private static final ContentModelManager INSTANCE = new ContentModelManager();
//
//	public static ContentModelManager getInstance() {
//		return INSTANCE;
//	}

	private final XSLoaderImpl loader;

	private final Map<String, CMDocument> cmDocumentCache;

	private final XMLCacheResolverExtension cacheResolverExtension;
	private final XMLCatalogResolverExtension catalogResolverExtension;
	private final XMLFileAssociationResolverExtension fileAssociationResolver;
	private final URIResolverExtensionManager resolverManager;

	public ContentModelManager(URIResolverExtensionManager resolverManager) {
		this.resolverManager = resolverManager;
		cmDocumentCache = Collections.synchronizedMap(new HashMap<>());
		loader = new XSLoaderImpl();
		loader.setParameter("http://apache.org/xml/properties/internal/entity-resolver", resolverManager);
		loader.setParameter(Constants.DOM_ERROR_HANDLER, new DOMErrorHandler() {

			@Override
			public boolean handleError(DOMError error) {
				if (error.getRelatedException() instanceof CacheResourceDownloadingException) {
					throw ((CacheResourceDownloadingException) error.getRelatedException());
				}
				return false;
			}
		});
		fileAssociationResolver = new XMLFileAssociationResolverExtension();
		resolverManager.registerResolver(fileAssociationResolver);
		catalogResolverExtension = new XMLCatalogResolverExtension();
		resolverManager.registerResolver(catalogResolverExtension);
		cacheResolverExtension = new XMLCacheResolverExtension();
		resolverManager.registerResolver(cacheResolverExtension);
		// Use cache by default
		setUseCache(true);
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
		return findCMDocument(xmlDocument.getDocumentURI(), namespaceURI, systemId);
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
		String key = resolverManager.resolve(uri, publicId, systemId);
		if (key == null) {
			return null;
		}
		CMDocument cmDocument = null;
		boolean isCacheable = isCacheable(key);
		if (isCacheable) {
			cmDocument = cmDocumentCache.get(key);
		}
		if (cmDocument == null) {
			XSModel model = loader.loadURI(key);
			if (model != null) {
				// XML Schema can be loaded
				cmDocument = new XSDDocument(model);
				if (isCacheable) {
					cmDocumentCache.put(key, cmDocument);
				}
			}
		}
		return cmDocument;
	}

	private boolean isCacheable(String uri) {
		return !URIUtils.isFileResource(uri);
	}

	/**
	 * Set up XML catalogs.
	 * 
	 * @param catalogs list of XML catalog files.
	 * @return true if catalogs changed and false otherwise
	 */
	public boolean setCatalogs(String[] catalogs) {
		return catalogResolverExtension.setCatalogs(catalogs);
	}

	/**
	 * Refresh the XML catalogs.
	 */
	public void refreshCatalogs() {
		catalogResolverExtension.refreshCatalogs();
	}

	/**
	 * Set file associations.
	 * 
	 * @param fileAssociations
	 * @return true if file associations changed and false otherwise
	 */
	public boolean setFileAssociations(XMLFileAssociation[] fileAssociations) {
		return this.fileAssociationResolver.setFileAssociations(fileAssociations);
	}

	public void setRootURI(String rootUri) {
		rootUri = URIUtils.sanitizingUri(rootUri);
		fileAssociationResolver.setRootUri(rootUri);
		catalogResolverExtension.setRootUri(rootUri);
	}

	public void setUseCache(boolean useCache) {
		cacheResolverExtension.setUseCache(useCache);
	}

}