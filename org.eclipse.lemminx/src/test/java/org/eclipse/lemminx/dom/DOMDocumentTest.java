/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.dom;

import static org.eclipse.lemminx.utils.IOUtils.convertStreamToString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.lemminx.commons.TextDocument;
import org.eclipse.lemminx.dom.parser.Scanner;
import org.eclipse.lemminx.dom.parser.TokenType;
import org.eclipse.lemminx.dom.parser.XMLScanner;
import org.junit.jupiter.api.Test;
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
		DOMParser.getInstance().parse(document, null);
		System.err.println("Parsed 'largeFile.xml' with XMLParser in " + (System.currentTimeMillis() - start) + " ms.");
	}

	@Test
	public void testBigLargeFileWithScanner() {
		InputStream in = DOMDocumentTest.class.getResourceAsStream("/xml/content.xml");
		String text = convertStreamToString(in);
		long start = System.currentTimeMillis();
		Scanner scanner = XMLScanner.createScanner(text);
		TokenType token = scanner.scan();
		while (token != TokenType.EOS) {
			token = scanner.scan();
		}
		System.err.println("Parsed 'content.xml' with XMLScanner in " + (System.currentTimeMillis() - start) + " ms.");
	}

	@Test
	public void testBigLargeFileWithDocument() {
		InputStream in = DOMDocumentTest.class.getResourceAsStream("/xml/content.xml");
		String text = convertStreamToString(in);
		TextDocument document = new TextDocument(text, "content.xml");
		long start = System.currentTimeMillis();
		DOMParser.getInstance().parse(document, null);
		System.err.println("Parsed 'content.xml' with XMLParser in " + (System.currentTimeMillis() - start) + " ms.");
	}

	@Test
	public void findOneElementWithW3CAndXPath() throws XPathExpressionException {
		DOMDocument document = DOMParser.getInstance().parse("<a><b><c>XXXX</c></b></a>", "test", null);

		// Get "c" element by w3c DOM model
		DOMNode a = document.getDocumentElement();
		assertNotNull(a);
		assertEquals("a", a.getNodeName());
		assertTrue(a.isElement());

		DOMNode b = a.getFirstChild();
		assertNotNull(b);
		assertEquals("b", b.getNodeName());
		assertTrue(b.isElement());

		DOMNode c = b.getFirstChild();
		assertNotNull(c);
		assertEquals("c", c.getNodeName());
		assertTrue(c.isElement());

		// As XMLDocument implement w3c DOM model, we can use XPath.
		// Get "c" element by XPath
		XPath xPath = XPathFactory.newInstance().newXPath();
		Object result = xPath.evaluate("/a/b/c", document, XPathConstants.NODE);
		assertNotNull(result);
		assertTrue(result instanceof DOMElement);
		DOMElement elt = (DOMElement) result;
		assertEquals("c", elt.getNodeName());
		assertEquals(c, elt);
		assertTrue(c.isElement());
	}

	@Test
	public void findTextWithXPath() throws XPathExpressionException {
		DOMDocument document = DOMParser.getInstance().parse("<a><b><c>XXXX</c></b></a>", "test", null);

		XPath xPath = XPathFactory.newInstance().newXPath();
		Object result = xPath.evaluate("/a/b/c/text()", document, XPathConstants.NODE);
		assertNotNull(result);
		assertTrue(result instanceof DOMText);
		DOMText text = (DOMText) result;
		assertEquals("XXXX", text.getData());

		result = xPath.evaluate("/a/b/c/text()", document, XPathConstants.STRING);
		assertNotNull(result);
		assertEquals("XXXX", result.toString());
	}

	@Test
	public void siblingTests() throws XPathExpressionException {
		DOMDocument document = DOMParser.getInstance().parse("<a><b><c>XXXX</c><c>YYYY</c></b></a>", "test", null);

		DOMNode a = document.getDocumentElement();
		assertNotNull(a);
		DOMNode b = a.getFirstChild();
		assertNotNull(b);
		DOMNode c1 = b.getFirstChild();
		assertNotNull(c1);
		DOMNode t1 = c1.getFirstChild();
		assertTrue(t1.isText());
		DOMText text1 = (DOMText) t1;
		assertEquals("XXXX", text1.getData());

		DOMNode c2 = c1.getNextSibling();
		assertNotNull(c2);
		DOMNode t2 = c2.getFirstChild();
		assertTrue(t2.isText());
		DOMText text2 = (DOMText) t2;
		assertEquals("YYYY", text2.getData());

		DOMNode c1Previous = c2.getPreviousSibling();
		assertNotNull(c1Previous);
		assertEquals(c1, c1Previous);
	}

	@Test
	public void findElementListWithXPath() throws XPathExpressionException {
		DOMDocument document = DOMParser.getInstance().parse("<a><b><c>XXXX</c><c>YYYY</c></b></a>", "test", null);

		XPath xPath = XPathFactory.newInstance().newXPath();
		Object result = xPath.evaluate("/a/b//c", document, XPathConstants.NODESET);
		assertNotNull(result);
		assertTrue(result instanceof NodeList);
		NodeList elts = (NodeList) result;
		assertEquals(2, elts.getLength());

	}

	@Test
	public void testDOMAsDTD() {
		String content = "<!ELEMENT";

		// .xml file extension
		DOMDocument xml = DOMParser.getInstance().parse(content, "test.xml", null);
		assertFalse(xml.isDTD());
		DOMNode element = xml.getChild(0);
		assertTrue(element.isElement());

		// .unknown file extension
		DOMDocument unknown = DOMParser.getInstance().parse(content, "test.unknown", null);
		assertFalse(unknown.isDTD());
		DOMNode unknownElement = unknown.getChild(0);
		assertTrue(unknownElement.isElement());

		// .dtd file extension
		DOMDocument dtd = DOMParser.getInstance().parse(content, "test.dtd", null);
		assertTrue(dtd.isDTD());
		DOMNode dtdDocType = dtd.getChild(0);
		assertTrue(dtdDocType.isDoctype());
		DOMNode dtdElementDecl = dtdDocType.getChild(0);
		assertTrue(dtdElementDecl.isDTDElementDecl());

		// .ent file extension
		DOMDocument ent = DOMParser.getInstance().parse(content, "test.ent", null);
		assertTrue(ent.isDTD());
		DOMNode entDocType = ent.getChild(0);
		assertTrue(entDocType.isDoctype());
		DOMNode entElementDecl = entDocType.getChild(0);
		assertTrue(entElementDecl.isDTDElementDecl());

		// .mod file extension
		DOMDocument mod = DOMParser.getInstance().parse(content, "test.mod", null);
		assertTrue(mod.isDTD());
		DOMNode modDocType = mod.getChild(0);
		assertTrue(modDocType.isDoctype());
		DOMNode modElemmodDecl = modDocType.getChild(0);
		assertTrue(modElemmodDecl.isDTDElementDecl());
	}

	@Test
	public void defaultNamespaceURI() {
		String xml = "<beans xmlns=\"http://www.springframework.org/schema/beans\"\r\n"
				+ "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ "       xmlns:camel=\"http://camel.apache.org/schema/spring\"\r\n"
				+ "       xsi:schemaLocation=\"\r\n"
				+ "       http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ "       http://camel.apache.org/schema/spring http://camel.apache.org/schema/spring/camel-spring.xsd\r\n"
				+ "    \">" + "<bean /><camel:camelContext>";
		DOMDocument dom = DOMParser.getInstance().parse(xml, "test.xml", null);

		DOMElement bean = (DOMElement) dom.getDocumentElement().getFirstChild();
		assertNull(bean.getPrefix());
		assertEquals("http://www.springframework.org/schema/beans", bean.getNamespaceURI());

		DOMElement camel = (DOMElement) bean.getNextSibling();
		assertEquals("camel", camel.getPrefix());
		assertEquals("http://camel.apache.org/schema/spring", camel.getNamespaceURI());

	}

	@Test
	public void noDefaultNamespaceURI() {
		String xml = "<b:beans xmlns:b=\"http://www.springframework.org/schema/beans\"\r\n"
				+ "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ "       xmlns:camel=\"http://camel.apache.org/schema/spring\"\r\n"
				+ "       xsi:schemaLocation=\"\r\n"
				+ "       http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ "       http://camel.apache.org/schema/spring http://camel.apache.org/schema/spring/camel-spring.xsd\r\n"
				+ "    \">" + "<bean /><camel:camelContext>";
		DOMDocument dom = DOMParser.getInstance().parse(xml, "test.xml", null);

		DOMElement bean = (DOMElement) dom.getDocumentElement().getFirstChild();
		assertNull(bean.getPrefix());
		assertNull(bean.getNamespaceURI());

		DOMElement camel = (DOMElement) bean.getNextSibling();
		assertEquals("camel", camel.getPrefix());
		assertEquals("http://camel.apache.org/schema/spring", camel.getNamespaceURI());

	}
}
