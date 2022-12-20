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

import static org.eclipse.lemminx.XMLAssert.hl;
import static org.eclipse.lemminx.XMLAssert.r;
import static org.eclipse.lsp4j.DocumentHighlightKind.Read;
import static org.eclipse.lsp4j.DocumentHighlightKind.Write;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.XMLAssert.SettingsSaveContext;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.references.settings.XMLReferenceExpression;
import org.eclipse.lemminx.extensions.references.settings.XMLReferences;
import org.eclipse.lemminx.extensions.references.settings.XMLReferencesSettings;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lsp4j.DocumentHighlight;
import org.junit.jupiter.api.Test;

/**
 * XML references highlighting tests.
 *
 * @author Angelo ZERR
 */
public class XMLReferencesHighlightingExtensionsTest extends AbstractCacheBasedTest {

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
				+ "      <anchor corresp=\"#bo|dy-id\"></anchor>\r\n" // <-- // highlighting here should highlight
																		// body/@xml-id="body-id"
				+ "    </body>\r\n"
				+ "  </text>\r\n"
				+ "</TEI>";
		testHighlightsFor(xml, "file:///test/tei.xml", //
				hl(r(18, 22, 18, 32), Read), hl(r(16, 17, 16, 26), Write));
	}

	@Test
	public void docbook() throws BadLocationException {
		// highlighting on define/@name
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				// + "<!DOCTYPE book PUBLIC \"-//OASIS//DTD DocBook XML V4.4//EN\"
				// \"http://www.docbook.org/xml/4.4/docbookx.dtd\">\r\n"
				+ "<book>\r\n"
				+ "    <chapter id=\"chapter-1\">\r\n"
				+ "\r\n"
				+ "        <xref linkend=\"chapt|er-1\" />\r\n" // <-- // highlighting here should highlight
				// chapter/@id="chapter-1"
				+ "\r\n"
				+ "    </chapter>\r\n"
				+ "\r\n"
				+ "    <chapter id=\"chapter-2\">\r\n"
				+ "\r\n"
				+ "    </chapter>\r\n"
				+ "</book>";
		testHighlightsFor(xml, "file:///test/docbook.xml", //
				hl(r(4, 22, 4, 33), Read), hl(r(2, 16, 2, 27), Write));
	}

	private static void testHighlightsFor(String value, String fileURI,
			DocumentHighlight... expected)
			throws BadLocationException {
		XMLReferencesSettings referencesSettings = new XMLReferencesSettings();
		referencesSettings.setReferences(createReferences());
		XMLLanguageService xmlLanguageService = new XMLLanguageService();
		xmlLanguageService.getExtensions();
		xmlLanguageService.doSave(new SettingsSaveContext(referencesSettings));

		XMLAssert.testHighlightsFor(xmlLanguageService, value, fileURI, expected);
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

		return references;
	}
}
