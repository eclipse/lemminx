package org.eclipse.lsp4xml.dom;

import java.io.InputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.dom.parser.Scanner;
import org.eclipse.lsp4xml.dom.parser.TokenType;
import org.eclipse.lsp4xml.dom.parser.XMLScanner;
import org.junit.Assert;
import org.junit.Test;

public class XMLDocumentTest {

	@Test
	public void testLargeFileWithScanner() {
		InputStream in = XMLDocumentTest.class.getResourceAsStream("/xml/largeFile.xml");
		String text = convertStreamToString(in);
		long start = System.currentTimeMillis();
		Scanner scanner = XMLScanner.createScanner(text);
		TokenType token = scanner.scan();
		while (token != TokenType.EOS) {
			token = scanner.scan();
		}
		System.err.println("Parsed with XMLScanner in " + (System.currentTimeMillis() - start) + " ms.");
	}

	@Test
	public void testLargeFileWithDocument() {
		InputStream in = XMLDocumentTest.class.getResourceAsStream("/xml/largeFile.xml");
		String text = convertStreamToString(in);
		TextDocument document = new TextDocument(text, "largeFile.xml");
		long start = System.currentTimeMillis();
		XMLDocument xmlDocument = XMLParser.getInstance().parse(document);
		System.err.println("Parsed with XMLParser in " + (System.currentTimeMillis() - start) + " ms.");
	}

	@Test
	public void findElementByW3CAndXPath() throws XPathExpressionException {
		XMLDocument document = XMLParser.getInstance().parse("<a><b><c>XXXX</c></b></a>", "test");

		// Get "c" element by w3c DOM model
		Node a = document.getDocumentElement();
		Assert.assertNotNull(a);
		Assert.assertEquals("a", a.getNodeName());
		Assert.assertTrue(a.isElement());

		Node b = a.getFirstChild();
		Assert.assertNotNull(b);
		Assert.assertEquals("b", b.getNodeName());
		Assert.assertTrue(b.isElement());

		Node c = b.getFirstChild();
		Assert.assertNotNull(c);
		Assert.assertEquals("c", c.getNodeName());
		Assert.assertTrue(c.isElement());

		// As XMLDocument implement w3c DOM model, we can use XPath.
		// Get "c" element by XPath
		XPath xPath = XPathFactory.newInstance().newXPath();
		Object result = xPath.evaluate("/a/b/c", document, XPathConstants.NODE);
		Assert.assertNotNull(result);
		Assert.assertTrue(result instanceof Element);
		Element elt = (Element) result;
		Assert.assertEquals("c", elt.getNodeName());
		Assert.assertEquals(c, elt);
		Assert.assertTrue(c.isElement());
	}

	static String convertStreamToString(InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}
}
