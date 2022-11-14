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
import org.eclipse.lsp4j.TextEdit;
import org.junit.jupiter.api.Test;

/**
 * XML experimental formatter services tests with split attributes.
 *
 */
public class XMLFormatterSplitAttributesTest extends AbstractCacheBasedTest {

	@Test
	public void splitAttributesIndentSize0() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		settings.getFormattingSettings().setSplitAttributesIndentSize(0);

		String content = "<root  a='a' b='b' c='c'/>\n";
		String expected = "<root\n" + //
				"a='a'\n" + //
				"b='b'\n" + //
				"c='c' />";

		assertFormat(content, expected, settings, //
				te(0, 5, 0, 7, "\n"), //
				te(0, 12, 0, 13, "\n"), //
				te(0, 18, 0, 19, "\n"), //
				te(0, 24, 0, 24, " "), //
				te(0, 26, 1, 0, ""));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void splitAttributesIndentSizeNegative() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		settings.getFormattingSettings().setSplitAttributesIndentSize(-1);

		String content = "<root  a='a' b='b' c='c'/>\n";
		String expected = "<root\n" + //
				"a='a'\n" + //
				"b='b'\n" + //
				"c='c' />";

		assertFormat(content, expected, settings, //
				te(0, 5, 0, 7, "\n"), //
				te(0, 12, 0, 13, "\n"), //
				te(0, 18, 0, 19, "\n"), //
				te(0, 24, 0, 24, " "), //
				te(0, 26, 1, 0, ""));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void splitAttributesIndentSize1() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		settings.getFormattingSettings().setSplitAttributesIndentSize(1);

		String content = "<root  a='a' b='b' c='c'/>\n";
		String expected = "<root\n" + //
				"  a='a'\n" + //
				"  b='b'\n" + //
				"  c='c' />";

		assertFormat(content, expected, settings, //
				te(0, 5, 0, 7, "\n  "), //
				te(0, 12, 0, 13, "\n  "), //
				te(0, 18, 0, 19, "\n  "), //
				te(0, 24, 0, 24, " "), //
				te(0, 26, 1, 0, ""));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void splitAttributesIndentSizeDefault() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(true);

		String content = "<root  a='a' b='b' c='c'/>\n";
		String expected = "<root\n" + //
				"    a='a'\n" + //
				"    b='b'\n" + //
				"    c='c' />";

		assertFormat(content, expected, settings, //
				te(0, 5, 0, 7, "\n    "), //
				te(0, 12, 0, 13, "\n    "), //
				te(0, 18, 0, 19, "\n    "), //
				te(0, 24, 0, 24, " "), //
				te(0, 26, 1, 0, ""));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testSplitAttributesNested() throws BadLocationException {
		String content = "<a k1=\"v1\" k2=\"v2\"><b aa=\"ok\" bb = \"oo\"></b></a>";
		String expected = "<a" + lineSeparator() + //
				"    k1=\"v1\"" + lineSeparator() + //
				"    k2=\"v2\">" + lineSeparator() + //
				"  <b" + lineSeparator() + //
				"      aa=\"ok\"" + lineSeparator() + //
				"      bb=\"oo\"></b>" + lineSeparator() + //
				"</a>";
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		assertFormat(content, expected, settings, //
				te(0, 2, 0, 3, lineSeparator() + "    "), //
				te(0, 10, 0, 11, lineSeparator() + "    "), //
				te(0, 19, 0, 19, lineSeparator() + "  "), //
				te(0, 21, 0, 22, lineSeparator() + "      "), //
				te(0, 29, 0, 30, lineSeparator() + "      "), //
				te(0, 32, 0, 33, ""), //
				te(0, 34, 0, 35, ""), //
				te(0, 44, 0, 44, lineSeparator()));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testNestedAttributesNoSplit() throws BadLocationException {
		String content = "<a k1=\"v1\" k2=\"v2\"><b aa=\"ok\" bb = \"oo\"></b></a>";
		String expected = "<a k1=\"v1\" k2=\"v2\">" + lineSeparator() + //
				"  <b aa=\"ok\" bb=\"oo\"></b>" + lineSeparator() + //
				"</a>";
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(false);
		assertFormat(content, expected, settings, //
				te(0, 19, 0, 19, lineSeparator() + "  "), //
				te(0, 32, 0, 33, ""), //
				te(0, 34, 0, 35, ""), //
				te(0, 44, 0, 44, lineSeparator()));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testSplitAttributesProlog() throws BadLocationException {
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		String expected = content;
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		assertFormat(content, expected, settings);
	}

	@Test
	public void testSplitAttributesSingle() throws BadLocationException {
		String content = "<a k1=\"v1\"></a>";
		String expected = content;
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		assertFormat(content, expected, settings);
	}

	@Test
	public void testSplitAttributes() throws BadLocationException {
		String content = "<a k1=\"v1\" k2=\"v2\"></a>";
		String expected = "<a" + lineSeparator() + //
				"    k1=\"v1\"" + lineSeparator() + //
				"    k2=\"v2\"></a>";
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		assertFormat(content, expected, settings, //
				te(0, 2, 0, 3, lineSeparator() + "    "), //
				te(0, 10, 0, 11, lineSeparator() + "    "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testEndTagMissingCloseBracket2() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSpaceBeforeEmptyCloseTag(false);
		settings.getFormattingSettings().setSplitAttributes(true);

		String content = "<web-app \n" + //
				"         xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\"\n" + //
				"         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + //
				"         xsi:schemaLocation=\"http://xmlns.jcp.org/xml/ns/javaee \n" + //
				"                http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd\"\n" + //
				"         version=\"3.1\">\n" + //
				"         <servlet>\n" + //
				"             <servlet-name>sssi</servlet-name>\n" + //
				"         </servlet\n" + //
				"</web-app>";
		String expected = "<web-app\n" + //
				"    xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\"\n" + //
				"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + //
				"    xsi:schemaLocation=\"http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd\"\n"
				+ //
				"    version=\"3.1\">\n" + //
				"  <servlet>\n" + //
				"    <servlet-name>sssi</servlet-name>\n" + //
				"  </servlet\n" + //
				"</web-app>";
		assertFormat(content, expected, settings, //
				te(0, 8, 1, 9, "\n    "), //
				te(1, 51, 2, 9, "\n    "), //
				te(2, 62, 3, 9, "\n    "), //
				te(3, 63, 4, 16, " "), //
				te(4, 67, 5, 9, "\n    "), //
				te(5, 23, 6, 9, "\n  "), //
				te(6, 18, 7, 13, "\n    "), //
				te(7, 46, 8, 9, "\n  "));
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
