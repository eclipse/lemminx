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

import static org.eclipse.lsp4xml.utils.XMLPositionUtility.selectCurrentTagOffset;

import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLLocator;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.codeactions.ElementUnterminatedCodeAction;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.codeactions.EqRequiredInAttributeCodeAction;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.diagnostics.IXMLErrorCode;
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
	InvalidCommentStart, LessthanInAttValue, MarkupEntityMismatch, MarkupNotRecognizedInContent,
	NameRequiredInReference, OpenQuoteExpected, PITargetRequired, PseudoAttrNameExpected, QuoteRequiredInXMLDecl,
	SDDeclInvalid, SpaceRequiredBeforeEncodingInXMLDecl, SpaceRequiredBeforeStandalone, SpaceRequiredInPI,
	VersionInfoRequired, VersionNotSupported, XMLDeclUnterminated, CustomETag; // https://wiki.xmldation.com/Support/Validator/EqRequiredInAttribute

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
		// adjust positions
		switch (code) {
		case SpaceRequiredBeforeStandalone:
		case SpaceRequiredBeforeEncodingInXMLDecl:
		case VersionInfoRequired:
		case ElementPrefixUnbound:
		case ElementUnterminated:
			return XMLPositionUtility.selectStartTag(offset, document);
		case EqRequiredInAttribute: {
			String attrName = (String) arguments[1];
			return XMLPositionUtility.selectAttributeNameFromGivenNameAt(attrName, offset, document);
		}
		case EncodingDeclRequired:
		case EqRequiredInXMLDecl:
		case AttributeNotUnique:
		case AttributeNSNotUnique:
			return XMLPositionUtility.selectAttributeNameAt(offset, document);
		case LessthanInAttValue: {
			String attrName = (String) arguments[1];
			return XMLPositionUtility.selectAttributeValueAt(attrName, offset, document);
		}
		case QuoteRequiredInXMLDecl: {
			String attrName = (String) arguments[0];
			return XMLPositionUtility.selectAttributeValueAt(attrName, offset, document);
		}
		case EmptyPrefixedAttName: {
			QName qName = (QName) arguments[0];
			return XMLPositionUtility.selectAttributeValueAt(qName.rawname, offset, document);
		}
		case SDDeclInvalid:
		case VersionNotSupported: {
			String attrValue = (String) arguments[0];
			return XMLPositionUtility.selectAttributeValueByGivenValueAt(attrValue, offset, document);
		}
		case ETagUnterminated:
			return XMLPositionUtility.selectEndTag(offset - 1, document);
		case CustomETag:
			return XMLPositionUtility.selectEndTag(offset, document);
		case ETagRequired: {
			String tag = (String) arguments[0];
			return XMLPositionUtility.selectChildEndTag(tag, offset, document);
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
		case IllegalQName:
		case InvalidCommentStart:
		case MarkupNotRecognizedInContent:
		case MarkupEntityMismatch:
			return XMLPositionUtility.createRange(offset, offset + 1, document);
		case NameRequiredInReference:
			break;
		case OpenQuoteExpected:
			return XMLPositionUtility.selectAttributeNameAt(offset - 1, document);
		case PITargetRequired:
			// Working
			break;
		case PseudoAttrNameExpected:
			// Working
			// Add better message
			break;
		case SpaceRequiredInPI:
			int start = selectCurrentTagOffset(offset, document) + 1;
			int end = offset + 1;
			return XMLPositionUtility.createRange(start, end, document);
		case XMLDeclUnterminated:
			break;
		default:
		}

		return null;

	}

	public static void registerCodeActionParticipants(Map<String, ICodeActionParticipant> codeActions) {
		codeActions.put(ElementUnterminated.getCode(), new ElementUnterminatedCodeAction());
		codeActions.put(EqRequiredInAttribute.getCode(), new EqRequiredInAttributeCodeAction());		
	}
}
