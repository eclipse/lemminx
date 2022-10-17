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
import org.junit.jupiter.api.Test;

/**
 * XML experimental formatter services tests with max line width.
 *
 */
public class XMLFormatterMaxLineWithTest extends AbstractCacheBasedTest {

	@Test
	public void splitText() throws BadLocationException {
		String content = "<a>abcde fghi</a>";
		String expected = "<a>abcde" + //
				System.lineSeparator() + //
				"  fghi</a>";
		assertFormat(content, expected, 6, //
				te(0, 8, 0, 9, System.lineSeparator() + "  "));
		assertFormat(expected, expected, 6);
	}

	@Test
	public void splitTextWithSpace() throws BadLocationException {
		String content = "<a> abcde fghi</a>";
		String expected = "<a>" + //
				System.lineSeparator() + //
				"  abcde" + //
				System.lineSeparator() + //
				"  fghi</a>";
		assertFormat(content, expected, 6, //
				te(0, 3, 0, 4, System.lineSeparator() + "  "),
				te(0, 9, 0, 10, System.lineSeparator() + "  "));
		assertFormat(expected, expected, 6);
	}

	@Test
	public void splitMixedText() throws BadLocationException {
		String content = "<a><b /> efgh</a>";
		String expected = "<a><b />" + //
				System.lineSeparator() + //
				"  efgh</a>";
		assertFormat(content, expected, 5, //
				te(0, 8, 0, 9, System.lineSeparator() + "  "));
		assertFormat(expected, expected, 5);
	}

	@Test
	public void noSplit() throws BadLocationException {
		String content = "<a>abcde fghi</a>";
		String expected = content;
		assertFormat(content, expected, 20);
	}

	@Test
	public void longText() throws BadLocationException {
		String content = "<foo>\r\n" + //
				"	<para>    \r\n" + //
				"		vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv\r\n"
				+ //
				"	</para>\r\n" + //
				"</foo>";
		String expected = "<foo>\r\n" + //
				"  <para>\r\n" + //
				"    vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv </para>\r\n"
				+ //
				"</foo>";
		assertFormat(content, expected, 20, //
				te(0, 5, 1, 1, "\r\n  "), //
				te(1, 7, 2, 2, "\r\n    "), //
				te(2, 102, 3, 1, " "));
		assertFormat(expected, expected, 20);
	}

	@Test
	public void complex() throws BadLocationException {
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<ip_log  version=\"1.0\">\r\n" + //
				"  <project id=\"org.apache.ant\" version=\"1.10.12\" status=\"done\">\r\n" + //
				"    <info>\r\n" + //
				"      <name>Apache            Ant (all-in-one) ffffffffffffffffff        fffffffffffffffffffffffff    ggggggggggggggg</name>\r\n"
				+ //
				"      <repository>scm:git:git.eclipse.org:/gitroot/orbit/recipes.git</repository>\r\n" + //
				"      <location>apache-parent/ant/org.apache.ant</location>\r\n" + //
				"    </info>\r\n" + //
				"    <contact>\r\n" + //
				"      <name>Sarika          \r\n" + //
				"        Sinha</name>\r\n" + //
				"      <email>sarika.\r\n" + //
				"        \r\n" + //
				"        \r\n" + //
				"        sinha@in.ibm.com</email>\r\n" + //
				"      <company>IBM</company>\r\n" + //
				"    </contact>\r\n" + //
				"  </project>\r\n" + //
				"</ip_log>";

		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<ip_log version=\"1.0\">\r\n" + //
				"  <project id=\"org.apache.ant\" version=\"1.10.12\" status=\"done\">\r\n" + //
				"    <info>\r\n" + //
				"      <name>Apache Ant (all-in-one) ffffffffffffffffff fffffffffffffffffffffffff\r\n" + //
				"        ggggggggggggggg</name>\r\n" + //
				"      <repository>scm:git:git.eclipse.org:/gitroot/orbit/recipes.git</repository>\r\n" + //
				"      <location>apache-parent/ant/org.apache.ant</location>\r\n" + //
				"    </info>\r\n" + //
				"    <contact>\r\n" + //
				"      <name>Sarika Sinha</name>\r\n" + //
				"      <email>sarika. sinha@in.ibm.com</email>\r\n" + //
				"      <company>IBM</company>\r\n" + //
				"    </contact>\r\n" + //
				"  </project>\r\n" + //
				"</ip_log>";

		assertFormat(content, expected, 80, //
				te(1, 7, 1, 9, " "), //
				te(4, 18, 4, 30, " "), //
				te(4, 65, 4, 73, " "), //
				te(4, 98, 4, 102, "\r\n        "), //
				te(9, 18, 10, 8, " "), //
				te(11, 20, 14, 8, " "));
		assertFormat(expected, expected, 80);
	}

