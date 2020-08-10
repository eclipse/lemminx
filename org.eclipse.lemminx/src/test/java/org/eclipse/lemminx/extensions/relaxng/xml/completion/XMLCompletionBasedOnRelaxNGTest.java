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

import static org.eclipse.lemminx.XMLAssert.CDATA_SNIPPETS;
import static org.eclipse.lemminx.XMLAssert.COMMENT_SNIPPETS;
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
				1 /*  teiHeader */ + CDATA_SNIPPETS + COMMENT_SNIPPETS, //
				c("teiHeader", te(3, 2, 3, 3, "<teiHeader></teiHeader>"), "<teiHeader"));
	}

	private static void testCompletionFor(String value, Integer expectedCount, CompletionItem... expectedItems)
			throws BadLocationException {
		XMLAssert.testCompletionFor(new XMLLanguageService(), value, null, null, "src/test/resources/relaxng/test.xml",
				expectedCount, true, expectedItems);
	}

}