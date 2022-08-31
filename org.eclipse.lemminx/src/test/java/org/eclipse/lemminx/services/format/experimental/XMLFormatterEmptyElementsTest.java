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
import org.eclipse.lemminx.settings.XMLFormattingOptions.EmptyElements;
import org.eclipse.lsp4j.TextEdit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * XML experimental formatter services tests with empty elements.
 *
 */
public class XMLFormatterEmptyElementsTest extends AbstractCacheBasedTest {

	// ------------ Tests with format empty elements settings

	@Test
	public void expandEmptyElements() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setEmptyElement(EmptyElements.expand);

		String content = "<example att=\"hello\" />";
		String expected = "<example att=\"hello\"></example>";
		assertFormat(content, expected, settings, //
				te(0, 20, 0, 23, "></example>"));

		content = "<example " + lineSeparator() + //
				"  att=\"hello\"" + lineSeparator() + //
				"  />";
		expected = "<example" + lineSeparator() + //
		"  att=\"hello\"></example>";
		assertFormat(content, expected, settings, //
				te(0, 8, 1, 2, lineSeparator() + "  "), //
				te(1, 13, 2, 4, "></example>"));

		assertFormat(expected, expected, settings);
	}

	@Test
	public void collapseEmptyElements() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setEmptyElement(EmptyElements.collapse);

		String content = "<example att=\"hello\"></example>";
		String expected = "<example att=\"hello\" />";
		assertFormat(content, expected, settings, //
				te(0, 20, 0, 31, " />"));

		content = "<example " + //
				"  att=\"hello\"" + System.lineSeparator() + //
				"  >" + System.lineSeparator() + //
				"</example>";
		assertFormat(content, expected, settings, //
				te(0, 8, 0, 11, " "), //
				te(0, 22, 2, 10, " />"));

		content = "<example att=\"hello\">   </example>";
		assertFormat(content, expected, settings, //
				te(0, 20, 0, 34, " />"));

		assertFormat(expected, expected, settings);

		content = "<example att=\"hello\"> X </example>";
		expected = "<example att=\"hello\"> X </example>";
		assertFormat(content, expected, settings);

		content = "<example att=\"hello\"> <X/> </example>";
		expected = "<example att=\"hello\">" + lineSeparator() + //
				"  <X />" + lineSeparator() + //
				"</example>";
		assertFormat(content, expected, settings, //
				te(0, 21, 0, 22, lineSeparator() + "  "), //
				te(0, 24, 0, 24, " "), //
				te(0, 26, 0, 27, lineSeparator()));
		assertFormat(expected, expected, settings);

	}

	@Test
	public void ignoreEmptyElements() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setEmptyElement(EmptyElements.ignore);

		String content = "<example att=\"hello\"></example>";
		assertFormat(content, content, settings);

		content = "<example att=\"hello\"   />";
		String expected = "<example att=\"hello\" />";
		assertFormat(content, expected, settings, //
				te(0, 20, 0, 23, " "));
		assertFormat(expected, expected, settings);
	}

	@Disabled
	@Test
	public void expandEmptyElementsAndPreserveEmptyContent() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setEmptyElement(EmptyElements.expand);
		settings.getFormattingSettings().setPreserveEmptyContent(true);

		String content = "<foo>\r\n" + //
				"    <bar>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"        \r\n" + //
				"    </bar>\r\n" + //
				"</foo>";
		String expected = "<foo>\r\n" + //
				"  <bar>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"        \r\n" + //
				"    </bar>\r\n" + //
				"</foo>";
		assertFormat(content, expected, settings);

		content = "<foo>\r\n" + //
				"    <bar></bar>\r\n" + //
				"</foo>";
		expected = "<foo>\r\n" + //
				"  <bar></bar>\r\n" + //
				"</foo>";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void collapseEmptyElementsAndPreserveEmptyContent() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setEmptyElement(EmptyElements.collapse);
		settings.getFormattingSettings().setPreserveEmptyContent(true);

		String content = "<foo>\r\n" + //
				"    <bar>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"        \r\n" + //
				"    </bar>\r\n" + //
				"</foo>";
		String expected = "<foo>\r\n" + //
				"  <bar>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"        \r\n" + //
				"    </bar>\r\n" + //
				"</foo>";
		assertFormat(content, expected, settings);

		content = "<foo>\r\n" + //
				"    <bar></bar>\r\n" + //
				"</foo>";
		expected = "<foo>\r\n" + //
				"  <bar />\r\n" + //
				"</foo>";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void collapseEmptyElementsInRange() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setEmptyElement(EmptyElements.collapse);

		// Range doesn't cover the b element, collapse cannot be done
		String content = "<a>\r\n" + //
				"<|b>\r\n" + //
				"   | \r\n" + //
				"</b>\r\n" + //
				"</a>";
		String expected = "<a>\r\n" + //
				"  <b>\r\n" + //
				"</b>\r\n" + //
				"</a>";
		assertFormat(content, expected, settings);

		// Range covers the b element, collapse is done
		content = "<a>\r\n" + //
				"<|b>\r\n" + //
				"    \r\n" + //
				"</|b>\r\n" + //
				"</a>";
		expected = "<a>\r\n" + //
				"  <b />\r\n" + //
				"</a>";
		assertFormat(content, expected, settings);
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
