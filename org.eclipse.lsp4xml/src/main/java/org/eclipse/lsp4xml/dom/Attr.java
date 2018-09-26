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

/**
 * An attribute node.
 *
 */
public class Attr extends Node {

	private final String name;

	private final Node nodeAttrName;

	private Node nodeAttrValue;

	private String value;

	private final Node ownerElement;

	public Attr(String name, Node nodeAttrName, Node ownerElement) {
		super(-1, -1, ownerElement.getOwnerDocument());
		this.name = name;
		this.nodeAttrName = nodeAttrName;
		this.ownerElement = ownerElement;
	}

	public String getName() {
		return name;
	}

	public Node getNodeAttrName() {
		return nodeAttrName;
	}

	public void setValue(String value, Node nodeValue) {
		this.value = getValue(value);
		this.nodeAttrValue = nodeValue;
	}

	private static String getValue(String value) {
		if (value == null) {
			return null;
		}
		if (value.isEmpty()) {
			return value;
		}
		int start = value.charAt(0) == '\"' ? 1 : 0;
		int end = value.charAt(value.length() - 1) == '\"' ? value.length() - 1 : value.length();
		return value.substring(start, end);
	}

	public Node getNodeAttrValue() {
		return nodeAttrValue;
	}

	public void setNodeAttrValue(Node nodeAttrValue) {
		this.nodeAttrValue = nodeAttrValue;
	}

	public boolean isIncluded(int offset) {
		return Node.isIncluded(getStart(), getEnd(), offset);
	}

	@Override
	public int getStart() {
		return nodeAttrName.start;
	}

	@Override
	public int getEnd() {
		return nodeAttrValue != null ? nodeAttrValue.end : nodeAttrName.end;
	}

	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Attr other = (Attr) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	public Element getOwnerElement() {
		return ownerElement.isElement() ? (Element) ownerElement : null;
	}

	@Override
	public short getNodeType() {
		return Node.ATTRIBUTE_NODE;
	}
}
