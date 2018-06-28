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
package org.eclipse.xml.languageserver.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.xml.languageserver.internal.parser.BadLocationException;
import org.eclipse.xml.languageserver.internal.parser.Scanner;
import org.eclipse.xml.languageserver.internal.parser.TokenType;
import org.eclipse.xml.languageserver.internal.parser.XMLScanner;
import org.eclipse.xml.languageserver.model.Node;
import org.eclipse.xml.languageserver.model.XMLDocument;

/**
 * XML highlighting support.
 * 
 * @author azerr
 *
 */
class XMLHighlighting {

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLHighlighting(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public List<DocumentHighlight> findDocumentHighlights(TextDocumentItem document, Position position,
			XMLDocument xmlDocument) {
		int offset = -1;
		try {
			offset = xmlDocument.offsetAt(position);
		} catch (BadLocationException e) {
			return Collections.emptyList();
		}
		Node node = xmlDocument.findNodeAt(offset);
		if (node.tag == null) {
			return Collections.emptyList();
		}
		List<DocumentHighlight> result = new ArrayList<>();
		Range startTagRange = getTagNameRange(TokenType.StartTag, document, node.start, xmlDocument);
		Range endTagRange = node.endTagStart != null
				? getTagNameRange(TokenType.EndTag, document, node.endTagStart, xmlDocument)
				: null;
		if (startTagRange != null && covers(startTagRange, position)
				|| endTagRange != null && covers(endTagRange, position)) {
			if (startTagRange != null) {
				result.add(new DocumentHighlight(startTagRange, DocumentHighlightKind.Read));
			}
			if (endTagRange != null) {
				result.add(new DocumentHighlight(endTagRange, DocumentHighlightKind.Read));
			}
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

	private static Range getTagNameRange(TokenType tokenType, TextDocumentItem document, int startOffset,
			XMLDocument fmDocument) {
		Scanner scanner = XMLScanner.createScanner(document.getText(), startOffset);
		TokenType token = scanner.scan();
		while (token != TokenType.EOS && token != tokenType) {
			token = scanner.scan();
		}
		if (token != TokenType.EOS) {
			try {
				return new Range(fmDocument.positionAt(scanner.getTokenOffset()),
						fmDocument.positionAt(scanner.getTokenEnd()));
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
}
