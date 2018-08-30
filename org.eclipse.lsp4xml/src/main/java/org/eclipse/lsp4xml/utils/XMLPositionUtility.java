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
package org.eclipse.lsp4xml.utils;

import org.apache.xerces.xni.XMLLocator;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.model.Attr;
import org.eclipse.lsp4xml.model.Node;
import org.eclipse.lsp4xml.model.XMLDocument;

/**
 * XML position utility.
 *
 */
public class XMLPositionUtility {

	/**
	 * Returns the LSP position from the SAX location.
	 * 
	 * @param offset   the adjusted offset.
	 * @param location the original SAX location.
	 * @param document the text document.
	 * @return the LSP position from the SAX location.
	 */
	public static Position toLSPPosition(int offset, XMLLocator location, TextDocument document) {
		if (location != null && offset == location.getCharacterOffset() - 1) {
			return new Position(location.getLineNumber() - 1, location.getColumnNumber() - 1);
		}
		try {
			return document.positionAt(offset);
		} catch (BadLocationException e) {
			return new Position(location.getLineNumber() - 1, location.getColumnNumber() - 1);
		}
	}

	public static Range selectAttributeName(String attrName, int offset, XMLDocument document) {
		return selectAttributeName(attrName, offset, false, document);
	}

	public static Range selectAttributeNameLast(String attrName, int offset, XMLDocument document) {
		return selectAttributeName(attrName, offset, true, document);
	}

	private static Range selectAttributeName(String attrName, int offset, boolean last, XMLDocument document) {
		Node element = document.findNodeAt(offset);
		if (element != null) {
			Attr attr = element.getAttributeNode(attrName, last);
			if (attr != null) {
				int startOffset = attr.getNodeName().start;
				int endOffset = attr.getNodeName().end;
				return createRange(startOffset, endOffset, document);
			}
		}
		return null;
	}

	public static Range selectAttributeValue(String attrName, int offset, XMLDocument document) {
		Node element = document.findNodeAt(offset);
		if (element != null) {
			Attr attr = element.getAttributeNode(attrName);
			if (attr != null) {
				int startOffset = attr.getNodeValue().start;
				int endOffset = attr.getNodeValue().end;
				return createRange(startOffset, endOffset, document);
			}
		}
		return null;
	}

	public static Range selectStartTag(int offset, XMLDocument document) {
		Node element = document.findNodeAt(offset);
		if (element != null) {
			int startOffset = element.start + 1; // <
			int endOffset = startOffset + element.tag.length();
			return createRange(startOffset, endOffset, document);
		}
		return null;
	}

	public static Range selectEndTag(int offset, XMLDocument document) {
		Node element = document.findNodeAt(offset);
		if (element != null) {
			if (element.endTagStart != null) {
				int startOffset = element.endTagStart + 2; // <\
				int endOffset = startOffset + element.tag.length();
				return createRange(startOffset, endOffset, document);
			}
		}
		return null;
	}

	public static Range createRange(int startOffset, int endOffset, XMLDocument document) {
		try {
			return new Range(document.positionAt(startOffset), document.positionAt(endOffset));
		} catch (BadLocationException e) {
			return null;
		}
	}

}
