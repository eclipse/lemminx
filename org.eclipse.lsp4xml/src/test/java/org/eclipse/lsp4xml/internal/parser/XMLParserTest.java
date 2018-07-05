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

	// private static boolean compareNodes(Node node, String tag, int start, int end, int endTagStart, boolean closed, boolean isCDATA){
	// 	//Assert.assertEquals()
	// }

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

	// 	@BeforeEach
// 	public void setup(){
// 		parser = XMLParser.getInstance();
		
// 	}

// 	@AfterEach
// 	public void teardown(){
// 		parser = null;
// 		document = null;
// 	}
//   //test
//   @Test
// 	public void testElementWithContent() throws BadLocationException {
		
//     document = parser.parse("<r><a>xx\nx</a><b>yyy</b></r>");
    
//     currNode = document.children.get(0);
//     assertEquals(2, currNode.children.size());
//     assertEquals("r", currNode.tag);
//     assertEquals(0, currNode.start);
//     assertEquals(24, currNode.endTagStart.intValue());
//     assertEquals(28, currNode.end);
//     assertTrue(currNode.closed);

//     Node parent = currNode;
//     currNode = currNode.children.get(0);
//     assertEquals("a", currNode.tag);
//     assertEquals(3, currNode.start);
//     assertEquals(10, currNode.endTagStart.intValue());
//     assertEquals(14, currNode.end);
//     assertTrue(currNode.closed);
    
// 		currNode = parent.children.get(1);
//     assertEquals("b", currNode.tag);
//     assertEquals(14, currNode.start);
//     assertEquals(20, currNode.endTagStart.intValue());
//     assertEquals(24, currNode.end);
//     assertTrue(currNode.closed);
// 	}

// 	@Test
//   public void testElementWithSingleSelfClosingTag() {
//     document = parser.parse("<project><atag/></project>");
//     assertNotNull(document);
//     assertEquals(0, document.start);
//     assertEquals(26, document.end);
//     assertEquals(1, document.children.size());

//     currNode = document.children.get(0);
//     assertEquals("project", currNode.tag);
//     assertEquals(1, document.children.size());
//     assertEquals(0, currNode.start);
//     assertEquals(16, currNode.endTagStart.intValue());
//     assertEquals(26, currNode.end);
//     assertTrue(currNode.closed);

//     currNode = currNode.children.get(0);
//     assertEquals("atag", currNode.tag);
//     assertEquals(9, currNode.start);
//     assertNull(currNode.endTagStart);
//     assertEquals(16, currNode.end);
//     assertTrue(currNode.closed);
//   }

  

  

//   @Test
//   public void testSingleElementTag() {
//     document = parser.parse("<project />");
//     currNode = document.children.get(0);
//     assertEquals("project", currNode.tag);
//     assertEquals(0, currNode.start);
//     assertNull(currNode.endTagStart);
//     assertEquals(11, currNode.end);
//     assertTrue(currNode.closed);
//   }

  

//   @Test
//   public void testNestedElementTags() {
//     document = parser.parse("<project1><project2></project2></project1>");
//     currNode = document.children.get(0);
//     assertEquals("project1", currNode.tag);
//     assertEquals(1, document.children.size());
//     assertEquals(0, currNode.start);
//     assertEquals(31, currNode.endTagStart.intValue());
//     assertEquals(42, currNode.end);
//     assertTrue(currNode.equals(currNode.children.get(0).parent));
//     assertTrue(currNode.closed);

//     currNode = currNode.children.get(0);
//     assertEquals("project2", currNode.tag);
//     assertEquals(10, currNode.start);
//     assertEquals(20, currNode.endTagStart.intValue());
//     assertEquals(31, currNode.end);
//     assertTrue(currNode.closed);
    
//   }

//   @Test
//   public void testDeeperNestedElementTags() {
//     document = parser.parse("<project1><project2><project3></project3></project2></project1>");
//     currNode = document.children.get(0);
//     assertEquals("project1", currNode.tag);
//     assertEquals(1, document.children.size());
//     assertEquals(0, currNode.start);
//     assertEquals(52, currNode.endTagStart.intValue());
//     assertEquals(63, currNode.end);
//     assertTrue(currNode.equals(currNode.children.get(0).parent));
//     assertTrue(currNode.closed);

