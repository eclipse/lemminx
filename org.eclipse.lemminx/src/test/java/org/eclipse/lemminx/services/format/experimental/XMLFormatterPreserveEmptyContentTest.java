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
 * XML experimental formatter services tests with preserve empty content
 * setting.
 *
 */
public class XMLFormatterPreserveEmptyContentTest {

	@Test
	public void testPreserveEmptyContentTag() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(true);

		String content = "<a>\n" + //
				"     " + //
				"</a>";
		String expected = "<a>\n" + //
				"     " + //
				"</a>";
		assertFormat(content, expected, settings);
	}

	@Test
	public void testDontPreserveEmptyContentTag() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(false);

		String content = "<a>\n" + //
				"     " + //
				"</a>";
		String expected = "<a>\n" + //
				"</a>";
		assertFormat(content, expected, settings, //
				te(0, 3, 1, 5, "\n"));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testPreserveTextContent() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(true);

		String content = "<a>\n" + //
				"   aaa  " + //
				"</a>";
		String expected = "<a>\n" + //
				"   aaa  " + //
				"</a>";
		assertFormat(content, expected, settings);
	}

	@Test
	public void testPreserveTextContent2() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(false);

		String content = "<a>\n" + //
				"   aaa   </a>";
		String expected = "<a>\n" + //
				"  aaa </a>";
		assertFormat(content, expected, settings, //
				te(0, 3, 1, 3, "\n  "), //
				te(1, 6, 1, 9, " "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testPreserveEmptyContentTagWithSiblings() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(true);

		String content = "<a>\n" + //
				"     " + //
				"  <b>  </b>" + //
				"     " + //
				"</a>";
		String expected = "<a>\n" + //
				"  <b>  </b>\n" + //
				"</a>";
		assertFormat(content, expected, settings, //
				te(0, 3, 1, 7, "\n  "), //
				te(1, 16, 1, 21, "\n"));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testPreserveEmptyContentTagWithSiblingContent() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(true);

		String content = "<a>\n" + //
				"   zz     <b>  </b>tt     </a>";
		String expected = "<a>\n" + //
				"   zz     <b>  </b>tt     </a>";
		assertFormat(content, expected, settings);
	}

	@Test
	public void testDontPreserveEmptyContentTagWithSiblingContent() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(false);

		String content = "<a>\n" + //
				"   zz     <b>  </b>tt     </a>";
		String expected = "<a> zz <b> </b>tt </a>";
		assertFormat(content, expected, settings, //
				te(0, 3, 1, 3, " "), //
				te(1, 5, 1, 10, " "), //
				te(1, 13, 1, 15, " "), //
				te(1, 21, 1, 26, " "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testPreserveEmptyContentTagWithSiblingWithComment() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(true);

		String content = "<a>\n" + //
				"   zz    <b>  </b>tt <!-- Comment -->     </a>";
		String expected = "<a>\n" + //
				"   zz    <b>  </b>tt <!-- Comment -->     </a>";
		assertFormat(content, expected, settings);
	}

	@Test
	public void testPreserveEmptyContentTagWithSiblingWithNewLines() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(true);

		String content = "<a>\n" + //
				"   zz    \n" + //
				"<b>\n" + //
				"  </b>\n" + //
				"tt <!-- Comment -->     </a>\n" + //
				"\n" + //
				"<c></c>";
		String expected = content;
		assertFormat(content, expected, settings);
	}

	@Test
	public void testDontPreserveEmptyContentTagWithSiblingWithComment() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(false);

		String content = "<a>\n" + //
				"   zz    <b>  </b>tt <!-- Comment -->     </a>";
		String expected = "<a> zz <b> </b>tt <!-- Comment -->\n" + //
				"</a>";
		assertFormat(content, expected, settings, //
				te(0, 3, 1, 3, " "), //
				te(1, 5, 1, 9, " "), //
				te(1, 12, 1, 14, " "), //
				te(1, 37, 1, 42, "\n"));
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
