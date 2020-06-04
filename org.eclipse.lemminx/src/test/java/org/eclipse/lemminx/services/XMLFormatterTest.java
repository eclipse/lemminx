/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.services;

import static java.lang.System.lineSeparator;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.commons.TextDocument;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMParser;
import org.eclipse.lemminx.settings.EnforceQuoteStyle;
import org.eclipse.lemminx.settings.QuoteStyle;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lemminx.settings.XMLFormattingOptions.EmptyElements;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * XML formatter services tests
 *
 */
public class XMLFormatterTest {

	@Test
	public void closeStartTagMissing() throws BadLocationException {
		// Don't close tag with bad XML
		String content = "<a";
		String expected = "<a";
		format(content, expected);
	}

	@Test
	public void closeTagMissing() throws BadLocationException {
		// Don't close tag with bad XML
		String content = "<a>";
		String expected = "<a>";
		format(content, expected);
	}

	@Test
	public void autoCloseTag() throws BadLocationException {
		String content = "<a/>";
		String expected = "<a />";
		format(content, expected);
	}

	@Test
	public void selfClosingTag() throws BadLocationException {
		String content = "<a></a>";
		String expected = "<a></a>";
		format(content, expected);
	}

	@Test
	public void singleEndTag() throws BadLocationException {
		String content = "</a>";
		String expected = "</a>";
		format(content, expected);
	}

	@Test
	public void endTagMissing() throws BadLocationException {
		String content = "<foo>\r\n" + //
				"  <bar>\r\n" + //
				"  <toto></toto>\r\n" + //
				"</foo>";
		String expected = "<foo>\r\n" + //
				"  <bar>\r\n" + //
				"    <toto></toto>\r\n" + //
				"</foo>";
		format(content, expected);
	}

	@Test
	public void fullDocument() throws BadLocationException {
		String content = "<div  class = \"foo\">\n" + //
				"<br/>\n" + //
				" </div>";
		String expected = "<div class=\"foo\">\n" + //
				"  <br />\n" + //
				"</div>";
		format(content, expected);
	}

	@Test
	public void range() throws BadLocationException {
		String content = "<div  class = \"foo\">\n" + //
				"  |<img  src = \"foo\"/>|\n" + //
				" </div>";
		String expected = "<div  class = \"foo\">\n" + //
				"  <img src=\"foo\" />\n" + //
				" </div>";
		format(content, expected);
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
		format(content, expected);
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

		format(content, expected);
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

		format(content, expected);
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

		format(content, expected);
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

		format(content, expected);
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

		format(content, expected);
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

		format(content, expected);
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

		format(content, expected);
	}

	@Test
	public void rangeSelectWithinText() throws BadLocationException {
		String content = "<licenses>\n" + //
				"                <name>Lic|en|se</name>\n" + //
				"</licenses>";

		String expected = "<licenses>\n" + //
				"  <name>License</name>\n" + //
				"</licenses>";

		format(content, expected);
	}

	@Test
	public void testProlog() throws BadLocationException {
		String content = "<?xml version=   \"1.0\"       encoding=\"UTF-8\"  ?>\r\n";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		format(content, expected);
	}

	@Test
	public void testProlog2() throws BadLocationException {
		String content = "<?xml version=   \"1.0\"       encoding=\"UTF-8\"  ?><a>bb</a>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lineSeparator() + //
				"<a>bb</a>";
		format(content, expected);
	}

	@Test
	public void testProlog3() throws BadLocationException {
		String content = "<?xml version=   \"1.0\"       encoding=\"UTF-8\"  ?><a><b>c</b></a>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lineSeparator() + //
				"<a>" + lineSeparator() + //
				"  <b>c</b>" + lineSeparator() + //
				"</a>";
		format(content, expected);
	}

	@Test
	public void testProlog4WithUnknownVariable() throws BadLocationException {
		String content = "<?xml version=   \"1.0\"       encoding=\"UTF-8\"  unknown=\"unknownValue\" ?><a><b>c</b></a>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" unknown=\"unknownValue\"?>" + lineSeparator() + //
				"<a>" + lineSeparator() + //
				"  <b>c</b>" + lineSeparator() + //
				"</a>";
		format(content, expected);
	}

	@Test
	public void testPI() throws BadLocationException {
		String content = "<a><?m2e asd as das das ?></a>";
		String expected = "<a>" + lineSeparator() + //
				"  <?m2e asd as das das ?>" + lineSeparator() + //
				"</a>";
		format(content, expected);
	}

	@Test
	public void testPINoContent() throws BadLocationException {
		String content = "<a><?m2e?></a>";
		String expected = "<a>" + lineSeparator() + //
				"  <?m2e ?>" + lineSeparator() + //
				"</a>";
		format(content, expected);
	}

	@Disabled
	@Test
	public void testDefinedPIWithVariables() throws BadLocationException {
		String content = "<a><?xml-stylesheet   href=\"my-style.css\"     type=   \"text/css\"?></a>";
		String expected = "<a>" + lineSeparator() + //
				"  <?xml-stylesheet href=\"my-style.css\" type=\"text/css\" ?>" + lineSeparator() + //
				"</a>";
		format(content, expected);
	}

