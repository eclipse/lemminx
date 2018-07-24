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
package org.eclipse.lsp4xml.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.internal.parser.XMLParser;
import org.eclipse.lsp4xml.model.Node;
import org.eclipse.lsp4xml.model.XMLDocument;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.utils.XMLBuilder;

/**
 * XML formatter support.
 *
 */
class XMLFormatter {
	private static final Logger LOGGER = Logger.getLogger(XMLFormatter.class.getName());
	private final XMLExtensionsRegistry extensionsRegistry;
	public XMLFormatter(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public List<? extends TextEdit> format(TextDocument document, Range range, FormattingOptions formattingOptions) {
		try {
			// Compute start/end offset range
			int start = -1;
			int end = -1;
			if (range == null) {
				start = 0;
				end = document.getText().length();
			} else {
				start = document.offsetAt(range.getStart());
				end = document.offsetAt(range.getEnd());
			}
			Position startPosition = document.positionAt(start);
			Position endPosition = document.positionAt(end);

			// Parse the content to format to create an XML document with full data (CData,
			// comments, etc)
			String text = document.getText().substring(start, end);
			XMLDocument doc = XMLParser.getInstance().parse(text, null, true);

			// Format the content
			XMLBuilder xml = new XMLBuilder(formattingOptions, "", document.lineDelimiter(startPosition.getLine()));
			format(doc, 0, xml);

			// Returns LSP list of TextEdits
			Range r = new Range(startPosition, endPosition);
			List<TextEdit> edits = new ArrayList<>();
			edits.add(new TextEdit(r, xml.toString()));
			return edits;

		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Formatting failed due to BadLocation from 'range' parameter", e);
		}
		return null;
	}

	private void format(Node node, int level, XMLBuilder xml) {
		if (node.tag != null) {
			// element to format
			if (level > 0) {
				// add new line + indent
				xml.linefeed();
				xml.indent(level);
			}
			// generate start element
			xml.startElement(node.tag, false);
			if (node.attributes != null) {
				// generate attributes
				Set<String> attributeNames = node.attributeNames();
				for (String attributeName : attributeNames) {
					xml.addAttribute(attributeName, node.getAttributeValue(attributeName));
				}
			}
			if (!node.children.isEmpty()) {
				// element has body
				xml.closeStartElement();
				level++;
				boolean hasElements = false;
				for (Node child : node.children) {
					hasElements = hasElements | child.tag != null;
					format(child, level, xml);
				}
				level--;
				if (hasElements) {
					xml.linefeed();
					xml.indent(level);
				}
				xml.endElement(node.tag);
			} else {
				// element has no content
				xml.endElement();
			}
		} else if (node.content != null) {
			// Generate content
			String content = normalizeSpace(node.content);
			if (!content.isEmpty()) {
				xml.addContent(content);
			}
		} else if (!node.children.isEmpty()) {
			// Other nodes kind like root
			for (Node child : node.children) {
				format(child, level, xml);
			}
		}
	}

	private static String normalizeSpace(String str) {
		StringBuilder b = new StringBuilder(str.length());
		for (int i = 0; i < str.length(); ++i) {
			char c = str.charAt(i);
			if (Character.isWhitespace(c)) {
				if (i <= 0 || Character.isWhitespace(str.charAt(i - 1)))
					continue;
				b.append(' ');
				continue;
			}
			b.append(c);
		}
		return b.toString();
	}

}
