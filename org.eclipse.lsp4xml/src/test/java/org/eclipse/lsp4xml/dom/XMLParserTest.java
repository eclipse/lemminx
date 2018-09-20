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
package org.eclipse.lsp4xml.dom;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

/**
 * XML parser tests.
 *
 */
public class XMLParserTest {

	@Test
	public void testSingleElement() {
		Node html = createNode(Node.ELEMENT_NODE, "html", 0, 6, 13, true);

		assertDocument("<html></html>", html);
	}

	@Test
	public void testNestedElement() {
		Node body = createNode(Node.ELEMENT_NODE, "body", 6, 12, 19, true);
		Node html = createNode(Node.ELEMENT_NODE, "html", 0, 19, 26, true);
		html.addChild(body);

		assertDocument("<html><body></body></html>", html);
	}

	@Test
	public void testNestedElements() {
		Node head = createNode(Node.ELEMENT_NODE, "head", 6, 12, 19, true);
		Node body = createNode(Node.ELEMENT_NODE, "body", 19, 25, 32, true);
		Node html = createNode(Node.ELEMENT_NODE, "html", 0, 32, 39, true);
		html.addChild(head);
		html.addChild(body);

		assertDocument("<html><head></head><body></body></html>", html);
	}

	@Test
	public void testNestedNestedElements() {
		Node c = createNode(Node.ELEMENT_NODE, "c", 6, 9, 13, true);
		Node b = createNode(Node.ELEMENT_NODE, "b", 3, 13, 17, true);
		b.addChild(c);

		Node a = createNode(Node.ELEMENT_NODE, "a", 0, 17, 21, true);
		a.addChild(b);

		assertDocument("<a><b><c></c></b></a>", a);
	}

	@Test
	public void testSelfClosing() {
		Node br = createNode(Node.ELEMENT_NODE, "br", 0, null, 5, true);

		assertDocument("<br/>", br);
	}

	@Test
	public void testNestedSelfClosingTag() {
		Node br = createNode(Node.ELEMENT_NODE, "br", 5, null, 10, true);
		Node span = createNode(Node.ELEMENT_NODE, "span", 10, 16, 23, true);
		Node div = createNode(Node.ELEMENT_NODE, "div", 0, 23, 29, true);
		div.addChild(br);
		div.addChild(span);

		assertDocument("<div><br/><span></span></div>", div);
	}

	@Test
	public void testEmptyTagT() {
		Node br = createNode(Node.ELEMENT_NODE, "br", 0, null, 4, false);

		assertDocument("<br>", br);
	}

	@Test
	public void singleEndTag() {
		Element meta = (Element) createNode(Node.ELEMENT_NODE, "meta", 0, 0, 7, false);
		assertDocument("</meta>", meta);
		Assert.assertFalse(meta.hasStartTag());
		Assert.assertTrue(meta.hasEndTag());
		Assert.assertNotNull(meta.getEndTagOpenOffset());
		Assert.assertEquals(meta.getEndTagOpenOffset().intValue(), 0); // |</meta>
	}

	@Test
	public void insideEndTag() {		
		Element meta = (Element) createNode(Node.ELEMENT_NODE, "meta", 6, 6, 13, false);
		Element html = (Element) createNode(Node.ELEMENT_NODE, "html", 0, 13, 20, true);
		html.addChild(meta);
		
		assertDocument("<html></meta></html>", html);
		Assert.assertFalse(meta.hasStartTag());
		Assert.assertTrue(meta.hasEndTag());
		Assert.assertNotNull(meta.getEndTagOpenOffset());
		Assert.assertEquals(meta.getEndTagOpenOffset().intValue(), 6); // |</meta>
	}
	
	@Test
	public void testEndTagInsideElement() {
		Node div = createNode(Node.ELEMENT_NODE, "div", 0, 5, 11, true);
		assertDocument("<div></div><div>", div);
	}

