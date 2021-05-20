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

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.TextEdit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * XML experimental formatter services tests with preserve new lines setting.
 *
 */
public class XMLFormatterPreserveNewLinesTest {

	@Disabled
	@Test
	public void testPreserveNewlines() throws BadLocationException {
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
		assertFormat(content, expected);
	}

	@Disabled
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
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testPreserveNewlines2() throws BadLocationException {
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
		assertFormat(content, expected);
	}

	@Disabled
	@Test
	public void testPreserveNewlinesBothSides() throws BadLocationException {
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
		assertFormat(content, expected);
	}

	@Disabled
	@Test
	public void testPreserveNewlinesBothSidesMultipleTags() throws BadLocationException {
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
		assertFormat(content, expected);
	}

	@Disabled
	@Test
	public void testPreserveNewlinesSingleLine() throws BadLocationException {
		String content = "<xml>\r\n" + //
				"  <a></a>\r\n" + //
				"  \r\n" + //
				"</xml>";
		String expected = "<xml>\r\n" + //
				"  <a></a>\r\n" + //
				"\r\n" + //
				"</xml>";
		assertFormat(content, expected);
	}

	@Test
	public void testPreserveNewlines4() throws BadLocationException {
		String content = "<xml>\r\n" + //
				"  <a></a>\r\n" + //
				"</xml>";
		String expected = "<xml>\r\n" + //
				"  <a></a>\r\n" + //
				"</xml>";
		assertFormat(content, expected);
	}

	@Disabled
	@Test
	public void testNoSpacesOnNewLine() throws BadLocationException {
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
