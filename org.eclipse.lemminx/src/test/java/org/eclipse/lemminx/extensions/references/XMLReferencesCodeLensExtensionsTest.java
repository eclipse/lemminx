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

import static org.eclipse.lemminx.XMLAssert.cl;
import static org.eclipse.lemminx.XMLAssert.r;
import static org.eclipse.lemminx.client.ClientCommands.SHOW_REFERENCES;

import java.util.Arrays;
import java.util.function.Consumer;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.XMLAssert.SettingsSaveContext;
import org.eclipse.lemminx.client.CodeLensKind;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lsp4j.CodeLens;
import org.junit.jupiter.api.Test;

/**
 * XML references codelens tests
 *
 */
public class XMLReferencesCodeLensExtensionsTest extends AbstractCacheBasedTest {

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
				+ "    <body xml:id=\"body-id\">\r\n"
				+ "      <p xml:id=\"p-id\" >Some text here.</p>\r\n"
				+ "      <anchor corresp=\"#body-id\"></anchor>\r\n"
				+ "    </body>\r\n"
				+ "  </text>\r\n"
				+ "</TEI>";
		testCodeLensFor(xml, "file:///test/tei.xml", //
				cl(r(16, 10, 16, 26), "1 reference", SHOW_REFERENCES));
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
				// [2 references]
				+ "			<p xml:id=\"A\" />\r\n"
				// [1 reference]
				+ "			<p xml:id=\"B\" />\r\n"
				+ "		</body>\r\n"
				+ "		<link target=\"#A #B\" />\r\n"
				+ "		<link target=\"#B\" />\r\n"
				+ "	</text>\r\n"
				+ "</TEI>";
		testCodeLensFor(xml, "file:///test/tei.xml", //
				cl(r(5, 6, 5, 16), "1 reference", SHOW_REFERENCES), //
				cl(r(6, 6, 6, 16), "2 references", SHOW_REFERENCES));
	}

	@Test
	public void web() throws BadLocationException {
		// completion on <|
		String xml = "<web-app xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\"\r\n"
				+ "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				// + " xsi:schemaLocation=\"http://xmlns.jcp.org/xml/ns/javaee
				// http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd\"\r\n"
				+ "  version=\"3.1\">\r\n"
				+ "  <servlet>\r\n"
				+ "    <servlet-name>comingsoon</servlet-name>\r\n"
				+ "    <servlet-class>mysite.server.ComingSoonServlet</servlet-class>\r\n"
				+ "  </servlet>\r\n"
				+ "  <servlet-mapping>\r\n"
				+ "    <servlet-name>comingsoon</servlet-name>\r\n"
				+ "    <url-pattern>/*</url-pattern>\r\n"
				+ "  </servlet-mapping>\r\n"
				+ "</web-app>\r\n"
				+ "";
		testCodeLensFor(xml, "file:///test/web.xml", //
				cl(r(4, 18, 4, 28), "1 reference", SHOW_REFERENCES));
	}

	@Test
	public void attrToText() throws BadLocationException {
		String xml = "<aaa ref=\"child1 child2 child3\">\r\n"
				+ "  <bbb>child1</bbb>\r\n"
				+ "  <bbb>child2</bbb>\r\n"
				+ "  <bbb>child3</bbb>\r\n"
				+ "</aaa>";
		testCodeLensFor(xml, "file:///test/attr-to-text.xml", //
				cl(r(1, 7, 1, 13), "1 reference", SHOW_REFERENCES), //
				cl(r(2, 7, 2, 13), "1 reference", SHOW_REFERENCES), //
				cl(r(3, 7, 3, 13), "1 reference", SHOW_REFERENCES));
	}

	private static void testCodeLensFor(String value, String fileURI, CodeLens... expected) {
		XMLLanguageService xmlLanguageService = new XMLLanguageService();
		xmlLanguageService.getExtensions();
		Consumer<XMLLanguageService> customConfiguration = ls -> {
			ls.doSave(new SettingsSaveContext(XMLReferencesSettingsForTest.createXMLReferencesSettings()));
		};
		XMLAssert.testCodeLensFor(value, fileURI, xmlLanguageService, Arrays.asList(CodeLensKind.References),
				customConfiguration, expected);
	}

}
