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
 * XML Schema error code.
 * 
 * @see https://wiki.xmldation.com/Support/Validator
 *
 */
public enum XMLSchemaErrorCode {

	cvc_complex_type_2_4_a("cvc-complex-type.2.4.a"); // https://wiki.xmldation.com/Support/Validator/cvc-complex-type.2.4.a

	private final String code;

	private XMLSchemaErrorCode() {
		this(null);
	}

	private XMLSchemaErrorCode(String code) {
		this.code = code;
	}

	public String getCode() {
		if (code == null) {
			return name();
		}
		return code;
	}

	private final static Map<String, XMLSchemaErrorCode> codes;

	static {
		codes = new HashMap<>();
		for (XMLSchemaErrorCode errorCode : values()) {
			codes.put(errorCode.getCode(), errorCode);
		}
	}

	public static XMLSchemaErrorCode get(String name) {
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
	public static Range toLSPRange(XMLLocator location, XMLSchemaErrorCode code, Object[] arguments,
			TextDocument document) {
		int offset = location.getCharacterOffset() - 1;
		int startOffset = location.getCharacterOffset() - 1;
		int endOffset = location.getCharacterOffset() - 1;

		// adjust positions
		switch (code) {
		case cvc_complex_type_2_4_a:
			// TODO...
			break;
		}

		// Create LSP range
		Position start = toLSPPosition(startOffset, location, document);
		Position end = toLSPPosition(endOffset, location, document);
		return new Range(start, end);
	}
}
