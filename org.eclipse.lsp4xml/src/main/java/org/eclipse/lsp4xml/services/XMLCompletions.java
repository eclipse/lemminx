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
package org.eclipse.lsp4xml.services;

import static java.lang.Character.isWhitespace;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.customservice.AutoCloseTagResponse;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.dom.parser.Scanner;
import org.eclipse.lsp4xml.dom.parser.ScannerState;
import org.eclipse.lsp4xml.dom.parser.TokenType;
import org.eclipse.lsp4xml.dom.parser.XMLScanner;
import org.eclipse.lsp4xml.extensions.prolog.PrologModel;
import org.eclipse.lsp4xml.services.extensions.CompletionSettings;
import org.eclipse.lsp4xml.services.extensions.ICompletionParticipant;
import org.eclipse.lsp4xml.services.extensions.ICompletionRequest;
import org.eclipse.lsp4xml.services.extensions.ICompletionResponse;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.settings.SharedSettings;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;
import org.eclipse.lsp4xml.utils.StringUtils;

/**
 * XML completions support.
 *
 */
public class XMLCompletions {

	private static final Logger LOGGER = Logger.getLogger(XMLCompletions.class.getName());
	private static final Pattern regionCompletionRegExpr = Pattern.compile("^(\\s*)(<(!(-(-\\s*(#\\w*)?)?)?)?)?$");

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLCompletions(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public CompletionList doComplete(DOMDocument xmlDocument, Position position,
			SharedSettings settings) {
		CompletionResponse completionResponse = new CompletionResponse();
		CompletionRequest completionRequest = null;
		try {
			completionRequest = new CompletionRequest(xmlDocument, position, settings, extensionsRegistry);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Creation of CompletionRequest failed", e);
			return completionResponse;
		}

		int offset = completionRequest.getOffset();
		DOMNode node = completionRequest.getNode();

		String text = xmlDocument.getText();
		if (text.isEmpty()) {
			// When XML document is empty, try to collect root element (from file
			// association)
			collectInsideContent(completionRequest, completionResponse);
			return completionResponse;
		}

		Scanner scanner = XMLScanner.createScanner(text, node.getStart(), isInsideDTDContent(node, xmlDocument));
		String currentTag = "";
		completionRequest.setCurrentAttributeName(null);
		TokenType token = scanner.scan();
		while (token != TokenType.EOS && scanner.getTokenOffset() <= offset) {
			switch (token) {
			case StartTagOpen:
				if (scanner.getTokenEnd() == offset) {
					int endPos = scanNextForEndPos(offset, scanner, TokenType.StartTag);
					collectTagSuggestions(offset, endPos, completionRequest, completionResponse);
					return completionResponse;
				}
				break;
			case StartTag:
				if (scanner.getTokenOffset() <= offset && offset <= scanner.getTokenEnd()) {
					collectOpenTagSuggestions(scanner.getTokenOffset(), scanner.getTokenEnd(), completionRequest,
							completionResponse);
					return completionResponse;
				}
				currentTag = scanner.getTokenText();
				break;
			case AttributeName:
				if (scanner.getTokenOffset() <= offset && offset <= scanner.getTokenEnd()) {
					collectAttributeNameSuggestions(scanner.getTokenOffset(), scanner.getTokenEnd(), completionRequest,
							completionResponse, settings);
					return completionResponse;
				}
				completionRequest.setCurrentAttributeName(scanner.getTokenText());
				break;
			case DelimiterAssign:
				if (scanner.getTokenEnd() == offset) {
					//int endPos = scanNextForEndPos(offset, scanner, TokenType.AttributeValue);
					collectAttributeValueSuggestions(offset, offset, completionRequest, completionResponse, settings);
					return completionResponse;
				}
				break;
			case AttributeValue:
				if (scanner.getTokenOffset() <= offset && offset <= scanner.getTokenEnd()) {
					collectAttributeValueSuggestions(scanner.getTokenOffset(), scanner.getTokenEnd(), completionRequest,
							completionResponse, settings);
					return completionResponse;
				}
				break;
			case Whitespace:
				if (offset <= scanner.getTokenEnd()) {
					switch (scanner.getScannerState()) {
					case AfterOpeningStartTag:
						int startPos = scanner.getTokenOffset();
						int endTagPos = scanNextForEndPos(offset, scanner, TokenType.StartTag);
						collectTagSuggestions(startPos, endTagPos, completionRequest, completionResponse);
						return completionResponse;
					case WithinTag:
					case AfterAttributeName:
						collectAttributeNameSuggestions(scanner.getTokenEnd(), completionRequest, completionResponse, settings);
						return completionResponse;
					case BeforeAttributeValue:
						collectAttributeValueSuggestions(scanner.getTokenEnd(), offset, completionRequest,
								completionResponse, settings);
						return completionResponse;
					case AfterOpeningEndTag:
						collectCloseTagSuggestions(scanner.getTokenOffset() - 1, false, offset, completionRequest,
								completionResponse);
						return completionResponse;
					case WithinContent:
						collectInsideContent(completionRequest, completionResponse);
						return completionResponse;
					default:
					}
				}
				break;
			case EndTagOpen:
				if (offset <= scanner.getTokenEnd()) {
					int afterOpenBracket = scanner.getTokenOffset() + 1;
					int endOffset = scanNextForEndPos(offset, scanner, TokenType.EndTag);
					collectCloseTagSuggestions(afterOpenBracket, false, endOffset, completionRequest,
							completionResponse);
					return completionResponse;
				}
				break;
			case EndTag:
				if (offset <= scanner.getTokenEnd()) {
					int start = scanner.getTokenOffset() - 1;
					while (start >= 0) {
						char ch = text.charAt(start);
						if (ch == '/') {
							collectCloseTagSuggestions(start, false, scanner.getTokenEnd(), completionRequest,
									completionResponse);
							return completionResponse;
						} else if (!isWhitespace(ch)) {
							break;
						}
						start--;
					}
				}
				break;
			case StartTagClose:
				if (offset <= scanner.getTokenEnd()) {
					if (currentTag != null && currentTag.length() > 0) {
						collectInsideContent(completionRequest, completionResponse);
						return completionResponse;
					}
				}
				break;
			case StartTagSelfClose:
				if (offset <= scanner.getTokenEnd()) {
					if (currentTag != null && currentTag.length() > 0 && xmlDocument.getText().charAt(offset - 1) == '>') { // if the actual character typed was '>'
						collectInsideContent(completionRequest, completionResponse);
						return completionResponse;
					}
				}
				break;
			case EndTagClose:
				if (offset <= scanner.getTokenEnd()) {
					if (currentTag != null && currentTag.length() > 0) {
						collectInsideContent(completionRequest, completionResponse);
						return completionResponse;
					}
				}
				break;
			case Content:
				if(completionRequest.getXMLDocument().isDTD() || completionRequest.getXMLDocument().isWithinInternalDTD(offset)) {
					if (scanner.getTokenOffset() <= offset) {
						collectInsideDTDContent(completionRequest, completionResponse, true);
						return completionResponse;
					}
					break;
				}
				if (offset <= scanner.getTokenEnd()) {
					collectInsideContent(completionRequest, completionResponse);
					return completionResponse;
				}
				break;
			case StartPrologOrPI: {
				try {
					boolean isFirstNode = xmlDocument.positionAt(scanner.getTokenOffset()).getLine() == 0;
					if (isFirstNode && offset <= scanner.getTokenEnd()) {
						collectPrologSuggestion(scanner.getTokenEnd(), "", completionRequest, completionResponse, settings);
						return completionResponse;
					}
				} catch (BadLocationException e) {
					LOGGER.log(Level.SEVERE, "In XMLCompletions, StartPrologOrPI position error", e);
				}
				break;
			}
			case PIName: {
				try {
					boolean isFirstNode = xmlDocument.positionAt(scanner.getTokenOffset()).getLine() == 0;
					if (isFirstNode && offset <= scanner.getTokenEnd()) {
						String substringXML = "xml".substring(0, scanner.getTokenText().length());
						if (scanner.getTokenText().equals(substringXML)) {
							PrologModel.computePrologCompletionResponses(scanner.getTokenEnd(), scanner.getTokenText(), completionRequest,
									completionResponse, true, settings);
							return completionResponse;
						}
					}
				} catch (BadLocationException e) {
					LOGGER.log(Level.SEVERE, "In XMLCompletions, StartPrologOrPI position error", e);
				}
				break;
			}
			case PrologName: {
				try {
					boolean isFirstNode = xmlDocument.positionAt(scanner.getTokenOffset()).getLine() == 0;
					if (isFirstNode && offset <= scanner.getTokenEnd()) {
						collectPrologSuggestion(scanner.getTokenEnd(), scanner.getTokenText(), completionRequest,
								completionResponse, settings);
						return completionResponse;
					}
				} catch (BadLocationException e) {
					LOGGER.log(Level.SEVERE, "In XMLCompletions, PrologName position error", e);
				}
				break;
			}
			// DTD
			case DTDAttlistAttributeName:
			case DTDAttlistAttributeType:
			case DTDAttlistAttributeValue:
			case DTDStartAttlist:
			case DTDStartElement:
			case DTDStartEntity:
			case DTDEndTag:
			case DTDStartInternalSubset:
			case DTDEndInternalSubset: {
				if (scanner.getTokenOffset() <= offset) {
					collectInsideDTDContent(completionRequest, completionResponse);
					return completionResponse;
				}
				break;
			}

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

	/**
	 * Returns true if completion was triggered inside DTD content (internal or
	 * external DTD) and false otherwise.
	 * 
	 * @param node
	 * @param xmlDocument
	 * @return true if completion was triggered inside DTD content (internal or
	 *         external DTD) and false otherwise.
	 */
	private static boolean isInsideDTDContent(DOMNode node, DOMDocument xmlDocument) {
		if (xmlDocument.isDTD()) {
			// external DTD
			return true;
		}
		// check if node belongs to internal DTD (<!ELEMENT, ....)
		return (node.getParentNode() != null && node.getParentNode().isDoctype());
	}

	public boolean isBalanced(DOMNode node) {
		if(node.isClosed() == false) {
			return false;
		}
		String name = node.getNodeName();
		DOMNode parent = node.getParentElement();
		while(parent != null && name.equals(parent.getNodeName())) {
			if(parent.isClosed() == false) {
				return false;
			}
			parent = parent.getParentElement();
		}
		return true;
	}

	public AutoCloseTagResponse doTagComplete(DOMDocument xmlDocument, Position position) {
		int offset;
		try {
			offset = xmlDocument.offsetAt(position);
			if(offset - 2 < 0) { //There is not enough content for autoClose
				return null;
			}
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "doTagComplete failed", e);
			return null;
		}
		if (offset <= 0) {
			return null;
		}
		char c = xmlDocument.getText().charAt(offset - 1);
		char cBefore = xmlDocument.getText().charAt(offset - 2);
		String snippet = null;
		if (c == '>') { // Case: <a>|
			DOMNode node = xmlDocument.findNodeBefore(offset);
			if(!(node instanceof DOMElement)) {
				return null;
			}
			DOMElement element = ((DOMElement) node);
			if (node != null 
					&& node.isElement() 
					&& !element.isSelfClosed() 
					&& element.getTagName() != null
					&& !isEmptyElement(((DOMElement) node).getTagName()) 
					&& node.getStart() < offset
					&& (!element.hasEndTag() || 
						(element.getTagName().equals(node.getParentNode().getNodeName()) && !isBalanced(node)))) {
				snippet = "$0</" + ((DOMElement) node).getTagName() + ">";
				
			}
		} else if (cBefore == '<' && c == '/') { // Case: <a> </|
			DOMNode node = xmlDocument.findNodeBefore(offset);
			while (node != null && node.isClosed()) {
				node = node.getParentNode();
			}
			if (node != null && node.isElement() && ((DOMElement) node).getTagName() != null) {
				snippet = ((DOMElement) node).getTagName() + ">$0";
			}
		} else {
			DOMNode node = xmlDocument.findNodeBefore(offset);
			if(node.isElement() && node.getNodeName() != null) {
				DOMElement element1 = (DOMElement) node;
				
				Integer slashOffset = element1.endsWith('/', offset);
				Position end = null;
				if(!element1.isInEndTag(offset) && slashOffset != null) { //The typed characted was '/'
					List<DOMAttr> attrList = element1.getAttributeNodes();
					if(attrList != null) {
						DOMAttr lastAttr = attrList.get(attrList.size() - 1);
						if(slashOffset < lastAttr.getEnd()) { //slash in attribute value
							return null;
						}
					}
					String text = xmlDocument.getText();
					boolean closeBracketAfterSlash = offset < text.length() ? text.charAt(offset) == '>' : false; // After the slash is a close bracket
					
					// Case: <a/| ...
					if(closeBracketAfterSlash == false) { // no '>' after slash
						if(element1.isStartTagClosed()) { // tag has closing '>', but slash is in incorrect area (not directly before the '>')
							return null;
						}
						snippet = ">$0";
						if(element1.hasEndTag()) { // Case: <a/| </a>
							try {
								end = xmlDocument.positionAt(element1.getEnd());
							} catch (BadLocationException e) {
								return null;
							}
						}
					} 
					else {
						DOMNode nextSibling = node.getNextSibling();
						//If there is text in between the tags it will skip this
						if(nextSibling != null && nextSibling.isElement()){ // Case: <a/|></a>
							DOMElement element2 = (DOMElement) nextSibling;
							if(!element2.hasStartTag() && node.getNodeName().equals(element2.getNodeName())) {
								try {
									snippet = ">$0";
									end = xmlDocument.positionAt(element2.getEnd());
								} catch (BadLocationException e) {
									return null;
								}
							}
						}
						else if(nextSibling == null) { // Case: <a> <a/|> </a> </a>
							DOMElement parentElement = node.getParentElement();
							if(parentElement != null && node.getNodeName().equals(parentElement.getTagName())) {
								DOMNode nodeAfterParent = parentElement.getNextSibling();
								if(nodeAfterParent != null && nodeAfterParent.isElement()) {
									DOMElement elementAfterParent = (DOMElement) nodeAfterParent;
									if(parentElement.getTagName().equals(elementAfterParent.getTagName())
										 && !elementAfterParent.hasStartTag()) {
										try {
											snippet = ">$0";
											end = xmlDocument.positionAt(parentElement.getEnd());
										} catch (BadLocationException e) {
											return null;
										}	
									}
								}
							}
						}
					}
					if(snippet != null && end != null) {
						return new AutoCloseTagResponse(snippet, new Range(position, end));
					}
				}
			}
		}
		if(snippet == null) {
			return null;
		}
		return new AutoCloseTagResponse(snippet);
	}

	// ---------------- Tags completion

	// Tags that look like '<'
	private void collectTagSuggestions(int tagStart, int tagEnd, CompletionRequest completionRequest,
			CompletionResponse completionResponse) {
		collectOpenTagSuggestions(tagStart, tagEnd, completionRequest, completionResponse);
		collectCloseTagSuggestions(tagStart, true, tagEnd, completionRequest, completionResponse);
	}

	private void collectOpenTagSuggestions(int afterOpenBracket, int tagNameEnd, CompletionRequest completionRequest,
			CompletionResponse completionResponse) {
		try {
			Range replaceRange = getReplaceRange(afterOpenBracket - 1, tagNameEnd, completionRequest);
			collectOpenTagSuggestions(true, replaceRange, completionRequest, completionResponse);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "While performing Completions the provided offset was a BadLocation", e);
			return;
		}
	}

	private void collectOpenTagSuggestions(boolean hasOpenBracket, Range replaceRange,
			CompletionRequest completionRequest, CompletionResponse completionResponse) {
		try {
			DOMDocument document = completionRequest.getXMLDocument();
			String text = document.getText();
			int tagNameEnd = document.offsetAt(replaceRange.getEnd());
			int newOffset = getOffsetFollowedBy(text, tagNameEnd, ScannerState.WithinEndTag, TokenType.EndTagClose);
			if (newOffset != -1) {
				newOffset++;
				replaceRange.setEnd(document.positionAt(newOffset));
			}

		} catch (BadLocationException e) {
			// do nothing
		}
		completionRequest.setHasOpenBracket(hasOpenBracket);
		completionRequest.setReplaceRange(replaceRange);
		for (ICompletionParticipant participant : getCompletionParticipants()) {
			try {
				participant.onTagOpen(completionRequest, completionResponse);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "While performing ICompletionParticipant#onTagOpen", e);
			}
		}
		DOMElement parentNode = completionRequest.getParentElement();
		if (parentNode != null && !completionResponse.hasSomeItemFromGrammar()) {
			// no grammar, collect similar tags from the parent node
			Set<String> seenElements = new HashSet<>();
			if (parentNode != null && parentNode.isElement() && parentNode.hasChildNodes()) {
				parentNode.getChildren().forEach(node -> {
					DOMElement element = node.isElement() ? (DOMElement) node : null;
					if (element == null || element.getTagName() == null
							|| seenElements.contains(element.getTagName())) {
						return;
					}
					String tag = element.getTagName();
					seenElements.add(tag);
					CompletionItem item = new CompletionItem();
					item.setLabel(tag);
					item.setKind(CompletionItemKind.Property);
					item.setFilterText(completionRequest.getFilterForStartTagName(tag));
					StringBuilder xml = new StringBuilder();
					xml.append("<");
					xml.append(tag);
					if (element.isSelfClosed()) {
						xml.append(" />");
					} else {
						xml.append(">");
						CompletionSettings completionSettings = completionRequest.getCompletionSettings();

						if (completionSettings.isCompletionSnippetsSupported()) {
							xml.append("$0");
						}
						if (completionSettings.isAutoCloseTags()) {
							xml.append("</").append(tag).append(">");
						}
					}
					item.setTextEdit(new TextEdit(replaceRange, xml.toString()));
					item.setInsertTextFormat(InsertTextFormat.Snippet);

					completionResponse.addCompletionItem(item);
				});
			}
		}
	}

	/**
	 * Collect xml prolog completions.
	 * 
	 * @param startOffset
	 * @param tag
	 * @param request
	 * @param response
	 */
	private void collectPrologSuggestion(int startOffset, String tag, CompletionRequest request,
			CompletionResponse response, SharedSettings settings) {
		PrologModel.computePrologCompletionResponses(startOffset, tag, request, response, false, settings);
	}

	private void collectCloseTagSuggestions(int afterOpenBracket, boolean inOpenTag, int tagNameEnd,
			CompletionRequest completionRequest, CompletionResponse completionResponse) {
		try {
			Range range = getReplaceRange(afterOpenBracket, tagNameEnd, completionRequest);
			String text = completionRequest.getXMLDocument().getText();
			boolean hasCloseTag = isFollowedBy(text, tagNameEnd, ScannerState.WithinEndTag, TokenType.EndTagClose);
			collectCloseTagSuggestions(range, false, !hasCloseTag, inOpenTag, completionRequest, completionResponse);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "While performing Completions the provided offset was a BadLocation", e);
		}
	}

