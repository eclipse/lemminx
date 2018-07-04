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
package org.eclipse.xml.languageserver.internal.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.xml.languageserver.model.Node;
import org.eclipse.xml.languageserver.model.XMLDocument;

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

	public XMLDocument parse(String text) {
		Scanner scanner = XMLScanner.createScanner(text);
		XMLDocument xmlDocument = new XMLDocument(text);

		Node curr = xmlDocument;
		int endTagStart = -1;
		String pendingAttribute = null;
		TokenType token = scanner.scan();
		while (token != TokenType.EOS) {
			switch (token) {
			case StartTagOpen:
				Node child = new Node(scanner.getTokenOffset(), text.length(), new ArrayList<>(), curr, xmlDocument);
				curr.children.add(child);
				curr = child;
				break;
			case StartTag:
				curr.tag = scanner.getTokenText();
				break;
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
				String attributeName = pendingAttribute = scanner.getTokenText();
				Map<String, String> attributes = curr.attributes;
				if (attributes == null) {
					curr.attributes = attributes = new HashMap<>();
				}
				attributes.put(pendingAttribute, null); // Support valueless attributes such as 'checked'
				break;
			}
			case AttributeValue: {
				String value = scanner.getTokenText();
				Map<String, String> attributes = curr.attributes;
				if (attributes != null && pendingAttribute != null) {
					attributes.put(pendingAttribute, value);
					pendingAttribute = null;
				}
				break;
			}

			case CDATATagOpen: {
				Node cdataNode = new Node(scanner.getTokenOffset(), text.length(), new ArrayList<>(), curr, xmlDocument);//TODO: might need arraylist
				cdataNode.isCDATA = true;
				curr.children.add(cdataNode);
				curr = cdataNode;
				break;
			}
			case CDATAContent: {
				
				if(curr.tag == null){
					curr.tag="";
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
