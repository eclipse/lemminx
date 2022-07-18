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
import org.junit.jupiter.api.Test;

/**
 * XML experimental formatter services tests with join comment lines setting.
 *
 */
public class XMLFormatterJoinCommentLinesTest {

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
		String content = "<root>" + lineSeparator() + //
				"    <a> content </a" + lineSeparator() + //
				"        <!-- comment -->" + lineSeparator() + //
				" </root>";
		String expected = "<root>" + lineSeparator() + //
				"  <a> content </a" + lineSeparator() + //
				"  <!-- comment -->" + lineSeparator() + //
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
		String content = "<a>" + lineSeparator() + //
				" Content" + lineSeparator() + //
				"</a> <!-- My   Comment   -->";
		String expected = "<a> Content </a> <!-- My Comment -->";

		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setJoinCommentLines(true);
		assertFormat(content, expected, settings, //
				te(0, 3, 1, 1, " "), //
				te(1, 8, 2, 0, " "), //
				te(2, 12, 2, 15, " "), //
				te(2, 22, 2, 25, " "));
		assertFormat(expected, expected, settings);

	}

	@Test
	public void testJoinCommentLinesLongWrap() throws BadLocationException {
		String content = "<a>" + lineSeparator() + //
				"  Content <!-- comment comment comment comment comment comment comment comment comment comment comment comment comment comment comment comment comment comment comment comment comment comment comment comment comment -->"
				+ //
				"</a>";
		String expected = "<a> Content <!-- comment comment comment comment comment comment comment comment comment"
				+ lineSeparator() + //
				"  comment comment comment comment comment comment comment comment comment"
				+ lineSeparator() + //
				"  comment comment comment comment comment comment comment -->" + //
				"</a>";
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setJoinCommentLines(true);
		assertFormat(content, expected, settings, //
				te(0, 3, 1, 2, " "), //
				te(1, 86, 1, 87, "\n  "), //
				te(1, 158, 1, 159, "\n  "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testJoinCommentLinesLongWrapSingleWord() throws BadLocationException {
		String content = "<a>" + lineSeparator() + //
				"<!-- commentcommentcommentcomment commentcommentcommentcomment commentcommentcommentcomment commentcommmentcommentcommentcomment commentcommentcommentscommentcomment commentcommentcommentscommentcomment commentcommentcomments -->"
				+ //
				"</a>";
		String expected = "<a>" + lineSeparator() + //
				"  <!-- commentcommentcommentcomment commentcommentcommentcomment" + lineSeparator() + //
				"  commentcommentcommentcomment commentcommmentcommentcommentcomment" + lineSeparator() + //
				"  commentcommentcommentscommentcomment commentcommentcommentscommentcomment" + lineSeparator() + //
				"  commentcommentcomments --></a>";
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setJoinCommentLines(true);
		assertFormat(content, expected, settings, //
				te(0, 3, 1, 0, "\n  "), //
				te(1, 62, 1, 63, "\n  "), //
				te(1, 128, 1, 129, "\n  "), //
				te(1, 202, 1, 203, "\n  "));
		assertFormat(expected, expected, settings);
	}

	private static void assertFormat(String unformatted, String expected, SharedSettings sharedSettings,
			TextEdit... expectedEdits)
			throws BadLocationException {
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