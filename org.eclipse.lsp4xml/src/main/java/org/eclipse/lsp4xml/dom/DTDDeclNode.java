/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.lsp4xml.dom;

/**
 * DTDNode
 */
public class DTDDeclNode extends DOMNode{

	/**
	 * This class is the base for all declaration nodes for DTD's.
	 * 
	 * It can also be used to represent an undefined tag, meaning
	 * it is not any of: ELEMENT, ATTLIST, ENTITY, or NOTATION
	 */
	
	protected final DOMDocumentType parentDocumentType;

	Integer unrecognizedStart, unrecognizedEnd;

	String unrecognized; // holds all content after parsing goes wrong in a DTD declaration (ENTITY, ATTLIST, ELEMENT).

	public DTDDeclNode(int start, int end, DOMDocumentType parentDocumentType) {
		super(start, end, parentDocumentType != null ? parentDocumentType.getOwnerDocument() : null);
		this.parentDocumentType = parentDocumentType;
	}

	@Override
	public String getNodeName() {
		return null;
	}

	@Override
	public short getNodeType() {
		return DOMNode.DTD_DECL_NODE;
	}

	public String getUnrecognized() {
		unrecognized = getValueFromOffsets(parentDocumentType, unrecognized, unrecognizedStart, unrecognizedEnd);
		return unrecognized;
	}

	public static String getValueFromOffsets(DOMDocumentType document, String value, Integer start, Integer end) {
		if(value == null && start != null && end != null) {
			return document.getSubstring(start, end);
		}
		return value;
	}

	
}