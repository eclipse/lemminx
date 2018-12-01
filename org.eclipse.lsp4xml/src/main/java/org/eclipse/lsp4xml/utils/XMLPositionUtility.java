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

import java.util.List;

import org.apache.xerces.xni.XMLLocator;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMCharacterData;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.dom.DOMProcessingInstruction;
import org.eclipse.lsp4xml.dom.DTDElementDecl;

/**
 * XML position utility.
 *
 */
public class XMLPositionUtility {

	private XMLPositionUtility() {
	}

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
			return location != null ? new Position(location.getLineNumber() - 1, location.getColumnNumber() - 1) : null;
		}
	}

	public static Range selectAttributeNameAt(int offset, DOMDocument document) {
		offset = adjustOffsetForAttribute(offset, document);
		DOMAttr attr = document.findAttrAt(offset);
		if (attr != null) {
			int startOffset = attr.getNodeAttrName().getStart();
			int endOffset = attr.getNodeAttrName().getEnd();
			return createRange(startOffset, endOffset, document);
		}
		return null;
	}

	public static Range selectAttributeValueAt(String attrName, int offset, DOMDocument document) {
		DOMNode element = document.findNodeAt(offset);
		if (element != null) {
			DOMAttr attr = element.getAttributeNode(attrName);
			if (attr != null) {
				return createAttrValueRange(attr, document);
			}
		}
		return null;
	}

	public static Range selectAttributeValueFromGivenValue(String attrValue, int offset, DOMDocument document) {
		DOMNode element = document.findNodeAt(offset);
		if (element != null && element.hasAttributes()) {
			List<DOMAttr> attribues = element.getAttributeNodes();
			for (DOMAttr attr : attribues) {
				if (attrValue.equals(attr.getValue())) {
					return createAttrValueRange(attr, document);
				}
			}
		}
		return null;
	}

	private static Range createAttrValueRange(DOMAttr attr, DOMDocument document) {
		int startOffset = attr.getNodeAttrValue().getStart();
		int endOffset = attr.getNodeAttrValue().getEnd();
		return createRange(startOffset, endOffset, document);
	}

	public static Range selectAttributeValueByGivenValueAt(String attrValue, int offset, DOMDocument document) {
		DOMNode element = document.findNodeAt(offset);
		if (element != null && element.hasAttributes()) {
			List<DOMAttr> attributes = element.getAttributeNodes();
			for (DOMAttr attr : attributes) {
				if (attrValue.equals(attr.getValue())) {
					return createAttrValueRange(attr, document);
				}
			}
		}
		return null;
	}

	public static Range selectAttributeNameFromGivenNameAt(String attrName, int offset, DOMDocument document) {
		DOMNode element = document.findNodeAt(offset);
		if (element != null && element.hasAttributes()) {
			DOMAttr attr = element.getAttributeNode(attrName);
			if (attr != null) {
				return createAttrNameRange(attr, document);
			}
		}
		return null;
	}

	private static Range createAttrNameRange(DOMAttr attr, DOMDocument document) {
		int startOffset = attr.getNodeAttrName().getStart();
		int endOffset = attr.getNodeAttrName().getEnd();
		return createRange(startOffset, endOffset, document);
	}

	public static Range selectAttributeFromGivenNameAt(String attrName, int offset, DOMDocument document) {
		DOMNode element = document.findNodeAt(offset);
		if (element != null && element.hasAttributes()) {
			DOMAttr attr = element.getAttributeNode(attrName);
			if (attr != null) {
				return createAttrRange(attr, document);
			}
		}
		return null;
	}
	
	private static Range createAttrRange(DOMAttr attr, DOMDocument document) {
		int startOffset = attr.getStart();
		int endOffset = attr.getEnd();
		return createRange(startOffset, endOffset, document);
	}
	
	private static int adjustOffsetForAttribute(int offset, DOMDocument document) {
		// For attribute value, Xerces report the error offset after the spaces which
		// are after " of the attribute value
		// Here sample with offset marked with |
		// -> <a b="" b="" |>
		// -> <a b="" b=""|>
		// Remove spaces
		String text = document.getText();
		char c = text.charAt(offset);
		if (c == '>') {
			offset--;
			c = text.charAt(offset);
			if (c == '/') {
				offset--;
			}
		}
		while (offset >= 0) {
			if (Character.isWhitespace(c)) {
				offset--;
			} else {
				break;
			}
			c = text.charAt(offset);
		}
		return offset;
	}

	public static Range selectChildEndTag(String childTag, int offset, DOMDocument document) {
		DOMNode parent = document.findNodeAt(offset);
		if (parent == null || !parent.isElement() || ((DOMElement) parent).getTagName() == null) {
			return null;
		}
		if (parent != null) {
			DOMNode child = findChildNode(childTag, parent.getChildren());
			if (child != null) {
				return createRange(child.getStart() + 1, child.getStart() + 1 + childTag.length(), document);
			}
		}
		return null;
	}

	static DOMNode findChildNode(String childTag, List<DOMNode> children) {
		for (DOMNode child : children) {
			if (child.isElement() && childTag != null && childTag.equals(((DOMElement) child).getTagName())
					&& !child.isClosed()) {
				return child;
			}
		}
		return null;
	}

	public static Range selectStartTag(int offset, DOMDocument document) {
		DOMNode element = document.findNodeAt(offset);
		if (element != null) {
			return selectStartTag(element);
		}
		return null;
	}

	public static Range selectStartTag(DOMNode element) {
		int startOffset = element.getStart() + 1; // <
		int endOffset = startOffset + getStartTagLength(element);
		if (element.isProcessingInstruction() || element.isProlog()) {
			// in the case of prolog or processing instruction, tag is equals to "xml"
			// without '?' -> <?xml
			// increment end offset to select '?xml' instead of selecting '?xm'
			endOffset++;
		}
		DOMDocument document = element.getOwnerDocument();
		return createRange(startOffset, endOffset, document);
	}

	private static int getStartTagLength(DOMNode node) {
		if (node.isElement()) {
			DOMElement element = (DOMElement) node;
			return element.getTagName() != null ? element.getTagName().length() : 0;
		} else if (node.isProcessingInstruction() || node.isProlog()) {
			DOMProcessingInstruction element = (DOMProcessingInstruction) node;
			return element.getTarget() != null ? element.getTarget().length() : 0;
		}
		return 0;
	}

	public static int selectCurrentTagOffset(int offset, DOMDocument document) {
		DOMNode element = document.findNodeAt(offset);
		if (element != null) {
			return element.getStart(); // <

		}
		return -1;
	}

	public static Range selectEndTag(int offset, DOMDocument document) {
		DOMNode node = document.findNodeAt(offset);
		if (node != null && node.isElement()) {
			DOMElement element = (DOMElement) node;
			if (element.hasEndTag()) {
				int startOffset = element.getEndTagOpenOffset() + 2; // <\
				int endOffset = startOffset + getStartTagLength(element);
				return createRange(startOffset, endOffset, document);
			}
		}
		return null;
	}

	public static Range selectAllAttributes(int offset, DOMDocument document) {
		DOMNode element = document.findNodeAt(offset);
		if (element != null && element.hasAttributes()) {
			int startOffset = -1;
			int endOffset = 0;
			List<DOMAttr> attributes = element.getAttributeNodes();
			for (DOMAttr attr : attributes) {
				if (startOffset == -1) {
					startOffset = attr.getStart();
					endOffset = attr.getEnd();
				} else {
					startOffset = Math.min(attr.getStart(), startOffset);
					endOffset = Math.min(attr.getEnd(), startOffset);
				}
			}
			if (startOffset != -1) {
				return createRange(startOffset, endOffset, document);
			}
		}
		return null;
	}

	public static Range selectFirstNonWhitespaceText(int offset, DOMDocument document) {
		DOMNode element = document.findNodeAt(offset);
		if (element != null) {
			for (DOMNode node : element.getChildren()) {
				if (node.isCharacterData() && ((DOMCharacterData) node).hasMultiLine()) {
					String content = ((DOMCharacterData) node).getData();
					int start = node.getStart();
					Integer end = null;
					for (int i = 0; i < content.length(); i++) {
						char c = content.charAt(i);
						if (end == null) {
							if (Character.isWhitespace(c)) {
								start++;
							} else {
								end = start;
							}
						} else {
							if (!Character.isWhitespace(c)) {
								end++;
							} else {
								break;
							}
						}
					}
					if (end != null) {
						end++;
						return createRange(start, end, document);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Finds the offset of the first tag it comes across behind the given offset.
	 * 
	 * This excludes the tag it starts in if offset is within a tag.
	 */
	public static Range selectPreviousEndTag(int offset, DOMDocument document) {
		// boolean firstBracket = false;
		int i = offset;
		char c = document.getText().charAt(i);
		while (i >= 0) {
			if (c == '>') {
				// if(firstBracket) {
				return selectStartTag(i, document);
				// }
				// else {
				// firstBracket = true;
				// }
			}
			i--;
			c = document.getText().charAt(i);
		}
		return null;
	}

	public static Range createRange(int startOffset, int endOffset, DOMDocument document) {
		try {
			return new Range(document.positionAt(startOffset), document.positionAt(endOffset));
		} catch (BadLocationException e) {
			return null;
		}
	}

	public static Range selectText(int offset, DOMDocument document) {
		DOMNode node = document.findNodeAt(offset);
		if (node != null) {
			if (node.hasChildNodes()) {
				// <root>BAD TEXT</root>
				for (DOMNode child : node.getChildren()) {
					if (child.isText()) {
						return createRange(child.getStart(), child.getEnd(), document);
					}
				}
			} else if (node.isElement()) {
				// node has NONE text (ex: <root></root>, select the start tag
				return selectStartTag(node);
			}
		}
		return null;
	}
	
	public static Range selectDTDElementDeclAt(int offset, DOMDocument document) {
		DOMNode node = document.findNodeAt(offset);
		if (node != null && node.isDTDElementDecl()) {
			return createRange(node.getStart(), node.getEnd(), document);
		}
		return null;
	}

	public static Range selectDTDElementDeclTagAt(int offset, DOMDocument document) {
		DOMNode node = document.findNodeAt(offset);
		if (node != null && node.isDTDElementDecl()) {
			DTDElementDecl elementDecl = (DTDElementDecl) node;
			return createRange(elementDecl.getStart(), elementDecl.getEndElementTag(), document);
		}
		return null;
	}


}
