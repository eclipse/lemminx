package org.eclipse.xml.languageserver.internal.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.xml.languageserver.XMLLanguageServer;
import org.eclipse.xml.languageserver.model.IXMLParser;
import org.eclipse.xml.languageserver.model.XMLDocument;
import org.eclipse.xml.languageserver.services.XMLLanguageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HighlightingTests {
	IXMLParser parser;
  XMLDocument document;

  @BeforeEach
	public void setup(){
		parser = XMLParser.getInstance();
		
	}

	@AfterEach
	public void teardown(){
		parser = null;
		document = null;
  }
  
  @Test
  public void testFindDocumentHighlights(){
    TextDocumentItem item = new TextDocumentItem();
    item.setText("<a>\n  <![CDATA[<Hello>\n</Hello>]]></a>");
    document = parser.parse(item.getText());
    Position p = new Position(2,9);
    XMLLanguageService t = new XMLLanguageService();
    List<DocumentHighlight> x = t.findDocumentHighlights(item, p, document);
    System.out.println();
  }

}