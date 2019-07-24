/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.dom;

import java.util.function.BiConsumer;

/**
 * DTD Element Declaration <!ELEMENT
 * 
 * @see https://www.w3.org/TR/REC-xml/#dt-eldecl
 *
 */
public class DTDElementDecl extends DTDDeclNode {

	/**
	 * Formats:
	 * 
	 * <!ELEMENT element-name category> or <!ELEMENT element-name (element-content)>
	 * 
	 */

	public DTDDeclParameter category;
	public DTDDeclParameter content;

	public DTDElementDecl(int start, int end) {
		super(start, end);
		setDeclType(start + 2, start + 9);
	}

	@Override
	public String getNodeName() {
		return getName();
	}

	public String getCategory() {
		return category != null ? category.getParameter() : null;
	}

	public void setCategory(int start, int end) {
		category = addNewParameter(start, end);
	}

	public String getContent() {
		return content != null ? content.getParameter() : null;
	}

	public void setContent(int start, int end) {
		content = addNewParameter(start, end);
	}

	@Override
	public short getNodeType() {
		return DOMNode.DTD_ELEMENT_DECL_NODE;
	}

	/**
	 * Returns the offset of the end of tag <!ELEMENT
	 * 
	 * @return the offset of the end of tag <!ELEMENT
	 */
	public int getEndElementTag() {
		return getStart() + "<!ELEMENT".length();
	}

	/**
	 * Returns the parameter (start/end offset) at the given offset and null
	 * otherwise.
	 * 
	 * <p>
	 * <!ELEMENT note (to,from,head|ing,body)> will return (start/end offset) of
	 * heading.
	 * </p>
	 *
	 * <p>
	 * <!ELEMENT n|ote (to,from,head|ing,body)> will return null.
	 * </p>
	 * 
	 * @param offset the offset
	 * @return the parameter (start/end offset) at the given offset and null
	 *         otherwise.
	 */
	public DTDDeclParameter getParameterAt(int offset) {
		// Check if offset is in the <!ELEMENT nam|e
		if (isInNameParameter(offset)) {
			return null;
		}
		// We are after the <!ELEMENT name, search the parameter
		int start = getNameParameter().getEnd();
		int end = getEnd();
		String text = getOwnerDocument().getText();
		// Find the start word offset from the left of the offset (ex : (head|ing) will
		// return offset of 'h'
		int paramStart = findStartWord(text, start, offset);
		// Find the end word to the right of the offset (ex : (head|ing) will return
		// offset of 'g'
		int paramEnd = findEndWord(text, offset, end);
		if (paramStart == -1 || paramEnd == -1) {
			// no word
			return null;
		}
		return new DTDDeclParameter(this, paramStart, paramEnd);
	}

	@Override
	public DTDDeclParameter getReferencedElementNameAt(int offset) {
		return getParameterAt(offset);
	}

	/**
	 * Collect parameters which matches the given target.
	 * 
	 * @param target    the target
	 * @param collector the collector to collect parameters.
	 */
	public void collectParameters(DTDDeclParameter target, BiConsumer<DTDDeclParameter, DTDDeclParameter> collector) {
		DTDDeclParameter name = getNameParameter();
		if (name == null) {
			return;
		}
		int start = name.getEnd();
		int end = getEnd();

		String text = getOwnerDocument().getText();
		text.length();
		int wordStart = -1;
		int wordEnd = -1;
		// Loop for content after <!ELEMENT element-name (
		// to check if target checks words

		// Ex : if 'svg' is the word to find (the target node)
		// and we have <!ELEMENT element-name (svg, title, font-face)
		// we must collect svg as child
		for (int i = start; i < end; i++) {
			char c = text.charAt(i);
			// check if current character is valid for a word.
			if (isValidChar(c)) {
				if (wordStart == -1) {
					// start of the word
					wordStart = i;
				}
			} else if (wordStart != -1) {
				// current character is not valid, it's the end of the word.
				wordEnd = i;
			}
			if (wordStart != -1 && wordEnd != -1) {
				// a word was found
				boolean check = isMatchName(target.getParameter(), text, wordStart, wordEnd);
				if (check) {
					collector.accept(new DTDDeclParameter(this, wordStart, wordEnd), target);
				}
				wordStart = -1;
				wordEnd = -1;
			}
		}
	}

	/**
	 * Returns the start word offset from the <code>from</code> offset to the
	 * <code>to</code> offse and -1 if no word.
	 * 
	 * @param text the text
	 * @param from the from offset
	 * @param to   the to offset
	 * @return the start word offset from the <code>from</code> offset to the
	 *         <code>to</code> offse and -1 if no word.
	 */
	private static int findStartWord(String text, int from, int to) {
		int wordStart = -1;
		int length = to - from;
		for (int i = 0; i < length; i++) {
			if (isValidChar(text.charAt(to - i))) {
				wordStart = to - i;
			} else {
				return wordStart;
			}
		}
		return wordStart;
	}

	/**
	 * Returns the end word offset from the <code>from</code> offset to the
	 * <code>to</code> offse and -1 if no word.
	 * 
	 * @param text the text
	 * @param from the from offset
	 * @param to   the to offset
	 * @return the end word offset from the <code>from</code> offset to the
	 *         <code>to</code> offse and -1 if no word.
	 */
	private static int findEndWord(String text, int from, int to) {
		int wordEnd = -1;
		int length = to - from;
		for (int i = 0; i < length; i++) {
			if (isValidChar(text.charAt(from + i))) {
				wordEnd = from + i + 1;
			} else {
				return wordEnd;
			}
		}
		return wordEnd;
	}

	/**
	 * Return true if the given character belong to the element name and false
	 * otherwise.
	 * 
	 * @param c the character
	 * @return true if the given character belong to the element name and false
	 */
	private static boolean isValidChar(char c) {
		return Character.isJavaIdentifierPart(c) || c == '-';
	}

	/**
	 * Returns true if the word in the given <code>text</code> which starts at
	 * <code>wordStart</code> offset and ends at <code>wordEnd</code> matches the
	 * given <code>searchWord</code>
	 * 
	 * @param searchWord the word to search
	 * @param text       the text
	 * @param wordStart  the word start offset
	 * @param wordEnd    the word end offset
	 * @return true if the word in the given <code>text</code> which starts at
	 *         <code>wordStart</code> offset and ends at <code>wordEnd</code>
	 *         matches the given <code>searchName</code>
	 */
	private static boolean isMatchName(String searchWord, String text, int wordStart, int wordEnd) {
		int length = wordEnd - wordStart;
		if (searchWord.length() != length) {
			return false;
		}
		for (int j = 0; j < length; j++) {
			if ((searchWord.charAt(j) != text.charAt(wordStart + j))) {
				return false;
			}
		}
		return true;
	}

}