	@Test
	public void testStartTagInsideElement() {
		Node div2 = createNode(Node.ELEMENT_NODE, "div", 5, 10, 16, true);
		Node div = createNode(Node.ELEMENT_NODE, "div", 0, null, 16, false);
		div.addChild(div2);

		assertDocument("<div><div></div>", div);
	}

	@Test
	public void testStartTagInsideElement2() {
		Node div = createNode(Node.ELEMENT_NODE, "div", 5, null, 10, false);
		Node cat = createNode(Node.ELEMENT_NODE, "cat", 0, 10, 16, true);
		cat.addChild(div);

		assertDocument("<cat><div></cat>", cat);
	}

	@Test
	public void testMultipleStartTagInsideElement() {
		Node span = createNode(Node.ELEMENT_NODE, "span", 9, null, 15, false);
		Node div = createNode(Node.ELEMENT_NODE, "div", 4, null, 15, false);
		div.addChild(span);
		Node h1 = createNode(Node.ELEMENT_NODE, "h1", 0, 15, 20, true);
		h1.addChild(div);

		assertDocument("<h1><div><span></h1>", h1);
	}

	@Test
	public void testAttributeInElement() {
		Node div = createNode(Node.ELEMENT_NODE, "div", 0, 17, 23, true);
		insertIntoAttributes(div, "key", "\"value\"");

		assertDocument("<div key=\"value\"></div>", div);
	}

	@Test
	public void testAttributesInElement() {
		Node div = createNode(Node.ELEMENT_NODE, "div", 0, 30, 36, true);
		insertIntoAttributes(div, "key", "\"value\"");
		insertIntoAttributes(div, "key2", "\"value\"");

		assertDocument("<div key=\"value\" key2=\"value\"></div>", div);
	}

	@Test
	public void testAttributesInSelfClosingElement() {
		Node div = createNode(Node.ELEMENT_NODE, "div", 0, null, 31, true);
		insertIntoAttributes(div, "key", "\"value\"");
		insertIntoAttributes(div, "key2", "\"value\"");

		assertDocument("<div key=\"value\" key2=\"value\"/>", div);
	}

	@Test
	public void testAttributeEmptyValue() {
		Node div = createNode(Node.ELEMENT_NODE, "div", 0, 12, 18, true);
		insertIntoAttributes(div, "key", "\"\"");

		assertDocument("<div key=\"\"></div>", div);
	}

	@Test
	public void testAttributeNoValue() {
		Node div = createNode(Node.ELEMENT_NODE, "div", 0, 10, 16, true);
		insertIntoAttributes(div, "key", null);

		assertDocument("<div key=></div>", div);
	}

	@Test
	public void testAttributeNoClosingQuotation() {
		Node div = createNode(Node.ELEMENT_NODE, "div", 0, null, 22, false);
		insertIntoAttributes(div, "key", "\"value></div>");

		assertDocument("<div key=\"value></div>", div);
	}

	@Test
	public void testCDATABasicTest() {
		Node text = createCDATANode("testText", 5, 25, true);
		Node div = createNode(Node.ELEMENT_NODE, "div", 0, 25, 31, true);
		div.addChild(text);

		assertDocument("<div><![CDATA[testText]]></div>", div);
	}

	@Test
	public void testCDATAWithOtherElement() {
		Node text = createCDATANode("TEXT", 5, 21, true);
		Node a = createNode(Node.ELEMENT_NODE, "a", 21, 24, 28, true);
		Node div = createNode(Node.ELEMENT_NODE, "div", 0, 28, 34, true);
		div.addChild(text);
		div.addChild(a);

		assertDocument("<div><![CDATA[TEXT]]><a></a></div>", div);
	}

	@Test
	public void testCDATANotClosedButNested() {
		Node text = createCDATANode("testText]</div>", 5, 29, false);
		Node div = createNode(Node.ELEMENT_NODE, "div", 0, null, 29, false);
		div.addChild(text);

		assertDocument("<div><![CDATA[testText]</div>", div);
	}

