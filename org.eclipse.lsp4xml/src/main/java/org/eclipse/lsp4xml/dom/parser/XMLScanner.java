/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.dom.parser;

import static org.eclipse.lsp4xml.dom.parser.Constants.ATTRIBUTE_NAME_REGEX;
import static org.eclipse.lsp4xml.dom.parser.Constants.ATTRIBUTE_VALUE_REGEX;
import static org.eclipse.lsp4xml.dom.parser.Constants.DOCTYPE_KIND_OPTIONS;
import static org.eclipse.lsp4xml.dom.parser.Constants.DTD_ELEMENT_CATEGORY;
import static org.eclipse.lsp4xml.dom.parser.Constants.ELEMENT_NAME_REGEX;
import static org.eclipse.lsp4xml.dom.parser.Constants.PI_TAG_NAME;
import static org.eclipse.lsp4xml.dom.parser.Constants.PROLOG_NAME_OPTIONS;
import static org.eclipse.lsp4xml.dom.parser.Constants.URL_VALUE_REGEX;
import static org.eclipse.lsp4xml.dom.parser.Constants._AVL;
import static org.eclipse.lsp4xml.dom.parser.Constants._CAR;
import static org.eclipse.lsp4xml.dom.parser.Constants._CMA;
import static org.eclipse.lsp4xml.dom.parser.Constants._CRB;
import static org.eclipse.lsp4xml.dom.parser.Constants._CSB;
import static org.eclipse.lsp4xml.dom.parser.Constants._CVL;
import static org.eclipse.lsp4xml.dom.parser.Constants._DQO;
import static org.eclipse.lsp4xml.dom.parser.Constants._DVL;
import static org.eclipse.lsp4xml.dom.parser.Constants._EQS;
import static org.eclipse.lsp4xml.dom.parser.Constants._EVL;
import static org.eclipse.lsp4xml.dom.parser.Constants._EXL;
import static org.eclipse.lsp4xml.dom.parser.Constants._FSL;
import static org.eclipse.lsp4xml.dom.parser.Constants._IVL;
import static org.eclipse.lsp4xml.dom.parser.Constants._LAN;
import static org.eclipse.lsp4xml.dom.parser.Constants._LVL;
import static org.eclipse.lsp4xml.dom.parser.Constants._MIN;
import static org.eclipse.lsp4xml.dom.parser.Constants._MVL;
import static org.eclipse.lsp4xml.dom.parser.Constants._NVL;
import static org.eclipse.lsp4xml.dom.parser.Constants._NWL;
import static org.eclipse.lsp4xml.dom.parser.Constants._ORB;
import static org.eclipse.lsp4xml.dom.parser.Constants._OSB;
import static org.eclipse.lsp4xml.dom.parser.Constants._OVL;
import static org.eclipse.lsp4xml.dom.parser.Constants._PVL;
import static org.eclipse.lsp4xml.dom.parser.Constants._QMA;
import static org.eclipse.lsp4xml.dom.parser.Constants._RAN;
import static org.eclipse.lsp4xml.dom.parser.Constants._SIQ;
import static org.eclipse.lsp4xml.dom.parser.Constants._SQO;
import static org.eclipse.lsp4xml.dom.parser.Constants._SVL;
import static org.eclipse.lsp4xml.dom.parser.Constants._TVL;
import static org.eclipse.lsp4xml.dom.parser.Constants._WSP;
import static org.eclipse.lsp4xml.dom.parser.Constants._YVL;

import org.eclipse.lsp4xml.dom.DOMDocumentType.DocumentTypeKind;;

/**
 * XML scanner implementation.
 *
 */
public class XMLScanner implements Scanner {

	MultiLineStream stream;
	ScannerState state;
	int tokenOffset;
	TokenType tokenType;
	String tokenError;

	boolean hasSpaceAfterTag;
	String lastTag;
	String lastAttributeName;
	String lastTypeValue;
	String lastDoctypeKind;
	String url;
	boolean isInsideDTDContent = false; // Either internal dtd in xml file OR external dtd in dtd file

	public XMLScanner(String input, int initialOffset, ScannerState initialState) {
		stream = new MultiLineStream(input, initialOffset);
		state = initialState;
		tokenOffset = 0;
		isInsideDTDContent = ScannerState.DTDWithinContent.equals(initialState);
		tokenType = TokenType.Unknown;
	}

