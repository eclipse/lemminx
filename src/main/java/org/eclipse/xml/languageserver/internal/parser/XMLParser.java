package org.eclipse.xml.languageserver.internal.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.xml.languageserver.model.IXMLParser;
import org.eclipse.xml.languageserver.model.Node;
import org.eclipse.xml.languageserver.model.XMLDocument;

public class XMLParser implements IXMLParser {
	
	private static final IXMLParser INSTANCE = new XMLParser();

	public static IXMLParser getInstance() {
		return INSTANCE;
	}


	private XMLParser() {
		
	}

	
	@Override
	public XMLDocument parse(String text) {
		Scanner scanner = XMLScanner.createScanner(text);
		XMLDocument fmDocument = new XMLDocument(text);

		Node curr = fmDocument;
		int endTagStart = -1;
		String pendingAttribute = null;
		TokenType token = scanner.scan();
		while (token != TokenType.EOS) {
			switch (token) {
			case StartTagOpen:
				Node child = new Node(scanner.getTokenOffset(), text.length(), new ArrayList<>(), curr);
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
				if (curr != fmDocument) {
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
			case CDATAContent: {
				curr.tag = scanner.getTokenText();
				break;
			}

			case CDATATagClose: {
				curr.end = scanner.getTokenEnd();
				curr.closed = true;
				curr = curr.parent;
				break;
			}

			case CDATATagOpen: {
				Node cdataNode = new Node(scanner.getTokenOffset(), text.length(), null, curr);//TODO: might need arraylist
				curr.children.add(cdataNode);
				curr = cdataNode;
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
		return fmDocument;
	}

	private static boolean isEmptyElement(String tag) {
		return false;
	}

}