	@Test
	public void testCDATANotClosedNotNested() {
		Node text = createCDATANode("testText]/div>", 5, 28, false);
		Node div = createNode(Node.ELEMENT_NODE, "div", 0, null, 28, false);
		div.addChild(text);

		assertDocument("<div><![CDATA[testText]/div>", div);
	}

	@Test
	public void testCDATABasicWithAngledBracket() {
		Node text = createCDATANode("<>", 5, 19, true);
		Node div = createNode(Node.ELEMENT_NODE, "div", 0, 19, 25, true);
		div.addChild(text);

		assertDocument("<div><![CDATA[<>]]></div>", div);
	}

	@Test
	public void testClosedWithIncompleteEndTag() {

		Node div = createNode(Node.ELEMENT_NODE, "div", 0, null, 14, false);
		Node divaaaz = createNode(Node.ELEMENT_NODE, "divaaaz", 5, 5, 14, false);
		div.addChild(divaaaz);

		assertDocument("<div></divaaaz", div);
	}

	@Test
	public void testNonClosedAndIncomplete() {
		Node h = createNode(Node.ELEMENT_NODE, "h", 14, null, 16, false);
		Node hello = createNode(Node.ELEMENT_NODE, "hello", 7, 16, 24, true);
		Node test1 = createNode(Node.ELEMENT_NODE, "test1", 0, null, 24, false);
		test1.addChild(hello);
		hello.addChild(h);

		assertDocument("<test1><hello><h</hello>", test1);
	}

	@Test
	public void testWithNewLineCharacters() {
		Node n = createNode(Node.ELEMENT_NODE, "n", 6, 12, 16, true);
		Node t = createNode(Node.ELEMENT_NODE, "t", 0, 17, 21, true);
		t.addChild(n);
		assertDocument("<t>\n  <n>\n  </n>\n</t>", t);
	}

	@Test
	public void testProlog() {
		Node prolog = createPrologNode("xml", 0, 38, true);
		insertIntoAttributes(prolog, "version", "\"1.0\"");
		insertIntoAttributes(prolog, "encoding", "\"UTF-8\"");

		assertDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", prolog);
	}

	@Test
	public void testPI() {
		Node processingInstruction = createPINode("m2e", 6, 20, true, "he haa");
		Node html = createNode(Node.ELEMENT_NODE, "html", 0, 20, 27, true);
		html.addChild(processingInstruction);

		assertDocument("<html><?m2e he haa?></html>", html);
	}

	@Test
	public void testPISpaces() {
		Node processingInstruction = createPINode("m2e", 6, 28, true, "he haa");
		Node html = createNode(Node.ELEMENT_NODE, "html", 0, 28, 35, true);
		html.addChild(processingInstruction);

		assertDocument("<html><?m2e    he haa     ?></html>", html);
	}

	@Test
	public void testPISpaces2() {
		Node processingInstruction = createPINode("m2e", 8, 22, true, "he haa");
		Node html = createNode(Node.ELEMENT_NODE, "html", 0, 24, 31, true);
		html.addChild(processingInstruction);

		assertDocument("<html>  <?m2e he haa?>  </html>", html);
	}

	@Test
	public void testPICloseToProlog() {
		Node processingInstruction = createPINode("xmll", 0, 24, true, "this is content");

		assertDocument("<?xmll this is content?>", processingInstruction);
	}

	@Test
	public void testPINoContent() {
		Node processingInstruction = createPINode("m2e", 0, 7, true, "");

		assertDocument("<?m2e?>", processingInstruction);
	}

	@Test
	public void testPINoContentButSpace() {
		Node processingInstruction = createPINode("m2e", 0, 8, true, "");

		assertDocument("<?m2e ?>", processingInstruction);
	}

