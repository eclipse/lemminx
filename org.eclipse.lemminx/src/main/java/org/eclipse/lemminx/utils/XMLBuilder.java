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

import static org.eclipse.lemminx.utils.StringUtils.normalizeSpace;
import static org.eclipse.lemminx.utils.StringUtils.normalizeSpace2;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMComment;
import org.eclipse.lemminx.dom.DTDDeclNode;
import org.eclipse.lemminx.settings.EnforceQuoteStyle;
import org.eclipse.lemminx.settings.SharedSettings;

/**
 * XML content builder utilities.
 *
 */
public class XMLBuilder {

	private final SharedSettings sharedSettings;
	private final String lineDelimiter;
	private final StringBuilder xml;
	private final String whitespacesIndent;
	private final int splitAttributesIndent = 2;

	public XMLBuilder(SharedSettings sharedSettings, String whitespacesIndent, String lineDelimiter) {
		this.whitespacesIndent = whitespacesIndent;
		this.sharedSettings = sharedSettings;
		this.lineDelimiter = lineDelimiter;
		this.xml = new StringBuilder();
	}

	public XMLBuilder appendSpace() {
		xml.append(" ");
		return this;
	}

	public XMLBuilder startElement(String prefix, String name, boolean close) {
		xml.append("<");
		if (prefix != null && !prefix.isEmpty()) {
			xml.append(prefix);
			xml.append(":");
		}
		xml.append(name);
		if (close) {
			closeStartElement();
		}
		return this;
	}

	public XMLBuilder startElement(String name, boolean close) {
		return startElement(null, name, close);
	}

	public XMLBuilder endElement(String name, boolean isEndTagClosed) {
		return endElement(null, name, isEndTagClosed);
	}

	public XMLBuilder endElement(String name) {
		return endElement(null, name, true);
	}

	public XMLBuilder endElement(String prefix, String name) {
		return endElement(prefix, name, true);
	}

	public XMLBuilder endElement(String prefix, String name, boolean isEndTagClosed) {
		xml.append("</");
		if (prefix != null && !prefix.isEmpty()) {
			xml.append(prefix);
			xml.append(":");
		}
		xml.append(name);
		if (isEndTagClosed) {
			xml.append(">");
		}
		return this;
	}

	public XMLBuilder closeStartElement() {
		xml.append(">");
		return this;
	}

	public XMLBuilder selfCloseElement() {
		if (isSpaceBeforeEmptyCloseTag() && !isLastLineEmptyOrWhitespace()) {
			appendSpace();
		}
		xml.append("/>");
		return this;
	}

	public XMLBuilder addSingleAttribute(DOMAttr attr) {
		return addSingleAttribute(attr, false, true);
	}

	public XMLBuilder addSingleAttribute(DOMAttr attr, boolean surroundWithQuotes, boolean prependSpace) {
		return addSingleAttribute(attr.getName(), attr.getOriginalValue(), surroundWithQuotes, prependSpace);
	}

	public XMLBuilder addSingleAttribute(String name, String value, boolean surroundWithQuotes) {
		return addSingleAttribute(name, value, surroundWithQuotes, true);
	}

	/**
	 * Used when only one attribute is being added to a node.
	 *
	 * It will not perform any linefeeds and only basic indentation.
	 *
	 * @param name               attribute name
	 * @param value              attribute value
	 * @param surroundWithQuotes true if quotes should be added around originalValue
	 * @return this XML Builder
	 */
	public XMLBuilder addSingleAttribute(String name, String value, boolean surroundWithQuotes, boolean prependSpace) {
		if (prependSpace) {
			appendSpace();
		}
		addAttributeContents(name, true, value, surroundWithQuotes);
		return this;
	}

	/**
	 * Add prolog attribute
	 *
	 * It will not perform any linefeeds and only basic indentation.
	 *
	 * @param attr attribute
	 * @return this XML Builder
	 */
	public XMLBuilder addPrologAttribute(DOMAttr attr) {
		appendSpace();
		addAttributeContents(attr.getName(), attr.hasDelimiter(), attr.getOriginalValue(), false);
		return this;
	}