	@Disabled
	@Test
	public void testDefinedPIWithJustAttributeNames() throws BadLocationException {
		String content = "<a><?xml-stylesheet    href     type  =       attName?></a>";
		String expected = "<a>" + lineSeparator() + //
				"  <?xml-stylesheet href type= attName ?>" + lineSeparator() + //
				"</a>";
		format(content, expected);
	}

	@Test
	public void testPIWithVariables() throws BadLocationException {
		String content = "<a><?xml-styleZZ   href=\"my-style.css\"     type=   \"text/css\"?></a>";
		String expected = "<a>" + lineSeparator() + //
				"  <?xml-styleZZ href=\"my-style.css\"     type=   \"text/css\" ?>" + lineSeparator() + //
				"</a>";
		format(content, expected);
	}

	@Test
	public void testSplitAttributesSingle() throws BadLocationException {
		String content = "<a k1=\"v1\"></a>";
		String expected = "<a k1=\"v1\"></a>";
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		format(content, expected, settings);
	}

	@Test
	public void testSplitAttributes() throws BadLocationException {
		String content = "<a k1=\"v1\" k2=\"v2\"></a>";
		String expected = "<a" + lineSeparator() + "    k1=\"v1\"" + lineSeparator() + //
				"    k2=\"v2\"></a>";
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		format(content, expected, settings);
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
		format(content, expected, settings);
	}

	@Test
	public void testNestedAttributesNoSplit() throws BadLocationException {
		String content = "<a k1=\"v1\" k2=\"v2\"><b aa=\"ok\" bb = \"oo\"></b></a>";
		String expected = "<a k1=\"v1\" k2=\"v2\">" + lineSeparator() + //
				"  <b aa=\"ok\" bb=\"oo\"></b>" + lineSeparator() + //
				"</a>";
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(false);
		format(content, expected, settings);
	}

