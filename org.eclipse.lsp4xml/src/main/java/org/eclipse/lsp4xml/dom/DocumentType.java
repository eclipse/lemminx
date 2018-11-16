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
public class DocumentType extends Node implements org.w3c.dom.DocumentType {

	public enum DocumentTypeKind{
		PUBLIC,
		SYSTEM
	}

	/** Document type name. */
	String name;
	private DocumentTypeKind kind;
	private String publicId;
	private String systemId;
	private String internalDTD; //TODO: THIS IS TEMPORARY. Implement actual parsing.
	
	private String content;
	int startContent;
	int endContent;



	public DocumentType(int start, int end, XMLDocument ownerDocument) {
		super(start, end, ownerDocument);
	}

	/**
	 * @return the internalDTD
	 */
	public String getInternalDTD() {
		return internalDTD;
	}

	/**
	 * @param internalDTD the internalDTD to set
	 */
	public void setInternalDTD(String internalDTD) {
		this.internalDTD = internalDTD;
	}

	public String getContent() {
		if (content == null) {
			content = getOwnerDocument().getText().substring(getStartContent(), getEndContent());
		}
		return content;
	}

	public int getStartContent() {
		return startContent;
	}

	public int getEndContent() {
		return endContent;
	}

	/**
	 * The text immediately after DOCTYPE, "<!DOCTYPE this_is_the_Name ..."
	 */
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the DocumentTypeKind
	 */
	public DocumentTypeKind getKind() {
		return kind;
	}

	/**
	 * @param kind the DocumentTypeKind to set
	 */
	public void setKind(DocumentTypeKind kind) {
		this.kind = kind;
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
		return Node.DOCUMENT_TYPE_NODE;
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
		throw new UnsupportedOperationException();
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
		return this.publicId;
	}

	/**
	 * @param publicId the publicId to set
	 */
	public void setPublicId(String publicId) {
		this.publicId = cleanURL(publicId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.DocumentType#getSystemId()
	 */
	@Override
	public String getSystemId() {
		return this.systemId;
	}

	/**
	 * @param systemId the systemId to set
	 */
	public void setSystemId(String systemId) {
		this.systemId = cleanURL(systemId);
	}

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


	


}
