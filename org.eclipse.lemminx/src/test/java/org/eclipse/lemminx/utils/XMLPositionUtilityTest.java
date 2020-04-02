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

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMParser;
import org.eclipse.lsp4j.Position;
import org.junit.jupiter.api.Test;

/**
 * XMLPositionUtilityTest
 */
public class XMLPositionUtilityTest {

	@Test
	public void testGetMatchingEndTagPositionMiddle() {
		String initialText= 
				"<Apple>\n" +
				"  <Or|ange></Orange>\n" +
				"</Apple>";

		String expectedText= 
				"<Apple>\n" +
				"  <Orange></Or|ange>\n" +
				"</Apple>";
		
		testMatchingTagPosition(initialText, expectedText);
	}

	@Test
	public void testGetMatchingEndTagPositionBeginning() {
		String initialText= 
				"<Apple>\n" +
				"  <|Orange></Orange>\n" +
				"</Apple>";

		String expectedText= 
				"<Apple>\n" +
				"  <Orange></|Orange>\n" +
				"</Apple>";
		
		testMatchingTagPosition(initialText, expectedText);
	}

	@Test
	public void testGetMatchingEndTagPositionEnd() {
		String initialText= 
				"<Apple>\n" +
				"  <Orange|></Orange>\n" +
				"</Apple>";

		String expectedText= 
				"<Apple>\n" +
				"  <Orange></Orange|>\n" +
				"</Apple>";
		
		testMatchingTagPosition(initialText, expectedText);
	}

	@Test
	public void testGetMatchingEndTagPositionAttributes() {
		String initialText= 
				"<Apple>\n" +
				"  <Orange| amount=\"1\"></Orange>\n" +
				"</Apple>";

		String expectedText= 
				"<Apple>\n" +
				"  <Orange amount=\"1\"></Orange|>\n" +
				"</Apple>";
		
		testMatchingTagPosition(initialText, expectedText);
	}

	@Test
	public void testGetMatchingEndTagNoResult() {
		String initialText= 
				"<Apple>\n" +
				"  |<Orange></Orange>\n" +
				"</Apple>";

		String expectedText= 
				"<Apple>\n" +
				"  <Orange></Orange>\n" +
				"</Apple>";
		
		testMatchingTagPosition(initialText, expectedText);
	}

	@Test
	public void testGetMatchingEndTagNoResult2() {
		String initialText= 
				"<Apple>\n" +
				"  <Orange |></Orange>\n" + // Because there is a space
				"</Apple>";

		String expectedText= 
				"<Apple>\n" +
				"  <Orange ></Orange>\n" +
				"</Apple>";
		
		testMatchingTagPosition(initialText, expectedText);
	}

	@Test
	public void testGetMatchingEndTagTextBetween() {
		String initialText= 
				"<Apple>\n" +
				"  <Orange|>Text Between</Orange>\n" +
				"</Apple>";

		String expectedText= 
				"<Apple>\n" +
				"  <Orange>Text Between</Orange|>\n" +
				"</Apple>";
		
		testMatchingTagPosition(initialText, expectedText);
	}

	@Test
	public void testGetMatchingEndTagElementBetween() {
		String initialText= 
				"<Apple>\n" +
				"  <Orange|><Lemon></Lemon></Orange>\n" +
				"</Apple>";

		String expectedText= 
				"<Apple>\n" +
				"  <Orange><Lemon></Lemon></Orange|>\n" +
				"</Apple>";
		
		testMatchingTagPosition(initialText, expectedText);
	}

	@Test
	public void testGetMatchingStartTagPositionMiddle() {
		String initialText= 
				"<Apple>\n" +
				"  <Orange></Or|ange>\n" +
				"</Apple>";

		String expectedText= 
				"<Apple>\n" +
				"  <Or|ange></Orange>\n" +
				"</Apple>";
		
		testMatchingTagPosition(initialText, expectedText);
	}

	@Test
	public void testGetMatchingStartTagPositionBeginning() {
		String initialText= 
				"<Apple>\n" +
				"  <Orange></|Orange>\n" +
				"</Apple>";
				
		String expectedText= 
				"<Apple>\n" +
				"  <|Orange></Orange>\n" +
				"</Apple>";

		
		testMatchingTagPosition(initialText, expectedText);
	}

	@Test
	public void testGetMatchingStartTagPositionEnd() {
		String initialText= 
				"<Apple>\n" +
				"  <Orange></Orange|>\n" +
				"</Apple>";
				

		String expectedText= 
				"<Apple>\n" +
				"  <Orange|></Orange>\n" +
				"</Apple>";
		
		testMatchingTagPosition(initialText, expectedText);
	}

	@Test
	public void testGetMatchingStartTagPositionAttributes() {
		String initialText= 
				"<Apple>\n" +
				"  <Orange amount=\"1\"></Orange|>\n" +
				"</Apple>";
				

		String expectedText= 
				"<Apple>\n" +
				"  <Orange| amount=\"1\"></Orange>\n" +
				"</Apple>";
		
		testMatchingTagPosition(initialText, expectedText);
	}

	@Test
	public void testGetMatchingEndTagPositionAttributesPrefixed() {
		String initialText= 
				"<Apple>\n" +
				"  <prefix:Orange| amount=\"1\"></prefix:Orange>\n" +
				"</Apple>";
				
		String expectedText= 
				"<Apple>\n" +
				"  <prefix:Orange amount=\"1\"></prefix:Orange|>\n" +
				"</Apple>";
		
		testMatchingTagPosition(initialText, expectedText);
	}

	@Test
	public void testGetMatchingEndTagPositionPrefixed() {
		String initialText= 
				"<Apple>\n" +
				"  <pref|ix:Orange></prefix:Orange>\n" +
				"</Apple>";
				
		String expectedText= 
				"<Apple>\n" +
				"  <prefix:Orange></pref|ix:Orange>\n" +
				"</Apple>";
		
		testMatchingTagPosition(initialText, expectedText);
	}

	private static void testMatchingTagPosition(String initialCursorText, String expectedCursorText) {

		int offset = initialCursorText.indexOf('|');
		initialCursorText = initialCursorText.substring(0, offset) + initialCursorText.substring(offset + 1);
		DOMDocument xmlDocument = DOMParser.getInstance().parse(initialCursorText, "testURI", null);
		Position initialCursorPosition;
		Position newCursorPosition;
		int newCursorOffset = -1;
		try {
			initialCursorPosition = xmlDocument.positionAt(offset);
			newCursorPosition = XMLPositionUtility.getMatchingTagPosition(xmlDocument, initialCursorPosition);
			if(newCursorPosition != null) { // a result for a matching position was found
				newCursorOffset = xmlDocument.offsetAt(newCursorPosition);
			}
		} catch (BadLocationException e) {
			fail(e.getMessage());
			return;
		}

		StringBuffer sBuffer = new StringBuffer(initialCursorText);
		String actualOutputString;
		if(newCursorOffset > -1) {
			actualOutputString = sBuffer.insert(newCursorOffset, "|").toString();
		} 
		else { // no matching position was found
			actualOutputString = initialCursorText;
		}

		assertEquals(expectedCursorText, actualOutputString);
	}
}