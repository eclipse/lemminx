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

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.TextEdit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * XML experimental formatter services tests with DTD.
 *
 */
public class XMLFormatterForDTDTest extends AbstractCacheBasedTest {

	@Disabled
	@Test
	public void testDoctypeNoInternalSubset() throws BadLocationException {
		String content = "<!DOCTYPE    note\r\n" + //
				"\r\n" + //
				">\r\n" + //
				"<note>\r\n" + //
				"  <to>Fred</to>\r\n" + //
				"\r\n" + //
				"  <from>Jani</from>\r\n" + //
				"\r\n" + //
				"  <heading>Reminder</heading>\r\n" + //
				"  \r\n" + //
				"  <body>Don't forget me this weekend</body>\r\n" + //
				"</note>";
		String expected = "<!DOCTYPE note>\r\n" + //
				"<note>\r\n" + //
				"  <to>Fred</to>\r\n" + //
				"\r\n" + //
				"  <from>Jani</from>\r\n" + //
				"\r\n" + //
				"  <heading>Reminder</heading>\r\n" + //
				"\r\n" + //
				"  <body>Don't forget me this weekend</body>\r\n" + //
				"</note>";
		assertFormat(content, expected);
	}

	@Test
	public void testDoctypeNoInternalSubsetNoNewlines() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreservedNewlines(0);

