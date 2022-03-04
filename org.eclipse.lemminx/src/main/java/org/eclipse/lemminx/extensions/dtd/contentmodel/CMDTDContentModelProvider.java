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

import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMDocumentType;
import org.eclipse.lemminx.dom.DOMRange;
import org.eclipse.lemminx.extensions.contentmodel.model.CMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelProvider;
import org.eclipse.lemminx.extensions.xerces.LSPSecurityManager;
import org.eclipse.lemminx.extensions.xerces.LSPXMLEntityManager;
import org.eclipse.lemminx.uriresolver.URIResolverExtensionManager;
import org.eclipse.lemminx.utils.DOMUtils;
import org.eclipse.lemminx.utils.StringUtils;

/**
 * DTD content model provider.
 */
public class CMDTDContentModelProvider implements ContentModelProvider {

	private static final String DOCTYPE_BINDING_KIND = "doctype";

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
	public Collection<Identifier> getIdentifiers(DOMDocument xmlDocument, String namespaceURI) {
		/*
		 * <!DOCTYPE catalog PUBLIC
		 * "-//OASIS/DTD Entity Resolution XML Catalog V1.0//EN"
		 * "http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd">
		 */
		DOMDocumentType documentType = xmlDocument.getDoctype();
		DOMRange range = documentType;
		if (documentType.getSystemIdNode() != null) {
			range = documentType.getSystemIdNode();
		} else if (documentType.getPublicIdNode() != null) {
			range = documentType.getPublicIdNode();
		}
		return Collections.singleton(new Identifier(documentType.getPublicIdWithoutQuotes(),
				documentType.getSystemIdWithoutQuotes(), range, DOCTYPE_BINDING_KIND));
	}

	@Override
	public CMDocument createCMDocument(String key, boolean resolveExternalEntities) {
		try {
			CMDTDDocument document = newCMDocument(key, resolveExternalEntities);
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
	public CMDocument createInternalCMDocument(DOMDocument xmlDocument, boolean resolveExternalEntities) {
		// This method create a CMDocument for DOCTYPE subset to manage XML completion,
		// hover based on DTD
		try {
			DOMDocumentType documentType = xmlDocument.getDoctype();
			String internalSubset = documentType != null ? documentType.getInternalSubset() : null;
			if (internalSubset == null) {
				return null;
			}
			CMDTDDocument document = newCMDocument(null, resolveExternalEntities);
			String baseSystemId = null;
			String systemId = null;
			document.loadInternalDTD(internalSubset, baseSystemId, systemId);
			return document;
		} catch (Exception e) {
			// Don't log error, because DOCTYPE subset is never valid when user type some
			// content in the DTD subset
			return null;
		}
	}

	private CMDTDDocument newCMDocument(String key, boolean resolveExternalEntities) {
		// Create Xerces XML entity manager configured with:
		// - the LemMinX Uri resolver.
		// - resolveExternalEntities coming from the settings.
		LSPXMLEntityManager entityManager = new LSPXMLEntityManager();
		// Update the resolver external entities capability
		entityManager.setResolveExternalEntities(resolveExternalEntities);
		// Update the resolver
		entityManager.setEntityResolver(resolverExtensionManager);
		// Update the security manager
		entityManager.setSecurityManager(LSPSecurityManager.getSecurityManager());
		return new CMDTDDocument(key, entityManager, new XMLErrorReporter(), resolverExtensionManager);
	}

}
