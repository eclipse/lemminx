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

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.internal.parser.BadLocationException;
import org.eclipse.lsp4xml.internal.parser.Scanner;
import org.eclipse.lsp4xml.internal.parser.TokenType;
import org.eclipse.lsp4xml.internal.parser.XMLScanner;
import org.eclipse.lsp4xml.model.Node;
import org.eclipse.lsp4xml.model.XMLDocument;

/**
 * XML hover support.
 *
 */
class XMLHover {

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLHover(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public Hover doHover(XMLDocument document, Position position) {
		int offset = 0;
		try {
			offset = document.offsetAt(position);
		} catch (BadLocationException e) {
			return null;
		}
		Node node = document.findNodeAt(offset);
		if (node == null || node.tag == null) {
			return null;
		}
		if (node.endTagStart != null && offset >= node.endTagStart) {
			Range tagRange = getTagNameRange(TokenType.EndTag, node.endTagStart, offset, document);
			if (tagRange != null) {
				return getTagHover(node.tag, tagRange, false);
			}
			return null;
		}

		Range tagRange = getTagNameRange(TokenType.StartTag, node.start, offset, document);
		if (tagRange != null) {
			return getTagHover(node.tag, tagRange, true);
		}
		return null;
	}

	private Hover getTagHover(String tag, Range tagRange, boolean b) {
		return null;
	}

	private Range getTagNameRange(TokenType tokenType, int startOffset, int offset, XMLDocument document) {
		Scanner scanner = XMLScanner.createScanner(document.getText(), startOffset);
		TokenType token = scanner.scan();
		while (token != TokenType.EOS
				&& (scanner.getTokenEnd() < offset || scanner.getTokenEnd() == offset && token != tokenType)) {
			token = scanner.scan();
		}
		if (token == tokenType && offset <= scanner.getTokenEnd()) {
			try {
				return new Range(document.positionAt(scanner.getTokenOffset()),
						document.positionAt(scanner.getTokenEnd()));
			} catch (BadLocationException e) {
				return null;
			}
		}
		return null;
	}
}
