package org.eclipse.xml.languageserver.internal.parser;

import org.eclipse.xml.languageserver.model.XMLDocument;

public class Test {

	public static void main(String[] args) {
		XMLParser parser = XMLParser.getInstance();
		XMLDocument document = parser.parse("<a<b /><c></c><d");
		System.err.println(document);
	}
	
	public static void main3(String[] args) {
		XMLParser parser = XMLParser.getInstance();
		XMLDocument document = parser.parse("<");
		System.err.println(document);
	}

	public static void main2(String[] args) {
		XMLParser parser = XMLParser.getInstance();
		XMLDocument document = parser.parse("<r><a>xx\nx</a><b>yyy</b></r>");
		System.err.println(document);
	}
}