//     currNode = currNode.children.get(0);
//     assertEquals("project2", currNode.tag);
//     assertEquals(10, currNode.start);
//     assertEquals(41, currNode.endTagStart.intValue());
//     assertEquals(52, currNode.end);
//     assertTrue(currNode.closed);

//     currNode = currNode.children.get(0);
//     assertEquals("project3", currNode.tag);
//     assertEquals(20, currNode.start);
//     assertEquals(30, currNode.endTagStart.intValue());
//     assertEquals(41, currNode.end);
//     assertTrue(currNode.closed);
//   }

//   @Test
//   public void testAttributeRecognized() {
//     String content = "<project attribute=\"hello world\"></project>";
//     document = parser.parse(content);
//     currNode = document.children.get(0);
//     assertEquals("project", currNode.tag);
//     assertEquals(1, document.children.size());
//     assertEquals(0, currNode.start);
//     assertEquals(33, currNode.endTagStart.intValue());
//     assertEquals(43, currNode.end);
//     assertTrue(currNode.closed);

//     assertNotNull(currNode.attributes.get("attribute"));
//     assertEquals("\"hello world\"", currNode.attributes.get("attribute"));
//     assertEquals(1, currNode.attributes.size());

    
//   }

//   @Test
//   public void testMultipleAttributesRecognized() {
//     document = parser.parse("<project a1=\"world\" a2=\"world\" a3=\"!\"></project>");
//     currNode = document.children.get(0);//<project>
//     assertEquals("\"world\"", currNode.attributes.get("a1"));
//     assertEquals("\"world\"", currNode.attributes.get("a2"));
//     assertEquals("\"!\"", currNode.attributes.get("a3"));
//     assertEquals(3, currNode.attributes.size());
//   }

  

//   @Test
//   public void testAttributePositionWithSpaces() {
//     document = parser.parse("<project a1      =  \"world\"></project>");//Dont modify
//     currNode = document.children.get(0);
//     assertEquals("project", currNode.tag);
//     assertEquals(1, document.children.size());
//     assertEquals(0, currNode.start);
//     assertEquals(28, currNode.endTagStart.intValue());
//     assertEquals(38, currNode.end);
//     assertTrue(currNode.closed);

//     assertNotNull(currNode.attributes.get("a1"));
//     assertEquals("\"world\"", currNode.attributes.get("a1"));
//     assertEquals(1, currNode.attributes.size());
//   }
  
//   // @Test
//   // public void testXmlDecl() {
//   //  /* @formatter:off */
//   //   document = parser.parse(
//   //   "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
//   //   "<project> \n" +
//   //   "  someText \n" +
//   //   "</project> \n");
//   //  /* @formatter:on */
    

//   // }

//   // @Test
//   // public void testValidPom() throws IOException{
//   //   File f = new File("src/test/resources/validPom.xml");
//   //   String s = FileUtils.readFileToString(f, "UTF-8");
//   //   runParser("test", s);
//   //   List<Diagnostic> d = this.parser.getDiagnostics();
//   //   assertEquals(0, d.size(), "Found errors:" + d); 
//   // }

//   // @Test
//   // public void testInvalidPom() throws IOException{
//   //   File f = new File("src/test/resources/invalidPom.xml");
//   //   String s = FileUtils.readFileToString(f, "UTF-8");
//   //   runParser("test", s);
//   //   List<Diagnostic> d = this.parser.getDiagnostics();
//   //   assertEquals(1, d.size(), "No errors found."); 
//   // }

//   // @Test
//   // public void testCData(){
//   //   XMLNode node = runParser("test", "<p> shi <![CDATA[<Hello>\n</Dude>]]> <![CDATA[<Hello>\n</Dude>]]> </p>");
//   //   assertNotNull(node);
    
//   // }

}
