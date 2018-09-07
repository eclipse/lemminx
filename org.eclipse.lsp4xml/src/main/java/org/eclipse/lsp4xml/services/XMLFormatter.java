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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.dom.Comment;
import org.eclipse.lsp4xml.dom.Node;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.dom.XMLParser;
import org.eclipse.lsp4xml.dom.XMLParser.Flag;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;
import org.eclipse.lsp4xml.utils.XMLBuilder;

/**
 * XML formatter support.
 *
 */
class XMLFormatter {

	private static final Logger LOGGER = Logger.getLogger(XMLFormatter.class.getName());
	public static final EnumSet<Flag> FORMAT_MASK = EnumSet.of(Flag.Content);

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLFormatter(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public List<? extends TextEdit> format(TextDocument document, Range range, XMLFormattingOptions formattingOptions) {
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
			XMLDocument doc = XMLParser.getInstance().parse(text, null, FORMAT_MASK);

			// Format the content
			XMLBuilder xml = new XMLBuilder(formattingOptions, "", document.lineDelimiter(startPosition.getLine()));
			format(doc, 0, end, xml);

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

	private void format(Node node, int level, int end, XMLBuilder xml) {
		if (node.tag != null) {

			// element to format
			if (level > 0 && !(node.isComment() && ((Comment) node).isCommentSameLineEndTag())) {
				// add new line + indent
				xml.linefeed();
				xml.indent(level);
			}
			// generate start element
			if (node.isCDATA()) {
				xml.startCDATA();
				xml.addContentCDATA(node.content);
				xml.endCDATA();
			} else if (node.isComment()) {
				xml.startComment((Comment) node);
				xml.addContentComment(node.content);
				xml.endComment();
				if (level == 0) {
					xml.linefeed();
				}
			}
			else if (node.isProcessingInstruction()) {
				xml.startPrologOrPI(node.tag);
				xml.addContentPI(node.content);
				xml.endPrologOrPI();
				if(level == 0) {
					xml.linefeed();
				}
			} else if (node.isProlog()) {
				xml.startPrologOrPI(node.tag);
				if (node.hasAttributes()) {
					// generate attributes
					String[] attributes = new String[3];
					attributes[0] = "version";
					attributes[1] = "encoding";
					attributes[2] = "standalone";

					int attributeIndex = 0;
					for (int i = 0; i < attributes.length; i++) {
						String name = attributes[i];
						String value = node.getAttributeValue(attributes[i]);
						if (value == null) {
							continue;
						}
						xml.addPrologAttribute(name, value, level);
						attributeIndex++;
					}
				}
				xml.endPrologOrPI();
				xml.linefeed();
			} else if (node.isDoctype()) {
				xml.startDoctype();
				xml.addContentDoctype(node.content);
				xml.endDoctype();
				xml.linefeed();
			} else {
				xml.startElement(node.tag, false);
				if (node.hasAttributes()) {
					// generate attributes
					Set<String> attributeNames = node.attributeNames();
					int attributeIndex = 0;
					for (String attributeName : attributeNames) {
						xml.addAttribute(attributeName, node.getAttributeValue(attributeName), attributeIndex, level, node.tag);
						attributeIndex++;
					}
				}
				boolean hasElements = false;
				boolean startElementClosed = false;
				if (node.hasChildren()) {
					// element has body
					xml.closeStartElement();
					startElementClosed = true;
					level++;
					for (Node child : node.getChildren()) {
						hasElements = hasElements | child.tag != null;
						format(child, level, end, xml);
					}
					level--;
				}
				if (node.isClosed()) {
					if (hasElements) {
						xml.linefeed();
						xml.indent(level);
					}
					// end tag element is done, only if the element is closed
					// the format, doesn't fix the close tag
					if (node.endTagStart != null && node.endTagStart.intValue() <= end) {
						if (!startElementClosed) {
							xml.closeStartElement();
						}
						xml.endElement(node.tag);
					} else {
						xml.endElement();
					}
				} else if (node.isStartTagClose()) {
					if (!startElementClosed) {
						xml.closeStartElement();
					}
				}
				return;
			}
		} else if (node.content != null) {
			// Generate content
			String content = node.content;
			if (!content.isEmpty()) {
				xml.addContent(content);
			}
		} else if (node.hasChildren()) {
			// Other nodes kind like root
			for (Node child : node.getChildren()) {
				format(child, level, end, xml);
			}
		}
	}

}
