/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.utils;

import static org.eclipse.lsp4xml.utils.StringUtils.normalizeSpace;
import static org.eclipse.lsp4xml.utils.StringUtils.trimNewLines;

import org.eclipse.lsp4xml.dom.Comment;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;

/**
 * XML content builder utilities.
 *
 */
public class XMLBuilder {

	private final XMLFormattingOptions formattingOptions;
	private final String lineDelimiter;
	private final StringBuilder xml;
	private final String whitespacesIndent;

	public XMLBuilder(XMLFormattingOptions formattingOptions, String whitespacesIndent, String lineDelimiter) {
		this.whitespacesIndent = whitespacesIndent;
		this.formattingOptions = formattingOptions;
		this.lineDelimiter = lineDelimiter;
		this.xml = new StringBuilder();
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

	public XMLBuilder endElement(String name) {
		return endElement(null, name);
	}

	public XMLBuilder endElement(String prefix, String name) {
		xml.append("</");
		if (prefix != null && !prefix.isEmpty()) {
			xml.append(prefix);
			xml.append(":");
		}
		xml.append(name);
		xml.append(">");
		return this;
	}

	public XMLBuilder closeStartElement() {
		xml.append(">");
		return this;
	}

	public XMLBuilder endElement() {
		xml.append(" />");
		return this;
	}

	public XMLBuilder addSingleAttribute(String name, String value) {
		xml.append(" ");
		xml.append(name);
		xml.append("=\"");
		xml.append(value);
		xml.append("\"");
		return this;
	}

	public XMLBuilder addAttribute(String name, String value, int index, int level, String tagName) {
		int spaces = 1; //Accounts for '<' if isSplitAttributes() or space for single attribute 
		if (isSplitAttributes()) {
			linefeed();
			indent(level);
			spaces += tagName.length();
		}

		for (int i = 0; i < spaces; i++) {
			xml.append(" ");
		}

		xml.append(name);
		xml.append("=\"");
		if (value != null) {
			xml.append(value);
		}
		xml.append("\"");
		return this;
	}

	public XMLBuilder linefeed() {
		xml.append(lineDelimiter);
		if (whitespacesIndent != null) {
			xml.append(whitespacesIndent);
		}
		return this;
	}

	public XMLBuilder addContent(String text) {
		if (isJoinContentLines()) {
			normalizeSpace(text, xml);
		} else {
			xml.append(text);
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

	public XMLBuilder startComment(Comment comment) {
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
		} else {
			xml.append(content);
		}
		return this;
	}

	public XMLBuilder startDoctype() {
		xml.append("<!DOCTYPE");
		return this;
	}

	public XMLBuilder addContentDoctype(String content) {
		xml.append(content);
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

	public boolean isJoinContentLines() {
		return formattingOptions != null && formattingOptions.isJoinContentLines();
	}
}
