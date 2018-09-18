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
package org.eclipse.lsp4xml.dom;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.dom.parser.Scanner;
import org.eclipse.lsp4xml.dom.parser.TokenType;
import org.eclipse.lsp4xml.dom.parser.XMLScanner;

/**
 * Tolerant XML parser.
 *
 */
public class XMLParser {

	private static final Logger LOGGER = Logger.getLogger(XMLParser.class.getName());

	private static final XMLParser INSTANCE = new XMLParser();

	public static final String CDATA_TAG = "#CDATA";
	public static final String DOCTYPE_TAG = "#Doctype";
	public static final String TEXT_TAG = "#Text";
	public static final String COMMENT_TAG = "#Comment";

	public static XMLParser getInstance() {
		return INSTANCE;
	}

	private XMLParser() {

	}

	public XMLDocument parse(String text, String uri) {
		return parse(new TextDocument(text, uri));
	}

	public XMLDocument parse(TextDocument document) {

		String text = document.getText();
		Scanner scanner = XMLScanner.createScanner(text);
		XMLDocument xmlDocument = new XMLDocument(document);

		Node curr = xmlDocument;
		Node lastClosed = xmlDocument;
		Attr attr = null;
		int endTagStart = -1;
		String pendingAttribute = null;
		TokenType token = scanner.scan();
		while (token != TokenType.EOS) {
			switch (token) {
			case StartTagOpen: {
				Element child = xmlDocument.createElement(scanner.getTokenOffset(), text.length());
				curr.addChild(child);
				curr = child;
				break;
			}

			case StartTag: {
				curr.tag = scanner.getTokenText();
				break;
			}

			case StartTagClose:
				curr.end = scanner.getTokenEnd(); // might be later set to end tag position
				curr.startTagClose = true;
				if (curr.tag != null && isEmptyElement(curr.tag) && curr.parent != null) {
					curr.closed = true;
					curr = curr.parent;
				}
				break;

			case EndTagOpen:
				endTagStart = scanner.getTokenOffset();
				break;

			case EndTag:
				String closeTag = scanner.getTokenText().toLowerCase();
				while (!curr.isSameTag(closeTag) && curr.parent != null) {
					curr.end = endTagStart;
					curr.closed = false;
					curr = curr.parent;
				}
				if (curr != xmlDocument) {
					curr.closed = true;
					curr.endTagStart = endTagStart;
				}
				break;

			case StartTagSelfClose:
				if (curr.parent != null) {
					curr.closed = true;
					curr.selfClosed = true;
					curr.end = scanner.getTokenEnd();
					lastClosed = curr;
					curr = curr.parent;
				}
				break;

			case EndTagClose:
				if (curr.parent != null) {
					curr.end = scanner.getTokenEnd();
					lastClosed = curr;
					curr = curr.parent;
				}
				break;

			case AttributeName: {
				pendingAttribute = scanner.getTokenText();
				attr = new Attr(pendingAttribute, new Node(scanner.getTokenOffset(),
						scanner.getTokenOffset() + pendingAttribute.length(), null, curr, xmlDocument));
				curr.setAttributeNode(attr);
				break;
			}

			case AttributeValue: {
				String value = scanner.getTokenText();
				if (curr.hasAttributes()) {
					attr.setValue(value, new Node(scanner.getTokenOffset(), scanner.getTokenOffset() + value.length(),
							null, curr, xmlDocument));
				}
				pendingAttribute = null;
				attr = null;
				break;
			}

			case CDATATagOpen: {
				CDataSection cdataNode = xmlDocument.createCDataSection(scanner.getTokenOffset(), text.length());
				cdataNode.tag = CDATA_TAG;
				curr.addChild(cdataNode);
				curr = cdataNode;
				break;
			}

			case CDATAContent: {
				CDataSection cdataNode = (CDataSection) curr;
				cdataNode.startContent = scanner.getTokenOffset();
				cdataNode.endContent = scanner.getTokenEnd();
				break;
			}

			case CDATATagClose: {
				curr.end = scanner.getTokenEnd();
				curr.closed = true;
				curr = curr.parent;
				break;
			}

			case StartPrologOrPI: {
				Node prologOrPINode = xmlDocument.createProcessingInstruction(scanner.getTokenOffset(), text.length());
				curr.addChild(prologOrPINode);
				curr = prologOrPINode;
				break;
			}

			case PIName: {
				curr.tag = scanner.getTokenText();
				((ProcessingInstruction) curr).processingInstruction = true;
				break;
			}

			case PrologName: {
				curr.tag = scanner.getTokenText();
				((ProcessingInstruction) curr).prolog = true;
				break;
			}

			case PIContent: {
				ProcessingInstruction processingInstruction = (ProcessingInstruction) curr;
				processingInstruction.startContent = scanner.getTokenOffset();
				processingInstruction.endContent = scanner.getTokenEnd();
				break;
			}

			case PIEnd:
			case PrologEnd: {
				curr.end = scanner.getTokenEnd();
				curr.closed = true;
				curr = curr.parent;
				break;
			}

			case StartCommentTag: {
				Comment comment = xmlDocument.createComment(scanner.getTokenOffset(), text.length());
				curr.addChild(comment);
				curr = comment;
				curr.tag = COMMENT_TAG;
				try {
					int endLine = document.positionAt(lastClosed.end).getLine();
					int startLine = document.positionAt(curr.start).getLine();
					if (endLine == startLine && lastClosed.end <= curr.start) {
						comment.commentSameLineEndTag = true;
					}
				} catch (BadLocationException e) {
					LOGGER.log(Level.SEVERE, "XMLParser StartCommentTag bad offset in document", e);
				}
				// }
				break;
			}

			case Comment: {
				Comment comment = (Comment) curr;
				comment.startContent = scanner.getTokenOffset();
				comment.endContent = scanner.getTokenEnd();
				break;
			}

			case StartDoctypeTag: {
				DocumentType doctype = xmlDocument.createDocumentType(scanner.getTokenOffset(), text.length());
				curr.addChild(doctype);
				curr = doctype;
				curr.tag = DOCTYPE_TAG;
				break;
			}

			case Doctype: {
				DocumentType doctype = (DocumentType) curr;
				doctype.startContent = scanner.getTokenOffset();
				doctype.endContent = scanner.getTokenEnd();
				break;
			}

			case EndCommentTag: {
				curr.end = scanner.getTokenEnd();
				curr.closed = true;
				curr = curr.parent;
				break;
			}
			case EndDoctypeTag: {
				curr.end = scanner.getTokenEnd();
				curr.closed = true;
				curr = curr.parent;
				break;
			}

			case Content: {
				// FIXME: don't use getTokenText (substring) to know if the content is only
				// spaces or line feed (scanner should know that).
				String content = scanner.getTokenText();
				if (content.trim().length() == 0) {
					break;
				}
				int start = scanner.getTokenOffset();
				int end = scanner.getTokenEnd();
				Text textNode = xmlDocument.createText(start, end);
				textNode.tag = TEXT_TAG;
				textNode.closed = true;
				curr.addChild(textNode);
				break;
			}

			}
			token = scanner.scan();
		}
		while (curr.parent != null) {
			curr.end = text.length();
			curr.closed = false;
			curr = curr.parent;
		}
		return xmlDocument;
	}

	private static boolean isEmptyElement(String tag) {
		return false;
	}

}
