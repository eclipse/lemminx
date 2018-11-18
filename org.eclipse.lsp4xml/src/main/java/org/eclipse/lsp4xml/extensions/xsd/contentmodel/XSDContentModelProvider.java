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
package org.eclipse.lsp4xml.extensions.xsd.contentmodel;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xs.XSLoaderImpl;
import org.apache.xerces.xs.XSModel;
import org.eclipse.lsp4xml.dom.NoNamespaceSchemaLocation;
import org.eclipse.lsp4xml.dom.SchemaLocation;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelProvider;
import org.eclipse.lsp4xml.uriresolver.CacheResourceDownloadingException;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtensionManager;
import org.eclipse.lsp4xml.utils.DOMUtils;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;

/**
 * XSD content model provider.
 */
public class XSDContentModelProvider implements ContentModelProvider {

	private final URIResolverExtensionManager resolverExtensionManager;

	private XSLoaderImpl loader;

	public XSDContentModelProvider(URIResolverExtensionManager resolverExtensionManager) {
		this.resolverExtensionManager = resolverExtensionManager;
	}

	@Override
	public boolean adaptFor(DOMDocument document) {
		return document.hasSchemaLocation() || document.hasNoNamespaceSchemaLocation();
	}

	@Override
	public boolean adaptFor(String uri) {
		return DOMUtils.isXSD(uri);
	}

	@Override
	public String getSystemId(DOMDocument xmlDocument, String namespaceURI) {
		SchemaLocation schemaLocation = xmlDocument.getSchemaLocation();
		if (schemaLocation != null) {
			return schemaLocation.getLocationHint(namespaceURI);
		} else {
			NoNamespaceSchemaLocation noNamespaceSchemaLocation = xmlDocument.getNoNamespaceSchemaLocation();
			if (noNamespaceSchemaLocation != null) {
				if (namespaceURI != null) {
					// xsi:noNamespaceSchemaLocation doesn't define namespaces
					return null;
				}
				return noNamespaceSchemaLocation.getLocation();
			}
		}
		return null;
	}

	@Override
	public CMDocument createCMDocument(String key) {
		XSModel model = getLoader().loadURI(key);
		if (model != null) {
			// XML Schema can be loaded
			return new XSDDocument(model);
		}
		return null;
	}

	public XSLoaderImpl getLoader() {
		if (loader == null) {
			loader = getSynchLoader();
		}
		return loader;
	}

	private synchronized XSLoaderImpl getSynchLoader() {
		if (loader != null) {
			return loader;
		}
		XSLoaderImpl loader = new XSLoaderImpl();
		loader.setParameter("http://apache.org/xml/properties/internal/entity-resolver", resolverExtensionManager);
		loader.setParameter(Constants.DOM_ERROR_HANDLER, new DOMErrorHandler() {

			@Override
			public boolean handleError(DOMError error) {
				if (error.getRelatedException() instanceof CacheResourceDownloadingException) {
					throw ((CacheResourceDownloadingException) error.getRelatedException());
				}
				return false;
			}
		});
		return loader;
	}

}
