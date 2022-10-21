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

import java.util.List;

import org.eclipse.lemminx.dom.DOMComment;
import org.eclipse.lsp4j.TextEdit;

/**
 * DOM comment formatter.
 */
public class DOMCommentFormatter {

	private final XMLFormatterDocumentNew formatterDocument;

	public DOMCommentFormatter(XMLFormatterDocumentNew formatterDocument) {
		this.formatterDocument = formatterDocument;
	}

	public void formatComment(DOMComment commentNode, XMLFormattingConstraints parentConstraints, int startRange,
			int endRange, List<TextEdit> edits) {

		// Don't format the spacing in text for case of preserve empty content setting
		if (isPreserveEmptyContent()) {
			return;
		}

		// Check the comment is closed properly
		if (commentNode.getEnd() == commentNode.getEndContent()) {
			return;
		}

		String text = formatterDocument.getText();
		int availableLineWidth = parentConstraints.getAvailableLineWidth();
		int start = commentNode.getStart();
		int leftWhitespaceOffset = start > 0 ? start - 1 : 0;

		while (leftWhitespaceOffset > 0 && Character.isWhitespace(text.charAt(leftWhitespaceOffset))) {
			leftWhitespaceOffset--;
		}

		int preservedNewLines = getPreservedNewlines();
		boolean addLineSeparator = formatterDocument.hasLineBreak(leftWhitespaceOffset, start);
		int indentLevel = parentConstraints.getIndentLevel();
		int currentNewLineCount = XMLFormatterDocumentNew.getExistingNewLineCount(formatterDocument.getText(),
				start,
				formatterDocument.getLineDelimiter());
		if (currentNewLineCount > preservedNewLines && startRange < start) {
			replaceLeftSpacesWithIndentationWithMultiNewLines(indentLevel, 0, start,
					preservedNewLines + 1, edits);
			availableLineWidth = getMaxLineWidth() - getTabSize() * indentLevel;
		} else if (addLineSeparator && startRange < start) {
			int newLineCount = currentNewLineCount == 0 && !addLineSeparator ? 1 : currentNewLineCount;
			replaceLeftSpacesWithIndentationWithMultiNewLines(indentLevel, 0, start,
					newLineCount, edits);
			availableLineWidth = getMaxLineWidth() - getTabSize() * indentLevel;
		}
		int spaceStart = -1;
		int spaceEnd = -1;
		availableLineWidth -= 4;

		for (int i = commentNode.getStartContent(); i < commentNode.getEndContent(); i++) {
			char c = text.charAt(i);
			if (Character.isWhitespace(c)) {
				// Whitespaces
				if (spaceStart == -1) {
					spaceStart = i;
				} else {
					spaceEnd = i;
				}
			} else {
				spaceEnd = i;
				// Ensure the edit is within the selected range
				if (startRange != -1 && endRange != -1 && (startRange > spaceStart || endRange < spaceEnd)) {
					return;
				}
				int contentStart = i;
				while (i + 1 < commentNode.getEnd() && !Character.isWhitespace(text.charAt(i + 1))) {
					i++;
				}
				int contentEnd = i;
				availableLineWidth -= (contentEnd + 1 - contentStart);
				if (availableLineWidth <= 0 && spaceStart != -1) {
					// Add new line when the comment extends over the maximum line width
					replaceLeftSpacesWithIndentation(indentLevel, spaceStart, contentStart,
							true, edits);
					int indentSpaces = (getTabSize() * indentLevel);
					availableLineWidth = getMaxLineWidth() - indentSpaces - (contentEnd + 1 - contentStart);
				} else if (isJoinCommentLines()) {
					replaceSpacesWithOneSpace(spaceStart, spaceEnd - 1, edits);
					availableLineWidth--;
				} else if (spaceStart != -1) {
					availableLineWidth -= spaceEnd - spaceStart;
				}
				spaceStart = -1;
				spaceEnd = -1;
			}
		}
		if (isJoinCommentLines()) {
			replaceSpacesWithOneSpace(spaceStart, spaceEnd, edits);
			availableLineWidth--;
		}
	}

	private boolean isJoinCommentLines() {
		return formatterDocument.getSharedSettings().getFormattingSettings().isJoinCommentLines();
	}

	private boolean isPreserveEmptyContent() {
		return formatterDocument.getSharedSettings().getFormattingSettings().isPreserveEmptyContent();
	}

	private int getTabSize() {
		return formatterDocument.getSharedSettings().getFormattingSettings().getTabSize();
	}

	private int getMaxLineWidth() {
		return formatterDocument.getMaxLineWidth();
	}

	private int getPreservedNewlines() {
		return formatterDocument.getSharedSettings().getFormattingSettings().getPreservedNewlines();
	}

	private void replaceSpacesWithOneSpace(int spaceStart, int spaceEnd, List<TextEdit> edits) {
		formatterDocument.replaceSpacesWithOneSpace(spaceStart, spaceEnd, edits);
	}

	private int replaceLeftSpacesWithIndentation(int indentLevel, int from, int to, boolean addLineSeparator,
			List<TextEdit> edits) {
		return formatterDocument.replaceLeftSpacesWithIndentation(indentLevel, from, to, addLineSeparator, edits);
	}

	private int replaceLeftSpacesWithIndentationWithMultiNewLines(int indentLevel, int leftLimit, int offset,
			int newLineCount,
			List<TextEdit> edits) {
		return formatterDocument.replaceLeftSpacesWithIndentationWithMultiNewLines(indentLevel, leftLimit, offset,
				newLineCount,
				edits);
	}
}
