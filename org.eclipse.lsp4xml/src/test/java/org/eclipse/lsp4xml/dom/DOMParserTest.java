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

import org.eclipse.lsp4xml.dom.DOMDocumentType.DocumentTypeKind;
import org.junit.Assert;
import org.junit.Test;

/**
 * XML parser tests.
 *
 */
public class DOMParserTest {

	@Test
	public void testSingleElement() {
		DOMNode html = createElement("html", 0, 6, 13, true);

		assertDocument("<html></html>", html);
	}

	@Test
	public void testNestedElement() {
		DOMNode body = createElement("body", 6, 12, 19, true);
		DOMNode html = createElement("html", 0, 19, 26, true);
		html.addChild(body);

		assertDocument("<html><body></body></html>", html);
	}

	@Test
	public void testNestedElements() {
		DOMNode head = createElement("head", 6, 12, 19, true);
		DOMNode body = createElement("body", 19, 25, 32, true);
		DOMNode html = createElement("html", 0, 32, 39, true);
		html.addChild(head);
		html.addChild(body);

		assertDocument("<html><head></head><body></body></html>", html);
	}

	@Test
	public void testNestedNestedElements() {
		DOMNode c = createElement("c", 6, 9, 13, true);
		DOMNode b = createElement("b", 3, 13, 17, true);
		b.addChild(c);

		DOMNode a = createElement("a", 0, 17, 21, true);
		a.addChild(b);

		assertDocument("<a><b><c></c></b></a>", a);
	}

	@Test
	public void testSelfClosing() {
		DOMNode br = createElement("br", 0, null, 5, true);

		assertDocument("<br/>", br);
	}

	@Test
	public void testNestedSelfClosingTag() {
		DOMNode br = createElement("br", 5, null, 10, true);
		DOMNode span = createElement("span", 10, 16, 23, true);
		DOMNode div = createElement("div", 0, 23, 29, true);
		div.addChild(br);
		div.addChild(span);

		assertDocument("<div><br/><span></span></div>", div);
	}

	@Test
	public void testEmptyTagT() {
		DOMNode br = createElement("br", 0, null, 4, false);

		assertDocument("<br>", br);
	}

	@Test
	public void singleEndTag() {
		DOMElement meta = (DOMElement) createElement("meta", 0, 0, 7, false);
		assertDocument("</meta>", meta);
		Assert.assertFalse(meta.hasStartTag());
		Assert.assertTrue(meta.hasEndTag());
		Assert.assertNotNull(meta.getEndTagOpenOffset());
		Assert.assertEquals(meta.getEndTagOpenOffset().intValue(), 0); // |</meta>
	}

	@Test
	public void insideEndTag() {
		DOMElement meta = (DOMElement) createElement("meta", 6, 6, 13, false);
		DOMElement html = (DOMElement) createElement("html", 0, 13, 20, true);
		html.addChild(meta);

		assertDocument("<html></meta></html>", html);
		Assert.assertFalse(meta.hasStartTag());
		Assert.assertTrue(meta.hasEndTag());
		Assert.assertNotNull(meta.getEndTagOpenOffset());
		Assert.assertEquals(meta.getEndTagOpenOffset().intValue(), 6); // |</meta>
	}

	@Test
	public void testEndTagInsideElement() {
		DOMNode div = createElement("div", 0, 5, 11, true);
		assertDocument("<div></div><div>", div);
	}

	@Test
	public void testStartTagInsideElement() {
		DOMNode div2 = createElement("div", 5, 10, 16, true);
		DOMNode div = createElement("div", 0, null, 16, false);
		div.addChild(div2);

		assertDocument("<div><div></div>", div);
	}

	@Test
	public void testStartTagInsideElement2() {
		DOMNode div = createElement("div", 5, null, 10, false);
		DOMNode cat = createElement("cat", 0, 10, 16, true);
		cat.addChild(div);

		assertDocument("<cat><div></cat>", cat);
	}

	@Test
	public void testMultipleStartTagInsideElement() {
		DOMNode span = createElement("span", 9, null, 15, false);
		DOMNode div = createElement("div", 4, null, 15, false);
		div.addChild(span);
		DOMNode h1 = createElement("h1", 0, 15, 20, true);
		h1.addChild(div);

		assertDocument("<h1><div><span></h1>", h1);
	}

	@Test
	public void testAttributeInElement() {
		DOMElement div = createElement("div", 0, 17, 23, true);
		insertIntoAttributes(div, "key", "\"value\"");

		assertDocument("<div key=\"value\"></div>", div);
	}

	@Test
	public void testAttributesInElement() {
		DOMElement div = createElement("div", 0, 30, 36, true);
		insertIntoAttributes(div, "key", "\"value\"");
		insertIntoAttributes(div, "key2", "\"value\"");

		assertDocument("<div key=\"value\" key2=\"value\"></div>", div);
	}

