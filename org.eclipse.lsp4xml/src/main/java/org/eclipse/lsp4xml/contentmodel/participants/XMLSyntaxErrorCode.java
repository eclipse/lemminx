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

import static org.eclipse.lsp4xml.utils.XMLPositionUtility.*;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLLocator;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.contentmodel.participants.codeactions.ElementUnterminatedCodeAction;
import org.eclipse.lsp4xml.contentmodel.participants.diagnostics.IXMLErrorCode;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.services.extensions.ICodeActionParticipant;
import org.eclipse.lsp4xml.utils.XMLPositionUtility;

/**
 * XML error code.
 * 
 * @see https://wiki.xmldation.com/Support/Validator
 *
 */
public enum XMLSyntaxErrorCode implements IXMLErrorCode {

	AttributeNotUnique, // https://wiki.xmldation.com/Support/Validator/AttributeNotUnique
	AttributeNSNotUnique, // https://wiki.xmldation.com/Support/Validator/AttributeNSNotUnique
	ContentIllegalInProlog, // https://wiki.xmldation.com/Support/Validator/ContentIllegalInProlog
	DashDashInComment, // https://wiki.xmldation.com/Support/Validator/DashDashInComment
	ElementUnterminated, // https://wiki.xmldation.com/Support/Validator/ElementUnterminated
	ElementPrefixUnbound, // https://wiki.xmldation.com/Support/Validator/ElementPrefixUnbound
	EmptyPrefixedAttName, // https://wiki.xmldation.com/Support/Validator/EmptyPrefixedAttName
	EncodingDeclRequired, // https://wiki.xmldation.com/Support/Validator/EncodingDeclRequired
	ETagRequired, // https://wiki.xmldation.com/Support/Validator/ETagRequired
	ETagUnterminated, // https://wiki.xmldation.com/Support/Validator/ETagUnterminated
	EqRequiredInAttribute, the_element_type_lmsg("the-element-type-lmsg"), EqRequiredInXMLDecl, IllegalQName,
	InvalidCommentStart, LessthanInAttValue, MarkupEntityMismatch, MarkupNotRecognizedInContent, NameRequiredInReference,
	OpenQuoteExpected, PITargetRequired, PseudoAttrNameExpected, QuoteRequiredInXMLDecl, SDDeclInvalid,
	SpaceRequiredBeforeEncodingInXMLDecl, SpaceRequiredBeforeStandalone, SpaceRequiredInPI, VersionInfoRequired,
	VersionNotSupported, XMLDeclUnterminated, CustomETag; // https://wiki.xmldation.com/Support/Validator/EqRequiredInAttribute

	private final String code;

	private XMLSyntaxErrorCode() {
		this(null);
	}

	private XMLSyntaxErrorCode(String code) {
		this.code = code;
	}

	@Override
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
			XMLDocument document) {
		int offset = location.getCharacterOffset() - 1;
		Range r;
		String tag;
		// adjust positions
		switch (code) {
		case AttributeNotUnique:
		case AttributeNSNotUnique: {
			String attrName = (String) arguments[1];
			return XMLPositionUtility.selectAttributeNameLast(attrName, offset, document);
		}
		case ContentIllegalInProlog: {
			int endOffset = document.getText().indexOf("<");
			int startOffset = offset + 1;
			return XMLPositionUtility.createRange(startOffset, endOffset, document);
		}
		case DashDashInComment: {
			int endOffset = offset + 1;
			int startOffset = offset - 1;
			return XMLPositionUtility.createRange(startOffset, endOffset, document);
		}
		case EmptyPrefixedAttName: {
			String attrName = ((QName) arguments[0]).rawname;
			return XMLPositionUtility.selectAttributeValue(attrName, offset, document);
		}
		case ElementUnterminated: {
			return XMLPositionUtility.selectStartTag(offset, document);
		}
		case ETagRequired:
			tag = (String) arguments[0];
			return XMLPositionUtility.selectChildEndTag(tag, offset, document);
		case ETagUnterminated:
			tag = (String) arguments[0];
			return XMLPositionUtility.selectEndTag(offset - 1, document);
		case EncodingDeclRequired:
			break;
		case EqRequiredInAttribute:
			tag = getNameFromArguents(arguments, 1);
			return XMLPositionUtility.selectAttributeName(tag, offset, document);

		case EqRequiredInXMLDecl:
			tag = getNameFromArguents(arguments, 1);
			return XMLPositionUtility.selectAttributeName(tag, offset, document);
		case IllegalQName:
			return XMLPositionUtility.createRange(offset, offset + 1, document);
		case InvalidCommentStart:
			return XMLPositionUtility.createRange(offset, offset + 1, document);
		case LessthanInAttValue:
			tag = getNameFromArguents(arguments, 1);
			return XMLPositionUtility.selectAttributeValue(tag, offset, document);
		case MarkupEntityMismatch:
			// return XMLPositionUtility.selectStartTag(offset, document);
		case MarkupNotRecognizedInContent:
			return XMLPositionUtility.createRange(offset, offset + 1, document);

		case NameRequiredInReference:
			// Good as is
		case OpenQuoteExpected:
			// Working
			break;
		case PITargetRequired:
			// Working
			break;
		case PseudoAttrNameExpected:
			// Working
			// Add better message
			break;
		case QuoteRequiredInXMLDecl:

		case SDDeclInvalid:
			return XMLPositionUtility.selectAttributeValue("standalone", offset, document);

		case SpaceRequiredInPI:
			int start = selectCurrentTagOffset(offset, document) + 1;
			int end = offset + 1;
			return XMLPositionUtility.createRange(start, end, document);

		case SpaceRequiredBeforeStandalone:
		case SpaceRequiredBeforeEncodingInXMLDecl:
		case VersionInfoRequired:
			tag = getNameFromArguents(arguments, 0);
			r = selectStartTag(offset, document);
			r.getEnd().setCharacter(r.getEnd().getCharacter() + 1);
			return r;
		case VersionNotSupported:
			return XMLPositionUtility.selectAttributeValue("version", offset, document);
		case XMLDeclUnterminated:
			break;
		case CustomETag:
			tag = (String) arguments[0];
			return XMLPositionUtility.selectEndTag(offset, document);
			
		}
		

		return null;

	}

	public static void registerCodeActionParticipants(Map<String, ICodeActionParticipant> codeActions) {
		codeActions.put(ElementUnterminated.getCode(), new ElementUnterminatedCodeAction());
	}
}
