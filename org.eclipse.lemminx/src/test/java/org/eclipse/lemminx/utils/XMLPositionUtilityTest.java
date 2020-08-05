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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMParser;
import static org.eclipse.lemminx.XMLAssert.r;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * XMLPositionUtilityTest
 */
public class XMLPositionUtilityTest {

	@Test
	public void testGetMatchingRangesStartTagMiddle() {
		String initialText = "<Apple>\n" + "  <Or|ange></Orange>\n" + "</Apple>";
		testMatchingTagRanges(initialText, r(1, 3, 1, 9), r(1, 12, 1, 18));
	}

	@Test
	public void testGetMatchingRangesStartTagBeginning() {
		String initialText = "<Apple>\n" + "  <|Orange></Orange>\n" + "</Apple>";
		testMatchingTagRanges(initialText, r(1, 3, 1, 9), r(1, 12, 1, 18));
	}

	@Test
	public void testGetMatchingRangesStartTagEnd() {
		String initialText = "<Apple>\n" + "  <Orange|></Orange>\n" + "</Apple>";
		testMatchingTagRanges(initialText, r(1, 3, 1, 9), r(1, 12, 1, 18));
	}

	@Test
	public void testGetMatchingRangesStartTagDifferentLine() {
		String initialText = "<Apple>\n" + "  <Orange|>\n  </Orange>\n" + "</Apple>";
		testMatchingTagRanges(initialText, r(1, 3, 1, 9), r(2, 4, 2, 10));
	}

	@Test
	public void testGetMatchingRangesStartTagAttributes() {
		String initialText = "<Apple>\n" + "  <Orange| amount=\"1\"></Orange>\n" + "</Apple>";
		testMatchingTagRanges(initialText, r(1, 3, 1, 9), r(1, 23, 1, 29));
	}

	@Test
	public void testGetMatchingEndTagNoResult() {
		String initialText = "<Apple>\n" + "  |<Orange></Orange>\n" + "</Apple>";
		testMatchingTagRanges(initialText);
	}

	@Test
	public void testGetMatchingEndTagNoResult2() {
		String initialText = "<Apple>\n" + "  <Orange |></Orange>\n" + // Because there is a space
				"</Apple>";
		testMatchingTagRanges(initialText);
	}

	@Test
	public void testGetMatchingEndTagTextBetween() {
		String initialText = "<Apple>\n" + "  <Orange|>Text Between</Orange>\n" + "</Apple>";
		testMatchingTagRanges(initialText, r(1, 3, 1, 9), r(1, 24, 1, 30));
	}

	@Test
	public void testGetMatchingEndTagElementBetween() {
		String initialText = "<Apple>\n" + "  <Orange|><Lemon></Lemon></Orange>\n" + "</Apple>";
		testMatchingTagRanges(initialText, r(1, 3, 1, 9), r(1, 27, 1, 33));
	}

	@Test
	public void testGetMatchingRangesEndTagMiddle() {
		String initialText = "<Apple>\n" + "  <Orange></Or|ange>\n" + "</Apple>";
		testMatchingTagRanges(initialText, r(1, 3, 1, 9), r(1, 12, 1, 18));
	}

	@Test
	public void testGetMatchingRangesEndTagBeginning() {
		String initialText = "<Apple>\n" + "  <Orange></|Orange>\n" + "</Apple>";
		testMatchingTagRanges(initialText, r(1, 3, 1, 9), r(1, 12, 1, 18));
	}

	@Test
	public void testGetMatchingRangesEndTagEnd() {
		String initialText = "<Apple>\n" + "  <Orange></Orange|>\n" + "</Apple>";
		testMatchingTagRanges(initialText, r(1, 3, 1, 9), r(1, 12, 1, 18));
	}

	@Test
	public void testGetMatchingRangesEndTagDifferentLine() {
		String initialText = "<Apple>\n" + "  <Orange>\n  </Or|ange>\n" + "</Apple>";
		testMatchingTagRanges(initialText, r(1, 3, 1, 9), r(2, 4, 2, 10));
	}

	@Test
	public void testGetMatchingRangesEndTagAttributes() {
		String initialText = "<Apple>\n" + "  <Orange amount=\"1\"></Orange|>\n" + "</Apple>";
		testMatchingTagRanges(initialText, r(1, 3, 1, 9), r(1, 23, 1, 29));
	}

	@Test
	public void testGetMatchingRangesStartTagAttributesPrefixed() {
		String initialText = "<Apple>\n" + "  <prefix:Orange| amount=\"1\"></prefix:Orange>\n" + "</Apple>";
		testMatchingTagRanges(initialText, r(1, 3, 1, 16), r(1, 30, 1, 43));
	}

	@Test
	public void testGetMatchingRangesStartTagAttributesPrefixed2() {
		String initialText = "<Apple>\n" + "  <pref|ix:Orange amount=\"1\"></prefix:Orange>\n" + "</Apple>";
		testMatchingTagRanges(initialText, r(1, 3, 1, 16), r(1, 30, 1, 43));
	}

	@Test
	public void entityReference() {
		assertEntityReferenceOffset("|", -1, -1);
		assertEntityReferenceOffset("ab|cd", -1, -1);
		assertEntityReferenceOffset("&|", 0, -1);
		assertEntityReferenceOffset("&a|", 0, -1);
		assertEntityReferenceOffset("\n&|", 1, -1);
		assertEntityReferenceOffset("\n&a|", 1, -1);
		assertEntityReferenceOffset("&ab|cd;&efgh;", 0, 6);
		assertEntityReferenceOffset("& ab|cd;&efgh;", -1, 7);
	}

	private static void assertEntityReferenceOffset(String xml, int start, int end) {
		int offset = xml.indexOf('|');
		xml = xml.substring(0, offset) + xml.substring(offset + 1, xml.length());
		Assertions.assertEquals(start, XMLPositionUtility.getEntityReferenceStartOffset(xml, offset), "test for start offset ");
		Assertions.assertEquals(end, XMLPositionUtility.getEntityReferenceEndOffset(xml, offset), "Test for end offset ");
	}

	private static void testMatchingTagRanges(String initialCursorText, Range ...expectedRanges) {

		int offset = initialCursorText.indexOf('|');
		initialCursorText = initialCursorText.substring(0, offset) + initialCursorText.substring(offset + 1);
		DOMDocument xmlDocument = DOMParser.getInstance().parse(initialCursorText, "testURI", null);
		Position initialCursorPosition;
		List<Range> ranges;
		try {
			initialCursorPosition = xmlDocument.positionAt(offset);
			ranges = XMLPositionUtility.getMatchingTagRanges(xmlDocument, initialCursorPosition);
		} catch (BadLocationException e) {
			fail(e.getMessage());
			return;
		}

		assertEquals(Arrays.asList(expectedRanges), ranges);
	}
}