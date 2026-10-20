/**
 *  Copyright (c) 2026 Red Hat Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lemminx.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SnippetsBuilder} snippet syntax generation and escaping.
 */
public class SnippetsBuilderTest {

	// -------------------- tabstops(int) --------------------

	@Test
	public void tabstopZero() {
		assertEquals("$0", SnippetsBuilder.tabstops(0));
	}

	@Test
	public void tabstopOne() {
		assertEquals("$1", SnippetsBuilder.tabstops(1));
	}

	@Test
	public void tabstopLargeIndex() {
		assertEquals("$99", SnippetsBuilder.tabstops(99));
	}

	// -------------------- tabstops(int, StringBuilder) --------------------

	@Test
	public void tabstopAppendToEmpty() {
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.tabstops(1, sb);
		assertEquals("$1", sb.toString());
	}

	@Test
	public void tabstopAppendToExisting() {
		StringBuilder sb = new StringBuilder("<element>");
		SnippetsBuilder.tabstops(0, sb);
		assertEquals("<element>$0", sb.toString());
	}

	@Test
	public void tabstopAppendMultiple() {
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.tabstops(1, sb);
		sb.append(" ");
		SnippetsBuilder.tabstops(2, sb);
		assertEquals("$1 $2", sb.toString());
	}

	// -------------------- placeholders: basic --------------------

	@Test
	public void placeholderBasic() {
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.placeholders(1, "text", sb);
		assertEquals("${1:text}", sb.toString());
	}

	@Test
	public void placeholderIndexZero() {
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.placeholders(0, "final", sb);
		assertEquals("${0:final}", sb.toString());
	}

	@Test
	public void placeholderLargeIndex() {
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.placeholders(42, "value", sb);
		assertEquals("${42:value}", sb.toString());
	}

	@Test
	public void placeholderEmptyText() {
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.placeholders(1, "", sb);
		assertEquals("${1:}", sb.toString());
	}

	@Test
	public void placeholderAppendToExisting() {
		StringBuilder sb = new StringBuilder("<tag>");
		SnippetsBuilder.placeholders(1, "val", sb);
		assertEquals("<tag>${1:val}", sb.toString());
	}

	@Test
	public void placeholderMultiple() {
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.placeholders(1, "first", sb);
		sb.append(" ");
		SnippetsBuilder.placeholders(2, "second", sb);
		assertEquals("${1:first} ${2:second}", sb.toString());
	}

	// -------------------- placeholders: escaping --------------------

	@Test
	public void placeholderEscapesDollar() {
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.placeholders(1, "$100", sb);
		assertEquals("${1:\\$100}", sb.toString());
	}

	@Test
	public void placeholderEscapesDollarAtEnd() {
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.placeholders(1, "price$", sb);
		assertEquals("${1:price\\$}", sb.toString());
	}

	@Test
	public void placeholderEscapesClosingBrace() {
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.placeholders(1, "a}b", sb);
		assertEquals("${1:a\\}b}", sb.toString());
	}

	@Test
	public void placeholderEscapesClosingBraceAtStart() {
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.placeholders(1, "}start", sb);
		assertEquals("${1:\\}start}", sb.toString());
	}

	@Test
	public void placeholderEscapesBackslash() {
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.placeholders(1, "a\\b", sb);
		assertEquals("${1:a\\\\b}", sb.toString());
	}

	@Test
	public void placeholderEscapesBackslashAtStart() {
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.placeholders(1, "\\start", sb);
		assertEquals("${1:\\\\start}", sb.toString());
	}

	@Test
	public void placeholderEscapesBackslashAtEnd() {
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.placeholders(1, "end\\", sb);
		assertEquals("${1:end\\\\}", sb.toString());
	}

	@Test
	public void placeholderEscapesMultipleDollars() {
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.placeholders(1, "$a$b$c", sb);
		assertEquals("${1:\\$a\\$b\\$c}", sb.toString());
	}

	@Test
	public void placeholderEscapesConsecutiveSpecialChars() {
		// \\$ => \\\\\\$
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.placeholders(1, "\\$", sb);
		assertEquals("${1:\\\\\\$}", sb.toString());
	}

	@Test
	public void placeholderEscapesSnippetVariable() {
		// ${name} must not be interpreted as snippet variable
		// Only $ and } are escaped, { is not a special char in placeholders
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.placeholders(1, "${name}", sb);
		assertEquals("${1:\\${name\\}}", sb.toString());
	}

	@Test
	public void placeholderEscapesSnippetTabstop() {
		// $0 must not be interpreted as tabstop
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.placeholders(1, "$0", sb);
		assertEquals("${1:\\$0}", sb.toString());
	}

