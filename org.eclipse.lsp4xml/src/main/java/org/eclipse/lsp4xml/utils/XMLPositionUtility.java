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
import org.eclipse.lsp4xml.dom.Attr;
import org.eclipse.lsp4xml.dom.CharacterData;
import org.eclipse.lsp4xml.dom.Element;
import org.eclipse.lsp4xml.dom.Node;
import org.eclipse.lsp4xml.dom.ProcessingInstruction;
import org.eclipse.lsp4xml.dom.XMLDocument;

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

	public static Range selectAttributeNameAt(int offset, XMLDocument document) {
		offset = adjustOffsetForAttribute(offset, document);
		Attr attr = document.findAttrAt(offset);
		if (attr != null) {
			int startOffset = attr.getNodeAttrName().getStart();
			int endOffset = attr.getNodeAttrName().getEnd();
			return createRange(startOffset, endOffset, document);
		}
		return null;
	}

	public static Range selectAttributeValueAt(String attrName, int offset, XMLDocument document) {
		Node element = document.findNodeAt(offset);
		if (element != null) {
			Attr attr = element.getAttributeNode(attrName);
			if (attr != null) {
				return createAttrValueRange(attr, document);
			}
		}
		return null;
	}

	public static Range selectAttributeValueFromGivenValue(String attrValue, int offset, XMLDocument document) {
		Node element = document.findNodeAt(offset);
		if (element != null && element.hasAttributes()) {
			List<Attr> attribues = element.getAttributeNodes();
			for (Attr attr : attribues) {
				if (attrValue.equals(attr.getValue())) {
					return createAttrValueRange(attr, document);
				}
			}
		}
		return null;
	}

	private static Range createAttrValueRange(Attr attr, XMLDocument document) {
		int startOffset = attr.getNodeAttrValue().getStart();
		int endOffset = attr.getNodeAttrValue().getEnd();
		return createRange(startOffset, endOffset, document);
	}

	public static Range selectAttributeValueByGivenValueAt(String attrValue, int offset, XMLDocument document) {
		Node element = document.findNodeAt(offset);
		if (element != null && element.hasAttributes()) {
			List<Attr> attributes = element.getAttributeNodes();
			for (Attr attr : attributes) {
				if (attrValue.equals(attr.getValue())) {
					return createAttrValueRange(attr, document);
				}
			}
		}
		return null;
	}

	public static Range selectAttributeNameFromGivenNameAt(String attrName, int offset, XMLDocument document) {
		Node element = document.findNodeAt(offset);
		if (element != null && element.hasAttributes()) {
			Attr attr = element.getAttributeNode(attrName);
			if (attr != null) {
				return createAttrNameRange(attr, document);
			}
		}
		return null;
	}

	private static Range createAttrNameRange(Attr attr, XMLDocument document) {
		int startOffset = attr.getNodeAttrName().getStart();
		int endOffset = attr.getNodeAttrName().getEnd();
		return createRange(startOffset, endOffset, document);
	}

	private static int adjustOffsetForAttribute(int offset, XMLDocument document) {
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

	public static Range selectChildEndTag(String childTag, int offset, XMLDocument document) {
		Node parent = document.findNodeAt(offset);
		if (parent == null || !parent.isElement() || ((Element) parent).getTagName() == null) {
			return null;
		}
		if (parent != null) {
			Node child = findChildNode(childTag, parent.getChildren());
			if (child != null) {
				return createRange(child.getStart() + 1, child.getStart() + 1 + childTag.length(), document);
			}
		}
		return null;
	}

	static Node findChildNode(String childTag, List<Node> children) {
		for (Node child : children) {
			if (child.isElement() && childTag != null && childTag.equals(((Element) child).getTagName())
					&& !child.isClosed()) {
				return child;
			}
		}
		return null;
	}

	public static Range selectStartTag(int offset, XMLDocument document) {
		Node element = document.findNodeAt(offset);
		if (element != null) {
			return selectStartTag(element, document);
		}
		return null;
	}

	private static Range selectStartTag(Node element, XMLDocument document) {
		int startOffset = element.getStart() + 1; // <
		int endOffset = startOffset + getStartTagLength(element);
		if (element.isProcessingInstruction() || element.isProlog()) {
			// in the case of prolog or processing instruction, tag is equals to "xml"
			// without '?' -> <?xml
			// increment end offset to select '?xml' instead of selecting '?xm'
			endOffset++;
		}
		return createRange(startOffset, endOffset, document);
	}

	private static int getStartTagLength(Node node) {
		if (node.isElement()) {
			Element element = (Element) node;
			return element.getTagName() != null ? element.getTagName().length() : 0;
		} else if (node.isProcessingInstruction() || node.isProlog()) {
			ProcessingInstruction element = (ProcessingInstruction) node;
			return element.getTarget() != null ? element.getTarget().length() : 0;
		}
		return 0;
	}

	public static int selectCurrentTagOffset(int offset, XMLDocument document) {
		Node element = document.findNodeAt(offset);
		if (element != null) {
			return element.getStart(); // <

		}
		return -1;
	}

	public static Range selectEndTag(int offset, XMLDocument document) {
		Node node = document.findNodeAt(offset);
		if (node != null && node.isElement()) {
			Element element = (Element) node;
			if (element.hasEndTag()) {
				int startOffset = element.getEndTagOpenOffset() + 2; // <\
				int endOffset = startOffset + getStartTagLength(element);
				return createRange(startOffset, endOffset, document);
			}
		}
		return null;
	}

	public static Range selectAllAttributes(int offset, XMLDocument document) {
		Node element = document.findNodeAt(offset);
		if (element != null && element.hasAttributes()) {
			int startOffset = -1;
			int endOffset = 0;
			List<Attr> attributes = element.getAttributeNodes();
			for (Attr attr : attributes) {
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

	public static Range selectFirstNonWhitespaceText(int offset, XMLDocument document) {
		Node element = document.findNodeAt(offset);
		if (element != null) {
			for (Node node : element.getChildren()) {
				if (node.isCharacterData() && ((CharacterData) node).hasMultiLine()) {
					String content = ((CharacterData) node).getData();
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
	public static Range selectPreviousEndTag(int offset, XMLDocument document) {
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

	public static Range createRange(int startOffset, int endOffset, XMLDocument document) {
		try {
			return new Range(document.positionAt(startOffset), document.positionAt(endOffset));
		} catch (BadLocationException e) {
			return null;
		}
	}

	public static Range selectText(int offset, XMLDocument document) {
		Node node = document.findNodeAt(offset);
		if (node != null) {
			if (node.hasChildren()) {
				// <root>BAD TEXT</root>
				for (Node child : node.getChildren()) {
					if (child.isText()) {
						return createRange(child.getStart(), child.getEnd(), document);
					}
				}
			} else if (node.isElement()) {
				// node has NONE text (ex: <root></root>, select the start tag
				return selectStartTag(node, document);
			}
		}
		return null;
	}

}
