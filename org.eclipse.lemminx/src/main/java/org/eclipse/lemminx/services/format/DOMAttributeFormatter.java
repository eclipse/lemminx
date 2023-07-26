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

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.settings.EnforceQuoteStyle;
import org.eclipse.lemminx.settings.XMLFormattingOptions.SplitAttributes;
import org.eclipse.lemminx.utils.StringUtils;
import org.eclipse.lsp4j.TextEdit;

/**
 * DOM attribute formatter.
 * 
 * @author Angelo ZERR
 *
 */
public class DOMAttributeFormatter {

	private final XMLFormatterDocument formatterDocument;

	public DOMAttributeFormatter(XMLFormatterDocument formatterDocument) {
		this.formatterDocument = formatterDocument;
	}

	public void formatAttribute(DOMAttr attr, int prevOffset, boolean singleAttribute, boolean useSettings,
			boolean isFirstAttr,
			XMLFormattingConstraints parentConstraints, List<TextEdit> edits) {
		int indentLevel = parentConstraints.getIndentLevel();
		// 1) format before attribute name : indent left of the attribute name
		// ex : <foo[space][space]attr=""
		// --> <foo[space]attr=""
		boolean alreadyIndented = false;
		if (useSettings) {
			// move the attribute to a new line and indent it
			if (isPreserveAttributeLineBreaks() && hasLineBreak(prevOffset, attr.getStart())) {
				replaceLeftSpacesWithIndentation(indentLevel + 1, prevOffset, attr.getStart(), true, edits);
				alreadyIndented = true;
			} else if (getSplitAttributes() == SplitAttributes.splitNewLine && !singleAttribute) {
				replaceLeftSpacesWithIndentation(indentLevel + getSplitAttributesIndentSize(), prevOffset,
						attr.getStart(), true, edits);
				alreadyIndented = true;
			} else if (getSplitAttributes() == SplitAttributes.alignWithFirstAttr && !isFirstAttr) {
				replaceLeftSpacesWithIndentationWithOffsetSpaces(getFirstAttrOffset(attr.getOwnerElement(), indentLevel), prevOffset,
						attr.getStart(), edits);
				alreadyIndented = true;
			}
		}

		// 2) format delimiter : remove whitespaces between '='
		// ex : <foo attr = ""
		// --> <foo attr=""
		int attributeNamelength = 0;
		if (attr.hasDelimiter()) {
			int delimiterOffset = attr.getDelimiterOffset(); // <foo attr =| ""

			// 2.1 Remove extra spaces between end of attribute name and delimiter
			int attrNameEnd = attr.getNodeAttrName().getEnd(); // <foo attr| = ""
			removeLeftSpaces(attrNameEnd, delimiterOffset, edits);

			if (attr.getNodeAttrValue() != null) {
				// 2.2 Remove extra spaces between delimiter and start of attribute value
				int attrValueStart = attr.getNodeAttrValue().getStart(); // <foo attr = |""
				removeLeftSpaces(delimiterOffset, attrValueStart, edits);
			}

			// Compute max line width for attribute name and indents if maxLineWidth is
			// enabled
			if (isMaxLineWidthSupported()) {
				int availableLineWidth = parentConstraints.getAvailableLineWidth();
				if (isPreserveAttributeLineBreaks() && hasLineBreak(prevOffset, attr.getStart())) {
					availableLineWidth = getMaxLineWidth() - getTabSize() * (indentLevel + 1);
				} else if (getSplitAttributes() == SplitAttributes.splitNewLine && !singleAttribute) {
					availableLineWidth = getMaxLineWidth()
							- getTabSize() * (indentLevel + getSplitAttributesIndentSize());
				} else {
					// counts the space between the start tag name and attribute value
					availableLineWidth--;
				}
				// Add width for length of attribute name and 3 for '=""'
				// between start tag name and tag
				attributeNamelength = attrNameEnd - attr.getNodeAttrName().getStart() + 3;
				parentConstraints.setAvailableLineWidth(availableLineWidth - attributeNamelength);
			}
			formatAttributeValue(attr, parentConstraints, edits);
		}

		if (!alreadyIndented) {
			int from = prevOffset;
			int to = attr.getStart();
			if (isMaxLineWidthSupported() && parentConstraints.getAvailableLineWidth() < 0
					&& getSplitAttributes() == SplitAttributes.preserve) {
				replaceLeftSpacesWithIndentation(indentLevel + 1, from, to, true, edits);
				int attrValuelength = attr.getValue() != null ? attr.getValue().length() : 0;
				parentConstraints.setAvailableLineWidth(
						getMaxLineWidth() - getTabSize() * (indentLevel + 1) - attributeNamelength
								- attrValuelength);
			} else {
				// remove extra whitespaces between previous attribute
				// attr0='name'[space][space][space]attr1='name' -->
				// attr0='name'[space]attr1='name'

				// Adjust the startAttr to avoid ignoring invalid content
				// ex : <asdf |""`=asdf />
				// must be adjusted with <asdf ""|`=asdf /> to keep the invalid content ""
				replaceLeftSpacesWithOneSpace(from, to, edits);
			}
		}

		// replace current quote with preferred quote in case of attribute value
		// ex: if preferred quote is single quote (')
		// <a name="value"> </a>
		// --> <a name='value'> </a>
		String originalValue = attr.getOriginalValue();
		if (getEnforceQuoteStyle() == EnforceQuoteStyle.preferred && originalValue != null) {
			if (originalValue.charAt(0) != getQuotationAsChar() && StringUtils.isQuote(originalValue.charAt(0))) {
				replaceQuoteWithPreferred(attr.getNodeAttrValue().getStart(), attr.getNodeAttrValue().getStart() + 1,
						edits);
			}
			if (originalValue.charAt(originalValue.length() - 1) != getQuotationAsChar()
					&& StringUtils.isQuote(originalValue.charAt(originalValue.length() - 1))) {
				replaceQuoteWithPreferred(attr.getNodeAttrValue().getEnd() - 1, attr.getNodeAttrValue().getEnd(),
						edits);
			}
		}
	}

