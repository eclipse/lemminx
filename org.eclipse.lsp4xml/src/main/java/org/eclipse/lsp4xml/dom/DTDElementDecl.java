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

	public DTDDeclParameter name;
	public DTDDeclParameter category;
	public DTDDeclParameter content;

	public DTDElementDecl(int start, int end, DOMDocumentType parentDocumentType) {
		super(start, end, parentDocumentType);
		setDeclType(start + 2, start + 9);
	}

	public boolean isInElementName(int offset) {
		if (name == null) {
			return false;
		}
		return DOMNode.isIncluded(name.getStart(), name.getEnd(), offset);
	}

	public DOMDocumentType getOwnerDoctype() {
		return parentDocumentType;
	}

	@Override
	public String getNodeName() {
		return getName();
	}

	public String getName() {
		return name != null ? name.getParameter() : null;
	}

	public DTDDeclParameter getNameNode() {
		return name;
	}

	public void setName(int start, int end) {
		name = addNewParameter(start, end);
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

	public DTDDeclParameter getParameterAt(int offset) {
		if (name == null) {
			return null;
		}
		if (isInElementName(offset)) {
			return null;
		}
		int start = name.getEnd();
		int end = getEnd();
		String text = getOwnerDocument().getText();
		int paramStart = findStartWord(text, start, offset);
		int paramEnd = findEndWord(text, offset, end);
		return new DTDDeclParameter(this, paramStart, paramEnd);
	}

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

	private static boolean isValidChar(char c) {
		return Character.isJavaIdentifierPart(c) || c == '-';
	}

	public void collectParameters(DTDDeclParameter target, BiConsumer<DTDDeclParameter, DTDDeclParameter> collector) {
		if (name == null) {
			return;
		}
		int start = name.getEnd();
		int end = getEnd();

		String text = getOwnerDocument().getText();
		text.length();
		int wordStart = -1;
		int wordEnd = -1;
		for (int i = start; i < end; i++) {
			char c = text.charAt(i);
			if (isValidChar(c)) {
				if (wordStart == -1) {
					wordStart = i;
				}
			} else if (wordStart != -1) {
				wordEnd = i;
			}
			if (wordStart != -1 && wordEnd != -1) {
				// check
				boolean check = matchName(target.getParameter(), text, wordStart, wordEnd);
				if (check) {
					collector.accept(new DTDDeclParameter(this, wordStart, wordEnd), target);
				}
				wordStart = -1;
				wordEnd = -1;
			}
		}
	}

	private boolean matchName(String searchName, String text, int wordStart, int wordEnd) {
		int length = wordEnd - wordStart;
		if (searchName.length() != length) {
			return false;
		}
		for (int j = 0; j < length; j++) {
			if ((searchName.charAt(j) != text.charAt(wordStart + j))) {
				return false;
			}
		}
		return true;
	}
}
