package org.eclipse.lsp4xml.contentmodel.participants;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.SchemaFactory;

import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.services.extensions.IDiagnosticsParticipant;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

public class ContentModelDiagnosticsParticipant implements IDiagnosticsParticipant {

	private static final String XML_DIAGNOSTIC_SOURCE = "xml";

	private CatalogResolver catalogResolver;

	public void didChangeConfiguration(DidChangeConfigurationParams params) {
		// TODO : load config to know XML catalog files.
	}

	@Override
	public void doDiagnostics(TextDocument document, List<Diagnostic> diagnostics, CancelChecker monitor) {
		String xmlContent = document.getText();

		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);

		String xmlSchemaFile = null;
		if (xmlSchemaFile != null) {
			try {
				factory.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
						.newSchema(new File(xmlSchemaFile)));
			} catch (SAXException saxException) {
				// TODO: create a diagnostic
			}
		} else {
			factory.setValidating(
					xmlContent.contains("schemaLocation") || xmlContent.contains("noNamespaceSchemaLocation"));
		}

		try {
			SAXParser parser = factory.newSAXParser();
			if (xmlSchemaFile == null) {
				parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
						"http://www.w3.org/2001/XMLSchema");
			}
			XMLReader reader = parser.getXMLReader();
			// reader.setProperty("http://apache.org/xml/properties/locale",
			// Locale.ENGLISH);

			// Error handler
			reader.setErrorHandler(new ErrorHandler() {

				@Override
				public void warning(SAXParseException e) throws SAXException {
					// Stop the validation quickly
					monitor.checkCanceled();

					Position start = new Position(e.getLineNumber() - 1, e.getColumnNumber() - 1);
					Position end = new Position(e.getLineNumber() - 1, e.getColumnNumber() - 1);
					String message = e.getLocalizedMessage();
					diagnostics.add(new Diagnostic(new Range(start, end), message, DiagnosticSeverity.Warning,
							XML_DIAGNOSTIC_SOURCE));
				}

				@Override
				public void error(SAXParseException e) throws SAXException {
					// Stop the validation quickly
					monitor.checkCanceled();

					Position start = new Position(e.getLineNumber() - 1, e.getColumnNumber() - 1);
					Position end = new Position(e.getLineNumber() - 1, e.getColumnNumber() - 1);
//					try {
//						int offset = document.offsetAt(start);
//						Node node = document.findNodeAt(offset);
//						if (node != null) {
//							start = document.positionAt(node.start);
//							end = document.positionAt(node.end);
//						}
//					} catch (BadLocationException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
					String message = e.getLocalizedMessage();
					diagnostics.add(new Diagnostic(new Range(start, end), message, DiagnosticSeverity.Error,
							XML_DIAGNOSTIC_SOURCE));
				}

				@Override
				public void fatalError(SAXParseException e) throws SAXException {
					// Stop the validation quickly
					monitor.checkCanceled();

					Position start = new Position(e.getLineNumber() - 1, e.getColumnNumber() - 1);
					Position end = new Position(e.getLineNumber() - 1, e.getColumnNumber() - 1);
					String message = e.getLocalizedMessage();
					diagnostics.add(new Diagnostic(new Range(start, end), message, DiagnosticSeverity.Error,
							XML_DIAGNOSTIC_SOURCE));
				}

			});

			// Parse XML
			String content = document.getText();
			String uri = document.getUri();
			InputSource inputSource = new InputSource();
			inputSource.setByteStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8.name())));
			inputSource.setSystemId(uri);
			reader.parse(inputSource);

		} catch (IOException | ParserConfigurationException | SAXException exception) {
		}
	}

	private CatalogResolver getCatalogResolver() {
		return catalogResolver;
	}

	private void setupXMLCatalog(String xmlCatalogFiles) {
		if (xmlCatalogFilesValid(xmlCatalogFiles)) {
			CatalogManager catalogManager = new CatalogManager();
			catalogManager.setUseStaticCatalog(false);
			catalogManager.setIgnoreMissingProperties(true);
			catalogManager.setCatalogFiles(xmlCatalogFiles);
			catalogResolver = new CatalogResolver(catalogManager);
		} else {
			// languageClient
			// .showMessage(new MessageParams(MessageType.Error,
			// XMLMessages.XMLDiagnostics_CatalogLoad_error));
			catalogResolver = null;
		}
	}

	private void clearXMLCatalog() {
		catalogResolver = null;
	}

	private boolean xmlCatalogFilesValid(String xmlCatalogFiles) {
		String[] paths = xmlCatalogFiles.split(";");
		for (int i = 0; i < paths.length; i++) {
			String currentPath = paths[i];
			File file = new File(currentPath);
			if (!file.exists()) {
				return false;
			}
		}
		return true;
	}

}
