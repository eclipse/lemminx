package org.eclipse.lsp4xml.dom;

import java.io.InputStream;

import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.dom.XMLParser;
import org.junit.Test;

public class XMLDocumentTest {

	@Test
	public void testLargeFile() {
		InputStream in = XMLDocumentTest.class.getResourceAsStream("/xml/largeFile.xml");
		String text = convertStreamToString(in);
		TextDocument document = new TextDocument(text, "largeFile.xml");
		long start = System.currentTimeMillis();
		XMLDocument xmlDocument = XMLParser.getInstance().parse(document);
		System.err.println("Parsed in " + (System.currentTimeMillis() - start) + " ms.");
	}

	static String convertStreamToString(InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}
}
