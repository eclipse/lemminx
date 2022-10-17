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
 * XML experimental formatter services tests with preserve new lines setting.
 *
 */
public class XMLFormatterPreserveNewLinesTest {

	@Test
	public void testPreserveNewlines() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		String content = "<xml>\r\n" + //
				"  <a></a>\r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"</xml>";
		String expected = "<xml>\r\n" + //
				"  <a></a>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"</xml>";
		assertFormat(content, expected, settings, //
				te(1, 9, 5, 0, "\r\n\r\n\r\n"));
		assertFormat(expected, expected);
	}

	@Test
	public void testPreserveNewlines3Max() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreservedNewlines(3);
		String content = "<xml>\r\n" + //
				"  <a></a>\r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"</xml>";
		String expected = "<xml>\r\n" + //
				"  <a></a>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"</xml>";
		assertFormat(content, expected, settings, //
				te(1, 9, 5, 0, "\r\n\r\n\r\n\r\n"));
		assertFormat(expected, expected, settings);

	}

	@Test
	public void testPreserveNewlines2() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		String content = "<xml>\r\n" + //
				"  <a></a>\r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"</xml>";
		String expected = "<xml>\r\n" + //
				"  <a></a>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"</xml>";
		assertFormat(content, expected, settings, //
				te(1, 9, 6, 0, "\r\n\r\n\r\n"));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testPreserveNewlinesBothSides() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		String content = "<xml>\r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  <a></a>\r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"</xml>";
		String expected = "<xml>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"  <a></a>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"</xml>";
		assertFormat(content, expected, settings, //
				te(0, 5, 5, 2, "\r\n\r\n\r\n  "), //
				te(5, 9, 10, 0, "\r\n\r\n\r\n"));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testPreserveNewlinesBothSidesSetToZero() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreservedNewlines(0);
		String content = "<xml>\r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  <a></a>\r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"</xml>";
		String expected = "<xml>\r\n" + //
				"  <a></a>\r\n" + //
				"</xml>";
		assertFormat(content, expected, settings, //
				te(0, 5, 5, 2, "\r\n  "), //
				te(5, 9, 10, 0, "\r\n"));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testPreserveNewlinesBothSidesSetToZeroSingleCharDelimiter() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreservedNewlines(0);
		String content = "<xml>\n" + //
				"  \n" + //
				"  \n" + //
				"  \n" + //
				"  \n" + //
				"  <a></a>\n" + //
				"  \n" + //
				"  \n" + //
				"  \n" + //
				"  \n" + //
				"</xml>";
		String expected = "<xml>\n" + //
				"  <a></a>\n" + //
				"</xml>";
		assertFormat(content, expected, settings, //
				te(0, 5, 5, 2, "\n  "), //
				te(5, 9, 10, 0, "\n"));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testPreserveNewlinesBothSidesMultipleTags() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		String content = "<xml>\r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  <a></a>\r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  <b></b>\r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"</xml>";
		String expected = "<xml>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"  <a></a>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"  <b></b>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"</xml>";
		assertFormat(content, expected, settings, //
				te(0, 5, 5, 2, "\r\n\r\n\r\n  "), //
				te(5, 9, 11, 2, "\r\n\r\n\r\n  "), //
				te(11, 9, 16, 0, "\r\n\r\n\r\n"));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testPreserveNewlinesWithChild() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		String content = "<xml>\r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  <a>\r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  <b></b>\r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  </a>\r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"</xml>";
		String expected = "<xml>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"  <a>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"    <b></b>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"  </a>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"</xml>";
		assertFormat(content, expected, settings, //
				te(0, 5, 5, 2, "\r\n\r\n\r\n  "), //
				te(5, 5, 10, 2, "\r\n\r\n\r\n    "), //
				te(10, 9, 13, 2, "\r\n\r\n\r\n  "), //
				te(13, 6, 16, 0, "\r\n\r\n\r\n"));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testPreserveNewlinesWithEmptyChild() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		String content = "<xml>\r\n" + //
				"  <a>\r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"</a>\r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"</xml>";
		String expected = "<xml>\r\n" + //
				"  <a>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"  </a>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"</xml>";
		assertFormat(content, expected, settings, //
				te(1, 5, 5, 0, "\r\n\r\n\r\n  "), //
				te(5, 4, 9, 0, "\r\n\r\n\r\n"));
		assertFormat(expected, expected);
	}

	@Test
	public void testPreserveNewlinesSingleLine() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		String content = "<xml>\r\n" + //
				"  <a></a>\r\n" + //
				"  \r\n" + //
				"</xml>";
		String expected = "<xml>\r\n" + //
				"  <a></a>\r\n" + //
				"\r\n" + //
				"</xml>";
		assertFormat(content, expected, settings, //
				te(1, 9, 3, 0, "\r\n\r\n"));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testPreserveNewlines4() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		String content = "<xml>\r\n" + //
				"  <a></a>\r\n" + //
				"</xml>";
		String expected = "<xml>\r\n" + //
				"  <a></a>\r\n" + //
				"</xml>";
		assertFormat(content, expected, settings);
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testNoSpacesOnNewLine() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		String content = "<a>\r\n" + //
				"  <b></b>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"  \r\n" + //
				"\r\n" + //
				"\r\n" + //
				"             \r\n" + //
				"\r\n" + //
				"\r\n" + //
				"</a>";
		String expected = "<a>\r\n" + //
				"  <b></b>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"</a>";
		assertFormat(content, expected, settings, //
				te(1, 9, 10, 0, "\r\n\r\n\r\n"));
		assertFormat(expected, expected, settings);
	}

	// https://github.com/redhat-developer/vscode-xml/issues/797
	@Test
	public void testPreserveNewlinesIssue797() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		String content = "<foo>\r\n" + //
				"  <bar>1</bar>\r\n" + //
				"\r\n" + //
				"  <bar>2</bar>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"  <bar>3</bar>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"  <bar>4</bar>\r\n" + //
				"\r\n" + //
				"</foo>";
		String expected = "<foo>\r\n" + //
				"  <bar>1</bar>\r\n" + //
				"\r\n" + //
				"  <bar>2</bar>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"  <bar>3</bar>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"  <bar>4</bar>\r\n" + //
				"\r\n" + //
				"</foo>";
		assertFormat(content, expected, settings, //
				te(6, 14, 10, 2, "\r\n\r\n\r\n  "));
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
