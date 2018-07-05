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
import org.eclipse.lsp4xml.extensions.IHoverParticipant;
import org.eclipse.lsp4xml.extensions.IHoverRequest;
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

	public Hover doHover(XMLDocument xmlDocument, Position position) {
		HoverRequest hoverRequest = null;
		try {
			hoverRequest = new HoverRequest(xmlDocument, position);
		} catch (BadLocationException e) {
			return null;
		}
		int offset = hoverRequest.getOffset();
		Node node = hoverRequest.getNode();
		if (node == null || node.tag == null) {
			return null;
		}
		if (node.endTagStart != null && offset >= node.endTagStart) {
			Range tagRange = getTagNameRange(TokenType.EndTag, node.endTagStart, offset, xmlDocument);
			if (tagRange != null) {
				return getTagHover(hoverRequest); // getTagHover(node.tag, tagRange, false);
			}
			return null;
		}

		Range tagRange = getTagNameRange(TokenType.StartTag, node.start, offset, xmlDocument);
		if (tagRange != null) {
			return getTagHover(hoverRequest); // getTagHover(node.tag, tagRange, true);
		}
		return null;
	}

	private Hover getTagHover(IHoverRequest hoverRequest) {
		for (IHoverParticipant participant : extensionsRegistry.getHoverParticipants()) {
			Hover hover = participant.onTag(hoverRequest);
			if (hover != null) {
				return hover;
			}
		}
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
