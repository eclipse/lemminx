/**
 *  Copyright (c) 2018 Angelo ZERR.
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
package org.eclipse.lemminx.extensions.dtd.contentmodel;

import java.util.Collection;
import java.util.Collections;

import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMDocumentType;
import org.eclipse.lemminx.extensions.contentmodel.model.CMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelProvider;
import org.eclipse.lemminx.uriresolver.URIResolverExtensionManager;
import org.eclipse.lemminx.utils.DOMUtils;
import org.eclipse.lemminx.utils.StringUtils;

/**
 * DTD content model provider.
 */
public class CMDTDContentModelProvider implements ContentModelProvider {

	private final URIResolverExtensionManager resolverExtensionManager;

	public CMDTDContentModelProvider(URIResolverExtensionManager resolverExtensionManager) {
		this.resolverExtensionManager = resolverExtensionManager;
	}

	@Override
	public boolean adaptFor(DOMDocument document, boolean internal) {
		if (internal) {
			DOMDocumentType documentType = document.getDoctype();
			return documentType != null && !StringUtils.isEmpty(documentType.getInternalSubset());
		}
		return document.hasDTD();
	}

	@Override
	public boolean adaptFor(String uri) {
		return DOMUtils.isDTD(uri);
	}

	@Override
	public Collection<String> getSystemIds(DOMDocument xmlDocument, String namespaceURI) {
		/*
		 * <!DOCTYPE catalog PUBLIC
		 * "-//OASIS/DTD Entity Resolution XML Catalog V1.0//EN"
		 * "http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd">
		 */
		DOMDocumentType documentType = xmlDocument.getDoctype();
		return Collections.singleton(documentType.getSystemIdWithoutQuotes());
	}

	@Override
	public CMDocument createCMDocument(String key) {
		try {
			CMDTDDocument document = new CMDTDDocument(key);
			document.setEntityResolver(resolverExtensionManager);
			Grammar grammar = document.loadGrammar(new XMLInputSource(null, key, null));
			if (grammar != null) {
				// DTD can be loaded
				return document;
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	@Override
	public CMDocument createInternalCMDocument(DOMDocument xmlDocument) {
		try {
			CMDTDDocument document = new CMDTDDocument();
			document.setEntityResolver(resolverExtensionManager);
			DOMDocumentType documentType = xmlDocument.getDoctype();
			String internalSubset = documentType.getInternalSubset();
			String baseSystemId = null;
			String systemId = null;
			document.loadInternalDTD(internalSubset, baseSystemId, systemId);
			return document;
		} catch (Exception e) {
			return null;
		}
	}
}
