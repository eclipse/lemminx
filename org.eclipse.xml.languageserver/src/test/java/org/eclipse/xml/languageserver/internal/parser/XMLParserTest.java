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
package org.eclipse.xml.languageserver.internal.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.xml.languageserver.model.Node;
import org.eclipse.xml.languageserver.model.XMLDocument;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XML parser tests.
 *
 */
public class XMLParserTest {
	Node currNode;
	List<Node> tempChildren;


	@Before
	public void startup() {
		if(tempChildren != null) newChildrenArray();
		currNode = null;
	}


	@Test
	public void testSingleElement() {
		currNode = createNode("html", 0, 6, 13, true, false);

		assertDocument("<html></html>", currNode);
	}

	@Test
	public void testNestedElement() {
		currNode = createNode("body", 6, 12, 19, true, false);
		addCurrNode();
		currNode = createNode("html", 0, 19, 26, true, false, tempChildren);

		assertDocument("<html><body></body></html>", currNode);
	}

	@Test
	public void testNestedElements() {
		currNode = createNode("head", 6, 12, 19 , true, false); addCurrNode();
		currNode = createNode("body", 19, 25, 32, true, false); addCurrNode();
		currNode = createNode("html", 0, 32, 39, true, false, tempChildren);

		assertDocument("<html><head></head><body></body></html>", currNode);
	}

	@Test
	public void testNestedNestedElements() {
		currNode = createNode("c", 6, 9, 13, true, false); addCurrNode();
		currNode = createNode("b", 3, 13, 17, true, false, tempChildren); newChildrenArray(); addCurrNode();
		currNode = createNode("a", 0, 17, 21, true, false, tempChildren);
		assertDocument("<a><b><c></c></b></a>", currNode);
	}


	@Test
	public void testSelfClosing() {
		currNode = createNode("br", 0, -1, 5, true, false);

		assertDocument("<br/>", currNode);
	}

	@Test
	public void testSelfClosingCompex() {
		currNode = createNode("br", 5, -1, 10, true, false); addCurrNode();
		currNode = createNode("span", 10, 16 ,23, true, false); addCurrNode();
		currNode = createNode("div", 0, 23, 29, true, false, tempChildren);

		assertDocument("<div><br/><span></span></div>", currNode);
	}

	@Test
	public void testEmptyTagT() {
		currNode = createNode("br", 0, -1, 4, false, false);

		assertDocument("<br>", currNode);
	}

	@Test
	public void singleEndTag() {
		assertFailedDocument("</meta>");
	}

	@Test
	public void testEndTagInsideElement() {
		currNode = createNode("div", 0, 5, 11, true, false);
		assertDocument("<div></div><div>", currNode);
	}

	@Test
	public void testStartTagInsideElement() {
		currNode = createNode("div", 5, 10, 16, true, false); addCurrNode();
		currNode = createNode("div", 0, -1, 16, false, false, tempChildren);

		assertDocument("<div><div></div>", currNode);
	}

	@Test 
	public void testStartTagInsideElement2() {
		currNode = createNode("div", 5, -1, 10, false, false); addCurrNode();
		currNode = createNode("cat", 0, 10, 16, true, false, tempChildren);

		assertDocument("<cat><div></cat>", currNode);
	}

	@Test 
	public void testMultipleStartTagInsideElement() {
		currNode = createNode("span", 9, -1, 15, false, false);  addCurrNode();
		currNode = createNode("div", 4, -1, 15, false, false, tempChildren); newChildrenArray(); addCurrNode();
		currNode = createNode("h1", 0, 15, 20, true, false, tempChildren);
		
		assertDocument("<h1><div><span></h1>", currNode);
	}

	@Test
	public void testAttributeInElement() {
		currNode = createNode("div", 0, 17, 23, true, false);
		insertIntoAttributes("key", "\"value\"");

		assertDocument("<div key=\"value\"></div>", currNode);
	}

	@Test
	public void testAttributesInElement() {
		currNode = createNode("div", 0, 30, 36, true, false);
		insertIntoAttributes("key", "\"value\"");
		insertIntoAttributes("key2", "\"value\"");

		assertDocument("<div key=\"value\" key2=\"value\"></div>", currNode);
	}
	@Test
	public void testAttributesInSelfClosingElement() {
		currNode = createNode("div", 0, -1, 31, true, false);
		insertIntoAttributes("key", "\"value\"");
		insertIntoAttributes("key2", "\"value\"");

		assertDocument("<div key=\"value\" key2=\"value\"/>", currNode);
	}

