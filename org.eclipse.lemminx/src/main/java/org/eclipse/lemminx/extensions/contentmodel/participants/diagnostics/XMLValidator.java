/**
 *  Copyright (c) 2018-2020 Angelo ZERR
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
package org.eclipse.lemminx.extensions.contentmodel.participants.diagnostics;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;

import org.apache.xerces.parsers.SAXParser;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMDocumentType;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.XMLModel;
import org.eclipse.lemminx.extensions.contentmodel.participants.XMLSchemaErrorCode;
import org.eclipse.lemminx.extensions.contentmodel.participants.XMLSyntaxErrorCode;
import org.eclipse.lemminx.extensions.contentmodel.settings.ContentModelSettings;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.services.extensions.diagnostics.LSPContentHandler;
import org.eclipse.lemminx.uriresolver.CacheResourceDownloadingException;
import org.eclipse.lemminx.uriresolver.IExternalSchemaLocationProvider;
import org.eclipse.lemminx.uriresolver.URIResolverExtension;
import org.eclipse.lemminx.uriresolver.URIResolverExtensionManager;
import org.eclipse.lemminx.utils.URIUtils;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;

/**
 * XML validator utilities class.
 *
 */
public class XMLValidator {

	private static final Logger LOGGER = Logger.getLogger(XMLValidator.class.getName());

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

			SAXParser parser = new LSPSAXParser(document, reporter, configuration, grammarPool);

			// Add LSP content handler to stop XML parsing if monitor is canceled.
			parser.setContentHandler(new LSPContentHandler(monitor));

			boolean hasGrammar = document.hasGrammar(true);

			
			//#region RNG

			// TODO: proof of concept, not finished!

			List<XMLModel> rngModels = new ArrayList<>();
			for (XMLModel model : document.getXMLModels()) {
				if (model.isRNG()) {
					rngModels.add(model);
				}
			}
			if (!rngModels.isEmpty()){
				doRNGDiagnostics(document, rngModels, diagnostics);
			}
			// TODO: how should xsd and rng model coexist/covalidate?

			//#endregion


			// If diagnostics for Schema preference is enabled
			if ((validationSettings == null) || validationSettings.isSchema()) {

				checkExternalSchema(document.getExternalSchemaLocation(), parser);

				parser.setFeature("http://apache.org/xml/features/validation/schema", hasGrammar); //$NON-NLS-1$

				// warn if XML document is not bound to a grammar according the settings
				warnNoGrammar(document, diagnostics, contentModelSettings);
			} else {
				hasGrammar = false; // validation for Schema was disabled
			}
			parser.setFeature("http://xml.org/sax/features/validation", hasGrammar); //$NON-NLS-1$

