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
import java.util.stream.Collectors;

import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.internal.parser.XMLParser;
import org.eclipse.lsp4xml.model.XMLDocument;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static java.lang.System.lineSeparator;

/**
 * XML formatter services tests
 *
 */
public class XMLFormatterTest {

	private XMLLanguageService languageService;

	@Before
	public void initializeLanguageService() {
		languageService = new XMLLanguageService();
	}

	@Test
	public void fullDocument() throws BadLocationException {
		String content = "<div  class = \"foo\">\n" + //
				"<br>\n" + //
				" </div>";
		String expected = "<div class=\"foo\">\n" + //
				"  <br></br>\n" + //
				"</div>";
		format(content, expected);
	}

	@Test
	public void range() throws BadLocationException {
		String content = "<div  class = \"foo\">\n" + //
				"  |<img  src = \"foo\">|\n" + //
				" </div>";
		String expected = "<div  class = \"foo\">\n" + //
				"  <img src=\"foo\" />\n" + //
				" </div>";
		format(content, expected);
	}

	@Test
	public void range2() throws BadLocationException {
		String content = "<div  class = \"foo\">\n" + //
				"  |<img  src = \"foo\">|\n" + //
				" \n" + //
				" </div>";
		String expected = "<div  class = \"foo\">\n" + //
				"  <img src=\"foo\" />\n" + //
				" \n" + //
				" </div>";
		format(content, expected);
	}

	@Test
	public void testProlog() throws BadLocationException {
		String content = "<?xml version=   \"1.0\"       encoding=\"UTF-8\"  ?>" + lineSeparator();
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lineSeparator();
		format(content, expected);
	}

	@Test
	public void testProlog2() throws BadLocationException {
		String content = "<?xml version=   \"1.0\"       encoding=\"UTF-8\"  ?><a>bb</a>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lineSeparator() + //
				"<a>bb</a>";
		format(content, expected);
	}

	@Test
	public void testProlog3() throws BadLocationException {
		String content = "<?xml version=   \"1.0\"       encoding=\"UTF-8\"  ?><a><b>c</b></a>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lineSeparator() + //
				"<a>" + lineSeparator() + //
				"  <b>c</b>" + lineSeparator() + //
				"</a>";
		format(content, expected);
	}

	@Test
	public void testPI() throws BadLocationException {
		String content = "<a><?m2e asd as das das ?></a>";
		String expected = "<a>" + lineSeparator() + //
				"  <?m2e asd as das das ?>" + lineSeparator() + //
				"</a>";
		format(content, expected);
	}

	private void format(String unformatted, String actual) throws BadLocationException {
		format(unformatted, actual, true);
	}

	private void format(String unformatted, String expected, boolean insertSpaces) throws BadLocationException {
		Range range = null;
		String uri = "test://test.html";
		int rangeStart = unformatted.indexOf('|');
		int rangeEnd = unformatted.lastIndexOf('|');
		if (rangeStart != -1 && rangeEnd != -1) {
			// remove '|'
			unformatted = unformatted.substring(0, rangeStart) + unformatted.substring(rangeStart + 1, rangeEnd)
					+ unformatted.substring(rangeEnd + 1);
			XMLDocument unformattedDoc = XMLParser.getInstance().parse(unformatted, uri, XMLFormatter.FORMAT_MASK);
			Position startPos = unformattedDoc.positionAt(rangeStart);
			Position endPos = unformattedDoc.positionAt(rangeEnd - 1);
			range = new Range(startPos, endPos);
		}

		TextDocument document = new TextDocument(unformatted, uri);
		List<? extends TextEdit> edits = languageService.format(document, range,
				new FormattingOptions(2, insertSpaces));
		String formatted = edits.stream().map(edit -> edit.getNewText()).collect(Collectors.joining(""));
		if (rangeStart != -1 && rangeEnd != -1) {
			formatted = unformatted.substring(0, rangeStart) + formatted
					+ unformatted.substring(rangeEnd - 1, unformatted.length());
		}
		Assert.assertEquals(expected, formatted);
	}
}
