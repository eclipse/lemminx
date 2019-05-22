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

public class DOMDocumentTest {

	@Test
	public void testLargeFileWithScanner() {
		InputStream in = DOMDocumentTest.class.getResourceAsStream("/xml/largeFile.xml");
		String text = convertStreamToString(in);
		long start = System.currentTimeMillis();
		Scanner scanner = XMLScanner.createScanner(text);
		TokenType token = scanner.scan();
		while (token != TokenType.EOS) {
			token = scanner.scan();
		}
		System.err.println("Parsed 'largeFile.xml' with XMLScanner in " + (System.currentTimeMillis() - start) + " ms.");
	}

	@Test
	public void testLargeFileWithDocument() {
		InputStream in = DOMDocumentTest.class.getResourceAsStream("/xml/largeFile.xml");
		String text = convertStreamToString(in);
		TextDocument document = new TextDocument(text, "largeFile.xml");
		long start = System.currentTimeMillis();
		DOMDocument xmlDocument = DOMParser.getInstance().parse(document, null);
		System.err.println("Parsed 'largeFile.xml' with XMLParser in " + (System.currentTimeMillis() - start) + " ms.");
	}
	
	@Test
	public void testBigLargeFileWithScanner() {
		InputStream in = DOMDocumentTest.class.getResourceAsStream("/xml/nasa.xml");
		String text = convertStreamToString(in);
		long start = System.currentTimeMillis();
		Scanner scanner = XMLScanner.createScanner(text);
		TokenType token = scanner.scan();
		while (token != TokenType.EOS) {
			token = scanner.scan();
		}
		System.err.println("Parsed 'nasa.xml' with XMLScanner in " + (System.currentTimeMillis() - start) + " ms.");
	}

	@Test
	public void testBigLargeFileWithDocument() {
		InputStream in = DOMDocumentTest.class.getResourceAsStream("/xml/nasa.xml");
		String text = convertStreamToString(in);
		TextDocument document = new TextDocument(text, "nasa.xml");
		long start = System.currentTimeMillis();
		DOMDocument xmlDocument = DOMParser.getInstance().parse(document, null);
		System.err.println("Parsed 'nasa.xml' with XMLParser in " + (System.currentTimeMillis() - start) + " ms.");
	}

	@Test
	public void findOneElementWithW3CAndXPath() throws XPathExpressionException {
		DOMDocument document = DOMParser.getInstance().parse("<a><b><c>XXXX</c></b></a>", "test", null);

		// Get "c" element by w3c DOM model
		DOMNode a = document.getDocumentElement();
		Assert.assertNotNull(a);
		Assert.assertEquals("a", a.getNodeName());
		Assert.assertTrue(a.isElement());

		DOMNode b = a.getFirstChild();
		Assert.assertNotNull(b);
		Assert.assertEquals("b", b.getNodeName());
		Assert.assertTrue(b.isElement());

		DOMNode c = b.getFirstChild();
		Assert.assertNotNull(c);
		Assert.assertEquals("c", c.getNodeName());
		Assert.assertTrue(c.isElement());

		// As XMLDocument implement w3c DOM model, we can use XPath.
		// Get "c" element by XPath
		XPath xPath = XPathFactory.newInstance().newXPath();
		Object result = xPath.evaluate("/a/b/c", document, XPathConstants.NODE);
		Assert.assertNotNull(result);
		Assert.assertTrue(result instanceof DOMElement);
		DOMElement elt = (DOMElement) result;
		Assert.assertEquals("c", elt.getNodeName());
		Assert.assertEquals(c, elt);
		Assert.assertTrue(c.isElement());
	}