	@Test
	public void testAttributesInSelfClosingElement() {
		DOMElement div = createElement("div", 0, null, 31, true);
		insertIntoAttributes(div, "key", "\"value\"");
		insertIntoAttributes(div, "key2", "\"value\"");

		assertDocument("<div key=\"value\" key2=\"value\"/>", div);
	}

	@Test
	public void testAttributeEmptyValue() {
		DOMElement div = createElement("div", 0, 12, 18, true);
		insertIntoAttributes(div, "key", "\"\"");

		assertDocument("<div key=\"\"></div>", div);
	}

	@Test
	public void testAttributeNoValue() {
		DOMElement div = createElement("div", 0, 10, 16, true);
		insertIntoAttributes(div, "key", null);

		assertDocument("<div key=></div>", div);
	}

	@Test
	public void testAttributeNoClosingQuotation() {
		DOMElement div = createElement("div", 0, null, 22, false);
		insertIntoAttributes(div, "key", "\"value></div>");

		assertDocument("<div key=\"value></div>", div);
	}

	@Test
	public void testCDATABasicTest() {
		DOMNode text = createCDATANode("testText", 5, 25, true);
		DOMNode div = createElement("div", 0, 25, 31, true);
		div.addChild(text);

		assertDocument("<div><![CDATA[testText]]></div>", div);
	}

	@Test
	public void testCDATAWithOtherElement() {
		DOMNode text = createCDATANode("TEXT", 5, 21, true);
		DOMNode a = createElement("a", 21, 24, 28, true);
		DOMNode div = createElement("div", 0, 28, 34, true);
		div.addChild(text);
		div.addChild(a);

		assertDocument("<div><![CDATA[TEXT]]><a></a></div>", div);
	}

	@Test
	public void testCDATANotClosedButNested() {
		DOMNode text = createCDATANode("testText]</div>", 5, 29, false);
		DOMNode div = createElement("div", 0, null, 29, false);
		div.addChild(text);

		assertDocument("<div><![CDATA[testText]</div>", div);
	}

	@Test
	public void testCDATANotClosedNotNested() {
		DOMNode text = createCDATANode("testText]/div>", 5, 28, false);
		DOMNode div = createElement("div", 0, null, 28, false);
		div.addChild(text);

		assertDocument("<div><![CDATA[testText]/div>", div);
	}

	@Test
	public void testCDATABasicWithAngledBracket() {
		DOMNode text = createCDATANode("<>", 5, 19, true);
		DOMNode div = createElement("div", 0, 19, 25, true);
		div.addChild(text);

		assertDocument("<div><![CDATA[<>]]></div>", div);
	}

	@Test
	public void testClosedWithIncompleteEndTag() {

		DOMNode div = createElement("div", 0, null, 14, false);
		DOMNode divaaaz = createElement("divaaaz", 5, 5, 14, false);
		div.addChild(divaaaz);

		assertDocument("<div></divaaaz", div);
	}

	@Test
	public void testNonClosedAndIncomplete() {
		DOMNode h = createElement("h", 14, null, 16, false);
		DOMNode hello = createElement("hello", 7, 16, 24, true);
		DOMNode test1 = createElement("test1", 0, null, 24, false);
		test1.addChild(hello);
		hello.addChild(h);

		assertDocument("<test1><hello><h</hello>", test1);
	}

	@Test
	public void testWithNewLineCharacters() {
		DOMNode n = createElement("n", 6, 12, 16, true);
		DOMNode t = createElement("t", 0, 17, 21, true);
		t.addChild(n);
		assertDocument("<t>\n  <n>\n  </n>\n</t>", t);
	}

	@Test
	public void testProlog() {
		ProcessingInstruction prolog = createPrologNode("xml", 0, 38, true);
		insertIntoAttributes(prolog, "version", "\"1.0\"");
		insertIntoAttributes(prolog, "encoding", "\"UTF-8\"");

		assertDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", prolog);
	}

	@Test
	public void testPI() {
		DOMNode processingInstruction = createPINode("m2e", 6, 20, true, "he haa");
		DOMNode html = createElement("html", 0, 20, 27, true);
		html.addChild(processingInstruction);

		assertDocument("<html><?m2e he haa?></html>", html);
	}

	@Test
	public void testPISpaces() {
		DOMNode processingInstruction = createPINode("m2e", 6, 28, true, "he haa");
		DOMNode html = createElement("html", 0, 28, 35, true);
		html.addChild(processingInstruction);

		assertDocument("<html><?m2e    he haa     ?></html>", html);
	}

