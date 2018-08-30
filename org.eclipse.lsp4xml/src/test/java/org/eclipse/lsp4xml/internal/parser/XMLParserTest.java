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
package org.eclipse.lsp4xml.internal.parser;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.lsp4xml.model.Node;
import org.eclipse.lsp4xml.model.XMLDocument;
import org.junit.Assert;
import org.junit.Test;

/**
 * XML parser tests.
 *
 */
public class XMLParserTest {

	@Test
	public void testSingleElement() {
		Node html = createNode("html", 0, 6, 13, true);

		assertDocument("<html></html>", html);
	}

	@Test
	public void testNestedElement() {
		Node body = createNode("body", 6, 12, 19, true);
		Node html = createNode("html", 0, 19, 26, true, body);

		assertDocument("<html><body></body></html>", html);
	}

	@Test
	public void testNestedElements() {
		Node head = createNode("head", 6, 12, 19, true);
		Node body = createNode("body", 19, 25, 32, true);
		Node html = createNode("html", 0, 32, 39, true, head, body);

		assertDocument("<html><head></head><body></body></html>", html);
	}

	@Test
	public void testNestedNestedElements() {
		Node c = createNode("c", 6, 9, 13, true);
		Node b = createNode("b", 3, 13, 17, true, c);
		Node a = createNode("a", 0, 17, 21, true, b);
		assertDocument("<a><b><c></c></b></a>", a);
	}

	@Test
	public void testSelfClosing() {
		Node br = createNode("br", 0, -1, 5, true);

		assertDocument("<br/>", br);
	}

	@Test
	public void testNestedSelfClosingTag() {
		Node br = createNode("br", 5, -1, 10, true);
		Node span = createNode("span", 10, 16, 23, true);
		Node div = createNode("div", 0, 23, 29, true, br, span);

		assertDocument("<div><br/><span></span></div>", div);
	}

	@Test
	public void testEmptyTagT() {
		Node br = createNode("br", 0, -1, 4, false);

		assertDocument("<br>", br);
	}

	@Test
	public void singleEndTag() {
		assertFailedDocument("</meta>");
	}

	@Test
	public void testEndTagInsideElement() {
		Node div = createNode("div", 0, 5, 11, true);
		assertDocument("<div></div><div>", div);
	}

	@Test
	public void testStartTagInsideElement() {
		Node div2 = createNode("div", 5, 10, 16, true);
		Node div = createNode("div", 0, -1, 16, false, div2);

		assertDocument("<div><div></div>", div);
	}

	@Test
	public void testStartTagInsideElement2() {
		Node div = createNode("div", 5, -1, 10, false);
		Node cat = createNode("cat", 0, 10, 16, true, div);

		assertDocument("<cat><div></cat>", cat);
	}

	@Test
	public void testMultipleStartTagInsideElement() {
		Node span = createNode("span", 9, -1, 15, false);
		Node div = createNode("div", 4, -1, 15, false, span);
		Node h1 = createNode("h1", 0, 15, 20, true, div);

		assertDocument("<h1><div><span></h1>", h1);
	}

	@Test
	public void testAttributeInElement() {
		Node div = createNode("div", 0, 17, 23, true);
		insertIntoAttributes(div, "key", "\"value\"");

		assertDocument("<div key=\"value\"></div>", div);
	}

	@Test
	public void testAttributesInElement() {
		Node div = createNode("div", 0, 30, 36, true);
		insertIntoAttributes(div, "key", "\"value\"");
		insertIntoAttributes(div, "key2", "\"value\"");

		assertDocument("<div key=\"value\" key2=\"value\"></div>", div);
	}

	@Test
	public void testAttributesInSelfClosingElement() {
		Node div = createNode("div", 0, -1, 31, true);
		insertIntoAttributes(div, "key", "\"value\"");
		insertIntoAttributes(div, "key2", "\"value\"");

		assertDocument("<div key=\"value\" key2=\"value\"/>", div);
	}

	@Test
	public void testAttributeEmptyValue() {
		Node div = createNode("div", 0, 12, 18, true);
		insertIntoAttributes(div, "key", "\"\"");

		assertDocument("<div key=\"\"></div>", div);
	}

