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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

	public String tag;
	boolean closed = false;
	public Integer endTagStart;
	boolean selfClosed;
	boolean startTagClose;

	private Map<String, String> attributes;
	private List<Attr> attributeNodes;
	private final List<Node> children;
	public final int start;
	public int end;

	public final Node parent;
	private final XMLDocument ownerDocument;
	public String content;

	public Set<String> attributeNames() {
		return hasAttributes() ? attributes.keySet() : Collections.emptySet();
	}

	public Node(int start, int end, List<Node> children, Node parent, XMLDocument ownerDocument) {
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
		result.append(", tag: ");
		result.append(tag);
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

	public boolean isSameTag(String tagInLowerCase) {
		return this.tag != null && tagInLowerCase != null && this.tag.length() == tagInLowerCase.length()
				&& this.tag.toLowerCase().equals(tagInLowerCase);
	}

	public Node firstChild() {
		return this.children.get(0);
	}

	public Node lastChild() {
		return this.children != null && this.children.size() > 0 ? this.children.get(this.children.size() - 1) : null;
	}

	public Node findNodeBefore(int offset) {
		int idx = findFirst(this.children, c -> offset <= c.start) - 1;
		if (idx >= 0) {
			Node child = this.children.get(idx);
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
		int idx = findFirst(this.children, c -> offset <= c.start) - 1;
		if (idx >= 0) {
			Node child = this.children.get(idx);
			if (offset > child.start && offset <= child.end) {
				return child.findNodeAt(offset);
			}
		}
		return this;
	}

	/**
	 * Takes a sorted array and a function p. The array is sorted in such a way that
	 * all elements where p(x) is false are located before all elements where p(x)
	 * is true.
	 * 
	 * @returns the least x for which p(x) is true or array.length if no element
	 *          fullfills the given function.
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

	public String getAttributeValue(String name) {
		String value = hasAttributes() ? attributes.get(name) : null;
		if (value == null) {
			return null;
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

	public boolean hasTag(String tag) {
		for (Node node : children) {
			if (tag.equals(node.tag)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasAttribute(String attribute) {
		return hasAttributes() && attributes.containsKey(attribute);
	}

	/**
	 * Returns true if there are attributes and null otherwise.
	 * 
	 * @return true if there are attributes and null otherwise.
	 */
	public boolean hasAttributes() {
		return attributes != null;
	}

	public void setAttribute(String key, String value) {
		if (!hasAttributes()) {
			attributes = new HashMap<>();
		}
		attributes.put(key, value);
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributeNode(Attr attr) {
		if (attributeNodes == null) {
			attributeNodes = new ArrayList<>();
		}
		attributeNodes.add(attr);
	}

	public Attr getAttributeNode(String name) {
		return getAttributeNode(name, false);
	}

	public Attr getAttributeNode(String name, boolean last) {
		if (attributeNodes == null) {
			return null;
		}
		if (last) {
			for (int i = attributeNodes.size() - 1; i >= 0; i--) {
				Attr attr = attributeNodes.get(i);
				if (name.equals(attr.getName())) {
					return attr;
				}
			}
		} else {
			for (int i = 0; i < attributeNodes.size(); i++) {
				Attr attr = attributeNodes.get(i);
				if (name.equals(attr.getName())) {
					return attr;
				}
			}
		}
		return null;
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

	public short getNodeType() {
		return 0;
	}

	public Node getParent() {
		return parent;
	}

	public boolean isSelfClosed() {
		return selfClosed;
	}

	public boolean isClosed() {
		return closed;
	}

	public boolean isStartTagClose() {
		return startTagClose;
	}

	public boolean isComment() {
		return getNodeType() == Node.COMMENT_NODE;
	}

	public boolean isProcessingInstruction() {
		return (getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) && ((ProcessingInstruction) this).isProcessingInstruction();
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
}