	/**
	 * Used when you are knowingly adding multiple attributes.
	 *
	 * Does linefeeds and indentation.
	 *
	 * @param name
	 * @param value
	 * @param level
	 * @return
	 */
	public XMLBuilder addAttribute(String name, String value, int level, boolean surroundWithQuotes) {
		if (isSplitAttributes()) {
			linefeed();
			indent(level + splitAttributesIndent);
		} else {
			appendSpace();
		}

		addAttributeContents(name, true, value, surroundWithQuotes);
		return this;
	}

	public XMLBuilder addAttribute(DOMAttr attr, int level) {
		return addAttribute(attr, level, false);
	}

	private XMLBuilder addAttribute(DOMAttr attr, int level, boolean surroundWithQuotes) {
		if (isSplitAttributes()) {
			linefeed();
			indent(level + splitAttributesIndent);
		} else {
			appendSpace();
		}

		addAttributeContents(attr.getName(), attr.hasDelimiter(), attr.getOriginalValue(), surroundWithQuotes);
		return this;
	}

	/**
	 * Builds the attribute {name, '=', and value}.
	 *
	 * Never puts quotes around unquoted values unless indicated to by
	 * 'surroundWithQuotes'
	 *
	 * @param name               name of the attribute
	 * @param equalsSign         true if equals sign exists, false otherwise
	 * @param originalValue      value of the attribute
	 * @param surroundWithQuotes true if quotes should be added around
	 *                           originalValue, false otherwise
	 */
	private void addAttributeContents(String name, boolean equalsSign, String originalValue,
			boolean surroundWithQuotes) {
		if (name != null) {
			xml.append(name);
		}
		if (equalsSign) {
			xml.append("=");
		}
		if (originalValue != null) {
			char quote = getQuotationAsChar();

			if (StringUtils.isQuoted(originalValue)) {
				if (getEnforceQuoteStyle() == EnforceQuoteStyle.preferred && originalValue.charAt(0) != quote) {

					originalValue = StringUtils.convertToQuotelessValue(originalValue);
					xml.append(quote);
					if (originalValue != null) {
						xml.append(originalValue);
					}
					xml.append(quote);
					return;
				} else {
					xml.append(originalValue);
					return;
				}
			} else if (surroundWithQuotes) {
				xml.append(quote);
				if (originalValue != null) {
					xml.append(originalValue);
				}
				xml.append(quote);
				return;
			} else {
				xml.append(originalValue);
			}
		}
	}

	public XMLBuilder linefeed() {
		xml.append(lineDelimiter);
		if (whitespacesIndent != null) {
			xml.append(whitespacesIndent);
		}
		return this;
	}

	/**
	 * Returns this XMLBuilder with <code>text</code> added
	 *
	 * @param text the text to add
	 * @return this XMLBuilder with <code>text</code> added
	 */
	public XMLBuilder addContent(String text) {
		return addContent(text, false, false, null);
	}

