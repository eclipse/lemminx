/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.eclipse.lsp4xml.dom.DOMText;
import org.eclipse.lsp4xml.dom.DTDAttlistDecl;
import org.eclipse.lsp4xml.dom.DTDDeclNode;
import org.eclipse.lsp4xml.dom.DTDDeclParameter;
import org.eclipse.lsp4xml.dom.DTDElementDecl;
import org.eclipse.lsp4xml.dom.parser.Scanner;
import org.eclipse.lsp4xml.dom.parser.TokenType;
import org.eclipse.lsp4xml.dom.parser.XMLScanner;

/**
 * XML position utility.
 *
 */
public class XMLPositionUtility {

	private static final Logger LOGGER = Logger.getLogger(XMLPositionUtility.class.getName());

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

		DOMNode curr = parent;
		DOMNode child;
		while (curr != null) {
			child = findUnclosedChildNode(childTag, curr.getChildren());
			if (child == null) {
				curr = findUnclosedChildNode(curr.getChildren());
			} else {
				return createRange(child.getStart() + 1, child.getStart() + 1 + childTag.length(), document);
			}
		}

		String parentName = ((DOMElement) parent).getTagName();
		return createRange(parent.getStart() + 2, parent.getStart() + 2 + parentName.length(), document);
	}

	public static DOMNode findUnclosedChildNode(List<DOMNode> children) {
		for (DOMNode child: children) {
			if (!child.isClosed()) {
				return child;
			}
		}
		return null;
	}

	static DOMNode findUnclosedChildNode(String childTag, List<DOMNode> children) {
		for (DOMNode child : children) {
			if (child.isElement() && childTag != null && childTag.equals(((DOMElement) child).getTagName())
					&& !child.isClosed()) {
				return child;
			}
		}
		return null;
	}

	public static Range selectRootStartTag(DOMDocument document) {
		DOMNode root = document.getDocumentElement();
		return selectStartTag(root);
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
	 * <p>This will include the tag it starts in if the offset is within a tag's content: </p>
	 * <p>		
	 * 		{@code  <a> <b> | </b> </a> } , will give {@code </b>} 
	 * </p>
	 * 
	 * or within an unclosed end tag:
	 * 
	 * <p>		
	 * 		{@code  <a> <b>  </b> </a| <c>} , will give  {@code </a>} 
	 * </p>
	 * 
	 * 
	 * <p>		
	 * 		{@code  <a> <b|>  </b> </a>} , will give  {@code </a>} 
	 * </p>
	 * 
	 */
	public static Range selectPreviousNodesEndTag(int offset, DOMDocument document) {
		
		DOMNode node = null;
		DOMNode nodeAt = document.findNodeAt(offset);
		if(nodeAt != null && nodeAt.isElement()) {
			node = nodeAt;
		} else {
			DOMNode nodeBefore = document.findNodeBefore(offset);
			if(nodeBefore != null && nodeBefore.isElement()) {
				node = nodeBefore;
			}
		}
		if(node != null) {
			DOMElement element = (DOMElement) node;
			if(element.isClosed() && !element.isEndTagClosed()) {
				return selectEndTag(element.getEnd(), document);
			}
		}
		
		// boolean firstBracket = false;
		int i = offset;
		char c = document.getText().charAt(i);
		while (i >= 0) {
			if (c == '>') {
				return selectEndTag(i, document);
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

	public static Range selectContent(int offset, DOMDocument document) {
		DOMNode node = document.findNodeAt(offset);
		if (node != null) {
			if (node.isElement()) {
				DOMElement element = (DOMElement) node;
				if(node.hasChildNodes()) {
					return createRange(element.getStartTagCloseOffset() + 1, element.getEndTagOpenOffset(), document);
				}
				// node has NO content (ex: <root></root>, select the start tag
				return selectStartTag(node);
			} else if (node.isText()) {
				DOMText text = (DOMText) node;
				return createRange(text.getStartContent(), text.getEndContent(), document);
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

	/**
	 * Will give the range for the last VALID DTD Decl parameter at 'offset'.
	 * An unrecognized Parameter is not considered VALID,
	 * 
	 * eg: "<!ELEMENT elementName (content) UNRECOGNIZED_CONTENT"
	 * 		will give the range of 1 character length, after '(content)'
	 */
	public static Range getLastValidDTDDeclParameter(int offset, DOMDocument document, boolean selectWholeParameter) {
		DOMNode node = document.findNodeAt(offset);
		if (node instanceof DTDDeclNode) {
			DTDDeclNode decl = (DTDDeclNode) node;
			List<DTDDeclParameter> params = decl.getParameters();
			DTDDeclParameter finalParam;
			if(params == null || params.isEmpty()) {
				return createRange(decl.declType.getStart(), decl.declType.getEnd(), document);
			}
			if(decl.unrecognized != null && decl.unrecognized.equals(params.get(params.size() - 1))) {
				if(params.size() > 1) { // not only an unrecognized parameter
					finalParam = params.get(params.size() - 2);
				} else {
					finalParam = decl.declType; // no valid parameters
				}
			}
			else {
				finalParam = params.get(params.size() - 1);
			}
			if(selectWholeParameter) {
				return createRange(finalParam.getStart(), finalParam.getEnd(), document);
			}
			return createRange(finalParam.getEnd(), finalParam.getEnd() + 1, document);
		}
		return null;
	}

	public static Range getLastValidDTDDeclParameter(int offset, DOMDocument document) {
		return getLastValidDTDDeclParameter(offset, document, false);
	}

	/**
	 * Will give the range for the last VALID DTD Decl parameter at 'offset'.
	 * An unrecognized Parameter is not considered VALID,
	 * 
	 * eg: <!ELEMENT elementName (content) UNRECOGNIZED_CONTENT
	 * 		will give the range 1 character after '(content)'
	 */
	public static Range getLastValidDTDDeclParameterOrUnrecognized(int offset, DOMDocument document) {
		DOMNode node = document.findNodeAt(offset);
		if (node instanceof DTDDeclNode) {
			DTDDeclNode decl = (DTDDeclNode) node;
			if(decl instanceof DTDAttlistDecl) {
				DTDAttlistDecl attlist = (DTDAttlistDecl) decl;
				ArrayList<DTDAttlistDecl> internal = attlist.getInternalChildren();
				if(internal != null && !internal.isEmpty()) {
					decl = internal.get(internal.size() - 1); //get last internal decl
				}
			}
			List<DTDDeclParameter> params = decl.getParameters();
			if(params == null || params.isEmpty()) {
				return createRange(decl.declType.getStart(), decl.declType.getEnd(), document);
			}
			DTDDeclParameter finalParam = params.get(params.size() - 1);
			if(decl.unrecognized != null && decl.unrecognized.equals(finalParam)) {
				return createRange(finalParam.getStart(), finalParam.getEnd(), document);
			}
			return createRange(finalParam.getEnd(), finalParam.getEnd() + 1, document);
		}
		return null;
	}

	/**
	 * Will give the range for the last DTD Decl parameter at 'offset'.
	 * An unrecognized Parameter is considered as well.
	 * 
	 * eg: <!ELEMENT elementName (content) UNRECOGNIZED_CONTENT
	 * 		will give the range 1 character after '(content)'
	 */
	public static Range getLastDTDDeclParameter(int offset, DOMDocument document) {
		DOMNode node = document.findNodeAt(offset);
		if (node instanceof DTDDeclNode) {
			DTDDeclNode decl = (DTDDeclNode) node;
			List<DTDDeclParameter> params = decl.getParameters();
			DTDDeclParameter lastParam;
			if(params != null && !params.isEmpty()) {
				lastParam = params.get(params.size() - 1);
				return createRange(lastParam.getStart(), lastParam.getEnd(), document);
			}
		}
		return null;
	}

	public static Range selectDTDDeclTagNameAt(int offset, DOMDocument document) {
		DOMNode node = document.findNodeAt(offset);
		if (node instanceof DTDDeclNode) {
			DTDDeclNode declNode = (DTDDeclNode) node;
			return createRange(declNode.declType.getStart(), declNode.declType.getEnd(), document);
		}
		return null;
	}

	public static Range selectWholeTag(int offset, DOMDocument document) {
		DOMNode node = document.findNodeAt(offset);
		if(node != null) {
			return createRange(node.getStart(), node.getEnd(), document);
		}
		return null;
	}

	public static Range getElementDeclMissingContentOrCategory(int offset, DOMDocument document) {
		DOMNode node = document.findNodeAt(offset);
		if (node instanceof DTDElementDecl) {
			DTDElementDecl declNode = (DTDElementDecl) node;
			List<DTDDeclParameter> params = declNode.getParameters();
			if(params.isEmpty()) {
				return null;
			}
			if(params.size() == 1) {
				DTDDeclParameter param = params.get(0);
				return createRange(param.getEnd(), param.getEnd() + 1, document);
			}
			else { 
				return createRange(params.get(1).getStart(), params.get(1).getEnd(), document);
			}
		}
		return null;
	}

	public static boolean doesTagCoverPosition(Range startTagRange, Range endTagRange, Position position) {
		return startTagRange != null && covers(startTagRange, position)
				|| endTagRange != null && covers(endTagRange, position);
	}

	public static boolean covers(Range range, Position position) {
		return isBeforeOrEqual(range.getStart(), position) && isBeforeOrEqual(position, range.getEnd());
	}

	public static boolean isBeforeOrEqual(Position pos1, Position pos2) {
		return pos1.getLine() < pos2.getLine()
				|| (pos1.getLine() == pos2.getLine() && pos1.getCharacter() <= pos2.getCharacter());
	}

	public static Range getTagNameRange(TokenType tokenType, int startOffset, DOMDocument xmlDocument) {

		Scanner scanner = XMLScanner.createScanner(xmlDocument.getText(), startOffset);

		TokenType token = scanner.scan();
		while (token != TokenType.EOS && token != tokenType) {
			token = scanner.scan();
		}
		if (token != TokenType.EOS) {
			try {
				return new Range(xmlDocument.positionAt(scanner.getTokenOffset()),
						xmlDocument.positionAt(scanner.getTokenEnd()));
			} catch (BadLocationException e) {
				LOGGER.log(Level.SEVERE,
						"While creating Range in XMLHighlighting the Scanner's Offset was a BadLocation", e);
				return null;
			}
		}
		return null;
	}

}
