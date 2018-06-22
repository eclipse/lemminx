package org.eclipse.xml.languageserver.internal.parser;

import org.eclipse.xml.languageserver.internal.parser.XMLParser;
import org.eclipse.xml.languageserver.model.XMLDocument;
import org.eclipse.xml.languageserver.model.IXMLParser;

public class Test {

	public static void main(String[] args) {
		IXMLParser parser = XMLParser.getInstance();
		XMLDocument document = parser.parse("<");
		System.err.println(document);
	}
	
	public static void main2(String[] args) {
		IXMLParser parser = XMLParser.getInstance();
		XMLDocument document = parser.parse("<r><a>xx\nx</a><b>yyy</b></r>");
		System.err.println(document);
	}
}
