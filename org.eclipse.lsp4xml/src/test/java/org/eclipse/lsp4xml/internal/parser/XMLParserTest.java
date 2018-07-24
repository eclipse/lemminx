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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
		Node html = createNode("html", 0, 6, 13, true, false);

		assertDocument("<html></html>", html);
	}

	@Test
	public void testNestedElement() {
		Node body = createNode("body", 6, 12, 19, true, false);
		Node html = createNode("html", 0, 19, 26, true, false, body);

		assertDocument("<html><body></body></html>", html);
	}

	@Test
	public void testNestedElements() {
		Node head = createNode("head", 6, 12, 19 , true, false); 
		Node body = createNode("body", 19, 25, 32, true, false); 
		Node html = createNode("html", 0, 32, 39, true, false, head,body);

		assertDocument("<html><head></head><body></body></html>", html);
	}

	@Test
	public void testNestedNestedElements() {
		Node c = createNode("c", 6, 9, 13, true, false); 
		Node b = createNode("b", 3, 13, 17, true, false, c);
		Node a = createNode("a", 0, 17, 21, true, false, b);
		assertDocument("<a><b><c></c></b></a>", a);
	}


	@Test
	public void testSelfClosing() {
		Node br = createNode("br", 0, -1, 5, true, false);

		assertDocument("<br/>", br);
	}

	@Test
	public void testNestedSelfClosingTag() {
		Node br = createNode("br", 5, -1, 10, true, false); 
		Node span = createNode("span", 10, 16 ,23, true, false); 
		Node div = createNode("div", 0, 23, 29, true, false, br, span);

		assertDocument("<div><br/><span></span></div>", div);
	}

	@Test
	public void testEmptyTagT() {
		Node br = createNode("br", 0, -1, 4, false, false);

		assertDocument("<br>", br);
	}

	@Test
	public void singleEndTag() {
		assertFailedDocument("</meta>");
	}

	@Test
	public void testEndTagInsideElement() {
		Node div = createNode("div", 0, 5, 11, true, false);
		assertDocument("<div></div><div>", div);
	}

	@Test
	public void testStartTagInsideElement() {
		Node div2 = createNode("div", 5, 10, 16, true, false); 
		Node div = createNode("div", 0, -1, 16, false, false, div2);

		assertDocument("<div><div></div>", div);
	}

	@Test 
	public void testStartTagInsideElement2() {
		Node div = createNode("div", 5, -1, 10, false, false); 
		Node cat = createNode("cat", 0, 10, 16, true, false, div);

		assertDocument("<cat><div></cat>", cat);
	}

	@Test 
	public void testMultipleStartTagInsideElement() {
		Node span = createNode("span", 9, -1, 15, false, false);  
		Node div = createNode("div", 4, -1, 15, false, false, span);
		Node h1 = createNode("h1", 0, 15, 20, true, false, div);
		
		assertDocument("<h1><div><span></h1>", h1);
	}

	@Test
	public void testAttributeInElement() {
		Node div = createNode("div", 0, 17, 23, true, false);
		insertIntoAttributes(div, "key", "\"value\"");

		assertDocument("<div key=\"value\"></div>", div);
	}

	@Test
	public void testAttributesInElement() {
		Node div = createNode("div", 0, 30, 36, true, false);
		insertIntoAttributes(div, "key", "\"value\"");
		insertIntoAttributes(div, "key2", "\"value\"");

		assertDocument("<div key=\"value\" key2=\"value\"></div>", div);
	}
	@Test
	public void testAttributesInSelfClosingElement() {
		Node div = createNode("div", 0, -1, 31, true, false);
		insertIntoAttributes(div, "key", "\"value\"");
		insertIntoAttributes(div, "key2", "\"value\"");

		assertDocument("<div key=\"value\" key2=\"value\"/>", div);
	}

	@Test
	public void testAttributeEmptyValue() {
		Node div = createNode("div", 0, 12, 18, true, false);
		insertIntoAttributes(div, "key", "\"\"");

		assertDocument("<div key=\"\"></div>", div);
	}

	@Test
	public void testAttributeNoValue() {
		Node div = createNode("div", 0, 10, 16, true, false);
		insertIntoAttributes(div, "key", null);

		assertDocument("<div key=></div>", div);
	}

	@Test
	public void testAttributeNoClosingQuotation() {
		Node div = createNode("div", 0, -1, 22, false, false);
		insertIntoAttributes(div, "key", "\"value></div>");

		assertDocument("<div key=\"value></div>", div);
	}
	
	@Test
	public void testCDATABasicTest() {
		Node text = createNode("testText", 5, -1, 25, true, true); 
		Node div = createNode("div", 0, 25, 31, true, false, text);

		assertDocument("<div><![CDATA[testText]]></div>", div);
	}
	
	@Test
	public void testCDATAWithOtherElement() {
		Node text = createNode("TEXT", 5, -1, 21, true, true); 
		Node a = createNode("a", 21, 24, 28, true, false); 
		Node div = createNode("div", 0, 28, 34, true, false, text, a);

		assertDocument("<div><![CDATA[TEXT]]><a></a></div>", div);
	}

	@Test
	public void testCDATABasicNotClosed() {
		Node text = createNode("testText]</div>", 5, -1, 29, false, true); 
		Node div = createNode("div", 0, -1, 29, false, false, text);

		assertDocument("<div><![CDATA[testText]</div>", div);
	}


	@Test
	public void testClosedWithIncompleteEndTag() {
		
		Node div = createNode("div", 0, -1, 5, false, false);

		assertDocument("<div></divaaaz", div);
	}

	@Test
	public void testNonClosedAndIncomplete() {
		Node h = createNode("h", 14, -1, 24, false, false);
		Node hello = createNode("hello", 7, -1, 24, false, false, h);
		Node test1= createNode("test1", 0, -1, 24, false, false, hello);
		assertDocument("<test1><hello><h</hello>", test1);
	}

	@Test
	public void testWithNewLineCharacters() {
		Node n = createNode("n", 6, 12, 16, true, false);
		Node t = createNode("t", 0, 17, 21, true, false, n);

		assertDocument("<t>\n  <n>\n  </n>\n</t>", t);
	}
	





	//--------------------------------------------------------------------------------
	//Tools

	public Node createNode(String tag, int start, int endTagStart, int end , boolean closed, boolean isCDATA, Node ... children) {
		List<Node> newChildren = Arrays.asList(children);
		Node n = new Node(start, end, newChildren, null, null);
		setRestOfNode(n, tag, endTagStart, closed, isCDATA);
		return n;
	}

	private void setRestOfNode(Node n, String tag, int endTagStart, boolean closed, boolean isCDATA) {
		n.tag = tag;
		n.endTagStart = endTagStart > -1 ? Integer.valueOf(endTagStart) : null;
		n.closed = closed;
		n.isCDATA = isCDATA;
	} 

	private static void assertDocument(String input, Node expectedNode) {
		XMLDocument document = XMLParser.getInstance().parse(input, "uri");
		Node actualNode = document.children.get(0);
		compareTrees(expectedNode, actualNode);
	}

	private static void assertFailedDocument(String input) {
		assertEquals(0, XMLParser.getInstance().parse(input, "uri").children.size());
	}

	private static void compareTrees(Node expectedNode, Node actualNode){
		assertEquals(expectedNode.tag, actualNode.tag);
		assertEquals(expectedNode.start, actualNode.start);
		assertEquals(expectedNode.end, actualNode.end);
		assertEquals(expectedNode.attributes, actualNode.attributes);
	
		if(expectedNode.endTagStart == null){
			Assert.assertNull(actualNode.endTagStart);
		}
		else{
			assertEquals(expectedNode.endTagStart, actualNode.endTagStart);
		}
		assertEquals(expectedNode.closed, actualNode.closed);
		assertEquals(expectedNode.isCDATA, actualNode.isCDATA);
		
		assertEquals(expectedNode.children.size(), actualNode.children.size());
		for(int i = 0; i < expectedNode.children.size(); i++) {
				compareTrees(expectedNode.children.get(i), actualNode.children.get(i));
		}
	}

	public void insertIntoAttributes(Node n, String key, String value){
		if(n.attributes == null) {
			n.attributes = new HashMap<>();
		}
		n.attributes.put(key, value);
	}

	public XMLDocument getXMLDocument(String input) {
		return XMLParser.getInstance().parse(input, "uri");
	}
}