	@Test
	public void placeholderEscapesAllThreeSpecialChars() {
		// All three special chars: \, $, }
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.placeholders(1, "\\$}", sb);
		assertEquals("${1:\\\\\\$\\}}", sb.toString());
	}

	@Test
	public void placeholderDoesNotEscapePipe() {
		// | is not special inside placeholders
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.placeholders(1, "a|b", sb);
		assertEquals("${1:a|b}", sb.toString());
	}

	@Test
	public void placeholderDoesNotEscapeComma() {
		// , is not special inside placeholders
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.placeholders(1, "a,b", sb);
		assertEquals("${1:a,b}", sb.toString());
	}

	@Test
	public void placeholderDoesNotEscapePipeAndComma() {
		// | and , are not special inside placeholders, only inside choices
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.placeholders(1, "a|b,c", sb);
		assertEquals("${1:a|b,c}", sb.toString());
	}

	@Test
	public void placeholderWithXmlContent() {
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.placeholders(1, "<element attr=\"value\"/>", sb);
		assertEquals("${1:<element attr=\"value\"/>}", sb.toString());
	}

	@Test
	public void placeholderWithNewlines() {
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.placeholders(1, "line1\nline2", sb);
		assertEquals("${1:line1\nline2}", sb.toString());
	}

	@Test
	public void placeholderOnlySpecialChars() {
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.placeholders(1, "$}\\", sb);
		assertEquals("${1:\\$\\}\\\\}", sb.toString());
	}

	// -------------------- choice: basic --------------------

	@Test
	public void choiceThreeValues() {
		assertEquals("${1|one,two,three|}", SnippetsBuilder.choice(1, Arrays.asList("one", "two", "three")));
	}

	@Test
	public void choiceSingleValue() {
		assertEquals("${1|only|}", SnippetsBuilder.choice(1, Collections.singletonList("only")));
	}

	@Test
	public void choiceTwoValues() {
		assertEquals("${1|a,b|}", SnippetsBuilder.choice(1, Arrays.asList("a", "b")));
	}

	@Test
	public void choiceLargeIndex() {
		assertEquals("${10|x,y|}", SnippetsBuilder.choice(10, Arrays.asList("x", "y")));
	}

	@Test
	public void choiceAppendToEmpty() {
		StringBuilder sb = new StringBuilder();
		SnippetsBuilder.choice(1, Arrays.asList("a", "b"), sb);
		assertEquals("${1|a,b|}", sb.toString());
	}

	@Test
	public void choiceAppendToExisting() {
		StringBuilder sb = new StringBuilder("<attr=");
		SnippetsBuilder.choice(2, Arrays.asList("true", "false"), sb);
		assertEquals("<attr=${2|true,false|}", sb.toString());
	}

	// -------------------- choice: escaping --------------------

	@Test
	public void choiceEscapesDollar() {
		assertEquals("${1|\\$100,\\$200|}", SnippetsBuilder.choice(1, Arrays.asList("$100", "$200")));
	}

	@Test
	public void choiceEscapesDollarAtEnd() {
		assertEquals("${1|price\\$|}", SnippetsBuilder.choice(1, Collections.singletonList("price$")));
	}

	@Test
	public void choiceEscapesComma() {
		assertEquals("${1|a\\,b,c|}", SnippetsBuilder.choice(1, Arrays.asList("a,b", "c")));
	}

	@Test
	public void choiceEscapesCommaAtStart() {
		assertEquals("${1|\\,start,end|}", SnippetsBuilder.choice(1, Arrays.asList(",start", "end")));
	}

	@Test
	public void choiceEscapesCommaAtEnd() {
		assertEquals("${1|end\\,,other|}", SnippetsBuilder.choice(1, Arrays.asList("end,", "other")));
	}

	@Test
	public void choiceEscapesPipe() {
		assertEquals("${1|a\\|b,c|}", SnippetsBuilder.choice(1, Arrays.asList("a|b", "c")));
	}

	@Test
	public void choiceEscapesPipeAtStart() {
		assertEquals("${1|\\|start,end|}", SnippetsBuilder.choice(1, Arrays.asList("|start", "end")));
	}

	@Test
	public void choiceEscapesPipeAtEnd() {
		assertEquals("${1|end\\|,other|}", SnippetsBuilder.choice(1, Arrays.asList("end|", "other")));
	}

	@Test
	public void choiceEscapesClosingBrace() {
		assertEquals("${1|a\\}b|}", SnippetsBuilder.choice(1, Collections.singletonList("a}b")));
	}

	@Test
	public void choiceEscapesClosingBraceAtStart() {
		assertEquals("${1|\\}start|}", SnippetsBuilder.choice(1, Collections.singletonList("}start")));
	}

	@Test
	public void choiceEscapesBackslash() {
		assertEquals("${1|a\\\\b|}", SnippetsBuilder.choice(1, Collections.singletonList("a\\b")));
	}

