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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * XML experimental formatter services tests with range.
 *
 */
public class XMLFormatterWithRangeTest {

	@Test
	public void range() throws BadLocationException {
		String content = "<div  class = \"foo\">\n" + //
				"  |<img  src = \"foo\"/>|\n" + //
				" </div>";
		String expected = "<div  class = \"foo\">\n" + //
				"  <img src=\"foo\" />\n" + //
				" </div>";
		assertFormat(content, expected, //
				te(1, 6, 1, 8, " "), //
				te(1, 11, 1, 12, ""), //
				te(1, 13, 1, 14, ""), //
				te(1, 19, 1, 19, " "));
		
		content = "<div  class = \"foo\">\n" + //
				"  |<img src=\"foo\" />|\n" + //
				" </div>";
		assertFormat(content, expected);
	}

	@Test
	public void range2() throws BadLocationException {
		String content = "<div  class = \"foo\">\n" + //
				"  |<img  src = \"foo\"/>|\n" + //
				" \n" + //
				" </div>";
		String expected = "<div  class = \"foo\">\n" + //
				"  <img src=\"foo\" />\n" + //
				" \n" + //
				" </div>";
		assertFormat(content, expected, //
				te(1, 6, 1, 8, " "), //
				te(1, 11, 1, 12, ""), //
				te(1, 13, 1, 14, ""), //
				te(1, 19, 1, 19, " "));
		
		content = "<div  class = \"foo\">\n" + //
				"  |<img src=\"foo\" />|\n" + //
				" \n" + //
				" </div>";
		assertFormat(content, expected);
	}

	@Test
	public void rangeChildrenFullSelection() throws BadLocationException {
		String content = "<licenses>\n" + //
				"  <license>\n" + //
				"    <name>License Name</name>\n" + //
				"|           <url>abcdefghijklmnop</url>\n" + //
				"            <distribution>repo</distribution>|\n" + //
				"  </license>\n" + //
				"</licenses>";

		String expected = "<licenses>\n" + //
				"  <license>\n" + //
				"    <name>License Name</name>\n" + //
				"    <url>abcdefghijklmnop</url>\n" + //
				"    <distribution>repo</distribution>\n" + //
				"  </license>\n" + //
				"</licenses>";

		assertFormat(content, expected, //
				te(2, 29, 3, 11, "\n    "), //
				te(3, 38, 4, 12, "\n    "));
		assertFormat(expected, expected);
	}

	@Test
	public void rangeChildrenPartialSelection() throws BadLocationException {
		String content = "<licenses>\n" + //
				"  <license>\n" + //
				"  <name>Licen|se Name</name>\n" + //
				"              <url>abcdefghijklmnop</url>\n" + //
				"              <distribution>repo</distribution>|\n" + //
				"  </license>\n" + //
				"</licenses>";

		String expected = "<licenses>\n" + //
				"  <license>\n" + //
				"    <name>License Name</name>\n" + //
				"    <url>abcdefghijklmnop</url>\n" + //
				"    <distribution>repo</distribution>\n" + //
				"  </license>\n" + //
				"</licenses>";

		assertFormat(content, expected, //
				te(1, 11, 2, 2, "\n    "), //
				te(2, 27, 3, 14, "\n    "), //
				te(3, 41, 4, 14, "\n    "));
		assertFormat(expected, expected);
	}

	@Test
	public void rangeSelectAll() throws BadLocationException {
		String content = "<licenses>\n" + //
				"                                            <license>\n" + //
				"                        <name>License Name</name>\n" + //
				"        <url>abcdefghijklmnop</url>\n" + //
				"        <distribution>repo</distribution>\n" + //
				"                                        </license>\n" + //
				"                                                                </licenses>";

		String expected = "<licenses>\n" + //
				"  <license>\n" + //
				"    <name>License Name</name>\n" + //
				"    <url>abcdefghijklmnop</url>\n" + //
				"    <distribution>repo</distribution>\n" + //
				"  </license>\n" + //
				"</licenses>";

		assertFormat(content, expected, //
				te(0, 10, 1, 44, "\n  "), //
				te(1, 53, 2, 24, "\n    "), //
				te(2, 49, 3, 8, "\n    "), //
				te(3, 35, 4, 8, "\n    "), //
				te(4, 41, 5, 40, "\n  "), //
				te(5, 50, 6, 64, "\n"));
		assertFormat(expected, expected);
	}

	@Test
	public void rangeSelectOnlyPartialStartTagAndChildren() throws BadLocationException {
		String content = "<licenses>\n" + //
				"                                 <lice|nse>\n" + //
				"                <name>License Name</name>\n" + //
				"                        <url>abcdefghijklmnop</url>\n" + //
				"            <distribution>repo</distribution>|\n" + //
				"  </license>\n" + //
				"</licenses>";

		String expected = "<licenses>\n" + //
				"  <license>\n" + //
				"    <name>License Name</name>\n" + //
				"    <url>abcdefghijklmnop</url>\n" + //
				"    <distribution>repo</distribution>\n" + //
				"  </license>\n" + //
				"</licenses>";

		assertFormat(content, expected, //
				te(0, 10, 1, 33, "\n  "), //
				te(1, 42, 2, 16, "\n    "), //
				te(2, 41, 3, 24, "\n    "), //
				te(3, 51, 4, 12, "\n    "));
		assertFormat(expected, expected);
	}

