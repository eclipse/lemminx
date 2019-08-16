/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.xsd.contentmodel;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xs.XSLoaderImpl;
import org.apache.xerces.impl.xs.models.CMBuilder;
import org.apache.xerces.impl.xs.models.CMNodeFactory;
import org.apache.xerces.xs.XSModel;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.NoNamespaceSchemaLocation;
import org.eclipse.lsp4xml.dom.SchemaLocation;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelProvider;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lsp4xml.uriresolver.CacheResourceDownloadingException;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtensionManager;
import org.eclipse.lsp4xml.utils.DOMUtils;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;

/**
 * XSD content model provider.
 */
public class CMXSDContentModelProvider implements ContentModelProvider {

	private static final String XML_SCHEMA_VERSION = Constants.XERCES_PROPERTY_PREFIX
			+ Constants.XML_SCHEMA_VERSION_PROPERTY;

	private final URIResolverExtensionManager resolverExtensionManager;

	private final ContentModelManager modelManager;

	private XSLoaderImpl loader;

	private CMBuilder cmBuilder;

	public CMXSDContentModelProvider(URIResolverExtensionManager resolverExtensionManager,
			ContentModelManager modelManager) {
		this.resolverExtensionManager = resolverExtensionManager;
		this.modelManager = modelManager;
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
			}
		}
		return null;
	}

	@Override
	public CMDocument createCMDocument(String key) {
		XSModel model = getLoader().loadURI(key);
		if (model != null) {
			// XML Schema can be loaded
			return new CMXSDDocument(model, key, cmBuilder);
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
		String version = XMLValidationSettings.getNamespaceSchemaVersion(modelManager.getSettings());
		loader.setParameter(XML_SCHEMA_VERSION, version);
		cmBuilder = new CMBuilder(new CMNodeFactory());
		cmBuilder.setSchemaVersion(getSchemaVersion(version));
		return loader;
	}

	private static short getSchemaVersion(String version) {
		if (Constants.W3C_XML_SCHEMA10_NS_URI.equals(version)) {
			return Constants.SCHEMA_VERSION_1_0;
		} else if (Constants.W3C_XML_SCHEMA11_NS_URI.equals(version)) {
			return Constants.SCHEMA_VERSION_1_1;
		}
		return Constants.SCHEMA_VERSION_1_0_EXTENDED;
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
