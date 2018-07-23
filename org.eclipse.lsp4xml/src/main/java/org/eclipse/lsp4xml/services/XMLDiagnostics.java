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
package org.eclipse.lsp4xml.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.SchemaFactory;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.internal.parser.BadLocationException;
import org.eclipse.lsp4xml.model.Node;
import org.eclipse.lsp4xml.utils.XMLLogger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * XML diagnostics support.
 *
 */
class XMLDiagnostics {
	private static final XMLLogger logger = new XMLLogger(XMLDiagnostics.class.getName());
	private static final String XML_DIAGNOSTIC_SOURCE = "xml";

	private final XMLExtensionsRegistry extensionsRegistry;

	private SAXParserFactory factory;

	public XMLDiagnostics(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
		factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(true);
	}

	public List<Diagnostic> doDiagnostics(TextDocumentItem document, String xmlSchemaFile, CancelChecker monitor) {
		List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
		String xmlContent = document.getText();

		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);

		if (xmlSchemaFile != null) {
			try {
				factory.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
						.newSchema(new File(xmlSchemaFile)));
			} catch (SAXException e) {
				logger.logCatch(e);
			}
		} else {
			factory.setValidating(
					xmlContent.contains("schemaLocation") || xmlContent.contains("noNamespaceSchemaLocation"));
		}

		try {
			SAXParser parser = factory.newSAXParser();
            if (xmlSchemaFile == null) {
                parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
            }
			XMLReader reader = parser.getXMLReader();
			// reader.setProperty("http://apache.org/xml/properties/locale",
			// Locale.ENGLISH);

			// Error handler
			reader.setErrorHandler(new ErrorHandler() {

				@Override
				public void warning(SAXParseException e) throws SAXException {
					Position start = new Position(e.getLineNumber() - 1, e.getColumnNumber() - 1);
					Position end = new Position(e.getLineNumber() - 1, e.getColumnNumber() - 1);
					String message = e.getLocalizedMessage();
					diagnostics.add(new Diagnostic(new Range(start, end), message, DiagnosticSeverity.Warning,
							XML_DIAGNOSTIC_SOURCE));
				}

				@Override
				public void error(SAXParseException e) throws SAXException {
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

		} catch (IOException | ParserConfigurationException | SAXException e) {
			logger.logCatch(e);
		}
		return diagnostics;
	}

}
