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

import static org.eclipse.lemminx.XMLAssert.le;
import static org.eclipse.lemminx.XMLAssert.r;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.XMLAssert.SettingsSaveContext;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lsp4j.LinkedEditingRanges;
import org.junit.jupiter.api.Test;

/**
 * XML references linked editing ranges tests.
 *
 * @author Angelo ZERR
 */
public class XMLReferencesLinkedEditingRangesExtensionsTest extends AbstractCacheBasedTest {

	@Test
	public void teiOnCorresp() throws BadLocationException {
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
				+ "      <anchor corresp=\"#bo|dy-id\"></anchor>\r\n" // <-- // linked editing ranges should do noting
				+ "      <anchor corresp=\"#body-id\"></anchor>\r\n"
				+ "    </body>\r\n"
				+ "  </text>\r\n"
				+ "</TEI>";
		testLinkedEditingFor(xml, "file:///test/tei.xml", null);
	}

	@Test
	public void teiOnXMLId() throws BadLocationException {
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
				+ "    <body xml:id=\"bod|y-id\">\r\n" // <-- linked editing ranges on xml:id should get the ranges of
														// the two anchor/@corresp attributes
				+ "      <p xml:id=\"p-id\" >Some text here.</p>\r\n"
				+ "      <anchor corresp=\"#body-id\"></anchor>\r\n"
				+ "      <anchor corresp=\"#body-id\"></anchor>\r\n"
				+ "    </body>\r\n"
				+ "  </text>\r\n"
				+ "</TEI>";
		testLinkedEditingFor(xml, "file:///test/tei.xml", //
				le(r(18, 24, 18, 31), r(19, 24, 19, 31), r(16, 18, 16, 25)));
	}

	@Test
	public void docbookOnLinked() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				// + "<!DOCTYPE book PUBLIC \"-//OASIS//DTD DocBook XML V4.4//EN\"
				// \"http://www.docbook.org/xml/4.4/docbookx.dtd\">\r\n"
				+ "<book>\r\n"
				+ "    <chapter id=\"chapter-1\">\r\n"
				+ "\r\n"
				+ "        <xref linkend=\"chapt|er-1\" />\r\n" // <-- // linked editing ranges should do noting
				+ "\r\n"
				+ "    </chapter>\r\n"
				+ "\r\n"
				+ "    <chapter id=\"chapter-2\">\r\n"
				+ "\r\n"
				+ "    </chapter>\r\n"
				+ "</book>";
		testLinkedEditingFor(xml, "file:///test/docbook.xml", null);
	}

	@Test
	public void docbookOnChapterId() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				// + "<!DOCTYPE book PUBLIC \"-//OASIS//DTD DocBook XML V4.4//EN\"
				// \"http://www.docbook.org/xml/4.4/docbookx.dtd\">\r\n"
				+ "<book>\r\n"
				+ "    <chapter id=\"chapt|er-1\">\r\n"
				+ "\r\n"
				+ "        <xref linkend=\"chapter-1\" />\r\n" // <-- // highlighting here should highlight
				// chapter/@id="chapter-1"
				+ "\r\n"
				+ "    </chapter>\r\n"
				+ "\r\n"
				+ "    <chapter id=\"chapter-2\">\r\n"
				+ "\r\n"
				+ "    </chapter>\r\n"
				+ "</book>";
		testLinkedEditingFor(xml, "file:///test/docbook.xml", //
				le(r(4, 23, 4, 32), r(2, 17, 2, 26)));
	}

	@Test
	public void noLinkedEditingRangeForDocument() throws BadLocationException {
		String xml = "|<book />";
		testLinkedEditingFor(xml, "file:///test/docbook.xml", null);
	}

	@Test
	public void noLinkedEditingRangeForNoneReferencedNodes() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				// + "<!DOCTYPE book PUBLIC \"-//OASIS//DTD DocBook XML V4.4//EN\"
				// \"http://www.docbook.org/xml/4.4/docbookx.dtd\">\r\n"
				+ "<book>\r\n"
				+ "    <chapter id=\"chapt|er-1\">\r\n"
				+ "\r\n"
				//+ "        <xref linkend=\"chapter-1\" />\r\n" // <-- // highlighting here should highlight
				// chapter/@id="chapter-1"
				+ "\r\n"
				+ "    </chapter>\r\n"
				+ "\r\n"
				+ "    <chapter id=\"chapter-2\">\r\n"
				+ "\r\n"
				+ "    </chapter>\r\n"
				+ "</book>";
		testLinkedEditingFor(xml, "file:///test/docbook.xml", null);
	}
	
	@Test
	public void webOnServletMapping() throws BadLocationException {
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
				+ "    <servlet-name>co|mingsoon</servlet-name>\r\n" // <-- linked editing ranges on
																		// servlet-mapping/servlet-name
																		// text
				+ "    <url-pattern>/*</url-pattern>\r\n"
				+ "  </servlet-mapping>\r\n"
				+ "  <servlet-mapping>\r\n"
				+ "    <servlet-name>comingsoon</servlet-name>\r\n"
				+ "    <url-pattern>/*</url-pattern>\r\n"
				+ "  </servlet-mapping>\r\n"
				+ "</web-app>\r\n"
				+ "";
		testLinkedEditingFor(xml, "file:///test/web.xml", null);
	}

	@Test
	public void webOnServlet() throws BadLocationException {
		String xml = "<web-app xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\"\r\n"
				+ "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				// + " xsi:schemaLocation=\"http://xmlns.jcp.org/xml/ns/javaee
				// http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd\"\r\n"
				+ "  version=\"3.1\">\r\n"
				+ "  <servlet>\r\n"
				+ "    <servlet-name>comi|ngsoon</servlet-name>\r\n" // <-- linked editing ranges on
																		// servlet/servlet-name
				// text should get the ranges of the two servlet-mapping/servlet-name text nodes
				+ "    <servlet-class>mysite.server.ComingSoonServlet</servlet-class>\r\n"
				+ "  </servlet>\r\n"
				+ "  <servlet-mapping>\r\n"
				+ "    <servlet-name>comingsoon</servlet-name>\r\n"
				+ "    <url-pattern>/*</url-pattern>\r\n"
				+ "  </servlet-mapping>\r\n"
				+ "  <servlet-mapping>\r\n"
				+ "    <servlet-name>comingsoon</servlet-name>\r\n"
				+ "    <url-pattern>/*</url-pattern>\r\n"
				+ "  </servlet-mapping>\r\n"
				+ "</web-app>\r\n"
				+ "";
		testLinkedEditingFor(xml, "file:///test/web.xml", //
				le(r(8, 18, 8, 28), r(12, 18, 12, 28), r(4, 18, 4, 28)));
	}

	private static void testLinkedEditingFor(String value, String fileURI,
			LinkedEditingRanges expected)
			throws BadLocationException {
		XMLLanguageService xmlLanguageService = new XMLLanguageService();
		xmlLanguageService.getExtensions();
		xmlLanguageService.doSave(new SettingsSaveContext(XMLReferencesSettingsForTest.createXMLReferencesSettings()));

		XMLAssert.testLinkedEditingFor(xmlLanguageService, value, fileURI, expected);
	}

}
