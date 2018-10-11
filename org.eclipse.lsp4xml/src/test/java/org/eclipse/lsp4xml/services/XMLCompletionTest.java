/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.services;

import static org.eclipse.lsp4xml.XMLAssert.c;
import static org.eclipse.lsp4xml.XMLAssert.r;
import static org.eclipse.lsp4xml.XMLAssert.testCompletionFor;
import static org.eclipse.lsp4xml.XMLAssert.testTagCompletion;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.dom.XMLParser;
import org.eclipse.lsp4xml.services.extensions.CompletionSettings;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;
import org.junit.Before;
import org.junit.Test;

/**
 * XML completion services tests
 *
 */
public class XMLCompletionTest {

	private XMLLanguageService languageService;
	private CompletionSettings sharedCompletionSettings;

	@Before
	public void initializeLanguageService() {
		languageService = new XMLLanguageService();
		sharedCompletionSettings = new CompletionSettings();

	}

	@Test
	public void successfulEndTagCompletion() throws BadLocationException {
		testCompletionFor("<a>|", 1, c("End with '</a>'", "</a>", r(0, 3, 0, 3), "</a>"));
		testCompletionFor("<a>a|", 1, c("End with '</a>'", "</a>", r(0, 3, 0, 4), "</a>"));
		testCompletionFor("<a><|", 1, c("End with '</a>'", "/a>", r(0, 4, 0, 4), "/a>"));
		testCompletionFor("<a></|", 1, c("End with '</a>'", "/a>", r(0, 4, 0, 5), "/a>"));

		testCompletionFor("<a><b>|</a>", 1, c("End with '</b>'", "</b>", r(0, 6, 0, 6), "</b>"));
		testCompletionFor("<a><b><|</a>", 1, c("End with '</b>'", "/b>", r(0, 7, 0, 7), "/b>"));
		testCompletionFor("<a><b></|</a>", 1, c("End with '</b>'", "/b>", r(0, 7, 0, 8), "/b>"));

		testCompletionFor("<a>   <b>|</a>", 1, c("End with '</b>'", "</b>", r(0, 9, 0, 9), "</b>"));
		testCompletionFor("<a>   <b><|</a>", 1, c("End with '</b>'", "/b>", r(0, 10, 0, 10), "/b>"));
		testCompletionFor("<a>   <b></|</a>", 1, c("End with '</b>'", "/b>", r(0, 10, 0, 11), "/b>"));

		testCompletionFor("<a><b>|", 2, c("End with '</b>'", "</b>", r(0, 6, 0, 6), "</b>"),
				c("End with '</a>'", "</a>", r(0, 6, 0, 6), "</a>"));
		testCompletionFor("<a><b><|", 2, c("End with '</b>'", "/b>", r(0, 7, 0, 7), "/b>"),
				c("End with '</a>'", "/a>", r(0, 7, 0, 7), "/a>"));
		testCompletionFor("<a><b></|", 2, c("End with '</b>'", "/b>", r(0, 7, 0, 8), "/b>"),
				c("End with '</a>'", "/a>", r(0, 7, 0, 8), "/a>"));
	}

	@Test
	public void successfulEndTagCompletionWithIndent() throws BadLocationException {
		testCompletionFor("  <a>\r\n" + //
				"|", 3, //
				c("End with '</a>'", "  </a>", r(1, 0, 1, 0), "</a>"), //
				c("#region", "<!-- #region $1-->", r(1, 0, 1, 0), ""), //
				c("#endregion", "<!-- #endregion-->", r(1, 0, 1, 0), ""));
		testCompletionFor("  <a>\r\n" + //
				"<|", 1, //
				c("End with '</a>'", "  </a>", r(1, 0, 1, 1), "</a>"));
		testCompletionFor("<a></|", 1, c("End with '</a>'", "/a>", r(0, 4, 0, 5), "/a>"));

		testCompletionFor("  <a>\r\n" + //
				"     <b>\r\n" + //
				"<|", 2, //
				c("End with '</b>'", "     </b>", r(2, 0, 2, 1), "</b>"), //
				c("End with '</a>'", "  </a>", r(2, 0, 2, 1), "</a>"));
	}

