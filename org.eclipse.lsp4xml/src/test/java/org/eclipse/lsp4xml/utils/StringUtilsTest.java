/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.utils;

import static org.eclipse.lsp4xml.utils.StringUtils.trimNewLines;
import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

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
		assertEquals(4, StringUtils.getOffsetAfterWhitespace("abc 123", 7));
		assertEquals(4, StringUtils.getOffsetAfterWhitespace("abc 123", 6));
		assertEquals(-1, StringUtils.getOffsetAfterWhitespace("abc 123", 4));
		assertEquals(0, StringUtils.getOffsetAfterWhitespace("123", 3));
		assertEquals(-1, StringUtils.getOffsetAfterWhitespace("123", 0));
	}

	private static void assertTrimNewLines(String valueToTrim, String expected) {
		String actual = trimNewLines(valueToTrim);
		Assert.assertEquals(expected, actual);
	}
}
