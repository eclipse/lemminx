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
 * XML experimental formatter services tests with join content lines
 * setting.
 *
 */
public class XMLFormatterJoinContentLinesTest {
	@Test
	public void testPreserveEmptyContentWithJoinContentLines() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(true);
		settings.getFormattingSettings().setJoinContentLines(true);

		String content = "<a>\n" + //
				"   xx  \n" + //
				"   yy  \n" + //
				"   <b>  </b>  \n" + //
				"</a>";
		String expected = "<a>\n" + //
				"   xx  \n" + //
				"   yy  \n" + //
				"   <b>  </b>\n" + //
				"</a>";
		assertFormat(content, expected, settings, //
				te(3, 12, 4, 0, "\n"));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testJoinContentLinesTrue() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(false);
		settings.getFormattingSettings().setJoinContentLines(true);

		String content = "<a>\n" + //
				"   zz  \n" + //
				"   zz  " + //
				"</a>";
		String expected = "<a> zz zz </a>";
		assertFormat(content, expected, settings, //
				te(0, 3, 1, 3, " "),
				te(1, 5, 2, 3, " "),
				te(2, 5, 2, 7, " "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testJoinContentLinesTrue2() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(false);
		settings.getFormattingSettings().setJoinContentLines(true);

		String content = "<a>zz zz zz</a>";
		String expected = "<a>zz zz zz</a>";
		assertFormat(content, expected, settings);
	}

	@Test
	public void testJoinContentLinesFalse() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(false);
		settings.getFormattingSettings().setJoinContentLines(false);

		String content = "<a>\n" + //
				"   zz  \n" + //
				"   zz  </a>";
		String expected = "<a> zz  \n" + //
				"   zz </a>";
		assertFormat(content, expected, settings, //
				te(0, 3, 1, 3, " "),
				te(2, 5, 2, 7, " "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testJoinContentLinesFalseEmptyContent() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(false);
		settings.getFormattingSettings().setJoinContentLines(false);

		String content = "<a>\n" + //
				"     \n" + //
				"     " + //
				"</a>";
		String expected = "<a> </a>";
		assertFormat(content, expected, settings, //
				te(0, 3, 2, 5, " "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testJoinContentLinesWithSiblingElementTrue() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(false);
		settings.getFormattingSettings().setJoinContentLines(true);

		String content = "<a>\n" + //
				"   zz  \n" + //
				"   zz  \n" + //
				"   <b>  </b>  \n" + //
				"</a>";
		String expected = "<a> zz zz <b> </b>\n" + //
				"</a>";
		assertFormat(content, expected, settings, //
				te(0, 3, 1, 3, " "),
				te(1, 5, 2, 3, " "),
				te(2, 5, 3, 3, " "),
				te(3, 6, 3, 8, " "),
				te(3, 12, 4, 0, "\n"));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testJoinContentLinesWithSiblingElementFalse() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(false);
		settings.getFormattingSettings().setJoinContentLines(false);

		String content = "<a>\n" + //
				"   zz  \n" + //
				"   zz  \n" + //
				"   <a>  </a>  \n" + //
				"</a>";
		String expected = "<a> zz  \n" + //
				"   zz <a> </a>\n" + //
				"</a>";
		assertFormat(content, expected, settings, //
				te(0, 3, 1, 3, " "),
				te(2, 5, 3, 3, " "),
				te(3, 6, 3, 8, " "),
				te(3, 12, 4, 0, "\n"));
		assertFormat(expected, expected, settings);
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
