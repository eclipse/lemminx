/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.utils;

import static org.eclipse.lemminx.utils.MarkdownConverter.convert;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * MarkdownConverterTest
 */
public class MarkdownConverterTest {

	@Test
	public void testHTMLConversion() {
		assertEquals("This is `my code`", convert("This is <code>my code</code>"));
		assertEquals("This is  " + System.lineSeparator() + "**bold**", convert("This is<br><b>bold</b>"));
		// This is suboptimal, but this is how `<project>` will need to be added into xml docs
		assertEquals("The `<project>` element is the root of the descriptor.", convert("The &lt;code>&amp;lt;project&amp;gt;&lt;/code> element is the root of the descriptor."));
		assertEquals("# Hey Man #", convert("<h1>Hey Man</h1>"));
		assertEquals("[Placeholder](https://www.xml.com)", convert("<a href=\"https://www.xml.com\">Placeholder</a>"));

		String htmlList =
			"<ul>" + System.lineSeparator() +
			"  <li>Coffee</li>" + System.lineSeparator() +
			"  <li>Tea</li>" + System.lineSeparator() +
			"  <li>Milk</li>" + System.lineSeparator() +
			"</ul>";
		String expectedList =
			" *  Coffee" + System.lineSeparator() +
			" *  Tea" + System.lineSeparator() +
			" *  Milk";
		assertEquals(expectedList, convert(htmlList));
		assertEquals("ONLY_THIS_TEXT", convert("<p>ONLY_THIS_TEXT</p>"));

		String multilineHTML =
			"multi" + System.lineSeparator() +
			"line" + System.lineSeparator() +
			"<code>HTML</code>" + System.lineSeparator() +
			"stuff";
		assertEquals("multi line `HTML` stuff", convert(multilineHTML));

		String multilineHTML2 =
			"<p>multi<p>" + System.lineSeparator() +
			"line" + System.lineSeparator() +
			"<code>HTML</code>" + System.lineSeparator() +
			"stuff";
			String multilineHTML2Expected =
			"multi" + System.lineSeparator() +
			"" + System.lineSeparator() +
			"line `HTML` stuff";
		assertEquals(multilineHTML2Expected, convert(multilineHTML2));
	}

	@Test
	public void testMarkdownConversion() {
		assertEquals("This is `my code`", convert("This is `my code`"));
		assertEquals("The `<thing>` element is the root of the descriptor.", convert("The `<thing>` element is the root of the descriptor."));
		assertEquals("The `<project>` element is the root of the descriptor.", convert("The `&lt;project&gt;` element is the root of the descriptor."));
	}

	@Test
	public void mixedMarkdownAndHTML() {
		assertEquals("*This* `code` is **bold**", convert("<em>This</em> `code` is **bold**"));
		assertEquals( //
				"This doesn't get turned into a code block", //
				convert(
				"    This doesn't" + System.lineSeparator() + //
				"    get turned into" + System.lineSeparator() + //
				"    a code block"));
		assertEquals("My documentation has *not* been misformatted" + System.lineSeparator() + System.lineSeparator() + "It still looks nice :D",
				convert(
				"<p>" + System.lineSeparator() + //
				"    My documentation has *not* been misformatted" + System.lineSeparator() + //
				"</p>" + System.lineSeparator() + //
				"It still looks nice :D"));
		assertEquals("Markdown line breaks..." + System.lineSeparator() + System.lineSeparator() + "...are respected", //
				"Markdown line breaks..." + System.lineSeparator() + //
				System.lineSeparator() + //
				"...are respected");
	}

}