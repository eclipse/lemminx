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
package org.eclipse.lsp4xml.contentmodel.participants.diagnostics;

import java.util.List;

import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.util.MessageFormatter;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLParseException;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
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

	private final TextDocument document;
	private final List<Diagnostic> diagnostics;

	public LSPErrorReporter(TextDocument document, List<Diagnostic> diagnostics) {
		this.document = document;
		this.diagnostics = diagnostics;
		XMLMessageFormatter xmft = new XMLMessageFormatter();
		super.putMessageFormatter(XMLMessageFormatter.XML_DOMAIN, xmft);
		super.putMessageFormatter(XMLMessageFormatter.XMLNS_DOMAIN, xmft);
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
		diagnostics.add(new Diagnostic(toLSPRange(location, key, arguments, document), message, toLSPSeverity(severity),
				XML_DIAGNOSTIC_SOURCE, key));

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
	private static Range toLSPRange(XMLLocator location, String key, Object[] arguments, TextDocument document) {
		int offset = location.getCharacterOffset() - 1;
		int startOffset = location.getCharacterOffset() - 1;
		int endOffset = location.getCharacterOffset() - 1;

		// adjust positions
		XMLErrorCode code = XMLErrorCode.get(key);
		if (code != null) {
			switch (code) {
			case AttributeNotUnique:
				String attrName = (String) arguments[1];
				endOffset = findOffsetOfAttrName(document.getText(), offset, attrName);
				startOffset = endOffset - attrName.length();
				break;
			case EmptyPrefixedAttName:
				endOffset = findOffsetOfFirstChar(document.getText(), offset);
				startOffset = endOffset - 2;
				break;
			case ElementUnterminated:
				String tag = (String) arguments[0];
				endOffset = findOffsetOfFirstChar(document.getText(), offset);
				startOffset = endOffset - tag.length();
				break;
			case ETagRequired:
				
				break;
			}
		}

		// Create LSP range
		Position start = toLSPPosition(startOffset, location, document);
		Position end = toLSPPosition(endOffset, location, document);
		return new Range(start, end);
	}

	/**
	 * Returns the LSP position from the SAX location.
	 * 
	 * @param offset
	 * @param location
	 * @param document
	 * @return the LSP position from the SAX location.
	 */
	private static Position toLSPPosition(int offset, XMLLocator location, TextDocument document) {
		if (offset == location.getCharacterOffset() - 1) {
			return new Position(location.getLineNumber() - 1, location.getColumnNumber() - 1);
		}
		try {
			return document.positionAt(offset);
		} catch (BadLocationException e) {
			return new Position(location.getLineNumber() - 1, location.getColumnNumber() - 1);
		}

	}

	private static int findOffsetOfAttrName(String text, int offset, String attrName) {
		boolean inQuote = false;
		boolean parsedValue = false;
		for (int i = offset; i >= 0; i--) {
			char c = text.charAt(i);
			if (!(c == ' ' || c == '\r' || c == '\n')) {
				if (c == '"' || c == '\'') {
					inQuote = !inQuote;
					if (!inQuote) {
						parsedValue = true;
					}
				} else {
					if (parsedValue && c != '=') {
						return i + 1;
					}
				}
			}
		}
		return -1;
	}

	private static int findOffsetOfFirstChar(String text, int offset) {
		for (int i = offset; i >= 0; i--) {
			char c = text.charAt(i);
			if (!(c == ' ' || c == '\r' || c == '\n')) {
				return i + 1;
			}
		}
		return -1;
	}
}
