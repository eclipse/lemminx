package org.eclipse.lsp4xml.extensions.contentmodel;

import static org.eclipse.lsp4xml.XMLAssert.c;
import static org.eclipse.lsp4xml.XMLAssert.te;

import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemCapabilities;
import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.eclipse.lsp4xml.services.extensions.CompletionSettings;
import org.junit.Test;

public class DTDCompletionExtensionsTest {

	@Test
	public void completionInRoot() throws BadLocationException {
		// completion on <|
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"  <!DOCTYPE catalog\r\n" + //
				"    PUBLIC \"-//OASIS//DTD Entity Resolution XML Catalog V1.0//EN\"\r\n" + //
				"           \"http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd\">\r\n" + //
				"\r\n" + //
				"  <catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\"\r\n" + //
				"           prefer=\"public\">\r\n" + //
				"\r\n" + //
				"    <|";
		testCompletionFor(xml,
				c("delegatePublic", te(8, 4, 8, 5, "<delegatePublic publicIdStartString=\"$1\" catalog=\"$2\" />$0"),
						"<delegatePublic"), //
				c("public", te(8, 4, 8, 5, "<public publicId=\"$1\" uri=\"$2\" />$0"), "<public"));
	}

	@Test
	public void completionWithChoiceAttribute() throws BadLocationException {
		// completion on <|
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"  <!DOCTYPE catalog\r\n" + //
				"    PUBLIC \"-//OASIS//DTD Entity Resolution XML Catalog V1.0//EN\"\r\n" + //
				"           \"http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd\">\r\n" + //
				"\r\n" + //
				"  <catalog |";
		testCompletionFor(xml, c("prefer", te(5, 11, 5, 11, "prefer=\"${1|system,public|}\"$0"), "prefer"));
	}