			// Parse XML
			String content = document.getText();
			String uri = document.getDocumentURI();
			parseXML(content, uri, parser);
		} catch (IOException | SAXException | CancellationException exception) {
			// ignore error
		} catch (CacheResourceDownloadingException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Unexpected XMLValidator error", e);
		}
	}

	private static void doRNGDiagnostics(DOMDocument document, List<XMLModel> rngModels, List<Diagnostic> diagnostics) {
		for (XMLModel xmlModel : rngModels) {
			doRNGDiagnostics(document, xmlModel.getHref(), diagnostics);
		}
	}

	private static void doRNGDiagnostics(DOMDocument document, String href, List<Diagnostic> diagnostics) {
		JingErrorHandler handler = new JingErrorHandler();
		
        PropertyMapBuilder mapBuilder = new PropertyMapBuilder();
        mapBuilder.put(ValidateProperty.ERROR_HANDLER, handler);
		PropertyMap propertyMap = mapBuilder.toPropertyMap();

		// String uri = Paths.get(href).toAbsolutePath().toUri().toString();
		// boolean exists = Files.exists(Paths.get(href));
		// InputSource schemaSource = new InputSource(uri);

		try {
			String s1 = URIUtils.sanitizingUri(href);
			boolean b1 = URIUtils.isFileResource(href);
			boolean b2 = URIUtils.isRemoteResource(href);

			StringBuilder schema = new StringBuilder();
			Files.lines(Paths.get(href)).forEach(s -> schema.append(s).append("\n"));
			InputSource schemaSource = new InputSource(XMLValidator.class.getResourceAsStream("/rng/tei_all.rng")); // FIXME: make dynamic
			// LOGGER.info(XMLValidator.class.getResource("/rng/core.rng").toString());
			// LOGGER.info(""+XMLValidator.class.getResourceAsStream("/rng/core.rng"));
			// InputSource schemaSource = new InputSource(new StringReader(schema.toString()));
			// InputSource schemaSource = new InputSource(Files.newInputStream(Paths.get(href)));
			// TODO: find reasonable way to load resource
			InputSource documentSource = new InputSource(new StringReader(document.getText()));

			ValidationDriver driver = new ValidationDriver(propertyMap);
			driver.loadSchema(schemaSource);
			// FIXME: schema == null

			boolean valid = driver.validate(documentSource);

			for (SAXParseException error : handler.getErrors()) {
				diagnostics.add(JingErrorHandler.parseDiagnostic(error, DiagnosticSeverity.Error));
			}

			for (SAXParseException warning : handler.getWarnings()) {
				diagnostics.add(JingErrorHandler.parseDiagnostic(warning, DiagnosticSeverity.Warning));
			}

			if (valid) {
				// TODO: warnings?
				LOGGER.info("RNG Validator found no problems. Warnings: "+handler.getWarnings().size());
			} else {
				// TODO: do something with the errors
				LOGGER.info("RNG Validator found problems: "+handler.getErrors().size());
			}
		} catch (Exception e) {
			// TODO: handle exceptions
			e.printStackTrace();
		}
	}

	private static void parseXML(String content, String uri, SAXParser parser) throws SAXException, IOException {
		InputSource inputSource = new InputSource();
		inputSource.setCharacterStream(new StringReader(content));
		inputSource.setSystemId(uri);
		parser.parse(inputSource);
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
				range = XMLPositionUtility.selectStartTagName(documentElement);
			}
			if (range == null) {
				range = new Range(new Position(0, 0), new Position(0, 0));
			}
			diagnostics.add(new Diagnostic(range, "No grammar constraints (DTD or XML Schema).", severity,
					document.getDocumentURI(), XMLSyntaxErrorCode.NoGrammarConstraints.name()));
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

	private static class JingErrorHandler implements ErrorHandler {
		public static final String UNKNOWN_ERROR_CODE = "unknownErrorCode";
		private List<SAXParseException> warnings = new ArrayList<>();
		private List<SAXParseException> errors = new ArrayList<>();
	
		public static Diagnostic parseDiagnostic(SAXParseException ex, DiagnosticSeverity severity) {
			Diagnostic d = new Diagnostic();

			// TODO: better way of calculating the length of the range
			int length = 5;
			Position start = new Position(ex.getLineNumber(), Math.max(0, ex.getColumnNumber()-length));
			Position end = new Position(ex.getLineNumber(), ex.getColumnNumber());
			d.setRange(new Range(start, end));

			d.setMessage(ex.getMessage());
			d.setCode(getErrorCode(ex.getMessage()));
			d.setSeverity(severity);

			// TODO: severity
			return d;
		}

		public static String getErrorCode(String message) {
			String pattern;

			// unexpected element
			// e.g. "element "am" not allowed here; expected element "titleStmt""
			pattern = "^element \".*?\" not allowed here; expected element .*";
			if (message.matches(pattern)){
				return XMLSchemaErrorCode.cvc_complex_type_2_4_a.getCode();
			}

			// unexpected attribute
			// e.g. "attribute "foo" not allowed here; expected attribute "ana", "cert", "change", "copyOf", "corresp", "exclude", "facs", "n", "next", "prev", "rend", "rendition", "resp", "sameAs", "select", "source", "style", "synch", "xml:base", "xml:id", "xml:lang" or "xml:space""
			pattern = "^attribute \".*?\" not allowed here; expected attribute .*";
			if (message.matches(pattern)){
				return XMLSchemaErrorCode.cvc_complex_type_3_2_2.getCode();
			}

			// TODO: more cases

			// unknown error
			return UNKNOWN_ERROR_CODE;
		}

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			warnings.add(exception);
		}

		@Override
		public void error(SAXParseException exception) throws SAXException {
			errors.add(exception);
		}
	
		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			throw exception;
		}
	
		public List<SAXParseException> getErrors() {
			return errors;
		}
	
		public List<SAXParseException> getWarnings() {
			return warnings;
		}
	}
}
