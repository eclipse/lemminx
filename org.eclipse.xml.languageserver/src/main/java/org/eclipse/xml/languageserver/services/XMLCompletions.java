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
package org.eclipse.xml.languageserver.services;

import java.util.Collection;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.xml.languageserver.extensions.ICompletionParticipant;
import org.eclipse.xml.languageserver.extensions.ICompletionRequest;
import org.eclipse.xml.languageserver.extensions.ICompletionResponse;
import org.eclipse.xml.languageserver.internal.parser.BadLocationException;
import org.eclipse.xml.languageserver.internal.parser.Scanner;
import org.eclipse.xml.languageserver.internal.parser.TokenType;
import org.eclipse.xml.languageserver.internal.parser.XMLScanner;
import org.eclipse.xml.languageserver.model.Node;
import org.eclipse.xml.languageserver.model.XMLDocument;

/**
 * XML completions support.
 *
 */
class XMLCompletions {

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLCompletions(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public CompletionList doComplete(XMLDocument xmlDocument, Position position, FormattingOptions settings) {
		CompletionRequest completionRequest = null;
		try {
			completionRequest = new CompletionRequest(xmlDocument, position, settings);
		} catch (BadLocationException e) {
			return null;
		}
		CompletionResponse completionResponse = new CompletionResponse();
		int offset = completionRequest.getOffset();
		Node node = completionRequest.getNode();

		String text = xmlDocument.getText();
		Scanner scanner = XMLScanner.createScanner(text, node.start);
		completionRequest.setCurrentTag("");
		completionRequest.setCurrentAttributeName(null);

		TokenType token = scanner.scan();
		while (token != TokenType.EOS && scanner.getTokenOffset() <= offset) {
			switch (token) {
			case StartTagOpen:
				if (scanner.getTokenEnd() == offset) {
					int endPos = scanNextForEndPos(offset, scanner, TokenType.StartTag);
					return collectTagSuggestions(offset, endPos);
				}
				break;
			case StartTag:
				if (scanner.getTokenOffset() <= offset && offset <= scanner.getTokenEnd()) {
					return collectOpenTagSuggestions(scanner.getTokenOffset(), scanner.getTokenEnd());
				}
				completionRequest.setCurrentTag(scanner.getTokenText());
				break;
			case AttributeName:
				if (scanner.getTokenOffset() <= offset && offset <= scanner.getTokenEnd()) {
					return collectAttributeNameSuggestions(scanner.getTokenOffset(), scanner.getTokenEnd());
				}
				completionRequest.setCurrentAttributeName(scanner.getTokenText());
				break;
			case DelimiterAssign:
				if (scanner.getTokenEnd() == offset) {
					int endPos = scanNextForEndPos(offset, scanner, TokenType.AttributeValue);
					return collectAttributeValueSuggestions(offset, endPos, completionRequest, completionResponse);
				}
				break;
			case AttributeValue:
				if (scanner.getTokenOffset() <= offset && offset <= scanner.getTokenEnd()) {
					return collectAttributeValueSuggestions(scanner.getTokenOffset(), scanner.getTokenEnd(),
							completionRequest, completionResponse);
				}
				break;
			case Whitespace:
				if (offset <= scanner.getTokenEnd()) {
					switch (scanner.getScannerState()) {
					case AfterOpeningStartTag:
						int startPos = scanner.getTokenOffset();
						int endTagPos = scanNextForEndPos(offset, scanner, TokenType.StartTag);
						return collectTagSuggestions(startPos, endTagPos);
					case WithinTag:
					case AfterAttributeName:
						return collectAttributeNameSuggestions(scanner.getTokenEnd());
					case BeforeAttributeValue:
						return collectAttributeValueSuggestions(scanner.getTokenEnd(), offset, completionRequest,
								completionResponse);
					case AfterOpeningEndTag:
						return collectCloseTagSuggestions(scanner.getTokenOffset() - 1, false);
					case WithinContent:
						collectInsideContent(completionRequest, completionResponse);
						return completionResponse;
					}
				}
				break;
			case EndTagOpen:
				if (offset <= scanner.getTokenEnd()) {
					int afterOpenBracket = scanner.getTokenOffset() + 1;
					int endOffset = scanNextForEndPos(offset, scanner, TokenType.EndTag);
					return collectCloseTagSuggestions(afterOpenBracket, false, endOffset);
				}
				break;
			case EndTag:
				if (offset <= scanner.getTokenEnd()) {
					int start = scanner.getTokenOffset() - 1;
					while (start >= 0) {
						char ch = text.charAt(start);
						if (ch == '/') {
							return collectCloseTagSuggestions(start, false, scanner.getTokenEnd());
						} else if (!isWhiteSpace(ch)) {
							break;
						}
						start--;
					}
				}
				break;
			case StartTagClose:
				if (offset <= scanner.getTokenEnd()) {
					String currentTag = completionRequest.getCurrentTag();
					if (currentTag.length() > 0) {
						collectAutoCloseTagSuggestion(scanner.getTokenEnd(), currentTag, completionRequest,
								completionResponse);
					}
				}
				break;
			case EndTagClose:
				if (offset <= scanner.getTokenEnd()) {
					String currentTag = completionRequest.getCurrentTag();
					if (currentTag.length() > 0) {
						collectInsideContent(completionRequest, completionResponse);
					}
				}
				break;
			case Content:
				if (offset <= scanner.getTokenEnd()) {
					collectInsideContent(completionRequest, completionResponse);
					return completionResponse;
				}
				break;
			default:
				if (offset <= scanner.getTokenEnd()) {
					return completionResponse;
				}
				break;
			}
			token = scanner.scan();
		}

		return completionResponse;
	}

	private CompletionList collectAutoCloseTagSuggestion(int tagCloseEnd, String tag, CompletionRequest request,
			CompletionResponse response) {

		Position pos;
		try {
			XMLDocument document = request.getXMLDocument();
			pos = document.positionAt(tagCloseEnd);
		} catch (BadLocationException e) {
			return response;
		}
		CompletionItem item = new CompletionItem();
		item.setLabel("</" + tag + ">");
		item.setKind(CompletionItemKind.Property);
		item.setFilterText("</" + tag + ">");
		item.setTextEdit(new TextEdit(new Range(pos, pos), "$0</" + tag + ">"));
		item.setInsertTextFormat(InsertTextFormat.Snippet);
		response.addCompletionItem(item);
		return response;
	}

	private boolean isEmptyElement(String tag) {
		// TODO Auto-generated method stub
		return false;
	}

	private CompletionList collectCloseTagSuggestions(int afterOpenBracket, boolean b, int endOffset) {
		// TODO Auto-generated method stub
		return null;
	}

	private void collectInsideContent(ICompletionRequest request, ICompletionResponse response) {
		for (ICompletionParticipant participant : extensionsRegistry.getCompletionParticipants()) {
			participant.onXMLContent(request, response);
		}
		collectCharacterEntityProposals(request, response);
	}

	private void collectCharacterEntityProposals(ICompletionRequest request, ICompletionResponse response) {
		// character entities
		/*
		 * int offset = request.getOffset(); Position position = request.getPosition();
		 * int k = offset - 1; int characterStart = position.getCharacter(); char ch =
		 * request.getDocument().getText().charAt(characterStart); while (k >= 0 &&
		 * (Character.isLetter(ch) || Character.isDigit(ch))) { k--; characterStart--; }
		 * if (k >= 0 && text[k] == '&') { let range =
		 * Range.create(Position.create(position.line, characterStart - 1), position);
		 * for (String entity : entities) { if (endsWith(entity, ';')) { String label =
		 * '&' + entity; result.items.push({ label, kind: CompletionItemKind.Keyword,
		 * documentation: localize('entity.propose', `Character entity representing
		 * '${entities[entity]}'`), textEdit: TextEdit.replace(range, label),
		 * insertTextFormat: InsertTextFormat.PlainText }); } } }
		 */
	}

	private CompletionList collectCloseTagSuggestions(int i, boolean b) {
		// TODO Auto-generated method stub
		return null;
	}

	private CompletionList collectAttributeNameSuggestions(int tokenEnd) {
		// TODO Auto-generated method stub
		return null;
	}

	private CompletionList collectAttributeNameSuggestions(int tokenOffset, int tokenEnd) {
		// TODO Auto-generated method stub
		return null;
	}

	private CompletionList collectAttributeValueSuggestions(int valueStart, int valueEnd,
			CompletionRequest completionRequest, CompletionResponse completionResponse) {
		Range range = null;
		boolean addQuotes = false;
		String valuePrefix;
		int offset = completionRequest.getOffset();
		String text = completionRequest.getXMLDocument().getText();
		if (offset > valueStart && offset <= valueEnd && isQuote(text.charAt(valueStart))) {
			// inside quoted attribute
			int valueContentStart = valueStart + 1;
			int valueContentEnd = valueEnd;
			// valueEnd points to the char after quote, which encloses the replace range
			if (valueEnd > valueStart && text.charAt(valueEnd - 1) == text.charAt(valueStart)) {
				valueContentEnd--;
			}
			int wsBefore = getWordStart(text, offset, valueContentStart);
			int wsAfter = getWordEnd(text, offset, valueContentEnd);
			try {
				range = getReplaceRange(wsBefore, wsAfter, completionRequest);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			valuePrefix = offset >= valueContentStart && offset <= valueContentEnd
					? text.substring(valueContentStart, offset)
					: "";
			addQuotes = false;
		} else {
			try {
				range = getReplaceRange(valueStart, valueEnd, completionRequest);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			valuePrefix = text.substring(valueStart, offset);
			addQuotes = true;
		}

		Collection<ICompletionParticipant> completionParticipants = extensionsRegistry.getCompletionParticipants();
		if (completionParticipants.size() > 0) {
			try {
				Range fullRange = getReplaceRange(valueStart, valueEnd, completionRequest);
				for (ICompletionParticipant participant : completionParticipants) {
					participant.onAttributeValue(valuePrefix, fullRange, completionRequest, completionResponse);
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		return completionResponse;
	}

	private CompletionList collectOpenTagSuggestions(int tokenOffset, int tokenEnd) {
		// TODO Auto-generated method stub
		return null;
	}

	private CompletionList collectTagSuggestions(int offset, int endPos) {
		// TODO Auto-generated method stub
		return null;
	}

	private static int scanNextForEndPos(int offset, Scanner scanner, TokenType nextToken) {
		if (offset == scanner.getTokenEnd()) {
			TokenType token = scanner.scan();
			if (token == nextToken && scanner.getTokenOffset() == offset) {
				return scanner.getTokenEnd();
			}
		}
		return offset;
	}

	private static boolean isQuote(char c) {
		return c == '\'' || c == '"';
	}

	private static boolean isWhiteSpace(char ch) {
		return ch == ' ';
	}

	private static int getWordStart(String s, int offset, int limit) {
		while (offset > limit && !isWhiteSpace(s.charAt(offset - 1))) {
			offset--;
		}
		return offset;
	}

	private static int getWordEnd(String s, int offset, int limit) {
		while (offset < limit && !isWhiteSpace(s.charAt(offset))) {
			offset++;
		}
		return offset;
	}

	private static Range getReplaceRange(int replaceStart, int replaceEnd, ICompletionRequest context)
			throws BadLocationException {
		int offset = context.getOffset();
		if (replaceStart > offset) {
			replaceStart = offset;
		}
		XMLDocument document = context.getXMLDocument();
		return new Range(document.positionAt(replaceStart), document.positionAt(replaceEnd));
	}

}
