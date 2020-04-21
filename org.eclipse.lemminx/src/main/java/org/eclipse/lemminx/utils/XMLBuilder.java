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

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMComment;
import org.eclipse.lemminx.dom.DTDDeclNode;
import org.eclipse.lemminx.settings.XMLFormattingOptions;

/**
 * XML content builder utilities.
 *
 */
public class XMLBuilder {

	private final XMLFormattingOptions formattingOptions;
	private final String lineDelimiter;
	private final StringBuilder xml;
	private final String whitespacesIndent;
	private final int splitAttributesIndent = 2;

	public XMLBuilder(XMLFormattingOptions formattingOptions, String whitespacesIndent, String lineDelimiter) {
		this.whitespacesIndent = whitespacesIndent;
		this.formattingOptions = formattingOptions != null ? formattingOptions : new XMLFormattingOptions(true);
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
		if (formattingOptions.isSpaceBeforeEmptyCloseTag()) {
			xml.append(" ");
		}
		xml.append("/>");
		return this;
	}

	public XMLBuilder addSingleAttribute(String name, String value) {
		return addSingleAttribute(name, value, false);
	}

	/**
	 * Used when only one attribute is being added to a node.
	 * 
	 * It will not perform any linefeeds and only basic indentation.
	 * 
	 * @param name  attribute name
	 * @param value attribute value
	 * @return
	 */
	public XMLBuilder addSingleAttribute(String name, String value, boolean surroundWithQuotes) {
		xml.append(" ");
		addAttributeContents(name, true, value, surroundWithQuotes);

		return this;
	}

	public XMLBuilder addAttributesOnSingleLine(DOMAttr attr, boolean surroundWithQuotes) {
		xml.append(" ");
		addAttributeContents(attr.getName(), attr.hasDelimiter(), attr.getValue(), surroundWithQuotes);

		return this;
	}

	public XMLBuilder addAttribute(String name, String value, int level) {
		return addAttribute(name, value, level, false);
	}

	/**
	 * Used when you are knowingly adding multiple attributes.
	 * 
	 * It will do linefeeds and indentation.
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
			xml.append(" ");
		}

		addAttributeContents(name, true, value, surroundWithQuotes);
		return this;
	}

	public XMLBuilder addAttribute(DOMAttr attr, int level) {
		return addAttribute(attr, level, false);
	}

	public XMLBuilder addAttribute(DOMAttr attr, int level, boolean surroundWithQuotes) {
		if (isSplitAttributes()) {
			linefeed();
			indent(level + splitAttributesIndent);
		} else {
			xml.append(" ");
		}

		addAttributeContents(attr.getName(), attr.hasDelimiter(), attr.getOriginalValue(), surroundWithQuotes);
		return this;
	}

	 /**
	  * * Builds the attribute {name, '=', and value}.
	  * 
	  * Never puts quotes around unquoted values unless indicated to by
	  * 'surroundWithQuotes'
	  * 
	  * @param name               name of the attribute
	  * @param equalsSign         true if equals sign exists, false otherwise
	  * @param originalValue      value of the attribute
	  * @param surroundWithQuotes true if value should be added with quotes, false otherwise
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
			String quote = formattingOptions.isQuotations(XMLFormattingOptions.DOUBLE_QUOTES_VALUE) ? "\"" : "'";

			if (DOMAttr.isQuoted(originalValue)) {
				if (originalValue.charAt(0) == '\''
						&& formattingOptions.isQuotations(XMLFormattingOptions.DOUBLE_QUOTES_VALUE)
						|| originalValue.charAt(0) == '\"'
								&& formattingOptions.isQuotations(XMLFormattingOptions.SINGLE_QUOTES_VALUE)) {

					originalValue = DOMAttr.convertToQuotelessValue(originalValue);
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
	 * @param isWhitespaceContent whether or not the text contains whitespace content
	 * @param hasSiblings         whether or not the corresponding text node has siblings
	 * @param delimiter           line delimiter
	 * @return this XMLBuilder with <code>text</code> added depending on
	 * <code>isWhitespaceContent</code>, <code>hasSiblings</code> and
	 * <code>delimiter</code>
	 */
	public XMLBuilder addContent(String text, boolean isWhitespaceContent, boolean hasSiblings,
			String delimiter) {
		if (!isWhitespaceContent) {
			if (isJoinContentLines()) {
				text = StringUtils.normalizeSpace(text);
			} else if (hasSiblings) {
				text = text.trim();
			}
			xml.append(text);
		} else if (!hasSiblings && isPreserveEmptyContent()) {
			xml.append(text);
		} else if (hasSiblings) {
			int preservedNewLines = getPreservedNewlines();
			if (preservedNewLines > 0) {
				int newLineCount = StringUtils.getNumberOfNewLines(text, isWhitespaceContent, delimiter,
						preservedNewLines);
				for (int i = 0; i < newLineCount - 1; i++) { // - 1 because the node after will insert a delimiter
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
					xml.append(" ");
				}
			} else {
				xml.append("\t");
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
		xml.append(" ");
		xml.append(content);
		xml.append(" ");
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
	 * Trims the trailing newlines for the current xml StringBuilder
	 */
	public void trimFinalNewlines() {
		int i = xml.length() - 1;
		while (i >= 0 && Character.isWhitespace(xml.charAt(i))) {
			xml.deleteCharAt(i--);
		}
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
			xml.append(" ");
		}
		xml.append("<!--");
		return this;
	}

	public XMLBuilder addContentComment(String content) {
		if (isJoinCommentLines()) {
			xml.append(" ");
			xml.append(normalizeSpace(content));
			xml.append(" ");
		} else {
			xml.append(content);
		}
		return this;
	}

	public XMLBuilder addDeclTagStart(DTDDeclNode tag) {

		xml.append("<!" + tag.getDeclType());
		return this;
	}

	public XMLBuilder startDoctype() {
		xml.append("<!DOCTYPE");
		return this;
	}

	public XMLBuilder addParameter(String parameter) {
		xml.append(" " + parameter);
		return this;
	}

	public XMLBuilder addUnindentedParameter(String parameter) {
		xml.append(parameter);
		return this;
	}

	public XMLBuilder startDoctypeInternalSubset() {
		xml.append(" [");
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

	private boolean isJoinCommentLines() {
		return formattingOptions != null && formattingOptions.isJoinCommentLines();
	}

	private boolean isJoinCDATALines() {
		return formattingOptions != null && formattingOptions.isJoinCDATALines();
	}

	private boolean isSplitAttributes() {
		return formattingOptions != null && formattingOptions.isSplitAttributes();
	}

	private boolean isInsertSpaces() {
		return formattingOptions != null && formattingOptions.isInsertSpaces();
	}

	private int getTabSize() {
		return formattingOptions != null ? formattingOptions.getTabSize() : 0;
	}

	private boolean isJoinContentLines() {
		return formattingOptions != null && formattingOptions.isJoinContentLines();
	}

	private boolean isPreserveEmptyContent() {
		return formattingOptions != null && formattingOptions.isPreserveEmptyContent();
	}

	private int getPreservedNewlines() {
		if (formattingOptions != null) {
			return formattingOptions.getPreservedNewlines();
		}
		return XMLFormattingOptions.DEFAULT_PRESERVER_NEW_LINES;
	}

}