		String content = "<!DOCTYPE    note\r\n" + //
				"\r\n" + //
				">\r\n" + //
				"<note>\r\n" + //
				"  <to>Fred</to>\r\n" + //
				"\r\n" + //
				"  <from>Jani</from>\r\n" + //
				"\r\n" + //
				"  <heading>Reminder</heading>\r\n" + //
				"  \r\n" + //
				"  <body>Don't forget me this weekend</body>\r\n" + //
				"</note>";
		String expected = "<!DOCTYPE note>\r\n" + //
				"<note>\r\n" + //
				"  <to>Fred</to>\r\n" + //
				"  <from>Jani</from>\r\n" + //
				"  <heading>Reminder</heading>\r\n" + //
				"  <body>Don't forget me this weekend</body>\r\n" + //
				"</note>";
		assertFormat(content, expected, //
				te(0, 9, 0, 13, " "), //
				te(0, 17, 2, 0, ""), //
				te(4, 15, 6, 2, "\r\n  "), //
				te(6, 19, 8, 2, "\r\n  "), //
				te(8, 29, 10, 2, "\r\n  "));
		assertFormat(expected, expected);
	}

	@Disabled
	@Test
	public void testDoctypeInternalSubset() throws BadLocationException {
		String content = "<!DOCTYPE note\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"[        <!ELEMENT note (to,from,heading,body)>\r\n" + //
				"  <!ELEMENT to (#PCDATA)><!ELEMENT from (#PCDATA)>\r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  <!ELEMENT heading (#PCDATA)>\r\n" + //
				"  <!ELEMENT body (#PCDATA)>\r\n" + //
				"]>\r\n" + //
				"<note>\r\n" + //
				"  <to>Fred</to>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"  <from>Jani</from>\r\n" + //
				"  \r\n" + //
				"  <heading>Reminder</heading>\r\n" + //
				"  <body>Don't forget me this weekend</body>\r\n" + //
				"</note>";
		String expected = "<!DOCTYPE note [\r\n" + //
				"  <!ELEMENT note (to,from,heading,body)>\r\n" + //
				"  <!ELEMENT to (#PCDATA)>\r\n" + //
				"  <!ELEMENT from (#PCDATA)>\r\n" + //
				"  <!ELEMENT heading (#PCDATA)>\r\n" + //
				"  <!ELEMENT body (#PCDATA)>\r\n" + //
				"]>\r\n" + //
				"<note>\r\n" + //
				"  <to>Fred</to>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"  <from>Jani</from>\r\n" + //
				"\r\n" + //
				"  <heading>Reminder</heading>\r\n" + //
				"  <body>Don't forget me this weekend</body>\r\n" + //
				"</note>";
		assertFormat(content, expected);
	}

	@Disabled
	@Test
	public void testDoctypeInternalSubsetNoNewlines() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreservedNewlines(0);

		String content = "<!DOCTYPE note\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"[        <!ELEMENT note (to,from,heading,body)>\r\n" + //
				"  <!ELEMENT to (#PCDATA)><!ELEMENT from (#PCDATA)>\r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  <!ELEMENT heading (#PCDATA)>\r\n" + //
				"  <!ELEMENT body (#PCDATA)>\r\n" + //
				"]>\r\n" + //
				"<note>\r\n" + //
				"  <to>Fred</to>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"  <from>Jani</from>\r\n" + //
				"  \r\n" + //
				"  <heading>Reminder</heading>\r\n" + //
				"  <body>Don't forget me this weekend</body>\r\n" + //
				"</note>";
		String expected = "<!DOCTYPE note [\r\n" + //
				"  <!ELEMENT note (to,from,heading,body)>\r\n" + //
				"  <!ELEMENT to (#PCDATA)>\r\n" + //
				"  <!ELEMENT from (#PCDATA)>\r\n" + //
				"  <!ELEMENT heading (#PCDATA)>\r\n" + //
				"  <!ELEMENT body (#PCDATA)>\r\n" + //
				"]>\r\n" + //
				"<note>\r\n" + //
				"  <to>Fred</to>\r\n" + //
				"  <from>Jani</from>\r\n" + //
				"  <heading>Reminder</heading>\r\n" + //
				"  <body>Don't forget me this weekend</body>\r\n" + //
				"</note>";
		assertFormat(content, expected, settings);
	}

	@Test
	public void testDoctypeInternalDeclSpacesBetweenParameters() throws BadLocationException {
		String content = "<!DOCTYPE note [\r\n" + //
				"  <!ELEMENT    note (to,from,heading,body)>\r\n" + //
				"  <!ELEMENT   to     (#PCDATA)>\r\n" + //
				"  <!ELEMENT from (#PCDATA)>\r\n" + //
				"  <!ELEMENT heading   (#PCDATA)>\r\n" + //
				"  <!ELEMENT body (#PCDATA)>\r\n" + //
				"]>\r\n" + //
				"<note>\r\n" + //
				"  <to>Fred</to>\r\n" + //
				"  <from>Jani</from>\r\n" + //
				"  <heading>Reminder</heading>\r\n" + //
				"  <body>Don't forget me this weekend</body>\r\n" + //
				"</note>";
		String expected = "<!DOCTYPE note [\r\n" + //
				"  <!ELEMENT note (to,from,heading,body)>\r\n" + //
				"  <!ELEMENT to (#PCDATA)>\r\n" + //
				"  <!ELEMENT from (#PCDATA)>\r\n" + //
				"  <!ELEMENT heading (#PCDATA)>\r\n" + //
				"  <!ELEMENT body (#PCDATA)>\r\n" + //
				"]>\r\n" + //
				"<note>\r\n" + //
				"  <to>Fred</to>\r\n" + //
				"  <from>Jani</from>\r\n" + //
				"  <heading>Reminder</heading>\r\n" + //
				"  <body>Don't forget me this weekend</body>\r\n" + //
				"</note>";
		assertFormat(content, expected, //
				te(1, 11, 1, 15, " "), //
				te(2, 11, 2, 14, " "), //
				te(2, 16, 2, 21, " "), //
				te(4, 19, 4, 22, " "));
		assertFormat(expected, expected);
	}

	@Disabled
	@Test
	public void testDoctypeInternalWithAttlist() throws BadLocationException {
		String content = "<!DOCTYPE note \r\n" + //
				"[\r\n" + //
				"  <!ELEMENT note (to,from,heading,body)>\r\n" + //
				"\r\n" + //
				"  <!ELEMENT to (#PCDATA)>\r\n" + //
				"\r\n" + //
				"  <!ATTLIST payment type CDATA \"check\">\r\n" + //
				"\r\n" + //
				"]>\r\n" + //
				"\r\n" + //
				"<note>\r\n" + //
				"  \r\n" + //
				"  <to>Fred</to>\r\n" + //
				"</note>";
		String expected = "<!DOCTYPE note [\r\n" + //
				"  <!ELEMENT note (to,from,heading,body)>\r\n" + //
				"  <!ELEMENT to (#PCDATA)>\r\n" + //
				"  <!ATTLIST payment type CDATA \"check\">\r\n" + //
				"]>\r\n" + //
				"\r\n" + //
				"<note>\r\n" + //
				"\r\n" + //
				"  <to>Fred</to>\r\n" + //
				"</note>";
		assertFormat(content, expected);
	}

	@Test
	public void testDoctypeInternalAllDecls() throws BadLocationException {
		String content = "<!DOCTYPE note\r\n" + //
				"[\r\n" + //
				"\r\n" + //
				"  <!ELEMENT note (to,from,heading,body)>\r\n" + //
				"\r\n" + //
				"  <!ATTLIST payment type CDATA \"check\">\r\n" + //
				"\r\n" + //
				"  <!ENTITY copyright SYSTEM \"https://www.w3schools.com/entities.dtd\">\r\n" + //
				"\r\n" + //
				"  <!NOTATION png PUBLIC \"PNG 1.0\" \"image/png\">\r\n" + //
				"]>\r\n" + //
				"";
		String expected = "<!DOCTYPE note [\r\n" + //
				"  <!ELEMENT note (to,from,heading,body)>\r\n" + //
				"  <!ATTLIST payment type CDATA \"check\">\r\n" + //
				"  <!ENTITY copyright SYSTEM \"https://www.w3schools.com/entities.dtd\">\r\n" + //
				"  <!NOTATION png PUBLIC \"PNG 1.0\" \"image/png\">\r\n" + //
				"]>";
		assertFormat(content, expected, //
				te(0, 14, 1, 0, " "), //
				te(1, 1, 3, 2, "\r\n  "), //
				te(3, 40, 5, 2, "\r\n  "), //
				te(5, 39, 7, 2, "\r\n  "), //
				te(7, 69, 9, 2, "\r\n  "), //
				te(10, 2, 11, 0, ""));
		assertFormat(expected, expected);
	}

	@Disabled
	@Test
	public void testDoctypeInternalWithComments() throws BadLocationException {
		String content = "<!DOCTYPE note\r\n" + //
				"[ \r\n" + //
				"  <!-- comment -->\r\n" + //
				"\r\n" + //
				"  <!ELEMENT note (to,from,heading,body)>\r\n" + //
				"  \r\n" + //
				"  \r\n" + //
				"  <!ATTLIST payment type CDATA \"check\">\r\n" + //
				"  \r\n" + //
				"  <!-- comment -->\r\n" + //
				"  \r\n" + //
				"  <!ENTITY copyright SYSTEM \"https://www.w3schools.com/entities.dtd\">\r\n" + //
				"  <!NOTATION png PUBLIC \"PNG 1.0\" \"image/png\">\r\n" + //
				"]>\r\n" + //
				"";
		String expected = "<!DOCTYPE note [\r\n" + //
				"  <!-- comment -->\r\n" + //
				"  <!ELEMENT note (to,from,heading,body)>\r\n" + //
				"  <!ATTLIST payment type CDATA \"check\">\r\n" + //
				"  <!-- comment -->\r\n" + //
				"  <!ENTITY copyright SYSTEM \"https://www.w3schools.com/entities.dtd\">\r\n" + //
				"  <!NOTATION png PUBLIC \"PNG 1.0\" \"image/png\">\r\n" + //
				"]>";
		assertFormat(content, expected);
	}

	@Disabled
	@Test
	public void testDoctypeInternalWithText() throws BadLocationException {
		String content = "<!DOCTYPE note\r\n" + //
				"[\r\n" + //
				"  <!ELEMENT note (to,from,heading,body)>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"  garbageazg df\r\n" + //
				"                gdf\r\n" + //
				"garbageazgdfg\r\n" + //
				"  df\r\n" + //
				"  gd\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"  \r\n" + //
				"  <!ELEMENT note (to,from,heading,body)>\r\n" + //
				"  \r\n" + //
				"]>";
		String expected = "<!DOCTYPE note [\r\n" + //
				"  <!ELEMENT note (to,from,heading,body)>\r\n" + //
				"  garbageazg df\r\n" + //
				"                gdf\r\n" + //
				"garbageazgdfg\r\n" + //
				"  df\r\n" + //
				"  gd\r\n" + //
				"  <!ELEMENT note (to,from,heading,body)>\r\n" + //
				"]>";
		assertFormat(content, expected);
	}

	@Test
	public void testDTDMultiParameterAttlist() throws BadLocationException {
		SharedSettings settings = new SharedSettings();

		String content = "\r\n<!ATTLIST array name CDATA #IMPLIED description CDATA #IMPLIED disabled CDATA #IMPLIED>";
		String expected = "<!ATTLIST array\r\n" + //
				"  name CDATA #IMPLIED\r\n" + //
				"  description CDATA #IMPLIED\r\n" + //
				"  disabled CDATA #IMPLIED>";
		assertDTDFormat(content, expected, settings, //
				te(0, 0, 1, 0, ""), //
				te(1, 15, 1, 16, "\r\n  "), //
				te(1, 35, 1, 36, "\r\n  "), //
				te(1, 62, 1, 63, "\r\n  "));
		assertDTDFormat(expected, expected);
	}

	@Disabled
	@Test
	public void testDTDIndentation() throws BadLocationException {
		SharedSettings settings = new SharedSettings();

		String content = "  <!ELEMENT note (to,from,heading,body)>\r\n" + //
				"			\r\n" + //
				"			<!ATTLIST payment type CDATA \"check\">\r\n" + //
				"			\r\n" + //
				"				  <!ENTITY copyright SYSTEM \"https://www.w3schools.com/entities.dtd\">\r\n" + //
				"				\r\n" + //
				"				  <!NOTATION png PUBLIC \"PNG 1.0\" \"image/png\">";
		String expected = "<!ELEMENT note (to,from,heading,body)>\r\n" + //
				"<!ATTLIST payment type CDATA \"check\">\r\n" + //
				"<!ENTITY copyright SYSTEM \"https://www.w3schools.com/entities.dtd\">\r\n" + //
				"<!NOTATION png PUBLIC \"PNG 1.0\" \"image/png\">";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testDTDNotEndBrackets() throws BadLocationException {
		SharedSettings settings = new SharedSettings();

		String content = "<!ELEMENT note (to,from,heading,body)\r\n" + //
				"\r\n" + //
				"<!ATTLIST payment type CDATA \"check\"\r\n" + //
				"\r\n" + //
				"<!ENTITY copyright SYSTEM \"https://www.w3schools.com/entities.dtd\"\r\n" + //
				"<!NOTATION png PUBLIC \"PNG 1.0\" \"image/png\"";
		String expected = "<!ELEMENT note (to,from,heading,body)\r\n" + //
				"<!ATTLIST payment type CDATA \"check\"\r\n" + //
				"<!ENTITY copyright SYSTEM \"https://www.w3schools.com/entities.dtd\"\r\n" + //
				"<!NOTATION png PUBLIC \"PNG 1.0\" \"image/png\"";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testDTDUnknownDeclNameAndText() throws BadLocationException {
		SharedSettings settings = new SharedSettings();

		String content = "<!ELEMENT note (to,from,heading,body)>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"  <!hellament afsfas >\r\n" + //
				"\r\n" + //
				"  asdasd\r\n" + //
				"  asd\r\n" + //
				"\r\n" + //
				"<!ATTLIST payment type CDATA \"check\">\r\n" + //
				"<!ENTITY copyright SYSTEM \"https://www.w3schools.com/entities.dtd\">\r\n" + //
				"<!NOTATION png PUBLIC \"PNG 1.0\" \"image/png\">";
		String expected = "<!ELEMENT note (to,from,heading,body)>\r\n" + //
				"<!hellament afsfas >\r\n" + //
				"asdasd\r\n" + //
				"  asd\r\n" + //
				"<!ATTLIST payment type CDATA \"check\">\r\n" + //
				"<!ENTITY copyright SYSTEM \"https://www.w3schools.com/entities.dtd\">\r\n" + //
				"<!NOTATION png PUBLIC \"PNG 1.0\" \"image/png\">";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testAllDoctypeParameters() throws BadLocationException {
		String content = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\r\n" + //
				"<!DOCTYPE web-app PUBLIC \"-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN\" \"http://java.sun.com/dtd/web-app_2_3.dtd\" [\r\n"
				+ //
				"        <!ELEMENT h1 %horiz.model;>\r\n" + //
				"  <!ATTLIST h1 %all;>\r\n" + //
				"  <!ELEMENT h2 %horiz.model;>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"          <!ATTLIST h2 %all;>\r\n" + //
				"  <!ELEMENT h3 %horiz.model;>\r\n" + //
				"  <!ATTLIST h3 %all;>\r\n" + //
				"]\r\n" + //
				"\r\n" + //
				"\r\n" + //
				">\r\n" + //
				"<web-app>\r\n" + //
				"  <display-name>sdsd</display-name>\r\n" + //
				"\r\n" + //
				"  <servlet>\r\n" + //
				"    \r\n" + //
				"    <servlet-name>er</servlet-name>\r\n" + //
				"    <servlet-class>dd</servlet-class>\r\n" + //
				"  </servlet>\r\n" + //
				"</web-app>";
		String expected = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\r\n" + //
				"<!DOCTYPE web-app PUBLIC \"-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN\" \"http://java.sun.com/dtd/web-app_2_3.dtd\" [\r\n"
				+ //
				"  <!ELEMENT h1 %horiz.model;>\r\n" + //
				"  <!ATTLIST h1 %all;>\r\n" + //
				"  <!ELEMENT h2 %horiz.model;>\r\n" + //
				"  <!ATTLIST h2 %all;>\r\n" + //
				"  <!ELEMENT h3 %horiz.model;>\r\n" + //
				"  <!ATTLIST h3 %all;>\r\n" + //
				"]>\r\n" + //
				"<web-app>\r\n" + //
				"  <display-name>sdsd</display-name>\r\n" + //
				"\r\n" + //
				"  <servlet>\r\n" + //
				"\r\n" + //
				"    <servlet-name>er</servlet-name>\r\n" + //
				"    <servlet-class>dd</servlet-class>\r\n" + //
				"  </servlet>\r\n" + //
				"</web-app>";
		assertFormat(content, expected);
	}

	@Disabled
	@Test
	public void testDTDElementContentWithAsterisk() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		String content = "<!ELEMENT data    (#PCDATA | data | d0)*   >";
		String expected = "<!ELEMENT data (#PCDATA | data | d0)*>";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testDoctypeSingleLineFormat() throws BadLocationException {
		String content = "<!DOCTYPE name [<!-- MY COMMENT --><!NOTATION postscript SYSTEM \"ghostview\">]>\r\n" + //
				"";
		String expected = "<!DOCTYPE name [\r\n" + //
				"  <!-- MY COMMENT -->\r\n" + //
				"  <!NOTATION postscript SYSTEM \"ghostview\">\r\n" + //
				"]>";
		assertFormat(content, expected);
	}

	@Test
	public void testDoctypeInvalidParameter() throws BadLocationException {
		String content = "<!DOCTYPE name \"url\" [\r\n" + //
				"  <!-- MY COMMENT -->\r\n" + //
				"  <!NOTATION postscript SYSTEM \"ghostview\">\r\n" + //
				"]>";
		String expected = "<!DOCTYPE name \"url\" [\r\n" + //
				"  <!-- MY COMMENT -->\r\n" + //
				"  <!NOTATION postscript SYSTEM \"ghostview\">\r\n" + //
				"]>";
		assertFormat(content, expected);
	}

	@Test
	public void testDoctypeInvalidParameterUnclosed() throws BadLocationException {
		String content = "<!DOCTYPE name \"url\"[ <!-- MY COMMENT -->\r\n" + //
				"  <!NOTATION postscript SYSTEM \"ghostview\">\r\n" + //
				"]\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"<a></a>";
		String expected = "<!DOCTYPE name \"url\"[ <!-- MY COMMENT -->\r\n" + //
				"  <!NOTATION postscript SYSTEM \"ghostview\">\r\n" + //
				"]\r\n" + //
				"<a></a>";
		assertFormat(content, expected, //
				te(2, 1, 5, 0, "\r\n"));
		assertFormat(expected, expected);
	}

	@Disabled
	@Test
	public void testUnclosedSystemId() throws BadLocationException {
		String content = "<!DOCTYPE name PUBLIC \"lass\" \"bass [ <!-- MY COMMENT -->\r\n" + //
				"\r\n" + //
				"  <!NOTATION postscript SYSTEM \"ghostview\">\r\n" + //
				"]>\r\n" + //
				"\r\n" + //
				"<a></a>";
		String expected = "<!DOCTYPE name PUBLIC \"lass\" \"bass [ <!-- MY COMMENT -->\r\n" + //
				"\r\n" + //
				"  <!NOTATION postscript SYSTEM \"ghostview\">\r\n" + //
				"]>\r\n" + //
				"\r\n" + //
				"<a></a>";
		assertFormat(content, expected);
	}

	@Disabled
	@Test
	public void testUnclosedPublicId() throws BadLocationException {
		String content = "<!DOCTYPE name PUBLIC \"lass  [ <!-- MY COMMENT -->\r\n" + //
				"\r\n" + //
				"  <!NOTATION postscript SYSTEM \"ghostview\">\r\n" + //
				"]>\r\n" + //
				"\r\n" + //
				"<a></a>";
		String expected = "<!DOCTYPE name PUBLIC \"lass  [ <!-- MY COMMENT -->\r\n" + //
				"\r\n" + //
				"  <!NOTATION postscript SYSTEM \"ghostview\">\r\n" + //
				"]>\r\n" + //
				"\r\n" + //
				"<a></a>";
		assertFormat(content, expected);
	}

	@Disabled
	@Test
	public void testCommentAfterMissingClosingBracket() throws BadLocationException {
		String content = "<!DOCTYPE name [\r\n" + //
				"  <!ENTITY % astroTerms SYSTEM \"http://xml.gsfc.nasa.gov/DTD/entities/astroTerms.ent\"\r\n" + //
				"\r\n" + //
				"  <!-- MY COMMENT -->\r\n" + //
				"]>\r\n" + //
				"\r\n" + //
				"<a></a>";
		String expected = "<!DOCTYPE name [\r\n" + //
				"  <!ENTITY % astroTerms SYSTEM \"http://xml.gsfc.nasa.gov/DTD/entities/astroTerms.ent\"\r\n" + //
				"  <!-- MY COMMENT -->\r\n" + //
				"]>\r\n" + //
				"\r\n" + //
				"<a></a>";
		assertFormat(content, expected);
	}

	@Disabled
	@Test
	public void testHTMLDTD() throws BadLocationException {
		SharedSettings settings = new SharedSettings();

		String content = "<!--\r\n" + //
				"  Further information about HTML 4.01 is available at:\r\n" + //
				"-->\r\n" + //
				"<!ENTITY % HTML.Version \"-//W3C//DTD HTML 4.01 Frameset//EN\"\r\n" + //
				"  -- Typical usage:\r\n" + //
				"\r\n" + //
				"    <!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\"\r\n" + //
				"            \"http://www.w3.org/TR/html4/frameset.dtd\">\r\n" + //
				"    <html>\r\n" + //
				"    <head>\r\n" + //
				"    ...\r\n" + //
				"    </head>\r\n" + //
				"    <frameset>\r\n" + //
				"    ...\r\n" + //
				"    </frameset>\r\n" + //
				"    </html>\r\n" + //
				"-->\r\n" + //
				"\r\n" + //
				"<!ENTITY % HTML.Frameset \"INCLUDE\">\r\n" + //
				"<!ENTITY % HTML4.dtd PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\r\n" + //
				"%HTML4.dtd;";
		String expected = "<!--\r\n" + //
				"  Further information about HTML 4.01 is available at:\r\n" + //
				"-->\r\n" + //
				"<!ENTITY % HTML.Version \"-//W3C//DTD HTML 4.01 Frameset//EN\" -- Typical usage:\r\n" + //
				"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\"\r\n" + //
				"            \"http://www.w3.org/TR/html4/frameset.dtd\">\r\n" + //
				"<html>\r\n" + //
				"<head>\r\n" + //
				"...\r\n" + //
				"</head>\r\n" + //
				"<frameset>\r\n" + //
				"...\r\n" + //
				"</frameset>\r\n" + //
				"</html>\r\n" + //
				"-->\r\n" + //
				"<!ENTITY % HTML.Frameset \"INCLUDE\">\r\n" + //
				"<!ENTITY % HTML4.dtd PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\r\n" + //
				"%HTML4.dtd;";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testXMLInDTDFile() throws BadLocationException {
		SharedSettings settings = new SharedSettings();

		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<resources variant=\"\">\r\n" + //
				"    <resource name=\"res00\" >\r\n" + //
				"        <property name=\"propA\" value=\"...\" />\r\n" + //
				"        <property name=\"propB\" value=\"...\" />\r\n" + //
				"    </resource>\r\n" + //
				"</resources>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<resources variant=\"\">\r\n" + //
				"<resource name=\"res00\" >\r\n" + //
				"<property name=\"propA\" value=\"...\" />\r\n" + //
				"<property name=\"propB\" value=\"...\" />\r\n" + //
				"</resource>\r\n" + //
				"</resources>";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testBadDTDFile() throws BadLocationException {
		SharedSettings settings = new SharedSettings();

		String content = "<![ %HTML.Reserved; [\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"<!ENTITY % reserved\r\n" + //
				" \"datasrc     %URI;          #IMPLIED  -- \"\r\n" + //
				"  >\r\n" + //
				"\r\n" + //
				"]]>\r\n" + //
				"\r\n" + //
				"<!--=================== Text Markup ======================================-->";
		String expected = "<![ %HTML.Reserved; [\r\n" + //
				"<!ENTITY % reserved \"datasrc     %URI;          #IMPLIED  -- \">\r\n" + //
				"]]>\r\n" + //
				"<!--=================== Text Markup ======================================-->";
		assertFormat(content, expected, settings);
	}

	@Disabled
	@Test
	public void testIncompleteAttlistInternalDecl() throws BadLocationException {
		SharedSettings settings = new SharedSettings();

		String content = "<!ATTLIST img src CDATA #REQUIRED %all;\r\n" + //
				">\r\n" + //
				"\r\n" + //
				"<!-- Hypertext anchors. -->";
		String expected = "<!ATTLIST img\r\n" + //
				"  src CDATA #REQUIRED\r\n" + //
				"  %all;\r\n" + //
				">\r\n" + //
				"<!-- Hypertext anchors. -->";
		assertDTDFormat(content, expected, settings);
	}

	private static void assertDTDFormat(String unformatted, String actual, TextEdit... expectedEdits)
			throws BadLocationException {
		assertDTDFormat(unformatted, actual, new SharedSettings(), expectedEdits);
	}

	private static void assertDTDFormat(String unformatted, String expected, SharedSettings sharedSettings,
			TextEdit... expectedEdits) throws BadLocationException {
		assertFormat(unformatted, expected, sharedSettings, "test.dtd", expectedEdits);
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
