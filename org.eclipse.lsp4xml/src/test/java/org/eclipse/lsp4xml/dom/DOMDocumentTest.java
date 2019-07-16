package org.eclipse.lsp4xml.dom;

import static org.eclipse.lsp4xml.utils.IOUtils.convertStreamToString;

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
		System.err
				.println("Parsed 'largeFile.xml' with XMLScanner in " + (System.currentTimeMillis() - start) + " ms.");
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

	@Test
	public void testDOMAsDTD() {
		String content = "<!ELEMENT";

		// .xml file extension
		DOMDocument xml = DOMParser.getInstance().parse(content, "test.xml", null);
		Assert.assertFalse(xml.isDTD());
		DOMNode element = xml.getChild(0);
		Assert.assertTrue(element.isElement());

		// .unknown file extension
		DOMDocument unknown = DOMParser.getInstance().parse(content, "test.unknown", null);
		Assert.assertFalse(unknown.isDTD());
		DOMNode unknownElement = unknown.getChild(0);
		Assert.assertTrue(unknownElement.isElement());

		// .dtd file extension
		DOMDocument dtd = DOMParser.getInstance().parse(content, "test.dtd", null);
		Assert.assertTrue(dtd.isDTD());
		DOMNode dtdDocType = dtd.getChild(0);
		Assert.assertTrue(dtdDocType.isDoctype());
		DOMNode dtdElementDecl = dtdDocType.getChild(0);
		Assert.assertTrue(dtdElementDecl.isDTDElementDecl());

		// .ent file extension
		DOMDocument ent = DOMParser.getInstance().parse(content, "test.ent", null);
		Assert.assertTrue(ent.isDTD());
		DOMNode entDocType = ent.getChild(0);
		Assert.assertTrue(entDocType.isDoctype());
		DOMNode entElementDecl = entDocType.getChild(0);
		Assert.assertTrue(entElementDecl.isDTDElementDecl());

		// .mod file extension
		DOMDocument mod = DOMParser.getInstance().parse(content, "test.mod", null);
		Assert.assertTrue(mod.isDTD());
		DOMNode modDocType = mod.getChild(0);
		Assert.assertTrue(modDocType.isDoctype());
		DOMNode modElemmodDecl = modDocType.getChild(0);
		Assert.assertTrue(modElemmodDecl.isDTDElementDecl());
	}
}
