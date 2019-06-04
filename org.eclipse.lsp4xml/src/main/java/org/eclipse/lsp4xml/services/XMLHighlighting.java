/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.services;

import static org.eclipse.lsp4xml.utils.XMLPositionUtility.covers;
import static org.eclipse.lsp4xml.utils.XMLPositionUtility.doesTagCoverPosition;
import static org.eclipse.lsp4xml.utils.XMLPositionUtility.getTagNameRange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.dom.parser.TokenType;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;

/**
 * XML highlighting support.
 *
 */
class XMLHighlighting {

	private static final Logger LOGGER = Logger.getLogger(XMLHighlighting.class.getName());

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLHighlighting(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public List<DocumentHighlight> findDocumentHighlights(DOMDocument xmlDocument, Position position,
			CancelChecker cancelChecker) {
		int offset = -1;
		try {
			offset = xmlDocument.offsetAt(position);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In XMLHighlighting the client provided Position is at a BadLocation", e);
			return Collections.emptyList();
		}
		DOMNode node = xmlDocument.findNodeAt(offset);
		if (node == null || !node.isElement() || ((DOMElement) node).getTagName() == null) {
			return Collections.emptyList();
		}

		Range startTagRange = null;
		Range endTagRange = null;
		if (node.isCDATA()) {
			Position startPos = null;
			Position endPos = null;
			Range tempRange = null;
			try {
				startPos = xmlDocument.positionAt(node.getStart());
				endPos = xmlDocument.positionAt(node.getEnd());
				tempRange = new Range(startPos, endPos);

			} catch (BadLocationException e) {
				LOGGER.log(Level.SEVERE, "In XMLHighlighting the Node at provided Offset is a BadLocation", e);
				return Collections.emptyList();
			}
			if (covers(tempRange, position)) {
				startPos.setCharacter(startPos.getCharacter() + 1); // {Cursor}<![CDATA[ -> <{Cursor}![CDATA[
				endPos.setCharacter(endPos.getCharacter() - 1); // ]]>{Cursor} -> ]]{Cursor}>
				Position startPosEnd = new Position(startPos.getLine(), startPos.getCharacter() + 8);
				Position endPosStart = new Position(endPos.getLine(), endPos.getCharacter() - 2);
				return getHighlightsList(new Range(startPos, startPosEnd), new Range(endPosStart, endPos));
			}
			return Collections.emptyList();
		} else if (node.isElement()) {
			DOMElement element = (DOMElement) node;
			startTagRange = getTagNameRange(TokenType.StartTag, node.getStart(), xmlDocument);
			endTagRange = element.hasEndTag()
					? getTagNameRange(TokenType.EndTag, element.getEndTagOpenOffset(), xmlDocument)
					: null;
			if (doesTagCoverPosition(startTagRange, endTagRange, position)) {
				return getHighlightsList(startTagRange, endTagRange);
			}
		}
		return Collections.emptyList();
	}

	private static List<DocumentHighlight> getHighlightsList(Range startTagRange, Range endTagRange) {

		List<DocumentHighlight> result = new ArrayList<>(2);
		if (startTagRange != null) {
			result.add(new DocumentHighlight(startTagRange, DocumentHighlightKind.Read));
		}
		if (endTagRange != null) {
			result.add(new DocumentHighlight(endTagRange, DocumentHighlightKind.Read));
		}
		return result;
	}

}
