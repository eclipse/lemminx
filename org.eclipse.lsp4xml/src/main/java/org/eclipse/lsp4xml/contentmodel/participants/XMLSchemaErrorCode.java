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
package org.eclipse.lsp4xml.contentmodel.participants;

import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.xni.XMLLocator;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.contentmodel.participants.codeactions.cvc_complex_type_2_3CodeAction;
import org.eclipse.lsp4xml.contentmodel.participants.codeactions.cvc_complex_type_4CodeAction;
import org.eclipse.lsp4xml.contentmodel.participants.diagnostics.IXMLErrorCode;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.services.extensions.ICodeActionParticipant;
import org.eclipse.lsp4xml.utils.XMLPositionUtility;

/**
 * XML Schema error code.
 * 
 * @see https://wiki.xmldation.com/Support/Validator
 *
 */
public enum XMLSchemaErrorCode implements IXMLErrorCode {

	cvc_complex_type_2_3("cvc-complex-type.2.3"), // https://wiki.xmldation.com/Support/Validator/cvc-complex-type-2-3
	cvc_complex_type_2_4_a("cvc-complex-type.2.4.a"), // https://wiki.xmldation.com/Support/Validator/cvc-complex-type-2-4-a
	cvc_complex_type_2_4_d("cvc-complex-type.2.4.d"), // https://wiki.xmldation.com/Support/Validator/cvc-complex-type-2-4-d
	cvc_complex_type_3_2_2("cvc-complex-type.3.2.2"), // https://wiki.xmldation.com/Support/Validator/cvc-complex-type-3-2-2
	cvc_complex_type_4("cvc-complex-type.4"), // https://wiki.xmldation.com/Support/Validator/cvc-complex-type-4
	cvc_elt_1_a("cvc-elt.1.a"), // https://wiki.xmldation.com/Support/Validator/cvc-elt-1
	cvc_type_3_1_1("cvc-type.3.1.1"); // https://wiki.xmldation.com/Support/Validator/cvc-type-3-1-1

	private final String code;

	private XMLSchemaErrorCode() {
		this(null);
	}

	private XMLSchemaErrorCode(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		if (code == null) {
			return name();
		}
		return code;
	}

	@Override
	public String toString() {
		return getCode();
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
	 * @param           document.ge
	 * @return the LSP range from the SAX error.
	 */
	public static Range toLSPRange(XMLLocator location, XMLSchemaErrorCode code, Object[] arguments,
			XMLDocument document) {
		int offset = location.getCharacterOffset() - 1;

		// adjust positions
		switch (code) {
		case cvc_complex_type_2_3:
			return XMLPositionUtility.selectFirstNonWhitespaceText(offset, document);
		case cvc_complex_type_2_4_a:
		case cvc_complex_type_2_4_d:
		case cvc_elt_1_a:
		case cvc_complex_type_4:
			return XMLPositionUtility.selectStartTag(offset, document);
		case cvc_complex_type_3_2_2: {
			String attrName = (String) arguments[1];
			return XMLPositionUtility.selectAttributeName(attrName, offset, document);
		}
		case cvc_type_3_1_1:
			return XMLPositionUtility.selectAllAttributes(offset, document);
		}

		return null;
	}

	public static void registerCodeActionParticipants(Map<String, ICodeActionParticipant> codeActions) {
		codeActions.put(cvc_complex_type_2_3.getCode(), new cvc_complex_type_2_3CodeAction());
		codeActions.put(cvc_complex_type_4.getCode(), new cvc_complex_type_4CodeAction());
	}
}
