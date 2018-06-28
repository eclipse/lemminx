package org.eclipse.xml.languageserver.internal.parser;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.xml.languageserver.model.Node;
import org.eclipse.xml.languageserver.model.XMLDocument;
import org.junit.Assert;
import org.junit.Test;

public class XMLParserTest {

	@Test
	public void testSimple() {
		assertDocument("<html></html>",
				"[{ tag: 'html', start: 0, end: 13, endTagStart: 6, closed: true, children: [] }]");
		assertDocument("<html><body></body></html>",
				"[{ tag: 'html', start: 0, end: 26, endTagStart: 19, closed: true, children: [{ tag: 'body', start: 6, end: 19, endTagStart: 12, closed: true, children: [] }] }]");
		//assertDocument("<html><head></head><body></body></html>",
		//		"[{ tag: 'html', start: 0, end: 39, endTagStart: 32, closed: true, children: [{ tag: 'head', start: 6, end: 19, endTagStart: 12, closed: true, children: [] }, { tag: 'body', start: 19, end: 32, endTagStart: 25, closed: true, children: [] }] }]");
	}

	private static void assertDocument(String input, String expected) {
		XMLDocument document = XMLParser.getInstance().parse(input);
		Assert.assertEquals(toJSON(document.getRoots()), expected);
	}

	private static String toJSON(Node node) {
		return "{ tag: '" + node.tag + "', start: " + node.start + ", end: " + node.end + ", endTagStart: "
				+ node.endTagStart + ", closed: " + node.closed + ", children: " + toJSON(node.children) + " }";
	}

	private static String toJSON(List<Node> nodes) {
		return "[" + nodes.stream().map(XMLParserTest::toJSON).collect(Collectors.joining(",")) + "]";
	}

}