	/**
	 * Returns this XMLBuilder with <code>text</code> added depending on
	 * <code>isWhitespaceContent</code>, <code>hasSiblings</code> and
	 * <code>delimiter</code>
	 *
	 * @param text                the proposed text to add
	 * @param isWhitespaceContent whether or not the text contains only whitespace
	 *                            content
	 * @param hasSiblings         whether or not the corresponding text node has
	 *                            siblings
	 * @param delimiter           line delimiter
	 * @return this XMLBuilder with <code>text</code> added depending on
	 *         <code>isWhitespaceContent</code>, <code>hasSiblings</code> and
	 *         <code>delimiter</code>
	 */
	public XMLBuilder addContent(String text, Boolean isWhitespaceContent, Boolean hasSiblings, String delimiter) {
        if (isWhitespaceContent) {
			// whoah: terrible, but this one seems to preserve single space.
			if (text.length() == 1) {
				// xml.append("+");
				xml.append(text);
			}
		}
	    // "compatible" if() follow, changes are based on basically no understanding what's really going on here.
	    if (!isWhitespaceContent) {
		    if(isJoinContentLines()) {
			    // ATSEC
			    //xml.append("A:");
			    text = normalizeSpace2(text);
			} else if (hasSiblings) {
				//xml.append("B:");
				text = text.trim();
				//	} else {
				// 	xml.append("C:");
			}
			if (isTrimTrailingWhitespace()) {
				// XXX: Investigate, the setting shoulÌd be false...
				// text = trimTrailingSpacesEachLine(text);
			}
		    //xml.append("-["+text+"]-");
		    xml.append(text);
        } else if (!hasSiblings && isPreserveEmptyContent()) {
		    xml.append(text);
        } else if (hasSiblings) {
		    int preservedNewLines = getPreservedNewlines();
		    if (preservedNewLines > 0) {
			    int newLineCount = StringUtils.getNumberOfNewLines(text, isWhitespaceContent, delimiter, preservedNewLines);
			    for (int i = 0; i < newLineCount - 1; i++) { // - 1 because the node after will insert a delimiter
				    //xml.append("+");
				    xml.append(delimiter);
			    }
		    }
        }
            return this;
	}

	public XMLBuilder indent(int level) {
		for (int i = 0; i < level; i++) {
			if (isInsertSpaces()) {
				for (int j = 0; j < getTabSize(); j++) {
					appendSpace();
				}
			} else {
				xml.append("\t");
				//xml.append("y");
			}
		}
		return this;
	}

	public XMLBuilder startPrologOrPI(String tagName) {
		xml.append("<?");
		xml.append(tagName);
		return this;
	}

	public XMLBuilder addContentPI(String content) {
		appendSpace();
		xml.append(content);
		appendSpace();
		return this;
	}

	public XMLBuilder endPrologOrPI() {
		xml.append("?>");
		return this;
	}

	@Override
	public String toString() {
		return xml.toString();
	}

	/**
	 * Trims the trailing newlines for the current XML StringBuilder
	 */
	public void trimFinalNewlines() {
		int i = xml.length() - 1;
		while (i >= 0 && Character.isWhitespace(xml.charAt(i))) {
			xml.deleteCharAt(i--);
		}
	}

	/**
	 * Returns <code>str</code> with the trailing spaces from each line removed
	 *
	 * @param str the String
	 * @return <code>str</code> with the trailing spaces from each line removed
	 */
	private static String trimTrailingSpacesEachLine(String str) {
		StringBuilder sb = new StringBuilder(str);
		int i = str.length() - 1;
		boolean removeSpaces = true;
		while (i >= 0) {
			char curr = sb.charAt(i);
			if (curr == '\n' || curr == '\r') {
				removeSpaces = true;
			} else if (removeSpaces && Character.isWhitespace(curr)) {
				sb.deleteCharAt(i);
			} else {
				removeSpaces = false;
			}
			i--;
		}
		return sb.toString();
	}

	public XMLBuilder startCDATA() {
		xml.append("<![CDATA[");
		return this;
	}

	public XMLBuilder addContentCDATA(String content) {
		if (isJoinCDATALines()) {
			content = normalizeSpace(content);
		}
		xml.append(content);
		return this;
	}

	public XMLBuilder endCDATA() {
		xml.append("]]>");
		return this;
	}

	public XMLBuilder startComment(DOMComment comment) {
		if (comment.isCommentSameLineEndTag()) {
			appendSpace();
		}
		xml.append("<!--");
		return this;
	}

	public XMLBuilder addContentComment(String content) {
		if (isJoinCommentLines()) {
			appendSpace();
			xml.append(normalizeSpace(content));
			appendSpace();
		} else {
			xml.append(content);
		}
		return this;
	}

