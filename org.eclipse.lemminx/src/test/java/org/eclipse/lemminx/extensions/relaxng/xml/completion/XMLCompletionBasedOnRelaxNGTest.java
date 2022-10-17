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
package org.eclipse.lemminx.extensions.relaxng.xml.completion;

import static org.eclipse.lemminx.XMLAssert.CATALOG_SNIPPETS;
import static org.eclipse.lemminx.XMLAssert.CDATA_SNIPPETS;
import static org.eclipse.lemminx.XMLAssert.COMMENT_SNIPPETS;
import static org.eclipse.lemminx.XMLAssert.NEW_XML_SNIPPETS;
import static org.eclipse.lemminx.XMLAssert.PROCESSING_INSTRUCTION_SNIPPETS;
import static org.eclipse.lemminx.XMLAssert.REGION_SNIPPETS;
import static org.eclipse.lemminx.XMLAssert.c;
import static org.eclipse.lemminx.XMLAssert.te;

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.contentmodel.BaseFileTempTest;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

/**
 * XML completion tests based on RelaxNG.
 *
 */
public class XMLCompletionBasedOnRelaxNGTest extends BaseFileTempTest {

	@Test
	public void completionOnRoot() throws BadLocationException {
		// completion on <|
		String xml = "<?xml-model href=\"tei_all.rng\" ?>\r\n" + //
				"<|";
		testCompletionFor(xml, //
				null, //
				c("TEI", te(1, 0, 1, 1, "<TEI></TEI>"), "<TEI"), //
				c("teiCorpus", te(1, 0, 1, 1, "<teiCorpus></teiCorpus>"), "<teiCorpus"));
	}

	@Test
	public void completionInDocumentElement() throws BadLocationException {
		// completion on <|
		String xml = "<?xml-model href=\"tei_all.rng\" ?>\r\n" + //
				"<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">\r\n" + //
				"  <|\r\n" + //
				"</TEI>";
		testCompletionFor(xml, //
				1 /* teiHeader */ + CDATA_SNIPPETS + COMMENT_SNIPPETS, //
				c("teiHeader", te(2, 2, 2, 3, "<teiHeader></teiHeader>"), "<teiHeader"));
	}

	@Test
	public void completionWithTwoSameRelaxNG() throws BadLocationException {
		// completion on <|
		String xml = "<?xml-model href=\"tei_all.rng\" ?>\r\n" + //
				"<?xml-model href=\"tei_all.rng\" ?>\r\n" + //
				"<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">\r\n" + //
				"  <|\r\n" + //
				"</TEI>";
		testCompletionFor(xml, //
				2 /* 2 * teiHeader */ + CDATA_SNIPPETS + COMMENT_SNIPPETS, //
				c("teiHeader", te(3, 2, 3, 3, "<teiHeader></teiHeader>"), "<teiHeader"));

		xml = "<?xml-model href=\"tei_all.rng\" schematypens=\"http://relaxng.org/ns/structure/1.0\" ?>\r\n" + // <--
																												// applicable
				"<?xml-model href=\"tei_all.rng\" schematypens=\"http://purl.oclc.org/dsdl/schematron\" ?>\r\n" + // //
																													// <--
																													// NOT
																													// applicable
				"<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">\r\n" + //
				"  <|\r\n" + //
				"</TEI>";
		testCompletionFor(xml, //
				1 /* teiHeader */ + CDATA_SNIPPETS + COMMENT_SNIPPETS, //
				c("teiHeader", te(3, 2, 3, 3, "<teiHeader></teiHeader>"), "<teiHeader"));
	}

