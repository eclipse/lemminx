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
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;

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
		if (offset == location.getCharacterOffset() - 1) {
			return new Position(location.getLineNumber() - 1, location.getColumnNumber() - 1);
		}
		try {
			return document.positionAt(offset);
		} catch (BadLocationException e) {
			return new Position(location.getLineNumber() - 1, location.getColumnNumber() - 1);
		}

	}

	public static int findOffsetOfAttrName(String text, int offset, String attrName) {
		boolean inQuote = false;
		boolean parsedValue = false;
		for (int i = offset; i >= 0; i--) {
			char c = text.charAt(i);
			if (!(c == ' ' || c == '\r' || c == '\n')) {
				if (c == '"' || c == '\'') {
					inQuote = !inQuote;
					if (!inQuote) {
						parsedValue = true;
					}
				} else {
					if (parsedValue && c != '=') {
						return i + 1;
					}
				}
			}
		}
		return -1;
	}

	public static int findOffsetOfStartTag(String text, int offset, String tag) {
		int lastIndex = tag.length();
		int j = lastIndex;
		for (int i = offset; i >= 0; i--) {
			char c = text.charAt(i);
			if (j == 0) {
				if (c == '<') {
					return i + 1;
				}
				j = lastIndex;
			} else {
				if (c == tag.charAt(j - 1)) {
					j--;
				} else {
					j = lastIndex;
				}
			}
		}
		return -1;
	}

	public static int findOffsetOfFirstChar(String text, int offset) {
		for (int i = offset; i >= 0; i--) {
			char c = text.charAt(i);
			if (!(c == ' ' || c == '\r' || c == '\n')) {
				return i + 1;
			}
		}
		return -1;
	}

	public static int findOffsetOfAfterChar(String text, int offset, char ch) {
		for (int i = offset; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == ch) {
				return i;
			}
		}
		return -1;
	}
}
