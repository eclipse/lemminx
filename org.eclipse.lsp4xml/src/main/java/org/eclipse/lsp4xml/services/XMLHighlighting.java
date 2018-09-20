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
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.Element;
import org.eclipse.lsp4xml.dom.Node;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.dom.parser.Scanner;
import org.eclipse.lsp4xml.dom.parser.TokenType;
import org.eclipse.lsp4xml.dom.parser.XMLScanner;
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

	public List<DocumentHighlight> findDocumentHighlights(XMLDocument xmlDocument, Position position) {
		int offset = -1;
		try {
			offset = xmlDocument.offsetAt(position);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In XMLHighlighting the client provided Position is at a BadLocation", e);
			return Collections.emptyList();
		}
		Node node = xmlDocument.findNodeAt(offset);
		if (node == null || !node.isElement() || ((Element) node).getTagName() == null) {
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
			Element element = (Element) node;
			startTagRange = getTagNameRange(TokenType.StartTag, node.getStart(), xmlDocument);
			endTagRange = element.hasEndTag() ? getTagNameRange(TokenType.EndTag, element.getEndTagOpenOffset(), xmlDocument)
					: null;
			if (doesTagCoverPosition(startTagRange, endTagRange, position)) {
				return getHighlightsList(startTagRange, endTagRange);
			}
		}
		return Collections.emptyList();
	}

	private static boolean doesTagCoverPosition(Range startTagRange, Range endTagRange, Position position) {
		return startTagRange != null && covers(startTagRange, position)
				|| endTagRange != null && covers(endTagRange, position);
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

	private static boolean isBeforeOrEqual(Position pos1, Position pos2) {
		return pos1.getLine() < pos2.getLine()
				|| (pos1.getLine() == pos2.getLine() && pos1.getCharacter() <= pos2.getCharacter());
	}

	private static boolean covers(Range range, Position position) {
		return isBeforeOrEqual(range.getStart(), position) && isBeforeOrEqual(position, range.getEnd());
	}

	private static Range getTagNameRange(TokenType tokenType, int startOffset, XMLDocument xmlDocument) {

		Scanner scanner = XMLScanner.createScanner(xmlDocument.getText(), startOffset);

		TokenType token = scanner.scan();
		while (token != TokenType.EOS && token != tokenType) {
			token = scanner.scan();
		}
		if (token != TokenType.EOS) {
			try {
				return new Range(xmlDocument.positionAt(scanner.getTokenOffset()),
						xmlDocument.positionAt(scanner.getTokenEnd()));
			} catch (BadLocationException e) {
				LOGGER.log(Level.SEVERE,
						"While creating Range in XMLHighlighting the Scanner's Offset was a BadLocation", e);
				return null;
			}
		}
		return null;
	}

}
