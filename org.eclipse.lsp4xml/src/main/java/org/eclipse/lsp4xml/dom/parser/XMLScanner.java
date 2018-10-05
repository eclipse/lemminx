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
import static org.eclipse.lsp4xml.dom.parser.Constants.ELEMENT_NAME_REGEX;
import static org.eclipse.lsp4xml.dom.parser.Constants.PI_TAG_NAME;
import static org.eclipse.lsp4xml.dom.parser.Constants.PROLOG_NAME_OPTIONS;
import static org.eclipse.lsp4xml.dom.parser.Constants._AVL;
import static org.eclipse.lsp4xml.dom.parser.Constants._BNG;
import static org.eclipse.lsp4xml.dom.parser.Constants._CSB;
import static org.eclipse.lsp4xml.dom.parser.Constants._CVL;
import static org.eclipse.lsp4xml.dom.parser.Constants._DQO;
import static org.eclipse.lsp4xml.dom.parser.Constants._DVL;
import static org.eclipse.lsp4xml.dom.parser.Constants._EQS;
import static org.eclipse.lsp4xml.dom.parser.Constants._EVL;
import static org.eclipse.lsp4xml.dom.parser.Constants._FSL;
import static org.eclipse.lsp4xml.dom.parser.Constants._LAN;
import static org.eclipse.lsp4xml.dom.parser.Constants._MIN;
import static org.eclipse.lsp4xml.dom.parser.Constants._OSB;
import static org.eclipse.lsp4xml.dom.parser.Constants._OVL;
import static org.eclipse.lsp4xml.dom.parser.Constants._PVL;
import static org.eclipse.lsp4xml.dom.parser.Constants._QMA;
import static org.eclipse.lsp4xml.dom.parser.Constants._RAN;
import static org.eclipse.lsp4xml.dom.parser.Constants._SIQ;
import static org.eclipse.lsp4xml.dom.parser.Constants._SQO;
import static org.eclipse.lsp4xml.dom.parser.Constants._TVL;
import static org.eclipse.lsp4xml.dom.parser.Constants._WSP;
import static org.eclipse.lsp4xml.dom.parser.Constants._YVL;
import static org.eclipse.lsp4xml.dom.parser.Constants._CAR;
import static org.eclipse.lsp4xml.dom.parser.Constants._NWL;;

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

	public XMLScanner(String input, int initialOffset, ScannerState initialState) {
		stream = new MultiLineStream(input, initialOffset);
		state = initialState;
		tokenOffset = 0;
		tokenType = TokenType.Unknown;
	}

	String nextElementName() {
		return stream.advanceIfRegExp(ELEMENT_NAME_REGEX).toLowerCase();
	}

	String nextAttributeName() {
		return stream.advanceIfRegExp(ATTRIBUTE_NAME_REGEX).toLowerCase();
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
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.EndCommentTag);
			}
			stream.advanceUntilChars(_MIN, _MIN, _RAN); // -->
			return finishToken(offset, TokenType.Comment);
		case WithinDoctype:
			if (stream.advanceIfChar(_RAN)) { // >
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.EndDoctypeTag);
			}
			stream.advanceUntilCharOrNewTag(_RAN); // >
			if (stream.peekChar() == _LAN) { // <
				state = ScannerState.WithinContent;
			}
			return finishToken(offset, TokenType.Doctype);
		case PrologOrPI:
			if (stream.advanceIfChars(_QMA, _RAN)) { // ?>
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.PIEnd);
			}
			if (stream.advanceUntilAnyOfChars(_NWL,_CAR,_WSP, _QMA,_RAN) || stream.eos()) { // \n or \r or ' ' or '?'
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
				if (!stream.eos() && stream.peekChar() == _BNG) { // !
					if (stream.advanceIfChars(_BNG, _MIN, _MIN)) { // !--
						state = ScannerState.WithinComment;
						return finishToken(offset, TokenType.StartCommentTag);
					}
					if (stream.advanceIfChars(_BNG, _OSB, _CVL, _DVL, _AVL, _TVL, _AVL, _OSB)) { // ![CDATA[
						state = ScannerState.WithinCDATA;
						return finishToken(offset, TokenType.CDATATagOpen);
					}

					if (stream.advanceIfChars(_BNG, _DVL, _OVL, _CVL, _TVL, _YVL, _PVL, _EVL)) { // !DOCTYPE
						state = ScannerState.WithinDoctype;
						return finishToken(offset, TokenType.StartDoctypeTag);
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
			if (ch == _SQO || ch == _DQO || ch == _SIQ) {
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
		}

		stream.advance(1);
		state = ScannerState.WithinContent;
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
	public int getTokenOffset() {
		return tokenOffset;
	}

	@Override
	public int getTokenLength() {
		return stream.pos() - tokenOffset;
	}

	@Override
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
		return createScanner(input, 0);
	}

	public static Scanner createScanner(String input, int initialOffset) {
		return createScanner(input, initialOffset, ScannerState.WithinContent);
	}

	public static Scanner createScanner(String input, int initialOffset, ScannerState initialState) {
		return new XMLScanner(input, initialOffset, initialState);
	}
}
