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
public class Attr {

	private final String name;

	private final Node nodeName;

	private Node nodeValue;

	private String value;

	public Attr(String name, Node nodeName) {
		this.name = name;
		this.nodeName = nodeName;
	}

	public String getName() {
		return name;
	}

	public Node getNodeName() {
		return nodeName;
	}

	public void setValue(String value, Node nodeValue) {
		this.value = getValue(value);
		this.nodeValue = nodeValue;
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

	public Node getNodeValue() {
		return nodeValue;
	}

	public void setNodeValue(Node nodeValue) {
		this.nodeValue = nodeValue;
	}

	public boolean isIncluded(int offset) {
		return Node.isIncluded(getStart(), getEnd(), offset);
	}

	public int getStart() {
		return nodeName.start;
	}

	public int getEnd() {
		return nodeValue != null ? nodeValue.end : nodeName.end;
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
		if (nodeName != null && nodeName.getParent() != null && nodeName.getParent().isElement()) {
			return (Element) nodeName.getParent();
		}
		return null;
	}
}
