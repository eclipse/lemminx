package org.eclipse.xml.languageserver.internal.parser;

import org.eclipse.lsp4j.Position;
import org.eclipse.xml.languageserver.internal.parser.XMLParser;
import org.eclipse.xml.languageserver.model.XMLDocument;
import org.eclipse.xml.languageserver.model.IXMLParser;
import org.eclipse.xml.languageserver.model.Node;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class ParserTest {
	IXMLParser parser;
	XMLDocument document;
  Node currNode;

	@BeforeEach
	public void setup(){
		parser = XMLParser.getInstance();
		
	}

	@AfterEach
	public void teardown(){
		parser = null;
		document = null;
	}
  //test
  @Test
  public void testElementWithContent() throws BadLocationException {
		
    document = parser.parse("<r><a>xx\nx</a><b>yyy</b></r>");
    
    currNode = document.children.get(0);
    assertEquals(2, currNode.children.size());
    assertEquals("r", currNode.tag);
    assertEquals(0, currNode.start);
    assertEquals(24, currNode.endTagStart.intValue());
    assertEquals(28, currNode.end);
    assertTrue(currNode.closed);

    Node parent = currNode;
    currNode = currNode.children.get(0);
    assertEquals("a", currNode.tag);
    assertEquals(3, currNode.start);
    assertEquals(10, currNode.endTagStart.intValue());
    assertEquals(14, currNode.end);
    assertTrue(currNode.closed);
    
		currNode = parent.children.get(1);
    assertEquals("b", currNode.tag);
    assertEquals(14, currNode.start);
    assertEquals(20, currNode.endTagStart.intValue());
    assertEquals(24, currNode.end);
    assertTrue(currNode.closed);
	}

	@Test
  public void testElementWithSingleSelfClosingTag() {
    document = parser.parse("<project><atag/></project>");
    assertNotNull(document);
    assertEquals(0, document.start);
    assertEquals(26, document.end);
    assertEquals(1, document.children.size());

    currNode = document.children.get(0);
    assertEquals("project", currNode.tag);
    assertEquals(1, document.children.size());
    assertEquals(0, currNode.start);
    assertEquals(16, currNode.endTagStart.intValue());
    assertEquals(26, currNode.end);
    assertTrue(currNode.closed);

    currNode = currNode.children.get(0);
    assertEquals("atag", currNode.tag);
    assertEquals(9, currNode.start);
    assertNull(currNode.endTagStart);
    assertEquals(16, currNode.end);
    assertTrue(currNode.closed);
  }

  

  

  @Test
  public void testSingleElementTag() {
    document = parser.parse("<project />");
    currNode = document.children.get(0);
    assertEquals("project", currNode.tag);
    assertEquals(0, currNode.start);
    assertNull(currNode.endTagStart);
    assertEquals(11, currNode.end);
    assertTrue(currNode.closed);
  }

  

  @Test
  public void testNestedElementTags() {
    document = parser.parse("<project1><project2></project2></project1>");
    currNode = document.children.get(0);
    assertEquals("project1", currNode.tag);
    assertEquals(1, document.children.size());
    assertEquals(0, currNode.start);
    assertEquals(31, currNode.endTagStart.intValue());
    assertEquals(42, currNode.end);
    assertTrue(currNode.equals(currNode.children.get(0).parent));
    assertTrue(currNode.closed);

    currNode = currNode.children.get(0);
    assertEquals("project2", currNode.tag);
    assertEquals(10, currNode.start);
    assertEquals(20, currNode.endTagStart.intValue());
    assertEquals(31, currNode.end);
    assertTrue(currNode.closed);
    
  }

  @Test
  public void testDeeperNestedElementTags() {
    document = parser.parse("<project1><project2><project3></project3></project2></project1>");
    currNode = document.children.get(0);
    assertEquals("project1", currNode.tag);
    assertEquals(1, document.children.size());
    assertEquals(0, currNode.start);
    assertEquals(52, currNode.endTagStart.intValue());
    assertEquals(63, currNode.end);
    assertTrue(currNode.equals(currNode.children.get(0).parent));
    assertTrue(currNode.closed);

    currNode = currNode.children.get(0);
    assertEquals("project2", currNode.tag);
    assertEquals(10, currNode.start);
    assertEquals(41, currNode.endTagStart.intValue());
    assertEquals(52, currNode.end);
    assertTrue(currNode.closed);

    currNode = currNode.children.get(0);
    assertEquals("project3", currNode.tag);
    assertEquals(20, currNode.start);
    assertEquals(30, currNode.endTagStart.intValue());
    assertEquals(41, currNode.end);
    assertTrue(currNode.closed);
  }

  @Test
  public void testAttributeRecognized() {
    String content = "<project attribute=\"hello world\"></project>";
    document = parser.parse(content);
    currNode = document.children.get(0);
    assertEquals("project", currNode.tag);
    assertEquals(1, document.children.size());
    assertEquals(0, currNode.start);
    assertEquals(33, currNode.endTagStart.intValue());
    assertEquals(43, currNode.end);
    assertTrue(currNode.closed);

    assertNotNull(currNode.attributes.get("attribute"));
    assertEquals("\"hello world\"", currNode.attributes.get("attribute"));
    assertEquals(1, currNode.attributes.size());

    
  }

  @Test
  public void testMultipleAttributesRecognized() {
    document = parser.parse("<project a1=\"world\" a2=\"world\" a3=\"!\"></project>");
    currNode = document.children.get(0);//<project>
    assertEquals("\"world\"", currNode.attributes.get("a1"));
    assertEquals("\"world\"", currNode.attributes.get("a2"));
    assertEquals("\"!\"", currNode.attributes.get("a3"));
    assertEquals(3, currNode.attributes.size());
  }

  

  @Test
  public void testAttributePositionWithSpaces() {
    document = parser.parse("<project a1      =  \"world\"></project>");//Dont modify
    currNode = document.children.get(0);
    assertEquals("project", currNode.tag);
    assertEquals(1, document.children.size());
    assertEquals(0, currNode.start);
    assertEquals(28, currNode.endTagStart.intValue());
    assertEquals(38, currNode.end);
    assertTrue(currNode.closed);

    assertNotNull(currNode.attributes.get("a1"));
    assertEquals("\"world\"", currNode.attributes.get("a1"));
    assertEquals(1, currNode.attributes.size());
  }
  
  // @Test
  // public void testXmlDecl() {
  //  /* @formatter:off */
  //   document = parser.parse(
  //   "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
  //   "<project> \n" +
  //   "  someText \n" +
  //   "</project> \n");
  //  /* @formatter:on */
    

  // }

  // @Test
  // public void testValidPom() throws IOException{
  //   File f = new File("src/test/resources/validPom.xml");
  //   String s = FileUtils.readFileToString(f, "UTF-8");
  //   runParser("test", s);
  //   List<Diagnostic> d = this.parser.getDiagnostics();
  //   assertEquals(0, d.size(), "Found errors:" + d); 
  // }

  // @Test
  // public void testInvalidPom() throws IOException{
  //   File f = new File("src/test/resources/invalidPom.xml");
  //   String s = FileUtils.readFileToString(f, "UTF-8");
  //   runParser("test", s);
  //   List<Diagnostic> d = this.parser.getDiagnostics();
  //   assertEquals(1, d.size(), "No errors found."); 
  // }

  // @Test
  // public void testCData(){
  //   XMLNode node = runParser("test", "<p> shi <![CDATA[<Hello>\n</Dude>]]> <![CDATA[<Hello>\n</Dude>]]> </p>");
  //   assertNotNull(node);
    
  // }
}