	@Test
	public void testPISpaces2() {
		DOMNode processingInstruction = createPINode("m2e", 8, 22, true, "he haa");
		DOMNode html = createElement("html", 0, 24, 31, true);
		html.addChild(processingInstruction);

		assertDocument("<html>  <?m2e he haa?>  </html>", html);
	}

	@Test
	public void testPICloseToProlog() {
		DOMNode processingInstruction = createPINode("xmll", 0, 24, true, "this is content");

		assertDocument("<?xmll this is content?>", processingInstruction);
	}

	@Test
	public void testPINoContent() {
		DOMNode processingInstruction = createPINode("m2e", 0, 7, true, "");

		assertDocument("<?m2e?>", processingInstruction);
	}

	@Test
	public void testPINoContentButSpace() {
		DOMNode processingInstruction = createPINode("m2e", 0, 8, true, "");

		assertDocument("<?m2e ?>", processingInstruction);
	}

	@Test
	public void testPrologNoContent() {
		DOMNode prolog = createPrologNode("xml", 0, 7, true);

		assertDocument("<?xml?>", prolog);
	}

	@Test
	public void testPrologNoContentButSpace() {
		DOMNode prolog = createPrologNode("xml", 0, 8, true);

		assertDocument("<?xml ?>", prolog);
	}

	@Test
	public void testCommentSingle() {
		DOMNode comment = createCommentNode(" test ", 0, 13, true);

		assertDocument("<!-- test -->", comment);
	}

	@Test
	public void testContentTextHasTag() {
		DOMNode textNode = createTextNode("  eek  ", 6, 13, true);
		DOMNode html = createElement("html", 0, 13, 20, true);
		html.addChild(textNode);

		assertDocument("<html>  eek  </html>", html);
	}

