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
package org.eclipse.xml.languageserver.utils;

import org.eclipse.lsp4j.FormattingOptions;

/**
 * XML content builder utilities.
 *
 */
public class XMLBuilder {

	private final FormattingOptions formattingOptions;
	private final String whitespacesIndent;
	private final String lineDelimiter;
	private final StringBuilder xml;

	public XMLBuilder(FormattingOptions formattingOptions, String whitespacesIndent, String lineDelimiter) {
		this.formattingOptions = formattingOptions;
		this.whitespacesIndent = whitespacesIndent;
		this.lineDelimiter = lineDelimiter;
		this.xml = new StringBuilder();
	}

	public XMLBuilder startElement(String name, boolean close) {
		xml.append("<");
		xml.append(name);
		if (close) {
			closeStartElement();
		}
		return this;
	}

	public XMLBuilder endElement(String name) {
		xml.append("</");
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

	public XMLBuilder addAttribute(String name, String value) {
		xml.append(" ");
		xml.append(name);
		xml.append("=\"");
		xml.append(value);
		xml.append("\"");
		return this;
	}

	public XMLBuilder linefeed() {
		xml.append(lineDelimiter);
		xml.append(whitespacesIndent);
		return this;
	}

	public XMLBuilder addContent(String text) {
		xml.append(text);
		return this;
	}

	public XMLBuilder indent(int level) {
		for (int i = 0; i < level; i++) {
			if (formattingOptions.isInsertSpaces()) {
				for (int j = 0; j < formattingOptions.getTabSize(); j++) {
					xml.append(" ");
				}
			} else {
				xml.append("\t");
			}
		}
		return this;
	}

	@Override
	public String toString() {
		return xml.toString();
	}
}
