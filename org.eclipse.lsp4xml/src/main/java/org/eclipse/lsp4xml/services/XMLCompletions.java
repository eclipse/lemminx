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
package org.eclipse.lsp4xml.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4xml.extensions.ICompletionParticipant;
import org.eclipse.lsp4xml.extensions.ICompletionRequest;
import org.eclipse.lsp4xml.extensions.ICompletionResponse;
import org.eclipse.lsp4xml.internal.parser.BadLocationException;
import org.eclipse.lsp4xml.internal.parser.Scanner;
import org.eclipse.lsp4xml.internal.parser.TokenType;
import org.eclipse.lsp4xml.internal.parser.XMLScanner;
import org.eclipse.lsp4xml.model.Node;
import org.eclipse.lsp4xml.model.XMLDocument;
import org.eclipse.lsp4xml.utils.XMLLogger;

/**
 * XML completions support.
 *
 */
class XMLCompletions {
	private static final XMLLogger logger = new XMLLogger(XMLCompletions.class.getName());

	private final XMLExtensionsRegistry extensionsRegistry;
	private CompletionRequest completionRequest;
	private static final String cdata = "![CDATA[]]"; 

	public XMLCompletions(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public CompletionList doComplete(XMLDocument xmlDocument, Position position, FormattingOptions settings) {
		completionRequest = null;
		
		try {
			completionRequest = new CompletionRequest(xmlDocument, position, settings);
		} catch (BadLocationException e) {
			logger.logCatch(e);
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
					return collectOpenTagSuggestions(scanner.getTokenOffset(), scanner.getTokenEnd(),scanner.getTokenText());
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
						return collectAutoCloseTagSuggestion(scanner.getTokenEnd(), currentTag, completionRequest,
								completionResponse);
					}
				}
				break;
			case EndTagClose:
				if (offset <= scanner.getTokenEnd()) {
					String currentTag = completionRequest.getCurrentTag();
					if (currentTag.length() > 0) {
						collectInsideContent(completionRequest, completionResponse);
						return completionResponse;
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
		Node n;
		try {
			XMLDocument document = request.getXMLDocument();
			pos = document.positionAt(tagCloseEnd);
			n = document.findNodeAt(tagCloseEnd);
		} catch (BadLocationException e) {
			logger.logCatch(e);
			return response;
		}
		// if(n.closed == true) {
		// 	return response;
		// }
		CompletionItem item = new CompletionItem();
		item.setLabel("</" + tag + ">");
		item.setKind(CompletionItemKind.Property);
		item.setFilterText("</" + tag + ">");
		item.setTextEdit(new TextEdit(new Range(pos, pos), "$0</" + tag + ">")); //"\n\t$0\n</" + tag + ">"
		item.setInsertTextFormat(InsertTextFormat.Snippet);
		response.addCompletionItem(item);
		return response;
	}

	private boolean isEmptyElement(String tag) {
		// TODO Auto-generated method stub
		return false;
	}

	private CompletionList collectCloseTagSuggestions(int afterOpenBracket, boolean b, int endOffset) {
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
				logger.logCatch(e);
			}
			valuePrefix = offset >= valueContentStart && offset <= valueContentEnd
					? text.substring(valueContentStart, offset)
					: "";
			addQuotes = false;
		} else {
			try {
				range = getReplaceRange(valueStart, valueEnd, completionRequest);
			} catch (BadLocationException e) {
				logger.logCatch(e);
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
				logger.logCatch(e);
			}
		}
		return completionResponse;
	}

	//Tags that look like '<{CHARS}'
	private CompletionList collectOpenTagSuggestions(int tokenOffset, int tokenEnd, String tag) {
		XMLDocument xmlDocument = completionRequest.getXMLDocument();
		if(xmlDocument.children.size() > 0){
			Position start;
			Position end;
			try {
				start = xmlDocument.positionAt(tokenOffset);
				end = xmlDocument.positionAt(tokenEnd);	
				
			} catch (Exception e) {
				logger.logCatch(e);
				return new CompletionList();
			}
			List<CompletionItem> list= new ArrayList<CompletionItem>();
			Node node = xmlDocument;
			Range range = new Range(start,end);
			if(cdata.regionMatches(0, tag, 0, tag.length())){
				//TODO: setCDATACompletionItem(list,range);
			}
			else{

				collectSimilarTags(list, node, tag, range);
			}
			CompletionList p = new CompletionList(list);
			return p;
		}
		return new CompletionList();
	}

	//Tags that look like '<'
	private CompletionList collectTagSuggestions(int offset, int endPos) {
		XMLDocument xmlDocument = completionRequest.getXMLDocument();
		List<CompletionItem> list= new ArrayList<CompletionItem>();
		if(xmlDocument.children.size() > 0){
			Position start;
			Position end;
			try {
				start = xmlDocument.positionAt(offset);
				end = xmlDocument.positionAt(endPos);	
			} catch (Exception e) {
				logger.logCatch(e);
				return new CompletionList();
			}
			Range range = new Range(start,end);
			collectAllTags(list, xmlDocument.children.get(0), range, xmlDocument);
			CompletionList p = new CompletionList(list);
			
			return p;
		}
		return new CompletionList();
		
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

	private void collectAllTags(List<CompletionItem> list, Node node, Range range, XMLDocument xmlDocument) {
		if(node.tag == null) {
			return;
		}
		CompletionItem item = createCompletionItem(node.tag, range);
		if(!list.contains(item)) {
			list.add(item);
		}
		for(int i = 0; i < node.children.size(); i++) {
			Node child = node.children.get(i);
			collectAllTags(list, child, range, xmlDocument);
		}
	}

	private void collectSimilarTags(List<CompletionItem> list, Node node, String tag, Range range) {
		int len = tag.length();
		if(node.tag != null) {
			if(node.tag.regionMatches(0, tag, 0, len)){
				CompletionItem item = createCompletionItem(node.tag, range);
				if(!list.contains(item)) {
					list.add(item);
				}
			}
		}
		for(int i = 0; i < node.children.size(); i++) {
			Node child = node.children.get(i);
			collectSimilarTags(list, child, tag, range);
		}
		
	}

	private void setCDATACompletionItem(List<CompletionItem> list, Range range) {
		CompletionItem item = new CompletionItem();
		item.setLabel("<![CDATA[]]>");
		item.setKind(CompletionItemKind.Property);
		item.setFilterText("![CDATA[]]");
		item.setTextEdit(new TextEdit(range, "![CDATA[$0]]>"));
		item.setInsertTextFormat(InsertTextFormat.Snippet);
		list.add(item);

	}

	private CompletionItem createCompletionItem(String tag, Range range) {
		CompletionItem item = new CompletionItem();
		item.setLabel("<" + tag + ">");
		item.setKind(CompletionItemKind.Property);
		item.setFilterText(tag);
		item.setTextEdit(new TextEdit(range, tag + ">$0</" + tag + ">"));
		item.setInsertTextFormat(InsertTextFormat.Snippet);
		return item;
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
