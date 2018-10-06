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
package org.eclipse.lsp4xml.extensions.contentmodel.participants;

import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.xni.XMLLocator;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.codeactions.cvc_attribute_3CodeAction;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.codeactions.cvc_complex_type_2_1CodeAction;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.codeactions.cvc_complex_type_2_3CodeAction;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.codeactions.cvc_complex_type_3_2_2CodeAction;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.codeactions.cvc_complex_type_4CodeAction;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.codeactions.cvc_enumeration_validCodeAction;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.codeactions.cvc_type_3_1_1CodeAction;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.diagnostics.IXMLErrorCode;
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
	cvc_complex_type_2_1("cvc-complex-type.2.1"), // https://wiki.xmldation.com/Support/Validator/cvc-complex-type-2-1
	cvc_complex_type_2_4_a("cvc-complex-type.2.4.a"), // https://wiki.xmldation.com/Support/Validator/cvc-complex-type-2-4-a
	cvc_complex_type_2_4_b("cvc-complex-type.2.4.b"), // https://wiki.xmldation.com/Support/Validator/cvc-complex-type-2-4-b
	cvc_complex_type_2_4_d("cvc-complex-type.2.4.d"), // https://wiki.xmldation.com/Support/Validator/cvc-complex-type-2-4-d
	cvc_complex_type_3_1("cvc-complex-type.3.1"), // https://wiki.xmldation.com/Support/Validator/cvc-complex-type-3-1
	cvc_complex_type_3_2_2("cvc-complex-type.3.2.2"), // https://wiki.xmldation.com/Support/Validator/cvc-complex-type-3-2-2
	cvc_complex_type_4("cvc-complex-type.4"), // https://wiki.xmldation.com/Support/Validator/cvc-complex-type-4
	cvc_datatype_valid_1_2_1("cvc-datatype-valid.1.2.1"), // https://wiki.xmldation.com/Support/Validator/cvc-datatype-valid-1-2-1
	cvc_elt_1_a("cvc-elt.1.a"), // https://wiki.xmldation.com/Support/Validator/cvc-elt-1
	cvc_elt_4_2("cvc-elt.4.2"), // https://wiki.xmldation.com/Support/Validator/cvc-elt-4-2
	cvc_type_3_1_1("cvc-type.3.1.1"), // https://wiki.xmldation.com/Support/Validator/cvc-type-3-1-1
	cvc_type_3_1_3("cvc-type.3.1.3"), // https://wiki.xmldation.com/Support/Validator/cvc-type-3-1-3,
	cvc_attribute_3("cvc-attribute.3"), // https://wiki.xmldation.com/Support/Validator/cvc-attribute-3
	cvc_enumeration_valid("cvc-enumeration-valid"), // https://wiki.xmldation.com/Support/Validator/cvc-enumeration-valid
	cvc_maxlength_valid("cvc-maxLength-valid"), // https://wiki.xmldation.com/Support/validator/cvc-maxlength-valid
	cvc_minlength_valid("cvc-minLength-valid"), // https://wiki.xmldation.com/Support/validator/cvc-minlength-valid
	cvc_maxExclusive_valid("cvc-maxExclusive-valid"), // https://wiki.xmldation.com/Support/validator/cvc-maxexclusive-valid
	cvc_maxInclusive_valid("cvc-maxInclusive-valid"), // https://wiki.xmldation.com/Support/validator/cvc-maxinclusive-valid
	cvc_minExclusive_valid("cvc-minExclusive-valid"), // https://wiki.xmldation.com/Support/validator/cvc-minexclusive-valid
	cvc_minInclusive_valid("cvc-minInclusive-valid"); // https://wiki.xmldation.com/Support/validator/cvc-mininclusive-valid

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
		case cvc_complex_type_2_4_b:
		case cvc_complex_type_2_4_d:
		case cvc_elt_1_a:
		case cvc_complex_type_4:
			return XMLPositionUtility.selectStartTag(offset, document);
		case cvc_complex_type_3_2_2: {
			String attrName = (String) arguments[1];
			return XMLPositionUtility.selectAttributeNameFromGivenNameAt(attrName, offset, document);
		}
		case cvc_attribute_3:
		case cvc_complex_type_3_1:
		case cvc_elt_4_2: {
			String attrName = (String) arguments[1];
			return XMLPositionUtility.selectAttributeValueAt(attrName, offset, document);
		}
		case cvc_type_3_1_1:
			return XMLPositionUtility.selectAllAttributes(offset, document);
		case cvc_complex_type_2_1:
		case cvc_type_3_1_3:
			return XMLPositionUtility.selectText(offset, document);
		case cvc_enumeration_valid:
		case cvc_datatype_valid_1_2_1:
		case cvc_maxlength_valid:
		case cvc_minlength_valid:
		case cvc_maxExclusive_valid:
		case cvc_maxInclusive_valid:
		case cvc_minExclusive_valid:
		case cvc_minInclusive_valid: {
			// this error can occur for attribute value or text
			// Try for attribute value
			String attrValue = (String) arguments[0];
			Range range = XMLPositionUtility.selectAttributeValueFromGivenValue(attrValue, offset, document);
			if (range != null) {
				return range;
			} else {
				// Try with text
				return XMLPositionUtility.selectText(offset, document);
			}
		}
		default:
		}
		return null;
	}

	public static void registerCodeActionParticipants(Map<String, ICodeActionParticipant> codeActions) {
		codeActions.put(cvc_complex_type_2_3.getCode(), new cvc_complex_type_2_3CodeAction());
		codeActions.put(cvc_complex_type_4.getCode(), new cvc_complex_type_4CodeAction());
		codeActions.put(cvc_type_3_1_1.getCode(), new cvc_type_3_1_1CodeAction());
		codeActions.put(cvc_attribute_3.getCode(), new cvc_attribute_3CodeAction());
		codeActions.put(cvc_complex_type_3_2_2.getCode(), new cvc_complex_type_3_2_2CodeAction());
		codeActions.put(cvc_enumeration_valid.getCode(), new cvc_enumeration_validCodeAction());
		codeActions.put(cvc_complex_type_2_1.getCode(), new cvc_complex_type_2_1CodeAction());
	}
}