	@Test
	public void externalDTDCompletionElement() throws BadLocationException {
		// completion on <|
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE Folks [\r\n" + //
				"	<!ELEMENT Folks (Person*)>\r\n" + //
				"	<!ELEMENT Person (Name,Email?)>\r\n" + //
				"	<!ATTLIST Person Pin ID #REQUIRED>\r\n" + //
				"	<!ATTLIST Person Friend IDREF #IMPLIED>\r\n" + //
				"	<!ATTLIST Person Likes IDREFS #IMPLIED>\r\n" + //
				"	<!ELEMENT Name (#PCDATA)>\r\n" + //
				"	<!ELEMENT Email (#PCDATA)>\r\n" + //
				"	]>\r\n" + //
				"<Folks>\r\n" + //
				"	|" + //
				"</Folks>";
		XMLAssert.testCompletionFor(xml, c("Person", te(11, 1, 11, 1, "<Person Pin=\"\"></Person>"), "Person"));
	}

	@Test
	public void externalDTDCompletionAttribute() throws BadLocationException {
		// completion on <|
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE Folks [\r\n" + //
				"	<!ELEMENT Folks (Person*)>\r\n" + //
				"	<!ELEMENT Person (Name,Email?)>\r\n" + //
				"	<!ATTLIST Person Pin ID #REQUIRED>\r\n" + //
				"	<!ATTLIST Person Friend IDREF #IMPLIED>\r\n" + //
				"	<!ATTLIST Person Likes IDREFS #IMPLIED>\r\n" + //
				"	<!ELEMENT Name (#PCDATA)>\r\n" + //
				"	<!ELEMENT Email (#PCDATA)>\r\n" + //
				"	]>\r\n" + //
				"<Folks>\r\n" + //
				"	<Person |" + //
				"</Folks>";
		XMLAssert.testCompletionFor(xml, c("Pin", te(11, 9, 11, 9, "Pin=\"\""), "Pin"), //
				c("Friend", te(11, 9, 11, 9, "Friend=\"\""), "Friend"), //
				c("Likes", te(11, 9, 11, 9, "Likes=\"\""), "Likes"));
	}

	@Test
	public void externalDTDCompletionElementDecl() throws BadLocationException {
		// completion on <|
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE Folks [\r\n" + //
				"	<!ELEMENT Folks (Person*)>\r\n" + //
				"	<!ELEMENT|\r\n" + //
				"]>\r\n" + //
				"<Folks>\r\n" + //
				"	" + //
				"</Folks>";
		testCompletionFor(xml, c("Insert DTD Element declaration", te(3, 1, 3, 11, "<!ELEMENT ${1:element-name} (${2:#PCDATA})>"), "<!ELEMENT "));
	}

	@Test
	public void externalDTDCompletionElementDecl2() throws BadLocationException {
		// completion on <|
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE Folks [\r\n" + //
				"	<!ELEMENT Folks (Person*)>\r\n" + //
				"	<!ELEM|\r\n" + //
				"]>\r\n" + //
				"<Folks>\r\n" + //
				"	" + //
				"</Folks>";
		testCompletionFor(xml, c("Insert DTD Element declaration", te(3, 1, 3, 7, "<!ELEMENT ${1:element-name} (${2:#PCDATA})>"), "<!ELEMENT "));
	}
	@Test
	public void externalDTDCompletionAllDecls() throws BadLocationException {
		// completion on <|
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE Folks [\r\n" + //
				"	<!ELEMENT Folks (Person*)>\r\n" + //
				"	|\r\n" + //
				"]>\r\n" + //
				"<Folks>\r\n" + //
				"	" + //
				"</Folks>";
		testCompletionFor(xml, true, 4, c("Insert DTD Element declaration", te(3, 1, 3, 1, "<!ELEMENT ${1:element-name} (${2:#PCDATA})>"), "<!ELEMENT ")
		,c("Insert Internal DTD Entity declaration", te(3, 1, 3, 1, "<!ENTITY ${1:entity-name} \"${2:entity-value}\">"), "<!ENTITY ")
		,c("Insert DTD Attributes list declaration", te(3, 1, 3, 1, "<!ATTLIST ${1:element-name} ${2:attribute-name} ${3:ID} ${4:#REQUIRED}>"), "<!ATTLIST ")
		,c("Insert External DTD Entity declaration", te(3, 1, 3, 1, "<!ENTITY ${1:entity-name} SYSTEM \"${2:entity-value}\">"), "<!ENTITY "));
	}

	@Test
	public void externalDTDCompletionAllDeclsSnippetsNotSupported() throws BadLocationException {
		// completion on <|
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE Folks [\r\n" + //
				"	<!ELEMENT Folks (Person*)>\r\n" + //
				"	|\r\n" + //
				"]>\r\n" + //
				"<Folks>\r\n" + //
				"	" + //
				"</Folks>";
		testCompletionFor(xml, false, 4, c("Insert DTD Element declaration", te(3, 1, 3, 1, "<!ELEMENT element-name (#PCDATA)>"), "<!ELEMENT ")
		,c("Insert Internal DTD Entity declaration", te(3, 1, 3, 1, "<!ENTITY entity-name \"entity-value\">"), "<!ENTITY ")
		,c("Insert DTD Attributes list declaration", te(3, 1, 3, 1, "<!ATTLIST element-name attribute-name ID #REQUIRED>"), "<!ATTLIST ")
		,c("Insert External DTD Entity declaration", te(3, 1, 3, 1, "<!ENTITY entity-name SYSTEM \"entity-value\">"), "<!ENTITY "));
	}
	
	@Test
	public void testNoDuplicateCompletionItems() throws BadLocationException {
		// completion on <|
		String xml = 
				"<?xml version=\"1.0\" standalone=\"no\" ?>\n" +
				"<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.0//EN\" \"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg.dtd\">\n" +
				"<svg xmlns=\"http://www.w3.org/2000/svg\">\n" +
				"    <animate attributeName=\"foo\">\n" +
				"        <|\n" + // <-- completion
				"    </animate>\n" +
				"</svg>";
		testCompletionFor(xml, false, 3, c("desc", te(4, 8, 4, 9, "<desc></desc>"), "<desc")
		,c("metadata", te(4, 8, 4, 9, "<metadata></metadata>"), "<metadata")
		,c("title", te(4, 8, 4, 9, "<title></title>"), "<title")
		);
	}
		
	private void testCompletionFor(String xml, CompletionItem... expectedItems) throws BadLocationException {
		testCompletionFor(xml,true, null, expectedItems);
	}

	private void testCompletionFor(String xml, boolean isSnippetsSupported, Integer expectedCount, CompletionItem... expectedItems) throws BadLocationException {
		CompletionSettings completionSettings = new CompletionSettings();
		CompletionCapabilities completionCapabilities = new CompletionCapabilities();
		CompletionItemCapabilities completionItem = new CompletionItemCapabilities(isSnippetsSupported); // activate snippets
		completionCapabilities.setCompletionItem(completionItem);
		completionSettings.setCapabilities(completionCapabilities);
		XMLAssert.testCompletionFor(new XMLLanguageService(), xml, "src/test/resources/catalogs/catalog.xml", null,
				null, expectedCount, completionSettings, expectedItems);
	}
}
