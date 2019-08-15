/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.contentmodel.participants;

import static org.eclipse.lsp4xml.utils.StringUtils.getString;
import static org.eclipse.lsp4xml.utils.XMLPositionUtility.selectCurrentTagOffset;

import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLLocator;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.codeactions.ElementUnterminatedCodeAction;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.codeactions.EqRequiredInAttributeCodeAction;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.codeactions.OpenQuoteExpectedCodeAction;
import org.eclipse.lsp4xml.services.extensions.ICodeActionParticipant;
import org.eclipse.lsp4xml.services.extensions.diagnostics.IXMLErrorCode;
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
	AttributePrefixUnbound,
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
	VersionInfoRequired, VersionNotSupported, XMLDeclUnterminated, CustomETag, PrematureEOF; // https://wiki.xmldation.com/Support/Validator/EqRequiredInAttribute

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
			DOMDocument document) {
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
			String attrName = getString(arguments[1]);
			return XMLPositionUtility.selectAttributeNameFromGivenNameAt(attrName, offset, document);
		}
		case EncodingDeclRequired:
		case EqRequiredInXMLDecl:
			return XMLPositionUtility.selectAttributeNameAt(offset, document);
		case AttributeNSNotUnique: {
			String attrName = getString(arguments[1]);
			Range xmlns = XMLPositionUtility.selectAttributeNameFromGivenNameAt("xmlns:" + attrName, offset, document);
			if (xmlns != null) {
				return xmlns;
			}
			return XMLPositionUtility.selectAttributeNameFromGivenNameAt(attrName, offset, document);
		}
		case AttributeNotUnique: {
			String attrName = getString(arguments[1]);
			return XMLPositionUtility.selectAttributeNameFromGivenNameAt(attrName, offset, document);
		}
		case AttributePrefixUnbound: {
			return XMLPositionUtility.selectAttributePrefixFromGivenNameAt(getString(arguments[1]), offset, document);
		}
		case LessthanInAttValue: {
			String attrName = getString(arguments[1]);
			return XMLPositionUtility.selectAttributeValueAt(attrName, offset, document);
		}
		case QuoteRequiredInXMLDecl: {
			String attrName = getString(arguments[0]);
			return XMLPositionUtility.selectAttributeValueAt(attrName, offset, document);
		}
		case EmptyPrefixedAttName: {
			QName qName = (QName) arguments[0];
			return XMLPositionUtility.selectAttributeValueAt(qName.rawname, offset, document);
		}
		case SDDeclInvalid:
		case VersionNotSupported: {
			String attrValue = getString(arguments[0]);
			return XMLPositionUtility.selectAttributeValueByGivenValueAt(attrValue, offset, document);
		}
		case ETagUnterminated:
			/**
			 * Cases:
			 * 
			 * <a> </b>
			 * 
			 * <a> <b> </b> </c>
			 * 
			 * <a> <a> </a> </b
			 */
			return XMLPositionUtility.selectPreviousNodesEndTag(offset, document);
		case CustomETag:
			return XMLPositionUtility.selectEndTag(offset, document);
		case ETagRequired: {
			String tag = getString(arguments[0]);
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
			return XMLPositionUtility.createRange(offset, offset + 1, document);
		case MarkupEntityMismatch:
			return XMLPositionUtility.selectRootStartTag(document);
		case NameRequiredInReference:
			break;
		case OpenQuoteExpected: {
			return XMLPositionUtility.selectAttributeNameAt(offset - 1, document);
		}
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
		case PrematureEOF:
		case XMLDeclUnterminated:
			break;
		default:
		}

		return null;

	}

	public static void registerCodeActionParticipants(Map<String, ICodeActionParticipant> codeActions) {
		codeActions.put(ElementUnterminated.getCode(), new ElementUnterminatedCodeAction());
		codeActions.put(EqRequiredInAttribute.getCode(), new EqRequiredInAttributeCodeAction());
		codeActions.put(OpenQuoteExpected.getCode(), new OpenQuoteExpectedCodeAction());
	}
}
