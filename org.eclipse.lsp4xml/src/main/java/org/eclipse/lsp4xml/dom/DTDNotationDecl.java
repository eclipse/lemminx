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

	Integer nameStart, nameEnd;
	Integer kindStart, kindEnd;
	Integer publicIdStart, publicIdEnd;
	Integer systemIdStart, systemIdEnd;

	String name;
	String kind;
	String publicId;
	String systemId;

	public DTDNotationDecl(int start, int end, DOMDocumentType parentDocumentType) {
		super(start, end, parentDocumentType);
	}

	void setName(int start, int end) {
		nameStart = start;
		nameEnd = end;
	}

	public String getName() {
		name = getValueFromOffsets(parentDocumentType, name, nameStart, nameEnd);
		return name;
	}

	void setKind(int start, int end) {
		kindStart = start;
		kindEnd = end;
	}

	public String getKind() {
		kind = getValueFromOffsets(parentDocumentType, kind, kindStart, kindEnd);
		return kind;
	}

	void setPublicId(int start, int end) {
		publicIdStart = start;
		publicIdEnd = end;
	}

	public String getPublicId() {
		publicId = getValueFromOffsets(parentDocumentType, publicId, publicIdStart, publicIdEnd);
		return publicId;
	}

	void setSystemId(int start, int end) {
		systemIdStart = start;
		systemIdEnd = end;
	}

	public String getSystemId() {
		systemId = getValueFromOffsets(parentDocumentType, systemId, systemIdStart, systemIdEnd);
		return systemId;
	}

	@Override
	public short getNodeType() {
		return DOMNode.DTD_NOTATION_DECL;
	}
}