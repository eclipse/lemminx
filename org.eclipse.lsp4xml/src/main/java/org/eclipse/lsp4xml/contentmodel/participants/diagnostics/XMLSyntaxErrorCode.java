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

import static org.eclipse.lsp4xml.contentmodel.participants.diagnostics.LSPErrorReporter.findOffsetOfAfterChar;
import static org.eclipse.lsp4xml.contentmodel.participants.diagnostics.LSPErrorReporter.findOffsetOfAttrName;
import static org.eclipse.lsp4xml.contentmodel.participants.diagnostics.LSPErrorReporter.findOffsetOfFirstChar;
import static org.eclipse.lsp4xml.contentmodel.participants.diagnostics.LSPErrorReporter.toLSPPosition;

import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.xni.XMLLocator;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.TextDocument;

/**
 * XML error code.
 * 
 * @see https://wiki.xmldation.com/Support/Validator
 *
 */
public enum XMLSyntaxErrorCode {

	AttributeNotUnique, // https://wiki.xmldation.com/Support/Validator/AttributeNotUnique
	AttributeNSNotUnique, // https://wiki.xmldation.com/Support/Validator/AttributeNSNotUnique
	ContentIllegalInProlog, // https://wiki.xmldation.com/Support/Validator/ContentIllegalInProlog
	DashDashInComment, // https://wiki.xmldation.com/Support/Validator/DashDashInComment
	EmptyPrefixedAttName, // https://wiki.xmldation.com/Support/Validator/EmptyPrefixedAttName
	ElementUnterminated, // https://wiki.xmldation.com/Support/Validator/ElementUnterminated
	ETagRequired; // https://wiki.xmldation.com/Support/Validator/ETagRequired

	private final String code;

	private XMLSyntaxErrorCode() {
		this(null);
	}

	private XMLSyntaxErrorCode(String code) {
		this.code = code;
	}

	public String getCode() {
		if (code == null) {
			return name();
		}
		return code;
	}

	private final static Map<String, XMLSyntaxErrorCode> codes;

	static {
		codes = new HashMap<>();
		for (XMLSyntaxErrorCode errorCode : values()) {
			codes.put(errorCode.getCode(), errorCode);
		}
	}

	public static XMLSyntaxErrorCode get(String name) {
		return codes.get(name);
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
	public static Range toLSPRange(XMLLocator location, XMLSyntaxErrorCode code, Object[] arguments,
			TextDocument document) {
		int offset = location.getCharacterOffset() - 1;
		int startOffset = location.getCharacterOffset() - 1;
		int endOffset = location.getCharacterOffset() - 1;

		// adjust positions
		switch (code) {
		case AttributeNotUnique:
		case AttributeNSNotUnique:
			String attrName = (String) arguments[1];
			endOffset = findOffsetOfAttrName(document.getText(), offset, attrName);
			startOffset = endOffset - attrName.length();
			break;
		case ContentIllegalInProlog:
			offset = location.getCharacterOffset();
			startOffset = offset;
			endOffset = findOffsetOfAfterChar(document.getText(), offset, '<');
			break;
		case DashDashInComment:
			startOffset = endOffset - 1;
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

		// Create LSP range
		Position start = toLSPPosition(startOffset, location, document);
		Position end = toLSPPosition(endOffset, location, document);
		return new Range(start, end);
	}
}