	@Test
	public void choiceEscapesBackslashAtStart() {
		assertEquals("${1|\\\\start|}", SnippetsBuilder.choice(1, Collections.singletonList("\\start")));
	}

	@Test
	public void choiceEscapesBackslashAtEnd() {
		assertEquals("${1|end\\\\|}", SnippetsBuilder.choice(1, Collections.singletonList("end\\")));
	}

	@Test
	public void choiceEscapesConsecutiveBackslashes() {
		assertEquals("${1|a\\\\\\\\b|}", SnippetsBuilder.choice(1, Collections.singletonList("a\\\\b")));
	}

	@Test
	public void choiceEscapesBackslashDollar() {
		// \$ => \\\\\\$
		assertEquals("${1|\\\\\\$|}", SnippetsBuilder.choice(1, Collections.singletonList("\\$")));
	}

	@Test
	public void choiceEscapesAllFiveSpecialChars() {
		// \$}|, => \\\\\\$\\}\\|\\,
		assertEquals("${1|\\\\\\$\\}\\|\\,|}", SnippetsBuilder.choice(1, Collections.singletonList("\\$}|,")));
	}

	@Test
	public void choiceEscapesMultipleSpecialCharsInContext() {
		// "$1|2,3}" should be fully escaped
		assertEquals("${1|\\$1\\|2\\,3\\}|}", SnippetsBuilder.choice(1, Collections.singletonList("$1|2,3}")));
	}

	@Test
	public void choiceEscapesOnlyInValues() {
		// Normal value + value with specials
		assertEquals("${1|normal,with\\$dollar|}", SnippetsBuilder.choice(1, Arrays.asList("normal", "with$dollar")));
	}

	@Test
	public void choiceEscapesInAllValues() {
		// Every value has special chars
		assertEquals("${1|\\$a,b\\|c,d\\,e|}", SnippetsBuilder.choice(1, Arrays.asList("$a", "b|c", "d,e")));
	}

	@Test
	public void choiceOnlySpecialChars() {
		// Input: $ } \ space | ,  — space is not a special char
		assertEquals("${1|\\$\\}\\\\ \\|\\,|}", SnippetsBuilder.choice(1, Collections.singletonList("$}\\ |,")));
	}

	@Test
	public void choiceWithSnippetLikeSyntax() {
		// ${1:text} in a choice value must be escaped
		// Only $ and } are escaped, { is not a special char in choices
		assertEquals("${1|\\${1:text\\}|}", SnippetsBuilder.choice(1, Collections.singletonList("${1:text}")));
	}

	@Test
	public void choiceWithXmlContent() {
		assertEquals("${1|<a>,<b>|}", SnippetsBuilder.choice(1, Arrays.asList("<a>", "<b>")));
	}

	@Test
	public void choiceWithNewlines() {
		assertEquals("${1|line1\nline2|}", SnippetsBuilder.choice(1, Collections.singletonList("line1\nline2")));
	}

	// -------------------- combined: tabstop + placeholder + choice --------------------

	@Test
	public void combinedSnippetWithTabstopAndPlaceholder() {
		// Simulates: <element ${1:attr}="$2">$0</element>
		StringBuilder sb = new StringBuilder("<element ");
		SnippetsBuilder.placeholders(1, "attr", sb);
		sb.append("=\"");
		SnippetsBuilder.tabstops(2, sb);
		sb.append("\">");
		SnippetsBuilder.tabstops(0, sb);
		sb.append("</element>");
		assertEquals("<element ${1:attr}=\"$2\">$0</element>", sb.toString());
	}

	@Test
	public void combinedSnippetWithChoiceAndTabstop() {
		// Simulates: <element type="${1|int,string|}">$0</element>
		StringBuilder sb = new StringBuilder("<element type=\"");
		SnippetsBuilder.choice(1, Arrays.asList("int", "string"), sb);
		sb.append("\">");
		SnippetsBuilder.tabstops(0, sb);
		sb.append("</element>");
		assertEquals("<element type=\"${1|int,string|}\">$0</element>", sb.toString());
	}

	@Test
	public void combinedSnippetWithPlaceholderAndChoice() {
		// Simulates: <${1:element} ${2|required,optional|}>$0</${1:element}>
		StringBuilder sb = new StringBuilder("<");
		SnippetsBuilder.placeholders(1, "element", sb);
		sb.append(" ");
		SnippetsBuilder.choice(2, Arrays.asList("required", "optional"), sb);
		sb.append(">");
		SnippetsBuilder.tabstops(0, sb);
		sb.append("</");
		SnippetsBuilder.placeholders(1, "element", sb);
		sb.append(">");
		assertEquals("<${1:element} ${2|required,optional|}>$0</${1:element}>", sb.toString());
	}
}
