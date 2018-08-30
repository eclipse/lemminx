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
package org.eclipse.lsp4xml.internal.parser;

import java.util.ArrayList;
import java.util.EnumSet;

import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.model.Attr;
import org.eclipse.lsp4xml.model.Node;
import org.eclipse.lsp4xml.model.XMLDocument;

/**
 * Tolerant XML parser.
 *
 */
public class XMLParser {

	private static final XMLParser INSTANCE = new XMLParser();

	public static XMLParser getInstance() {
		return INSTANCE;
	}

	private XMLParser() {

	}

	public enum Flag {
		Content, Attribute;

		public static final EnumSet<Flag> ALL_OPTS = EnumSet.allOf(Flag.class);
	}

	public XMLDocument parse(String text, String uri) {
		return parse(text, uri, null);
	}

	public XMLDocument parse(String text, String uri, EnumSet<Flag> mask) {
		return parse(new TextDocument(text, uri), mask);
	}

	public XMLDocument parse(TextDocument document) {
		return parse(document, null);
	}

	public XMLDocument parse(TextDocument document, EnumSet<Flag> mask) {

		String text = document.getText();
		Scanner scanner = XMLScanner.createScanner(text);
		XMLDocument xmlDocument = new XMLDocument(document);

		Node curr = xmlDocument;
		Attr attr = null;
		int endTagStart = -1;
		String pendingAttribute = null;
		TokenType token = scanner.scan();
		while (token != TokenType.EOS) {
			switch (token) {
			case StartTagOpen: {
				Node child = new Node(scanner.getTokenOffset(), text.length(), new ArrayList<>(), curr, xmlDocument);
				curr.children.add(child);
				curr = child;
				break;
			}

			case StartTag: {
				curr.tag = scanner.getTokenText();
				break;
			}

			case StartTagClose:
				curr.end = scanner.getTokenEnd(); // might be later set to end tag position
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
					curr.end = scanner.getTokenEnd();
					curr = curr.parent;
				}
				break;

			case EndTagClose:
				if (curr.parent != null) {
					curr.end = scanner.getTokenEnd();
					curr = curr.parent;
				}
				break;

			case AttributeName: {
				pendingAttribute = scanner.getTokenText();
				curr.setAttribute(pendingAttribute, null); // Support valueless attributes such as 'checked'
				if (mask != null && mask.contains(Flag.Attribute)) {
					attr = new Attr(pendingAttribute, new Node(scanner.getTokenOffset(),
							scanner.getTokenOffset() + pendingAttribute.length(), null, curr, xmlDocument));
					curr.setAttributeNode(attr);
				}
				break;
			}

			case AttributeValue: {
				String value = scanner.getTokenText();
				if (curr.hasAttributes()) {
					curr.setAttribute(pendingAttribute, value);
					if (mask != null && mask.contains(Flag.Attribute) && attr != null) {						
						attr.setNodeValue(new Node(scanner.getTokenOffset(), scanner.getTokenOffset() + value.length(),
								null, curr, xmlDocument));
					}
					pendingAttribute = null;
					attr = null;
				}
				break;
			}

			case CDATATagOpen: {
				Node cdataNode = new Node(scanner.getTokenOffset(), text.length(), new ArrayList<>(), curr,
						xmlDocument);// TODO: might need arraylist
				cdataNode.isCDATA = true;
				curr.children.add(cdataNode);
				curr = cdataNode;
				break;
			}

			case CDATAContent: {
				if (curr.tag == null) {
					curr.tag = "";
				}
				curr.tag += scanner.getTokenText();
				break;
			}

			case CDATATagClose: {
				curr.end = scanner.getTokenEnd();
				curr.closed = true;
				curr = curr.parent;
				break;
			}

			case StartPrologOrPI: {
				Node prologOrPINode = new Node(scanner.getTokenOffset(), text.length(), new ArrayList<>(), curr,
						xmlDocument);
				curr.children.add(prologOrPINode);
				curr = prologOrPINode;
				break;
			}

			case PIName: {
				curr.tag = scanner.getTokenText();
				curr.isProcessingInstruction = true;
				curr.content = "";
				break;
			}

			case PrologName: {
				curr.tag = scanner.getTokenText();
				curr.isProlog = true;
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

			case Content: {
				if (mask != null && mask.contains(Flag.Content)) {
					String content = scanner.getTokenText();
					Node cdata = new Node(scanner.getTokenOffset(), content.length(), null, curr, xmlDocument);
					cdata.content = content;
					curr.children.add(cdata);
				}
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
