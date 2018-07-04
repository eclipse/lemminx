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

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4xml.internal.parser.XMLParser;
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
	public void testSimple() {
		assertDocument("<html></html>",
				"[{ tag: 'html', start: 0, end: 13, endTagStart: 6, closed: true, children: [] }]");
		assertDocument("<html><body></body></html>",
				"[{ tag: 'html', start: 0, end: 26, endTagStart: 19, closed: true, children: [{ tag: 'body', start: 6, end: 19, endTagStart: 12, closed: true, children: [] }] }]");
		assertDocument("<html><head></head><body></body></html>",
				"[{ tag: 'html', start: 0, end: 39, endTagStart: 32, closed: true, children: [{ tag: 'head', start: 6, end: 19, endTagStart: 12, closed: true, children: [] }, { tag: 'body', start: 19, end: 32, endTagStart: 25, closed: true, children: [] }] }]");
	}

	@Test
	public void testSelfClose() {
		assertDocument("<br/>", "[{ tag: 'br', start: 0, end: 5, endTagStart: null, closed: true, children: [] }]");
		assertDocument("<div><br/><span></span></div>",
				"[{ tag: 'div', start: 0, end: 29, endTagStart: 23, closed: true, children: [{ tag: 'br', start: 5, end: 10, endTagStart: null, closed: true, children: [] }, { tag: 'span', start: 10, end: 23, endTagStart: 16, closed: true, children: [] }] }]");
	}

	@Test
	public void testEmptyTag() {
		assertDocument("<meta>", "[{ tag: 'meta', start: 0, end: 6, endTagStart: null, closed: false, children: [] }]");
		/*
		 * assertDocument("<div><input type=\"button\" ><span><br><br></span></div>",
		 * "[{ " +
		 * "tag: 'div', start: 0, end: 53, endTagStart: 47, closed: true, children: [" +
		 * "{ tag: 'input', start: 5, end: 26, endTagStart: null, closed: true, children: [] },"
		 * +
		 * "{ tag: 'span', start: 26, end: 47, endTagStart: 40, closed: true, children: [{ tag: 'br', start: 32, end: 36, endTagStart: null, closed: true, children: [] }, { tag: 'br', start: 36, end: 40, endTagStart: null, closed: true, children: [] }] }"
		 * + "]" + "}]");
		 */
	}

	@Test
	public void testMissingTags() {
		assertDocument("</meta>", "[]");
		assertDocument("<div></div></div>",
				"[{ tag: 'div', start: 0, end: 11, endTagStart: 5, closed: true, children: [] }]");
		assertDocument("<div><div></div>",
				"[{ tag: 'div', start: 0, end: 16, endTagStart: null, closed: false, children: [{ tag: 'div', start: 5, end: 16, endTagStart: 10, closed: true, children: [] }] }]");
		assertDocument("<title><div></title>",
				"[{ tag: 'title', start: 0, end: 20, endTagStart: 12, closed: true, children: [{ tag: 'div', start: 7, end: 12, endTagStart: null, closed: false, children: [] }] }]");
		assertDocument("<h1><div><span></h1>",
				"[{ tag: 'h1', start: 0, end: 20, endTagStart: 15, closed: true, children: [{ tag: 'div', start: 4, end: 15, endTagStart: null, closed: false, children: [{ tag: 'span', start: 9, end: 15, endTagStart: null, closed: false, children: [] }] }] }]");
	}

	private static void assertDocument(String input, String expected) {
		XMLDocument document = XMLParser.getInstance().parse(input, null);
		Assert.assertEquals(expected, toJSON(document.getRoots()));
	}

	private static String toJSON(Node node) {
		return "{ tag: '" + node.tag + "', start: " + node.start + ", end: " + node.end + ", endTagStart: "
				+ node.endTagStart + ", closed: " + node.closed + ", children: " + toJSON(node.children) + " }";
	}

	private static String toJSON(List<Node> nodes) {
		return "[" + nodes.stream().map(XMLParserTest::toJSON).collect(Collectors.joining(", ")) + "]";
	}

}
