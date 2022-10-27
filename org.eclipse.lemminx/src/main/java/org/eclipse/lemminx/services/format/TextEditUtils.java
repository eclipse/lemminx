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
package org.eclipse.lemminx.services.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.commons.TextDocument;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

/**
 * Utilities for {@link TextEdit}.
 * 
 * @author Angelo ZERR
 *
 */
public class TextEditUtils {

	private static final Logger LOGGER = Logger.getLogger(TextEditUtils.class.getName());

	/**
	 * Returns the {@link TextEdit} to insert the given expected content from the
	 * given range (from, to) of the given text document and null otherwise.
	 * 
	 * @param from            the range from.
	 * @param to              the range to.
	 * @param expectedContent the expected content.
	 * @param textDocument    the text document.
	 * 
	 * @return the {@link TextEdit} to insert the given expected content from the
	 *         given range (from, to) of the given text document and null otherwise.
	 */
	public static TextEdit createTextEditIfNeeded(int from, int to, String expectedContent, TextDocument textDocument) {
		String text = textDocument.getText();

		// Check if content from the range [from, to] is the same than expected content
		if (isMatchExpectedContent(from, to, expectedContent, text)) {
			// The expected content exists, no need to create a TextEdit
			return null;
		}

		// Insert the expected content.
		try {
			Position endPos = textDocument.positionAt(to);
			Position startPos = to == from ? endPos : textDocument.positionAt(from);
			Range range = new Range(startPos, endPos);
			return new TextEdit(range, expectedContent);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Returns true if the given content from the range [from, to] of the given text
	 * is the same than expected content and false otherwise.
	 * 
	 * @param from            the from range.
	 * @param to              the to range.
	 * @param expectedContent the expected content.
	 * @param text            the text document.
	 * 
	 * @return true if the given content from the range [from, to] of the given text
	 *         is the same than expected content and false otherwise.
	 */
	private static boolean isMatchExpectedContent(int from, int to, String expectedContent, String text) {
		if (expectedContent.length() == to - from) {
			int j = 0;
			for (int i = from; i < to; i++) {
				char c = text.charAt(i);
				if (expectedContent.charAt(j) != c) {
					return false;
				}
				j++;
			}
		} else {
			return false;
		}
		return true;
	}

	public static String applyEdits(TextDocument document, List<? extends TextEdit> edits) throws BadLocationException {
		String text = document.getText();
		Collections.sort(edits /* .map(getWellformedEdit) */, (a, b) -> {
			int diff = a.getRange().getStart().getLine() - b.getRange().getStart().getLine();
			if (diff == 0) {
				return a.getRange().getStart().getCharacter() - b.getRange().getStart().getCharacter();
			}
			return diff;
		});
		int lastModifiedOffset = 0;
		List<String> spans = new ArrayList<>();
		for (TextEdit e : edits) {
			int startOffset = document.offsetAt(e.getRange().getStart());
			if (startOffset < lastModifiedOffset) {
				throw new Error("Overlapping edit");
			} else if (startOffset > lastModifiedOffset) {
				spans.add(text.substring(lastModifiedOffset, startOffset));
			}
			if (e.getNewText() != null) {
				spans.add(e.getNewText());
			}
			lastModifiedOffset = document.offsetAt(e.getRange().getEnd());
		}
		spans.add(text.substring(lastModifiedOffset));
		return spans.stream() //
				.collect(Collectors.joining());
	}

	/**
	 * Returns the offset of the first whitespace that's found in the given range
	 * [leftLimit,to] from the left of the to, and leftLimit otherwise.
	 * 
	 * @param leftLimit the left limit range.
	 * @param to        the to range.
	 * 
	 * @return the offset of the first whitespace that's found in the given range
	 *         [leftLimit,to] from the left of the to, and leftLimit otherwise.
	 */
	public static int adjustOffsetWithLeftWhitespaces(int leftLimit, int to, String text) {
		if (to == 0) {
			return -1;
		}
		for (int i = to - 1; i >= leftLimit; i--) {
			char c = text.charAt(i);
			if (!Character.isWhitespace(c)) {
				// The current character is not a whitespace, return the offset of the character
				return i + 1;
			}
		}
		return leftLimit;
	}

}
