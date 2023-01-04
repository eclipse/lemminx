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

import static org.eclipse.lemminx.XMLAssert.pr;
import static org.eclipse.lemminx.XMLAssert.r;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.XMLAssert.SettingsSaveContext;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lsp4j.PrepareRenameResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * XML references prepare rename tests.
 *
 */
public class XMLReferencesPrepareRenameExtensionsTest extends AbstractCacheBasedTest {

	@Test
	public void teiOnXmlId() throws BadLocationException {
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
				+ "    <body xml:id=\"bo|dy-id\">\r\n" // prepare rename here should be allowed.
				+ "      <p xml:id=\"p-id\" >Some text here.</p>\r\n"
				+ "      <anchor corresp=\"#body-id\"></anchor>\r\n"
				+ "      <anchor corresp=\"#body-id\"></anchor>\r\n"
				+ "    </body>\r\n"
				+ "  </text>\r\n"
				+ "</TEI>";
		assertPrepareRename(xml, "file:///test/tei.xml", //
				pr(r(16, 18, 25), "body-id"));
	}

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
				+ "      <anchor corresp=\"#bod|y-id\"></anchor>\r\n" // rename here should be allowed.
				+ "      <anchor corresp=\"#body-id\"></anchor>\r\n"
				+ "    </body>\r\n"
				+ "  </text>\r\n"
				+ "</TEI>";
		assertPrepareRename(xml, "file:///test/tei.xml", //
				pr(r(18, 24, 31), "body-id"));
	}

	@Test
	public void teiOnCorrespWithoutPrefix() throws BadLocationException {
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
				+ "      <anchor corresp=\"bod|y-id\"></anchor>\r\n" // rename here should be forbidden because it
																		// misses '#'.
				+ "      <anchor corresp=\"#body-id\"></anchor>\r\n"
				+ "    </body>\r\n"
				+ "  </text>\r\n"
				+ "</TEI>";
		assertPrepareRename(xml, "file:///test/tei.xml", null);
	}

	@Test
	public void webOnServlet() throws BadLocationException {
		String xml = "<web-app xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\"\r\n"
				+ "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				// + " xsi:schemaLocation=\"http://xmlns.jcp.org/xml/ns/javaee
				// http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd\"\r\n"
				+ "  version=\"3.1\">\r\n"
				+ "  <servlet>\r\n"
				+ "    <servlet-name>co|mingsoon</servlet-name>\r\n" // rename here should should rename
																		// servlet-mapping/servlet-name text
				// value
				+ "    <servlet-class>mysite.server.ComingSoonServlet</servlet-class>\r\n"
				+ "  </servlet>\r\n"
				+ "  <servlet-mapping>\r\n"
				+ "    <servlet-name>comingsoon</servlet-name>\r\n"
				+ "    <url-pattern>/*</url-pattern>\r\n"
				+ "  </servlet-mapping>\r\n"
				+ "</web-app>\r\n"
				+ "";
		assertPrepareRename(xml, "file:///test/web.xml", //
				pr(r(4, 18, 28), "comingsoon"));
	}

	@Test
	public void webOnServletMapping() throws BadLocationException {
		String xml = "<web-app xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\"\r\n"
				+ "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				// + " xsi:schemaLocation=\"http://xmlns.jcp.org/xml/ns/javaee
				// http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd\"\r\n"
				+ "  version=\"3.1\">\r\n"
				+ "  <servlet>\r\n"
				+ "    <servlet-name>comingsoon</servlet-name>\r\n" // rename here should be allowed
				+ "    <servlet-class>mysite.server.ComingSoonServlet</servlet-class>\r\n"
				+ "  </servlet>\r\n"
				+ "  <servlet-mapping>\r\n"
				+ "    <servlet-name>comin|gsoon</servlet-name>\r\n"
				+ "    <url-pattern>/*</url-pattern>\r\n"
				+ "  </servlet-mapping>\r\n"
				+ "</web-app>\r\n"
				+ "";
		assertPrepareRename(xml, "file:///test/web.xml", //
				pr(r(8, 18, 28), "comingsoon"));
	}

	// Reference attribute -> text

	@Test
	public void attrToText() throws BadLocationException {
		String xml = "<aaa ref=\"child1 child2 child3\">\r\n"
				+ "  <bbb>chi|ld1</bbb>\r\n"
				+ "  <bbb>child2</bbb>\r\n"
				+ "  <bbb>child3</bbb>\r\n"
				+ "</aaa>";
		assertPrepareRename(xml, "file:///test/attr-to-text.xml", //
				pr(r(1, 7, 13), "child1"));
	}

	@Test
	public void attrToTextOnRef() throws BadLocationException {
		String xml = "<aaa ref=\"ch|ild1 child2 child3\">\r\n"
				+ "  <bbb>chi|ld1</bbb>\r\n"
				+ "  <bbb>child2</bbb>\r\n"
				+ "  <bbb>child3</bbb>\r\n"
				+ "</aaa>";
		assertPrepareRename(xml, "file:///test/attr-to-text.xml", //
				pr(r(0, 10, 16), "child1"));
	}

	@Test
	public void attrToTextWithoutFrom() throws BadLocationException {
		String xml = "<aaa ref=\"new-name child2 child3\">\r\n"
				+ "  <bbb>chi|ld1</bbb>\r\n"
				+ "  <bbb>child2</bbb>\r\n"
				+ "  <bbb>child3</bbb>\r\n"
				+ "</aaa>";
		assertPrepareRename(xml, "file:///test/attr-to-text.xml", //
				pr(r(1, 7, 13), "child1"));
	}

	@Test
	public void attrToTextNoRename() throws BadLocationException {
		String xml = "<aaa ref=\"new-name child2 child3\">\r\n"
				+ "  <ccc>chi|ld1</ccc>\r\n"
				+ "  <bbb>child2</bbb>\r\n"
				+ "  <bbb>child3</bbb>\r\n"
				+ "</aaa>";
		assertPrepareRename(xml, "file:///test/attr-to-text.xml", //
				null);
	}

	// Reference text -> text

	@Test
	public void textToText() throws BadLocationException {
		String xml = "<foo>\r\n"
				+ "  <from>child1</to>\r\n"
				+ "  <from>child1</to>\r\n"
				+ "  <from>child2</to>\r\n"
				+ "  <to>chi|ld1</to>\r\n"
				+ "  <to>child2</to>\r\n"
				+ "</foo>";
		assertPrepareRename(xml, "file:///test/text-to-text.xml", //
				pr(r(4, 6, 12), "child1"));
	}

	@Test
	public void textToTextWithoutFrom() throws BadLocationException {
		String xml = "<foo>\r\n"
//				+ "  <from>child1</to>\r\n"
//				+ "  <from>child1</to>\r\n"
//				+ "  <from>child2</to>\r\n"
				+ "  <to>chi|ld1</to>\r\n"
				+ "  <to>child2</to>\r\n"
				+ "</foo>";
		assertPrepareRename(xml, "file:///test/text-to-text.xml", //
				pr(r(1, 6, 12), "child1"));
	}

	@Test
	public void textToTextNoRename() throws BadLocationException {
		String xml = "<foo>\r\n"
//				+ "  <from>child1</to>\r\n"
//				+ "  <from>child1</to>\r\n"
				+ "  <from>child2</to>\r\n"
				+ "  <to2>chi|ld1</to2M>\r\n"
				+ "  <to>child2</to>\r\n"
				+ "</foo>";
		assertPrepareRename(xml, "file:///test/text-to-text.xml", null);
	}

	@Test
	public void noPrepareRenameResult() throws BadLocationException {
		String xml = "<bar></bar>\n"
				+ "|";
		// Elements bar matches none pattern of "xml.references"
		assertPrepareRename(xml, "file:///test/tei.xml", null);
		assertPrepareRename(xml, "file:///test/foo.xml", null);
	}
	
	@Test
	public void noPrepareRenameResulWithOnlyPrefix() throws BadLocationException {
		String xml = "<link target=\"#|\" />";
		// Elements from matches pattern of "xml.references" with 'text-to-text.xml'
		assertPrepareRename(xml, "file:///test/tei.xml", null);
		assertPrepareRename(xml, "file:///test/foo.xml", null);
	}
	
	@Test
	public void noPrepareRenameResultWichMatchesPattern() throws BadLocationException {
		String xml = "<from>|</from>";
		// Elements from matches pattern of "xml.references" with 'text-to-text.xml'
		assertPrepareRename(xml, "file:///test/text-to-text.xml", null);
		assertPrepareRename(xml, "file:///test/foo.xml", null);
	}
	
	@Test
	public void noPrepareRenameInEmptyContent() throws BadLocationException {
		String xml = "<from>#A | #B</from>";
		// Elements from matches pattern of "xml.references" with 'text-to-text.xml'
		assertPrepareRename(xml, "file:///test/text-to-text.xml", null);
		assertPrepareRename(xml, "file:///test/foo.xml", null);
	}
	
	private static void assertPrepareRename(String value, String fileURI,
			PrepareRenameResult expected) throws BadLocationException {
		XMLLanguageService xmlLanguageService = new XMLLanguageService();
		xmlLanguageService.getExtensions();
		xmlLanguageService.doSave(new SettingsSaveContext(XMLReferencesSettingsForTest.createXMLReferencesSettings()));

		XMLAssert.assertPrepareRename(xmlLanguageService, value, fileURI, expected);
	}

}