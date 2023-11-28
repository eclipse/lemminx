/*******************************************************************************
 * Copyright (c) 2016-2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.lemminx.utils;

import static org.apache.commons.text.StringEscapeUtils.unescapeJava;
import static org.apache.commons.text.StringEscapeUtils.unescapeXml;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;

/**
 * Converts HTML content into Markdown equivalent.
 *
 * @author Fred Bricon
 */
public class MarkdownConverter {

	private static final FlexmarkHtmlConverter CONVERTER = FlexmarkHtmlConverter.builder().build();

	private MarkdownConverter() {
		// no public instanciation
	}

	public static String convert(String html) {
		if (!StringUtils.isTagOutsideOfBackticks(html)) {
			return unescapeXml(html); // is not html so it can be returned as-is (aside from unescaping)
		}

		Document document = Jsoup.parse(html);
		//Add missing table headers if necessary, else most Markdown renderers will crap out
		document.select("table").forEach(MarkdownConverter::addMissingTableHeaders);
		
		String markdown = CONVERTER.convert(document);
		if (markdown.endsWith("\n")) {// FlexmarkHtmlConverter keeps adding an extra line
			markdown = markdown.substring(0, markdown.length() - 1);
		}
		return unescapeJava(markdown);
	}

	/**
	 * Adds a new row header if the given table doesn't have any.
	 * @param table the HTML table to check for a header
	 */
	private static void addMissingTableHeaders(Element table) {
		int numCols = 0;
		for (Element child : table.children()) {
			if ("thead".equals(child.nodeName())) {
				// Table already has a header, nothing else to do
				return;
			}
			if ("tbody".equals(child.nodeName())) {
				Elements rows = child.getElementsByTag("tr");
				if (!rows.isEmpty()) {
					for (Element row : rows) {
						int colSize = row.getElementsByTag("td").size();
						//Keep the biggest column size
						if (colSize > numCols) {
							numCols = colSize;
						}
					}
				}
			}
		}
		if (numCols > 0) {
			//Create a new header row based on the number of columns already found
			Element newHeader = new Element("tr");
			for (int i = 0; i < numCols; i++) {
				newHeader.appendChild(new Element("th"));
			}
			//Insert header row in 1st position in the table
			table.insertChildren(0, newHeader);
		}
	}
}