	@Test
	public void rangeSelectOnlyFullStartTagAndChildren() throws BadLocationException {
		String content = "<licenses>\n" + //
				"                                 |<license>\n" + //
				"                <name>License Name</name>\n" + //
				"                        <url>abcdefghijklmnop</url>\n" + //
				"            <distribution>repo</distribution>|\n" + //
				"  </license>\n" + //
				"</licenses>";

		String expected = "<licenses>\n" + //
				"  <license>\n" + //
				"    <name>License Name</name>\n" + //
				"    <url>abcdefghijklmnop</url>\n" + //
				"    <distribution>repo</distribution>\n" + //
				"  </license>\n" + //
				"</licenses>";

		assertFormat(content, expected, //
				te(0, 10, 1, 33, "\n  "), //
				te(1, 42, 2, 16, "\n    "), //
				te(2, 41, 3, 24, "\n    "), //
				te(3, 51, 4, 12, "\n    "));
		assertFormat(expected, expected);
	}

	@Test
	public void rangeSelectOnlyPartialEndTagAndChildren() throws BadLocationException {
		String content = "<licenses>\n" + //
				"  <license>\n" + //
				"                <nam|e>License Name</name>\n" + //
				"                        <url>abcdefghijklmnop</url>\n" + //
				"            <distribution>repo</distribution>\n" + //
				"  </licen|se>\n" + //
				"</licenses>";

		String expected = "<licenses>\n" + //
				"  <license>\n" + //
				"    <name>License Name</name>\n" + //
				"    <url>abcdefghijklmnop</url>\n" + //
				"    <distribution>repo</distribution>\n" + //
				"  </license>\n" + //
				"</licenses>";

		assertFormat(content, expected, //
				te(1, 11, 2, 16, "\n    "), //
				te(2, 41, 3, 24, "\n    "), //
				te(3, 51, 4, 12, "\n    "));
		assertFormat(expected, expected);
	}

	@Test
	public void rangeSelectOnlyFullEndTagAndChildren() throws BadLocationException {
		String content = "<licenses>\n" + //
				"  <license>\n" + //
				"                <nam|e>License Name</name>\n" + //
				"                        <url>abcdefghijklmnop</url>\n" + //
				"            <distribution>repo</distribution>\n" + //
				"  </license>|\n" + //
				"</licenses>";

		String expected = "<licenses>\n" + //
				"  <license>\n" + //
				"    <name>License Name</name>\n" + //
				"    <url>abcdefghijklmnop</url>\n" + //
				"    <distribution>repo</distribution>\n" + //
				"  </license>\n" + //
				"</licenses>";

		assertFormat(content, expected, //
				te(1, 11, 2, 16, "\n    "), //
				te(2, 41, 3, 24, "\n    "), //
				te(3, 51, 4, 12, "\n    "));
		assertFormat(expected, expected);
	}

	@Test
	public void rangeSelectWithinText() throws BadLocationException {
		String content = "<licenses>\n" + //
				"                <name>Lic|en|se</name>\n" + //
				"</licenses>";

		String expected = "<licenses>\n" + //
				"                <name>License</name>\n" + //
				"</licenses>";

		assertFormat(content, expected);
	}

	@Disabled
	@Test
	public void rangeSelectEntityNoIndent() throws BadLocationException {
		String content = "<?xml version='1.0' standalone='no'?>\r\n" + //
				"<!DOCTYPE root-element [\r\n" + //
				"|<!ENTITY local \"LOCALLY DECLARED ENTITY\">|\r\n" + //
				"]>";
		String expected = "<?xml version='1.0' standalone='no'?>\r\n" + //
				"<!DOCTYPE root-element [\r\n" + //
				"  <!ENTITY local \"LOCALLY DECLARED ENTITY\">\r\n" + //
				"]>";
		assertFormat(content, expected);
	}

	@Test
	public void rangeSelectEntityWithIndent() throws BadLocationException {
		String content = "<?xml version='1.0' standalone='no'?>\r\n" + //
				"<!DOCTYPE root-element [\r\n" + //
				"  |<!ENTITY local \"LOCALLY DECLARED ENTITY\">|\r\n" + //
				"]>";
		String expected = "<?xml version='1.0' standalone='no'?>\r\n" + //
				"<!DOCTYPE root-element [\r\n" + //
				"  <!ENTITY local \"LOCALLY DECLARED ENTITY\">\r\n" + //
				"]>";
		assertFormat(content, expected);
	}
	
	@Disabled
	@Test
	public void testSplitAttributesRangeOneLine() throws BadLocationException {
		String content = "<note>\r\n" + //
				"  <from\r\n" + //
				"      |foo     =           \"bar\"|\r\n" + //
				"      bar=\"foo\">sss</from>\r\n" + //
				"</note>";

		String expected = "<note>\r\n" + //
				"  <from\r\n" + //
				"      foo=\"bar\"\r\n" + //
				"      bar=\"foo\">sss</from>\r\n" + //
				"</note>";

		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testSplitAttributesRangeMultipleLines() throws BadLocationException {
		String content = "<note>\r\n" + //
				"  <from\r\n" + //
				"        |foo       =       \"bar\"\r\n" + //
				"bar  =    \"foo\"   abc  =  \r\n" + //
				"    \"def\"\r\n" + //
				"      ghi=\"jkl\"|>sss</from>\r\n" + //
				"</note>";

		String expected = "<note>\r\n" + //
				"  <from\r\n" + //
				"      foo=\"bar\"\r\n" + //
				"      bar=\"foo\"\r\n" + //
				"      abc=\"def\"\r\n" + //
				"      ghi=\"jkl\">sss</from>\r\n" + //
				"</note>";
		;

		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		assertFormat(content, expected, settings);
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
