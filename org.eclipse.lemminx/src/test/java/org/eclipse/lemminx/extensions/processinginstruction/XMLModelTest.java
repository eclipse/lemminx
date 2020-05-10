package org.eclipse.lemminx.extensions.processinginstruction;

import static org.eclipse.lemminx.XMLAssert.dl;
import static org.eclipse.lemminx.XMLAssert.r;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.XMLTextDocumentService;
import org.eclipse.lemminx.commons.TextDocument;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMParser;
import org.junit.jupiter.api.Test;

/**
 * 
 */
public class XMLModelTest {
	@Test
	public void xmlModelDeclaration(){
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<?xml-model href=\"http://www.docbook.org/xml/5.0/xsd/docbook.xsd\"?>\r\n" + //
				"<book>\r\n" + //
				"  ...\r\n" + //
				"</book>\r\n";

		TextDocument textDocument = new TextDocument(xml, "test.xml");
		DOMDocument d = DOMParser.getInstance().parse(xml, textDocument.getUri(), null);
		assertNotNull(d.getXMLModel());

		assertEquals("http://www.docbook.org/xml/5.0/xsd/docbook.xsd",d.getXMLModel().getSchemaLocation());
	}
}