	private int getFirstAttrOffset(DOMElement ownerElement, int indentLevel) {
		return getTabSize() * indentLevel + ownerElement.getTagName().length() + 2 /*
																					 * +1 for '<', +1 for space between
																					 * element name and first attr name
																					 */;
	}

	private void formatAttributeValue(DOMAttr attr, XMLFormattingConstraints parentConstraints, List<TextEdit> edits) {
		formatterDocument.formatAttributeValue(attr, parentConstraints, edits);
	}

	private void replaceQuoteWithPreferred(int from, int to, List<TextEdit> edits) {
		formatterDocument.replaceQuoteWithPreferred(from, to, edits);
	}

	private void replaceLeftSpacesWithIndentationWithOffsetSpaces(int spaceCount, int from, int to,
			List<TextEdit> edits) {
		formatterDocument.replaceLeftSpacesWithIndentationWithOffsetSpaces(spaceCount, from, to, true, edits);
	}

	private void replaceLeftSpacesWithOneSpace(int from, int to, List<TextEdit> edits) {
		formatterDocument.replaceLeftSpacesWithOneSpace(from, to, edits);
	}

	private void replaceLeftSpacesWithIndentation(int indentLevel, int leftLimit, int to, boolean addLineSeparator,
			List<TextEdit> edits) {
		formatterDocument.replaceLeftSpacesWithIndentation(indentLevel, leftLimit, to, addLineSeparator, edits);
	}

	private void removeLeftSpaces(int from, int to, List<TextEdit> edits) {
		formatterDocument.removeLeftSpaces(from, to, edits);
	}

	private SplitAttributes getSplitAttributes() {
		return formatterDocument.getSharedSettings().getFormattingSettings().getSplitAttributes();
	}

	private int getSplitAttributesIndentSize() {
		return formatterDocument.getSharedSettings().getFormattingSettings().getSplitAttributesIndentSize();
	}

	boolean isPreserveAttributeLineBreaks() {
		return formatterDocument.getSharedSettings().getFormattingSettings().isPreserveAttributeLineBreaks();
	}

	private boolean hasLineBreak(int prevOffset, int start) {
		return formatterDocument.hasLineBreak(prevOffset, start);
	}

	private char getQuotationAsChar() {
		return formatterDocument.getSharedSettings().getPreferences().getQuotationAsChar();
	}

	private EnforceQuoteStyle getEnforceQuoteStyle() {
		return formatterDocument.getSharedSettings().getFormattingSettings().getEnforceQuoteStyle();
	}

	private int getTabSize() {
		return formatterDocument.getSharedSettings().getFormattingSettings().getTabSize();
	}

	private int getMaxLineWidth() {
		return formatterDocument.getMaxLineWidth();
	}

	private boolean isMaxLineWidthSupported() {
		return formatterDocument.isMaxLineWidthSupported();
	}
}
