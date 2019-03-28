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

import org.w3c.dom.DOMException;
import org.w3c.dom.TypeInfo;

/**
 * An attribute node.
 *
 */
public class DOMAttr extends DOMNode implements org.w3c.dom.Attr {

	private final String name;

	private final DOMNode nodeAttrName;

	private DOMNode nodeAttrValue;

	private String quotelessValue;//Value without quotes

	private String originalValue;//Exact value from document

	private final DOMNode ownerElement;

	private boolean hasDelimiter; // has '='

	class AttrNameOrValue extends DOMNode {

		private final DOMAttr ownerAttr;

		public AttrNameOrValue(int start, int end, DOMAttr ownerAttr) {
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

		public DOMAttr getOwnerAttr() {
			return ownerAttr;
		}
	}

	public DOMAttr(String name, DOMNode ownerElement) {
		this(name, -1, -1, ownerElement);
	}

	public DOMAttr(String name, int start, int end, DOMNode ownerElement) {
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
		return DOMNode.ATTRIBUTE_NODE;
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
	public DOMElement getOwnerElement() {
		return ownerElement.isElement() ? (DOMElement) ownerElement : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Attr#getValue()
	 */
	@Override
	public String getValue() {
		return quotelessValue;
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

	public DOMNode getNodeAttrName() {
		return nodeAttrName;
	}

	public void setDelimiter(boolean hasDelimiter) {
		this.hasDelimiter = hasDelimiter;
	}

	public boolean hasDelimiter() {
		return this.hasDelimiter;
	}

	/**
	 * Get original attribute value from the document.
	 * 
	 * This will include quotations (", ').
	 * @return attribute value with quotations if it had them.
	 */
	public String getOriginalValue() {
		return originalValue;
	}

	public void setValue(String value, int start, int end) {
		this.originalValue = value;
		this.quotelessValue = convertToQuotelessValue(value);
		this.nodeAttrValue = start != -1 ? new AttrNameOrValue(start, end, this) : null;
	}

	/**
	 * Returns a String of 'value' without surrounding quotes if it had them.
	 * @param value
	 * @return
	 */
	public static String convertToQuotelessValue(String value) {
		if (value == null) {
			return null;
		}
		if (value.isEmpty()) {
			return value;
		}
		char quoteValue = value.charAt(0);
		int start = quoteValue == '\"' || quoteValue == '\'' ? 1 : 0;
		quoteValue = value.charAt(value.length() - 1);
		int end = quoteValue == '\"' || quoteValue == '\'' ? value.length() - 1 : value.length();
		return value.substring(start, end);
	}

	/**
	 * Checks if 'value' has matching surrounding quotations.
	 * @param value
	 * @return
	 */
	public static boolean isQuoted(String value) {
		if (value == null) {
			return false;
		}
		if (value.isEmpty()) {
			return false;
		}
		char quoteValueStart = value.charAt(0);
		boolean start = quoteValueStart == '\"' || quoteValueStart == '\'' ? true : false;
		if(start == false) {
			return false;
		}
		char quoteValueEnd = value.charAt(value.length() - 1);
		boolean end = (quoteValueEnd == '\"' || quoteValueEnd == '\'') && quoteValueEnd == quoteValueStart ? true : false;
		return end;
	}

	public DOMNode getNodeAttrValue() {
		return nodeAttrValue;
	}

	public void setNodeAttrValue(DOMNode nodeAttrValue) {
		this.nodeAttrValue = nodeAttrValue;
	}

	public boolean valueContainsOffset(int offset) {
		return nodeAttrValue != null && offset >= nodeAttrValue.getStart() && offset < nodeAttrValue.getEnd();
	}

	public boolean isIncluded(int offset) {
		return DOMNode.isIncluded(getStart(), getEnd(), offset);
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
		result = prime * result + ((quotelessValue == null) ? 0 : quotelessValue.hashCode());
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
		DOMAttr other = (DOMAttr) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (quotelessValue == null) {
			if (other.quotelessValue != null)
				return false;
		} else if (!quotelessValue.equals(other.quotelessValue))
			return false;
		return true;
	}

}
