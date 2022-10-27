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

import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMText;
import org.eclipse.lsp4j.TextEdit;

/**
 * DOM text formatter.
 * 
 * @author Angelo ZERR
 *
 */
public class DOMTextFormatter {

	private final XMLFormatterDocumentNew formatterDocument;

	public DOMTextFormatter(XMLFormatterDocumentNew formatterDocument) {
		this.formatterDocument = formatterDocument;
	}

	public void formatText(DOMText textNode, XMLFormattingConstraints parentConstraints, List<TextEdit> edits) {
		// Don't format the spacing in text for case of preserve empty content setting
		if (isPreserveEmptyContent()) {
			return;
		}
		FormatElementCategory formatElementCategory = parentConstraints.getFormatElementCategory();
		String text = formatterDocument.getText();
		int availableLineWidth = parentConstraints.getAvailableLineWidth();
		int indentLevel = parentConstraints.getIndentLevel();
		int maxLineWidth = getMaxLineWidth();

		int spaceStart = -1;
		int spaceEnd = -1;
		int lineSeparatorOffset = -1;
		boolean containsNewLine = false;

		for (int i = textNode.getStart(); i < textNode.getEnd(); i++) {
			char c = text.charAt(i);
			if (Character.isWhitespace(c)) {
				// Whitespaces...
				if (isLineSeparator(c)) {
					if (!containsNewLine) {
						lineSeparatorOffset = i;
					}
					containsNewLine = true;
				}
				if (spaceStart == -1) {
					spaceStart = i;
				} else {
					spaceEnd = i;
				}
			} else {
				// Text content...
				spaceEnd = i;
				int contentStart = i;
				while (i + 1 < textNode.getEnd() && !Character.isWhitespace(text.charAt(i + 1))) {
					i++;
				}
				int contentEnd = i + 1;
				availableLineWidth -= contentEnd - contentStart;
				if (formatElementCategory != FormatElementCategory.PreserveSpace) {
					if (textNode.getStart() != contentStart && availableLineWidth >= 0
							&& (isJoinContentLines() || !containsNewLine)) {
						// Decrement width for normalized space between text content (not done at
						// beginning)
						availableLineWidth--;
					}
					if (availableLineWidth < 0) {
						int mixedContentIndentLevel = parentConstraints.getMixedContentIndentLevel() == 0 ? indentLevel
								: parentConstraints.getMixedContentIndentLevel();
						if (spaceStart != -1) {
							replaceLeftSpacesWithIndentation(mixedContentIndentLevel, spaceStart, contentStart,
									true, edits);
							availableLineWidth = maxLineWidth - (contentEnd - contentStart)
									- mixedContentIndentLevel * getTabSize();
							containsNewLine = false;
						}
					} else if (isJoinContentLines() || !containsNewLine) {
						// Case of isJoinContent == true: join all text content with single space
						// Case of isJoinContent == false: normalize space only between element start
						// tag and start of text content or doesn't contain a new line
						replaceSpacesWithOneSpace(spaceStart, spaceEnd - 1, edits);
						containsNewLine = false;
					} else if (containsNewLine) {
						replaceLeftSpacesWithIndentationPreservedNewLines(spaceStart, spaceEnd,
								indentLevel, edits);
						containsNewLine = false;
						availableLineWidth = maxLineWidth - (contentEnd - contentStart)
								- indentLevel * getTabSize();
					} else {
						availableLineWidth -= spaceEnd - spaceStart;
					}
				}
				parentConstraints.setAvailableLineWidth(availableLineWidth);
				spaceStart = -1;
				spaceEnd = -1;
			}
		}
		if (formatElementCategory != FormatElementCategory.PreserveSpace
				&& formatElementCategory != FormatElementCategory.IgnoreSpace && spaceEnd + 1 != text.length()) {
			DOMElement parentElement = textNode.getParentElement();
			// Don't format final spaces if text is at the end of the file
			if (!containsNewLine || isJoinContentLines()) {
				// Replace spaces with single space in the case of:
				// 1. there is no new line
				// 2. isJoinContentLines
				replaceSpacesWithOneSpace(spaceStart, spaceEnd, edits);
				if (spaceStart != -1) {
					availableLineWidth--;
					parentConstraints.setAvailableLineWidth(availableLineWidth);
				}
			} else {
				if (formatElementCategory == FormatElementCategory.NormalizeSpace
						|| parentElement.getLastChild() == textNode) {
					// Decrement indent level if is mixed content and text content is the last child
					indentLevel--;
				}
				replaceLeftSpacesWithIndentationPreservedNewLines(spaceStart, spaceEnd + 1, indentLevel,
						edits);
			}
		} else if (isTrimTrailingWhitespace()) {
			removeLeftSpaces(spaceStart, lineSeparatorOffset, edits);
		}
	}

	private static boolean isLineSeparator(char c) {
		return c == '\r' || c == '\n';
	}

	private int getMaxLineWidth() {
		return formatterDocument.getMaxLineWidth();
	}

	private int getTabSize() {
		return formatterDocument.getSharedSettings().getFormattingSettings().getTabSize();
	}

	private boolean isPreserveEmptyContent() {
		return formatterDocument.getSharedSettings().getFormattingSettings().isPreserveEmptyContent();
	}

	private void replaceSpacesWithOneSpace(int spaceStart, int spaceEnd, List<TextEdit> edits) {
		formatterDocument.replaceSpacesWithOneSpace(spaceStart, spaceEnd, edits);
	}

	private int replaceLeftSpacesWithIndentation(int indentLevel, int from, int to, boolean addLineSeparator,
			List<TextEdit> edits) {
		return formatterDocument.replaceLeftSpacesWithIndentation(indentLevel, from, to, addLineSeparator, edits);
	}

	private void replaceLeftSpacesWithIndentationPreservedNewLines(int spaceStart, int spaceEnd,
			int indentLevel, List<TextEdit> edits) {
		formatterDocument.replaceLeftSpacesWithIndentationPreservedNewLines(spaceStart, spaceEnd, indentLevel,
				edits);
	}

	private void removeLeftSpaces(int leftLimit, int to, List<TextEdit> edits) {
		formatterDocument.replaceLeftSpacesWith(leftLimit, to, "", edits);
	}

	private boolean isJoinContentLines() {
		return formatterDocument.getSharedSettings().getFormattingSettings().isJoinContentLines();
	}

	private boolean isTrimTrailingWhitespace() {
		return formatterDocument.getSharedSettings().getFormattingSettings().isTrimTrailingWhitespace();
	}
}
