/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.lsp4xml.dom;

/**
 * DTDDeclParameter
 */
public class DTDDeclParameter implements DOMRange {

	String parameter;

	int start, end;

	DTDDeclNode ownerNode;

	public DTDDeclParameter(DTDDeclNode ownerNode, int start, int end) {
		this.ownerNode = ownerNode;
		this.start = start;
		this.end = end;
	}

	@Override
	public DOMDocument getOwnerDocument() {
		return getOwnerDoctype().getOwnerDocument();
	}

	public DOMDocumentType getOwnerDoctype() {
		return getOwnerNode().getOwnerDoctype();
	}

	public DTDDeclNode getOwnerNode() {
		return ownerNode;
	}

	@Override
	public int getStart() {
		return start;
	}

	@Override
	public int getEnd() {
		return end;
	}

	public String getParameter() {
		if (parameter == null) {
			parameter = getOwnerDoctype().getSubstring(start, end);
		}
		return parameter;
	}

	/**
	 * Will get the parameter with the first and last character removed
	 * 
	 * Can be used to remove the quotations from a URL value...
	 */
	public String getParameterWithoutFirstAndLastChar() {
		if (parameter == null) {
			parameter = getOwnerDoctype().getSubstring(start + 1, end - 1);
		}
		return parameter;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DTDDeclParameter)) {
			return false;
		}
		DTDDeclParameter temp = (DTDDeclParameter) obj;
		return start == temp.start && end == temp.end;
	}

}