	@Test
	public void unneededEndTagCompletion() throws BadLocationException {
		testCompletionFor("<a>|</a>", 0);
		testCompletionFor("<a><|</a>", 0);
		testCompletionFor("<a></|</a>", 0);

		testCompletionFor("<a><b>|</b></a>", 0);
		testCompletionFor("<a><b><|</b></a>", 0);
		testCompletionFor("<a><b></|</b></a>", 0);
	}

	@Test
	public void startTagOpenBracket() throws BadLocationException {
		testCompletionFor("<hello><h|</hello>", 1, c("h", "<h></h>", "<h"));
		testCompletionFor("<hello><h1/><h2></h2><h|</hello>", 3, c("h", "<h></h>", "<h"), c("h1", "<h1 />", "<h1"),
				c("h2", "<h2></h2>", "<h2"));
	}

	@Test
	public void replaceRangeOUnusedfCloseTag() throws BadLocationException {
		testCompletionFor("<hello><h|></hello>", 1, c("h", "<h></h>", "<h"));
		testCompletionFor("<hello><h1/><h2></h2><h|</hello>", 3, c("h", "<h></h>", "<h"), c("h1", "<h1 />", "<h1"),
				c("h2", "<h2></h2>", "<h2"));
	}

	@Test
	public void doTagComplete() throws BadLocationException {
		testTagCompletion("<div>|", "$0</div>");
		testTagCompletion("<div>|</div>", null);
		testTagCompletion("<div class=\"\">|", "$0</div>");
		testTagCompletion("<img />|", null);
		testTagCompletion("<div><br /></|", "div>");
		testTagCompletion("<div><br /><span></span></|", "div>");
		// testTagCompletion("<div><h1><br /><span></span><img /></| </h1></div>",
		// "h1>");
	}

	@Test
	public void testAutoCloseTagCompletion() {
		assertAutoCloseEndTagCompletion("<a>|", "$0</a>");
		assertAutoCloseEndTagCompletion("<a><b>|</a>", "$0</b>");
		assertAutoCloseEndTagCompletion("<a>   <b>|</a>", "$0</b>");
		assertAutoCloseEndTagCompletion("<a><b>|", "$0</b>");
	}

	@Test
	public void testAutoCloseEnabledDisabled() throws BadLocationException {
		testCompletionFor("<a><div|<a>", false, c("div", "<div>"));
		testCompletionFor("<a><div|<a>", true, c("div", "<div></div>"));
		testCompletionFor("<a>  <div|    <a>", false, c("div", "<div>"));
		testCompletionFor("<a>   <div|    <a>", true, c("div", "<div></div>"));
	}

	@Test
	public void testAutoCompletionPrologWithXML() throws BadLocationException {
		//With 'xml' label
		testCompletionFor("<?xml|", false, c("<?xml ... ?>", "xml version=\"1.0\" encoding=\"UTF-8\"?>$0", r(0, 2, 0, 5),
				"xml version=\"1.0\" encoding=\"UTF-8\"?>"));
		testCompletionFor("<?xml|>", true, c("<?xml ... ?>", "xml version=\"1.0\" encoding=\"UTF-8\"?>$0", r(0, 2, 0, 6),
				"xml version=\"1.0\" encoding=\"UTF-8\"?>"));
		testCompletionFor("<?xml|?>", true, c("<?xml ... ?>", "xml version=\"1.0\" encoding=\"UTF-8\"?>$0", r(0, 2, 0, 7),
				"xml version=\"1.0\" encoding=\"UTF-8\"?>"));
	}

	@Test
	public void testAutoCompletionPrologWithoutXML() throws BadLocationException {
		//No 'xml' label
		testCompletionFor("<?|", false, c("<?xml ... ?>", "xml version=\"1.0\" encoding=\"UTF-8\"?>$0", r(0, 2, 0, 2),
			"xml version=\"1.0\" encoding=\"UTF-8\"?>"));
		testCompletionFor("<?|>", true, c("<?xml ... ?>", "xml version=\"1.0\" encoding=\"UTF-8\"?>$0", r(0, 2, 0, 3),
			"xml version=\"1.0\" encoding=\"UTF-8\"?>"));
		testCompletionFor("<?|?>", true, c("<?xml ... ?>", "xml version=\"1.0\" encoding=\"UTF-8\"?>$0", r(0, 2, 0, 4),
			"xml version=\"1.0\" encoding=\"UTF-8\"?>"));
	}

