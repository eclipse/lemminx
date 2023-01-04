/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.references;

import static org.eclipse.lemminx.XMLAssert.l;
import static org.eclipse.lemminx.XMLAssert.r;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.XMLAssert.SettingsSaveContext;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lsp4j.Location;
import org.junit.jupiter.api.Test;

/**
 * XML references tests
 *
 */
public class XMLReferencesReferenceExtensionsTest extends AbstractCacheBasedTest {

	@Test
	public void tei() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				// + "<?xml-model
				// href=\"http://www.tei-c.org/release/xml/tei/custom/schema/relaxng/tei_lite.rng\"
				// type=\"application/xml\"
				// schematypens=\"http://relaxng.org/ns/structure/1.0\"?>\r\n"
				+ "<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">\r\n"
				+ "  <teiHeader>  \r\n"
				+ "    <fileDesc>\r\n"
				+ "      <titleStmt>\r\n"
				+ "        <title>Title</title>\r\n"
				+ "      </titleStmt>\r\n"
				+ "      <publicationStmt>\r\n"
				+ "        <p>Publication information</p>  \r\n"
				+ "      </publicationStmt>\r\n"
				+ "      <sourceDesc>\r\n"
				+ "        <p>Information about the source</p>\r\n"
				+ "      </sourceDesc>\r\n"
				+ "    </fileDesc>\r\n"
				+ "  </teiHeader>\r\n"
				+ "  <text>\r\n"
				+ "    <body xml:id=\"bo|dy-id\">\r\n" // find references here
				+ "      <p xml:id=\"p-id\" >Some text here.</p>\r\n"
				+ "      <anchor corresp=\"#body-id\"></anchor>\r\n"
				+ "    </body>\r\n"
				+ "  </text>\r\n"
				+ "</TEI>";
		testReferencesFor(xml, "file:///test/tei.xml",
				l("file:///test/tei.xml", r(18, 23, 18, 31)));
	}

	@Test
	public void teiTargetMulti() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				// + "<?xml-model
				// href=\"http://www.tei-c.org/release/xml/tei/custom/schema/relaxng/tei_all.rng\"
				// type=\"application/xml\"
				// schematypens=\"http://relaxng.org/ns/structure/1.0\"?>\r\n"
				+ "<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">\r\n"
				+ "	<teiHeader></teiHeader>\r\n"
				+ "	<text>\r\n"
				+ "		<body>\r\n"
				+ "			<p xml:id=\"A\" />\r\n"
				+ "			<p xml:id=\"B|\" />\r\n"
				+ "		</body>\r\n"
				+ "		<link target=\"#A #B\" />\r\n" // [1]
				+ "		<link target=\"#A #B #C\" />\r\n" // [2]
				+ "		<link target=\"#A\" />\r\n"
				+ "		<link target=\"#B\" />\r\n" // [3]
				+ "		<link target2=\"#B\" />\r\n"
				+ "	</text>\r\n"
				+ "</TEI>";
		testReferencesFor(xml, "file:///test/tei.xml",
				l("file:///test/tei.xml", r(8, 19, 8, 21)), // [1]
				l("file:///test/tei.xml", r(9, 19, 9, 21)), // [2]
				l("file:///test/tei.xml", r(11, 16, 11, 18))); // [3]
	}

	private void testReferencesFor(String xml, String fileURI, Location... expectedItems) throws BadLocationException {
		XMLLanguageService xmlLanguageService = new XMLLanguageService();
		xmlLanguageService.getExtensions();
		xmlLanguageService.doSave(new SettingsSaveContext(XMLReferencesSettingsForTest.createXMLReferencesSettings()));

		XMLAssert.testReferencesFor(xmlLanguageService, xml, fileURI, expectedItems);
	}
}
