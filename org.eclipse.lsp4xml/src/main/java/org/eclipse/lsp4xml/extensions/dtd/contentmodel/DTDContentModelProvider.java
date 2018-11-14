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
package org.eclipse.lsp4xml.extensions.dtd.contentmodel;

import org.apache.xerces.impl.dtd.DTDGrammar;
import org.apache.xerces.impl.dtd.XMLDTDLoader;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.eclipse.lsp4xml.dom.DocumentType;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelProvider;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtensionManager;
import org.eclipse.lsp4xml.utils.DOMUtils;

/**
 * DTD content model provider.
 */
public class DTDContentModelProvider implements ContentModelProvider {

	private final URIResolverExtensionManager resolverExtensionManager;

	private XMLDTDLoader loader;

	public DTDContentModelProvider(URIResolverExtensionManager resolverExtensionManager) {
		this.resolverExtensionManager = resolverExtensionManager;
	}

	@Override
	public boolean adaptFor(XMLDocument document) {
		return document.hasDTD();
	}

	@Override
	public boolean adaptFor(String uri) {
		return DOMUtils.isDTD(uri);
	}

	@Override
	public String getSystemId(XMLDocument xmlDocument, String namespaceURI) {
		/*
		 * <!DOCTYPE catalog PUBLIC
		 * "-//OASIS/DTD Entity Resolution XML Catalog V1.0//EN"
		 * "http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd">
		 */
		DocumentType documentType = xmlDocument.getDoctype();
		// FIXME!!! get system if correctly with XMLDocument
		String content = documentType.getContent();
		int lastQuoteIndex = content.lastIndexOf("\"");
		if (lastQuoteIndex != -1) {
			content = content.substring(0, lastQuoteIndex);
			lastQuoteIndex = content.lastIndexOf("\"");
			if (lastQuoteIndex != -1) {
				return content.substring(lastQuoteIndex + 1, content.length());
				
			}
		}
		return null;
	}

	@Override
	public CMDocument createCMDocument(String key) {
		DTDGrammar model;
		try {
			model = (DTDGrammar) getLoader().loadGrammar(new XMLInputSource(null, key, null));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		if (model != null) {
			// XML Schema can be loaded
			return new DTDDocument(model);
		}
		return null;
	}

	public XMLDTDLoader getLoader() {
		if (loader == null) {
			loader = getSynchLoader();
		}
		return loader;
	}

	private synchronized XMLDTDLoader getSynchLoader() {
		if (loader != null) {
			return loader;
		}
		XMLDTDLoader loader = new XMLDTDLoader();
		loader.setEntityResolver(resolverExtensionManager);
		/*
		 * loader.setErrorHandler(new DOMErrorHandler() {
		 * 
		 * @Override public boolean handleError(DOMError error) { if
		 * (error.getRelatedException() instanceof CacheResourceDownloadingException) {
		 * throw ((CacheResourceDownloadingException) error.getRelatedException()); }
		 * return false; } });
		 */
		return loader;
	}

}
