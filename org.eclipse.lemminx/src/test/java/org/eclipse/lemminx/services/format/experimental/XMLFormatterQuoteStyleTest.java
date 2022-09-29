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

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.settings.EnforceQuoteStyle;
import org.eclipse.lemminx.settings.QuoteStyle;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.TextEdit;
import org.junit.jupiter.api.Test;

/**
 * XML experimental formatter services tests with Quote style setting.
 *
 */
public class XMLFormatterQuoteStyleTest {

	@Test
	public void testUseDoubleQuotesFromDoubleQuotes() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		settings.getPreferences().setQuoteStyle(QuoteStyle.doubleQuotes);

		String content = "<a name=  \" value \"> </a>";
		String expected = "<a name=\" value \"> </a>";
		assertFormat(content, expected, settings, //
				te(0, 8, 0, 10, ""));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testUseSingleQuotesFromSingleQuotes() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		String content = "<a name=  \' value \'> </a>";
		String expected = "<a name=\' value \'> </a>";
		assertFormat(content, expected, settings, //
				te(0, 8, 0, 10, ""));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testUseSingleQuotesFromDoubleQuotes() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);

		String content = "<a name=  \" value \"> </a>";
		String expected = "<a name=\' value \'> </a>";
		assertFormat(content, expected, settings, //
				te(0, 8, 0, 10, ""), //
				te(0, 10, 0, 11, "'"), //
				te(0, 18, 0, 19, "'"));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testUseDoubleQuotesFromSingleQuotes() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		String content = "<a name=  \' value \'> </a>";
		String expected = "<a name=\" value \"> </a>";
		assertFormat(content, expected, settings, //
				te(0, 8, 0, 10, ""), //
				te(0, 10, 0, 11, "\""), //
				te(0, 18, 0, 19, "\""));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testUseDoubleQuotesFromSingleQuotesUnclosedEnd() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		String content = "<a name=  \' value > </a>";
		String expected = "<a name=\" value > </a>";
		assertFormat(content, expected, settings, //
				te(0, 8, 0, 10, ""), //
				te(0, 10, 0, 11, "\""));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testUseDoubleQuotesFromSingleQuotesUnclosedStart() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		String content = "<a name=  value \'> </a>";
		String expected = "<a name= value \"> </a>";
		assertFormat(content, expected, settings, //
				te(0, 8, 0, 10, " "), //
				te(0, 16, 0, 17, "\""));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testUseDoubleQuotesFromSingleQuotesMisMatchStart() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		String content = "<a name=  \' value \"> </a>";
		String expected = "<a name=\" value \"> </a>";
		assertFormat(content, expected, settings, //
				te(0, 8, 0, 10, ""), //
				te(0, 10, 0, 11, "\""));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testUseDoubleQuotesFromSingleQuotesMisMatchEnd() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		String content = "<a name=  \" value \'> </a>";
		String expected = "<a name=\" value \'> </a>";
		assertFormat(content, expected, settings, //
				te(0, 8, 0, 10, ""));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testUseSingleQuotesNoQuotes() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		String content = "<a name = test> </a>";
		String expected = "<a name= test> </a>";
		assertFormat(content, expected, settings, //
				te(0, 7, 0, 8, ""));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testUseSingleQuotesNoQuotesSplit() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setSplitAttributes(true);
		String content = "<a name = test> </a>";
		String expected = "<a" + lineSeparator() + "    name=" + lineSeparator() + "    test> </a>";
		assertFormat(content, expected, settings, //
				te(0, 2, 0, 3, lineSeparator() + "    "), //
				te(0, 7, 0, 8, ""), //
				te(0, 9, 0, 10, lineSeparator() + "    "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testAttValueOnlyStartQuote() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		String content = "<a name = \"> </a>";
		String expected = "<a name=\"> </a>";
		assertFormat(content, expected, settings, //
				te(0, 7, 0, 8, ""), //
				te(0, 9, 0, 10, ""));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testUseDoubleQuotesMultipleAttributes() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		String content = "<a name1=  \" value1 \"  name2= \" value2 \"   name3= \' value3 \' > </a>";
		String expected = "<a name1=\" value1 \" name2=\" value2 \" name3=\" value3 \"> </a>";
		assertFormat(content, expected, settings, //
				te(0, 9, 0, 11, ""), //
				te(0, 21, 0, 23, " "), //
				te(0, 29, 0, 30, ""), //
				te(0, 40, 0, 43, " "), //
				te(0, 49, 0, 50, ""), //
				te(0, 50, 0, 51, "\""), //
				te(0, 59, 0, 60, "\""), //
				te(0, 60, 0, 61, ""));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testUseSingleQuotesMultipleAttributes() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		String content = "<a name1=  \" value1 \"  name2= \" value2 \"   name3= \' value3 \' > </a>";
		String expected = "<a name1=\' value1 \' name2=\' value2 \' name3=\' value3 \'> </a>";
		assertFormat(content, expected, settings, //
				te(0, 9, 0, 11, ""), //
				te(0, 11, 0, 12, "'"), //
				te(0, 20, 0, 21, "'"), //
				te(0, 21, 0, 23, " "), //
				te(0, 29, 0, 30, ""), //
				te(0, 30, 0, 31, "'"), //
				te(0, 39, 0, 40, "'"), //
				te(0, 40, 0, 43, " "), //
				te(0, 49, 0, 50, ""), //
				te(0, 60, 0, 61, ""));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testUseDoubleQuotesMultipleAttributesSplit() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);

		String content = "<a name1=  \" value1 \"  name2= \" value2 \"   name3= \' value3 \' > </a>\n";
		String expected = "<a\n" + "    name1=\" value1 \"\n" + "    name2=\" value2 \"\n" + //
				"    name3=\" value3 \"> </a>";
		assertFormat(content, expected, settings, //
				te(0, 2, 0, 3, "\n    "), //
				te(0, 9, 0, 11, ""), //
				te(0, 21, 0, 23, "\n    "), //
				te(0, 29, 0, 30, ""), //
				te(0, 40, 0, 43, "\n    "), //
				te(0, 49, 0, 50, ""), //
				te(0, 50, 0, 51, "\""), //
				te(0, 59, 0, 60, "\""), //
				te(0, 60, 0, 61, ""), //
				te(0, 67, 1, 0, ""));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testUseSingleQuotesMultipleAttributesSplit() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		String content = "<a name1=  \" value1 \"  name2= \" value2 \"   name3= \' value3 \' > </a>\n";
		String expected = "<a\n" + "    name1=\' value1 \'\n" + "    name2=\' value2 \'\n" + //
				"    name3=\' value3 \'> </a>";
		assertFormat(content, expected, settings, //
				te(0, 2, 0, 3, "\n    "), //
				te(0, 9, 0, 11, ""), //
				te(0, 11, 0, 12, "'"), //
				te(0, 20, 0, 21, "'"), //
				te(0, 21, 0, 23, "\n    "), //
				te(0, 29, 0, 30, ""), //
				te(0, 30, 0, 31, "'"), //
				te(0, 39, 0, 40, "'"), //
				te(0, 40, 0, 43, "\n    "), //
				te(0, 49, 0, 50, ""), //
				te(0, 60, 0, 61, ""), //
				te(0, 67, 1, 0, ""));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testUseSingleQuotesLocalDTD() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		String content = "<!DOCTYPE note SYSTEM \"note.dtd\">";
		String expected = "<!DOCTYPE note SYSTEM \'note.dtd\'>";
		assertFormat(content, expected, settings, //
				te(0, 22, 0, 23, "'"), //
				te(0, 31, 0, 32, "'"));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testUseSingleQuotesLocalDTDUnclosedStart() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		String content = "<!DOCTYPE note SYSTEM note.dtd\">";
		String expected = "<!DOCTYPE note SYSTEM note.dtd\">";
		assertFormat(content, expected, settings);
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testUseSingleQuotesLocalDTDUnclosedEnd() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		String content = "<!DOCTYPE note SYSTEM \"note.dtd>";
		String expected = "<!DOCTYPE note SYSTEM \"note.dtd>";
		assertFormat(content, expected, settings);
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testUseSingleQuotesLocalDTDWithSubset() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		String content = "<!DOCTYPE article [\n" + //
				"  <!ENTITY AUTHOR \"John Doe\">\n" + //
				"  <!ENTITY COMPANY \"JD Power Tools, Inc.\">\n" + //
				"  <!ENTITY EMAIL \"jd@jd-tools.com\">\n" + //
				"  <!ELEMENT E EMPTY>\n" + //
				"  <!ATTLIST E WIDTH CDATA \"0\">\n" + //
				"]>\n" + //
				"\n" + //
				"<root attr=\"hello\"></root>";
		String expected = "<!DOCTYPE article [\n" + //
				"  <!ENTITY AUTHOR \'John Doe\'>\n" + //
				"  <!ENTITY COMPANY \'JD Power Tools, Inc.\'>\n" + //
				"  <!ENTITY EMAIL \'jd@jd-tools.com\'>\n" + //
				"  <!ELEMENT E EMPTY>\n" + //
				"  <!ATTLIST E WIDTH CDATA \'0\'>\n" + //
				"]>\n" + //
				"<root attr=\'hello\'></root>";
		assertFormat(content, expected, settings, //
				te(1, 18, 1, 19, "'"), //
				te(1, 27, 1, 28, "'"), //
				te(2, 19, 2, 20, "'"), //
				te(2, 40, 2, 41, "'"), //
				te(3, 17, 3, 18, "'"), //
				te(3, 33, 3, 34, "'"), //
				te(5, 26, 5, 27, "'"), //
				te(5, 28, 5, 29, "'"), //
				te(6, 2, 8, 0, "\n"), //
				te(8, 11, 8, 12, "'"), //
				te(8, 17, 8, 18, "'"));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testUseSingleQuotesLocalDTDWithSubsetUnclosed() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		String content = "<!DOCTYPE article [\n" + //
				"  <!ENTITY AUTHOR John Doe\">\n" + //
				"  <!ENTITY COMPANY \"JD Power Tools, Inc.>\n" + //
				"  <!ENTITY EMAIL \"jd@jd-tools.com\">\n" + //
				"  <!ELEMENT E EMPTY>\n" + //
				"  <!ATTLIST E WIDTH CDATA 0\">\n" + //
				"]>\n" + //
				"\n" + //
				"<root attr=\"hello\"></root>";
		String expected = "<!DOCTYPE article [\n" + //
				"  <!ENTITY AUTHOR John Doe\">\n" + //
				"  <!ENTITY COMPANY \"JD Power Tools, Inc.>\n" + //
				"  <!ENTITY EMAIL \'jd@jd-tools.com\'>\n" + //
				"  <!ELEMENT E EMPTY>\n" + //
				"  <!ATTLIST E WIDTH CDATA 0\">\n" + //
				"]>\n" + //
				"<root attr=\'hello\'></root>";
		assertFormat(content, expected, settings, //
				te(3, 17, 3, 18, "'"), //
				te(3, 33, 3, 34, "'"), //
				te(6, 2, 8, 0, "\n"), //
				te(8, 11, 8, 12, "'"), //
				te(8, 17, 8, 18, "'"));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testUseSingleQuotesDTDFile() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		String content = "<!ENTITY AUTHOR \"John Doe\">\n" + //
				"<!ENTITY COMPANY \"JD Power Tools, Inc.\">\n" + //
				"<!ENTITY EMAIL \"jd@jd-tools.com\">\n" + //
				"<!ELEMENT E EMPTY>\n" + //
				"<!ATTLIST E WIDTH CDATA \"0\">";
		String expected = "<!ENTITY AUTHOR \'John Doe\'>\n" + //
				"<!ENTITY COMPANY \'JD Power Tools, Inc.\'>\n" + //
				"<!ENTITY EMAIL \'jd@jd-tools.com\'>\n" + //
				"<!ELEMENT E EMPTY>\n" + //
				"<!ATTLIST E WIDTH CDATA \'0\'>";
		assertFormat(content, expected, settings, "test.dtd", //
				te(0, 16, 0, 17, "'"), //
				te(0, 25, 0, 26, "'"), //
				te(1, 17, 1, 18, "'"), //
				te(1, 38, 1, 39, "'"), //
				te(2, 15, 2, 16, "'"), //
				te(2, 31, 2, 32, "'"), //
				te(4, 24, 4, 25, "'"), //
				te(4, 26, 4, 27, "'"));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testDontFormatQuotesByDefault() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		String content = "<a number=\'\"one\"\'></a>";
		String expected = content;
		assertFormat(content, expected, settings);
		settings.getPreferences().setQuoteStyle(QuoteStyle.doubleQuotes);
		assertFormat(content, expected, settings);
		assertFormat(expected, expected, settings);
	}

	@Test
	public void testAttributeNameTouchingPreviousValue() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		settings.getFormattingSettings().setSplitAttributes(true);

		String content = "<xml>\r\n" + //
				"  <a zz= tt = \"aa\"aa ></a>\r\n" + //
				"</xml>";
		String expected = "<xml>\r\n" + //
				"  <a\r\n" + //
				"      zz=\r\n" + //
				"      tt='aa'\r\n" + //
				"      aa></a>\r\n" + //
				"</xml>";
		assertFormat(content, expected, settings, //
				te(1, 4, 1, 5, "\r\n      "), //
				te(1, 8, 1, 9, "\r\n      "), //
				te(1, 11, 1, 12, ""), //
				te(1, 13, 1, 14, ""), //
				te(1, 14, 1, 15, "'"), //
				te(1, 17, 1, 18, "'"), //
				te(1, 18, 1, 18, "\r\n      "), //
				te(1, 20, 1, 21, ""));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void enforceSingleQuoteStyle() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);

		String content = "<a  attr   =     \"value\" />";
		String expected = "<a attr=\'value\' />";
		assertFormat(content, expected, settings, //
				te(0, 2, 0, 4, " "), //
				te(0, 8, 0, 11, ""), //
				te(0, 12, 0, 17, ""), //
				te(0, 17, 0, 18, "'"), //
				te(0, 23, 0, 24, "'"));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void enforceDoubleQuoteStyle() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.doubleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);

		String content = "<a  attr   =     \'value\' />";
		String expected = "<a attr=\"value\" />";
		assertFormat(content, expected, settings, //
				te(0, 2, 0, 4, " "), //
				te(0, 8, 0, 11, ""), //
				te(0, 12, 0, 17, ""), //
				te(0, 17, 0, 18, "\""), //
				te(0, 23, 0, 24, "\""));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void enforceSingleQuoteStyleProlog() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);

		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		String expected = "<?xml version=\'1.0\' encoding=\'UTF-8\'?>";
		assertFormat(content, expected, settings, //
				te(0, 14, 0, 15, "'"), //
				te(0, 18, 0, 19, "'"), //
				te(0, 29, 0, 30, "'"), //
				te(0, 35, 0, 36, "'"));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void enforceDoubleQuoteStyleProlog() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.doubleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);

		String content = "<?xml version=\'1.0\' encoding=\'UTF-8\'?>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		assertFormat(content, expected, settings, //
				te(0, 14, 0, 15, "\""), //
				te(0, 18, 0, 19, "\""), //
				te(0, 29, 0, 30, "\""), //
				te(0, 35, 0, 36, "\""));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void enforceDoubleQuoteStyleProlo() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.doubleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);

		String content = "<?xml version= 1.0\' encoding=\'UTF-8?>";
		String expected = "<?xml version= 1.0\" encoding=\" UTF-8?>";
		assertFormat(content, expected, settings, //
				te(0, 18, 0, 19, "\""), //
				te(0, 29, 0, 30, "\""), //
				te(0, 30, 0, 30, " "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void dontEnforceSingleQuoteStyle() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.ignore);

		String content = "<a attr  =   \"\'\" attr2   =     \'\"\' />";
		String expected = "<a attr=\"\'\" attr2=\'\"\' />";
		assertFormat(content, expected, settings, //
				te(0, 7, 0, 9, ""), //
				te(0, 10, 0, 13, ""), //
				te(0, 22, 0, 25, ""), //
				te(0, 26, 0, 31, ""));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void dontEnforceSingleQuoteStyleProlog() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.ignore);

		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		String expected = content;
		assertFormat(content, expected, settings);
		assertFormat(expected, expected, settings);
	}

	@Test
	public void dontEnforceDoubleQuoteStyleProlog() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.ignore);

		String content = "<?xml version=\'1.0\' encoding=\'UTF-8\'?>";
		String expected = content;
		assertFormat(content, expected, settings);
		assertFormat(expected, expected, settings);
	}

	@Test
	public void dontEnforceDoubleQuoteStyle() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.doubleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.ignore);

		String content = "<a attr  =   \"\'\" attr2   =     \'\"\' />";
		String expected = "<a attr=\"\'\" attr2=\'\"\' />";
		assertFormat(content, expected, settings, //
				te(0, 7, 0, 9, ""), //
				te(0, 10, 0, 13, ""), //
				te(0, 22, 0, 25, ""), //
				te(0, 26, 0, 31, ""));
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