	@Test
	public void testAttributeEmptyValue() {
		currNode = createNode("div", 0, 12, 18, true, false);
		insertIntoAttributes("key", "\"\"");

		assertDocument("<div key=\"\"></div>", currNode);
	}

	@Test
	public void testAttributeNoValue() {
		currNode = createNode("div", 0, 10, 16, true, false);
		insertIntoAttributes("key", null);

		assertDocument("<div key=></div>", currNode);
	}

	@Test
	public void testAttributeNoClosingQuotation() {
		currNode = createNode("div", 0, -1, 22, false, false);
		insertIntoAttributes("key", "\"value></div>");

		assertDocument("<div key=\"value></div>", currNode);
	}
	
	@Test
	public void testCDATABasicTest() {
		currNode = createNode("testText", 5, -1, 25, true, true); addCurrNode();
		currNode = createNode("div", 0, 25, 31, true, false, tempChildren);

		assertDocument("<div><![CDATA[testText]]></div>", currNode);
	}
	
	@Test
	public void testCDATAWithOtherElement() {
		currNode = createNode("TEXT", 5, -1, 21, true, true); addCurrNode();
		currNode = createNode("a", 21, 24, 28, true, false); addCurrNode();
		currNode = createNode("div", 0, 28, 34, true, false, tempChildren);

		assertDocument("<div><![CDATA[TEXT]]><a></a></div>", currNode);
	}

	@Test
	public void testCDATABasicNotClosed() {
		currNode = createNode("testText]</div>", 5, -1, 29, false, true); addCurrNode();
		currNode = createNode("div", 0, -1, 29, false, false, tempChildren);

		assertDocument("<div><![CDATA[testText]</div>", currNode);
	}








	//--------------------------------------------------------------------------------
	//Tools

	private static void compareTrees(Node root, Node inputRoot){
		Assert.assertEquals(root.tag, inputRoot.tag);
		Assert.assertEquals(root.start, inputRoot.start);
		Assert.assertEquals(root.end, inputRoot.end);
		
		if(root.attributes != null && root.isCDATA == false){
			Assert.assertEquals(root.attributes, inputRoot.attributes);
		}

		if(root.endTagStart == null){
			Assert.assertNull(inputRoot.endTagStart);
		}
		else{
			Assert.assertEquals(root.endTagStart.intValue(), inputRoot.endTagStart.intValue());
		}
		Assert.assertEquals(root.closed, inputRoot.closed);
		Assert.assertEquals(root.isCDATA, inputRoot.isCDATA);
		
		for(int i = 0; i < root.children.size(); i++) {
			try {
				compareTrees(root.children.get(i), inputRoot.children.get(i));
			} catch (Exception e) {
				Assert.fail("Children out of index");
			}
			
		}
		
		
	}

	public void insertIntoAttributes(String key, String value){
		if(currNode.attributes == null) {
			currNode.attributes = new HashMap<>();
		}
		currNode.attributes.put(key, value);
	}


	public Node createNode(String tag, int start, int endTagStart, int end , boolean closed, boolean isCDATA) {
		Node n = new Node(start, end, new ArrayList<>(), null, null);
		setRestOfNode(n, tag, endTagStart, closed, isCDATA);
		return n;
	}

	public Node createNode(String tag, int start, int endTagStart, int end , boolean closed, boolean isCDATA, List<Node> children) {
		Node n = new Node(start, end, children, null, null);
		setRestOfNode(n, tag, endTagStart, closed, isCDATA);
		return n;
	}

	private void setRestOfNode(Node n, String tag, int endTagStart, boolean closed, boolean isCDATA) {
		n.tag = tag;
		n.endTagStart = endTagStart >= 0 ? new Integer(endTagStart) : null;
		n.closed = closed;
		n.isCDATA = isCDATA;
	}

	public XMLDocument getXMLDocument(String input) {
		return XMLParser.getInstance().parse(input);
	}

	private static void assertDocument(String input, Node currNode) {
		XMLDocument document = XMLParser.getInstance().parse(input);
		Node inputRoot = document.children.get(0);
		compareTrees(currNode, inputRoot);
	}

	private static void assertFailedDocument(String input) {
		Assert.assertEquals(0, XMLParser.getInstance().parse(input).children.size());
	}

	private void addCurrNode() {
		if(tempChildren == null) {
			tempChildren = new ArrayList<Node>();
		}
		tempChildren.add(currNode);
	}

	private void newChildrenArray() {
		tempChildren = new ArrayList<Node>();
	}
}
