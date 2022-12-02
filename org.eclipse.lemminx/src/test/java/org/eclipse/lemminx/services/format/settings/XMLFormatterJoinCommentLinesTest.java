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
package org.eclipse.lemminx.services.format.settings;

import static java.lang.System.lineSeparator;
import static org.eclipse.lemminx.XMLAssert.te;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.TextEdit;
import org.junit.jupiter.api.Test;

/**
 * XML formatter services tests with join comment lines setting.
 *
 */
public class XMLFormatterJoinCommentLinesTest extends AbstractCacheBasedTest {

	@Test
	public void testJoinCommentLines() throws BadLocationException {
		String content = "<!--" + lineSeparator() + //
				" line 1" + lineSeparator() + //
				" " + lineSeparator() + //
				" " + lineSeparator() + //
				"   line 2" + lineSeparator() + //
				" -->";
		String expected = "<!-- line 1 line 2 -->";
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setJoinCommentLines(true);
		assertFormat(content, expected, settings, //
				te(0, 4, 1, 1, " "), //
				te(1, 7, 4, 3, " "), //
				te(4, 9, 5, 1, " "));
		assertFormat(expected, expected, settings);

	}

	@Test
	public void testUnclosedEndTagTrailingComment() throws BadLocationException {
		String content = "<root>\n" + //
				"    <a> content </a\n" + //
				"        <!-- comment -->\n" + //
				" </root>";
		String expected = "<root>\n" + //
				"  <a> content </a\n" + //
				"  <!-- comment -->\n" + //
				"</root>";
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setJoinCommentLines(true);
		assertFormat(content, expected, settings, //
				te(0, 6, 1, 4, "\n  "), //
				te(1, 19, 2, 8, "\n  "), //
				te(2, 24, 3, 1, "\n"));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testJoinCommentLinesNested() throws BadLocationException {
		String content = "<a>" + lineSeparator() + //
				"  <!--" + lineSeparator() + //
				"   line 1" + lineSeparator() + //
				"   " + lineSeparator() + //
				"   " + lineSeparator() + //
				"     line 2" + lineSeparator() + //
				"   -->" + lineSeparator() + //
				"</a>";
		String expected = "<a>" + lineSeparator() + //
				"  <!-- line 1 line 2 -->" + lineSeparator() + //
				"</a>";

		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setJoinCommentLines(true);
		assertFormat(content, expected, settings, //
				te(1, 6, 2, 3, " "), //
				te(2, 9, 5, 5, " "), //
				te(5, 11, 6, 3, " "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testCommentFormatSameLine() throws BadLocationException {
		String content = "<a>\n" + //
				" Content\n" + //
				"</a> <!-- My   Comment   -->";
		String expected = "<a>\n" + //
				"  Content\n" + //
				"</a> <!-- My Comment -->";
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setJoinCommentLines(true);
		assertFormat(content, expected, settings, //
				te(0, 3, 1, 1, "\n  "), //
				te(2, 12, 2, 15, " "), //
				te(2, 22, 2, 25, " "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testCommentFormatSameLineJoinContentLines() throws BadLocationException {
		String content = "<a>\n" + //
				" Content\n" + //
				"</a> <!-- My   Comment   -->";
		String expected = "<a> Content </a> <!-- My Comment -->";
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setJoinCommentLines(true);
		settings.getFormattingSettings().setJoinContentLines(true);
		assertFormat(content, expected, settings, //
				te(0, 3, 1, 1, " "), //
				te(1, 8, 2, 0, " "), //
				te(2, 12, 2, 15, " "), //
				te(2, 22, 2, 25, " "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testJoinCommentLinesLongWrap() throws BadLocationException {
		String content = "<a>\n" + //
				"  Content <!-- comment comment comment comment comment comment comment comment comment comment comment comment comment comment comment comment comment comment comment comment comment comment comment comment comment -->"
				+ //
				"</a>";
		String expected = "<a> Content <!-- comment comment comment comment comment comment comment comment\n" + //
				"  comment comment comment comment comment comment comment comment comment\n" + //
				"  comment comment comment comment comment comment comment comment --></a>";
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setJoinCommentLines(true);
		settings.getFormattingSettings().setMaxLineWidth(80);
		assertFormat(content, expected, settings, //
				te(0, 3, 1, 2, " "), //
				te(1, 78, 1, 79, "\n  "), //
				te(1, 150, 1, 151, "\n  "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testJoinCommentLinesLongWrapSingleWord() throws BadLocationException {
		String content = "<a>\n" + //
				"<!-- commentcommentcommentcomment commentcommentcommentcomment commentcommentcommentcomment commentcommmentcommentcommentcomment commentcommentcommentscommentcomment commentcommentcommentscommentcomment commentcommentcomments -->"
				+ //
				"</a>";
		String expected = "<a>\n" + //
				"  <!-- commentcommentcommentcomment commentcommentcommentcomment\n" + //
				"  commentcommentcommentcomment commentcommmentcommentcommentcomment\n" + //
				"  commentcommentcommentscommentcomment commentcommentcommentscommentcomment\n" + //
				"  commentcommentcomments -->\n" + //
				"</a>";
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setJoinCommentLines(true);
		settings.getFormattingSettings().setMaxLineWidth(80);
		assertFormat(content, expected, settings, //
				te(0, 3, 1, 0, "\n  "), //
				te(1, 62, 1, 63, "\n  "), //
				te(1, 128, 1, 129, "\n  "), //
				te(1, 202, 1, 203, "\n  "),
				te(1, 229, 1, 229, "\n"));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testCommentWithRange() throws BadLocationException {
		String content = "<foo>\r\n" + //
				"  <!-- |<bar>|\r\n" + //
				"  </bar>\r\n" + //
				"  -->\r\n" + //
				"</foo>";
		String expected = "<foo>\r\n" + //
				"  <!-- <bar>\r\n" + //
				"  </bar>\r\n" + //
				"  -->\r\n" + //
				"</foo>";
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setJoinCommentLines(true);
		assertFormat(content, expected, settings);
	}

	@Test
	public void testCommentWithRange2() throws BadLocationException {
		String content = "<foo>\r\n" + //
				"  |<!-- <bar>\r\n" + //
				"  </bar>\r\n" + //
				"  -->|\r\n" + //
				"<test></test>\r\n" + //
				"</foo>";
		String expected = "<foo>\r\n" + //
				"  <!-- <bar> </bar> -->\r\n" + //
				"<test></test>\r\n" + //
				"</foo>";
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setJoinCommentLines(true);
		assertFormat(content, expected, settings, //
				te(1, 12, 2, 2, " "), //
				te(2, 8, 3, 2, " "));
	}

	@Test
	public void testCommentWithRange3() throws BadLocationException {
		String content = "<foo>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"  |<!-- <bar>\r\n" + //
				"  </bar>|\r\n" + //
				"  -->\r\n" + //
				"<test></test>\r\n" + //
				"</foo>";
		String expected = "<foo>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"  <!-- <bar> </bar> -->\r\n" + //
				"<test></test>\r\n" + //
				"</foo>";
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setJoinCommentLines(true);
		assertFormat(content, expected, settings, //
				te(4, 12, 5, 2, " "), //
				te(5, 8, 6, 2, " "));
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
		XMLAssert.assertFormat(null, unformatted, expected, sharedSettings, uri, considerRangeFormat, expectedEdits);
	}
}