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
 * DTD Attribute List declaration <!ATTRIBUTES
 *
 */
public class DTDAttList extends DOMNode {

	private final DOMDocumentType ownerDTDDocument;
	String name;

	public DTDAttList(int start, int end, DOMDocumentType ownerDTDDocument) {
		super(start, end, ownerDTDDocument.getOwnerDocument());
		this.ownerDTDDocument = ownerDTDDocument;
	}

	public DOMDocumentType getOwnerDocumentType() {
		return ownerDTDDocument;
	}

	@Override
	public String getNodeName() {
		return getName();
	}

	public String getName() {
		return name;
	}

	@Override
	public short getNodeType() {
		return DOMNode.DTD_ATT_LIST_NODE;
	}

}