	// https://github.com/eclipse/lemminx/issues/594
	@Test
	public void mixedText() throws BadLocationException {
		String content = "<para>All DocBook V5.0 elements are in the namespace <uri>http://docbook.org/ns/docbook</uri>. <acronym>XML <alt>Extensible Markup\r\n"
				+ //
				"  Language</alt></acronym> namespaces are used to distinguish between different element sets. In the last few years, almost all new\r\n"
				+ //
				"  XML grammars have used their own namespace. It is easy to create compound documents that contain elements from different XML\r\n"
				+ //
				"  vocabularies. DocBook V5.0 is\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"  <emphasis>following</emphasis> this\r\n" + //
				"  <emphasis>design</emphasis>/<emphasis>rule</emphasis>.\r\n" + //
				"\r\n" + //
				"  Using\r\n" + //
				"  namespaces in your documents is very easy. Consider this simple article marked up in DocBook V4.5:</para>";
		String expected = "<para>All DocBook V5.0 elements are in the namespace <uri>http://docbook.org/ns/docbook</uri>. <acronym>XML <alt>Extensible Markup\r\n"
				+ //
				"  Language</alt></acronym> namespaces are used to distinguish between different element sets. In the last few years, almost all\r\n"
				+ //
				"  new XML grammars have used their own namespace. It is easy to create compound documents that contain elements from different XML\r\n"
				+ //
				"  vocabularies. DocBook V5.0 is <emphasis>following</emphasis> this <emphasis>design</emphasis>/<emphasis>rule</emphasis>. Using\r\n"
				+ //
				"  namespaces in your documents is very easy. Consider this simple article marked up in DocBook V4.5:</para>";
		assertFormat(content, expected, 130, //
				te(1, 127, 1, 128, "\r\n  "), //
				te(1, 131, 2, 2, " "), //
				te(3, 31, 6, 2, " "), //
				te(6, 37, 7, 2, " "), //
				te(7, 56, 9, 2, " "));
		assertFormat(expected, expected, 130);
	}

	@Test
	public void mixedTextDefaultLineWidth() throws BadLocationException {
		String content = "<para>All DocBook V5.0 elements are in the namespace <uri>http://docbook.org/ns/docbook</uri>. <acronym>XML <alt>Extensible Markup\r\n"
				+ //
				"  Language</alt></acronym> namespaces are used to distinguish between different element sets. In the last few years, almost all new\r\n"
				+ //
				"  XML grammars have used their own namespace. It is easy to create compound documents that contain elements from different XML\r\n"
				+ //
				"  vocabularies. DocBook V5.0 is\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"  <emphasis>following</emphasis> this\r\n" + //
				"  <emphasis>design</emphasis>/<emphasis>rule</emphasis>.\r\n" + //
				"\r\n" + //
				"  Using\r\n" + //
				"  namespaces in your documents is very easy. Consider this simple article marked up in DocBook V4.5:</para>";
		String expected = "<para>All DocBook V5.0 elements are in the namespace <uri>http://docbook.org/ns/docbook</uri>. <acronym>XML <alt>Extensible\r\n"
				+ //
				"  Markup Language</alt></acronym> namespaces are used to distinguish between\r\n" + //
				"  different element sets. In the last few years, almost all new XML grammars\r\n" + //
				"  have used their own namespace. It is easy to create compound documents that\r\n" + //
				"  contain elements from different XML vocabularies. DocBook V5.0 is <emphasis>following</emphasis>\r\n" + //
				"  this <emphasis>design</emphasis>/<emphasis>rule</emphasis>. Using namespaces\r\n" + //
				"  in your documents is very easy. Consider this simple article marked up in\r\n" + //
				"  DocBook V4.5:</para>";
		assertFormat(content, expected, 80, //
				te(0, 123, 0, 124, "\r\n  "), //
				te(0, 130, 1, 2, " "), //
				te(1, 69, 1, 70, "\r\n  "), //
				te(1, 131, 2, 2, " "), //
				te(2, 14, 2, 15, "\r\n  "),
				te(2, 90, 2, 91, "\r\n  "), //
				te(2, 126, 3, 2, " "), //
				te(3, 31, 6, 2, " "), //
				te(6, 32, 6, 33, "\r\n  "), //
				te(6, 37, 7, 2, " "),
				te(7, 56, 9, 2, " "), //
				te(9, 7, 10, 2, " "), //
				te(10, 12, 10, 13, "\r\n  "), //
				te(10, 86, 10, 87, "\r\n  "));
		assertFormat(expected, expected, 80);
	}