	String nextElementName() {
		return stream.advanceIfRegExp(ELEMENT_NAME_REGEX).toLowerCase();
	}

	String nextAttributeName() {
		return stream.advanceIfRegExp(ATTRIBUTE_NAME_REGEX).toLowerCase();
	}

	String doctypeName() {
		return stream.advanceIfRegExp(ATTRIBUTE_NAME_REGEX).toLowerCase();
	}

	String doctypeKind() {
		return stream.advanceIfRegExp(DOCTYPE_KIND_OPTIONS);
	}

	TokenType finishToken(int offset, TokenType type) {
		return finishToken(offset, type, null);
	}

	TokenType finishToken(int offset, TokenType type, String errorMessage) {
		tokenType = type;
		tokenOffset = offset;
		tokenError = errorMessage;
		return type;
	}

	@Override
	public TokenType scan() {
		int offset = stream.pos();
		ScannerState oldState = state;
		TokenType token = internalScan();
		if (token != TokenType.EOS && offset == stream.pos()) {
			log("Scanner.scan has not advanced at offset " + offset + ", state before: " + oldState + " after: "
					+ state);
			stream.advance(1);
			return finishToken(offset, TokenType.Unknown);
		}
		return token;
	}

	private void log(String message) {
		System.err.println(message);
	}

	TokenType internalScan() {
		int offset = stream.pos();
		if (stream.eos()) {
			return finishToken(offset, TokenType.EOS);
		}
		String errorMessage = null;

		switch (state) {
		case WithinComment:
			if (stream.advanceIfChars(_MIN, _MIN, _RAN)) { // -->
				state = !isInsideDTDContent ? ScannerState.WithinContent : ScannerState.DTDWithinContent;
				return finishToken(offset, TokenType.EndCommentTag);
			}
			stream.advanceUntilChars(_MIN, _MIN, _RAN); // -->
			return finishToken(offset, TokenType.Comment);

		case PrologOrPI:
			if (stream.advanceIfChars(_QMA, _RAN)) { // ?>
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.PIEnd);
			}
			if (stream.advanceUntilAnyOfChars(_NWL, _CAR, _WSP, _QMA, _RAN) || stream.eos()) { // \n or \r or ' ' or '?'
				String name = getTokenTextFromOffset(offset);
				if (PROLOG_NAME_OPTIONS.matcher(name).matches()) { // name eg: xml
					state = ScannerState.WithinTag;
					return finishToken(offset, TokenType.PrologName);
				}
				if (PI_TAG_NAME.matcher(name).matches()) { // {name} eg: m2e
					state = ScannerState.WithinPI;
					return finishToken(offset, TokenType.PIName);
				}
			}
			stream.advanceUntilCharsOrNewTag(_QMA, _RAN); // ?>
			if (stream.peekChar() == _LAN) {
				state = ScannerState.WithinContent; // TODO: check if EOF causes issues
			}
			return internalScan();

		case WithinPI:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (stream.advanceIfChars(_QMA, _RAN)) {
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.PIEnd);
			}
			if (stream.advanceUntilCharsOrNewTag(_QMA, _RAN)) { // ?>
				if (stream.peekChar() == _LAN) {
					state = ScannerState.WithinContent;
				}
				if (getTokenTextFromOffset(offset).length() == 0) {
					return finishToken(offset, TokenType.PIEnd);
				}
			}
			return finishToken(offset, TokenType.PIContent);

		case WithinContent:
			if (stream.advanceIfChar(_LAN)) { // <
				if (!stream.eos() && stream.peekChar() == _EXL) { // !
					if (stream.advanceIfChars(_EXL, _MIN, _MIN)) { // !--
						state = ScannerState.WithinComment;
						return finishToken(offset, TokenType.StartCommentTag);
					}
					if (stream.advanceIfChars(_EXL, _OSB, _CVL, _DVL, _AVL, _TVL, _AVL, _OSB)) { // ![CDATA[
						state = ScannerState.WithinCDATA;
						return finishToken(offset, TokenType.CDATATagOpen);
					}

					if (stream.advanceIfChars(_EXL, _DVL, _OVL, _CVL, _TVL, _YVL, _PVL, _EVL)) { // !DOCTYPE
						state = ScannerState.DTDWithinDoctype;
						return finishToken(offset, TokenType.DTDStartDoctypeTag);
					}

				} else if (!stream.eos() && stream.advanceIfChar(_QMA)) { // ?
					state = ScannerState.PrologOrPI;
					return finishToken(offset, TokenType.StartPrologOrPI);
				}
				if (stream.advanceIfChar(_FSL)) { // /
					state = ScannerState.AfterOpeningEndTag;
					return finishToken(offset, TokenType.EndTagOpen);
				}
				state = ScannerState.AfterOpeningStartTag;
				return finishToken(offset, TokenType.StartTagOpen);
			}
			stream.advanceUntilChar(_LAN); // <
			return finishToken(offset, TokenType.Content);

