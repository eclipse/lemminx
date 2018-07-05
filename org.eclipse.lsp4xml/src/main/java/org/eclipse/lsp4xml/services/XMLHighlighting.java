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

import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4xml.internal.parser.BadLocationException;
import org.eclipse.lsp4xml.internal.parser.Scanner;
import org.eclipse.lsp4xml.internal.parser.TokenType;
import org.eclipse.lsp4xml.internal.parser.XMLScanner;
import org.eclipse.lsp4xml.model.Node;
import org.eclipse.lsp4xml.model.XMLDocument;

/**
 * XML highlighting support.
 *
 */
class XMLHighlighting {

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLHighlighting(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public List<DocumentHighlight> findDocumentHighlights(XMLDocument xmlDocument, Position position) {
		int offset = -1;
		try {
			offset = xmlDocument.offsetAt(position);
		} catch (BadLocationException e) {
			return Collections.emptyList();
		}
		Node node = xmlDocument.findNodeAt(offset);
		if (node.closed == false) {
			return Collections.emptyList();
		}

		Range startTagRange = null;
		Range endTagRange = null;
		if(node.isCDATA == true){
			Position startPos = null;
			Position endPos = null;
			Range tempRange = null;
			try {
				startPos = xmlDocument.positionAt(node.start); 
				endPos = xmlDocument.positionAt(node.end); 
				tempRange = new Range(startPos, endPos);
				
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return Collections.emptyList();
			}
			if(doTagsCoverPosition(tempRange, endTagRange, position)) {
				startPos.setCharacter(startPos.getCharacter() + 1); // {Here}<![CDATA[   ->   <{Here}![CDATA[
				endPos.setCharacter(endPos.getCharacter() - 1); // ]]>{Here}  ->   ]]{Here}>
				Position startPosEnd = new Position(startPos.getLine(), startPos.getCharacter() + 8);
				Position endPosStart = new Position(endPos.getLine(), endPos.getCharacter() - 2);
				return getHighlightList(new Range(startPos, startPosEnd), new Range(endPosStart, endPos));
			}
			return Collections.emptyList();
		}
		else{//Regular element
			if (node.closed == false) {
				return Collections.emptyList();
			}
			startTagRange = getTagNameRange(TokenType.StartTag, node.start, xmlDocument);
			endTagRange = node.endTagStart != null ? 
				getTagNameRange(TokenType.EndTag, node.endTagStart, xmlDocument) : null;
			if(doTagsCoverPosition(startTagRange, endTagRange, position)) {
				return getHighlightList(startTagRange, endTagRange);
			}
		}
		
		
		return Collections.emptyList();
		
	}

	private static boolean doTagsCoverPosition(Range startTagRange, Range endTagRange, Position position){
		return startTagRange != null && covers(startTagRange, position)
		|| endTagRange != null && covers(endTagRange, position);
	}

	private static List<DocumentHighlight> getHighlightList(Range startTagRange, Range endTagRange){
		
		List<DocumentHighlight> result = new ArrayList<>();
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


	private static Range getTagNameRange(TokenType tokenType, int startOffset,
																				XMLDocument fmDocument) {

		Scanner scanner = XMLScanner.createScanner(fmDocument.getText(), startOffset);

		TokenType token = scanner.scan();
		while (token != TokenType.EOS && token != tokenType) {
			token = scanner.scan();
		}
		if (token != TokenType.EOS) {
			try {
				return new Range(fmDocument.positionAt(scanner.getTokenOffset()),
						fmDocument.positionAt(scanner.getTokenEnd()));
			} catch (BadLocationException e) {
				e.printStackTrace();
				return null;
			}	
		}
		return null;
	}


}
