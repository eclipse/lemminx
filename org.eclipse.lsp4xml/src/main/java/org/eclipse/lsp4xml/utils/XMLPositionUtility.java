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
import org.eclipse.lsp4xml.dom.Node;
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

	public static String getNameFromArguents(Object[] arguments, int index) {
		if(arguments == null) {
			return "ARGUMENTS_ARE_NULL";
		}
		if(index < arguments.length) {
			return (String) arguments[index];
		}
		return "INDEX_OUT_OF_RANGE";
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


	public static Range selectChildEndTag(String childTag, int offset, XMLDocument document) {
		Node parent = document.findNodeAt(offset);
		if(parent.tag == null) {
			return null;
		}
		if(parent != null) {
			Node child = findChildNode(childTag,parent.getChildren());
			if(child != null) {
				return createRange(child.start + 1, child.start + 1 + childTag.length(), document);
			}
		}
		return null;
	}


	static Node findChildNode(String childTag, List<Node> children) {
		for (Node child : children) {
			if(child.tag.equals(childTag) && !child.isClosed()) {
				return child;
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

	public static int selectCurrentTagOffset(int offset, XMLDocument document) {
		Node element = document.findNodeAt(offset);
		if (element != null) {
			return element.start; // <
			
		}
		return -1;
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

	public static Range selectAllAttributes(int offset, XMLDocument document) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Range selectFirstNonWhitespaceText(int offset, XMLDocument document) {
		Node element = document.findNodeAt(offset);
		if (element != null) {
			for (Node node : element.getChildren()) {
				if (node.content != null) {
					int start = node.start;
					Integer end = null;
					for (int i = 0; i < node.content.length(); i++) {
						char c = node.content.charAt(i);
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
	 * Finds the offset of the first tag it comes across behind the
	 * given offset. 
	 * 
	 * This excludes the tag it starts in if offset is within a tag.
	 */
	public static Range selectPreviousEndTag( int offset, XMLDocument document) {
		//boolean firstBracket = false;
		int i = offset;
		char c = document.getText().charAt(i);
		while(i >= 0) {
			if(c == '>') {
				//if(firstBracket) {
					return selectStartTag(i, document);
				//}
				//else {
				//	firstBracket = true;
				//}
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

}
