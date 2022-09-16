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

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.settings.XMLFormattingOptions.EmptyElements;
import org.eclipse.lsp4j.TextEdit;

/**
 * DOM element formatter.
 * 
 * @author Angelo ZERR
 *
 */
public class DOMElementFormatter {

	private final XMLFormatterDocumentNew formatterDocument;

	private final DOMAttributeFormatter attributeFormatter;

	public DOMElementFormatter(XMLFormatterDocumentNew formatterDocument, DOMAttributeFormatter attributeFormatter) {
		this.formatterDocument = formatterDocument;
		this.attributeFormatter = attributeFormatter;
	}

	public void formatElement(DOMElement element, XMLFormattingConstraints parentConstraints, int start, int end,
			List<TextEdit> edits) {
		EmptyElements emptyElements = getEmptyElements(element);

		// Format start tag element with proper indentation
		int indentLevel = parentConstraints.getIndentLevel();
		int nb = formatStartTagElement(element, parentConstraints, emptyElements, edits);

		if (emptyElements == EmptyElements.ignore) {
			// Format children of the element
			XMLFormattingConstraints constraints = new XMLFormattingConstraints();
			constraints.copyConstraints(parentConstraints);
			if ((element.isClosed())) {
				constraints.setIndentLevel(indentLevel + 1);
			}
			constraints.setFormatElementCategory(getFormatElementCategory(element, parentConstraints));
			constraints.setAvailableLineWidth(getMaxLineWidth() - nb);

			formatChildren(element, constraints, start, end, edits);

			// Format end tag element with proper indentation
			if (element.hasEndTag()) {
				formatEndTagElement(element, parentConstraints, constraints, edits);
			}
		}
	}

	private int formatStartTagElement(DOMElement element, XMLFormattingConstraints parentConstraints,
			EmptyElements emptyElements, List<TextEdit> edits) {
		int width = 0;
		int indentLevel = parentConstraints.getIndentLevel();
		FormatElementCategory formatElementCategory = parentConstraints.getFormatElementCategory();

		switch (formatElementCategory) {
		case PreserveSpace:
			// Preserve existing spaces
			break;
		case MixedContent:
			break;
		case IgnoreSpace:
			int startTagOffset = element.getStartTagOpenOffset();
			// If preserve new lines
			int preservedNewLines = getPreservedNewlines();
			int currentNewLineCount = getExistingNewLineCount(formatterDocument.getText(), startTagOffset,
					formatterDocument.getLineDelimiter());
			if (currentNewLineCount > preservedNewLines) {
				replaceLeftSpacesWithIndentationWithMultiNewLines(indentLevel, startTagOffset,
						preservedNewLines + 1, edits);
			} else {
				// remove spaces and indent
				boolean addLineSperator = element.getParentElement() == null
						&& element.getPreviousSibling() == null;
				int nbSpaces = replaceLeftSpacesWithIndentation(indentLevel, startTagOffset, !addLineSperator,
						edits);
				width = nbSpaces + element.getStartTagCloseOffset() - startTagOffset;
			}
		case NormalizeSpace:
			break;
		}

		if (formatElementCategory != FormatElementCategory.PreserveSpace) {
			formatAttributes(element, parentConstraints, edits);

			boolean formatted = false;
			switch (emptyElements) {
			case expand: {
				if (element.isSelfClosed()) {
					// expand empty element: <example /> -> <example></example>
					StringBuilder tag = new StringBuilder();
					tag.append(">");
					tag.append("</");
					tag.append(element.getTagName());
					tag.append('>');
					createTextEditIfNeeded(element.getEnd() - 3, element.getEnd(), tag.toString(), edits);
					formatted = true;
				}
				break;
			}
			case collapse: {
				// collapse empty element: <example></example> -> <example />
				if (!element.isSelfClosed()) {
					StringBuilder tag = new StringBuilder();
					if (isSpaceBeforeEmptyCloseTag()) {
						tag.append(" ");
					}
					tag.append("/>");
					createTextEditIfNeeded(element.getStartTagCloseOffset() - 1, element.getEnd(), tag.toString(),
							edits);
					formatted = true;
				}
				break;
			}
			default:
			}

			if (!formatted) {
				if (element.isSelfClosed()) {
					// <foo/> --> <foo />
					int offset = element.getEnd() - 2;
					if (shouldFormatClosingBracketNewLine(element)) {
						// Add newline with indent before closing bracket. Ex:
						// <a
						// b=""
						// c=""|/> --> add newline here at '|'
						replaceLeftSpacesWithIndentation(indentLevel + getSplitAttributesIndentSize(), offset, true,
								edits);
					} else if (isSpaceBeforeEmptyCloseTag()) {
						replaceLeftSpacesWithOneSpace(offset, edits);
					} else {
						removeLeftSpaces(offset, edits);
					}
				} else if (element.isStartTagClosed()) {
					formatElementStartTagCloseBracket(element, parentConstraints, edits);
				}
			}
		}
		return width;
	}