		case WithinCDATA:
			if (stream.advanceIfChars(_CSB, _CSB, _RAN)) { // ]]>
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.CDATATagClose);
			}
			stream.advanceUntilChars(_CSB, _CSB, _RAN); // ]]>
			return finishToken(offset, TokenType.CDATAContent);

		case AfterOpeningEndTag:
			String tagName = nextElementName();
			if (tagName.length() > 0) {
				state = ScannerState.WithinEndTag;
				return finishToken(offset, TokenType.EndTag);
			}
			if (stream.skipWhitespace()) { // white space is not valid here
				return finishToken(offset, TokenType.Whitespace,
						localize("error.unexpectedWhitespace", "Tag name must directly follow the open bracket."));
			}
			state = ScannerState.WithinEndTag;
			if (stream.advanceUntilCharOrNewTag(_RAN)) { // >
				if (stream.peekChar() == _LAN) { // <
					state = ScannerState.WithinContent;
				}
				return internalScan();
			}
			return finishToken(offset, TokenType.Unknown);

		case WithinEndTag:
			if (stream.skipWhitespace()) { // white space is valid here
				return finishToken(offset, TokenType.Whitespace);
			}
			if (stream.advanceIfChar(_RAN)) { // >
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.EndTagClose);
			}
			if (stream.advanceUntilChar(_LAN)) { // <
				state = ScannerState.WithinContent;
				return internalScan();
			}
			return finishToken(offset, TokenType.Whitespace);

		case AfterOpeningStartTag:
			lastTag = nextElementName();
			lastTypeValue = null;
			lastAttributeName = null;
			if (lastTag.length() > 0) {
				hasSpaceAfterTag = false;
				state = ScannerState.WithinTag;
				return finishToken(offset, TokenType.StartTag);
			}
			if (stream.skipWhitespace()) { // white space is not valid here
				return finishToken(offset, TokenType.Whitespace,
						localize("error.unexpectedWhitespace", "Tag name must directly follow the open bracket."));
			}
			state = ScannerState.WithinTag;
			if (stream.advanceUntilCharOrNewTag(_RAN)) { // >
				if (stream.peekChar() == _LAN) { // <
					state = ScannerState.WithinContent;
				}
				return internalScan();
			}
			return finishToken(offset, TokenType.Unknown);

		case WithinTag:
			if (stream.skipWhitespace()) {
				hasSpaceAfterTag = true; // remember that we have seen a whitespace
				return finishToken(offset, TokenType.Whitespace);
			}
			if (stream.advanceIfChars(_QMA, _RAN)) { // ?>
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.PrologEnd);
			}
			if (hasSpaceAfterTag) {
				lastAttributeName = nextAttributeName();
				if (lastAttributeName.length() > 0) {
					state = ScannerState.AfterAttributeName;
					hasSpaceAfterTag = false;
					return finishToken(offset, TokenType.AttributeName);
				}
			}
			if (stream.advanceIfChars(_FSL, _RAN)) { // />
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.StartTagSelfClose);
			}
			if (stream.advanceIfChar(_RAN)) { // >
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.StartTagClose);
			}

			if (stream.advanceUntilChar(_LAN)) { // <
				state = ScannerState.WithinContent;
				return internalScan();
			}
			return finishToken(offset, TokenType.Unknown);

		case AfterAttributeName:
			if (stream.skipWhitespace()) {
				hasSpaceAfterTag = true;
				return finishToken(offset, TokenType.Whitespace);
			}

			if (stream.advanceIfChar(_EQS)) {
				state = ScannerState.BeforeAttributeValue;
				return finishToken(offset, TokenType.DelimiterAssign);
			}
			state = ScannerState.WithinTag;
			return internalScan(); // no advance yet - jump to WithinTag

		case BeforeAttributeValue:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}
			String attributeValue = stream.advanceIfRegExp(ATTRIBUTE_VALUE_REGEX);
			if (attributeValue.length() > 0) {
				if ("type".equals(lastAttributeName)) {
					lastTypeValue = attributeValue;
				}
				state = ScannerState.WithinTag;
				hasSpaceAfterTag = false;
				return finishToken(offset, TokenType.AttributeValue);
			}
			int ch = stream.peekChar();
			if (ch == _SQO || ch == _DQO || ch == _SIQ) { // " || " || '
				stream.advance(1); // consume quote
				if (stream.advanceUntilChar(ch)) {
					stream.advance(1); // consume quote
				}
				if ("type".equals(lastAttributeName)) {
					lastTypeValue = stream.getSource().substring(offset + 1, stream.pos() - 1);
				}
				state = ScannerState.WithinTag;
				hasSpaceAfterTag = false;
				return finishToken(offset, TokenType.AttributeValue);
			}
			state = ScannerState.WithinTag;
			hasSpaceAfterTag = false;
			return internalScan(); // no advance yet - jump to WithinTag

		// DTD

		case DTDWithinDoctype:
			// Possible formats:
			// https://en.wikipedia.org/wiki/Document_type_declaration#Syntax
			lastDoctypeKind = null;
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (stream.advanceIfChar(_OSB)) { // [
				state = ScannerState.DTDWithinContent;
				isInsideDTDContent = true;
				return finishToken(offset, TokenType.DTDStartInternalSubset);
			}

			if (stream.advanceIfChar(_CSB)) { // ]
				state = ScannerState.DTDWithinDoctype;
				isInsideDTDContent = false;
				return finishToken(offset, TokenType.DTDEndInternalSubset);
			}
			
			if (stream.advanceIfChar(_RAN)) { // >
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.DTDEndDoctypeTag);
			}

			String doctypeName = doctypeName();
			if (!doctypeName.equals("")) {
				state = ScannerState.DTDAfterDoctypeName;
				return finishToken(offset, TokenType.DTDDoctypeName);
			}
			state = ScannerState.DTDWithinDoctype;
			return internalScan();

		case DTDAfterDoctypeName:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}
			if (stream.advanceIfChar(_OSB)) { // [
				state = ScannerState.DTDWithinContent;
				isInsideDTDContent = true;
				return finishToken(offset, TokenType.DTDStartInternalSubset);
			}
			lastDoctypeKind = doctypeKind(); // eg: PUBLIC || SYSTEM
			if (lastDoctypeKind.equals(DocumentTypeKind.PUBLIC.name())) {
				state = ScannerState.DTDAfterDoctypePUBLIC;
				return finishToken(offset, TokenType.DTDDocTypeKindPUBLIC);
			} else if (lastDoctypeKind.equals(DocumentTypeKind.SYSTEM.name())) {
				state = ScannerState.DTDAfterDoctypeSYSTEM;
				return finishToken(offset, TokenType.DTDDocTypeKindSYSTEM);
			}
			state = ScannerState.DTDWithinDoctype;
			return internalScan();

		case DTDAfterDoctypePUBLIC:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}
			url = stream.advanceIfRegExp(URL_VALUE_REGEX);
			if (!url.equals("")) {
				state = ScannerState.DTDAfterDoctypePublicId;
				return finishToken(offset, TokenType.DTDDoctypePublicId);
			}

			state = ScannerState.DTDWithinDoctype;
			return internalScan();

		case DTDAfterDoctypeSYSTEM:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			state = ScannerState.DTDWithinDoctype;
			url = stream.advanceIfRegExp(URL_VALUE_REGEX);
			if (!url.equals("")) {
				return finishToken(offset, TokenType.DTDDoctypeSystemId);
			}
			return internalScan();

		case DTDAfterDoctypePublicId:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			state = ScannerState.DTDWithinDoctype;
			url = stream.advanceIfRegExp(URL_VALUE_REGEX); // scan the System Identifier URL
			if (!url.equals("")) {
				return finishToken(offset, TokenType.DTDDoctypeSystemId);
			}
			return internalScan();

		case DTDWithinContent:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (stream.advanceIfChar(_CSB)) { // ]
				state = ScannerState.DTDWithinDoctype;
				isInsideDTDContent = false;
				return finishToken(offset, TokenType.DTDEndInternalSubset);
			}

			if (stream.advanceIfChars(_LAN, _EXL)) { // <!
				if (stream.advanceIfChars(_EVL, _LVL, _EVL, _MVL, _EVL, _NVL, _TVL)) { // ELEMENT
					state = ScannerState.DTDWithinElement;
					return finishToken(offset, TokenType.DTDStartElementDecl);
				} else if (stream.advanceIfChars(_AVL, _TVL, _TVL, _LVL, _IVL, _SVL, _TVL)) { // ATTLIST
					state = ScannerState.DTDWithinAttlist;
					return finishToken(offset, TokenType.DTDStartAttlistDecl);
				} else if (stream.advanceIfChars(_EVL, _NVL, _TVL, _IVL, _TVL, _YVL)) { // ENTITY
					state = ScannerState.DTDWithinEntity;
					return finishToken(offset, TokenType.DTDStartEntity);
				} else if (stream.advanceIfChars(_MIN, _MIN)) { // -- (for comment)
					state = ScannerState.WithinComment;
					return finishToken(offset, TokenType.StartCommentTag);
				}
			}

			if (stream.peekChar() == _LAN) { // <
				if (stream.advanceUntilChar(_RAN)) { // >
					stream.advance(1); // consume >
				}
				return finishToken(offset, TokenType.DTDUndefineTag);
			}
			if (stream.advanceUntilChar(_CSB)) {  // >
				state = ScannerState.DTDWithinDoctype;
				isInsideDTDContent = false;
				return finishToken(offset, TokenType.DTDEndInternalSubset);
			}
			return internalScan();

		case DTDIncorrectTagFormat: // Covers ATTLIST, ELEMENT, ENTITY when there is an issue parsing
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (stream.advanceIfChar(_RAN) || stream.peekChar() == _LAN) { // > || <
				state = ScannerState.DTDWithinContent;
				return finishToken(offset, TokenType.DTDEndTag);
			}

			if (stream.advanceUntilCharOrNewTag(_RAN)) { // > || <
				return finishToken(offset, TokenType.DTDTagExcessContent); // More items provided in tag than schema
																			// defines
			}
			return internalScan();

		case DTDWithinElement:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (stream.advanceIfChar(_RAN)) { // >
				state = ScannerState.DTDWithinContent;
				return finishToken(offset, TokenType.DTDEndTag);
			}

			if (!stream.advanceIfRegExp(Constants.ELEMENT_NAME_REGEX).equals("")) {
				state = ScannerState.DTDAfterElementName;
				return finishToken(offset, TokenType.DTDElementDeclName);
			}

			state = ScannerState.DTDIncorrectTagFormat;
			return internalScan();

		case DTDAfterElementName:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (stream.advanceIfChar(_ORB)) { // (
				state = ScannerState.DTDWithinElementContent;
				return finishToken(offset, TokenType.DTDStartElementContent);
			}

			if (!stream.advanceIfRegExp(DTD_ELEMENT_CATEGORY).equals("")) {
				state = ScannerState.DTDWithinElement;
				return finishToken(offset, TokenType.DTDElementCategory);
			}

			state = ScannerState.DTDIncorrectTagFormat;
			return internalScan();

		case DTDWithinElementContent:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (stream.advanceIfChar(_CMA)) { // ,
				return finishToken(offset, TokenType.DTDElementContentComma);
			}

			if (stream.advanceIfChar(_CRB)) { // )
				state = ScannerState.DTDWithinElement;
				return finishToken(offset, TokenType.DTDEndElementContent);
			}

			if (stream.advanceUntilAnyOfChars(_CMA, _CRB)) { // , || )
				return finishToken(offset, TokenType.DTDElementContent);
			}
			return internalScan();

		case DTDWithinAttlist:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (stream.advanceIfChar(_RAN)) { // >
				state = ScannerState.DTDWithinContent;
				return finishToken(offset, TokenType.DTDEndTag);
			}

			if (!stream.advanceIfRegExp(Constants.ELEMENT_NAME_REGEX).equals("")) {
				state = ScannerState.DTDAfterAttlistElementName;
				return finishToken(offset, TokenType.DTDAttlistAttributeName);
			}

			state = ScannerState.DTDIncorrectTagFormat;
			return internalScan();

		case DTDAfterAttlistElementName:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (!stream.advanceIfRegExp(Constants.ATTRIBUTE_NAME_REGEX).equals("")) {
				state = ScannerState.DTDAfterAttlistAttributeName;
				return finishToken(offset, TokenType.DTDAttlistAttributeName);
			}
			state = ScannerState.DTDIncorrectTagFormat;
			return internalScan();

		case DTDAfterAttlistAttributeName:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (!stream.advanceIfRegExp(Constants.DTD_ATTLIST_ATTRIBUTE_TYPE).equals("")) {
				state = ScannerState.DTDAfterAttlistAttributeType;
				return finishToken(offset, TokenType.DTDAttlistType);
			}
			state = ScannerState.DTDIncorrectTagFormat;
			return internalScan();

		case DTDAfterAttlistAttributeType:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (!stream.advanceIfRegExp(Constants.DTD_ATTLIST_ATTRIBUTE_VALUE).equals("")) {
				state = ScannerState.DTDWithinAttlist;
				return finishToken(offset, TokenType.DTDAttlistAttributeValue);
			}
			state = ScannerState.DTDIncorrectTagFormat;
			return internalScan();

		case DTDWithinEntity:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (stream.advanceIfChar(_RAN)) { // >
				state = ScannerState.DTDWithinContent;
				return finishToken(offset, TokenType.DTDEndTag);
			}

			if (!stream.advanceIfRegExp(Constants.ELEMENT_NAME_REGEX).equals("")) {
				state = ScannerState.DTDAfterEntityName;
				return finishToken(offset, TokenType.DTDEntityName);
			}

			state = ScannerState.DTDIncorrectTagFormat;
			return internalScan();

		case DTDAfterEntityName:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (!stream.advanceIfRegExp(Constants.DTD_ENTITY_VALUE).equals("")) {
				state = ScannerState.DTDWithinEntity;
				return finishToken(offset, TokenType.DTDEntityValue);
			}

			if (stream.advanceIfChars(_SVL, _YVL, _SVL, _TVL, _EVL, _MVL)) { // SYSTEM
				state = ScannerState.DTDAfterEntityKind;
				return finishToken(offset, TokenType.DTDEntityKind);
			}

			state = ScannerState.DTDIncorrectTagFormat;
			return internalScan();

		case DTDAfterEntityKind:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (!stream.advanceIfRegExp(URL_VALUE_REGEX).equals("")) {
				state = ScannerState.DTDWithinEntity;
				return finishToken(offset, TokenType.DTDEntityURL);
			}

			state = ScannerState.DTDIncorrectTagFormat;
			return internalScan();
		}

		stream.advance(1);
		state = isInsideDTDContent ? ScannerState.DTDWithinContent : ScannerState.WithinContent;
		return finishToken(offset, TokenType.Unknown, errorMessage);
	}

	private String localize(String string, String string2) {
		// TODO Auto-generated method stub
		return string;
	}

	@Override
	public TokenType getTokenType() {
		return tokenType;
	}

	@Override
	/**
	 * Starting offset position of the current token
	 * 
	 * @return Starting offset position of the current token
	 */
	public int getTokenOffset() {
		return tokenOffset;
	}

	@Override
	public int getTokenLength() {
		return stream.pos() - tokenOffset;
	}

	@Override
	/**
	 * Ending offset position of the current token
	 * 
	 * @return Ending offset position of the current token
	 */
	public int getTokenEnd() {
		return stream.pos();
	}

	@Override
	public String getTokenText() {
		return stream.getSource().substring(tokenOffset, stream.pos());
	}

	@Override
	public ScannerState getScannerState() {
		return state;
	}

	@Override
	public String getTokenError() {
		return tokenError;
	}

	public String getTokenTextFromOffset(int offset) {
		return stream.getSource().substring(offset, stream.pos());
	}

	public static Scanner createScanner(String input) {
		return createScanner(input, false);
	}

	public static Scanner createScanner(String input, boolean isDTD) {
		return createScanner(input, 0, isDTD);
	}

	public static Scanner createScanner(String input, int initialOffset) {
		return createScanner(input, initialOffset, false);
	}

	public static Scanner createScanner(String input, int initialOffset, boolean insideDTD) {
		return createScanner(input, initialOffset, insideDTD ? ScannerState.DTDWithinContent : ScannerState.WithinContent);
	}

	public static Scanner createScanner(String input, int initialOffset, ScannerState initialState) {
		return new XMLScanner(input, initialOffset, initialState);
	}

}