	@Test
	public void testAttributeNoValue() {
		Node div = createNode("div", 0, 10, 16, true);
		insertIntoAttributes(div, "key", null);

		assertDocument("<div key=></div>", div);
	}

	@Test
	public void testAttributeNoClosingQuotation() {
		Node div = createNode("div", 0, -1, 22, false);
		insertIntoAttributes(div, "key", "\"value></div>");

		assertDocument("<div key=\"value></div>", div);
	}

	@Test
	public void testCDATABasicTest() {
		Node text = createCDATANode("testText", 5, -1, 25, true);
		Node div = createNode("div", 0, 25, 31, true, text);

		assertDocument("<div><![CDATA[testText]]></div>", div);
	}

	@Test
	public void testCDATAWithOtherElement() {
		Node text = createCDATANode("TEXT", 5, -1, 21, true);
		Node a = createNode("a", 21, 24, 28, true);
		Node div = createNode("div", 0, 28, 34, true, text, a);

		assertDocument("<div><![CDATA[TEXT]]><a></a></div>", div);
	}

	@Test
	public void testCDATANotClosedButNested() {
		Node text = createCDATANode("testText]</div>", 5, -1, 29, false);
		Node div = createNode("div", 0, -1, 29, false, text);

		assertDocument("<div><![CDATA[testText]</div>", div);
	}

	@Test
	public void testCDATANotClosedNotNested() {
		Node text = createCDATANode("testText]/div>", 5, -1, 28, false);
		Node div = createNode("div", 0, -1, 28, false, text);

		assertDocument("<div><![CDATA[testText]/div>", div);
	}

	@Test
	public void testCDATABasicWithAngledBracket() {
		Node text = createCDATANode("<>", 5, -1, 19, true);
		Node div = createNode("div", 0, 19, 25, true, text);

		assertDocument("<div><![CDATA[<>]]></div>", div);
	}

	@Test
	public void testClosedWithIncompleteEndTag() {

		Node div = createNode("div", 0, -1, 5, false);

		assertDocument("<div></divaaaz", div);
	}

	@Test
	public void testNonClosedAndIncomplete() {
		Node h = createNode("h", 14, -1, 16, false);
		Node hello = createNode("hello", 7, 16, 24, true, h);
		Node test1 = createNode("test1", 0, -1, 24, false, hello);
		assertDocument("<test1><hello><h</hello>", test1);
	}

	@Test
	public void testWithNewLineCharacters() {
		Node n = createNode("n", 6, 12, 16, true);
		Node t = createNode("t", 0, 17, 21, true, n);

		assertDocument("<t>\n  <n>\n  </n>\n</t>", t);
	}

	@Test
	public void testProlog() {
		Node html = createNode("html", 38, 44, 51, true);
		Node prolog = createPrologNode("xml", 0, -1, 38, true);
		insertIntoAttributes(prolog, "version", "\"1.0\"");
		insertIntoAttributes(prolog, "encoding", "\"UTF-8\"");

		assertDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", prolog);
	}

	@Test
	public void testPI() {
		Node processingInstruction = createPINode("m2e", 6, -1, 20, true, "he haa");
		Node html = createNode("html", 0, 20, 27, true, processingInstruction);

		assertDocument("<html><?m2e he haa?></html>", html);
	}

	@Test
	public void testPISpaces() {
		Node processingInstruction = createPINode("m2e", 6, -1, 28, true, "he haa");
		Node html = createNode("html", 0, 28, 35, true, processingInstruction);

		assertDocument("<html><?m2e    he haa     ?></html>", html);
	}

	@Test
	public void testPISpaces2() {
		Node processingInstruction = createPINode("m2e", 8, -1, 22, true, "he haa");
		Node html = createNode("html", 0, 24, 31, true, processingInstruction);

		assertDocument("<html>  <?m2e he haa?>  </html>", html);
	}

	@Test
	public void testPICloseToProlog() {
		Node processingInstruction = createPINode("xmll", 0, -1, 24, true, "this is content");

		assertDocument("<?xmll this is content?>", processingInstruction);
	}

