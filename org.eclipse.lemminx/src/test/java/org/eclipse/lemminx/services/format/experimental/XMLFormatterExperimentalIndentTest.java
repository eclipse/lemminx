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
package org.eclipse.lemminx.services.format.experimental;

import static java.lang.System.lineSeparator;
import static org.eclipse.lemminx.XMLAssert.te;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.TextEdit;
import org.junit.jupiter.api.Test;

/**
 * XML experimental formatter services tests with indentation.
 *
 */
public class XMLFormatterExperimentalIndentTest extends AbstractCacheBasedTest {

	@Test
	public void startWithSpaces() throws BadLocationException {
		String content = "\r\n    " + //
				"   <a></a>";
		String expected = "<a></a>";
		assertFormat(content, expected, //
				te(0, 0, 1, 7, ""));
		assertFormat(expected, expected);
	}

	@Test
	public void oneElementsInSameLine() throws BadLocationException {
		String content = "<a></a>";
		String expected = content;
		assertFormat(content, expected);

		content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<a></a>";
		expected = content;
		assertFormat(content, expected);
	}

	@Test
	public void oneElementsInDifferentLine() throws BadLocationException {
		String content = "<a>\r\n" + //
				"</a>";
		String expected = "<a> </a>";
		assertFormat(content, expected, //
				te(0, 3, 1, 0, " "));
		assertFormat(expected, expected);
	}

	@Test
	public void oneElementsInDifferentLineWithSpace() throws BadLocationException {
		String content = "<a>\r\n" + //
				"  </a>";
		String expected = "<a> </a>";
		assertFormat(content, expected, //
				te(0, 3, 1, 2, " "));
		assertFormat(expected, expected);
	}

	@Test
	public void twoElementsInSameLine() throws BadLocationException {
		String content = "<a><b></b></a>";
		String expected = "<a>" + lineSeparator() + //
				"  <b></b>" + lineSeparator() + //
				"</a>";
		assertFormat(content, expected, //
				te(0, 3, 0, 3, lineSeparator() + "  "), //
				te(0, 10, 0, 10, lineSeparator()));
		assertFormat(expected, expected);
	}

	@Test
	public void textSpaces() throws BadLocationException {
		String content = "<a>b  c</a>";
		String expected = "<a>b c</a>";
		assertFormat(content, expected, //
				te(0, 4, 0, 6, " "));
		assertFormat(expected, expected);
	}

	@Test
	public void mixedContent() throws BadLocationException {
		String content = "<a><b>B</b></a>";
		String expected = "<a>" + lineSeparator() + //
				"  <b>B</b>" + lineSeparator() + // indent with 2 spaces
				"</a>";
		assertFormat(content, expected, //
				te(0, 3, 0, 3, lineSeparator() + "  "), // indent with 2 spaces
				te(0, 11, 0, 11, lineSeparator()));
		assertFormat(expected, expected);
	}

	@Test
	public void mixedContentWithTabs4Spaces() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setInsertSpaces(true);
		settings.getFormattingSettings().setTabSize(4);

		String content = "<a><b>B</b></a>";
		String expected = "<a>" + lineSeparator() + //
				"    <b>B</b>" + lineSeparator() + // indent with 4 spaces
				"</a>";
		assertFormat(content, expected, settings, //
				te(0, 3, 0, 3, lineSeparator() + "    "), // indent with 4 spaces
				te(0, 11, 0, 11, lineSeparator()));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void mixedContentWithTabs() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setInsertSpaces(false);

		String content = "<a><b>B</b></a>";
		String expected = "<a>" + lineSeparator() + //
				"	<b>B</b>" + lineSeparator() + // indent with one tab
				"</a>";
		assertFormat(content, expected, settings, //
				te(0, 3, 0, 3, lineSeparator() + "	"), // indent with one tab
				te(0, 11, 0, 11, lineSeparator()));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void mixedContent2() throws BadLocationException {
		String content = "<a>A<b>B</b></a>";
		String expected = content;
		assertFormat(content, expected);
	}

	private static void assertFormat(String unformatted, String actual, TextEdit... expectedEdits)
			throws BadLocationException {
		assertFormat(unformatted, actual, new SharedSettings(), expectedEdits);
	}

	private static void assertFormat(String unformatted, String expected, SharedSettings sharedSettings,
			TextEdit... expectedEdits) throws BadLocationException {
		assertFormat(unformatted, expected, sharedSettings, "test://test.html", expectedEdits);
	}

	private static void assertFormat(String unformatted, String expected, SharedSettings sharedSettings, String uri,
			TextEdit... expectedEdits) throws BadLocationException {
		assertFormat(unformatted, expected, sharedSettings, uri, true, expectedEdits);
	}

	private static void assertFormat(String unformatted, String expected, SharedSettings sharedSettings, String uri,
			Boolean considerRangeFormat, TextEdit... expectedEdits) throws BadLocationException {
		// Force to "experimental" formatter
		sharedSettings.getFormattingSettings().setExperimental(true);
		XMLAssert.assertFormat(null, unformatted, expected, sharedSettings, uri, considerRangeFormat, expectedEdits);
	}
}