	private void collectCloseTagSuggestions(Range range, boolean openEndTag, boolean closeEndTag, boolean inOpenTag,
			CompletionRequest completionRequest, CompletionResponse completionResponse) {
		try {
			String text = completionRequest.getXMLDocument().getText();
			DOMNode curr = completionRequest.getNode();
			if (inOpenTag) {
				curr = curr.getParentNode(); // don't suggest the own tag, it's not yet open
			}
			String closeTag = closeEndTag ? ">" : "";
			int afterOpenBracket = completionRequest.getXMLDocument().offsetAt(range.getStart());
			if (!openEndTag) {
				afterOpenBracket--;
			}
			int offset = completionRequest.getOffset();
			while (curr != null) {
				if (curr.isElement()) {
					DOMElement element = ((DOMElement) curr);
					String tag = element.getTagName();
					if (tag != null && (!element
							.isClosed() /* || element.hasEndTag() && (element.getEndTagOpenOffset() > offset) */)) {
						CompletionItem item = new CompletionItem();
						item.setLabel("End with '</" + tag + ">'");
						item.setKind(CompletionItemKind.Property);
						item.setInsertTextFormat(InsertTextFormat.PlainText);

						String startIndent = getLineIndent(element.getStart(), text);
						String endIndent = getLineIndent(afterOpenBracket, text);
						if (startIndent != null && endIndent != null && !startIndent.equals(endIndent)) {
							String insertText = startIndent + "</" + tag + closeTag;
							item.setTextEdit(new TextEdit(
									getReplaceRange(afterOpenBracket - endIndent.length(), offset, completionRequest),
									insertText));
							item.setFilterText(endIndent + "</" + tag + closeTag);
						} else {
							String openTag = openEndTag ? "<" : "";
							String insertText = openTag + "/" + tag + closeTag;
							item.setFilterText(insertText);
							item.setTextEdit(new TextEdit(range, insertText));
						}
						completionResponse.addCompletionItem(item);
					}
				}
				curr = curr.getParentNode();
			}
			if (inOpenTag) {
				return;
			}

		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "While performing Completions the provided offset was a BadLocation", e);
		}
	}

	private void collectInsideContent(CompletionRequest request, CompletionResponse response) {
		Range tagNameRange = request.getXMLDocument().getElementNameRangeAt(request.getOffset());
		if (tagNameRange != null) {
			collectOpenTagSuggestions(false, tagNameRange, request, response);
			collectCloseTagSuggestions(tagNameRange, true, true, false, request, response);
		}
		// Participant completion on XML content
		for (ICompletionParticipant participant : getCompletionParticipants()) {
			try {
				participant.onXMLContent(request, response);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "While performing ICompletionParticipant#onXMLContent", e);
			}
		}
		collectionRegionProposals(request, response);
		collectCharacterEntityProposals(request, response);
	}

	private void collectionRegionProposals(ICompletionRequest request, ICompletionResponse response) {
		// Completion for #region
		try {
			int offset = request.getOffset();
			TextDocument document = request.getXMLDocument().getTextDocument();
			Position pos = document.positionAt(offset);
			String lineText = document.lineText(pos.getLine());
			String lineUntilPos = lineText.substring(0, pos.getCharacter());
			Matcher match = regionCompletionRegExpr.matcher(lineUntilPos);
			if (match.find()) {
				Range range = new Range(new Position(pos.getLine(), pos.getCharacter() + match.regionStart()), pos);

				CompletionItem beginProposal = new CompletionItem("#region");
				beginProposal.setTextEdit(new TextEdit(range, "<!-- #region $1-->"));
				beginProposal.setDocumentation("Folding Region Start");
				beginProposal.setFilterText(match.group());
				beginProposal.setSortText("za");
				beginProposal.setKind(CompletionItemKind.Snippet);
				beginProposal.setInsertTextFormat(InsertTextFormat.Snippet);
				response.addCompletionAttribute(beginProposal);

				CompletionItem endProposal = new CompletionItem("#endregion");
				endProposal.setKind(CompletionItemKind.Snippet);
				endProposal.setTextEdit(new TextEdit(range, "<!-- #endregion-->"));
				endProposal.setDocumentation("Folding Region End");
				endProposal.setFilterText(match.group());
				endProposal.setSortText("zb");
				response.addCompletionAttribute(endProposal);
			}
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "While performing collectRegionCompletion", e);
		}
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

	private void collectAttributeNameSuggestions(int nameStart, CompletionRequest completionRequest,
			CompletionResponse completionResponse, SharedSettings settings) {
		collectAttributeNameSuggestions(nameStart, completionRequest.getOffset(), completionRequest,
				completionResponse, settings);
	}

	private void collectAttributeNameSuggestions(int nameStart, int nameEnd, CompletionRequest completionRequest,
			CompletionResponse completionResponse, SharedSettings settings) {
		int replaceEnd = completionRequest.getOffset();
		String text = completionRequest.getXMLDocument().getText();
		while (replaceEnd < nameEnd && text.charAt(replaceEnd) != '<' && text.charAt(replaceEnd) != '?') { // < is a valid attribute name character, but
																			// we rather assume the attribute name ends.
																			// See #23236.
			replaceEnd++;
		}
		try {
			Range range = getReplaceRange(nameStart, replaceEnd, completionRequest);
			boolean generateValue = !isFollowedBy(text, nameEnd, ScannerState.AfterAttributeName,
					TokenType.DelimiterAssign);
			for (ICompletionParticipant participant : getCompletionParticipants()) {
				participant.onAttributeName(generateValue, range, completionRequest, completionResponse, settings);
			}
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "While performing Completions, getReplaceRange() was given a bad Offset location",
					e);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "While performing ICompletionParticipant#onAttributeName", e);
		}
	}

	private void collectAttributeValueSuggestions(int valueStart, int valueEnd, CompletionRequest completionRequest,
			CompletionResponse completionResponse, SharedSettings settings) {
		Range range = null;
		boolean addQuotes = false;
		String valuePrefix;
		int offset = completionRequest.getOffset();
		String text = completionRequest.getXMLDocument().getText();
		
		// Adjusts range to handle if quotations for the value exist
		if (offset > valueStart && offset <= valueEnd && StringUtils.isQuote(text.charAt(valueStart))) {
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
				LOGGER.log(Level.SEVERE,
						"While performing Completions, getReplaceRange() was given a bad Offset location", e);
			}
			valuePrefix = offset >= valueContentStart && offset <= valueContentEnd
					? text.substring(valueContentStart, offset)
					: "";
			addQuotes = false;
		} else {
			try {
				range = getReplaceRange(valueStart, valueEnd, completionRequest);
			} catch (BadLocationException e) {
				LOGGER.log(Level.SEVERE,
						"While performing Completions, getReplaceRange() was given a bad Offset location", e);
			}
			valuePrefix = text.substring(valueStart, offset);
			addQuotes = true;
		}

		Collection<ICompletionParticipant> completionParticipants = getCompletionParticipants();
		if (completionParticipants.size() > 0) {
			try {
				Range fullRange = getReplaceRange(valueStart, valueEnd, completionRequest);
				for (ICompletionParticipant participant : completionParticipants) {
					participant.onAttributeValue(valuePrefix, fullRange, addQuotes, completionRequest,
							completionResponse, settings);
				}
			} catch (BadLocationException e) {
				LOGGER.log(Level.SEVERE,
						"While performing Completions, getReplaceRange() was given a bad Offset location", e);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "While performing ICompletionParticipant#onAttributeValue", e);
			}
		}
	}

	private void collectInsideDTDContent(CompletionRequest request, CompletionResponse response) {
		collectInsideDTDContent(request,response, false);
	}
	private void collectInsideDTDContent(CompletionRequest request, CompletionResponse response, boolean isContent) {
		// Insert DTD Element Declaration
		// see https://www.w3.org/TR/REC-xml/#dt-eldecl
		boolean isSnippetsSupported = request.getCompletionSettings().isCompletionSnippetsSupported();
		InsertTextFormat insertFormat = isSnippetsSupported ? InsertTextFormat.Snippet : InsertTextFormat.PlainText;
		CompletionItem elementDecl = new CompletionItem();
		elementDecl.setLabel("Insert DTD Element declaration");
		elementDecl.setKind(CompletionItemKind.EnumMember);
		elementDecl.setFilterText("<!ELEMENT ");
		elementDecl.setInsertTextFormat(insertFormat);
		int startOffset = request.getOffset();
		Range editRange = null;
		DOMDocument document = request.getXMLDocument();
		DOMNode node = document.findNodeAt(startOffset);
		try {
			if(node.isDoctype()) {
				editRange = getReplaceRange(startOffset, startOffset, request);
			} else {
				if(isContent) {
					editRange = document.getTrimmedRange(node.getStart(), node.getEnd());
				} 
				if(editRange == null) {
					editRange = getReplaceRange(node.getStart(), node.getEnd(), request);
				}
			}
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "While performing getReplaceRange for DTD completion.", e);
		}
		String textEdit = isSnippetsSupported ? "<!ELEMENT ${1:element-name} (${2:#PCDATA})>" : "<!ELEMENT element-name (#PCDATA)>";
		elementDecl.setTextEdit(new TextEdit(editRange, textEdit));
		elementDecl.setDocumentation("<!ELEMENT element-name (#PCDATA)>");
		
		response.addCompletionItem(elementDecl);

		// Insert DTD AttrList Declaration
		// see https://www.w3.org/TR/REC-xml/#attdecls
		CompletionItem attrListDecl = new CompletionItem();
		attrListDecl.setLabel("Insert DTD Attributes list declaration");
		attrListDecl.setKind(CompletionItemKind.EnumMember);
		attrListDecl.setFilterText("<!ATTLIST ");
		attrListDecl.setInsertTextFormat(insertFormat);
		startOffset = request.getOffset();
		
		textEdit = isSnippetsSupported ? "<!ATTLIST ${1:element-name} ${2:attribute-name} ${3:ID} ${4:#REQUIRED}>" 
										: "<!ATTLIST element-name attribute-name ID #REQUIRED>";
		attrListDecl.setTextEdit(new TextEdit(editRange, textEdit));
		attrListDecl.setDocumentation("<!ATTLIST element-name attribute-name ID #REQUIRED>");
		
		response.addCompletionItem(attrListDecl);

		// Insert Internal DTD Entity Declaration
		// see https://www.w3.org/TR/REC-xml/#dt-entdecl
		CompletionItem internalEntity = new CompletionItem();
		internalEntity.setLabel("Insert Internal DTD Entity declaration");
		internalEntity.setKind(CompletionItemKind.EnumMember);
		internalEntity.setFilterText("<!ENTITY ");
		internalEntity.setInsertTextFormat(insertFormat);
		startOffset = request.getOffset();
		
		textEdit = isSnippetsSupported ? "<!ENTITY ${1:entity-name} \"${2:entity-value}\">" : "<!ENTITY entity-name \"entity-value\">";
		internalEntity.setTextEdit(new TextEdit(editRange, textEdit));
		internalEntity.setDocumentation("<!ENTITY entity-name \"entity-value\">");
		
		response.addCompletionItem(internalEntity);

		// Insert External DTD Entity Declaration
		// see https://www.w3.org/TR/REC-xml/#dt-entdecl
		CompletionItem externalEntity = new CompletionItem();
		externalEntity.setLabel("Insert External DTD Entity declaration");
		externalEntity.setKind(CompletionItemKind.EnumMember);
		externalEntity.setFilterText("<!ENTITY ");
		externalEntity.setInsertTextFormat(insertFormat);
		startOffset = request.getOffset();
		
		textEdit = isSnippetsSupported ? "<!ENTITY ${1:entity-name} SYSTEM \"${2:entity-value}\">" : "<!ENTITY entity-name SYSTEM \"entity-value\">";
		externalEntity
				.setTextEdit(new TextEdit(editRange, textEdit));
		externalEntity.setDocumentation("<!ENTITY entity-name SYSTEM \"entity-value\">");
		
		response.addCompletionItem(externalEntity);
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

	/**
	 * Returns list of {@link ICompletionParticipant}.
	 * 
	 * @return list of {@link ICompletionParticipant}.
	 */
	private Collection<ICompletionParticipant> getCompletionParticipants() {
		return extensionsRegistry.getCompletionParticipants();
	}


	private static boolean isFollowedBy(String s, int offset, ScannerState intialState, TokenType expectedToken) {
		return getOffsetFollowedBy(s, offset, intialState, expectedToken) != -1;
	}

	/**
	 * Returns starting offset of 'expectedToken' if it the next non whitespace
	 * token after 'initialState'
	 * 
	 * @param s
	 * @param offset
	 * @param intialState
	 * @param expectedToken
	 * @return
	 */
	public static int getOffsetFollowedBy(String s, int offset, ScannerState intialState, TokenType expectedToken) {
		Scanner scanner = XMLScanner.createScanner(s, offset, intialState);
		TokenType token = scanner.scan();
		while (token == TokenType.Whitespace) {
			token = scanner.scan();
		}
		return (token == expectedToken) ? scanner.getTokenOffset() : -1;
	}

	private static int getWordStart(String s, int offset, int limit) {
		while (offset > limit && !isWhitespace(s.charAt(offset - 1))) {
			offset--;
		}
		return offset;
	}

	private static int getWordEnd(String s, int offset, int limit) {
		while (offset < limit && !isWhitespace(s.charAt(offset))) {
			offset++;
		}
		return offset;
	}

	public static Range getReplaceRange(int replaceStart, int replaceEnd, ICompletionRequest context)
			throws BadLocationException {
		int offset = context.getOffset();
		if (replaceStart > offset) {
			replaceStart = offset;
		}
		DOMDocument document = context.getXMLDocument();
		return new Range(document.positionAt(replaceStart), document.positionAt(replaceEnd));
	}

	private static String getLineIndent(int offset, String text) {
		int start = offset;
		while (start > 0) {
			char ch = text.charAt(start - 1);
			if ("\n\r".indexOf(ch) >= 0) {
				return text.substring(start, offset);
			}
			if (!isWhitespace(ch)) {
				return null;
			}
			start--;
		}
		return text.substring(0, offset);
	}

	private boolean isEmptyElement(String tag) {
		return false;
	}
}
