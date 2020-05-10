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
package org.eclipse.lemminx.extensions.xsd.contentmodel;

import org.apache.commons.lang3.ObjectUtils.Null;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xs.XSLoaderImpl;
import org.apache.xerces.xs.XSModel;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.NoNamespaceSchemaLocation;
import org.eclipse.lemminx.dom.SchemaLocation;
import org.eclipse.lemminx.dom.XMLModel;
import org.eclipse.lemminx.extensions.contentmodel.model.CMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelProvider;
import org.eclipse.lemminx.uriresolver.CacheResourceDownloadingException;
import org.eclipse.lemminx.uriresolver.URIResolverExtensionManager;
import org.eclipse.lemminx.utils.DOMUtils;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;

/**
 * XSD content model provider.
 */
public class CMXSDContentModelProvider implements ContentModelProvider {

	private final URIResolverExtensionManager resolverExtensionManager;

	private XSLoaderImpl loader;

	public CMXSDContentModelProvider(URIResolverExtensionManager resolverExtensionManager) {
		this.resolverExtensionManager = resolverExtensionManager;
	}

	@Override
	public boolean adaptFor(DOMDocument document, boolean internal) {
		if (internal) {
			return false;
		}
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
			} else { // TODO: should be done better, I guess --- FIXME: Does nothing
				XMLModel xmlModel = xmlDocument.getXMLModel();
				if (xmlModel == null){
					return null;
				} else {
					return xmlModel.getSchemaLocation();
				}
			}
		}
		//return null;
	}

	@Override
	public CMDocument createCMDocument(String key) {
		XSModel model = getLoader().loadURI(key);
		if (model != null) {
			// XML Schema can be loaded
			return new CMXSDDocument(model, key);
		}
		return null;
	}

	@Override
	public CMDocument createInternalCMDocument(DOMDocument xmlDocument) {
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
