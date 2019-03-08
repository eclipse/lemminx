/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.dom.parser;

import static org.eclipse.lsp4xml.dom.parser.Constants.*;

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

	
	String lastTag;
	String lastAttributeName;
	String lastTypeValue;
	String lastDoctypeKind;
	String url;
	boolean isInsideDTDContent = false; // Either internal dtd in xml file OR external dtd in dtd file
	boolean isDeclCompleted = false; // If any type of DTD declaration was supplied with all the required properties
	TokenType tempToken;
	boolean isDTDFile;
	/**
	 * boolean completedInitialAttDef;
	 * 
	 * If the first attribute definition was completed in an ATTLIST declaration
	 * eg:
	 * <!ATTLIST unit
			|power NMTOKEN #IMPLIED|  << AFTER THIS IS COMPLETE, will be set to true
			description CDATA #IMPLIED
		>
	 */
	boolean isInitialAttlistDeclCompleted = false; 
	private int nbBraceOpened;

	public XMLScanner(String input, int initialOffset, ScannerState initialState, boolean isDTDFile) {
		stream = new MultiLineStream(input, initialOffset);
		state = initialState;
		tokenOffset = 0;
		isInsideDTDContent = ScannerState.DTDWithinContent.equals(initialState);
		tokenType = TokenType.Unknown;
		this.isDTDFile = isDTDFile;
	}

	String nextElementName() {
		return stream.advanceIfRegExp(ELEMENT_NAME_REGEX).toLowerCase();
	}

	String nextAttributeName() {
		return stream.advanceIfRegExp(ATTRIBUTE_NAME_REGEX).toLowerCase();
	}

	String doctypeName() {
		return stream.advanceIfRegExp(ELEMENT_NAME_REGEX).toLowerCase();
	}

	/**
	 * Tries to advance off the regex for either 'PUBLIC' or 'SYSTEM'
	 * 
	 * @return "PUBLIC" or "SYSTEM" or "" otherwise
	 */
	String doctypeKind() {
		return stream.advanceIfRegExpGroup1(DOCTYPE_KIND_OPTIONS);
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
						isDeclCompleted = false;
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
				return finishToken(offset, TokenType.Whitespace);
			}
			if (stream.advanceIfChars(_QMA, _RAN)) { // ?>
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.PrologEnd);
			}
			
				lastAttributeName = nextAttributeName();
				if (lastAttributeName.length() > 0) {
					state = ScannerState.AfterAttributeName;
					return finishToken(offset, TokenType.AttributeName);
				}
			
			if (stream.advanceIfChar(_FSL)) { // /
				state = ScannerState.WithinContent;
				if(stream.advanceIfChar(_RAN)) { // >
					return finishToken(offset, TokenType.StartTagSelfClose);
				}
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
				return finishToken(offset, TokenType.AttributeValue);
			}
			state = ScannerState.WithinTag;
			return internalScan();

		// DTD

		case DTDWithinDoctype:
			// Possible formats:
			// https://en.wikipedia.org/wiki/Document_type_declaration#Syntax
			
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
				isDeclCompleted = true;
				return finishToken(offset, TokenType.DTDEndInternalSubset);
			}

			if (stream.advanceIfChar(_RAN)) { // >
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.DTDEndDoctypeTag);
			}

			if(stream.peekChar() == _LAN) { // <
				state = ScannerState.WithinContent;
				return internalScan();
			}

			
			if (isDeclCompleted == false) {
				String doctypeName = doctypeName();
				if(!doctypeName.equals("")) {
					state = ScannerState.DTDAfterDoctypeName;
					return finishToken(offset, TokenType.DTDDoctypeName);
				}
			}
			stream.advanceUntilCharOrNewTag(_RAN); // > || <
			return finishToken(offset, TokenType.DTDUnrecognizedParameters);

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
			state = ScannerState.DTDUnrecognizedParameters;
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

			state = ScannerState.DTDUnrecognizedParameters;
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

			state = ScannerState.DTDUnrecognizedParameters;
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

			state = ScannerState.DTDUnrecognizedParameters;
			return internalScan();

		case DTDWithinContent:
			if (!isDTDFile && (stream.advanceIfChar(_CSB) || stream.peekChar() == _RAN)) { // ] || >
				state = ScannerState.DTDWithinDoctype;
				isInsideDTDContent = false;
				return finishToken(offset, TokenType.DTDEndInternalSubset);
			}
			boolean startsWithLessThanBracket = false;
			if (stream.advanceIfChar(_LAN)) { // <
				startsWithLessThanBracket = true;
				if (!stream.eos() && stream.peekChar() == _EXL) { // !
					isDeclCompleted = false;
					if (stream.advanceIfChars(_EXL, _EVL, _LVL, _EVL, _MVL, _EVL, _NVL, _TVL)) { // !ELEMENT
						state = ScannerState.DTDWithinElement;
						return finishToken(offset, TokenType.DTDStartElement);
					} else if (stream.advanceIfChars(_EXL, _AVL, _TVL, _TVL, _LVL, _IVL, _SVL, _TVL)) { // !ATTLIST
						isInitialAttlistDeclCompleted = false;
						state = ScannerState.DTDWithinAttlist;
						return finishToken(offset, TokenType.DTDStartAttlist);
					} else if (stream.advanceIfChars(_EXL, _EVL, _NVL, _TVL, _IVL, _TVL, _YVL)) { // !ENTITY
						state = ScannerState.DTDWithinEntity;
						return finishToken(offset, TokenType.DTDStartEntity);
					} else if (stream.advanceIfChars(_EXL, _NVL, _OVL, _TVL, _AVL, _TVL, _IVL, _OVL, _NVL)) { // !NOTATION
						state = ScannerState.DTDWithinNotation;
						return finishToken(offset, TokenType.DTDStartNotation);
					} else if (stream.advanceIfChars(_EXL, _MIN, _MIN)) { // !-- (for comment)
						state = ScannerState.WithinComment;
						return finishToken(offset, TokenType.StartCommentTag);
					}
				}
			}
			if(isDTDFile) {
				if(startsWithLessThanBracket) {
					if(stream.advanceUntilCharOrNewTag(_RAN)){ // >
						if(stream.peekChar() == _RAN) {
							stream.advance(1); //consume '>'
						}
					}
				}
				else {
					stream.advanceUntilAnyOfChars(_LAN); // <
				}
			} else {
				stream.advanceUntilAnyOfChars(_RAN, _LAN, _CSB); // > || < || ]
				if(startsWithLessThanBracket && stream.peekChar() == _RAN) {
					stream.advance(1); //consume '>'
				}
			}
			return finishToken(offset, TokenType.Content);
			
	

		case DTDUnrecognizedParameters:

			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if(stream.advanceIfChar(_RAN)) { // >
				state = isInsideDTDContent ? ScannerState.DTDWithinContent : ScannerState.WithinContent;
				tempToken = isInsideDTDContent ? TokenType.DTDEndTag : TokenType.EndTagClose;
				return finishToken(offset, tempToken);
			}

			if(stream.peekChar() == _LAN) { // <
				state = isInsideDTDContent ? ScannerState.DTDWithinContent : ScannerState.WithinContent;
				return internalScan();
			}

			if(stream.peekChar() == _CSB && isInsideDTDContent) { // ]
				state = ScannerState.DTDWithinDoctype;
				return internalScan();
			}

			//If in DOCTYPE this will skip over the whole internal subset
			if(!isInsideDTDContent) {
				stream.advanceUntilAnyOfChars(_OSB, _RAN, _LAN); // [ | < | >
				if(stream.peekChar() == _OSB) {
					stream.advance(1);
					stream.advanceUntilCharUsingStack(_CSB);// ]
					
				}
			}
			
			if(stream.advanceUntilCharOrNewTag(_RAN)) { // >
				if(stream.peekChar() == _LAN) { // <
					state = isInsideDTDContent ? ScannerState.DTDWithinContent : ScannerState.DTDWithinDoctype;
				}
			}
			return finishToken(offset, TokenType.DTDUnrecognizedParameters);
			

		case DTDWithinElement:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (stream.advanceIfChar(_RAN)) { // >
				state = ScannerState.DTDWithinContent;
				return finishToken(offset, TokenType.DTDEndTag);
			}

			if(isDeclCompleted == true) {
				state = ScannerState.DTDUnrecognizedParameters;
				return internalScan();
			}

			if (!stream.advanceIfRegExp(Constants.ELEMENT_NAME_REGEX).equals("")) {
				state = ScannerState.DTDElementAfterName;
				return finishToken(offset, TokenType.DTDElementDeclName);
			}

			state = ScannerState.DTDUnrecognizedParameters;
			return internalScan();

		case DTDElementAfterName:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (stream.advanceIfChar(_ORB)) { // (
				nbBraceOpened = 1;
				state = ScannerState.DTDElementWithinContent;
				return finishToken(offset, TokenType.DTDStartElementContent);
			}

			if (!stream.advanceIfRegExpGroup1(DTD_ELEMENT_CATEGORY).equals("")) {
				isDeclCompleted = true;
				state = ScannerState.DTDWithinElement;
				return finishToken(offset, TokenType.DTDElementCategory);
			}

			state = ScannerState.DTDUnrecognizedParameters;
			return internalScan();

		case DTDElementWithinContent: // <!ELEMENT name |(content)|>

			if(stream.advanceIfChar(_CRB)) { // )
				isDeclCompleted = true;
				state = ScannerState.DTDWithinElement;
				stream.advanceIfAnyOfChars(_QMA, _AST, _PLS); // ? | * | +
				return finishToken(offset, TokenType.DTDEndElementContent);
			}

			while(nbBraceOpened > 0) {
				int c = stream.peekChar();

				if(c == _ORB) { // (
					nbBraceOpened++;
				}
				else if(c == _CRB) { // )
					nbBraceOpened--;
					if(nbBraceOpened == 0) {
						return finishToken(offset, TokenType.DTDElementContent);
					}
				}
				else if(c == _RAN) { // >
					state = ScannerState.DTDWithinElement;
					return finishToken(offset, TokenType.DTDElementContent);
				}
				else if(c == _LAN) { // <
					state = ScannerState.DTDWithinContent;
					return finishToken(offset, TokenType.DTDElementContent);
				}
				else if(c == -1) {
					return finishToken(offset, TokenType.DTDElementContent);
				}
				stream.advance(1);
			}

			state = ScannerState.DTDUnrecognizedParameters;
			return internalScan();

		case DTDWithinAttlist:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (stream.advanceIfChar(_RAN)) { // >
				state = ScannerState.DTDWithinContent;
				return finishToken(offset, TokenType.DTDEndTag);
			}

			if(isDeclCompleted == true) {
				state = ScannerState.DTDUnrecognizedParameters;
				return internalScan();
			}

			if (isInitialAttlistDeclCompleted == false && !stream.advanceIfRegExp(Constants.ELEMENT_NAME_REGEX).equals("")) {
				state = ScannerState.DTDAfterAttlistElementName;
				return finishToken(offset, TokenType.DTDAttlistElementName);
			}

			state = ScannerState.DTDUnrecognizedParameters;
			return internalScan();

		case DTDAfterAttlistElementName:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (!stream.advanceIfRegExp(Constants.ATTRIBUTE_NAME_REGEX).equals("")) {
				state = ScannerState.DTDAfterAttlistAttributeName;
				return finishToken(offset, TokenType.DTDAttlistAttributeName);
			}

			if (stream.advanceIfChar(_RAN)) { // >
				state = ScannerState.DTDWithinContent;
				return finishToken(offset, TokenType.DTDEndTag);
			}

			state = ScannerState.DTDUnrecognizedParameters;
			return internalScan();

		case DTDAfterAttlistAttributeName:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (!stream.advanceIfRegExpGroup1(Constants.DTD_ATTLIST_ATTRIBUTE_TYPE).equals("")) {
				state = ScannerState.DTDAfterAttlistAttributeType;
				return finishToken(offset, TokenType.DTDAttlistAttributeType);
			}

			if (stream.advanceIfChar(_RAN)) { // >
				state = ScannerState.DTDWithinContent;
				return finishToken(offset, TokenType.DTDEndTag);
			}

			state = ScannerState.DTDUnrecognizedParameters;
			return internalScan();

		case DTDAfterAttlistAttributeType:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (!stream.advanceIfRegExpGroup1(Constants.DTD_ATTLIST_ATTRIBUTE_VALUE).equals("")) {
				isInitialAttlistDeclCompleted = true; //we completed the initial attribute declaration
				isDeclCompleted = true;
				state = ScannerState.DTDAfterAttlistElementName;
				return finishToken(offset, TokenType.DTDAttlistAttributeValue);
			}

			if (stream.advanceIfChar(_RAN)) { // >
				state = ScannerState.DTDWithinContent;
				return finishToken(offset, TokenType.DTDEndTag);
			}

			state = ScannerState.DTDUnrecognizedParameters;
			return internalScan();

		case DTDWithinEntity:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (stream.advanceIfChar(_RAN)) { // >
				state = ScannerState.DTDWithinContent;
				return finishToken(offset, TokenType.DTDEndTag);
			}

			if(isDeclCompleted == true) {
				state = ScannerState.DTDUnrecognizedParameters;
				return internalScan();
			}

			if (!stream.advanceIfRegExp(Constants.ELEMENT_NAME_REGEX).equals("")) {
				state = ScannerState.DTDAfterEntityName;
				return finishToken(offset, TokenType.DTDEntityName);
			}

			if (stream.advanceIfChar(_PCT)) { // %
				return finishToken(offset, TokenType.DTDEntityPercent);
			}

			state = ScannerState.DTDUnrecognizedParameters;
			return internalScan();

		case DTDAfterEntityName:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (!stream.advanceIfRegExp(Constants.DTD_ENTITY_VALUE).equals("")) {
				isDeclCompleted = true;
				state = ScannerState.DTDWithinEntity;
				return finishToken(offset, TokenType.DTDEntityValue);
			}

			lastDoctypeKind = doctypeKind(); // eg: PUBLIC || SYSTEM, will advance the stream if either of these
			if (lastDoctypeKind.equals(DocumentTypeKind.PUBLIC.name())) {
				state = ScannerState.DTDAfterEntityPUBLIC;
				return finishToken(offset, TokenType.DTDEntityKindPUBLIC);
			} else if (lastDoctypeKind.equals(DocumentTypeKind.SYSTEM.name())) {
				state = ScannerState.DTDAfterEntitySYSTEM;
				return finishToken(offset, TokenType.DTDEntityKindSYSTEM);
			}

			if (stream.advanceIfChar(_RAN)) { // >
				state = ScannerState.DTDWithinContent;
				return finishToken(offset, TokenType.DTDEndTag);
			}

			state = ScannerState.DTDUnrecognizedParameters;
			return internalScan();
		
		case DTDAfterEntityPUBLIC:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (!stream.advanceIfRegExp(URL_VALUE_REGEX).equals("")) {
				state = ScannerState.DTDAfterEntitySYSTEM;
				return finishToken(offset, TokenType.DTDEntityPublicId);
			}

			if (stream.advanceIfChar(_RAN)) { // >
				state = ScannerState.DTDWithinContent;
				return finishToken(offset, TokenType.DTDEndTag);
			}

			state = ScannerState.DTDUnrecognizedParameters;
			return internalScan();

		case DTDAfterEntitySYSTEM:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (!stream.advanceIfRegExp(URL_VALUE_REGEX).equals("")) {
				isDeclCompleted = true;
				state = ScannerState.DTDWithinEntity;
				return finishToken(offset, TokenType.DTDEntitySystemId);
			}

			if (stream.advanceIfChar(_RAN)) { // >
				state = ScannerState.DTDWithinContent;
				return finishToken(offset, TokenType.DTDEndTag);
			}

			state = ScannerState.DTDUnrecognizedParameters;
			return internalScan();		
		
		case DTDWithinNotation: 
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if(stream.advanceIfChar(_RAN)) { // >
				state = ScannerState.DTDWithinContent;
				return finishToken(offset, TokenType.DTDEndTag);
			}

			if(isDeclCompleted == true) {
				state = ScannerState.DTDUnrecognizedParameters;
				return internalScan();
			}

			if (!stream.advanceIfRegExp(ELEMENT_NAME_REGEX).equals("")) {
				state = ScannerState.DTDAfterNotationName;
				return finishToken(offset, TokenType.DTDNotationName);
			}

			state = ScannerState.DTDUnrecognizedParameters;
			return internalScan();

		case DTDAfterNotationName:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}
		
			lastDoctypeKind = doctypeKind(); // eg: PUBLIC || SYSTEM, will advance the stream if either of these
			if (lastDoctypeKind.equals(DocumentTypeKind.PUBLIC.name())) {
				state = ScannerState.DTDAfterNotationPUBLIC;
				return finishToken(offset, TokenType.DTDNotationKindPUBLIC);
			} else if (lastDoctypeKind.equals(DocumentTypeKind.SYSTEM.name())) {
				state = ScannerState.DTDAfterNotationSYSTEM;
				return finishToken(offset, TokenType.DTDNotationKindSYSTEM);
			}

			state = ScannerState.DTDUnrecognizedParameters;
			return internalScan();

		case DTDAfterNotationPUBLIC:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}
			url = stream.advanceIfRegExp(URL_VALUE_REGEX);
			if (!url.equals("")) {
				isDeclCompleted = true;
				state = ScannerState.DTDAfterNotationPublicId;
				return finishToken(offset, TokenType.DTDNotationPublicId);
			}

			state = ScannerState.DTDUnrecognizedParameters;
			return internalScan();

		case DTDAfterNotationSYSTEM:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			state = ScannerState.DTDWithinNotation;
			url = stream.advanceIfRegExp(URL_VALUE_REGEX);
			if (!url.equals("")) {
				isDeclCompleted = true;
				state = ScannerState.DTDAfterNotationName;
				return finishToken(offset, TokenType.DTDNotationSystemId);
			}
			state = ScannerState.DTDUnrecognizedParameters;
			return internalScan();

		case DTDAfterNotationPublicId:
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			state = ScannerState.DTDAfterNotationName;
			url = stream.advanceIfRegExp(URL_VALUE_REGEX); // scan the System Identifier URL
			if (!url.equals("")) {
				isDeclCompleted = true;
				return finishToken(offset, TokenType.DTDNotationSystemId);
			}

			state = ScannerState.DTDUnrecognizedParameters;
			return internalScan();

		default:
		}

		
		stream.advance(1);
		state = isInsideDTDContent ? ScannerState.DTDWithinContent : ScannerState.WithinContent;
		return finishToken(offset, TokenType.Unknown, errorMessage);
	}

	private String localize(String string, String string2) {
		return string;
	}

	public int getLastNonWhitespaceOffset() {
		return stream.getLastNonWhitespaceOffset();
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

	public static Scanner createScanner(String input, int initialOffset, boolean isDTDFile) {
		return createScanner(input, initialOffset,
				isDTDFile ? ScannerState.DTDWithinContent : ScannerState.WithinContent, isDTDFile);
	}

	public static Scanner createScanner(String input, int initialOffset, ScannerState initialState) {
		return new XMLScanner(input, initialOffset, initialState, false);
	}

	public static Scanner createScanner(String input, int initialOffset, ScannerState initialState, boolean isDTDFile) {
		return new XMLScanner(input, initialOffset, initialState, isDTDFile);
	}

}