	@Test
	public void testPrologNoContent() {
		Node prolog = createPrologNode("xml", 0, 7, true);

		assertDocument("<?xml?>", prolog);
	}

	@Test
	public void testPrologNoContentButSpace() {
		Node prolog = createPrologNode("xml", 0, 8, true);

		assertDocument("<?xml ?>", prolog);
	}

	@Test
	public void testCommentSingle() {
		Node comment = createCommentNode(" test ", 0, 13, true);

		assertDocument("<!-- test -->", comment);
	}

	@Test
	public void testContentTextHasTag() {
		Node textNode = createTextNode("  eek  ", 6, 13, true);
		Node html = createNode(Node.ELEMENT_NODE, "html", 0, 13, 20, true);
		html.addChild(textNode);

		assertDocument("<html>  eek  </html>", html);
	}

	@Test
	public void elementOffsets() {
		XMLDocument document = XMLParser.getInstance().parse("<a></a>", null);
		Element a = document.getDocumentElement();
		Assert.assertNotNull(a);
		Assert.assertEquals(a.getTagName(), "a");
		Assert.assertEquals(a.getStart(), 0); // |<a></a>
		Assert.assertNotNull(a.getStartTagOpenOffset()); // |<a></a>
		Assert.assertEquals(a.getStartTagOpenOffset().intValue(), 0); // |<a></a>
		Assert.assertNotNull(a.getStartTagCloseOffset()); // <a|></a>
		Assert.assertEquals(a.getStartTagCloseOffset().intValue(), 2); // <a|></a>
		Assert.assertEquals(a.getEndTagOpenOffset().intValue(), 3); // <a>|</a>
		Assert.assertEquals(a.getEnd(), 7); // <a></a>|

		Assert.assertFalse(a.isInStartTag(0)); // |<a></a>
		Assert.assertTrue(a.isInStartTag(1)); // <|a></a>
		Assert.assertTrue(a.isInStartTag(2)); // <a|></a>
		Assert.assertFalse(a.isInStartTag(3)); // <a>|</a>
	}

	// --------------------------------------------------------------------------------
	// Tools

	private static Node createCDATANode(String content, int start, int end, boolean closed) {
		MockCDataSection n = (MockCDataSection) createNode(Node.CDATA_SECTION_NODE, null, start, null, end, closed);
		n.content = content;
		return n;
	}

	private static Node createCommentNode(String content, int start, int end, boolean closed) {
		MockComment n = (MockComment) createNode(Node.COMMENT_NODE, null, start, null, end, closed);
		n.content = content;
		return n;
	}

	private static Node createTextNode(String content, int start, int end, boolean closed) {
		MockText n = (MockText) createNode(Node.TEXT_NODE, null, start, null, end, closed);
		n.content = content;
		return n;
	}

	private static Node createPrologNode(String tag, int start, int end, boolean closed) {
		ProcessingInstruction n = (ProcessingInstruction) createNode(Node.PROCESSING_INSTRUCTION_NODE, tag, start, null,
				end, closed);
		n.prolog = true;
		return n;
	}

	private static Node createPINode(String tag, int start, int end, boolean closed, String content) {
		MockProcessingInstruction n = (MockProcessingInstruction) createNode(Node.PROCESSING_INSTRUCTION_NODE, tag,
				start, null, end, closed);
		n.content = content;
		n.processingInstruction = true;
		return n;
	}

	private static Node createNode(short nodeType, String tag, int start, Integer endTagStart, int end,
			boolean closed) {
		Node n = createNode(nodeType, start, end);
		setRestOfNode(n, tag, endTagStart, closed);
		return n;
	}

	private static class MockProcessingInstruction extends ProcessingInstruction {

		public String content;

		public MockProcessingInstruction(int start, int end, XMLDocument ownerDocument) {
			super(start, end, ownerDocument);
		}

		@Override
		public String getData() {
			return content;
		}
	}