	@Test
	public void testPINoContent() {
		Node processingInstruction = createPINode("m2e", 0, -1, 7, true, "");

		assertDocument("<?m2e?>", processingInstruction);
	}

	@Test
	public void testPINoContentButSpace() {
		Node processingInstruction = createPINode("m2e", 0, -1, 8, true, "");

		assertDocument("<?m2e ?>", processingInstruction);
	}

	@Test
	public void testPrologNoContent() {
		Node prolog = createPrologNode("xml", 0, -1, 7, true);

		assertDocument("<?xml?>", prolog);
	}

	@Test
	public void testPrologNoContentButSpace() {
		Node prolog = createPrologNode("xml", 0, -1, 8, true);

		assertDocument("<?xml ?>", prolog);
	}
	// --------------------------------------------------------------------------------
	// Tools

	public Node createRegularNode(String tag, int start, int endTagStart, int end, boolean closed, Node... children) {
		return createNode(tag, start, endTagStart, end, closed, children);
	}

	public Node createCDATANode(String content, int start, int endTagStart, int end, boolean closed, Node... children) {
		Node n = createNode("CDATA", start, endTagStart, end, closed, children);
		n.isCDATA = true;
		n.content = content;
		return n;
	}

	public Node createPrologNode(String tag, int start, int endTagStart, int end, boolean closed, Node... children) {
		Node n = createNode(tag, start, endTagStart, end, closed, children);
		n.isProlog = true;
		return n;
	}

	public Node createPINode(String tag, int start, int endTagStart, int end, boolean closed, String content,
			Node... children) {
		Node n = createNode(tag, start, endTagStart, end, closed, children);
		n.isProcessingInstruction = true;
		n.content = content;
		return n;
	}

	public Node createNode(String tag, int start, int endTagStart, int end, boolean closed, Node... children) {
		List<Node> newChildren = Arrays.asList(children);
		Node n = new Node(start, end, newChildren, null, null);
		setRestOfNode(n, tag, endTagStart, closed);
		return n;
	}

	private void setRestOfNode(Node n, String tag, int endTagStart, boolean closed) {
		n.tag = tag;
		n.endTagStart = endTagStart > -1 ? Integer.valueOf(endTagStart) : null;
		n.closed = closed;
	}

	private static void assertDocument(String input, Node expectedNode) {
		XMLDocument document = XMLParser.getInstance().parse(input, "uri", EnumSet.of(XMLParser.Flag.Content));
		Node actualNode = document.children.get(0);
		compareTrees(expectedNode, actualNode);
	}

	private static void assertFailedDocument(String input) {
		assertEquals(0, XMLParser.getInstance().parse(input, "uri").children.size());
	}

	private static void compareTrees(Node expectedNode, Node actualNode) {
		assertEquals(expectedNode.tag, actualNode.tag);
		assertEquals(expectedNode.start, actualNode.start);
		assertEquals(expectedNode.end, actualNode.end);
		assertEquals(expectedNode.getAttributes(), actualNode.getAttributes());

		if (expectedNode.endTagStart == null) {
			Assert.assertNull(actualNode.endTagStart);
		} else {
			assertEquals(expectedNode.endTagStart, actualNode.endTagStart);
		}
		assertEquals(expectedNode.closed, actualNode.closed);
		assertEquals(expectedNode.isCDATA, actualNode.isCDATA);
		assertEquals(expectedNode.isProlog, actualNode.isProlog);
		assertEquals(expectedNode.isProcessingInstruction, actualNode.isProcessingInstruction);
		if (expectedNode.isProcessingInstruction) {
			assertEquals(expectedNode.content, actualNode.content);
		}
		assertEquals(expectedNode.children.size(), actualNode.children.size());
		for (int i = 0; i < expectedNode.children.size(); i++) {
			compareTrees(expectedNode.children.get(i), actualNode.children.get(i));
		}
	}

	public void insertIntoAttributes(Node n, String key, String value) {
		n.setAttribute(key, value);
	}

	public XMLDocument getXMLDocument(String input) {
		return XMLParser.getInstance().parse(input, "uri");
	}
}
