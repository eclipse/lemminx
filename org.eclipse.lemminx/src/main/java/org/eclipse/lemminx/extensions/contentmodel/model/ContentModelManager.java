/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.extensions.contentmodel.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.extensions.contentmodel.participants.diagnostics.LSPXMLGrammarPool;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLFileAssociation;
import org.eclipse.lemminx.extensions.contentmodel.uriresolver.XMLCacheResolverExtension;
import org.eclipse.lemminx.extensions.contentmodel.uriresolver.XMLCatalogResolverExtension;
import org.eclipse.lemminx.extensions.contentmodel.uriresolver.XMLFileAssociationResolverExtension;
import org.eclipse.lemminx.uriresolver.CacheResourceDownloadingException;
import org.eclipse.lemminx.uriresolver.URIResolverExtensionManager;
import org.eclipse.lemminx.utils.StringUtils;
import org.eclipse.lemminx.utils.URIUtils;

/**
 * Content model manager used to load XML Schema, DTD.
 *
 */
public class ContentModelManager {

	private final Map<String, CMDocument> cmDocumentCache;

	private final URIResolverExtensionManager resolverManager;
	private final List<ContentModelProvider> modelProviders;

	private final XMLCacheResolverExtension cacheResolverExtension;
	private final XMLCatalogResolverExtension catalogResolverExtension;
	private final XMLFileAssociationResolverExtension fileAssociationResolver;
	private final XMLGrammarPool grammarPool;

	public ContentModelManager(URIResolverExtensionManager resolverManager) {
		this.resolverManager = resolverManager;
		modelProviders = new ArrayList<>();
		cmDocumentCache = Collections.synchronizedMap(new HashMap<>());
		fileAssociationResolver = new XMLFileAssociationResolverExtension();
		resolverManager.registerResolver(fileAssociationResolver);
		catalogResolverExtension = new XMLCatalogResolverExtension();
		resolverManager.registerResolver(catalogResolverExtension);
		cacheResolverExtension = new XMLCacheResolverExtension();
		resolverManager.registerResolver(cacheResolverExtension);
		grammarPool = new LSPXMLGrammarPool();
		// Use cache by default
		setUseCache(true);
	}