	private int formatAttributes(DOMElement element, XMLFormattingConstraints parentConstraints, List<TextEdit> edits) {
		if (element.hasAttributes()) {
			List<DOMAttr> attributes = element.getAttributeNodes();
			int prevOffset = element.getOffsetAfterStartTag();
			boolean singleAttribute = attributes.size() == 1;
			for (DOMAttr attr : attributes) {
				attributeFormatter.formatAttribute(attr, prevOffset, singleAttribute, true, parentConstraints, edits);
				prevOffset = attr.getEnd();
			}
		}
		return 0;
	}

	/**
	 * Formats the start tag's closing bracket (>) according to
	 * {@code XMLFormattingOptions#isPreserveAttrLineBreaks()}
	 *
	 * {@code XMLFormattingOptions#isPreserveAttrLineBreaks()}: If true, must add a
	 * newline + indent before the closing bracket if the last attribute of the
	 * element and the closing bracket are in different lines.
	 *
	 * @param element
	 * @throws BadLocationException
	 */
	private void formatElementStartTagCloseBracket(DOMElement element, XMLFormattingConstraints parentConstraints,
			List<TextEdit> edits) {
		int offset = element.getStartTagCloseOffset();
		String replace = "";
		if (isPreserveAttributeLineBreaks() && element.hasAttributes()
				&& hasLineBreak(getLastAttribute(element).getEnd(), element.getStartTagCloseOffset())) {
			int indentLevel = parentConstraints.getIndentLevel();
			if (indentLevel == 0) {
				// Add newline when there is no indent
				replace = formatterDocument.getLineDelimiter();
			} else {
				// Add newline with indent according to indent level
				replaceLeftSpacesWithIndentation(indentLevel, offset, true, edits);
				return;
			}
		} else if (shouldFormatClosingBracketNewLine(element)) {
			int indentLevel = parentConstraints.getIndentLevel();
			replaceLeftSpacesWithIndentation(indentLevel + getSplitAttributesIndentSize(), offset, true, edits);
			return;
		}
		replaceLeftSpacesWith(offset, replace, edits);
	}

	private int formatEndTagElement(DOMElement element, XMLFormattingConstraints parentConstraints,
			XMLFormattingConstraints constraints, List<TextEdit> edits) {
		// 1) remove / add some spaces on the left of the end tag element
		// before formatting : [space][space]</a>
		// after formatting : </a>
		int indentLevel = parentConstraints.getIndentLevel();
		FormatElementCategory formatElementCategory = constraints.getFormatElementCategory();
		switch (formatElementCategory) {
		case PreserveSpace:
			// Preserve existing spaces
			break;
		case MixedContent:
			break;
		case IgnoreSpace:
			int endTagOffset = element.getEndTagOpenOffset();
			// If preserve new lines
			int preservedNewLines = getPreservedNewlines();
			int currentNewLineCount = getExistingNewLineCount(formatterDocument.getText(), endTagOffset,
					formatterDocument.getLineDelimiter());
			if (currentNewLineCount > preservedNewLines) {
				replaceLeftSpacesWithIndentationWithMultiNewLines(indentLevel, endTagOffset, preservedNewLines + 1,
						edits);
			} else {
				// remove spaces and indent
				replaceLeftSpacesWithIndentation(indentLevel, endTagOffset, true, edits);
				break;
			}
		case NormalizeSpace:
			break;
		}
		// 2) remove some spaces between the end tag and and close bracket
		// before formatting : <a></a[space][space]>
		// after formatting : <a></a>
		if (element.isEndTagClosed()) {
			int endTagOffset = element.getEndTagCloseOffset();
			removeLeftSpaces(endTagOffset, edits);
		}
		return 0;
	}

	/**
	 * Return the number of new lines in the whitespaces to the left of the given
	 * offset.
	 *
	 * @param text      the xml text.
	 * @param offset    the offset to begin the count from.
	 * @param delimiter the delimiter.
	 *
	 * @return the number of new lines in the whitespaces to the left of the given
	 *         offset.
	 */
	private int getExistingNewLineCount(String text, int offset, String delimiter) {
		boolean delimiterHasTwoCharacters = delimiter.length() == 2;
		int newLineCounter = 0;
		for (int i = offset; i > 0; i--) {
			String c;
			if (!Character.isWhitespace(text.charAt(i - 1))) {
				if (!delimiterHasTwoCharacters) {
					c = String.valueOf(text.charAt(i));
					if (delimiter.equals(c)) {
						newLineCounter++;
					}
				}
				return newLineCounter;
			}
			if (delimiterHasTwoCharacters) {
				c = text.substring(i - 2, i);
				if (delimiter.equals(c)) {
					newLineCounter++;
					i--; // skip the second char of the delimiter
				}
			} else {
				c = String.valueOf(text.charAt(i));
				if (delimiter.equals(c)) {
					newLineCounter++;
				}
			}
		}
		return newLineCounter;
	}

