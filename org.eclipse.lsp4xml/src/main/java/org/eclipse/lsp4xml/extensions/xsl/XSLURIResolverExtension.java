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
package org.eclipse.lsp4xml.extensions.xsl;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.lsp4xml.dom.Element;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.services.IXMLDocumentProvider;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtension;
import org.eclipse.lsp4xml.utils.FilesUtils;

/**
 * Resolve the XSL XML Schema to use according the xsl:stylesheet/@version
 *
 */
public class XSLURIResolverExtension implements URIResolverExtension {

	/**
	 * The XSL namespace URI (= http://www.w3.org/1999/XSL/Transform)
	 */
	private static final String XSL_NAMESPACE_URI = "http://www.w3.org/1999/XSL/Transform"; //$NON-NLS-1$

	private final IXMLDocumentProvider documentProvider;

	public XSLURIResolverExtension(IXMLDocumentProvider documentProvider) {
		this.documentProvider = documentProvider;
	}

	@Override
	public String resolve(String baseLocation, String publicId, String systemId) {
		if (!XSL_NAMESPACE_URI.equals(publicId)) {
			return null;
		} else {

		}
		String version = getVersion(baseLocation);
		if (version == null) {
			return null;
		}

		String schemaFileName = "xslt-" + version + ".xsd";
		String schemaPath = "schemas/xslt/" + schemaFileName;
		try {
			Path outFile = FilesUtils.getDeployedPath(Paths.get(schemaPath));
			if (!outFile.toFile().exists()) {
				try (InputStream in = XSLURIResolverExtension.class.getResourceAsStream("/" + schemaPath)) {
					FilesUtils.saveToFile(in, outFile);
				}
			}
			return outFile.toFile().toURI().toString();
		} catch (Exception e) {
			// Do nothing?
		}
		return null;
	}

	/**
	 * Returns the version coming from xsl:stylesheet/@version of the XML document
	 * retrieved by the given uri
	 * 
	 * @param uri
	 * @return the version coming from xsl:stylesheet/@version of the XML document
	 *         retrieved by the given uri
	 */
	private String getVersion(String uri) {
		if (documentProvider == null) {
			return null;
		}
		XMLDocument document = documentProvider.getDocument(uri);
		if (document != null) {
			Element element = document.getDocumentElement();
			if (element != null) {
				String version = element.getAttributeValue("version");
				if (version != null) {
					return version;
				}
			}
		}
		return "1.0";
	}

}
