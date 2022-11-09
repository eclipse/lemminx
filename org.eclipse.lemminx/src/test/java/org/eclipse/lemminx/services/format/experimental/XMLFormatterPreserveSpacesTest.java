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
				"  <b>\r\n" + //
				"    c <d></d> e\r\n" + //
				"  </b>\r\n" + //
				"  <b xml:space=\"preserve\">\r\n" + //
				"    c  <d></d>  e\r\n" + //
				"  </b>\r\n" + //
				"</a>";

		assertFormat(content, expected, //
				te(2, 5, 2, 7, " "), //
				te(2, 14, 2, 16, " "));
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

	@Test
	public void preserveSpacesWithXsdStringWithNoMaxLineWidth() throws Exception {
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 src/test/resources/xsd/maven-4.0.0.xsd\">\r\n"
				+ //
				"  <description>a    b     c</description>\r\n" + //
				"  <description2>a    b     c</description2>\r\n" + //
				"</project>";

		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 src/test/resources/xsd/maven-4.0.0.xsd\">\r\n"
				+ //
				"  <description>a    b     c</description>\r\n" + // <-- preserve space because description is xs:string
				"  <description2>a b c</description2>\r\n" + // <-- no preserve space because description2 is not a
																// xs:string
				"</project>";

		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setMaxLineWidth(0);
		settings.getFormattingSettings().setGrammarAwareFormatting(true);
		assertFormat(content, expected, settings, //
				te(3, 17, 3, 21, " "), //
				te(3, 22, 3, 27, " "));
	}

	@Test
	public void preserveSpacesWithXsdStringWithMaxLineWidth() throws Exception {
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 src/test/resources/xsd/maven-4.0.0.xsd\">\r\n"
				+ //
				"  <description>a    b     c</description>\r\n" + //
				"  <description2>a    b     c</description2>\r\n" + //
				"</project>";

		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\r\n" + //
				"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 src/test/resources/xsd/maven-4.0.0.xsd\">\r\n"
				+ //
				"  <description>a    b     c</description>\r\n" + // <-- preserve space because description is xs:string
				"  <description2>a b c</description2>\r\n" + // <-- no preserve space because description2 is not a
																// xs:string
				"</project>";

		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setGrammarAwareFormatting(true);
		settings.getFormattingSettings().setMaxLineWidth(80);
		assertFormat(content, expected, settings, //
				te(1, 50, 1, 51, "\r\n  "), //
				te(1, 104, 1, 105, "\r\n  "), //
				te(3, 17, 3, 21, " "), //
				te(3, 22, 3, 27, " "));
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
