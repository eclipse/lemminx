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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.Node;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.internal.parser.Scanner;
import org.eclipse.lsp4xml.internal.parser.TokenType;
import org.eclipse.lsp4xml.internal.parser.XMLScanner;
import org.eclipse.lsp4xml.services.extensions.IHoverParticipant;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;

/**
 * XML hover support.
 *
 */
class XMLHover {
	private static final Logger LOGGER = Logger.getLogger(XMLHover.class.getName());
	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLHover(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public Hover doHover(XMLDocument xmlDocument, Position position) {
		HoverRequest hoverRequest = null;
		try {
			hoverRequest = new HoverRequest(xmlDocument, position);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Failed creating HoverRequest", e);
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
				return getTagHover(hoverRequest, tagRange, false);
			}
			return null;
		}

		Range tagRange = getTagNameRange(TokenType.StartTag, node.start, offset, xmlDocument);
		if (tagRange != null) {
			return getTagHover(hoverRequest, tagRange, true);
		}
		return null;
	}

	private Hover getTagHover(HoverRequest hoverRequest, Range tagRange, boolean open) {
		hoverRequest.setTagRange(tagRange);
		hoverRequest.setOpen(open);
		for (IHoverParticipant participant : extensionsRegistry.getHoverParticipants()) {
			try {
				Hover hover = participant.onTag(hoverRequest);
				if (hover != null) {
					return hover;
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "While performing IHoverParticipant#onTag", e);
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
				LOGGER.log(Level.SEVERE, "While creating Range in XMLHover the Scanner's Offset was a BadLocation", e);
				return null;
			}
		}
		return null;
	}
}
