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

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.TextEdit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * XML experimental formatter services tests with whitespace setting.
 *
 */
public class XMLFormatterWhitespaceSettingTest {

	@Disabled
	@Test
	public void testTrimTrailingWhitespaceText() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setTrimTrailingWhitespace(true);
		String content = "<a>   \n" + //
				"text     \n" + //
				"    text text text    \n" + //
				"    text\n" + //
				"</a>   ";
		String expected = "<a>\n" + //
				"text\n" + //
				"    text text text\n" + //
				"    text\n" + //
				"</a>";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testTrimTrailingWhitespaceNewlines() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setTrimTrailingWhitespace(true);
		String content = "<a>   \n" + //
				"   \n" + //
				"</a>   ";
		String expected = "<a></a>";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testTrimTrailingWhitespaceTextAndNewlines() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setTrimTrailingWhitespace(true);
		String content = "<a>   \n" + //
				"    \n" + //
				"text     \n" + //
				"    text text text    \n" + //
				"   \n" + //
				"    text\n" + //
				"        \n" + //
				"</a>   ";
		String expected = "<a>\n" + //
				"\n" + //
				"text\n" + //
				"    text text text\n" + //
				"\n" + //
				"    text\n" + //
				"\n" + //
				"</a>";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testDontInsertFinalNewLineWithRange() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setInsertFinalNewline(true);
		String content = "<div  class = \"foo\">\r\n" + //
				"  |<img  src = \"foo\"|/>\r\n" + //
				" </div>";
		String expected = "<div  class = \"foo\">\r\n" + //
				"  <img src=\"foo\" />\r\n" + //
				" </div>";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testInsertFinalNewLineWithRange2() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setInsertFinalNewline(true);
		String content = "<div  class = \"foo\">\r\n" + //
				"  |<img  src = \"foo\"/>\r\n" + //
				" </div>|";
		String expected = "<div  class = \"foo\">\r\n" + //
				"  <img src=\"foo\" />\r\n" + //
				"</div>\r\n";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testInsertFinalNewLineWithRange3() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setInsertFinalNewline(true);
		String content = "<div  class = \"foo\">\r\n" + //
				"  |<img  src = \"foo\"/>\r\n" + //
				"\r\n" + "|" + "\r\n" + //
				"<h1></h1>\r\n" + //
				" </div>";
		String expected = "<div  class = \"foo\">\r\n" + //
				"  <img src=\"foo\" />\r\n" + //
				"\r\n" + //
				"<h1></h1>" + "\r\n" + //
				" </div>";
		assertFormat(content, expected, settings);
	}

	@Test
	public void testDontTrimFinalNewLines() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setTrimFinalNewlines(false);
		String content = "<a  ></a>\r\n\r\n\r\n";
		String expected = "<a></a>\r\n\r\n\r\n";

		assertFormat(content, expected, settings, //
				te(0, 2, 0, 4, ""));
		assertFormat(expected, expected, settings);
	}

	@Disabled
	@Test
	public void testDontTrimFinalNewLines2() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setTrimFinalNewlines(false);
		String content = "<a  ></a>\r\n" + //
				"   \r\n\r\n";
		String expected = "<a></a>\r\n" + //
				"   \r\n\r\n";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testDontTrimFinalNewLines3() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setTrimFinalNewlines(false);
		String content = "<a  ></a>\r\n" + //
				"  text \r\n" + //
				"  more text   \r\n" + //
				"   \r\n";
		String expected = "<a></a>\r\n" + //
				"  text \r\n" + //
				"  more text   \r\n" + //
				"   \r\n";
		assertFormat(content, expected, settings);
	}

	@Test
	public void testFormatRemoveFinalNewlinesWithoutTrimTrailing() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setTrimFinalNewlines(true);
		settings.getFormattingSettings().setTrimTrailingWhitespace(false);
		settings.getFormattingSettings().setSpaceBeforeEmptyCloseTag(false);

		String content = "<aaa/>    \r\n\r\n\r\n";
		String expected = "<aaa/>    ";
		assertFormat(content, expected, settings, //
				te(0, 10, 3, 0, ""));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testClosingBracketNewLine() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		settings.getFormattingSettings().setSplitAttributesIndentSize(0);
		settings.getFormattingSettings().setClosingBracketNewLine(true);
		String content = "<a b='' c=''/>";
		String expected = "<a" + lineSeparator() + "b=''" + lineSeparator() + "c=''" + lineSeparator() + "/>";
		assertFormat(content, expected, settings, //
				te(0, 2, 0, 3, lineSeparator()), //
				te(0, 7, 0, 8, lineSeparator()), //
				te(0, 12, 0, 12, lineSeparator()));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testClosingBracketNewLineWithDefaultIndentSize() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		settings.getFormattingSettings().setClosingBracketNewLine(true);
		settings.getFormattingSettings().setPreserveAttributeLineBreaks(true);
		String content = "<a b='b' c='c'/>";
		String expected = "<a" + System.lineSeparator() + //
				"    b='b'" + System.lineSeparator() + //
				"    c='c'" + System.lineSeparator() + //
				"    />";
		assertFormat(content, expected, settings, //
				te(0, 2, 0, 3, lineSeparator() + "    "), //
				te(0, 8, 0, 9, lineSeparator() + "    "), //
				te(0, 14, 0, 14, lineSeparator() + "    "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testClosingBracketNewLineWithoutSplitAttributes() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(false);
		settings.getFormattingSettings().setClosingBracketNewLine(true);
		String content = "<a b='' c=''/>";
		String expected = "<a b='' c='' />";
		assertFormat(content, expected, settings, //
				te(0, 12, 0, 12, " "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testClosingBracketNewLineWithSingleAttribute() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		settings.getFormattingSettings().setSplitAttributesIndentSize(0);
		settings.getFormattingSettings().setClosingBracketNewLine(true);
		String content = "<a b=''/>";
		String expected = "<a b='' />";
		assertFormat(content, expected, settings,
				te(0, 7, 0, 7, " "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testClosingBracketNewLineWithChildElementIndent() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		settings.getFormattingSettings().setSplitAttributesIndentSize(0);
		settings.getFormattingSettings().setClosingBracketNewLine(true);
		String content = "<a>" + lineSeparator() + "  <b c='' d=''/>" + lineSeparator() + "</a>";
		String expected = "<a>" + lineSeparator() + "  <b" + lineSeparator() + "  c=''" + lineSeparator() + "  d=''"
				+ lineSeparator() + "  />" + lineSeparator() + "</a>";
		assertFormat(content, expected, settings, //
		te(1, 4, 1, 5, lineSeparator() + "  "), //
		te(1, 9, 1, 10, lineSeparator() + "  "), //
		te(1, 14, 1, 14, lineSeparator() + "  "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testClosingBracketNewLineWithPreserveEmptyContent() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		settings.getFormattingSettings().setSplitAttributesIndentSize(0);
		settings.getFormattingSettings().setPreserveEmptyContent(true);
		settings.getFormattingSettings().setClosingBracketNewLine(true);
		String content = "<a>" + lineSeparator() + "<b c='' d=''></b>" + lineSeparator() + "</a>";
		String expected = "<a>" + lineSeparator() + "  <b" + lineSeparator() + "  c=''" + lineSeparator() + "  d=''"
				+ lineSeparator() + "  ></b>" + lineSeparator() + "</a>";
		assertFormat(content, expected, settings, //
				te(0, 3, 1, 0, lineSeparator() + "  "), //
				te(1, 2, 1, 3, lineSeparator() + "  "), //
				te(1, 7, 1, 8, lineSeparator() + "  "), //
				te(1, 12, 1, 12, lineSeparator() + "  "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testClosingBracketNewLineWithPreserveEmptyContentSingleAttribute() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		settings.getFormattingSettings().setSplitAttributesIndentSize(0);
		settings.getFormattingSettings().setPreserveEmptyContent(true);
		settings.getFormattingSettings().setClosingBracketNewLine(true);
		String content = "<a>" + lineSeparator() + "<b></b>" + lineSeparator() + "</a>";
		String expected = "<a>" + lineSeparator() + "  <b></b>" + lineSeparator() + "</a>";
		assertFormat(content, expected, settings, //
				te(0, 3, 1, 0, lineSeparator() + "  "));
		assertFormat(expected, expected, settings);
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