	private static class MockCDataSection extends CDataSection {

		public String content;

		public MockCDataSection(int start, int end, XMLDocument ownerDocument) {
			super(start, end, ownerDocument);
		}

		@Override
		public String getData() {
			return content;
		}
	}

	private static class MockText extends Text {

		public String content;

		public MockText(int start, int end, XMLDocument ownerDocument) {
			super(start, end, ownerDocument);
		}

		@Override
		public String getData() {
			return content;
		}
	}

	private static class MockComment extends Comment {

		public String content;

		public MockComment(int start, int end, XMLDocument ownerDocument) {
			super(start, end, ownerDocument);
		}

		@Override
		public String getData() {
			return content;
		}
	}

	private static Node createNode(short nodeType, int start, int end) {
		switch (nodeType) {
		case Node.ELEMENT_NODE:
			return new Element(start, end, null);
		case Node.PROCESSING_INSTRUCTION_NODE:
			return new MockProcessingInstruction(start, end, null);
		case Node.CDATA_SECTION_NODE:
			return new MockCDataSection(start, end, null);
		case Node.TEXT_NODE:
			return new MockText(start, end, null);
		case Node.COMMENT_NODE:
			return new MockComment(start, end, null);
		}
		return new Node(start, end, null);
	}

	private static void setRestOfNode(Node n, String tag, Integer endTagStart, boolean closed) {
		if (n.isElement()) {
			((Element) n).tag = tag;
			((Element) n).endTagOpenOffset = endTagStart;
		} else if (n instanceof ProcessingInstruction) {
			((ProcessingInstruction) n).target = tag;
			((ProcessingInstruction) n).endTagOpenOffset = endTagStart;
		}
		n.closed = closed;
	}

	private static void assertDocument(String input, Node expectedNode) {
		XMLDocument document = XMLParser.getInstance().parse(input, "uri");
		Node actualNode = document.getChild(0);
		compareTrees(expectedNode, actualNode);
	}

	private static void compareTrees(Node expectedNode, Node actualNode) {
		if (expectedNode.isElement()) {
			assertEquals(((Element) expectedNode).getTagName(), ((Element) actualNode).getTagName());
			assertEquals(((Element) expectedNode).getEndTagOpenOffset(), ((Element) actualNode).getEndTagOpenOffset());
		} else if (expectedNode.isProcessingInstruction() || expectedNode.isProlog()) {
			assertEquals(((ProcessingInstruction) expectedNode).getTarget(),
					((ProcessingInstruction) actualNode).getTarget());
			assertEquals(((ProcessingInstruction) expectedNode).getEndTagStart(),
					((ProcessingInstruction) actualNode).getEndTagStart());
		}
		assertEquals(expectedNode.start, actualNode.start);
		assertEquals(expectedNode.end, actualNode.end);
		assertEquals(expectedNode.getAttributeNodes(), actualNode.getAttributeNodes());

		if (expectedNode.isCharacterData()) {
			assertEquals(((CharacterData) expectedNode).getData(), ((CharacterData) actualNode).getData());
		}
		assertEquals(expectedNode.isClosed(), actualNode.isClosed());
		assertEquals(expectedNode.isCDATA(), actualNode.isCDATA());
		assertEquals(expectedNode.isProcessingInstruction(), actualNode.isProcessingInstruction());
		assertEquals(expectedNode.isProlog(), actualNode.isProlog());

		assertEquals(expectedNode.getChildren().size(), actualNode.getChildren().size());
		for (int i = 0; i < expectedNode.getChildren().size(); i++) {
			compareTrees(expectedNode.getChild(i), actualNode.getChild(i));
		}
	}

	public void insertIntoAttributes(Node n, String key, String value) {
		n.setAttribute(key, value);
	}

	public XMLDocument getXMLDocument(String input) {
		return XMLParser.getInstance().parse(input, "uri");
	}
}
