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

import static org.eclipse.lemminx.XMLAssert.c;
import static org.eclipse.lemminx.XMLAssert.te;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.XMLAssert.SettingsSaveContext;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.contentmodel.BaseFileTempTest;
import org.eclipse.lemminx.extensions.references.settings.XMLReferenceExpression;
import org.eclipse.lemminx.extensions.references.settings.XMLReferences;
import org.eclipse.lemminx.extensions.references.settings.XMLReferencesSettings;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

/**
 * XML references completion tests.
 *
 */
public class XMLReferencesCompletionExtensionsTest extends BaseFileTempTest {

	@Test
	public void tei() throws BadLocationException {
		// completion on <|
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
				+ "      <anchor corresp=\"|\"></anchor>\r\n" // <-- completion on @corresp attribute
				+ "    </body>\r\n"
				+ "  </text>\r\n"
				+ "</TEI>";
		testCompletionFor(xml, "file:///test/tei.xml", //
				2, //
				c("#body-id", te(18, 23, 18, 23, "#body-id"), "#body-id"), //
				c("#p-id", te(18, 23, 18, 23, "#p-id"), "#p-id"));
	}

	@Test
	public void docbook() throws BadLocationException {
		// completion on <|
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				// + "<!DOCTYPE book PUBLIC \"-//OASIS//DTD DocBook XML V4.4//EN\"
				// \"http://www.docbook.org/xml/4.4/docbookx.dtd\">\r\n"
				+ "<book>\r\n"
				+ "    <chapter id=\"chapter-1\">\r\n"
				+ "\r\n"
				+ "        <xref linkend=\"|\" />\r\n" // <-- completion on linkend attribute
				+ "\r\n"
				+ "    </chapter>\r\n"
				+ "\r\n"
				+ "    <chapter id=\"chapter-2\">\r\n"
				+ "\r\n"
				+ "    </chapter>\r\n"
				+ "</book>";
		testCompletionFor(xml, "file:///test/docbook.xml", //
				2, //
				c("chapter-1", te(4, 23, 4, 23, "chapter-1"), "chapter-1"), //
				c("chapter-2", te(4, 23, 4, 23, "chapter-2"), "chapter-2"));
	}

	@Test
	public void web() throws BadLocationException {
		// completion on <|
		String xml = "<web-app xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\"\r\n"
				+ "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				// + "  xsi:schemaLocation=\"http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd\"\r\n"
				+ "  version=\"3.1\">\r\n"
				+ "  <servlet>\r\n"
				+ "    <servlet-name>comingsoon</servlet-name>\r\n"
				+ "    <servlet-class>mysite.server.ComingSoonServlet</servlet-class>\r\n"
				+ "  </servlet>\r\n"
				+ "  <servlet-mapping>\r\n"
				+ "    <servlet-name>co|m</servlet-name>\r\n" // <-- completion on servlet-mapping/servlet-name text
				+ "    <url-pattern>/*</url-pattern>\r\n"
				+ "  </servlet-mapping>\r\n"
				+ "</web-app>\r\n"
				+ "";
		testCompletionFor(xml, "file:///test/web.xml", //
				1 + XMLAssert.CDATA_SNIPPETS + XMLAssert.COMMENT_SNIPPETS, //
				c("comingsoon", te(8, 18, 8, 21, "comingsoon"), "comingsoon"));
	}

	@Test
	public void webInEmptyText() throws BadLocationException {
		// completion on <|
		String xml = "<web-app xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\"\r\n"
				+ "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				// + "  xsi:schemaLocation=\"http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd\"\r\n"
				+ "  version=\"3.1\">\r\n"
				+ "  <servlet>\r\n"
				+ "    <servlet-name>comingsoon</servlet-name>\r\n"
				+ "    <servlet-class>mysite.server.ComingSoonServlet</servlet-class>\r\n"
				+ "  </servlet>\r\n"
				+ "  <servlet-mapping>\r\n"
				+ "    <servlet-name>|</servlet-name>\r\n" // <-- completion on servlet-mapping/servlet-name text
				+ "    <url-pattern>/*</url-pattern>\r\n"
				+ "  </servlet-mapping>\r\n"
				+ "</web-app>\r\n"
				+ "";
		testCompletionFor(xml, "file:///test/web.xml", //
				1 + XMLAssert.CDATA_SNIPPETS + XMLAssert.COMMENT_SNIPPETS, //
				c("comingsoon", te(8, 18, 8, 18, "comingsoon"), "comingsoon"));
	}
	private static void testCompletionFor(String value, String fileURI, Integer expectedCount,
			CompletionItem... expectedItems)
			throws BadLocationException {
		XMLAssert.testCompletionFor(new XMLLanguageService(), value, null, ls -> {

			XMLReferencesSettings referencesSettings = new XMLReferencesSettings();
			referencesSettings.setReferences(createReferences());
			ls.doSave(new SettingsSaveContext(referencesSettings));

		}, fileURI,
				expectedCount, true, expectedItems);
	}

	private static List<XMLReferences> createReferences() {
		List<XMLReferences> references = new ArrayList<>();
		/*
		 * {
		 * "prefix": "#",
		 * "from": "@corresp",
		 * "to": "@xml:id"
		 * }
		 */
		XMLReferences tei = new XMLReferences();
		tei.setPattern("**/*tei.xml");
		XMLReferenceExpression corresp = new XMLReferenceExpression();
		corresp.setPrefix("#");
		corresp.setFrom("@corresp");
		corresp.setTo("@xml:id");
		tei.setExpressions(Arrays.asList(corresp));
		references.add(tei);
		/*
		 * {
		 * "from": "xref/@linkend",
		 * "to": "@id"
		 * }
		 */
		XMLReferences docbook = new XMLReferences();
		docbook.setPattern("**/*docbook.xml");
		XMLReferenceExpression linkend = new XMLReferenceExpression();
		linkend.setFrom("xref/@linkend");
		linkend.setTo("@id");
		docbook.setExpressions(Arrays.asList(linkend));
		references.add(docbook);

		/*
		 * {
		 * "from": "servlet-mapping/servlet-name/text()",
		 * "to": "servlet/servlet-name/text()"
		 * }
		 */
		XMLReferences web = new XMLReferences();
		web.setPattern("**/web.xml");
		XMLReferenceExpression servletName = new XMLReferenceExpression();
		servletName.setFrom("servlet-mapping/servlet-name/text()");
		servletName.setTo("servlet/servlet-name/text()");
		web.setExpressions(Arrays.asList(servletName));
		references.add(web);

		return references;
	}
}