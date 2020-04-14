/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.extensions.contentmodel.participants;

import static org.eclipse.lemminx.utils.StringUtils.getString;

import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.xni.XMLLocator;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.extensions.contentmodel.participants.codeactions.ElementDeclUnterminatedCodeAction;
import org.eclipse.lemminx.services.extensions.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.diagnostics.IXMLErrorCode;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

/**
 * DTD error code.
 * 
 * @see https://wiki.xmldation.com/Support/Validator
 *
 */
public enum DTDErrorCode implements IXMLErrorCode {

	AttNameRequiredInAttDef,
	AttTypeRequiredInAttDef,
	ElementDeclUnterminated,
	EntityDeclUnterminated,
	EntityNotDeclared,
	ExternalIDorPublicIDRequired,
	IDInvalidWithNamespaces,
	IDREFInvalidWithNamespaces,
	IDREFSInvalid,
	LessthanInAttValue,
	MSG_ATTRIBUTE_NOT_DECLARED,
	MSG_ATTRIBUTE_VALUE_NOT_IN_LIST,
	MSG_CONTENT_INCOMPLETE,
	MSG_CONTENT_INVALID,
	MSG_ELEMENT_ALREADY_DECLARED,
	MSG_ELEMENT_NOT_DECLARED,
	MSG_ELEMENT_TYPE_REQUIRED_IN_ATTLISTDECL,
	MSG_ELEMENT_TYPE_REQUIRED_IN_ELEMENTDECL,
	MSG_ELEMENT_WITH_ID_REQUIRED,
	MSG_ENTITY_NAME_REQUIRED_IN_ENTITYDECL,
	MSG_FIXED_ATTVALUE_INVALID,
	MSG_MARKUP_NOT_RECOGNIZED_IN_DTD,
	MSG_NOTATION_NAME_REQUIRED_IN_NOTATIONDECL,
	MSG_OPEN_PAREN_OR_ELEMENT_TYPE_REQUIRED_IN_CHILDREN,
	MSG_REQUIRED_ATTRIBUTE_NOT_SPECIFIED,
	MSG_SPACE_REQUIRED_AFTER_NOTATION_NAME_IN_NOTATIONDECL,
	NotationDeclUnterminated,
	OpenQuoteExpected,
	OpenQuoteMissingInDecl,
	PEReferenceWithinMarkup,
	QuoteRequiredInPublicID,
	QuoteRequiredInSystemID,
	SpaceRequiredAfterSYSTEM,
	dtd_not_found("dtd-not-found");
			
	private final String code;

	private DTDErrorCode() {
		this(null);
	}

	private DTDErrorCode(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		if (code == null) {
			return name();
		}
		return code;
	}

	private final static Map<String, DTDErrorCode> codes;

	static {
		codes = new HashMap<>();
		for (DTDErrorCode errorCode : values()) {
			codes.put(errorCode.getCode(), errorCode);
		}
	}

	public static DTDErrorCode get(String name) {
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
	public static Range toLSPRange(XMLLocator location, DTDErrorCode code, Object[] arguments, DOMDocument document) {
		int offset = location.getCharacterOffset() - 1;
		// adjust positions
		switch (code) {
		case MSG_CONTENT_INCOMPLETE:
		case MSG_REQUIRED_ATTRIBUTE_NOT_SPECIFIED:
		case MSG_ELEMENT_NOT_DECLARED:
		case MSG_CONTENT_INVALID: {
			return XMLPositionUtility.selectStartTagName(offset, document);
		}
		case MSG_ATTRIBUTE_NOT_DECLARED: {
			return XMLPositionUtility.selectAttributeNameFromGivenNameAt(getString(arguments[1]), offset, document);
		}
		case MSG_FIXED_ATTVALUE_INVALID: {
			String attrName = getString(arguments[1]);
			return XMLPositionUtility.selectAttributeValueAt(attrName, offset, document);
		}
		case MSG_ATTRIBUTE_VALUE_NOT_IN_LIST: {
			String attrName = getString(arguments[0]);
			return XMLPositionUtility.selectAttributeValueAt(attrName, offset, document);
		}
		
		case MSG_ELEMENT_WITH_ID_REQUIRED: {
			DOMElement element = document.getDocumentElement();
			if (element != null) {
				return XMLPositionUtility.selectStartTagName(element);
			}
		}
		case IDREFSInvalid:
		case IDREFInvalidWithNamespaces:
		case IDInvalidWithNamespaces: {
			String attrValue = getString(arguments[0]);
			return XMLPositionUtility.selectAttributeValueByGivenValueAt(attrValue, offset, document);
		}

		case MSG_MARKUP_NOT_RECOGNIZED_IN_DTD: {
			return XMLPositionUtility.selectWholeTag(offset + 2, document);
		}

		// ---------- DTD Doc type

		case ExternalIDorPublicIDRequired: {
			return XMLPositionUtility.getLastValidDTDDeclParameter(offset, document);
		}

		case PEReferenceWithinMarkup: {
			return XMLPositionUtility.getLastValidDTDDeclParameter(offset, document, true);
		}
		case EntityNotDeclared: {
			try {
				Position position = document.positionAt(offset);
				int line = position.getLine();
				String text = document.lineText(line);
				String name = getString(arguments[0]);
				String subString = "&" + name + ";";
				int start = text.indexOf(subString);
				int end = start + subString.length();
				Position startPosition = new Position(line, start);
				Position endPosition = new Position(line, end);
				return new Range(startPosition, endPosition);
			} catch (BadLocationException e) {

			}

		}
		case QuoteRequiredInPublicID:
		case QuoteRequiredInSystemID:
		case OpenQuoteMissingInDecl:
		case SpaceRequiredAfterSYSTEM:
		case MSG_SPACE_REQUIRED_AFTER_NOTATION_NAME_IN_NOTATIONDECL:
		case AttTypeRequiredInAttDef:
		case LessthanInAttValue:
		case OpenQuoteExpected:
		case AttNameRequiredInAttDef:
		case EntityDeclUnterminated:
		case NotationDeclUnterminated:
		case ElementDeclUnterminated: {
			return XMLPositionUtility.getLastValidDTDDeclParameterOrUnrecognized(offset, document);
		}

		case MSG_OPEN_PAREN_OR_ELEMENT_TYPE_REQUIRED_IN_CHILDREN: {
			return XMLPositionUtility.getElementDeclMissingContentOrCategory(offset, document);
		}

		case MSG_ELEMENT_ALREADY_DECLARED:
		case MSG_NOTATION_NAME_REQUIRED_IN_NOTATIONDECL:
		case MSG_ENTITY_NAME_REQUIRED_IN_ENTITYDECL:
		case MSG_ELEMENT_TYPE_REQUIRED_IN_ATTLISTDECL:
		case MSG_ELEMENT_TYPE_REQUIRED_IN_ELEMENTDECL: {
			return XMLPositionUtility.selectDTDDeclTagNameAt(offset, document);
		}
		default:
			try {
				return new Range(new Position(0, 0), document.positionAt(document.getEnd()));
			} catch (BadLocationException e) {
				
			}
		}
		return null;
	}

	public static void registerCodeActionParticipants(Map<String, ICodeActionParticipant> codeActions) {
		codeActions.put(ElementDeclUnterminated.getCode(), new ElementDeclUnterminatedCodeAction());
	}
}
