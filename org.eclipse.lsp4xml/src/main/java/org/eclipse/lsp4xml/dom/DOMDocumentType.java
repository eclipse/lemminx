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
	Integer nameStart, nameEnd;
	Integer kindStart, kindEnd;
	Integer publicIdStart, publicIdEnd;
	Integer systemIdStart, systemIdEnd;
	Integer internalSubsetStart, internalSubsetEnd;
	

	private String name;
	private String kind; // SYSTEM || PUBLIC
	private String publicId;
	private String systemId;
	private String internalSubset;

	private String content; // |<!DOCTYPE ... >|

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
		if (name == null && this.nameStart != null && this.nameEnd != null) {
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
		if (kind == null && kindStart != null && kindEnd != null) {
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
		if (internalSubset == null && internalSubsetStart != null && internalSubsetEnd != null) {
			internalSubset = getSubstring(internalSubsetStart + 1, internalSubsetEnd - 1);
		}
		return internalSubset;
	}

	/**
	 * Returns the start offset of internal subset and null otherwise.
	 * 
	 * @return the start offset of internal subset and null otherwise.
	 */
	public Integer getStartInternalSubset() {
		return internalSubsetStart;
	}

	/**
	 * Returns the end offset of internal subset and null otherwise.
	 * 
	 * @return the end offset of internal subset and null otherwise.
	 */
	public Integer getEndInternalSubset() {
		return internalSubsetEnd;
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
		if (publicId == null && publicIdStart != null && publicIdEnd != null) {
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
		if (systemId == null && systemIdStart != null && systemIdEnd != null) {
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

	/**
	 * Since offset values are relative to 'this.start' we need to 
	 * subtract getStart() to make them relative to 'content'
	 */ 
	public String getSubstring(int start, int end) {
		return getContent().substring(start - getStart(), end - getStart());
	}

}
