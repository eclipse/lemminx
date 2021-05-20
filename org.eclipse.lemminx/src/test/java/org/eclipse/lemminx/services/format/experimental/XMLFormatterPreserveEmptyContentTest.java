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
 * XML experimental formatter services tests with preserve empty content
 * setting.
 *
 */
public class XMLFormatterPreserveEmptyContentTest {

	@Disabled
	@Test
	public void testPreserveEmptyContentTag() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(true);

		String content = "<a>\r" + //
				"     " + //
				"</a>";
		String expected = "<a>\r" + //
				"     " + //
				"</a>";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testDontPreserveEmptyContentTag() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(false);

		String content = "<a>\r" + //
				"     " + //
				"</a>";
		String expected = "<a></a>";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testPreserveTextContent() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(true);

		String content = "<a>\r" + //
				"   aaa  " + //
				"</a>";
		String expected = "<a>\r" + //
				"   aaa  " + //
				"</a>";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testPreserveTextContent2() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(false);

		String content = "<a>\r" + //
				"   aaa  " + //
				"</a>";
		String expected = "<a>\r" + //
				"   aaa  " + //
				"</a>";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testPreserveEmptyContentTagWithSiblings() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(true);

		String content = "<a>\r" + //
				"     " + //
				"  <b>  </b>" + //
				"     " + //
				"</a>";
		String expected = "<a>\r" + //
				"  <b>  </b>\r" + //
				"</a>";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testPreserveEmptyContentTagWithSiblingContent() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(true);

		String content = "<a>\r" + //
				"   zz  " + //
				"  <b>  </b>tt" + //
				"     " + //
				"</a>";
		String expected = "<a>\r" + //
				"  zz\r" + //
				"  <b>  </b>\r" + //
				"  tt\r" + //
				"</a>";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testDontPreserveEmptyContentTagWithSiblingContent() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(false);

		String content = "<a>\r" + //
				"   zz  " + //
				"  <b>  </b>tt" + //
				"     " + //
				"</a>";
		String expected = "<a>\r" + //
				"  zz\r" + //
				"  <b></b>\r" + //
				"  tt\r" + //
				"</a>";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testPreserveEmptyContentTagWithSiblingWithComment() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(true);

		String content = "<a>\r" + //
				"   zz  " + //
				"  <b>  </b>tt <!-- Comment -->" + //
				"     " + //
				"</a>";
		String expected = "<a>\r" + //
				"  zz\r" + //
				"  <b>  </b>\r" + //
				"  tt <!-- Comment -->\r" + //
				"</a>";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testDontPreserveEmptyContentTagWithSiblingWithComment() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(false);

		String content = "<a>\r" + //
				"   zz  " + //
				"  <b>  </b>tt <!-- Comment -->" + //
				"     " + //
				"</a>";
		String expected = "<a>\r" + //
				"  zz\r" + //
				"  <b></b>\r" + //
				"  tt <!-- Comment -->\r" + //
				"</a>";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testPreserveEmptyContentWithJoinContentLines() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(true);
		settings.getFormattingSettings().setJoinContentLines(true);

		String content = "<a>\n" + //
				"   zz  \n" + //
				"   zz  \n" + //
				"   <a>  </a>  \n" + //
				"</a>";
		String expected = "<a>\n" + //
				"  zz zz\n" + //
				"  <a>  </a>\n" + //
				"</a>";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testJoinContentLinesTrue() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(false);
		settings.getFormattingSettings().setJoinContentLines(true);

		String content = "<a>\n" + //
				"   zz  \n" + //
				"   zz  " + //
				"</a>";
		String expected = "<a>zz zz</a>";
		assertFormat(content, expected, settings);
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

	@Disabled
	@Test
	public void testJoinContentLinesFalse() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(false);
		settings.getFormattingSettings().setJoinContentLines(false);

		String content = "<a>\n" + //
				"   zz  \n" + //
				"   zz  " + //
				"</a>";
		String expected = "<a>\n" + //
				"   zz  \n" + //
				"   zz  " + //
				"</a>";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testJoinContentLinesWithSiblingElementTrue() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(false);
		settings.getFormattingSettings().setJoinContentLines(true);

		String content = "<a>\n" + //
				"   zz  \n" + //
				"   zz  \n" + //
				"   <a>  </a>  \n" + //
				"</a>";
		String expected = "<a>\n" + //
				"  zz zz\n" + //
				"  <a></a>\n" + //
				"</a>";
		assertFormat(content, expected, settings);
	}

	@Disabled
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
		String expected = "<a>\n" + //
				"  zz  \n" + //
				"   zz\n" + //
				"  <a></a>\n" + //
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