	@Test
	public void elementOffsets() {
		DOMDocument document = DOMParser.getInstance().parse("<a></a>", null, null);
		DOMElement a = document.getDocumentElement();
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

	@Test
	public void assertDoctype1() {
		String xml = 
		"<!DOCTYPE note [\n" +
		"  <!ENTITY nbsp \"&#xA0;\"> \n" +
		"  <!ENTITY writer \"Writer: Donald Duck.\">\n" +
		"  <!ENTITY copyright \"Copyright: W3Schools.\">\n" +
		"]>";
		String internal = 
		"\n" +
		"  <!ENTITY nbsp \"&#xA0;\"> \n" +
		"  <!ENTITY writer \"Writer: Donald Duck.\">\n" +
		"  <!ENTITY copyright \"Copyright: W3Schools.\">\n";
		DOMDocument document = DOMParser.getInstance().parse(xml, null, null);
		assertDoctype((DOMDocumentType)(document.getChild(0)), 0, 134, "note", null, null, null, internal);
	}
	
	@Test
	public void assertDoctype2() {
		String xml = 
		"<!DOCTYPE html SYSTEM\n" +
		"  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"\n" +
		"  [\n" +
		"    <!ENTITY nbsp \"&#xA0;\"> \n" +
		"    <!ENTITY writer \"Writer: Donald Duck.\">\n" +
		"    <!ENTITY copyright \"Copyright: W3Schools.\">\n" +
		"  ]\n" +
		">";
		String internal = 
		"\n" +
		"    <!ENTITY nbsp \"&#xA0;\"> \n" +
		"    <!ENTITY writer \"Writer: Donald Duck.\">\n" +
		"    <!ENTITY copyright \"Copyright: W3Schools.\">\n  ";
		DOMDocument document = DOMParser.getInstance().parse(xml, null, null);
		assertDoctype((DOMDocumentType)(document.getChild(0)), 0, 212, "html", DocumentTypeKind.SYSTEM, null, "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd", internal);
	}

	// --------------------------------------------------------------------------------
	// Tools

	private static DOMCDataSection createCDATANode(String content, int start, int end, boolean closed) {
		MockCDataSection n = (MockCDataSection) createNode(DOMNode.CDATA_SECTION_NODE, null, start, null, end, closed);
		n.content = content;
		return n;
	}

	private static DOMComment createCommentNode(String content, int start, int end, boolean closed) {
		MockComment n = (MockComment) createNode(DOMNode.COMMENT_NODE, null, start, null, end, closed);
		n.content = content;
		return n;
	}

	private static DOMText createTextNode(String content, int start, int end, boolean closed) {
		MockText n = (MockText) createNode(DOMNode.TEXT_NODE, null, start, null, end, closed);
		n.content = content;
		return n;
	}

	private static ProcessingInstruction createPrologNode(String tag, int start, int end, boolean closed) {
		ProcessingInstruction n = (ProcessingInstruction) createNode(DOMNode.PROCESSING_INSTRUCTION_NODE, tag, start, null,
				end, closed);
		n.prolog = true;
		return n;
	}

	private static DOMNode createPINode(String tag, int start, int end, boolean closed, String content) {
		MockProcessingInstruction n = (MockProcessingInstruction) createNode(DOMNode.PROCESSING_INSTRUCTION_NODE, tag,
				start, null, end, closed);
		n.content = content;
		n.processingInstruction = true;
		return n;
	}

	private static DOMElement createElement(String tag, int start, Integer endTagStart, int end, boolean closed) {
		return (DOMElement) createNode(DOMNode.ELEMENT_NODE, tag, start, endTagStart, end, closed);
	}

	private static DOMNode createNode(short nodeType, String tag, int start, Integer endTagStart, int end,
			boolean closed) {
		DOMNode n = createNode(nodeType, start, end);
		setRestOfNode(n, tag, endTagStart, closed);
		return n;
	}

	private static void assertDoctype(DOMDocumentType doctype, int start, int end, String name, DocumentTypeKind kind, String publicId, String systemId, String internalDTD) {
		assertEquals(start, doctype.getStart());
		assertEquals(end, doctype.getEnd());
		assertEquals(name, doctype.getName());
		assertEquals(kind, doctype.getKind());
		assertEquals(publicId, doctype.getPublicId());
		assertEquals(systemId, doctype.getSystemId());
		assertEquals(internalDTD, doctype.getInternalDTD());
	}

	private static class MockProcessingInstruction extends ProcessingInstruction {

		public String content;

		public MockProcessingInstruction(int start, int end, DOMDocument ownerDocument) {
			super(start, end, ownerDocument);
		}

		@Override
		public String getData() {
			return content;
		}
	}

	private static class MockCDataSection extends DOMCDataSection {

		public String content;

		public MockCDataSection(int start, int end, DOMDocument ownerDocument) {
			super(start, end, ownerDocument);
		}

		@Override
		public String getData() {
			return content;
		}
	}

	private static class MockText extends DOMText {

		public String content;

		public MockText(int start, int end, DOMDocument ownerDocument) {
			super(start, end, ownerDocument);
		}

		@Override
		public String getData() {
			return content;
		}
	}

	private static class MockComment extends DOMComment {

		public String content;

		public MockComment(int start, int end, DOMDocument ownerDocument) {
			super(start, end, ownerDocument);
		}

		@Override
		public String getData() {
			return content;
		}
	}

	private static class MockNode extends DOMNode {

		public MockNode(int start, int end, DOMDocument ownerDocument) {
			super(start, end, ownerDocument);
		}

		@Override
		public String getNodeName() {
			return null;
		}

		@Override
		public short getNodeType() {
			return 0;
		}

	}

	private static DOMNode createNode(short nodeType, int start, int end) {
		switch (nodeType) {
		case DOMNode.ELEMENT_NODE:
			return new DOMElement(start, end, null);
		case DOMNode.PROCESSING_INSTRUCTION_NODE:
			return new MockProcessingInstruction(start, end, null);
		case DOMNode.CDATA_SECTION_NODE:
			return new MockCDataSection(start, end, null);
		case DOMNode.TEXT_NODE:
			return new MockText(start, end, null);
		case DOMNode.COMMENT_NODE:
			return new MockComment(start, end, null);
		}
		return new MockNode(start, end, null);
	}

	private static void setRestOfNode(DOMNode n, String tag, Integer endTagStart, boolean closed) {
		if (n.isElement()) {
			((DOMElement) n).tag = tag;
			((DOMElement) n).endTagOpenOffset = endTagStart;
		} else if (n instanceof ProcessingInstruction) {
			((ProcessingInstruction) n).target = tag;
			((ProcessingInstruction) n).endTagOpenOffset = endTagStart;
		}
		n.closed = closed;
	}

	private static void assertDocument(String input, DOMNode expectedNode) {
		DOMDocument document = DOMParser.getInstance().parse(input, "uri", null);
		DOMNode actualNode = document.getChild(0);
		compareTrees(expectedNode, actualNode);
	}

	private static void compareTrees(DOMNode expectedNode, DOMNode actualNode) {
		if (expectedNode.isElement()) {
			assertEquals(((DOMElement) expectedNode).getTagName(), ((DOMElement) actualNode).getTagName());
			assertEquals(((DOMElement) expectedNode).getEndTagOpenOffset(), ((DOMElement) actualNode).getEndTagOpenOffset());
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
			assertEquals(((DOMCharacterData) expectedNode).getData(), ((DOMCharacterData) actualNode).getData());
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

	public void insertIntoAttributes(DOMNode n, String key, String value) {
		n.setAttribute(key, value);
	}

	public DOMDocument getXMLDocument(String input) {
		return DOMParser.getInstance().parse(input, "uri", null);
	}
}
