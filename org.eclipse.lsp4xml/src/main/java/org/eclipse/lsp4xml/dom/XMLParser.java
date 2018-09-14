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

import java.util.ArrayList;
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
				Element child = new Element(scanner.getTokenOffset(), text.length(), new ArrayList<>(), curr,
						xmlDocument);
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
				CDataSection cdataNode = new CDataSection(scanner.getTokenOffset(), text.length(), curr, xmlDocument);
				cdataNode.tag = CDATA_TAG;
				curr.addChild(cdataNode);
				curr = cdataNode;
				break;
			}

			case CDATAContent: {
				if (curr.content == null) {
					curr.content = "";
				}
				curr.content += scanner.getTokenText();
				break;
			}

			case CDATATagClose: {
				curr.end = scanner.getTokenEnd();
				curr.closed = true;
				curr = curr.parent;
				break;
			}

			case StartPrologOrPI: {
				Node prologOrPINode = new ProcessingInstruction(scanner.getTokenOffset(), text.length(), curr,
						xmlDocument);
				curr.addChild(prologOrPINode);
				curr = prologOrPINode;
				break;
			}

			case PIName: {
				curr.tag = scanner.getTokenText();
				curr.content = "";
				((ProcessingInstruction) curr).processingInstruction = true;
				break;
			}

			case PrologName: {
				curr.tag = scanner.getTokenText();
				((ProcessingInstruction) curr).prolog = true;
				break;
			}

			case PIContent: {
				curr.content += scanner.getTokenText().trim();
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
				// if (mask != null && mask.contains(Flag.Comment)) {
				Comment comment = new Comment(scanner.getTokenOffset(), text.length(), curr, xmlDocument);
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
				// if (mask != null && mask.contains(Flag.Comment)) {
				curr.content = scanner.getTokenText();
				// }
				break;
			}

			case StartDoctypeTag: {
				Node doctype = new DocumentType(scanner.getTokenOffset(), text.length(), curr, xmlDocument);
				curr.addChild(doctype);
				curr = doctype;
				curr.tag = DOCTYPE_TAG;
				break;
			}

			case Doctype: {
				if (curr.content == null) {
					curr.content = "";
				}
				curr.content += scanner.getTokenText();
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
				String content = scanner.getTokenText();
				if (content.trim().length() == 0) {
					break;
				}
				int start = scanner.getTokenOffset();
				Text textNode = new Text(start, start + content.length(), curr, xmlDocument);
				textNode.tag = TEXT_TAG;
				textNode.content = content;
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