	@Test
	public void findTextWithXPath() throws XPathExpressionException {
		DOMDocument document = DOMParser.getInstance().parse("<a><b><c>XXXX</c></b></a>", "test", null);

		XPath xPath = XPathFactory.newInstance().newXPath();
		Object result = xPath.evaluate("/a/b/c/text()", document, XPathConstants.NODE);
		Assert.assertNotNull(result);
		Assert.assertTrue(result instanceof DOMText);
		DOMText text = (DOMText) result;
		Assert.assertEquals("XXXX", text.getData());

		result = xPath.evaluate("/a/b/c/text()", document, XPathConstants.STRING);
		Assert.assertNotNull(result);
		Assert.assertEquals("XXXX", result.toString());
	}

	@Test
	public void siblingTests() throws XPathExpressionException {
		DOMDocument document = DOMParser.getInstance().parse("<a><b><c>XXXX</c><c>YYYY</c></b></a>", "test", null);

		DOMNode a = document.getDocumentElement();
		Assert.assertNotNull(a);
		DOMNode b = a.getFirstChild();
		Assert.assertNotNull(b);
		DOMNode c1 = b.getFirstChild();
		Assert.assertNotNull(c1);
		DOMNode t1 = c1.getFirstChild();
		Assert.assertTrue(t1.isText());
		DOMText text1 = (DOMText) t1;
		Assert.assertEquals("XXXX", text1.getData());

		DOMNode c2 = c1.getNextSibling();
		Assert.assertNotNull(c2);
		DOMNode t2 = c2.getFirstChild();
		Assert.assertTrue(t2.isText());
		DOMText text2 = (DOMText) t2;
		Assert.assertEquals("YYYY", text2.getData());

		DOMNode c1Previous = c2.getPreviousSibling();
		Assert.assertNotNull(c1Previous);
		Assert.assertEquals(c1, c1Previous);
	}