	@Test
	public void testAutoCompletionPrologWithPartialXML() throws BadLocationException {
		testCompletionFor("<?x|", false, c("<?xml ... ?>", "xml version=\"1.0\" encoding=\"UTF-8\"?>$0", r(0, 2, 0, 3),
			"xml version=\"1.0\" encoding=\"UTF-8\"?>"));
		testCompletionFor("<?xm|", false, c("<?xml ... ?>", "xml version=\"1.0\" encoding=\"UTF-8\"?>$0", r(0, 2, 0, 4),
			"xml version=\"1.0\" encoding=\"UTF-8\"?>"));
		testCompletionFor("<?x|", false, c("<?xml ... ?>", "xml version=\"1.0\" encoding=\"UTF-8\"?>$0", r(0, 2, 0, 3),
			"xml version=\"1.0\" encoding=\"UTF-8\"?>"));
		testCompletionFor("<?xm|?>", false, c("<?xml ... ?>", "xml version=\"1.0\" encoding=\"UTF-8\"?>$0", r(0, 2, 0, 6),
			"xml version=\"1.0\" encoding=\"UTF-8\"?>"));
	}

	// -------------------Tools----------------------------------------------------------

	public void assertOpenStartTagCompletion(String xmlText, int expectedStartTagOffset, boolean startWithTagOpen,
			String... expectedTag) {

		List<String> expectedTags = Arrays.asList(expectedTag);
		int offset = getOffset(xmlText);
		XMLDocument xmlDocument = initializeXMLDocument(xmlText, offset);
		CompletionList completionList = initializeCompletion(xmlText, xmlDocument, offset);
		String currentTag, currentTextEdit;
		CompletionItem completionItem;

		assertEquals(expectedTag.length, completionList.getItems().size());
		for (int i = 0; i < expectedTag.length; i++) {
			currentTag = expectedTags.get(i);
			currentTextEdit = createTextEditElement(currentTag);
			completionItem = completionList.getItems().get(i);

			assertEquals(currentTag, completionItem.getLabel());
			assertEquals(startWithTagOpen ? "<" + currentTag : currentTag, completionItem.getFilterText());
			try {
				Range range = completionItem.getTextEdit().getRange();
				assertEquals(expectedStartTagOffset, xmlDocument.offsetAt(range.getStart()));
			} catch (Exception e) {
				fail("Couldn't get offset at position");
			}
			assertEquals(currentTextEdit, completionItem.getTextEdit().getNewText());
		}
	}

	public void assertAutoCloseEndTagCompletion(String xmlText, String expectedTextEdit) {
		int offset = getOffset(xmlText);
		XMLDocument xmlDocument = initializeXMLDocument(xmlText, offset);
		Position position = null;
		try {
			position = xmlDocument.positionAt(offset);
		} catch (Exception e) {
			fail("Couldn't get position at offset");
		}
		String completionList = languageService.doTagComplete(xmlDocument, position);
		assertEquals(expectedTextEdit, completionList);
	}

	public int getOffset(String xmlText) {
		return xmlText.indexOf("|");
	}

	public XMLDocument initializeXMLDocument(String xmlText, int offset) {
		xmlText = xmlText.substring(0, offset) + xmlText.substring(offset + 1);
		return XMLParser.getInstance().parse(xmlText, "test:uri");
	}

	public CompletionList initializeCompletion(String xmlText, XMLDocument xmlDocument, int offset) {
		Position position = null;
		try {
			position = xmlDocument.positionAt(offset);
		} catch (Exception e) {
			fail("Couldn't get position at offset");
		}
		CompletionList completionList = languageService.doComplete(xmlDocument, position, sharedCompletionSettings,
				new XMLFormattingOptions(4, false));
		return completionList;
	}

	// TextEdits are created with the "<" symbol already existing and offset set to
	// the first character
	// of the tag name
	public String createTextEditElement(String tag) {
		return tag + ">$0</" + tag + ">";
	}
}