	public XMLBuilder addDeclTagStart(DTDDeclNode tag) {
		xml.append("<!" + tag.getDeclType());
		return this;
	}

	public XMLBuilder addDeclTagStart(String declTagName) {
		xml.append("<!" + declTagName);
		return this;
	}

	public XMLBuilder startDoctype() {
		xml.append("<!DOCTYPE");
		return this;
	}

	public XMLBuilder startDTDElementDecl() {
		xml.append("<!ELEMENT");
		return this;
	}

	public XMLBuilder startDTDAttlistDecl() {
		xml.append("<!ATTLIST");
		return this;
	}

	public XMLBuilder addParameter(String parameter) {
		return addUnindentedParameter(" " + replaceQuotesIfNeeded(parameter));
	}

	public XMLBuilder addUnindentedParameter(String parameter) {
		xml.append(replaceQuotesIfNeeded(parameter));
		return this;
	}

	public XMLBuilder startDoctypeInternalSubset() {
		xml.append(" [");
		return this;
	}

	public XMLBuilder startUnindentedDoctypeInternalSubset() {
		xml.append("[");
		return this;
	}

	public XMLBuilder endDoctypeInternalSubset() {
		xml.append("]");
		return this;
	}

	public XMLBuilder endComment() {
		xml.append("-->");
		return this;
	}

	public XMLBuilder endDoctype() {
		xml.append(">");
		return this;
	}

	public boolean isLastLineEmptyOrWhitespace() {
		if (this.xml.length() == 0) {
			return true;
		}
		int i = this.xml.length() - 1;
		while (i > 0 && Character.isSpaceChar(this.xml.charAt(i))) {
			i--;
		}
		return i > 0 && (this.xml.charAt(i) == '\r' || this.xml.charAt(i) == '\n');
	}

	private String replaceQuotesIfNeeded(String str) {
		if (getEnforceQuoteStyle() != EnforceQuoteStyle.preferred) {
			return str;
		}
		if (StringUtils.isQuoted(str)) {
			String quote = getQuotationAsString();
			return quote + StringUtils.convertToQuotelessValue(str) + quote;
		}
		return str;
	}

	private EnforceQuoteStyle getEnforceQuoteStyle() {
		return this.sharedSettings.getFormattingSettings().getEnforceQuoteStyle();
	}

	private String getQuotationAsString() {
		return this.sharedSettings.getPreferences().getQuotationAsString();
	}

	private boolean isJoinCommentLines() {
		return sharedSettings.getFormattingSettings().isJoinCommentLines();
	}

	private boolean isJoinCDATALines() {
		return sharedSettings.getFormattingSettings().isJoinCDATALines();
	}

	private boolean isSplitAttributes() {
		return sharedSettings.getFormattingSettings().isSplitAttributes();
	}

	private boolean isInsertSpaces() {
		return sharedSettings.getFormattingSettings().isInsertSpaces();
	}

	private int getTabSize() {
		return sharedSettings.getFormattingSettings().getTabSize();
	}

	private boolean isJoinContentLines() {
		return sharedSettings.getFormattingSettings().isJoinContentLines();
	}

	private boolean isPreserveEmptyContent() {
		return sharedSettings.getFormattingSettings().isPreserveEmptyContent();
	}

	private boolean isTrimTrailingWhitespace() {
		return sharedSettings.getFormattingSettings().isTrimTrailingWhitespace();
	}

	private int getPreservedNewlines() {
		return sharedSettings.getFormattingSettings().getPreservedNewlines();
	}

	private boolean isSpaceBeforeEmptyCloseTag() {
		return sharedSettings.getFormattingSettings().isSpaceBeforeEmptyCloseTag();
	}

	private char getQuotationAsChar() {
		return sharedSettings.getPreferences().getQuotationAsChar();
	}

}

// Local Variables:
// tab-width: 8
// indent-tabs-mode: t
// c-basic-offset: 8
// End:
