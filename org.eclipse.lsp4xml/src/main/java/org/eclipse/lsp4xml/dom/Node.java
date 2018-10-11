/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * XML node.
 *
 */
public class Node {

	/**
	 * The node is an <code>Element</code>.
	 */
	public static final short ELEMENT_NODE = 1;

	/**
	 * The node is an <code>Attribute</code>.
	 */
	public static final short ATTRIBUTE_NODE = 2;

	/**
	 * The node is a <code>Text</code> node.
	 */
	public static final short TEXT_NODE = 3;

	/**
	 * The node is a <code>CDATASection</code>.
	 */
	public static final short CDATA_SECTION_NODE = 4;

	/**
	 * The node is a <code>ProcessingInstruction</code>.
	 */
	public static final short PROCESSING_INSTRUCTION_NODE = 7;

	/**
	 * The node is a <code>Comment</code>.
	 */
	public static final short COMMENT_NODE = 8;

	/**
	 * The node is a <code>Document</code>.
	 */
	public static final short DOCUMENT_NODE = 9;
	/**
	 * The node is a <code>DocumentType</code>.
	 */
	public static final short DOCUMENT_TYPE_NODE = 10;

	boolean closed = false;

	private List<Attr> attributeNodes;
	private List<Node> children;

	final int start;
	int end;

	Node parent;
	private final XMLDocument ownerDocument;

	public Node(int start, int end, XMLDocument ownerDocument) {
		this.start = start;
		this.end = end;
		this.ownerDocument = ownerDocument;
	}

	Node(int start, int end, List<Node> children, Node parent, XMLDocument ownerDocument) {
		this.start = start;
		this.end = end;
		this.children = children;
		this.parent = parent;
		this.ownerDocument = ownerDocument;
	}

	public XMLDocument getOwnerDocument() {
		return ownerDocument;
	}

	@Override
	public String toString() {
		return toString(0);
	}

	private String toString(int indent) {
		StringBuilder result = new StringBuilder("");
		for (int i = 0; i < indent; i++) {
			result.append("\t");
		}
		result.append("{start: ");
		result.append(start);
		result.append(", end: ");
		result.append(end);
		result.append(", name: ");
		result.append(getNodeName());
		result.append(", closed: ");
		result.append(closed);
		if (children != null && children.size() > 0) {
			result.append(", \n");
			for (int i = 0; i < indent + 1; i++) {
				result.append("\t");
			}
			result.append("children:[");
			for (int i = 0; i < children.size(); i++) {
				Node node = children.get(i);
				result.append("\n");
				result.append(node.toString(indent + 2));
				if (i < children.size() - 1) {
					result.append(",");
				}
			}
			result.append("\n");
			for (int i = 0; i < indent + 1; i++) {
				result.append("\t");
			}
			result.append("]");
			result.append("\n");
			for (int i = 0; i < indent; i++) {
				result.append("\t");
			}
			result.append("}");
		} else {
			result.append("}");
		}
		return result.toString();
	}

	public Node firstChild() {
		return this.children != null && children.size() > 0 ? this.children.get(0) : null;
	}

	public Node lastChild() {
		return this.children != null && this.children.size() > 0 ? this.children.get(this.children.size() - 1) : null;
	}

	public Node findNodeBefore(int offset) {
		List<Node> children = getChildren();
		int idx = findFirst(children, c -> offset <= c.start) - 1;
		if (idx >= 0) {
			Node child = children.get(idx);
			if (offset > child.start) {
				if (offset < child.end) {
					return child.findNodeBefore(offset);
				}
				Node lastChild = child.lastChild();
				if (lastChild != null && lastChild.end == child.end) {
					return child.findNodeBefore(offset);
				}
				return child;
			}
		}
		return this;
	}

	public Node findNodeAt(int offset) {
		List<Node> children = getChildren();
		int idx = findFirst(children, c -> offset <= c.start) - 1;
		if (idx >= 0) {
			Node child = children.get(idx);
			if (isIncluded(child, offset)) {
				return child.findNodeAt(offset);
			}
		}
		return this;
	}

	/**
	 * Returns true if the node included the given offset and false otherwise.
	 * 
	 * @param node
	 * @param offset
	 * @return true if the node included the given offset and false otherwise.
	 */
	public static boolean isIncluded(Node node, int offset) {
		if (node == null) {
			return false;
		}
		return isIncluded(node.start, node.end, offset);
	}

	public static boolean isIncluded(int start, int end, int offset) {
		return offset > start && offset <= end;
	}

	public Attr findAttrAt(int offset) {
		Node node = findNodeAt(offset);
		return findAttrAt(node, offset);
	}

	public Attr findAttrAt(Node node, int offset) {
		if (node != null && node.hasAttributes()) {
			for (Attr attr : node.getAttributeNodes()) {
				if (attr.isIncluded(offset)) {
					return attr;
				}
			}
		}
		return null;
	}

	/**
	 * Takes a sorted array and a function p. The array is sorted in such a way that
	 * all elements where p(x) is false are located before all elements where p(x)
	 * is true.
	 * 
	 * @returns the least x for which p(x) is true or array.length if no element
	 *          full fills the given function.
	 */
	private static <T> int findFirst(List<T> array, Function<T, Boolean> p) {
		int low = 0, high = array.size();
		if (high == 0) {
			return 0; // no children
		}
		while (low < high) {
			int mid = (int) Math.floor((low + high) / 2);
			if (p.apply(array.get(mid))) {
				high = mid;
			} else {
				low = mid + 1;
			}
		}
		return low;
	}

	public Attr getAttributeNode(String name) {
		if (!hasAttributes()) {
			return null;
		}
		for (Attr attr : attributeNodes) {
			if (name.equals(attr.getName())) {
				return attr;
			}
		}
		return null;
	}

	public String getAttributeValue(String name) {
		Attr attr = getAttributeNode(name);
		String value = attr != null ? attr.getValue() : null;
		if (value == null) {
			return null;
		}
		if (value.isEmpty()) {
			return value;
		}
		// remove quote
		char c = value.charAt(0);
		if (c == '"' || c == '\'') {
			if (value.charAt(value.length() - 1) == c) {
				return value.substring(1, value.length() - 1);
			}
			return value.substring(1, value.length());
		}
		return value;
	}

	public boolean hasAttribute(String name) {
		return hasAttributes() && getAttributeNode(name) != null;
	}

	/**
	 * Returns true if there are attributes and null otherwise.
	 * 
	 * @return true if there are attributes and null otherwise.
	 */
	public boolean hasAttributes() {
		return attributeNodes != null && attributeNodes.size() != 0;
	}

	public void setAttribute(String name, String value) {
		Attr attr = getAttributeNode(name);
		if (attr == null) {
			attr = new Attr(name, null, this);
			setAttributeNode(attr);
		}
		attr.setValue(value, null);
	}

	public void setAttributeNode(Attr attr) {
		if (attributeNodes == null) {
			attributeNodes = new ArrayList<>();
		}
		attributeNodes.add(attr);
	}

	public List<Attr> getAttributeNodes() {
		return attributeNodes;
	}

	/**
	 * Returns the node children.
	 * 
	 * @return the node children.
	 */
	public List<Node> getChildren() {
		if (children == null) {
			return Collections.emptyList();
		}
		return children;
	}

	/**
	 * Returns true if node has children and false otherwise.
	 * 
	 * @return true if node has children and false otherwise.
	 */
	public boolean hasChildren() {
		return children != null && !children.isEmpty();
	}

	/**
	 * Add node child
	 * 
	 * @param child the node child to add.
	 */
	public void addChild(Node child) {
		child.parent = this;
		if (children == null) {
			children = new ArrayList<>();
		}
		getChildren().add(child);
	}

	/**
	 * Returns node child at the given index.
	 * 
	 * @param index
	 * @return node child at the given index.
	 */
	public Node getChild(int index) {
		return getChildren().get(index);
	}

	public Node getParent() {
		return parent;
	}

	public boolean isClosed() {
		return closed;
	}

	public Element getParentElement() {
		Node parent = getParent();
		while (parent != null && parent != getOwnerDocument()) {
			if (parent.isElement()) {
				return (Element) parent;
			}
		}
		return null;
	}

	/**
	 * Checks if previous sibling node is of 'type'
	 * @param type
	 * @return
	 */
	public boolean isPreviousNodeType(Short type) {
		int currentIndex = this.getParent().getChildren().indexOf(this);
		return currentIndex > 0 ? this.getParent().getChild(currentIndex - 1).isText() : false;
	}

	public boolean isFirstChildNode() {
		return this.getParent().getChildren().indexOf(this) == 0;
	}

	public boolean isComment() {
		return getNodeType() == Node.COMMENT_NODE;
	}

	public boolean isProcessingInstruction() {
		return (getNodeType() == Node.PROCESSING_INSTRUCTION_NODE)
				&& ((ProcessingInstruction) this).isProcessingInstruction();
	}

	public boolean isProlog() {
		return (getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) && ((ProcessingInstruction) this).isProlog();
	}

	public boolean isCDATA() {
		return getNodeType() == Node.CDATA_SECTION_NODE;
	}

	public boolean isDoctype() {
		return getNodeType() == Node.DOCUMENT_TYPE_NODE;
	}

	public boolean isElement() {
		return getNodeType() == Node.ELEMENT_NODE;
	}
	
	public boolean isAttribute() {
		return getNodeType() == Node.ATTRIBUTE_NODE;
	}

	public boolean isText() {
		return getNodeType() == Node.TEXT_NODE;
	}

	public boolean isCharacterData() {
		return isCDATA() || isText() || isProcessingInstruction() || isComment();
	}

	public short getNodeType() {
		return 0;
	}

	public String getNodeName() {
		return null;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}
}