	@Test
	public void findElementListWithXPath() throws XPathExpressionException {
		DOMDocument document = DOMParser.getInstance().parse("<a><b><c>XXXX</c><c>YYYY</c></b></a>", "test", null);

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

	@Test
	public void testUsesSchemaTrue1WithNamespace() {
		String text = 
								"<root\n" +
								"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
								"  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 testXSD.xsd\"> </root> ";
		TextDocument textDocument = new TextDocument(text, "/home/nikolas/testXML.xml");
		DOMDocument d = DOMParser.getInstance().parse(text, textDocument.getUri(), null);
		Assert.assertTrue(d.hasSchemaInstancePrefix());
		Assert.assertTrue(d.usesSchema("/home/nikolas/testXSD.xsd"));
	}

	@Test
	public void testUsesSchemaTrue2WithNamespace() {
		String text = 
								"<root\n" +
								"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
								"  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 nested/testXSD.xsd\"> </root> ";
		TextDocument textDocument = new TextDocument(text, "/home/nikolas/testXML.xml");
		DOMDocument d = DOMParser.getInstance().parse(text, textDocument.getUri(), null);
		Assert.assertTrue(d.hasSchemaInstancePrefix());
		Assert.assertTrue(d.usesSchema("/home/nikolas/nested/testXSD.xsd"));
	}

	@Test
	public void testUsesSchemaTrue3WithNamespace() {
		String text = 
								"<root\n" +
								"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
								"  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 file:///home/nikolas/nested/testXSD.xsd\"> </root> ";
		TextDocument textDocument = new TextDocument(text, "/home/nikolas/testXML.xml");
		DOMDocument d = DOMParser.getInstance().parse(text, textDocument.getUri(), null);
		Assert.assertTrue(d.hasSchemaInstancePrefix());
		Assert.assertTrue(d.usesSchema("/home/nikolas/nested/testXSD.xsd"));
	}

	@Test
	public void testUsesSchemaTrue1NoNamespace() {
		String text = 
								"<root\n" +
								"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
								"  xsi:noNamespaceSchemaLocation=\"testXSD.xsd\"> </root> ";
		TextDocument textDocument = new TextDocument(text, "/home/nikolas/testXML.xml");
		DOMDocument d = DOMParser.getInstance().parse(text, textDocument.getUri(), null);
		Assert.assertTrue(d.hasSchemaInstancePrefix());
		Assert.assertTrue(d.usesSchema("/home/nikolas/testXSD.xsd"));
	}

	@Test
	public void testUsesSchemaTrue2NoNamespace() {
		String text = 
								"<root\n" +
								"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
								"  xsi:noNamespaceSchemaLocation=\"nested/testXSD.xsd\"> </root> ";
		TextDocument textDocument = new TextDocument(text, "/home/nikolas/testXML.xml");
		DOMDocument d = DOMParser.getInstance().parse(text, textDocument.getUri(), null);
		Assert.assertTrue(d.hasSchemaInstancePrefix());
		Assert.assertTrue(d.usesSchema("/home/nikolas/nested/testXSD.xsd"));
	}

	@Test
	public void testUsesSchemaTrue3NoNamespace() {
		String text = 
								"<root\n" +
								"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
								"  xsi:noNamespaceSchemaLocation=\"file:///home/nikolas/nested/testXSD.xsd\"> </root> ";
		TextDocument textDocument = new TextDocument(text, "/home/nikolas/testXML.xml");
		DOMDocument d = DOMParser.getInstance().parse(text, textDocument.getUri(), null);
		Assert.assertTrue(d.hasSchemaInstancePrefix());
		Assert.assertTrue(d.usesSchema("/home/nikolas/nested/testXSD.xsd"));
	}

	@Test
	public void testUsesSchemaFalseWithNamespace() {
		String text = 
								"<root\n" +
								"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
								"  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 testXSD.xsd\"> </root> ";
		TextDocument textDocument = new TextDocument(text, "/home/nikolas/testXML.xml");
		DOMDocument d = DOMParser.getInstance().parse(text, textDocument.getUri(), null);
		Assert.assertTrue(d.hasSchemaInstancePrefix());
		Assert.assertFalse(d.usesSchema("/home/NOT_NIKOLAS/testXSD.xsd")); //bad path
	}

	@Test
	public void testUsesSchemaFalseNoNamespace() {
		String text = 
								"<root\n" +
								"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
								"  xsi:noNamespaceSchemaLocation=\"nested/testXSD.xsd\"> </root> ";
		TextDocument textDocument = new TextDocument(text, "/home/nikolas/testXML.xml");
		DOMDocument d = DOMParser.getInstance().parse(text, textDocument.getUri(), null);
		Assert.assertTrue(d.hasSchemaInstancePrefix());
		Assert.assertFalse(d.usesSchema("/home/NOT_NIKOLAS/nested/testXSD.xsd")); //bad path
	}

	@Test
	public void testUsesSchemaTrueAbsolutePath() {
		String text = 
								"<root\n" +
								"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
								"  xsi:noNamespaceSchemaLocation=\"/home/nikolas/nested/testXSD.xsd\"> </root> ";
		TextDocument textDocument = new TextDocument(text, "/home/nikolas/testXML.xml");
		DOMDocument d = DOMParser.getInstance().parse(text, textDocument.getUri(), null);
		Assert.assertTrue(d.hasSchemaInstancePrefix());
		Assert.assertTrue(d.usesSchema("/home/nikolas/nested/testXSD.xsd")); //bad path
	}
	
	@Test
	public void testNoNamespaceSchemaLocationAndShemaLocationBoth() {
		String text = "<root\n" + //
				"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + //
				 "  xsi:noNamespaceSchemaLocation=\"/home/nikolas/nested/testXSD.xsd\"" + //
				 " xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 testXSD.xsd\"" + //
				 ">" + //
				 " </root> ";
		TextDocument textDocument = new TextDocument(text, "/home/test.xml");
		DOMDocument d = DOMParser.getInstance().parse(text, textDocument.getUri(), null);
		Assert.assertNotNull(d.getNoNamespaceSchemaLocation());
		Assert.assertNotNull(d.getSchemaLocation());
	}
}
