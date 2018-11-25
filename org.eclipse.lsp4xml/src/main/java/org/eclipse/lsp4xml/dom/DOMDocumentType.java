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

import org.w3c.dom.NamedNodeMap;

/**
 * A doctype node.
 *
 */
public class DOMDocumentType extends DOMNode implements org.w3c.dom.DocumentType {

	public enum DocumentTypeKind {
		PUBLIC, SYSTEM, INVALID
	}

	// Offset values relative to start of the XML Document
	int nameStart = -1;
	int nameEnd = -1;
	int kindStart = -1;
	int kindEnd = -1;
	int publicIdStart = -1;
	int publicIdEnd = -1;
	int systemIdStart = -1;
	int systemIdEnd = -1;
	int startInternalDTD = -1;
	int endInternalDTD = -1;

	private String name;
	private String kind;
	private String publicId;
	private String systemId;
	private String internalSubset;

	private String content;

	public DOMDocumentType(int start, int end, DOMDocument ownerDocument) {
		super(start, end, ownerDocument);
	}

	public String getContent() {
		if (content == null) {
			content = getOwnerDocument().getText().substring(getStart(), getEnd());
		}
		return content;
	}

	void setEnd(int end) {
		this.end = end;
		this.content = getOwnerDocument().getText().substring(start, end);
	}

	/**
	 * The text immediately after DOCTYPE, "<!DOCTYPE this_is_the_name ..."
	 */
	@Override
	public String getName() {
		if (name == null && this.nameStart != -1 && this.nameEnd != -1) {
			name = getSubstring(nameStart, nameEnd);
		}
		return name;
	}

	void setName(int start, int end) {
		nameStart = start;
		nameEnd = end;
	}

	/**
	 * @return the DocumentTypeKind
	 */
	public String getKind() {
		if (kind == null && kindStart != -1 && kindEnd != -1) {
			kind = getSubstring(kindStart, kindEnd);
		}
		return kind;
	}

	/**
	 * @param kind the DocumentTypeKind to set
	 */
	void setKind(int start, int end) {
		kindStart = start;
		kindEnd = end;
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
	 * @see org.w3c.dom.Node#getNodeType()
	 */
	@Override
	public short getNodeType() {
		return DOMNode.DOCUMENT_TYPE_NODE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.DocumentType#getEntities()
	 */
	@Override
	public NamedNodeMap getEntities() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.DocumentType#getInternalSubset()
	 */
	@Override
	public String getInternalSubset() {
		if (internalSubset == null && startInternalDTD != -1 && endInternalDTD != -1) {
			internalSubset = getSubstring(startInternalDTD, endInternalDTD);
		}
		return internalSubset;
	}

	void setInternalSubset(int start, int end) {
		startInternalDTD = start;
		endInternalDTD = end;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.DocumentType#getNotations()
	 */
	@Override
	public NamedNodeMap getNotations() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.DocumentType#getPublicId()
	 */
	@Override
	public String getPublicId() {
		if (publicId == null && publicIdStart != -1 && publicIdEnd != -1) {
			publicId = cleanURL(getSubstring(publicIdStart, publicIdEnd));
		}
		return publicId;
	}

	/**
	 * @param publicId the publicId to set
	 */
	void setPublicId(int start, int end) {
		publicIdStart = start;
		publicIdEnd = end;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.DocumentType#getSystemId()
	 */
	@Override
	public String getSystemId() {
		if (systemId == null && systemIdStart != -1 && systemIdEnd != -1) {
			systemId = cleanURL(getSubstring(systemIdStart, systemIdEnd));
		}
		return systemId;
	}

	/**
	 * @param systemId the systemId to set
	 */
	void setSystemId(int start, int end) {
		systemIdStart = start;
		systemIdEnd = end;
	}

	/**
	 * Removes trailing " characters
	 */
	private static String cleanURL(String url) {
		if (url == null) {
			return null;
		}
		if (url.isEmpty()) {
			return url;
		}
		int start = url.charAt(0) == '\"' ? 1 : 0;
		int end = url.charAt(url.length() - 1) == '\"' ? url.length() - 1 : url.length();
		return url.substring(start, end);
	}

	private String getSubstring(int start, int end) {
		return getContent().substring(start - getStart(), end - getStart());
	}

}
