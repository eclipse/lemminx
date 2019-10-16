/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.contentmodel.participants.diagnostics;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.parsers.SAXParser;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMDocumentType;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.DTDErrorCode;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.ContentModelSettings;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lsp4xml.services.extensions.diagnostics.LSPContentHandler;
import org.eclipse.lsp4xml.uriresolver.CacheResourceDownloadingException;
import org.eclipse.lsp4xml.uriresolver.IExternalSchemaLocationProvider;
import org.eclipse.lsp4xml.utils.XMLPositionUtility;
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

	private static final String DTD_NOT_FOUND = "Cannot find DTD ''{0}''.\nCreate the DTD file or configure an XML catalog for this DTD.";

	public static void doDiagnostics(DOMDocument document, XMLEntityResolver entityResolver,
			List<Diagnostic> diagnostics, ContentModelSettings contentModelSettings, XMLGrammarPool grammarPool,
			CancelChecker monitor) {
		try {
			XMLValidationSettings validationSettings = contentModelSettings != null
					? contentModelSettings.getValidation()
					: null;
			LSPXMLParserConfiguration configuration = new LSPXMLParserConfiguration(grammarPool,
					isDisableOnlyDTDValidation(document), validationSettings);

			if (entityResolver != null) {
				configuration.setProperty("http://apache.org/xml/properties/internal/entity-resolver", entityResolver); //$NON-NLS-1$
			}

			final LSPErrorReporterForXML reporter = new LSPErrorReporterForXML(document, diagnostics);
			boolean externalDTDValid = checkExternalDTD(document, reporter, configuration);
			SAXParser parser = new SAXParser(configuration);
			// Add LSP error reporter to fill LSP diagnostics from Xerces errors
			parser.setProperty("http://apache.org/xml/properties/internal/error-reporter", reporter);
			parser.setFeature("http://apache.org/xml/features/continue-after-fatal-error", false); //$NON-NLS-1$
			parser.setFeature("http://xml.org/sax/features/namespace-prefixes", true /* document.hasNamespaces() */); //$NON-NLS-1$
			parser.setFeature("http://xml.org/sax/features/namespaces", true /* document.hasNamespaces() */); //$NON-NLS-1$

			// Add LSP content handler to stop XML parsing if monitor is canceled.
			parser.setContentHandler(new LSPContentHandler(monitor));

			boolean hasGrammar = document.hasGrammar();

			// If diagnostics for Schema preference is enabled
			if ((validationSettings == null) || validationSettings.isSchema()) {

				checkExternalSchema(document.getExternalSchemaLocation(), parser);

				parser.setFeature("http://apache.org/xml/features/validation/schema", hasGrammar); //$NON-NLS-1$

				// warn if XML document is not bound to a grammar according the settings
				warnNoGrammar(document, diagnostics, contentModelSettings);
			} else {
				hasGrammar = false; // validation for Schema was disabled
			}

			parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", externalDTDValid);
			parser.setFeature("http://xml.org/sax/features/validation", hasGrammar && externalDTDValid); //$NON-NLS-1$

			// Parse XML
			String content = document.getText();
			String uri = document.getDocumentURI();
			InputSource inputSource = new InputSource();
			inputSource.setByteStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
			inputSource.setSystemId(uri);
			parser.parse(inputSource);

		} catch (IOException | SAXException | CancellationException exception) {
			// ignore error
		} catch (CacheResourceDownloadingException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Unexpected XMLValidator error", e);
		}
	}

	/**
	 * Returns true is DTD validation must be disabled and false otherwise.
	 * 
	 * @param document the DOM document
	 * @return true is DTD validation must be disabled and false otherwise.
	 */
	private static boolean isDisableOnlyDTDValidation(DOMDocument document) {
		// When XML declares a DOCTYPE only to define entities like
		// <!DOCTYPE root [
		// <!ENTITY foo "Bar">
		// ]>
		// Xerces try to validate the XML and report an error on each XML elements
		// because they are not declared in the DOCTYPE.
		// In this case, DTD validation must be disabled.
		if (!document.hasDTD()) {
			return false;
		}
		DOMDocumentType docType = document.getDoctype();
		if (docType.getKindNode() != null) {
			return false;
		}
		// Disable the DTD validation only if there are not <!ELEMENT or an <!ATTRLIST
		return !docType.getChildren().stream().anyMatch(node -> node.isDTDElementDecl() || node.isDTDAttListDecl());
	}

	/**
	 * Returns true if the given document has a valid DTD (or doesn't define a DTD)
	 * and false otherwise.
	 * 
	 * @param document      the DOM document
	 * @param reporter      the reporter
	 * @param configuration the configuration
	 * @return true if the given document has a valid DTD (or doesn't define a DTD)
	 *         and false otherwise.
	 */
	private static boolean checkExternalDTD(DOMDocument document, LSPErrorReporterForXML reporter,
			XMLParserConfiguration configuration) {
		if (!document.hasDTD()) {
			return true;
		}
		DOMDocumentType docType = document.getDoctype();
		if (docType.getKindNode() == null) {
			return true;
		}

		// When XML is bound with a DTD path which doesn't exist, Xerces throws an
		// IOException which breaks the validation of XML syntax instead of reporting it
		// (like XML Schema). Here we parse only the
		// DOCTYPE to catch this error. If there is an error
		// the next validation with be disabled by using
		// http://xml.org/sax/features/validation &
		// http://apache.org/xml/features/nonvalidating/load-external-dtd (disable uses
		// of DTD for validation)

		// Parse only the DOCTYPE of the DOM document

		int end = document.getDoctype().getEnd();
		String xml = document.getText().substring(0, end);
		xml += "<root/>";
		try {

			// Customize the entity manager to collect the error when DTD doesn't exist.
			XMLEntityManager entityManager = new XMLEntityManager() {
				@Override
				public String setupCurrentEntity(String name, XMLInputSource xmlInputSource, boolean literal,
						boolean isExternal) throws IOException, XNIException {
					// Catch the setupCurrentEntity method which throws an IOException when DTD is
					// not found
					try {
						return super.setupCurrentEntity(name, xmlInputSource, literal, isExternal);
					} catch (IOException e) {
						// Report the DTD invalid error
						try {
							Range range = new Range(document.positionAt(docType.getSystemIdNode().getStart()),
									document.positionAt(docType.getSystemIdNode().getEnd()));
							reporter.addDiagnostic(range,
									MessageFormat.format(DTD_NOT_FOUND, xmlInputSource.getSystemId()),
									DiagnosticSeverity.Error, DTDErrorCode.dtd_not_found.getCode());
						} catch (BadLocationException e1) {
							// Do nothing
						}
						throw e;
					}
				}
			};
			entityManager.reset(configuration);

			SAXParser parser = new SAXParser(configuration);
			parser.setProperty("http://apache.org/xml/properties/internal/entity-manager", entityManager);
			parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);

			InputSource inputSource = new InputSource();
			inputSource.setByteStream(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
			inputSource.setSystemId(document.getDocumentURI());
			parser.parse(inputSource);
		} catch (SAXException | CancellationException exception) {
			// ignore error
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Warn if XML document is not bound to a grammar according the settings
	 * 
	 * @param document    the XML document
	 * @param diagnostics the diagnostics list to populate
	 * @param settings    the settings to use to know the severity of warn.
	 */
	private static void warnNoGrammar(DOMDocument document, List<Diagnostic> diagnostics,
			ContentModelSettings settings) {
		boolean hasGrammar = document.hasGrammar();
		if (hasGrammar) {
			return;
		}
		// By default "hint" settings.
		DiagnosticSeverity severity = XMLValidationSettings.getNoGrammarSeverity(settings);
		if (severity == null) {
			// "ignore" settings
			return;
		}
		if (!hasGrammar) {
			// No grammar, add a warn diagnostic with the severity coming from the settings.
			Range range = null;
			DOMElement documentElement = document.getDocumentElement();
			if (documentElement != null) {
				range = XMLPositionUtility.selectStartTag(documentElement);
			}
			if (range == null) {
				range = new Range(new Position(0, 0), new Position(0, 0));
			}
			diagnostics.add(new Diagnostic(range, "No grammar constraints (DTD or XML Schema).", severity,
					document.getDocumentURI(), "XML"));
		}
	}

	private static void checkExternalSchema(Map<String, String> result, SAXParser reader)
			throws SAXNotRecognizedException, SAXNotSupportedException {
		if (result != null) {
			String noNamespaceSchemaLocation = result.get(IExternalSchemaLocationProvider.NO_NAMESPACE_SCHEMA_LOCATION);
			if (noNamespaceSchemaLocation != null) {
				reader.setProperty(IExternalSchemaLocationProvider.NO_NAMESPACE_SCHEMA_LOCATION,
						noNamespaceSchemaLocation);
			}
		}
	}
}
