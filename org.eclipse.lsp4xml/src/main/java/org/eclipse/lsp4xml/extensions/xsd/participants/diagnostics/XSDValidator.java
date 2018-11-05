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
package org.eclipse.lsp4xml.extensions.xsd.participants.diagnostics;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.parsers.XMLGrammarPreparser;
import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.dom.XMLDocument;

/**
 * XSD validator utilities class.
 *
 */
public class XSDValidator {

	private static final Logger LOGGER = Logger.getLogger(XSDValidator.class.getName());

	public static void doDiagnostics(XMLDocument document, XMLEntityResolver entityResolver,
			List<Diagnostic> diagnostics, CancelChecker monitor) {

		try {
			XMLGrammarPreparser grammarPreparser = new LSPXMLGrammarPreparser();
			grammarPreparser.registerPreparser(XMLGrammarDescription.XML_SCHEMA, null/* schemaLoader */);

			grammarPreparser.setProperty(Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY,
					new XMLGrammarPoolImpl());
			grammarPreparser.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE,
					false);
			grammarPreparser.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.NAMESPACES_FEATURE, true);
			grammarPreparser.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.NAMESPACE_PREFIXES_FEATURE, true);
			grammarPreparser.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.VALIDATION_FEATURE, true);
			grammarPreparser.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_VALIDATION_FEATURE, true);

			grammarPreparser.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE,
					true);
			grammarPreparser.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE,
					true);
			grammarPreparser.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.WARN_ON_DUPLICATE_ATTDEF_FEATURE,
					true);

			/*
			 * if(configuration.getFeature(XSDValidationConfiguration.
			 * HONOUR_ALL_SCHEMA_LOCATIONS)) { try {
			 * grammarPreparser.setFeature(Constants.XERCES_FEATURE_PREFIX +
			 * "honour-all-schemaLocations", true); //$NON-NLS-1$ } catch (Exception e) { //
			 * catch the exception and ignore } }
			 * 
			 * if(configuration.getFeature(XSDValidationConfiguration.
			 * FULL_SCHEMA_CONFORMANCE)) { try {
			 * grammarPreparser.setFeature(Constants.XERCES_FEATURE_PREFIX +
			 * Constants.SCHEMA_FULL_CHECKING, true); } catch (Exception e) { // ignore
			 * since we don't want to set it or can't. }
			 * 
			 * }
			 */

			// Add LSP content handler to stop XML parsing if monitor is canceled.
			// grammarPreparser.setContentHandler(new LSPContentHandler(monitor));

			// Add LSP error reporter to fill LSP diagnostics from Xerces errors
			grammarPreparser.setProperty("http://apache.org/xml/properties/internal/error-reporter",
					new LSPErrorReporterForXSD(document, diagnostics));

			if (entityResolver != null) {
				grammarPreparser.setEntityResolver(entityResolver);
			}

			try {
				String content = document.getText();
				String uri = document.getDocumentURI();
				InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
				XMLInputSource is = new XMLInputSource(null, uri, uri, inputStream, null);
				grammarPreparser.getLoader(XMLGrammarDescription.XML_SCHEMA);
				grammarPreparser.preparseGrammar(XMLGrammarDescription.XML_SCHEMA, is);
			} catch (Exception e) {
				// parser will return null pointer exception if the document is structurally
				// invalid
				// TODO: log error message
				// System.out.println(e);
			}
		} catch (Exception e) {
			// TODO: log error.
			// System.out.println(e);
		}
	}

}
