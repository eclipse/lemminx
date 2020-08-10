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
package org.eclipse.lemminx.extensions.relaxng.grammar;

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
 * XML completion tests for RelaxNG grammar.
 *
 */
public class XMLCompletionBasedForRelaxNGGrammarTest extends BaseFileTempTest {

	@Test
	public void completionOnRoot() throws BadLocationException {
		// completion on <|
		String xml = "<grammar xmlns=\"http://relaxng.org/ns/structure/1.0\">\r\n" + //
				"  <|\r\n" + //
				"</grammar>";
		testCompletionFor(xml, //
				4 + CDATA_SNIPPETS + COMMENT_SNIPPETS, //
				c("include", te(1, 2, 1, 3, "<include href=\"\"></include>"), "<include"), //
				c("div", te(1, 2, 1, 3, "<div></div>"), "<div"), //
				c("start", te(1, 2, 1, 3, "<start></start>"), "<start"), //
				c("define", te(1, 2, 1, 3, "<define name=\"\"></define>"), "<define"));
	}

	@Test
	public void completionOnElementNoName() throws BadLocationException {
		// completion on <|
		String xml = "<grammar xmlns=\"http://relaxng.org/ns/structure/1.0\"\r\n" + //
				"	datatypeLibrary=\"http://www.w3.org/2001/XMLSchema-datatypes\">\r\n" + //
				"	<start>\r\n" + //
				"		<ref name=\"foo\"/>\r\n" + //
				"	</start>\r\n" + //
				"	<define name=\"foo\">\r\n" + //
				"			<element>\r\n" + //
				"				<|\r\n" + //
				"			</element>\r\n" + //
				"	</define>\r\n" + //
				"</grammar>";
		testCompletionFor(xml, //
				4 + CDATA_SNIPPETS + COMMENT_SNIPPETS, //
				c("choice", te(7, 4, 7, 5, "<choice></choice>"), "<choice"), //
				c("nsName", te(7, 4, 7, 5, "<nsName></nsName>"), "<nsName"), //
				c("name", te(7, 4, 7, 5, "<name></name>"), "<name"), //
				c("anyName", te(7, 4, 7, 5, "<anyName></anyName>"), "<anyName"));
	}

	@Test
	public void completionOnElementWithName() throws BadLocationException {
		// completion on <|
		String xml = "<grammar xmlns=\"http://relaxng.org/ns/structure/1.0\"\r\n" + //
				"	datatypeLibrary=\"http://www.w3.org/2001/XMLSchema-datatypes\">\r\n" + //
				"	<start>\r\n" + //
				"		<ref name=\"foo\"/>\r\n" + //
				"	</start>\r\n" + //
				"	<define name=\"foo\">\r\n" + //
				"			<element name=\"foo\">\r\n" + //
				"				<|\r\n" + //
				"			</element>\r\n" + //
				"	</define>\r\n" + //
				"</grammar>";
		testCompletionFor(xml, //
				19 + CDATA_SNIPPETS + COMMENT_SNIPPETS, //
				c("interleave", te(7, 4, 7, 5, "<interleave></interleave>"), "<interleave"), //
				c("zeroOrMore", te(7, 4, 7, 5, "<zeroOrMore></zeroOrMore>"), "<zeroOrMore"), //
				c("grammar", te(7, 4, 7, 5, "<grammar></grammar>"), "<grammar"), //
				c("ref", te(7, 4, 7, 5, "<ref name=\"\"></ref>"), "<ref"), //
				c("value", te(7, 4, 7, 5, "<value></value>"), "<value"), //
				c("choice", te(7, 4, 7, 5, "<choice></choice>"), "<choice"), //
				c("notAllowed", te(7, 4, 7, 5, "<notAllowed></notAllowed>"), "<notAllowed"), //
				c("element", te(7, 4, 7, 5, "<element></element>"), "<element"), //
				c("group", te(7, 4, 7, 5, "<group></group>"), "<group"), //
				c("attribute", te(7, 4, 7, 5, "<attribute></attribute>"), "<attribute"), //
				c("text", te(7, 4, 7, 5, "<text></text>"), "<text"), //
				c("data", te(7, 4, 7, 5, "<data type=\"\"></data>"), "<data"), //
				c("oneOrMore", te(7, 4, 7, 5, "<oneOrMore></oneOrMore>"), "<oneOrMore"), //
				c("externalRef", te(7, 4, 7, 5, "<externalRef href=\"\"></externalRef>"), "<externalRef"), //
				c("list", te(7, 4, 7, 5, "<list></list>"), "<list"), //
				c("parentRef", te(7, 4, 7, 5, "<parentRef name=\"\"></parentRef>"), "<parentRef"), //
				c("empty", te(7, 4, 7, 5, "<empty></empty>"), "<empty"), //
				c("optional", te(7, 4, 7, 5, "<optional></optional>"), "<optional"), //
				c("mixed", te(7, 4, 7, 5, "<mixed></mixed>"), "<mixed"));
	}

	private static void testCompletionFor(String value, Integer expectedCount, CompletionItem... expectedItems)
			throws BadLocationException {
		XMLAssert.testCompletionFor(new XMLLanguageService(), value, null, null, "src/test/resources/relaxng/test.xml",
				expectedCount, true, expectedItems);
	}

}