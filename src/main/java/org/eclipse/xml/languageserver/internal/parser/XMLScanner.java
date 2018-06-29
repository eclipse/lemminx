package org.eclipse.xml.languageserver.internal.parser;

import static org.eclipse.xml.languageserver.internal.parser.Constants._BNG;
import static org.eclipse.xml.languageserver.internal.parser.Constants._DQO;
import static org.eclipse.xml.languageserver.internal.parser.Constants._EQS;
import static org.eclipse.xml.languageserver.internal.parser.Constants._FSL;
import static org.eclipse.xml.languageserver.internal.parser.Constants._LAN;
import static org.eclipse.xml.languageserver.internal.parser.Constants._MIN;
import static org.eclipse.xml.languageserver.internal.parser.Constants._RAN;
import static org.eclipse.xml.languageserver.internal.parser.Constants._SQO;
import static org.eclipse.xml.languageserver.internal.parser.Constants._OSB;
import static org.eclipse.xml.languageserver.internal.parser.Constants._CSB;
import static org.eclipse.xml.languageserver.internal.parser.Constants._CVL;
import static org.eclipse.xml.languageserver.internal.parser.Constants._DVL;
import static org.eclipse.xml.languageserver.internal.parser.Constants._AVL;
import static org.eclipse.xml.languageserver.internal.parser.Constants._TVL;


import java.util.regex.Pattern;

public class XMLScanner implements Scanner {

	private static final Pattern ELEMENT_NAME_REGEX = Pattern.compile("^[_:\\w][_:\\w-.\\d]*");

	private static final Pattern ATTRIBUTE_NAME_REGEX = Pattern.compile("^[^\\s\"'>/=\\x00-\\x0F\\x7F\\x80-\\x9F]*");

	private static final Pattern ATTRIBUTE_VALUE_REGEX = Pattern.compile("^[^\\s\"'`=<>\\/]+");

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
			if (stream.advanceIfChar(_RAN)) {
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.EndDoctypeTag);
			}
			stream.advanceUntilChar(_RAN); // >
			return finishToken(offset, TokenType.Doctype);
		case WithinContent:
			if (stream.advanceIfChar(_LAN)) { // <
				if (!stream.eos() && stream.peekChar() == _BNG) { // !
					if (stream.advanceIfChars(_BNG, _MIN, _MIN)) { // !--
						state = ScannerState.WithinComment;
						return finishToken(offset, TokenType.StartCommentTag);
					}
					if(stream.advanceIfChars(_BNG, _OSB, _CVL, _DVL,_AVL, _TVL, _AVL, _OSB)) { // ![CDATA[
						state = ScannerState.WithinCDATA;
						return finishToken(offset, TokenType.CDATATagOpen);
					}
					
						/*
					 * AZ: if (stream.advanceIfRegExp(/^!doctype/i)) { state =
					 * ScannerState.WithinDoctype; return finishToken(offset,
					 * TokenType.StartDoctypeTag); }
					 */
				}
				if (stream.advanceIfChar(_FSL)) { // /
					state = ScannerState.AfterOpeningEndTag;
					return finishToken(offset, TokenType.EndTagOpen);
				}
				state = ScannerState.AfterOpeningStartTag;
				return finishToken(offset, TokenType.StartTagOpen);
			}
			stream.advanceUntilChar(_LAN);
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
			stream.advanceUntilChar(_RAN);
			if (offset < stream.pos()) {
				return finishToken(offset, TokenType.Unknown,
						localize("error.endTagNameExpected", "End tag name expected."));
			}
			return internalScan();
		case WithinEndTag:
			if (stream.skipWhitespace()) { // white space is valid here
				return finishToken(offset, TokenType.Whitespace);
			}
			if (stream.advanceIfChar(_RAN)) { // >
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.EndTagClose);
			}
			errorMessage = localize("error.tagNameExpected", "Closing bracket expected.");
			break;
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
			stream.advanceUntilChar(_RAN);
			if (offset < stream.pos()) {
				return finishToken(offset, TokenType.Unknown,
						localize("error.startTagNameExpected", "Start tag name expected."));
			}
			return internalScan();
		case WithinTag:
			if (stream.skipWhitespace()) {
				hasSpaceAfterTag = true; // remember that we have seen a whitespace
				return finishToken(offset, TokenType.Whitespace);
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
				if (lastTag == "script") {
					if (lastTypeValue != null /* AZ: && htmlScriptContents[lastTypeValue] */) {
						// stay in html
						state = ScannerState.WithinContent;
					} else {
						state = ScannerState.WithinScriptContent;
					}
				} else if ("style".equals(lastTag)) {
					state = ScannerState.WithinStyleContent;
				} else {
					state = ScannerState.WithinContent;
				}
				return finishToken(offset, TokenType.StartTagClose);
			}
			stream.advance(1);
			return finishToken(offset, TokenType.Unknown,
					localize("error.unexpectedCharacterInTag", "Unexpected character in tag."));
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
			if (ch == _SQO || ch == _DQO) {
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
		case WithinScriptContent:
			// see
			// http://stackoverflow.com/questions/14574471/how-do-browsers-parse-a-script-tag-exactly
			int sciptState = 1;
			while (!stream.eos()) {
				String match = ""; // AZ: stream.advanceIfRegExp(/<!--|-->|<\/?script\s*\/?>?/i);
				if (match.length() == 0) {
					stream.goToEnd();
					return finishToken(offset, TokenType.Script);
				} else if ("<!--".equals(match)) {
					if (sciptState == 1) {
						sciptState = 2;
					}
				} else if ("-->".equals(match)) {
					sciptState = 1;
				} /*
					 * TODO : else if (match[1] !== '/') { // <script if (sciptState === 2) {
					 * sciptState = 3; } }
					 */ else { // </script
					if (sciptState == 3) {
						sciptState = 2;
					} else {
						stream.goBack(match.length()); // to the beginning of the closing tag
						break;
					}
				}
			}
			state = ScannerState.WithinContent;
			if (offset < stream.pos()) {
				return finishToken(offset, TokenType.Script);
			}
			return internalScan(); // no advance yet - jump to content
		case WithinStyleContent:
			// AZ: stream.advanceUntilRegExp(/<\/style/i);
			state = ScannerState.WithinContent;
			if (offset < stream.pos()) {
				return finishToken(offset, TokenType.Styles);
			}
			return internalScan(); // no advance yet - jump to content
		}

		stream.advance(1);
		state = ScannerState.WithinContent;
		return finishToken(offset, TokenType.Unknown, errorMessage);
	}

	private String localize(String string, String string2) {
		// TODO Auto-generated method stub
		return null;
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
