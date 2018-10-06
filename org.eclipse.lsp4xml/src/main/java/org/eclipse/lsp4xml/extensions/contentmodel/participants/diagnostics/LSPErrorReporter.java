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

import static org.eclipse.lsp4xml.utils.XMLPositionUtility.toLSPPosition;

import java.util.List;

import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.impl.xs.XSMessageFormatter;
import org.apache.xerces.util.MessageFormatter;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLParseException;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.DTDErrorCode;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.XMLSchemaErrorCode;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.XMLSyntaxErrorCode;
import org.xml.sax.ErrorHandler;

/**
 * The SAX {@link ErrorHandler} gives just information of the offset where there
 * is an error. To improve highlight XML error, this class extends the Xerces
 * XML reporter to catch location, key, arguments which is helpful to adjust the
 * LSP range.
 *
 */
public class LSPErrorReporter extends XMLErrorReporter {

	private static final String XML_DIAGNOSTIC_SOURCE = "xml";

	private final XMLDocument xmlDocument;
	private final List<Diagnostic> diagnostics;

	public LSPErrorReporter(XMLDocument xmlDocument, List<Diagnostic> diagnostics) {
		this.xmlDocument = xmlDocument;
		this.diagnostics = diagnostics;
		XMLMessageFormatter xmft = new XMLMessageFormatter();
		super.putMessageFormatter(XMLMessageFormatter.XML_DOMAIN, xmft);
		super.putMessageFormatter(XMLMessageFormatter.XMLNS_DOMAIN, xmft);
		super.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN, new XSMessageFormatter());
	}

	public String reportError(XMLLocator location, String domain, String key, Object[] arguments, short severity,
			Exception exception) throws XNIException {
		// format message
		MessageFormatter messageFormatter = getMessageFormatter(domain);
		String message;
		if (messageFormatter != null) {
			message = messageFormatter.formatMessage(fLocale, key, arguments);
		} else {
			StringBuffer str = new StringBuffer();
			str.append(domain);
			str.append('#');
			str.append(key);
			int argCount = arguments != null ? arguments.length : 0;
			if (argCount > 0) {
				str.append('?');
				for (int i = 0; i < argCount; i++) {
					str.append(arguments[i]);
					if (i < argCount - 1) {
						str.append('&');
					}
				}
			}
			message = str.toString();
		}

		// Fill diagnostic
		diagnostics.add(new Diagnostic(toLSPRange(location, key, arguments, xmlDocument), message,
				toLSPSeverity(severity), XML_DIAGNOSTIC_SOURCE, key));

		if (severity == SEVERITY_FATAL_ERROR && !fContinueAfterFatalError) {
			XMLParseException parseException = (exception != null) ? new XMLParseException(location, message, exception)
					: new XMLParseException(location, message);
			throw parseException;
		}
		return message;
	}

	/**
	 * Returns the LSP diagnostic severity according the SAX severity.
	 * 
	 * @param severity the SAX severity
	 * @return the LSP diagnostic severity according the SAX severity.
	 */
	private static DiagnosticSeverity toLSPSeverity(int severity) {
		switch (severity) {
		case SEVERITY_WARNING:
			return DiagnosticSeverity.Warning;
		default:
			return DiagnosticSeverity.Error;
		}
	}

	/**
	 * Create the LSP range from the SAX error.
	 * 
	 * @param location
	 * @param key
	 * @param arguments
	 * @param document
	 * @return the LSP range from the SAX error.
	 */
	private static Range toLSPRange(XMLLocator location, String key, Object[] arguments, XMLDocument document) {
		if (location == null) {
			Position start = toLSPPosition(0, location, document.getTextDocument());
			Position end = toLSPPosition(0, location, document.getTextDocument());
			return new Range(start, end);
		}
		// try adjust positions for XML syntax error
		XMLSyntaxErrorCode syntaxCode = XMLSyntaxErrorCode.get(key);
		if (syntaxCode != null) {
			Range range = XMLSyntaxErrorCode.toLSPRange(location, syntaxCode, arguments, document);
			if (range != null) {
				return range;
			}
		} else {
			// try adjust positions for XML schema error
			XMLSchemaErrorCode schemaCode = XMLSchemaErrorCode.get(key);
			if (schemaCode != null) {
				Range range = XMLSchemaErrorCode.toLSPRange(location, schemaCode, arguments, document);
				if (range != null) {
					return range;
				}
			} else {
				// try adjust positions for DTD error
				DTDErrorCode dtdCode = DTDErrorCode.get(key);
				if (dtdCode != null) {
					Range range = DTDErrorCode.toLSPRange(location, dtdCode, arguments, document);
					if (range != null) {
						return range;
					}
				}
			}
		}

		int startOffset = location.getCharacterOffset() - 1;
		int endOffset = location.getCharacterOffset() - 1;

		// Create LSP range
		Position start = toLSPPosition(startOffset, location, document.getTextDocument());
		Position end = toLSPPosition(endOffset, location, document.getTextDocument());
		return new Range(start, end);
	}
}
