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

import java.util.List;

import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.dom.XMLParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XML highlighting services tests
 *
 */
public class XMLHighlightingTest {

	private XMLLanguageService languageService;

	@Before
	public void initializeLanguageService() {
		languageService = new XMLLanguageService();
	}

	@Test
	public void single() throws BadLocationException {
		assertHighlights("|<html></html>", new int[] {}, null);
		assertHighlights("<|html></html>", new int[] { 1, 8 }, "html");
		assertHighlights("<h|tml></html>", new int[] { 1, 8 }, "html");
		assertHighlights("<htm|l></html>", new int[] { 1, 8 }, "html");
		assertHighlights("<html|></html>", new int[] { 1, 8 }, "html");
		assertHighlights("<html>|</html>", new int[] {}, null);
		assertHighlights("<html><|/html>", new int[] {}, null);
		assertHighlights("<html></|html>", new int[] { 1, 8 }, "html");
		assertHighlights("<html></h|tml>", new int[] { 1, 8 }, "html");
		assertHighlights("<html></ht|ml>", new int[] { 1, 8 }, "html");
		assertHighlights("<html></htm|l>", new int[] { 1, 8 }, "html");
		assertHighlights("<html></html|>", new int[] { 1, 8 }, "html");
		assertHighlights("<html></html>|", new int[] {}, null);
	}

	@Test
	public void nested() throws BadLocationException {
		assertHighlights("<html>|<div></div></html>", new int[] {}, null);
		assertHighlights("<html><|div></div></html>", new int[] { 7, 13 }, "div");
		assertHighlights("<html><div>|</div></html>", new int[] {}, null);
		assertHighlights("<html><div></di|v></html>", new int[] { 7, 13 }, "div");
		assertHighlights("<html><div><div></div></di|v></html>", new int[] { 7, 24 }, "div");
		assertHighlights("<html><div><div></div|></div></html>", new int[] { 12, 18 }, "div");
		assertHighlights("<html><div><div|></div></div></html>", new int[] { 12, 18 }, "div");
		assertHighlights("<html><div><div></div></div></h|tml>", new int[] { 1, 30 }, "html");
		assertHighlights("<html><di|v></div><div></div></html>", new int[] { 7, 13 }, "div");
		assertHighlights("<html><div></div><div></d|iv></html>", new int[] { 18, 24 }, "div");
	}

	@Test
	public void selfclosed() throws BadLocationException {
		assertHighlights("<html><|div/></html>", new int[] { 7 }, "div");
		assertHighlights("<html><|br></html>", new int[] { 7 }, "br");
		assertHighlights("<html><div><d|iv/></div></html>", new int[] { 12 }, "div");
	}

	@Test
	public void caseInsensivity() throws BadLocationException {
		assertHighlights("<HTML><diV><Div></dIV></dI|v></html>", new int[] { 7, 24 }, "div");
		assertHighlights("<HTML><diV|><Div></dIV></dIv></html>", new int[] { 7, 24 }, "div");
	}
	
	@Test
	public void insideEndTag() throws BadLocationException {		
		assertHighlights("<html|></meta></html>", new int[] { 1, 15 }, "html");
	}

	private void assertHighlights(String value, int[] expectedMatches, String elementName) throws BadLocationException {
		int offset = value.indexOf("|");
		value = value.substring(0, offset) + value.substring(offset + 1);

		XMLDocument document = XMLParser.getInstance().parse(value, "test://test/test.html");

		Position position = document.positionAt(offset);
		// XMLDocument htmlDoc = ls.parseHTMLDocument(document);

		List<DocumentHighlight> highlights = languageService.findDocumentHighlights(document, position);
		Assert.assertEquals(expectedMatches.length, highlights.size());
		for (int i = 0; i < highlights.size(); i++) {
			DocumentHighlight highlight = highlights.get(i);
			int actualStartOffset = document.offsetAt(highlight.getRange().getStart());
			Assert.assertEquals(expectedMatches[i], actualStartOffset);
			int actualEndOffset = document.offsetAt(highlight.getRange().getEnd());
			Assert.assertEquals(expectedMatches[i] + (elementName != null ? elementName.length() : 0), actualEndOffset);
			Assert.assertEquals(elementName,
					document.getText().substring(actualStartOffset, actualEndOffset).toLowerCase());
		}
	}

}