	// https://github.com/eclipse/lemminx/issues/594
	@Test
	public void mixedTextIsChild() throws BadLocationException {
		String content = "<parent>\r\n" + //
				"<para>All DocBook V5.0 elements are in the namespace <uri>http://docbook.org/ns/docbook</uri>. <acronym>XML <alt>Extensible Markup\r\n"
				+ //
				"  Language</alt></acronym> namespaces are used to distinguish between different element sets. In the last few years, almost all new\r\n"
				+ //
				"  XML grammars have used their own namespace. It is easy to create compound documents that contain elements from different XML\r\n"
				+ //
				"  vocabularies. DocBook V5.0 is\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"  <emphasis>following</emphasis> this\r\n" + //
				"  <emphasis>design</emphasis>/<emphasis>rule</emphasis>.\r\n" + //
				"\r\n" + //
				"  Using\r\n" + //
				"  namespaces in your documents is very easy. Consider this simple article marked up in DocBook V4.5:</para>"
				+ //
				"</parent> <para>All DocBook V5.0 elements are in the namespace <uri>http://docbook.org/ns/docbook</uri>. <acronym>XML <alt>Extensible Markup\r\n"
				+ //
				"  Language</alt></acronym> namespaces are used to distinguish between different element sets. In the last few years, almost all\r\n"
				+ //
				"  new XML grammars have used their own namespace. It is easy to create compound documents that contain elements from different XML\r\n"
				+ //
				"  vocabularies. DocBook V5.0 is <emphasis>following</emphasis> this <emphasis>design</emphasis>/<emphasis>rule</emphasis>. Using\r\n"
				+ //
				"  namespaces in your documents is very easy. Consider this simple article marked up in DocBook V4.5:</para>";
		String expected = "<parent>\r\n" + //
				"  <para>All DocBook V5.0 elements are in the namespace <uri>http://docbook.org/ns/docbook</uri>. <acronym>XML <alt>Extensible\r\n"
				+ //
				"    Markup Language</alt></acronym> namespaces are used to distinguish between different element sets. In the last few years,\r\n"
				+ //
				"    almost all new XML grammars have used their own namespace. It is easy to create compound documents that contain elements from\r\n"
				+ //
				"    different XML vocabularies. DocBook V5.0 is <emphasis>following</emphasis> this <emphasis>design</emphasis>/<emphasis>rule</emphasis>.\r\n"
				+ //
				"    Using namespaces in your documents is very easy. Consider this simple article marked up in DocBook V4.5:</para>\r\n"
				+ //
				"</parent>\r\n" + //
				"<para>All DocBook V5.0 elements are in the namespace <uri>http://docbook.org/ns/docbook</uri>. <acronym>XML <alt>Extensible Markup\r\n"
				+ //
				"  Language</alt></acronym> namespaces are used to distinguish between different element sets. In the last few years, almost all\r\n"
				+ //
				"  new XML grammars have used their own namespace. It is easy to create compound documents that contain elements from different XML\r\n"
				+ //
				"  vocabularies. DocBook V5.0 is <emphasis>following</emphasis> this <emphasis>design</emphasis>/<emphasis>rule</emphasis>. Using\r\n"
				+ //
				"  namespaces in your documents is very easy. Consider this simple article marked up in DocBook V4.5:</para>";
		assertFormat(content, expected, 130, //
				te(0, 8, 1, 0, "\r\n  "), //
				te(1, 123, 1, 124, "\r\n    "), //
				te(1, 130, 2, 2, " "), //
				te(2, 116, 2, 117, "\r\n    "), //
				te(2, 131, 3, 2, " "),
				te(3, 112, 3, 113, "\r\n    "), //
				te(3, 126, 4, 2, " "), //
				te(4, 31, 7, 2, " "), //
				te(7, 37, 8, 2, " "), //
				te(8, 56, 10, 2, "\r\n    "),
				te(10, 7, 11, 2, " "), //
				te(11, 107, 11, 107, "\r\n"), //
				te(11, 116, 11, 117, "\r\n"));
		assertFormat(expected, expected, 130);
	}

