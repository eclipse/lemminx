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

import java.util.Arrays;

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.TextEdit;
import org.junit.jupiter.api.Test;

/**
 * XML experimental formatter services tests with preserve spaces.
 *
 */
public class XMLFormatterPreserveSpacesTest {

	@Test
	public void noPreserveSpaces() throws BadLocationException {
		String content = "<a>b  c</a>";
		String expected = "<a>b c</a>";
		assertFormat(content, expected, //
				te(0, 4, 0, 6, " "));
		assertFormat(expected, expected);
	}

	@Test
	public void preserveSpacesWithXmlSpace() throws BadLocationException {
		String content = "<a xml:space=\"preserve\">b  c</a>";
		String expected = content;
		assertFormat(content, expected);
	}

	@Test
	public void preserveSpacesWithXmlSpace2() throws BadLocationException {
		String content = "<a>\r\n" + //
				"  <b>\r\n" + //
				"    c  <d></d>  e\r\n" + //
				"  </b>\r\n" + //
				"  <b xml:space=\"preserve\">\r\n" + //
				"    c  <d></d>  e\r\n" + //
				"  </b>\r\n" + //
				"</a>";
		String expected = "<a>\r\n" + //
				"  <b> c <d></d> e </b>\r\n" + //
				"  <b xml:space=\"preserve\">\r\n" + //
				"    c  <d></d>  e\r\n" + //
				"  </b>\r\n" + //
				"</a>";

		assertFormat(content, expected, //
				te(1, 5, 2, 4, " "), //
				te(2, 5, 2, 7, " "), //
				te(2, 14, 2, 16, " "), //
				te(2, 17, 3, 2, " "));
		assertFormat(expected, expected);
	}

	@Test
	public void preserveSpacesWithSettings() throws BadLocationException {
		String content = "<a>b  c</a>";
		String expected = content;

		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveSpace(Arrays.asList("a"));
		assertFormat(content, expected, settings);
	}

	@Test
	public void preserveSpacesWithDefaultSettings() throws BadLocationException {
		String content = "<xsl:text>b \r\n" + //
				" c</xsl:text>";
		String expected = content;
		assertFormat(content, expected);
	}

	private static void assertFormat(String unformatted, String actual, TextEdit... expectedEdits)
			throws BadLocationException {
		assertFormat(unformatted, actual, new SharedSettings(), expectedEdits);
	}

	private static void assertFormat(String unformatted, String expected, SharedSettings sharedSettings,
			TextEdit... expectedEdits) throws BadLocationException {
		assertFormat(unformatted, expected, sharedSettings, "test.xml", expectedEdits);
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
