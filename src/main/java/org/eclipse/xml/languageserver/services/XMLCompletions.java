package org.eclipse.xml.languageserver.services;

import java.util.Collection;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.xml.languageserver.internal.parser.BadLocationException;
import org.eclipse.xml.languageserver.internal.parser.Scanner;
import org.eclipse.xml.languageserver.internal.parser.TokenType;
import org.eclipse.xml.languageserver.internal.parser.XMLScanner;
import org.eclipse.xml.languageserver.model.Node;
import org.eclipse.xml.languageserver.model.XMLDocument;

public class XMLCompletions {

	private Collection<ICompletionParticipant> completionParticipants;

	public void setCompletionParticipants(Collection<ICompletionParticipant> completionParticipants) {
		this.completionParticipants = completionParticipants;
	}

	public CompletionList doComplete(TextDocumentItem document, Position position, XMLDocument xmlDocument,
			CompletionConfiguration settings) {
		CompletionList result = new CompletionList();
		CompletionContext context = null;
		try {
			context = new CompletionContext(document, position, xmlDocument);
		} catch (BadLocationException e) {
			return null;
		}

		int offset = context.getOffset();
		Node node = xmlDocument.findNodeBefore(offset);
		if (node == null) {
			return result;
		}

		String text = document.getText();
		Scanner scanner = XMLScanner.createScanner(text, node.start);
		context.setCurrentTag("");
		context.setCurrentAttributeName(null);

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
				context.setCurrentTag(scanner.getTokenText());
				break;
			case AttributeName:
				if (scanner.getTokenOffset() <= offset && offset <= scanner.getTokenEnd()) {
					return collectAttributeNameSuggestions(scanner.getTokenOffset(), scanner.getTokenEnd());
				}
				context.setCurrentAttributeName(scanner.getTokenText());
				break;
			case DelimiterAssign:
				if (scanner.getTokenEnd() == offset) {
					int endPos = scanNextForEndPos(offset, scanner, TokenType.AttributeValue);
					return collectAttributeValueSuggestions(offset, endPos, context);
				}
				break;
			case AttributeValue:
				if (scanner.getTokenOffset() <= offset && offset <= scanner.getTokenEnd()) {
					return collectAttributeValueSuggestions(scanner.getTokenOffset(), scanner.getTokenEnd(), context);
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
						return collectAttributeValueSuggestions(scanner.getTokenEnd(), offset, context);
					case AfterOpeningEndTag:
						return collectCloseTagSuggestions(scanner.getTokenOffset() - 1, false);
					case WithinContent:
						return collectInsideContent();
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
					String currentTag = context.getCurrentTag();
					if (currentTag.length() > 0) {
						return collectAutoCloseTagSuggestion(scanner.getTokenEnd(), currentTag, xmlDocument, result);
					}
				}
				break;
			case Content:
				if (offset <= scanner.getTokenEnd()) {
					return collectInsideContent();
				}
				break;
			default:
				if (offset <= scanner.getTokenEnd()) {
					return result;
				}
				break;
			}
			token = scanner.scan();
		}

		return result;
	}

	private CompletionList collectAutoCloseTagSuggestion(int tagCloseEnd, String tag, XMLDocument document,
			CompletionList result) {
		// if (!isEmptyElement(tag)) {
		Position pos;
		try {
			pos = document.positionAt(tagCloseEnd);
		} catch (BadLocationException e) {
			return result;
		}
		CompletionItem item = new CompletionItem();
		item.setLabel("</" + tag + ">");
		item.setKind(CompletionItemKind.Property);
		item.setFilterText("</" + tag + ">");
		item.setTextEdit(new TextEdit(new Range(pos, pos), "$0</" + tag + ">"));
		item.setInsertTextFormat(InsertTextFormat.Snippet);
		// }
		return result;
	}

	private boolean isEmptyElement(String tag) {
		// TODO Auto-generated method stub
		return false;
	}

	private CompletionList collectCloseTagSuggestions(int afterOpenBracket, boolean b, int endOffset) {
		// TODO Auto-generated method stub
		return null;
	}

	private CompletionList collectInsideContent() {
		// TODO Auto-generated method stub
		return null;
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

	private CompletionList collectAttributeValueSuggestions(int valueStart, int valueEnd, CompletionContext context) {
		Range range = null;
		boolean addQuotes = false;
		String valuePrefix;
		int offset = context.getOffset();
		String text = context.getDocument().getText();
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
				range = getReplaceRange(wsBefore, wsAfter, context);
			} catch (BadLocationException e) {				
				e.printStackTrace();
			}
			valuePrefix = offset >= valueContentStart && offset <= valueContentEnd
					? text.substring(valueContentStart, offset)
					: "";
			addQuotes = false;
		} else {
			try {
				range = getReplaceRange(valueStart, valueEnd, context);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			valuePrefix = text.substring(valueStart, offset);
			addQuotes = true;
		}

		String tag = context.getCurrentTag().toLowerCase();
		String attribute = context.getCurrentAttributeName().toLowerCase();
		
		if (completionParticipants != null && completionParticipants.size()> 0) {
			try {
				Range fullRange = getReplaceRange(valueStart, valueEnd, context);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (ICompletionParticipant participant : completionParticipants) {
				//if (participant.onHtmlAttributeValue) {
					//participant.onXMLAttributeValue({ document, position, tag, attribute, value: valuePrefix, range: fullRange });
				//}
			}
		}
		return null;
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

	private static Range getReplaceRange(int replaceStart, int replaceEnd, CompletionContext context)
			throws BadLocationException {
		int offset = context.getOffset();
		if (replaceStart > offset) {
			replaceStart = offset;
		}
		XMLDocument document = context.getXMLDocument();
		return new Range(document.positionAt(replaceStart), document.positionAt(replaceEnd));
	}

}