	@Test
	public void testSplitAttributesProlog() throws BadLocationException {
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		format(content, expected, settings);
	}

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
		format(content, expected, settings);
	}

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
		format(content, expected, settings);
	}

	@Test
	public void testUnclosedEndTagBracketTrailingElement() throws BadLocationException {
		String content = "<root>" + lineSeparator() + //
				"         <a> content </a" + lineSeparator() + //
				"      <b></b>" + lineSeparator() + //
				"</root>";
		String expected = "<root>" + lineSeparator() + //
				"  <a> content </a" + lineSeparator() + //
				"  <b></b>" + lineSeparator() + //
				"</root>";
		format(content, expected);
	}

	@Test
	public void testComment() throws BadLocationException {
		String content = "<!-- CommentText --><a>Val</a>";
		String expected = "<!-- CommentText -->" + lineSeparator() + //
				"<a>Val</a>";
		format(content, expected);
	}

	@Test
	public void testComment2() throws BadLocationException {
		String content = "<!-- CommentText --><!-- Comment2 --><a>Val</a>";
		String expected = "<!-- CommentText -->" + lineSeparator() + //
				"<!-- Comment2 -->" + lineSeparator() + //
				"<a>Val</a>";
		format(content, expected);
	}

	@Test
	public void testCommentNested() throws BadLocationException {
		String content = "<a><!-- CommentText --></a>";
		String expected = "<a>" + lineSeparator() + //
				"  <!-- CommentText -->" + lineSeparator() + //
				"</a>";
		format(content, expected);
	}

	@Test
	public void testCommentNested2() throws BadLocationException {
		String content = "<a><!-- CommentText --><b><!-- Comment2 --></b></a>";
		String expected = "<a>" + lineSeparator() + //
				"  <!-- CommentText -->" + lineSeparator() + //
				"  <b>" + lineSeparator() + //
				"    <!-- Comment2 -->" + lineSeparator() + //
				"  </b>" + lineSeparator() + //
				"</a>";
		format(content, expected);
	}

	@Test
	public void testCommentMultiLineContent() throws BadLocationException {
		String content = "<a><!-- CommentText" + lineSeparator() + //
				"2222" + lineSeparator() + //
				"  3333 --></a>";
		String expected = "<a>" + lineSeparator() + //
				"  <!-- CommentText" + lineSeparator() + //
				"2222" + lineSeparator() + //
				"  3333 -->" + lineSeparator() + //
				"</a>";
		format(content, expected);
	}

	@Test
	public void testJoinCDATALines() throws BadLocationException {
		String content = "<a>" + lineSeparator() + //
				"<![CDATA[" + lineSeparator() + //
				"line 1" + lineSeparator() + //
				"" + lineSeparator() + //
				"" + lineSeparator() + //
				"line 2" + lineSeparator() + //
				"line 3" + lineSeparator() + //
				"]]> </a>";
		String expected = "<a>" + lineSeparator() + //
				"  <![CDATA[line 1 line 2 line 3]]>" + lineSeparator() + //
				"</a>";
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setJoinCDATALines(true);
		format(content, expected, settings);
	}

	@Test
	public void testJoinCommentLines() throws BadLocationException {
		String content = "<!--" + lineSeparator() + //
				" line 1" + lineSeparator() + //
				" " + lineSeparator() + //
				" " + lineSeparator() + //
				"   line 2" + lineSeparator() + //
				" -->";
		String expected = "<!-- line 1 line 2 -->";
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setJoinCommentLines(true);
		format(content, expected, settings);
	}

	@Test
	public void testUnclosedEndTagTrailingComment() throws BadLocationException {
		String content = "<root>" + lineSeparator() + //
				"    <a> content </a" + lineSeparator() + //
				"        <!-- comment -->" + lineSeparator() + //
				" </root>";
		String expected = "<root>" + lineSeparator() + //
				"  <a> content </a" + lineSeparator() + //
				"  <!-- comment -->" + lineSeparator() + //
				"</root>";
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setJoinCommentLines(true);
		format(content, expected, settings);
	}

	@Test
	public void testJoinCommentLinesNested() throws BadLocationException {
		String content = "<a>" + lineSeparator() + //
				"  <!--" + lineSeparator() + //
				"   line 1" + lineSeparator() + //
				"   " + lineSeparator() + //
				"   " + lineSeparator() + //
				"     line 2" + lineSeparator() + //
				"   -->" + lineSeparator() + //
				"</a>";
		String expected = "<a>" + lineSeparator() + //
				"  <!-- line 1 line 2 -->" + lineSeparator() + //
				"</a>";

		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setJoinCommentLines(true);
		format(content, expected, settings);
	}

	@Test
	public void testCommentFormatSameLine() throws BadLocationException {
		String content = "<a>" + lineSeparator() + //
				" Content" + lineSeparator() + //
				"</a> <!-- My   Comment   -->";
		String expected = "<a>" + lineSeparator() + //
				" Content" + lineSeparator() + //
				"</a> <!-- My Comment -->";

		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setJoinCommentLines(true);
		format(content, expected, settings);
	}

	@Test
	public void testElementContentNotNormalized() throws BadLocationException {
		String content = "<a>\r" + //
				" Content\r" + //
				"     Content2\r" + //
				"      Content3\r" + //
				" Content4\r" + //
				"  Content5\r" + //
				"</a>";
		String expected = "<a>\r" + //
				" Content\r" + //
				"     Content2\r" + //
				"      Content3\r" + //
				" Content4\r" + //
				"  Content5\r" + //
				"</a>";

		format(content, expected);
	}

	@Test
	public void testContentFormatting2() throws BadLocationException {
		String content = "<a>\r" + //
				" Content\r" + //
				" <b>\r" + //
				"   Content2\r" + //
				"    Content3\r" + //
				" </b>\r" + //
				"</a>";
		String expected = "<a>\r" + //
				"  Content\r" + //
				"  <b>\r" + //
				"   Content2\r" + //
				"    Content3\r" + //
				" </b>\r" + //
				"</a>";

		format(content, expected);
	}

	@Test
	public void testContentFormattingDontMoveEndTag() throws BadLocationException {
		String content = "<a>\r" + //
				" Content\r" + //
				" <b>\r" + //
				"   Content2\r" + //
				"    Content3 </b>\r" + //
				"</a>";
		String expected = "<a>\r" + //
				"  Content\r" + //
				"  <b>\r" + //
				"   Content2\r" + //
				"    Content3 </b>\r" + //
				"</a>";

		format(content, expected);
	}

	@Test
	public void testContentFormatting3() throws BadLocationException {
		String content = "<a> content </a>";
		String expected = "<a> content </a>";

		format(content, expected);
	}

	@Test
	public void testContentFormatting6() throws BadLocationException {
		String content = "<a>\r" + //
				"\r" + //
				" Content\r" + //
				"</a>";
		String expected = "<a>\r" + //
				"\r" + //
				" Content\r" + //
				"</a>";
		format(content, expected);

		content = "<a>\r\n" + //
				"\r\n" + //
				" Content\r\n" + //
				"</a>";
		expected = "<a>\r\n" + //
				"\r\n" + //
				" Content\r\n" + //
				"</a>";
		format(content, expected);
	}

	@Test
	public void testSelfCloseTagSpace() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSpaceBeforeEmptyCloseTag(true);

		String content = "<a>\r" + //
				" <b/>\r" + //
				"</a>";
		String expected = "<a>\r" + //
				"  <b />\r" + //
				"</a>";
		format(content, expected, settings);
	}

	@Test
	public void testSelfCloseTagAlreadyHasSpace() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSpaceBeforeEmptyCloseTag(true);

		String content = "<a>\r" + //
				" <b />\r" + //
				"</a>";
		String expected = "<a>\r" + //
				"  <b />\r" + //
				"</a>";
		format(content, expected, settings);
	}

	@Test
	public void testSelfCloseTagSpaceFalse() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSpaceBeforeEmptyCloseTag(false);

		String content = "<a>\r" + //
				" <b/>\r" + //
				"</a>";
		String expected = "<a>\r" + //
				"  <b/>\r" + //
				"</a>";
		format(content, expected, settings);
	}

	@Test
	public void testSelfCloseTagSpaceFalseAlreadyHasSpace() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSpaceBeforeEmptyCloseTag(false);

		String content = "<a>\r" + //
				" <b />\r" + //
				"</a>";
		String expected = "<a>\r" + //
				"  <b/>\r" + //
				"</a>";
		format(content, expected, settings);
	}

	@Test
	public void testDontAddClosingBracket() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSpaceBeforeEmptyCloseTag(false);

		String content = "<a>\r" + //
				" <b\r" + //
				"</a>";
		String expected = "<a>\r" + //
				"  <b\r" + //
				"</a>";
		format(content, expected, settings);
	}

	@Test
	public void testEndTagMissingCloseBracket() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSpaceBeforeEmptyCloseTag(false);

		String content = "<a>\r" + //
				" <b> Value </b\r" + //
				"</a>";
		String expected = "<a>\r" + //
				"  <b> Value </b\r" + //
				"</a>";
		format(content, expected, settings);
	}

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
		format(content, expected, settings);
	}

	@Test
	public void testDontPreserveEmptyContentTag() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(false);

		String content = "<a>\r" + //
				"     " + //
				"</a>";
		String expected = "<a></a>";
		format(content, expected, settings);
	}

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
		format(content, expected, settings);
	}

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
		format(content, expected, settings);
	}

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
		format(content, expected, settings);
	}

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
		format(content, expected, settings);
	}

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
		format(content, expected, settings);
	}

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
		format(content, expected, settings);
	}

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
		format(content, expected, settings);
	}

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
		format(content, expected, settings);
	}

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
		format(content, expected, settings);
	}

	@Test
	public void testJoinContentLinesTrue2() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setPreserveEmptyContent(false);
		settings.getFormattingSettings().setJoinContentLines(true);

		String content = "<a>zz zz zz</a>";
		String expected = "<a>zz zz zz</a>";
		format(content, expected, settings);
	}

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
		format(content, expected, settings);
	}

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
		format(content, expected, settings);
	}

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
		format(content, expected, settings);
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
				"    xsi:schemaLocation=\"http://xmlns.jcp.org/xml/ns/javaee \n" + //
				"                http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd\"\n" + //
				"    version=\"3.1\">\n" + //
				"  <servlet>\n" + //
				"    <servlet-name>sssi</servlet-name>\n" + //
				"  </servlet\n" + //
				"</web-app>";
		format(content, expected, settings);
	}

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
		format(content, expected);
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
		format(content, expected, settings);
	}

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
		format(content, expected);
	}

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
		format(content, expected, settings);
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
		format(content, expected);
	}

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
		format(content, expected);
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
		format(content, expected);
	}

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
		format(content, expected);
	}

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
		format(content, expected);
	}

	@Test
	public void testDTDMultiParameterAttlist() throws BadLocationException {
		SharedSettings settings = new SharedSettings();

		String content = "\r\n<!ATTLIST array name CDATA #IMPLIED description CDATA #IMPLIED disabled CDATA #IMPLIED>";
		String expected = "<!ATTLIST array\r\n" + //
				"  name CDATA #IMPLIED\r\n" + //
				"  description CDATA #IMPLIED\r\n" + //
				"  disabled CDATA #IMPLIED\r\n" + //
				">";
		format(content, expected, settings, "test.dtd");
	}

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
		format(content, expected, settings, "test.dtd");
	}

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
		format(content, expected, settings, "test.dtd");
	}

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
		format(content, expected, settings, "test.dtd");
	}

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
		format(content, expected);
	}

	@Test
	public void testDTDElementContentWithAsterisk() throws BadLocationException {
		SharedSettings settings = new SharedSettings();

		String content = "<!ELEMENT data    (#PCDATA | data | d0)*   >";
		String expected = "<!ELEMENT data (#PCDATA | data | d0)*>";
		format(content, expected, settings, "test.dtd", false);
	}

	@Test
	public void testDoctypeSingleLineFormat() throws BadLocationException {
		String content = "<!DOCTYPE name [<!-- MY COMMENT --><!NOTATION postscript SYSTEM \"ghostview\">]>\r\n" + //
				"";
		String expected = "<!DOCTYPE name [\r\n" + //
				"  <!-- MY COMMENT -->\r\n" + //
				"  <!NOTATION postscript SYSTEM \"ghostview\">\r\n" + //
				"]>";
		format(content, expected);
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
		format(content, expected);
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
		format(content, expected);
	}

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
		format(content, expected);
	}

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
		format(content, expected);
	}

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
		format(content, expected);
	}

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
		format(content, expected, settings, "test.dtd");
	}

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
		format(content, expected, settings, "test.dtd");
	}

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
		format(content, expected, settings, "test.dtd");
	}

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
		format(content, expected, settings, "test.dtd");
	}

	@Test
	public void testUseDoubleQuotesFromDoubleQuotes() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		settings.getPreferences().setQuoteStyle(QuoteStyle.doubleQuotes);

		String content = "<a name=  \" value \"> </a>";
		String expected = "<a name=\" value \"></a>";
		format(content, expected, settings);
	}

	@Test
	public void testUseSingleQuotesFromSingleQuotes() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		String content = "<a name=  \' value \'> </a>";
		String expected = "<a name=\' value \'></a>";
		format(content, expected, settings);
	}

	@Test
	public void testUseSingleQuotesFromDoubleQuotes() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);

		String content = "<a name=  \" value \"> </a>";
		String expected = "<a name=\' value \'></a>";
		format(content, expected, settings);
	}

	@Test
	public void testUseDoubleQuotesFromSingleQuotes() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		String content = "<a name=  \' value \'> </a>";
		String expected = "<a name=\" value \"></a>";
		format(content, expected, settings);
	}

	@Test
	public void testUseSingleQuotesNoQuotes() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		String content = "<a name = test> </a>";
		String expected = "<a name= test></a>";
		format(content, expected, settings);
	}

	@Test
	public void testUseSingleQuotesNoQuotesSplit() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setSplitAttributes(true);
		String content = "<a name = test> </a>";
		String expected = "<a" + lineSeparator() + "    name=" + lineSeparator() + "    test></a>";
		format(content, expected, settings);
	}

	@Test
	public void testAttValueOnlyStartQuote() throws BadLocationException {
		String content = "<a name = \"> </a>";
		String expected = "<a name=\"> </a>";
		format(content, expected);
	}

	@Test
	public void testUseDoubleQuotesMultipleAttributes() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		String content = "<a name1=  \" value1 \"  name2= \" value2 \"   name3= \' value3 \' > </a>";
		String expected = "<a name1=\" value1 \" name2=\" value2 \" name3=\" value3 \"></a>";
		format(content, expected, settings);
	}

	@Test
	public void testUseSingleQuotesMultipleAttributes() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		String content = "<a name1=  \" value1 \"  name2= \" value2 \"   name3= \' value3 \' > </a>";
		String expected = "<a name1=\' value1 \' name2=\' value2 \' name3=\' value3 \'></a>";
		format(content, expected, settings);
	}

	@Test
	public void testUseDoubleQuotesMultipleAttributesSplit() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);

		String content = "<a name1=  \" value1 \"  name2= \" value2 \"   name3= \' value3 \' > </a>\n";
		String expected = "<a\n" + "    name1=\" value1 \"\n" + "    name2=\" value2 \"\n"
				+ "    name3=\" value3 \"></a>";
		format(content, expected, settings);
	}

	@Test
	public void testUseSingleQuotesMultipleAttributesSplit() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		String content = "<a name1=  \" value1 \"  name2= \" value2 \"   name3= \' value3 \' > </a>\n";
		String expected = "<a\n" + "    name1=\' value1 \'\n" + "    name2=\' value2 \'\n"
				+ "    name3=\' value3 \'></a>";
		format(content, expected, settings);
	}

	@Test
	public void testUseSingleQuotesLocalDTD() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		String content = "<!DOCTYPE note SYSTEM \"note.dtd\">";
		String expected = "<!DOCTYPE note SYSTEM \'note.dtd\'>";
		format(content, expected, settings);
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
				"\n" + //
				"<root attr=\'hello\'></root>";
		format(content, expected, settings);
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
		format(content, expected, settings, "test.dtd");
	}

	@Test
	public void testDontFormatQuotesByDefault() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		String content = "<a number=\'\"one\"\' /></a>";
		String expected = content;
		format(content, expected, settings);
		settings.getPreferences().setQuoteStyle(QuoteStyle.doubleQuotes);
		format(content, expected, settings);
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
		format(content, expected, settings);
	}

	@Test
	public void testAttributeNameValueTwoLines() throws BadLocationException {
		String content = "<xml>\r\n" + //
				"  <a \r\n" + //
				"   |a             =         \"aa\"|>\r\n" + //
				"    <b></b>\r\n" + //
				"  </a>\r\n" + //
				"</xml>";
		String expected = "<xml>\r\n" + //
				"  <a a=\"aa\">\r\n" + //
				"    <b></b>\r\n" + //
				"  </a>\r\n" + //
				"</xml>";
		format(content, expected);
	}

	@Test
	public void testAttributeNameValueMultipleLines() throws BadLocationException {
		String content = "<xml>\r\n" + //
				"  <a \r\n" + //
				"  |a\r\n" + //
				"  =\r\n" + //
				"  \"aa\"\r\n" + //
				"  \r\n" + //
				"  >|\r\n" + //
				"    <b></b>\r\n" + //
				"  </a>\r\n" + //
				"</xml>";
		String expected = "<xml>\r\n" + //
				"  <a a=\"aa\">\r\n" + //
				"    <b></b>\r\n" + //
				"  </a>\r\n" + //
				"</xml>";
		format(content, expected);
	}

	@Test
	public void testAttributeNameValueMultipleLinesWithChild() throws BadLocationException {
		String content = "<xml>\r\n" + //
				"  <a \r\n" + //
				"   |a          =        \r\n" + //
				"   \r\n" + //
				"   \"aa\">|<b></b>\r\n" + //
				"  </a>\r\n" + //
				"</xml>";
		String expected = "<xml>\r\n" + //
				"  <a a=\"aa\">\r\n" + //
				"    <b></b>\r\n" + //
				"  </a>\r\n" + //
				"</xml>";
		format(content, expected);
	}

	@Test
	public void testAttributeNameValueMultipleLinesWithChildrenSiblings() throws BadLocationException {
		String content = "<xml>\r\n" + //
				"  <a \r\n" + //
				"  |a\r\n" + //
				"  =\r\n" + //
				"  \"aa\"\r\n" + //
				"  \r\n" + //
				"  >\r\n" + //
				"        <b>\r\n" + //
				"          <c></c>\r\n" + //
				"    </b>\r\n" + //
				"  </a>\r\n" + //
				"        <d></d>|\r\n" + //
				"</xml>";
		String expected = "<xml>\r\n" + //
				"  <a a=\"aa\">\r\n" + //
				"    <b>\r\n" + //
				"      <c></c>\r\n" + //
				"    </b>\r\n" + //
				"  </a>\r\n" + //
				"  <d></d>\r\n" + //
				"</xml>";
		format(content, expected);
	}

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
		format(content, expected);
	}

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
		format(content, expected, settings);
	}

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
		format(content, expected);
	}

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
		format(content, expected);
	}

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
		format(content, expected);
	}

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
		format(content, expected);
	}

	@Test
	public void testPreserveNewlines4() throws BadLocationException {
		String content = "<xml>\r\n" + //
				"  <a></a>\r\n" + //
				"</xml>";
		String expected = "<xml>\r\n" + //
				"  <a></a>\r\n" + //
				"</xml>";
		format(content, expected);
	}

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
		format(content, expected);
	}

	@Test
	public void testTrimFinalNewlinesDefault() throws BadLocationException {
		String content = "<a  ></a>\r\n";
		String expected = "<a></a>";
		format(content, expected);
	}

	@Test
	public void testDontInsertFinalNewLine1() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setTrimFinalNewlines(false);
		settings.getFormattingSettings().setInsertFinalNewline(true);
		String content = "";
		format(content, content, settings);
	}

	@Test
	public void testDontInsertFinalNewLine2() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setTrimFinalNewlines(false);
		settings.getFormattingSettings().setInsertFinalNewline(true);
		String content = "<a  ></a>\r\n";
		String expected = "<a></a>\r\n";
		format(content, expected, settings);
	}

	@Test
	public void testDontInsertFinalNewLine3() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setTrimFinalNewlines(false);
		settings.getFormattingSettings().setInsertFinalNewline(true);
		String content = "<a  ></a>\r\n" + "   ";
		String expected = "<a></a>\r\n" + "   ";
		format(content, expected, settings);
	}

	@Test
	public void testInsertFinalNewLine1() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setInsertFinalNewline(true);
		String content = "<a></a>";
		String expected = "<a></a>" + lineSeparator();
		format(content, expected, settings);
	}

	@Test
	public void testInsertFinalNewLine2() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setTrimFinalNewlines(true);
		settings.getFormattingSettings().setInsertFinalNewline(true);
		String content = "<a></a>\r\n\r\n";
		String expected = "<a></a>\r\n";
		format(content, expected, settings);
	}

	@Test
	public void testInsertFinalNewLine3() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setTrimFinalNewlines(true);
		settings.getFormattingSettings().setInsertFinalNewline(true);
		String content = "<a></a>\n\n";
		String expected = "<a></a>\n";
		format(content, expected, settings);
	}

	@Test
	public void testDontInsertFinalNewLineWithRange() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setInsertFinalNewline(true);
		String content = "<div  class = \"foo\">\r\n" + //
				"  |<img  src = \"foo\"|/>\r\n" + //
				" </div>";
		String expected = "<div  class = \"foo\">\r\n" + //
				"  <img src=\"foo\" />\r\n" + //
				" </div>";
		format(content, expected, settings);
	}

	@Test
	public void testInsertFinalNewLineWithRange2() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setInsertFinalNewline(true);
		String content = "<div  class = \"foo\">\r\n" + //
				"  |<img  src = \"foo\"/>\r\n" + //
				" </div>|";
		String expected = "<div  class = \"foo\">\r\n" + //
				"  <img src=\"foo\" />\r\n" + //
				"</div>\r\n";
		format(content, expected, settings);
	}

	@Test
	public void testInsertFinalNewLineWithRange3() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setInsertFinalNewline(true);
		String content = "<div  class = \"foo\">\r\n" + //
				"  |<img  src = \"foo\"/>\r\n" + //
				"\r\n"+ "|" + "\r\n" + //
				"<h1></h1>\r\n" + //
				" </div>";
		String expected = "<div  class = \"foo\">\r\n" + //
				"  <img src=\"foo\" />\r\n" + //
				"\r\n" + //
				"<h1></h1>" + "\r\n" + //
				" </div>";
		format(content, expected, settings);
	}

	@Test
	public void testDontTrimFinalNewLines() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setTrimFinalNewlines(false);
		String content = "<a  ></a>\r\n\r\n\r\n";
		String expected = "<a></a>\r\n\r\n\r\n";
		format(content, expected, settings);
	}

	@Test
	public void testDontTrimFinalNewLines2() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setTrimFinalNewlines(false);
		String content = "<a  ></a>\r\n" + //
				"   \r\n\r\n";
		String expected = "<a></a>\r\n" + //
				"   \r\n\r\n";
		format(content, expected, settings);
	}

	@Test
	public void testDontTrimFinalNewLines3() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setTrimFinalNewlines(false);
		String content = "<a  ></a>\r\n" + //
				"  text \r\n" + //
				"  more text   \r\n" + //
				"   \r\n";
		String expected = "<a></a>\r\n" + //
				"  text \r\n" + //
				"  more text   \r\n" + //
				"   \r\n";
		format(content, expected, settings);
	}

	@Test
	public void testFormatLoneQuoteProlog() throws BadLocationException {
		String content = "<?xml version=\"1.0\" e\'ncoding=\"UTF-8\"?>\n" + //
				"<foo><bar></bar></foo>";
		String expected = "<?xml version=\"1.0\" e\'ncoding=\"UTF-8\"?>\n" + //
		"<foo><bar></bar></foo>?>";
		format(content, expected);
	}

	@Test
	public void testFormatLoneQuoteProlog2() throws BadLocationException {
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\" \"?><foo><bar></bar></foo>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" \"?><foo><bar></bar></foo>?>";
		format(content, expected);
	}

	@Test
	public void testFormatLoneQuoteStartTag() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setTrimFinalNewlines(false);
		String content = "<fo\"o><bar></bar></foo>";
		String expected = "<fo \"o><bar></bar></foo>";
		format(content, expected, settings);
	}

	@Test
	public void testFormatLoneQuoteStartTagWithAttr() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setTrimFinalNewlines(false);
		String content = "<fo\"o attr=\"value\"><bar></bar></foo>";
		String expected = "<fo \"o attr=\" value\"><bar></bar></foo>";
		format(content, expected, settings);
	}

	@Test
	public void testFormatLoneQuoteStartTagWithAttr2() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setTrimFinalNewlines(false);
		String content = "<foo attr=\"value\" \"><bar></bar></foo>";
		String expected = content;
		format(content, expected, settings);
	}

	@Test
	public void testFormatLoneQuoteStartTagWithAttr3() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setTrimFinalNewlines(false);
		String content = "<foo at\"tr=\"value\"><bar></bar></foo>";
		String expected = "<foo at\"tr=\" value\"><bar></bar></foo>";
		format(content, expected, settings);
	}

	@Test
	public void testFormatLoneQuoteStartTagWithAttr4() throws BadLocationException {
		String content = "<foo>\n" + //
				"  <foobar><foobar2></foobar2></foobar>\n" + //
				"  <ba\'r></bar>\n" + //
				"  <foobar><foobar2></foobar2></foobar>\n" + //
				"</foo>";
		String expected = "<foo>\n" + //
				"  <foobar>\n" + //
				"    <foobar2></foobar2>\n" + //
				"  </foobar>\n" + //
				"  <ba \'r></bar>\n" + //
				"  <foobar><foobar2></foobar2></foobar>\n" + //
				"</foo>";
		format(content, expected);
	}


	// ------------ Tests with format empty elements settings

	@Test
	public void expandEmptyElements() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setEmptyElement(EmptyElements.expand);

		String content = "<example att=\"hello\" />";
		String expected = "<example att=\"hello\"></example>";
		format(content, expected, settings);

		content = "<example \r\n" + //
				"  att=\"hello\"\r\n" + //
				"  />";
		format(content, expected, settings);
	}

	@Test
	public void collapseEmptyElements() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setEmptyElement(EmptyElements.collapse);

		String content = "<example att=\"hello\"></example>";
		String expected = "<example att=\"hello\" />";
		format(content, expected, settings);

		content = "<example " + //
				"  att=\"hello\"\r\n" + //
				"  >\r\n" + //
				"</example>";
		format(content, expected, settings);

		content = "<example att=\"hello\">   </example>";
		format(content, expected, settings);

		content = "<example att=\"hello\"> X </example>";
		expected = "<example att=\"hello\"> X </example>";
		format(content, expected, settings);

		content = "<example att=\"hello\"> <X/> </example>";
		expected = "<example att=\"hello\">" + lineSeparator() + //
				"  <X />" + lineSeparator() + //
				"</example>";
		format(content, expected, settings);
	}

	@Test
	public void ignoreEmptyElements() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setEmptyElement(EmptyElements.ignore);

		String content = "<example att=\"hello\"></example>";
		format(content, content, settings);

		content = "<example att=\"hello\" />";
		format(content, content, settings);
	}

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
		format(content, expected, settings);

		content = "<foo>\r\n" + //
				"    <bar></bar>\r\n" + //
				"</foo>";
		expected = "<foo>\r\n" + //
				"  <bar></bar>\r\n" + //
				"</foo>";
		format(content, expected, settings);
	}

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
		format(content, expected, settings);

		content = "<foo>\r\n" + //
				"    <bar></bar>\r\n" + //
				"</foo>";
		expected = "<foo>\r\n" + //
				"  <bar />\r\n" + //
				"</foo>";
		format(content, expected, settings);
	}

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
		format(content, expected, settings);

		// Range covers the b element, collapse is done
		content = "<a>\r\n" + //
				"<|b>\r\n" + //
				"    \r\n" + //
				"</|b>\r\n" + //
				"</a>";
		expected = "<a>\r\n" + //
				"  <b />\r\n" + //
				"</a>";
		format(content, expected, settings);
	}

	@Test
	public void enforceSingleQuoteStyle() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);

		String content = "<a  attr   =     \"value\" />";
		String expected = "<a attr=\'value\' />";
		format(content, expected, settings);
		format(expected, expected, settings);
	}

	@Test
	public void enforceDoubleQuoteStyle() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.doubleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);

		String content = "<a  attr   =     \'value\' />";
		String expected = "<a attr=\"value\" />";
		format(content, expected, settings);
		format(expected, expected, settings);
	}

	@Test
	public void enforceSingleQuoteStyleProlog() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);

		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		String expected = "<?xml version=\'1.0\' encoding=\'UTF-8\'?>";
		format(content, expected, settings);
		format(expected, expected, settings);
	}

	@Test
	public void enforceDoubleQuoteStyleProlog() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.doubleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);

		String content = "<?xml version=\'1.0\' encoding=\'UTF-8\'?>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		format(content, expected, settings);
		format(expected, expected, settings);
	}

	@Test
	public void dontEnforceSingleQuoteStyle() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.ignore);

		String content = "<a attr  =   \"\'\" attr2   =     \'\"\' />";
		String expected = "<a attr=\"\'\" attr2=\'\"\' />";
		format(content, expected, settings);
	}

	@Test
	public void dontEnforceSingleQuoteStyleProlog() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.ignore);

		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		String expected = content;
		format(content, expected, settings);
	}

	@Test
	public void dontEnforceDoubleQuoteStyleProlog() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.ignore);

		String content = "<?xml version=\'1.0\' encoding=\'UTF-8\'?>";
		String expected = content;
		format(content, expected, settings);
	}


	@Test
	public void dontEnforceDoubleQuoteStyle() throws BadLocationException {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.doubleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.ignore);

		String content = "<a attr  =   \"\'\" attr2   =     \'\"\' />";
		String expected = "<a attr=\"\'\" attr2=\'\"\' />";
		format(content, expected, settings);
	}


	@Test
	public void testTemplate() throws BadLocationException {
		String content = "";
		String expected = "";
		format(content, expected);
	}

	// -------------------------Tools-----------------------------------------

	private static void format(String unformatted, String actual) throws BadLocationException {
		format(unformatted, actual, new SharedSettings());
	}

	private static void format(String unformatted, String expected, SharedSettings sharedSettings)
			throws BadLocationException {
		format(unformatted, expected, sharedSettings, "test://test.html");
	}

	private static void format(String unformatted, String expected, SharedSettings sharedSettings, String uri)
			throws BadLocationException {
		format(unformatted, expected, sharedSettings, uri, true);
	}

	private static void format(String unformatted, String expected, SharedSettings sharedSettings, String uri,
			Boolean considerRangeFormat) throws BadLocationException {

		Range range = null;
		int rangeStart = considerRangeFormat ? unformatted.indexOf('|') : -1;
		int rangeEnd = considerRangeFormat ? unformatted.lastIndexOf('|') : -1;
		if (rangeStart != -1 && rangeEnd != -1) {
			// remove '|'
			unformatted = unformatted.substring(0, rangeStart) + unformatted.substring(rangeStart + 1, rangeEnd)
					+ unformatted.substring(rangeEnd + 1);
			DOMDocument unformattedDoc = DOMParser.getInstance().parse(unformatted, uri, null);
			Position startPos = unformattedDoc.positionAt(rangeStart);
			Position endPos = unformattedDoc.positionAt(rangeEnd - 1);
			range = new Range(startPos, endPos);
		}

		TextDocument document = new TextDocument(unformatted, uri);
		XMLLanguageService languageService = new XMLLanguageService();
		List<? extends TextEdit> edits = languageService.format(document, range, sharedSettings);

		String formatted = edits.stream().map(edit -> edit.getNewText()).collect(Collectors.joining(""));

		Range textEditRange = edits.get(0).getRange();
		int textEditStartOffset = document.offsetAt(textEditRange.getStart());
		int textEditEndOffset = document.offsetAt(textEditRange.getEnd()) + 1;

		if (textEditStartOffset != -1 && textEditEndOffset != -1) {
			formatted = unformatted.substring(0, textEditStartOffset) + formatted
					+ unformatted.substring(textEditEndOffset - 1, unformatted.length());
		}

		assertEquals(expected, formatted);
	}
}
