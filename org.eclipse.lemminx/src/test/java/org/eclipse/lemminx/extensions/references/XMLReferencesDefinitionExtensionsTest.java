/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
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

import static org.eclipse.lemminx.XMLAssert.ll;
import static org.eclipse.lemminx.XMLAssert.r;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.XMLAssert.SettingsSaveContext;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lsp4j.LocationLink;
import org.junit.jupiter.api.Test;

/**
 * XML references definition tests.
 *
 * @author Angelo ZERR
 */
public class XMLReferencesDefinitionExtensionsTest extends AbstractCacheBasedTest {

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
				+ "      <anchor corresp=\"#bo|dy-id\"></anchor>\r\n" // <-- definition on #body-id
				+ "    </body>\r\n"
				+ "  </text>\r\n"
				+ "</TEI>";
		testDefinitionFor(xml, "file:///test/tei.xml",
				ll("file:///test/tei.xml", r(18, 23, 18, 31), r(16, 18, 16, 25)));
	}

	@Test
	public void teiTargetMulti1() throws BadLocationException {
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
				+ "			<p xml:id=\"B\" />\r\n"
				+ "		</body>\r\n"
				+ "		<link target=\"#|A #B\" />\r\n"
				+ "	</text>\r\n"
				+ "</TEI>";
		testDefinitionFor(xml, "file:///test/tei.xml",
				ll("file:///test/tei.xml", r(8, 16, 8, 18), r(5, 14, 5, 15)));
	}

	@Test
	public void teiTargetMulti2() throws BadLocationException {
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
				+ "			<p xml:id=\"B\" />\r\n"
				+ "		</body>\r\n"
				+ "		<link target=\"#A #|B\" />\r\n"
				+ "	</text>\r\n"
				+ "</TEI>";
		testDefinitionFor(xml, "file:///test/tei.xml",
				ll("file:///test/tei.xml", r(8, 19, 8, 21), r(6, 14, 6, 15)));
	}

	@Test
	public void docbook() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				// + "<!DOCTYPE book PUBLIC \"-//OASIS//DTD DocBook XML V4.4//EN\"
				// \"http://www.docbook.org/xml/4.4/docbookx.dtd\">\r\n"
				+ "<book>\r\n"
				+ "    <chapter id=\"chapter-1\">\r\n"
				+ "\r\n"
				+ "        <xref linkend=\"chap|ter-1\" />\r\n" // <-- definition on #chapter-1
				+ "\r\n"
				+ "    </chapter>\r\n"
				+ "\r\n"
				+ "    <chapter id=\"chapter-2\">\r\n"
				+ "\r\n"
				+ "    </chapter>\r\n"
				+ "</book>";
		testDefinitionFor(xml, "file:///test/docbook.xml",
				ll("file:///test/docbook.xml", r(4, 23, 4, 32), r(2, 17, 2, 26)));
	}

	@Test
	public void web() throws BadLocationException {
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
				+ "    <servlet-name>co|mingsoon</servlet-name>\r\n" // <-- definition on servlet-mapping/servlet-name
																		// text
				+ "    <url-pattern>/*</url-pattern>\r\n"
				+ "  </servlet-mapping>\r\n"
				+ "</web-app>\r\n"
				+ "";
		testDefinitionFor(xml, "file:///test/web.xml",
				ll("file:///test/web.xml", r(8, 18, 8, 28), r(4, 18, 4, 28)));
	}

	@Test
	public void noDefinitionWithTEI() throws BadLocationException {
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
				// + " <body xml:id=\"body-id\">\r\n"
				+ "      <p xml:id=\"p-id\" >Some text here.</p>\r\n"
				+ "      <anchor corresp=\"#bo|dy-id\"></anchor>\r\n" // <-- definition on #body-id
				// + " </body>\r\n"
				+ "  </text>\r\n"
				+ "</TEI>";
		testDefinitionFor(xml, "file:///test/tei.xml");
	}

	@Test
	public void noDefinitionWithTEIAndNotHash() throws BadLocationException {
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
				+ "      <anchor corresp=\"bo|dy-id\"></anchor>\r\n" // <-- definition doesn't work her because corresp
																		// need to start with '#'.
				+ "    </body>\r\n"
				+ "  </text>\r\n"
				+ "</TEI>";
		testDefinitionFor(xml, "file:///test/tei.xml");
	}

	@Test
	public void attrToText() throws BadLocationException {
		String xml = "<aaa ref=\"chi|ld1 child2 child3\">\r\n"
				+ "  <bbb>child1</bbb>\r\n"
				+ "  <bbb>child2</bbb>\r\n"
				+ "  <bbb>child3</bbb>\r\n"
				+ "</aaa>";
		testDefinitionFor(xml, "file:///test/attr-to-text.xml",
				ll("file:///test/attr-to-text.xml", r(0, 10, 0, 16), r(1, 7, 1, 13)));
	}

	private static void testDefinitionFor(String xml, String fileURI, LocationLink... expectedItems)
			throws BadLocationException {
		XMLLanguageService xmlLanguageService = new XMLLanguageService();
		xmlLanguageService.getExtensions();
		xmlLanguageService.doSave(new SettingsSaveContext(XMLReferencesSettingsForTest.createXMLReferencesSettings()));

		XMLAssert.testDefinitionFor(xmlLanguageService, xml, fileURI, expectedItems);
	}

}
