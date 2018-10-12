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
package org.eclipse.lsp4xml.extensions.contentmodel.participants.diagnostics;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xerces.parsers.SAXParser;
import org.apache.xerces.parsers.XMLGrammarCachingConfiguration;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.uriresolver.CacheResourceLoadingException;
import org.eclipse.lsp4xml.uriresolver.IExternalSchemaLocationProvider;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * XML validator utilities class.
 *
 */
public class XMLValidator {

	private static final Logger LOGGER = Logger.getLogger(XMLValidator.class.getName());

	public static void doDiagnostics(XMLDocument document, XMLEntityResolver entityResolver,
			IExternalSchemaLocationProvider externalSchemaLocationProvider, List<Diagnostic> diagnostics,
			CancelChecker monitor) {

		try {
			XMLParserConfiguration configuration = new XMLGrammarCachingConfiguration();
			SAXParser reader = new SAXParser(configuration);

			// Add LSP error reporter to fill LSP diagnostics from Xerces errors
			reader.setProperty("http://apache.org/xml/properties/internal/error-reporter",
					new LSPErrorReporter(document, diagnostics));
			reader.setFeature("http://apache.org/xml/features/continue-after-fatal-error", false); //$NON-NLS-1$
			reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true /* document.hasNamespaces() */); //$NON-NLS-1$
			reader.setFeature("http://xml.org/sax/features/namespaces", true /* document.hasNamespaces() */); //$NON-NLS-1$

			// Add LSP content handler to stop XML parsing if monitor is canceled.
			reader.setContentHandler(new LSPContentHandler(monitor));

			if (entityResolver != null) {
				reader.setProperty("http://apache.org/xml/properties/internal/entity-resolver", entityResolver); //$NON-NLS-1$
			}

			boolean hasGrammar = document.hasGrammar();
			if (!hasGrammar) {
				hasGrammar = checkExternalSchema(new URI(document.getUri()), externalSchemaLocationProvider, reader);
			}
			reader.setFeature("http://xml.org/sax/features/validation", hasGrammar); //$NON-NLS-1$
			reader.setFeature("http://apache.org/xml/features/validation/schema", hasGrammar); //$NON-NLS-1$

			// Parse XML
			String content = document.getText();
			String uri = document.getUri();
			InputSource inputSource = new InputSource();
			inputSource.setByteStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
			inputSource.setSystemId(uri);
			reader.parse(inputSource);

		} catch (IOException | SAXException | CancellationException | CacheResourceLoadingException exception) {
			// ignore error
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Unexpected XMLValidator error", e);
		}
	}

	private static boolean checkExternalSchema(URI fileURI,
			IExternalSchemaLocationProvider externalSchemaLocationProvider, SAXParser reader)
			throws SAXNotRecognizedException, SAXNotSupportedException {
		boolean hasGrammar = false;
		if (externalSchemaLocationProvider != null) {
			Map<String, String> result = externalSchemaLocationProvider.getExternalSchemaLocation(fileURI);
			if (result != null) {
				String noNamespaceSchemaLocation = result
						.get(IExternalSchemaLocationProvider.NO_NAMESPACE_SCHEMA_LOCATION);
				if (noNamespaceSchemaLocation != null) {
					reader.setProperty(IExternalSchemaLocationProvider.NO_NAMESPACE_SCHEMA_LOCATION,
							noNamespaceSchemaLocation);
					hasGrammar = true;
				}
			}
		}
		return hasGrammar;
	}
}
