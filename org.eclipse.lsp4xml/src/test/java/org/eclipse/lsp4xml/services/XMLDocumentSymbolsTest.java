package org.eclipse.lsp4xml.services;

import static org.eclipse.lsp4xml.XMLAssert.ds;
import static org.eclipse.lsp4xml.XMLAssert.r;

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4xml.XMLAssert;
import org.junit.Test;

/**
 * XML symbols test with {@link DocumentSymbol} (with hierarchies).
 */
public class XMLDocumentSymbolsTest {

	@Test
	public void externalDTD() {
		String dtd = "<!ELEMENT br EMPTY>\n" + //
				"<!ATTLIST br\n" + //
				"	%all;>";
		XMLAssert.testDocumentSymbolsFor(dtd, "test.dtd", //
				ds("br", SymbolKind.Property, r(0, 0, 0, 19), r(0, 0, 0, 19), null, //
						Arrays.asList(ds("%all;", SymbolKind.Key, r(1, 0, 2, 7), r(1, 0, 2, 7), null,
								Collections.emptyList()))));

	}

	@Test
	public void internalDTD() {
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
				"	\r\n" + //
				"</Folks>";
		XMLAssert.testDocumentSymbolsFor(xml, "test.xml", //
				ds("xml", SymbolKind.Property, r(0, 0, 0, 23), r(0, 0, 0, 23), null, //
						Collections.emptyList()), //
				ds("DOCTYPE:Folks", SymbolKind.Struct, r(1, 0, 9, 3), r(1, 0, 9, 3), null, Arrays.asList(
						ds("Folks", SymbolKind.Property, r(2, 1, 2, 27), r(2, 1, 2, 27), null, Collections.emptyList()), //
						ds("Person", SymbolKind.Property, r(3, 1, 3, 32), r(3, 1, 3, 32), null, //
								Arrays.asList( //
										ds("Pin", SymbolKind.Key, r(4, 1, 4, 35), r(4, 1, 4, 35), null,
												Collections.emptyList()), //
										ds("Friend", SymbolKind.Key, r(5, 1, 5, 40), r(5, 1, 5, 40), null,
												Collections.emptyList()), //
										ds("Likes", SymbolKind.Key, r(6, 1, 6, 40), r(6, 1, 6, 40), null,
												Collections.emptyList()))), //
						ds("Name", SymbolKind.Property, r(7, 1, 7, 26), r(7, 1, 7, 26), null, Collections.emptyList()), //
						ds("Email", SymbolKind.Property, r(8, 1, 8, 27), r(8, 1, 8, 27), null, Collections.emptyList()) //
				)), //
				ds("Folks", SymbolKind.Field, r(10, 0, 12, 8), r(10, 0, 12, 8), null, Collections.emptyList()));

	}
}
