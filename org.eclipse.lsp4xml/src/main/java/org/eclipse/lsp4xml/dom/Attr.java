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

import org.w3c.dom.DOMException;
import org.w3c.dom.TypeInfo;

/**
 * An attribute node.
 *
 */
public class Attr extends Node implements org.w3c.dom.Attr {

	private final String name;

	private final Node nodeAttrName;

	private Node nodeAttrValue;

	private String value;

	private final Node ownerElement;

	class AttrNameOrValue extends Node {

		private final Attr ownerAttr;

		public AttrNameOrValue(int start, int end, Attr ownerAttr) {
			super(start, end, ownerAttr.getOwnerDocument());
			this.ownerAttr = ownerAttr;
		}

		@Override
		public String getNodeName() {
			return null;
		}

		@Override
		public short getNodeType() {
			return -1;
		}

		public Attr getOwnerAttr() {
			return ownerAttr;
		}
	}

	public Attr(String name, Node ownerElement) {
		this(name, -1, -1, ownerElement);
	}

	public Attr(String name, int start, int end, Node ownerElement) {
		super(-1, -1, ownerElement.getOwnerDocument());
		this.name = name;
		this.nodeAttrName = start != -1 ? new AttrNameOrValue(start, end, this) : null;
		this.ownerElement = ownerElement;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getNodeType()
	 */
	@Override
	public short getNodeType() {
		return Node.ATTRIBUTE_NODE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getNodeName()
	 */
	@Override
	public String getNodeName() {
		return getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Attr#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Attr#getOwnerElement()
	 */
	public Element getOwnerElement() {
		return ownerElement.isElement() ? (Element) ownerElement : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Attr#getValue()
	 */
	@Override
	public String getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Attr#getSchemaTypeInfo()
	 */
	@Override
	public TypeInfo getSchemaTypeInfo() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Attr#getSpecified()
	 */
	@Override
	public boolean getSpecified() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Attr#isId()
	 */
	@Override
	public boolean isId() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Attr#setValue(java.lang.String)
	 */
	@Override
	public void setValue(String value) throws DOMException {
		setValue(value, -1, -1);
	}

	public Node getNodeAttrName() {
		return nodeAttrName;
	}

	public void setValue(String value, int start, int end) {
		this.value = getValue(value);
		this.nodeAttrValue = start != -1 ? new AttrNameOrValue(start, end, this) : null;
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

}
