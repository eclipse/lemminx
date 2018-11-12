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
import org.w3c.dom.NodeList;

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
		XMLDocument xmlDocument = XMLParser.getInstance().parse(document, null);
		System.err.println("Parsed with XMLParser in " + (System.currentTimeMillis() - start) + " ms.");
	}

	@Test
	public void findOneElementWithW3CAndXPath() throws XPathExpressionException {
		XMLDocument document = XMLParser.getInstance().parse("<a><b><c>XXXX</c></b></a>", "test", null);

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

	@Test
	public void findTextWithXPath() throws XPathExpressionException {
		XMLDocument document = XMLParser.getInstance().parse("<a><b><c>XXXX</c></b></a>", "test", null);

		XPath xPath = XPathFactory.newInstance().newXPath();
		Object result = xPath.evaluate("/a/b/c/text()", document, XPathConstants.NODE);
		Assert.assertNotNull(result);
		Assert.assertTrue(result instanceof Text);
		Text text = (Text) result;
		Assert.assertEquals("XXXX", text.getData());

		result = xPath.evaluate("/a/b/c/text()", document, XPathConstants.STRING);
		Assert.assertNotNull(result);
		Assert.assertEquals("XXXX", result.toString());
	}

	@Test
	public void siblingTests() throws XPathExpressionException {
		XMLDocument document = XMLParser.getInstance().parse("<a><b><c>XXXX</c><c>YYYY</c></b></a>", "test", null);

		Node a = document.getDocumentElement();
		Assert.assertNotNull(a);
		Node b = a.getFirstChild();
		Assert.assertNotNull(b);
		Node c1 = b.getFirstChild();
		Assert.assertNotNull(c1);
		Node t1 = c1.getFirstChild();
		Assert.assertTrue(t1.isText());
		Text text1 = (Text) t1;
		Assert.assertEquals("XXXX", text1.getData());

		Node c2 = c1.getNextSibling();
		Assert.assertNotNull(c2);
		Node t2 = c2.getFirstChild();
		Assert.assertTrue(t2.isText());
		Text text2 = (Text) t2;
		Assert.assertEquals("YYYY", text2.getData());

		Node c1Previous = c2.getPreviousSibling();
		Assert.assertNotNull(c1Previous);
		Assert.assertEquals(c1, c1Previous);
	}

	@Test
	public void findElementListWithXPath() throws XPathExpressionException {
		XMLDocument document = XMLParser.getInstance().parse("<a><b><c>XXXX</c><c>YYYY</c></b></a>", "test", null);

		XPath xPath = XPathFactory.newInstance().newXPath();
		Object result = xPath.evaluate("/a/b//c", document, XPathConstants.NODESET);
		Assert.assertNotNull(result);
		Assert.assertTrue(result instanceof NodeList);
		NodeList elts = (NodeList) result;
		Assert.assertEquals(2, elts.getLength());

	}

	static String convertStreamToString(InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}
}