	// https://github.com/eclipse/lemminx/issues/594
	@Test
	public void mixedTextNoJoinContentLines() throws BadLocationException {
		String content = "<para>All DocBook V5.0 elements are in the namespace <uri>http://docbook.org/ns/docbook</uri>. <acronym>XML <alt>Extensible Markup\r\n"
				+ //
				"  Language</alt></acronym> namespaces are used to distinguish between different element sets. In the last few years, almost all new\r\n"
				+ //
				"  XML grammars have used their own namespace. It is easy to create compound documents that contain elements from different XML\r\n"
				+ //
				"  vocabularies. DocBook V5.0 is\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"  <emphasis>following</emphasis> this\r\n" + //
				"  <emphasis>design</emphasis>/<emphasis>rule</emphasis>.\r\n" + //
				"\r\n" + //
				"  Using\r\n" + //
				"  namespaces in your documents is very easy. Consider this simple article marked up in DocBook V4.5:</para>";
		String expected = "<para>All DocBook V5.0 elements are in the namespace <uri>http://docbook.org/ns/docbook</uri>. <acronym>XML <alt>Extensible Markup\r\n"
				+ //
				"  Language</alt></acronym> namespaces are used to distinguish between different element sets. In the last few years, almost all\r\n"
				+ //
				"  new\r\n" + //
				"  XML grammars have used their own namespace. It is easy to create compound documents that contain elements from different XML\r\n"
				+ //
				"  vocabularies. DocBook V5.0 is\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"  <emphasis>following</emphasis> this\r\n" + //
				"  <emphasis>design</emphasis>/<emphasis>rule</emphasis>.\r\n" + //
				"\r\n" + //
				"  Using\r\n" + //
				"  namespaces in your documents is very easy. Consider this simple article marked up in DocBook V4.5:</para>";
		SharedSettings settings = new SharedSettings();

		assertFormat(content, expected, 130, settings, //
				te(1, 127, 1, 128, "\r\n  "));
		assertFormat(expected, expected, 130, settings);
	}

	private static void assertFormat(String unformatted, String expected, int maxLineWidth,
			SharedSettings sharedSettings, TextEdit... expectedEdits)
			throws BadLocationException {
		sharedSettings.getFormattingSettings().setMaxLineWidth(maxLineWidth);
		// Force to "experimental" formatter
		sharedSettings.getFormattingSettings().setExperimental(true);
		XMLAssert.assertFormat(null, unformatted, expected, sharedSettings, "test.xml", Boolean.FALSE, expectedEdits);
	}

	private static void assertFormat(String unformatted, String expected, int maxLineWidth, TextEdit... expectedEdits)
			throws BadLocationException {
		SharedSettings sharedSettings = new SharedSettings();
		sharedSettings.getFormattingSettings().setMaxLineWidth(maxLineWidth);
		sharedSettings.getFormattingSettings().setJoinContentLines(true);
		// Force to "experimental" formatter
		sharedSettings.getFormattingSettings().setExperimental(true);
		XMLAssert.assertFormat(null, unformatted, expected, sharedSettings, "test.xml", Boolean.FALSE, expectedEdits);
	}
}
