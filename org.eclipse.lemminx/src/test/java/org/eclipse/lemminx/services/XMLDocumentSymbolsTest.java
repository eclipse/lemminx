/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services;

import static org.eclipse.lemminx.XMLAssert.ds;
import static org.eclipse.lemminx.XMLAssert.r;

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.SymbolKind;
import org.junit.jupiter.api.Test;

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
						Arrays.asList(ds("%all;", SymbolKind.Key, r(2, 1, 2, 6), r(2, 1, 2, 6), null, Arrays.asList()))));

	}

	@Test
	public void multipleAttlistValues() {
		String dtd = 
			"<!ELEMENT target EMPTY>\r\n" +
			" \r\n" +
			"<!ATTLIST target\r\n" +
			"          tid ID #IMPLIED\r\n" +
			"\r\n" +
			"<!ATTLIST target\r\n" +
			"          bee ID #IMPLIED>\r\n" +
			"        \r\n" +
			"         \r\n" +
			"<!ATTLIST extension-point\r\n" +
			"          ep CDATA #IMPLIED\r\n" +
			"          ep2 CDATA #IMPLIED>\r\n";

		XMLAssert.testDocumentSymbolsFor(dtd, "test.dtd", 
					ds("target", SymbolKind.Property, r(0, 0, 0, 23), r(0, 0, 0, 23), null,
									Arrays.asList(
										ds("tid", SymbolKind.Key, r(3, 10, 3, 13), r(3, 10, 3, 13), null, Arrays.asList()),
										ds("bee", SymbolKind.Key, r(6, 10, 6, 13), r(6, 10, 6, 13), null, Arrays.asList())
									)
					),
					ds("extension-point", SymbolKind.Key, r(9, 0, 11, 29), r(9, 0, 11, 29), null, 
									Arrays.asList(
										ds("ep", SymbolKind.Key, r(10, 10, 10, 12), r(10, 10, 10, 12), null, Arrays.asList()),
										ds("ep2", SymbolKind.Key, r(11, 10, 11, 13), r(11, 10, 11, 13), null, Arrays.asList())
									)));

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
				ds("DOCTYPE:Folks", SymbolKind.Struct, r(1, 0, 9, 3), r(1, 0, 9, 3), null,
						Arrays.asList(
								ds("Folks", SymbolKind.Property, r(2, 1, 2, 27), r(2, 1, 2, 27), null, Collections.emptyList()), //
								ds("Person", SymbolKind.Property, r(3, 1, 3, 32), r(3, 1, 3, 32), null, //
										Arrays.asList( //
												ds("Pin", SymbolKind.Key, r(4, 18, 4, 21), r(4, 18, 4, 21), null, Collections.emptyList()), //
												ds("Friend", SymbolKind.Key, r(5, 18, 5, 24), r(5, 18, 5, 24), null, Collections.emptyList()), //
												ds("Likes", SymbolKind.Key, r(6, 18, 6, 23), r(6, 18, 6, 23), null, Collections.emptyList()))), //
								ds("Name", SymbolKind.Property, r(7, 1, 7, 26), r(7, 1, 7, 26), null, Collections.emptyList()), //
								ds("Email", SymbolKind.Property, r(8, 1, 8, 27), r(8, 1, 8, 27), null, Collections.emptyList()) //
						)), //
				ds("Folks", SymbolKind.Field, r(10, 0, 12, 8), r(10, 0, 12, 8), null, Collections.emptyList()));
	}
}
