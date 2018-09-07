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
import org.junit.Ignore;
import org.junit.Test;

/**
 * XML completion services tests
 *
 */
public class XMLCompletionTest {

	private XMLLanguageService languageService;
	private CompletionSettings sharedCompletionSettings = new CompletionSettings();

	@Before
	public void initializeLanguageService() {
		languageService = new XMLLanguageService();
	}

	@Test
	public void successfulEndTagCompletion() {
		assertEndTagCompletion("<a>|", 3, "$0</a>");
		assertEndTagCompletion("<a><b>|</a>", 6, "$0</b>");
		assertEndTagCompletion("<a>   <b>|</a>", 9, "$0</b>");
		assertEndTagCompletion("<a><b>|", 6, "$0</b>");
	}

	@Test
	public void unneededEndTagCompletion() {
		assertEndTagCompletion("<a><a>|</a>", 6, "$0</a>");
		assertEndTagCompletion("<a><b>|</b></a>", 6, "$0</b>");
	}

	@Test
	@Ignore
	public void startTagOpenBracket() {
		assertOpenStartTagCompletion("<hello><h|</hello>", 8, "hello", "h");
		assertOpenStartTagCompletion("<test1><hello><h|</hello>", 15, "hello", "h");
		assertOpenStartTagCompletion("<bae><bee></bee><b|<cee></cee></bae>", 17, "bae", "bee", "b");
		assertOpenStartTagCompletion("<ata><akk><atp><at|</atp></akk></ata>", 16, "ata", "atp", "at");
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

	// -------------------Tools----------------------------------------------------------

	public void assertOpenStartTagCompletion(String xmlText, int expectedStartTagOffset, String... expectedTag) {

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

			assertEquals("<" + currentTag + ">", completionItem.getLabel());
			assertEquals(currentTag, completionItem.getFilterText());
			try {
				Range range = completionItem.getTextEdit().getRange();
				assertEquals(expectedStartTagOffset, xmlDocument.offsetAt(range.getStart()));
			} catch (Exception e) {
				fail("Couldn't get offset at position");
			}
			assertEquals(currentTextEdit, completionItem.getTextEdit().getNewText());
		}
	}

	private void assertEndTagCompletion(String xmlText, int expectedEndTagStartOffset, String expectedTextEdit) {

		int offset = getOffset(xmlText);
		XMLDocument xmlDocument = initializeXMLDocument(xmlText, offset);
		CompletionList completionList = initializeCompletion(xmlText, xmlDocument, offset);

		if (expectedTextEdit == null) {// Tag is already closed
			assertEquals(0, completionList.getItems().size());
		} else {
			assertEquals(1, completionList.getItems().size());
			CompletionItem item = completionList.getItems().get(0);
			assertEquals(expectedTextEdit.substring(2), item.getLabel());
			assertEquals(expectedTextEdit.substring(2), item.getFilterText());

			try {
				Range range = item.getTextEdit().getRange();
				assertEquals(expectedEndTagStartOffset, xmlDocument.offsetAt(range.getStart()));
			} catch (Exception e) {
				fail("Couldn't get offset at position");
			}
			assertEquals(expectedTextEdit, item.getTextEdit().getNewText());
		}
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
