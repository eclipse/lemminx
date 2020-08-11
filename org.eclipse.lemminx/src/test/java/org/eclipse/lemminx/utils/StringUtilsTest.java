/**
 *  Copyright (c) 2018 Angelo ZERR
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
package org.eclipse.lemminx.utils;

import static org.eclipse.lemminx.utils.StringUtils.getOffsetAfterWhitespace;
import static org.eclipse.lemminx.utils.StringUtils.isTagOutsideOfBackticks;
import static org.eclipse.lemminx.utils.StringUtils.trimNewLines;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * {@link StringUtils} tests.
 *
 */
public class StringUtilsTest {

	@Test
	public void testTrimOfOneNewLine() {
		assertTrimNewLines("\r", "");
		assertTrimNewLines("\n", "");
		assertTrimNewLines("\r\n", "");
	}

	@Test
	public void testNoTrim() {
		assertTrimNewLines(" ", " ");
		assertTrimNewLines("  abcd  ", "  abcd  ");
		assertTrimNewLines("  a\rbcd  ", "  a\rbcd  ");
		assertTrimNewLines("  a\nbcd  ", "  a\nbcd  ");
		assertTrimNewLines("  a\r\nbcd  ", "  a\r\nbcd  ");
	}

	@Test
	public void testTrimOfSpace() {
		assertTrimNewLines(" ", " ");
		assertTrimNewLines("\r \r", " ");
		assertTrimNewLines("\r\n \r\n", " ");
		assertTrimNewLines(" \n \n ", " ");
		assertTrimNewLines("      \r\n \r\n       ", " ");
	}

	@Test
	public void testTrimWhichContainsNewLine() {
		assertTrimNewLines(" \r abc\rdef", " abc\rdef");
		assertTrimNewLines("   abc\rdef", "   abc\rdef");
	}

	@Test
	public void testGetOffsetAfterWhitespace() {
		assertEquals(4, getOffsetAfterWhitespace("abc 123", 7));
		assertEquals(4, getOffsetAfterWhitespace("abc 123", 6));
		assertEquals(-1, getOffsetAfterWhitespace("abc 123", 4));
		assertEquals(0, getOffsetAfterWhitespace("123", 3));
		assertEquals(-1, getOffsetAfterWhitespace("123", 0));
	}

	@Test
	public void testIsTagOutsideOfBackTicks() {
		assertFalse(isTagOutsideOfBackticks("abc `<def></def>`"));
		assertFalse(isTagOutsideOfBackticks("abc `<def>` abc"));
		assertFalse(isTagOutsideOfBackticks("asd `<abc></abc>"));
		assertFalse(isTagOutsideOfBackticks("`<hij></hij>`"));
		assertFalse(isTagOutsideOfBackticks(""));
		assertFalse(isTagOutsideOfBackticks("A"));

		assertTrue(isTagOutsideOfBackticks("<A></A>"));
		assertTrue(isTagOutsideOfBackticks("test `<a></a>` <A></A>"));
		assertTrue(isTagOutsideOfBackticks("<A></A> `<a></a>`"));
		assertTrue(isTagOutsideOfBackticks("<A> `<a></a>`"));
	}

	@Test
	public void testGetString() {
		StringBuilder buffer = new StringBuilder();
		String bufferText = "This is buffer text";
		buffer.append(bufferText);
		assertEquals(bufferText, StringUtils.getString(buffer));

		String regularText = "This is regular text";
		assertEquals(regularText, StringUtils.getString(regularText));
	}

	private static void assertTrimNewLines(String valueToTrim, String expected) {
		String actual = trimNewLines(valueToTrim);
		assertEquals(expected, actual);
	}
}
