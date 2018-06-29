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
import org.eclipse.xml.languageserver.internal.parser.XMLScanner;
import org.eclipse.xml.languageserver.internal.parser.Scanner;
import org.eclipse.xml.languageserver.internal.parser.TokenType;
import org.eclipse.xml.languageserver.model.XMLDocument;
import org.eclipse.xml.languageserver.model.Node;

class XMLHighlighting {

	public static List<DocumentHighlight> findDocumentHighlights(TextDocumentItem document, Position position,
			XMLDocument xmlDocument) {
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
		else{
			if (node.closed == false) {
				return Collections.emptyList();
			}
			startTagRange = getTagNameRange(TokenType.StartTag, document, node.start, xmlDocument);
			endTagRange = node.endTagStart != null ? 
				getTagNameRange(TokenType.EndTag, document, node.endTagStart, xmlDocument) : null;
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
				return null;
			}	
		}
		return null;
	}


}
