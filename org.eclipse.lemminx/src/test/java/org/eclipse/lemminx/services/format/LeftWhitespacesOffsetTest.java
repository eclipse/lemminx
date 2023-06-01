/*******************************************************************************
* Copyright (c) 2022, 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services.format;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.utils.TextEditUtils;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link TextEditUtils#adjustOffsetWithLeftWhitespaces(int, int, String)}.
 *
 * @author Angelo ZERR
 *
 */
public class LeftWhitespacesOffsetTest extends AbstractCacheBasedTest {

	@Test
	public void spaces() {
		assertLeftWhitespacesOffset("<a|    |>", "<a|    >");
		assertLeftWhitespacesOffset("<a |    |>", "<a |    >");
		assertLeftWhitespacesOffset("<a |b    |>", "<a b|    >");
		assertLeftWhitespacesOffset("|<a b    |>", "<a b|    >");
	}

	@Test
	public void lineSeparator() {
		assertLeftWhitespacesOffset("<a|\r\n    |>", "<a|\r\n    >");
		assertLeftWhitespacesOffset("<a\r\n |    |>", "<a\r\n |    >");
		assertLeftWhitespacesOffset("<a\r\n |b\r\n    |>", "<a\r\n b|\r\n    >");
		assertLeftWhitespacesOffset("|<a\r\n b\r\n    |>", "<a\r\n b|\r\n    >");
	}

	@Test
	public void nullOffset() {
		assertLeftWhitespacesOffset("||<a >", "<a >");
	}

	@Test
	public void noSpaces() {
		assertLeftWhitespacesOffset("<a|bcd|>", "<abcd|>");
	}
	
	@Test
	public void getLeftWhitespacesOffsetWithToZero() {
		int offset = TextEditUtils.adjustOffsetWithLeftWhitespaces(0, 0, "<a>");
		assertEquals(-1, offset);
		offset = TextEditUtils.adjustOffsetWithLeftWhitespaces(1, 0, "<a    >");
		assertEquals(-1, offset);
	}

	@Test
	public void getLeftWhitespacesOffsetWith() {
		int offset = TextEditUtils.adjustOffsetWithLeftWhitespaces(0, 0, "<a    >");
		assertEquals(-1, offset);

		offset = TextEditUtils.adjustOffsetWithLeftWhitespaces(1, 0, "<a    >");
		assertEquals(-1, offset);
	}

	private static void assertLeftWhitespacesOffset(String textWithRanges, String expectedWithOffset) {
		int leftLimit = textWithRanges.indexOf('|');
		int to = textWithRanges.indexOf('|', leftLimit + 1) - 1;
		StringBuilder text = new StringBuilder();
		text.append(textWithRanges.substring(0, leftLimit));
		text.append(textWithRanges.substring(leftLimit + 1, to + 1));
		text.append(textWithRanges.substring(to + 2, textWithRanges.length()));

		int offset = TextEditUtils.adjustOffsetWithLeftWhitespaces(leftLimit, to, text.toString());
		if (offset != -1) {
			text.insert(offset, '|');
		}
		assertEquals(expectedWithOffset, text.toString());
	}

}
