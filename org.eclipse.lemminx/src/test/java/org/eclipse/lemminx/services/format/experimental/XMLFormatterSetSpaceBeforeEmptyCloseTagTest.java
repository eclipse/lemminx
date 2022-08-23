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

import static org.eclipse.lemminx.XMLAssert.te;

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.TextEdit;
import org.junit.jupiter.api.Test;

/**
 * XML experimental formatter services tests with setSpaceBeforeEmptyCloseTag
 * setting.
 *
 */
public class XMLFormatterSetSpaceBeforeEmptyCloseTagTest {

	@Test
	public void testSelfCloseTagSpaceSingleElement() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSpaceBeforeEmptyCloseTag(true);

		String content = "<a/>";
		String expected = "<a />";
		assertFormat(content, expected, settings,
				te(0, 2, 0, 2, " "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testSelfCloseTagSpaceSingleElementMultipleSpaces() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSpaceBeforeEmptyCloseTag(true);

		String content = "<a         />";
		String expected = "<a />";
		assertFormat(content, expected, settings,
				te(0, 2, 0, 11, " "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testSelfCloseTagSpaceSingleElementNewLine() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSpaceBeforeEmptyCloseTag(true);

		String content = "<a\r\n" + //
				"  />";
		String expected = "<a />";
		assertFormat(content, expected, settings,
				te(0, 2, 1, 2, " "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testSelfCloseTagSpace() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSpaceBeforeEmptyCloseTag(true);

		String content = "<a>\r\n" + //
				" <b/>\r\n" + //
				"</a>";
		String expected = "<a>\r\n" + //
				"  <b />\r\n" + //
				"</a>";
		assertFormat(content, expected, settings,
				te(0, 3, 1, 1, "\r\n  "),
				te(1, 3, 1, 3, " "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testSelfCloseTagAlreadyHasSpace() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSpaceBeforeEmptyCloseTag(true);

		String content = "<a>\r\n" + //
				" <b />\r\n" + //
				"</a>";
		String expected = "<a>\r\n" + //
				"  <b />\r\n" + //
				"</a>";
		assertFormat(content, expected, settings, //
				te(0, 3, 1, 1, "\r\n  "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testSelfCloseTagSpaceFalse() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSpaceBeforeEmptyCloseTag(false);

		String content = "<a>\r\n" + //
				" <b/>\r\n" + //
				"</a>";
		String expected = "<a>\r\n" + //
				"  <b/>\r\n" + //
				"</a>";
		assertFormat(content, expected, settings, //
				te(0, 3, 1, 1, "\r\n  "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testSelfCloseTagSpaceFalseAlreadyHasSpace() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSpaceBeforeEmptyCloseTag(false);

		String content = "<a>\r\n" + //
				" <b />\r\n" + //
				"</a>";
		String expected = "<a>\r\n" + //
				"  <b/>\r\n" + //
				"</a>";
		assertFormat(content, expected, settings,
				te(0, 3, 1, 1, "\r\n  "),
				te(1, 3, 1, 4, ""));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testDontAddClosingBracket() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSpaceBeforeEmptyCloseTag(false);

		String content = "<a>\r\n" + //
				" <b\r\n" + //
				"</a>";
		String expected = "<a>\r\n" + //
				"  <b\r\n" + //
				"</a>";
		assertFormat(content, expected, settings, //
				te(0, 3, 1, 1, "\r\n  "));
		assertFormat(expected, expected, settings);

	}

	@Test
	public void testEndTagMissingCloseBracket() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSpaceBeforeEmptyCloseTag(false);

		String content = "<a>\r\n" + //
				" <b> Value </b\r\n" + //
				"</a>";
		String expected = "<a>\r\n" + //
				"  <b> Value </b\r\n" + //
				"</a>";
		assertFormat(content, expected, settings, //
				te(0, 3, 1, 1, "\r\n  "));
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