	@Test
	public void completionOnRootWithXMLNS() throws BadLocationException {
		// completion on <|
		String xml = "<?xml-model href=\"simple.rng\"?>\r\n" + //
				"|";
		testCompletionFor(xml, //
				1 + //
						REGION_SNIPPETS /* #region */ + //
						NEW_XML_SNIPPETS /* DOCTYPE snippets */ + //
						PROCESSING_INSTRUCTION_SNIPPETS /* Processing Instruction snippets */ + //
						COMMENT_SNIPPETS /* Comment snippets */ + //
						CATALOG_SNIPPETS /* Catalog snippets */ , //
				c("rootelt", te(1, 0, 1, 0,
						"<rootelt xmlns:lmx=\"https://github.com/eclipse/lemminx\" xml:lang=\"\" lmx:type=\"\"></rootelt>"),
						"rootelt"));
	}

	@Test
	public void completionInElementWithXMLNS() throws BadLocationException {
		// completion on <|
		String xml = "<?xml-model href=\"simple.rng\"?>\r\n" + //
				"<rootelt xmlns:lmx=\"https://github.com/eclipse/lemminx\" xml:lang=\"\" lmx:type=\"\">\r\n" + //
				"|\r\n" + //
				"</rootelt>";
		testCompletionFor(xml, //
				2 + //
						REGION_SNIPPETS /* #region */ + //
						COMMENT_SNIPPETS /* Comment snippets */ + //
						CDATA_SNIPPETS /* CDATA snippets */ , //
				c("child", te(2, 0, 2, 0,
						"<child xmlns:vx=\"https://github.com/redhat-developer/vscode-xml\" vx:type=\"\"></child>"),
						"child"));

		xml = "<?xml-model href=\"simple.rng\"?>\r\n" + //
				"<rootelt xmlns:lmx=\"https://github.com/eclipse/lemminx\" xml:lang=\"\" lmx:type=\"\">\r\n" + //
				"<|\r\n" + //
				"</rootelt>";
		testCompletionFor(xml, //
				2 + //
						COMMENT_SNIPPETS /* Comment snippets */ + //
						CDATA_SNIPPETS /* CDATA snippets */ , //
				c("child", te(2, 0, 2, 1,
						"<child xmlns:vx=\"https://github.com/redhat-developer/vscode-xml\" vx:type=\"\"></child>"),
						"<child"));
	}

	@Test
	public void completionInElementWithXMLNSAndDefinedNS() throws BadLocationException {
		// completion on <|
		String xml = "<?xml-model href=\"simple.rng\"?>\r\n" + //
				"<rootelt xmlns:lmx=\"https://github.com/eclipse/lemminx\" xmlns:myvx=\"https://github.com/redhat-developer/vscode-xml\" xml:lang=\"\" lmx:type=\"\">\r\n"
				+ //
				"|\r\n" + //
				"</rootelt>";
		testCompletionFor(xml, //
				3 + //
						REGION_SNIPPETS /* #region */ + //
						COMMENT_SNIPPETS /* Comment snippets */ + //
						CDATA_SNIPPETS /* CDATA snippets */ , //
				c("child", te(2, 0, 2, 0, "<child myvx:type=\"\"></child>"), "child"));

		xml = "<?xml-model href=\"simple.rng\"?>\r\n" + //
				"<rootelt xmlns:lmx=\"https://github.com/eclipse/lemminx\" xmlns:myvx=\"https://github.com/redhat-developer/vscode-xml\" xml:lang=\"\" lmx:type=\"\">\r\n"
				+ //
				"<|\r\n" + //
				"</rootelt>";
		testCompletionFor(xml, //
				3 + //
						COMMENT_SNIPPETS /* Comment snippets */ + //
						CDATA_SNIPPETS /* CDATA snippets */ , //
				c("child", te(2, 0, 2, 1, "<child myvx:type=\"\"></child>"), "<child"));
	}

	private static void testCompletionFor(String value, Integer expectedCount, CompletionItem... expectedItems)
			throws BadLocationException {
		XMLAssert.testCompletionFor(new XMLLanguageService(), value, null, null, "src/test/resources/relaxng/test.xml",
				expectedCount, true, expectedItems);
	}

}