	public CMElementDeclaration findCMElement(DOMElement element) throws Exception {
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
	public CMElementDeclaration findCMElement(DOMElement element, String namespaceURI) throws Exception {
		CMDocument cmDocument = findCMDocument(element, namespaceURI);
		return cmDocument != null ? cmDocument.findCMElement(element, namespaceURI) : null;
	}

	public CMDocument findCMDocument(DOMElement element, String namespaceURI) {
		return findCMDocument(element.getOwnerDocument(), namespaceURI);
	}

	public CMDocument findCMDocument(DOMDocument xmlDocument, String namespaceURI) {
		ContentModelProvider modelProvider = getModelProviderByStandardAssociation(xmlDocument, false);
		String systemId = modelProvider != null ? modelProvider.getSystemId(xmlDocument, namespaceURI) : null;
		return findCMDocument(xmlDocument.getDocumentURI(), namespaceURI, systemId, modelProvider);
	}

	/**
	 * Returns true if the given document is linked to the given grammar URI (XML
	 * Schema, DTD) and false otherwise.
	 * 
	 * @param document   the DOM document
	 * @param grammarURI the grammar URI
	 * @return true if the given document is linked to the given grammar URI (XML
	 *         Schema, DTD) and false otherwise.
	 */
	public boolean dependsOnGrammar(DOMDocument document, String grammarURI) {
		if (StringUtils.isEmpty(grammarURI)) {
			return false;
		}
		ContentModelProvider modelProvider = getModelProviderByStandardAssociation(document, false);
		String systemId = modelProvider != null ? modelProvider.getSystemId(document, document.getNamespaceURI())
				: null;
		String key = resolverManager.resolve(document.getDocumentURI(), null, systemId);
		return grammarURI.equals(key);
	}

	/**
	 * Returns the content model document loaded by the given uri and null
	 * otherwise.
	 * 
	 * @param publicId      the public identifier.
	 * @param systemId      the expanded system identifier.
	 * @param modelProvider
	 * @return the content model document loaded by the given uri and null
	 *         otherwise.
	 */
	private CMDocument findCMDocument(String uri, String publicId, String systemId,
			ContentModelProvider modelProvider) {
		// Resolve the XML Schema/DTD uri (file, http, etc)
		String resolvedUri = resolverManager.resolve(uri, publicId, systemId);
		if (resolvedUri == null) {
			return null;
		}
		// the XML Schema, DTD can be resolved
		if (modelProvider == null) {
			// the model provider cannot be get with standard mean (xsi:schemaLocation,
			// xsi:noNamespaceSchemaLocation, doctype)
			// try to get it by using extension (ex: .xsd, .dtd)
			modelProvider = getModelProviderByURI(resolvedUri);
		}
		if (modelProvider == null) {
			return null;
		}
		// Try to get the document from the cache
		CMDocument cmDocument = getCMDocumentFromCache(resolvedUri);
		if (cmDocument != null) {
			return cmDocument;
		}
		boolean isFileResource = URIUtils.isFileResource(resolvedUri);
		if (!isFileResource && cacheResolverExtension.isUseCache()) {
			// The DTD/XML Schema comes from http://, ftp:// etc and cache manager is
			// activated
			// Try to load the DTD/XML Schema with the cache manager
			try {
				Path file = cacheResolverExtension.getCachedResource(resolvedUri);
				if (file != null) {
					cmDocument = modelProvider.createCMDocument(file.toFile().getPath());
				}
			} catch (CacheResourceDownloadingException e) {
				// the DTD/XML Schema is downloading
				return null;
			} catch (Exception e) {
				// other error like network which is not available
				cmDocument = modelProvider.createCMDocument(resolvedUri);
			}
		} else {
			cmDocument = modelProvider.createCMDocument(resolvedUri);
		}
		// Cache the document
		if (cmDocument != null) {
			cache(resolvedUri, cmDocument);
		}
		return cmDocument;
	}

	private CMDocument getCMDocumentFromCache(String key) {
		CMDocument document = null;
		synchronized (cmDocumentCache) {
			document = cmDocumentCache.get(key);
			if (document != null && document.isDirty()) {
				cmDocumentCache.remove(key);
				return null;
			}
		}
		return document;
	}

	private void cache(String key, CMDocument cmDocument) {
		synchronized (cmDocumentCache) {
			cmDocumentCache.put(key, cmDocument);
		}
	}

	public CMElementDeclaration findInternalCMElement(DOMElement element) throws Exception {
		return findInternalCMElement(element, element.getNamespaceURI());
	}

	/**
	 * Returns the declared element which matches the given XML element and null
	 * otherwise.
	 * 
	 * @param element the XML element
	 * @return the declared element which matches the given XML element and null
	 *         otherwise.
	 */
	public CMElementDeclaration findInternalCMElement(DOMElement element, String namespaceURI) throws Exception {
		CMDocument cmDocument = findInternalCMDocument(element, namespaceURI);
		return cmDocument != null ? cmDocument.findCMElement(element, namespaceURI) : null;
	}

	public CMDocument findInternalCMDocument(DOMElement element, String namespaceURI) {
		return findInternalCMDocument(element.getOwnerDocument(), namespaceURI);
	}

	public CMDocument findInternalCMDocument(DOMDocument xmlDocument, String namespaceURI) {
		ContentModelProvider modelProvider = getModelProviderByStandardAssociation(xmlDocument, true);
		if (modelProvider != null) {
			return modelProvider.createInternalCMDocument(xmlDocument);
		}
		return null;
	}

	/**
	 * Returns the content model provider by using standard association
	 * (xsi:schemaLocation, xsi:noNamespaceSchemaLocation, doctype) an dnull
	 * otherwise.
	 * 
	 * @param xmlDocument
	 * @return the content model provider by using standard association
	 *         (xsi:schemaLocation, xsi:noNamespaceSchemaLocation, doctype) an dnull
	 *         otherwise.
	 */
	private ContentModelProvider getModelProviderByStandardAssociation(DOMDocument xmlDocument, boolean internal) {
		for (ContentModelProvider modelProvider : modelProviders) {
			if (modelProvider.adaptFor(xmlDocument, internal)) {
				return modelProvider;
			}
		}
		return null;
	}

	private ContentModelProvider getModelProviderByURI(String uri) {
		for (ContentModelProvider modelProvider : modelProviders) {
			if (modelProvider.adaptFor(uri)) {
				return modelProvider;
			}
		}
		return null;
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
		if (!useCache) {
			grammarPool.clear();
		}
	}

	public void registerModelProvider(ContentModelProvider modelProvider) {
		modelProviders.add(modelProvider);
	}

	public void unregisterModelProvider(ContentModelProvider modelProvider) {
		modelProviders.remove(modelProvider);
	}

	public XMLGrammarPool getGrammarPool() {
		return cacheResolverExtension.isUseCache() ? grammarPool : null;
	}

}