/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4xml.utils;

import static org.eclipse.lsp4xml.utils.MarkdownConverter.convert;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * MarkdownConverterTest
 */
public class MarkdownConverterTest {

	@Test
	public void testHTMLConversion() {
		assertEquals("This is `my code`", convert("This is <code>my code</code>"));
		assertEquals("This is\n**bold**", convert("This is<br><b>bold</b>"));
		assertEquals("The `<project>` element is the root of the descriptor.", convert("The <code>&lt;project&gt;</code> element is the root of the descriptor."));
		assertEquals("# Hey Man #", convert("<h1>Hey Man</h1>"));
		assertEquals("[Placeholder](https://www.xml.com)", convert("<a href=\"https://www.xml.com\">Placeholder</a>"));

		String htmlList =
			"<ul>\n" +
			"  <li>Coffee</li>\n" +
			"  <li>Tea</li>\n" +
			"  <li>Milk</li>\n" +
			"</ul>";
		String expectedList = 
			" *  Coffee\n" +
			" *  Tea\n" +
			" *  Milk";
		assertEquals(expectedList, convert(htmlList));
		assertEquals("ONLY_THIS_TEXT", convert("<p>ONLY_THIS_TEXT</p>"));

		String multilineHTML = 
			"multi\n" +
			"line\n" +
			"<code>HTML</code>\n" +
			"stuff";
		assertEquals("multi line `HTML` stuff", convert(multilineHTML));

		String multilineHTML2 = 
			"<p>multi<p>\n" +
			"line\n" +
			"<code>HTML</code>\n" +
			"stuff";
			String multilineHTML2Expected = 
			"multi\n" +
			"\n" +
			"line `HTML` stuff";
		assertEquals(multilineHTML2Expected, convert(multilineHTML2));
	}
	
	@Test
	public void testMarkdownConversion() {
		assertEquals("This is `my code`", convert("This is `my code`"));
		assertEquals("The `<thing>` element is the root of the descriptor.", convert("The `<thing>` element is the root of the descriptor."));
		assertEquals("The `<project>` element is the root of the descriptor.", convert("The `&lt;project&gt;` element is the root of the descriptor."));
	}

}