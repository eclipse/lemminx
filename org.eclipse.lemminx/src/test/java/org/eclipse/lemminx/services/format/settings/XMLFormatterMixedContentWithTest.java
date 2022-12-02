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

import static org.eclipse.lemminx.XMLAssert.te;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.TextEdit;
import org.junit.jupiter.api.Test;

/**
 * XML formatter services tests with mixed content.
 *
 */
public class XMLFormatterMixedContentWithTest extends AbstractCacheBasedTest {

	@Test
	public void mixedContent() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setMaxLineWidth(20);
		settings.getFormattingSettings().setJoinContentLines(true);
		String content = "<a>abcd    \r\n   efgh</a>";
		String expected = "<a>abcd efgh</a>";
		assertFormat(content, expected, settings, //
				te(0, 7, 1, 3, " "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void ignoreSpace() throws BadLocationException {
		String content = "<a><b><c></c></b></a>";
		String expected = "<a>" + System.lineSeparator() + //
				"  <b>" + System.lineSeparator() + //
				"    <c></c>" + System.lineSeparator() + //
				"  </b>" + System.lineSeparator() + //
				"</a>";
		assertFormat(content, expected, //
				te(0, 3, 0, 3, System.lineSeparator() + "  "), //
				te(0, 6, 0, 6, System.lineSeparator() + "    "), //
				te(0, 13, 0, 13, System.lineSeparator() + "  "), //
				te(0, 17, 0, 17, System.lineSeparator()));
		assertFormat(expected, expected);
	}

	@Test
	public void withMixedContent() throws BadLocationException {
		String content = "<a><b>A<c></c></b></a>";
		String expected = "<a>" + System.lineSeparator() + //
				"  <b>A<c></c></b>" + System.lineSeparator() + //
				"</a>";
		assertFormat(content, expected, //
				te(0, 3, 0, 3, System.lineSeparator() + "  "), //
				te(0, 18, 0, 18, System.lineSeparator()));
		assertFormat(expected, expected);
	}

	@Test
	public void withMixedContentWhiteSpaceLeft() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		String content = "<a>   <b> content </b> test  </a>";
		String expected = "<a>" + System.lineSeparator() + //
				"  <b> content </b> test </a>";
		assertFormat(content, expected, settings, //
				te(0, 3, 0, 6, System.lineSeparator() + "  "), //
				te(0, 27, 0, 29, " "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void withMixedContentNoWhiteSpaceLeft() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		String content = "<a><b> content </b> test </a>";
		String expected = content;
		assertFormat(content, expected, settings);
	}

	@Test
	public void withMixedContentWhiteSpaceRight() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		String content = "<a> test  <b> content </b>   </a>";
		String expected = "<a> test <b> content </b>" + System.lineSeparator() + //
				"</a>";
		assertFormat(content, expected, settings, //
				te(0, 8, 0, 10, " "), //
				te(0, 26, 0, 29, System.lineSeparator()));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void withMixedContentNoWhiteSpaceRight() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setMaxLineWidth(20);
		settings.getFormattingSettings().setJoinContentLines(true);
		String content = "<a>abcd    \r\n   efgh</a>";
		String expected = "<a>abcd efgh</a>";
		assertFormat(content, expected, settings, //
				te(0, 7, 1, 3, " "));
		assertFormat(expected, expected, settings);
	}

	private static void assertFormat(String unformatted, String expected, TextEdit... expectedEdits)
			throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		assertFormat(unformatted, expected, settings, "test://test.html", true, expectedEdits);
	}

	private static void assertFormat(String unformatted, String expected, SharedSettings sharedSettings,
			TextEdit... expectedEdits) throws BadLocationException {
		assertFormat(unformatted, expected, sharedSettings, "test://test.html", true, expectedEdits);
	}

	private static void assertFormat(String unformatted, String expected, SharedSettings sharedSettings, String uri,
			Boolean considerRangeFormat, TextEdit... expectedEdits) throws BadLocationException {
		XMLAssert.assertFormat(null, unformatted, expected, sharedSettings, uri, considerRangeFormat, expectedEdits);
	}
}