	/**
	 * Return the option to use to generate empty elements.
	 *
	 * @param element the DOM element
	 * @return the option to use to generate empty elements.
	 */
	private EmptyElements getEmptyElements(DOMElement element) {
		EmptyElements emptyElements = getEmptyElements();
		if (emptyElements != EmptyElements.ignore) {
			if (element.isClosed() && element.isEmpty()) {
				// Element is empty and closed
				switch (emptyElements) {
				case expand:
				case collapse: {
					if (isPreserveEmptyContent()) {
						// preserve content
						if (element.hasChildNodes()) {
							// The element is empty and contains somes spaces which must be preserved
							return EmptyElements.ignore;
						}
					}
					return emptyElements;
				}
				default:
					return emptyElements;
				}
			}
		}
		return EmptyElements.ignore;
	}

	/**
	 * Return true if conditions are met to format according to the
	 * closingBracketNewLine setting.
	 * 
	 * 1. splitAttribute must be set to true
	 * 2. there must be at least 2 attributes in the element
	 *
	 * @param element the DOM element
	 * @return true if should format according to closingBracketNewLine setting.
	 */
	private boolean shouldFormatClosingBracketNewLine(DOMElement element) {
		boolean isSingleAttribute = element.getAttributeNodes() != null
				? element.getAttributeNodes().size() == 1
				: true;
		return (formatterDocument.getSharedSettings().getFormattingSettings().getClosingBracketNewLine()
				&& isSplitAttributes() && !isSingleAttribute);
	}

	private void replaceLeftSpacesWith(int offset, String replace, List<TextEdit> edits) {
		formatterDocument.replaceLeftSpacesWith(offset, replace, edits);
	}

	private void replaceLeftSpacesWithOneSpace(int offset, List<TextEdit> edits) {
		formatterDocument.replaceLeftSpacesWithOneSpace(offset, edits);
	}

	private int replaceLeftSpacesWithIndentation(int indentLevel, int offset, boolean addLineSeparator,
			List<TextEdit> edits) {
		return formatterDocument.replaceLeftSpacesWithIndentation(indentLevel, offset, addLineSeparator, edits);
	}

	private int replaceLeftSpacesWithIndentationWithMultiNewLines(int indentLevel, int offset, int newLineCount,
			List<TextEdit> edits) {
		return formatterDocument.replaceLeftSpacesWithIndentationWithMultiNewLines(indentLevel, offset, newLineCount,
				edits);
	}

	private void removeLeftSpaces(int to, List<TextEdit> edits) {
		formatterDocument.removeLeftSpaces(to, edits);
	}

	private void createTextEditIfNeeded(int from, int to, String expectedContent, List<TextEdit> edits) {
		formatterDocument.createTextEditIfNeeded(from, to, expectedContent, edits);
	}

	private boolean hasLineBreak(int end, int startTagCloseOffset) {
		return formatterDocument.hasLineBreak(end, startTagCloseOffset);
	}

	private DOMAttr getLastAttribute(DOMElement element) {
		if (!element.hasAttributes()) {
			return null;
		}
		List<DOMAttr> attributes = element.getAttributeNodes();
		return attributes.get(attributes.size() - 1);
	}

	private int getPreservedNewlines() {
		return formatterDocument.getSharedSettings().getFormattingSettings().getPreservedNewlines();
	}

	private boolean isPreserveAttributeLineBreaks() {
		return formatterDocument.getSharedSettings().getFormattingSettings().isPreserveAttributeLineBreaks();
	}

	private boolean isSplitAttributes() {
		return formatterDocument.getSharedSettings().getFormattingSettings().isSplitAttributes();
	}

	private int getSplitAttributesIndentSize() {
		return formatterDocument.getSharedSettings().getFormattingSettings().getSplitAttributesIndentSize();
	}

	private boolean isSpaceBeforeEmptyCloseTag() {
		return formatterDocument.getSharedSettings().getFormattingSettings().isSpaceBeforeEmptyCloseTag();
	}

	private EmptyElements getEmptyElements() {
		return formatterDocument.getSharedSettings().getFormattingSettings().getEmptyElements();
	}

	private boolean isPreserveEmptyContent() {
		return formatterDocument.getSharedSettings().getFormattingSettings().isPreserveEmptyContent();
	}

	private void formatChildren(DOMElement element, XMLFormattingConstraints constraints, int start, int end,
			List<TextEdit> edits) {
		formatterDocument.formatChildren(element, constraints, start, end, edits);
	}

	private FormatElementCategory getFormatElementCategory(DOMElement element,
			XMLFormattingConstraints parentConstraints) {
		return formatterDocument.getFormatElementCategory(element, parentConstraints);
	}

	private int getMaxLineWidth() {
		return formatterDocument.getMaxLineWidth();
	}
}
