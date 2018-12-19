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

import java.util.ArrayList;

/**
 * DTDNotationDecl
 */
public class DTDNotationDecl extends DTDDeclNode {

	/**
	 * Format:
	 * 
	 * <!NOTATION Name PUBLIC PublicID>
	 * 
	 * or
	 * 
	 * <!NOTATION Name PUBLIC PublicID SystemID>
	 * 
	 * or
	 * 
	 * <!NOTATION Name SYSTEM SystemID>
	 */

	DTDDeclParameter name;
	DTDDeclParameter kind;
	DTDDeclParameter publicId;
	DTDDeclParameter systemId;

	public DTDNotationDecl(int start, int end, DOMDocumentType parentDocumentType) {
		super(start, end, parentDocumentType);
		declType = new DTDDeclParameter(parentDocumentType, start + 2, start + 10);
	}

	void setName(int start, int end) {
		name = addNewParameter(start, end);
	}

	public String getName() {
		return name != null ? name.getParameter() : null;
	}

	void setKind(int start, int end) {
		kind = addNewParameter(start, end);
	}

	public String getKind() {
		return kind != null ? kind.getParameter() : null;
	}

	void setPublicId(int start, int end) {
		publicId = addNewParameter(start, end);
	}

	public String getPublicId() {
		return publicId != null ? publicId.getParameter() : null;
	}

	void setSystemId(int start, int end) {
		systemId = addNewParameter(start, end);
	}

	public String getSystemId() {
		return systemId != null ? systemId.getParameter() : null;
	}

	@Override
	public short getNodeType() {
		return DOMNode.DTD_NOTATION_DECL;
	}
}