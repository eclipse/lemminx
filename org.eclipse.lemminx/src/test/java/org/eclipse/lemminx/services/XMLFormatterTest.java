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
import org.eclipse.lemminx.settings.XMLFormattingOptions;
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
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setSplitAttributes(true);
		format(content, expected, formattingOptions);
	}

	@Test
	public void testSplitAttributes() throws BadLocationException {
		String content = "<a k1=\"v1\" k2=\"v2\"></a>";
		String expected = "<a" + lineSeparator() + "    k1=\"v1\"" + lineSeparator() + //
				"    k2=\"v2\"></a>";
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setSplitAttributes(true);
		format(content, expected, formattingOptions);
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
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setSplitAttributes(true);
		format(content, expected, formattingOptions);
	}

	@Test
	public void testNestedAttributesNoSplit() throws BadLocationException {
		String content = "<a k1=\"v1\" k2=\"v2\"><b aa=\"ok\" bb = \"oo\"></b></a>";
		String expected = "<a k1=\"v1\" k2=\"v2\">" + lineSeparator() + //
				"  <b aa=\"ok\" bb=\"oo\"></b>" + lineSeparator() + //
				"</a>";
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setSplitAttributes(false);
		format(content, expected, formattingOptions);
	}

	@Test
	public void testSplitAttributesProlog() throws BadLocationException {
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setSplitAttributes(true);
		format(content, expected, formattingOptions);
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

		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setSplitAttributes(true);
		format(content, expected, formattingOptions);
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

		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setSplitAttributes(true);
		format(content, expected, formattingOptions);
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
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setJoinCDATALines(true);
		format(content, expected, formattingOptions);
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
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setJoinCommentLines(true);
		format(content, expected, formattingOptions);
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
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setJoinCommentLines(true);
		format(content, expected, formattingOptions);
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

		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setJoinCommentLines(true);
		format(content, expected, formattingOptions);
	}

	@Test
	public void testCommentFormatSameLine() throws BadLocationException {
		String content = "<a>" + lineSeparator() + //
				" Content" + lineSeparator() + //
				"</a> <!-- My   Comment   -->";
		String expected = "<a>" + lineSeparator() + //
				" Content" + lineSeparator() + //
				"</a> <!-- My Comment -->";

		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setJoinCommentLines(true);
		format(content, expected, formattingOptions);
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

		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		format(content, expected, formattingOptions);
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

		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		format(content, expected, formattingOptions);
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

		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		format(content, expected, formattingOptions);
	}

	@Test
	public void testContentFormatting3() throws BadLocationException {
		String content = "<a> content </a>";
		String expected = "<a> content </a>";

		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		format(content, expected, formattingOptions);
	}

	@Test
	public void testContentFormatting6() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

		String content = "<a>\r" + //
				"\r" + //
				" Content\r" + //
				"</a>";
		String expected = "<a>\r" + //
				"\r" + //
				" Content\r" + //
				"</a>";
		format(content, expected, formattingOptions);

		content = "<a>\r\n" + //
				"\r\n" + //
				" Content\r\n" + //
				"</a>";
		expected = "<a>\r\n" + //
				"\r\n" + //
				" Content\r\n" + //
				"</a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testSelfCloseTagSpace() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setSpaceBeforeEmptyCloseTag(true);

		String content = "<a>\r" + //
				" <b/>\r" + //
				"</a>";
		String expected = "<a>\r" + //
				"  <b />\r" + //
				"</a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testSelfCloseTagAlreadyHasSpace() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setSpaceBeforeEmptyCloseTag(true);

		String content = "<a>\r" + //
				" <b />\r" + //
				"</a>";
		String expected = "<a>\r" + //
				"  <b />\r" + //
				"</a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testSelfCloseTagSpaceFalse() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setSpaceBeforeEmptyCloseTag(false);

		String content = "<a>\r" + //
				" <b/>\r" + //
				"</a>";
		String expected = "<a>\r" + //
				"  <b/>\r" + //
				"</a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testSelfCloseTagSpaceFalseAlreadyHasSpace() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setSpaceBeforeEmptyCloseTag(false);

		String content = "<a>\r" + //
				" <b />\r" + //
				"</a>";
		String expected = "<a>\r" + //
				"  <b/>\r" + //
				"</a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testDontAddClosingBracket() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setSpaceBeforeEmptyCloseTag(false);

		String content = "<a>\r" + //
				" <b\r" + //
				"</a>";
		String expected = "<a>\r" + //
				"  <b\r" + //
				"</a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testEndTagMissingCloseBracket() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setSpaceBeforeEmptyCloseTag(false);

		String content = "<a>\r" + //
				" <b> Value </b\r" + //
				"</a>";
		String expected = "<a>\r" + //
				"  <b> Value </b\r" + //
				"</a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testPreserveEmptyContentTag() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setPreserveEmptyContent(true);

		String content = "<a>\r" + //
				"     " + //
				"</a>";
		String expected = "<a>\r" + //
				"     " + //
				"</a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testDontPreserveEmptyContentTag() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setPreserveEmptyContent(false);

		String content = "<a>\r" + //
				"     " + //
				"</a>";
		String expected = "<a></a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testPreserveTextContent() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setPreserveEmptyContent(true);

		String content = "<a>\r" + //
				"   aaa  " + //
				"</a>";
		String expected = "<a>\r" + //
				"   aaa  " + //
				"</a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testPreserveTextContent2() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setPreserveEmptyContent(false);

		String content = "<a>\r" + //
				"   aaa  " + //
				"</a>";
		String expected = "<a>\r" + //
				"   aaa  " + //
				"</a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testPreserveEmptyContentTagWithSiblings() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setPreserveEmptyContent(true);

		String content = "<a>\r" + //
				"     " + //
				"  <b>  </b>" + //
				"     " + //
				"</a>";
		String expected = "<a>\r" + //
				"  <b>  </b>\r" + //
				"</a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testPreserveEmptyContentTagWithSiblingContent() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setPreserveEmptyContent(true);

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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testDontPreserveEmptyContentTagWithSiblingContent() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setPreserveEmptyContent(false);

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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testPreserveEmptyContentTagWithSiblingWithComment() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setPreserveEmptyContent(true);

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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testDontPreserveEmptyContentTagWithSiblingWithComment() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setPreserveEmptyContent(false);

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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testPreserveEmptyContentWithJoinContentLines() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setPreserveEmptyContent(true);
		formattingOptions.setJoinContentLines(true);

		String content = "<a>\n" + //
				"   zz  \n" + //
				"   zz  \n" + //
				"   <a>  </a>  \n" + //
				"</a>";
		String expected = "<a>\n" + //
				"  zz zz\n" + //
				"  <a>  </a>\n" + //
				"</a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testJoinContentLinesTrue() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setPreserveEmptyContent(false);
		formattingOptions.setJoinContentLines(true);

		String content = "<a>\n" + //
				"   zz  \n" + //
				"   zz  " + //
				"</a>";
		String expected = "<a>zz zz</a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testJoinContentLinesTrue2() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setPreserveEmptyContent(false);
		formattingOptions.setJoinContentLines(true);

		String content = "<a>zz zz zz</a>";
		String expected = "<a>zz zz zz</a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testJoinContentLinesFalse() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setPreserveEmptyContent(false);
		formattingOptions.setJoinContentLines(false);

		String content = "<a>\n" + //
				"   zz  \n" + //
				"   zz  " + //
				"</a>";
		String expected = "<a>\n" + //
				"   zz  \n" + //
				"   zz  " + //
				"</a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testJoinContentLinesWithSiblingElementTrue() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setPreserveEmptyContent(false);
		formattingOptions.setJoinContentLines(true);

		String content = "<a>\n" + //
				"   zz  \n" + //
				"   zz  \n" + //
				"   <a>  </a>  \n" + //
				"</a>";
		String expected = "<a>\n" + //
				"  zz zz\n" + //
				"  <a></a>\n" + //
				"</a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testJoinContentLinesWithSiblingElementFalse() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setPreserveEmptyContent(false);
		formattingOptions.setJoinContentLines(false);

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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testEndTagMissingCloseBracket2() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setSpaceBeforeEmptyCloseTag(false);
		formattingOptions.setSplitAttributes(true);

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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testDoctypeNoInternalSubset() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testDoctypeNoInternalSubsetNoNewlines() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setPreservedNewlines(0);

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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testDoctypeInternalSubset() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testDoctypeInternalSubsetNoNewlines() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setPreservedNewlines(0);

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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testDoctypeInternalDeclSpacesBetweenParameters() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testDoctypeInternalWithAttlist() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testDoctypeInternalAllDecls() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testDoctypeInternalWithComments() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testDoctypeInternalWithText() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testDTDMultiParameterAttlist() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

		String content = "\r\n<!ATTLIST array name CDATA #IMPLIED description CDATA #IMPLIED disabled CDATA #IMPLIED>";
		String expected = "<!ATTLIST array\r\n" + //
				"  name CDATA #IMPLIED\r\n" + //
				"  description CDATA #IMPLIED\r\n" + //
				"  disabled CDATA #IMPLIED\r\n" + //
				">";
		format(content, expected, formattingOptions, "test.dtd");
	}

	@Test
	public void testDTDIndentation() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

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
		format(content, expected, formattingOptions, "test.dtd");
	}

	@Test
	public void testDTDNotEndBrackets() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

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
		format(content, expected, formattingOptions, "test.dtd");
	}

	@Test
	public void testDTDUnknownDeclNameAndText() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

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
		format(content, expected, formattingOptions, "test.dtd");
	}

	@Test
	public void testAllDoctypeParameters() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testDTDElementContentWithAsterisk() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

		String content = "<!ELEMENT data    (#PCDATA | data | d0)*   >";
		String expected = "<!ELEMENT data (#PCDATA | data | d0)*>";
		format(content, expected, formattingOptions, "test.dtd", false);
	}

	@Test
	public void testDoctypeSingleLineFormat() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

		String content = "<!DOCTYPE name [<!-- MY COMMENT --><!NOTATION postscript SYSTEM \"ghostview\">]>\r\n" + //
				"";
		String expected = "<!DOCTYPE name [\r\n" + //
				"  <!-- MY COMMENT -->\r\n" + //
				"  <!NOTATION postscript SYSTEM \"ghostview\">\r\n" + //
				"]>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testDoctypeInvalidParameter() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

		String content = "<!DOCTYPE name \"url\" [\r\n" + //
				"  <!-- MY COMMENT -->\r\n" + //
				"  <!NOTATION postscript SYSTEM \"ghostview\">\r\n" + //
				"]>";
		String expected = "<!DOCTYPE name \"url\" [\r\n" + //
				"  <!-- MY COMMENT -->\r\n" + //
				"  <!NOTATION postscript SYSTEM \"ghostview\">\r\n" + //
				"]>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testDoctypeInvalidParameterUnclosed() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testUnclosedSystemId() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testUnclosedPublicId() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testCommentAfterMissingClosingBracket() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testHTMLDTD() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

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
		format(content, expected, formattingOptions, "test.dtd");
	}

	@Test
	public void testXMLInDTDFile() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

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
		format(content, expected, formattingOptions, "test.dtd");
	}

	@Test
	public void testBadDTDFile() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

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
		format(content, expected, formattingOptions, "test.dtd");
	}

	@Test
	public void testIncompleteAttlistInternalDecl() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

		String content = "<!ATTLIST img src CDATA #REQUIRED %all;\r\n" + //
				">\r\n" + //
				"\r\n" + //
				"<!-- Hypertext anchors. -->";
		String expected = "<!ATTLIST img\r\n" + //
				"  src CDATA #REQUIRED\r\n" + //
				"  %all;\r\n" + //
				">\r\n" + //
				"<!-- Hypertext anchors. -->";
		format(content, expected, formattingOptions, "test.dtd");
	}

	@Test
	public void testUseDoubleQuotesFromDoubleQuotes() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setQuotations(XMLFormattingOptions.DOUBLE_QUOTES_VALUE);

		String content = "<a name=  \" value \"> </a>";
		String expected = "<a name=\" value \"></a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testUseInvalidValueFromDoubleQuotes() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setQuotations("INVALID_VALUE");

		String content = "<a name=  \" value \"> </a>";
		String expected = "<a name=\" value \"></a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testUseSingleQuotesFromSingleQuotes() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setQuotations(XMLFormattingOptions.SINGLE_QUOTES_VALUE);
		String content = "<a name=  \' value \'> </a>";
		String expected = "<a name=\' value \'></a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testUseSingleQuotesFromDoubleQuotes() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setQuotations(XMLFormattingOptions.SINGLE_QUOTES_VALUE);

		String content = "<a name=  \" value \"> </a>";
		String expected = "<a name=\' value \'></a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testUseDoubleQuotesFromSingleQuotes() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

		String content = "<a name=  \' value \'> </a>";
		String expected = "<a name=\" value \"></a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testUseSingleQuotesNoQuotes() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setQuotations(XMLFormattingOptions.SINGLE_QUOTES_VALUE);
		String content = "<a name = test> </a>";
		String expected = "<a name= test></a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testUseSingleQuotesNoQuotesSplit() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setQuotations(XMLFormattingOptions.SINGLE_QUOTES_VALUE);
		formattingOptions.setSplitAttributes(true);
		String content = "<a name = test> </a>";
		String expected = "<a" + lineSeparator() + "    name=" + lineSeparator() + "    test></a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testAttValueOnlyStartQuote() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setQuotations(XMLFormattingOptions.SINGLE_QUOTES_VALUE);
		String content = "<a name = \"> </a>";
		String expected = "<a name=\"> </a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testUseDoubleQuotesMultipleAttributes() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

		String content = "<a name1=  \" value1 \"  name2= \" value2 \"   name3= \' value3 \' > </a>";
		String expected = "<a name1=\" value1 \" name2=\" value2 \" name3=\" value3 \"></a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testUseSingleQuotesMultipleAttributes() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setQuotations(XMLFormattingOptions.SINGLE_QUOTES_VALUE);

		String content = "<a name1=  \" value1 \"  name2= \" value2 \"   name3= \' value3 \' > </a>";
		String expected = "<a name1=\' value1 \' name2=\' value2 \' name3=\' value3 \'></a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testUseDoubleQuotesMultipleAttributesSplit() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setSplitAttributes(true);

		String content = "<a name1=  \" value1 \"  name2= \" value2 \"   name3= \' value3 \' > </a>\n";
		String expected = "<a\n" + "    name1=\" value1 \"\n" + "    name2=\" value2 \"\n"
				+ "    name3=\" value3 \"></a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testUseSingleQuotesMultipleAttributesSplit() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setSplitAttributes(true);
		formattingOptions.setQuotations(XMLFormattingOptions.SINGLE_QUOTES_VALUE);
		String content = "<a name1=  \" value1 \"  name2= \" value2 \"   name3= \' value3 \' > </a>\n";
		String expected = "<a\n" + "    name1=\' value1 \'\n" + "    name2=\' value2 \'\n"
				+ "    name3=\' value3 \'></a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testAttributeNameTouchingPreviousValue() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setQuotations(XMLFormattingOptions.SINGLE_QUOTES_VALUE);
		formattingOptions.setSplitAttributes(true);

		String content = "<xml>\r\n" + //
				"  <a zz= tt = \"aa\"aa ></a>\r\n" + //
				"</xml>";
		String expected = "<xml>\r\n" + //
				"  <a\r\n" + //
				"      zz=\r\n" + //
				"      tt='aa'\r\n" + //
				"      aa></a>\r\n" + //
				"</xml>";
		format(content, expected, formattingOptions);
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
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testPreserveNewlines3Max() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setPreservedNewlines(3);
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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testPreserveNewlines2() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testPreserveNewlinesBothSides() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testPreserveNewlinesBothSidesMultipleTags() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testPreserveNewlinesSingleLine() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		String content = "<xml>\r\n" + //
				"  <a></a>\r\n" + //
				"  \r\n" + //
				"</xml>";
		String expected = "<xml>\r\n" + //
				"  <a></a>\r\n" + //
				"\r\n" + //
				"</xml>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testPreserveNewlines4() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		String content = "<xml>\r\n" + //
				"  <a></a>\r\n" + //
				"</xml>";
		String expected = "<xml>\r\n" + //
				"  <a></a>\r\n" + //
				"</xml>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testNoSpacesOnNewLine() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testTrimFinalNewlinesDefault() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		String content = "<a  ></a>\r\n";
		String expected = "<a></a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testDontInsertFinalNewLine1() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setTrimFinalNewlines(false);
		formattingOptions.setInsertFinalNewline(true);
		String content = "";
		format(content, content, formattingOptions);
	}

	@Test
	public void testDontInsertFinalNewLine2() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setTrimFinalNewlines(false);
		formattingOptions.setInsertFinalNewline(true);
		String content = "<a  ></a>\r\n";
		String expected = "<a></a>\r\n";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testDontInsertFinalNewLine3() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setTrimFinalNewlines(false);
		formattingOptions.setInsertFinalNewline(true);
		String content = "<a  ></a>\r\n" + "   ";
		String expected = "<a></a>\r\n" + "   ";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testInsertFinalNewLine1() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setInsertFinalNewline(true);
		String content = "<a></a>";
		String expected = "<a></a>" + lineSeparator();
		format(content, expected, formattingOptions);
	}

	@Test
	public void testInsertFinalNewLine2() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setTrimFinalNewlines(true);
		formattingOptions.setInsertFinalNewline(true);
		String content = "<a></a>\r\n\r\n";
		String expected = "<a></a>\r\n";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testInsertFinalNewLine3() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setTrimFinalNewlines(true);
		formattingOptions.setInsertFinalNewline(true);
		String content = "<a></a>\n\n";
		String expected = "<a></a>\n";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testDontInsertFinalNewLineWithRange() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setInsertFinalNewline(true);
		String content = "<div  class = \"foo\">\r\n" + //
				"  |<img  src = \"foo\"|/>\r\n" + //
				" </div>";
		String expected = "<div  class = \"foo\">\r\n" + //
				"  <img src=\"foo\" />\r\n" + //
				" </div>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testInsertFinalNewLineWithRange2() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setInsertFinalNewline(true);
		String content = "<div  class = \"foo\">\r\n" + //
				"  |<img  src = \"foo\"/>\r\n" + //
				" </div>|";
		String expected = "<div  class = \"foo\">\r\n" + //
				"  <img src=\"foo\" />\r\n" + //
				"</div>\r\n";
		format(content, expected, formattingOptions);
	}

	// Problem
	@Test
	public void testInsertFinalNewLineWithRange3() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setInsertFinalNewline(true);
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
		format(content, expected, formattingOptions);
	}

	@Test
	public void testDontTrimFinalNewLines() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setTrimFinalNewlines(false);
		String content = "<a  ></a>\r\n\r\n\r\n";
		String expected = "<a></a>\r\n\r\n\r\n";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testDontTrimFinalNewLines2() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setTrimFinalNewlines(false);
		String content = "<a  ></a>\r\n" + //
				"   \r\n\r\n";
		String expected = "<a></a>\r\n" + //
				"   \r\n\r\n";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testDontTrimFinalNewLines3() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setTrimFinalNewlines(false);
		String content = "<a  ></a>\r\n" + //
				"  text \r\n" + //
				"  more text   \r\n" + //
				"   \r\n";
		String expected = "<a></a>\r\n" + //
				"  text \r\n" + //
				"  more text   \r\n" + //
				"   \r\n";
		format(content, expected, formattingOptions);
	}

	// ------------ Tests with format empty elements settings

	@Test
	public void expandEmptyElements() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setEmptyElement(EmptyElements.expand);

		String content = "<example att=\"hello\" />";
		String expected = "<example att=\"hello\"></example>";
		format(content, expected, formattingOptions);

		content = "<example \r\n" + //
				"  att=\"hello\"\r\n" + //
				"  />";
		format(content, expected, formattingOptions);
	}

	@Test
	public void collapseEmptyElements() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setEmptyElement(EmptyElements.collapse);

		String content = "<example att=\"hello\"></example>";
		String expected = "<example att=\"hello\" />";
		format(content, expected, formattingOptions);

		content = "<example " + //
				"  att=\"hello\"\r\n" + //
				"  >\r\n" + //
				"</example>";
		format(content, expected, formattingOptions);

		content = "<example att=\"hello\">   </example>";
		format(content, expected, formattingOptions);

		content = "<example att=\"hello\"> X </example>";
		expected = "<example att=\"hello\"> X </example>";
		format(content, expected, formattingOptions);

		content = "<example att=\"hello\"> <X/> </example>";
		expected = "<example att=\"hello\">" + lineSeparator() + //
				"  <X />" + lineSeparator() + //
				"</example>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void ignoreEmptyElements() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setEmptyElement(EmptyElements.ignore);

		String content = "<example att=\"hello\"></example>";
		format(content, content, formattingOptions);

		content = "<example att=\"hello\" />";
		format(content, content, formattingOptions);
	}

	@Test
	public void expandEmptyElementsAndPreserveEmptyContent() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setEmptyElement(EmptyElements.expand);
		formattingOptions.setPreserveEmptyContent(true);

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
		format(content, expected, formattingOptions);

		content = "<foo>\r\n" + //
				"    <bar></bar>\r\n" + //
				"</foo>";
		expected = "<foo>\r\n" + //
				"  <bar></bar>\r\n" + //
				"</foo>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void collapseEmptyElementsAndPreserveEmptyContent() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setEmptyElement(EmptyElements.collapse);
		formattingOptions.setPreserveEmptyContent(true);

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
		format(content, expected, formattingOptions);

		content = "<foo>\r\n" + //
				"    <bar></bar>\r\n" + //
				"</foo>";
		expected = "<foo>\r\n" + //
				"  <bar />\r\n" + //
				"</foo>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void collapseEmptyElementsInRange() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();
		formattingOptions.setEmptyElement(EmptyElements.collapse);

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
		format(content, expected, formattingOptions);

		// Range covers the b element, collapse is done
		content = "<a>\r\n" + //
				"<|b>\r\n" + //
				"    \r\n" + //
				"</|b>\r\n" + //
				"</a>";
		expected = "<a>\r\n" + //
				"  <b />\r\n" + //
				"</a>";
		format(content, expected, formattingOptions);
	}

	@Test
	public void testTemplate() throws BadLocationException {
		XMLFormattingOptions formattingOptions = createDefaultFormattingOptions();

		String content = "";
		String expected = "";
		format(content, expected, formattingOptions);
	}

	// -------------------------Tools-----------------------------------------

	private static void format(String unformatted, String actual) throws BadLocationException {
		format(unformatted, actual, createDefaultFormattingOptions());
	}

	private static void format(String unformatted, String expected, XMLFormattingOptions formattingOptions)
			throws BadLocationException {
		format(unformatted, expected, formattingOptions, "test://test.html");
	}

	private static void format(String unformatted, String expected, XMLFormattingOptions formattingOptions, String uri)
			throws BadLocationException {
		format(unformatted, expected, formattingOptions, uri, true);
	}

	private static void format(String unformatted, String expected, XMLFormattingOptions formattingOptions, String uri,
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
		List<? extends TextEdit> edits = languageService.format(document, range, formattingOptions);

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

	private static XMLFormattingOptions createDefaultFormattingOptions() {
		return new XMLFormattingOptions(true);
	}
}
