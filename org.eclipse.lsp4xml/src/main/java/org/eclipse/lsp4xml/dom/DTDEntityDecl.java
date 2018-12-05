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

import org.w3c.dom.Entity;
import org.w3c.dom.Node;

/**
 * DOM Entity declaration <!ENTITY
 * 
 * @see https://www.w3.org/TR/REC-xml/#dt-entdecl
 */
public class DTDEntityDecl extends DTDDeclNode implements Entity {


	/**
	* Formats:
	* 
	* <!ENTITY entity-name "entity-value">
	* 
	* or
	* 
	* <!ENTITY % entity-name "entity-value">
	* 
	* or
	* 
	* <!ENTITY % entity-name SYSTEM "systemId">
	* 
	* or
	* 
	* <!ENTITY % entity-name PUBLIC "publicId" "systemId">
	* 
	* or
	* 
	* <!ENTITY % entity-name SYSTEM "systemId" NDATA name>
	* 
	* or
	* 
	* <!ENTITY % entity-name PUBLIC "publicId" "systemId" NDATA name>
	*/

	String name;
	String value;
	String kind;
	String publicId;
	String systemId;

	Integer percentStart, percentEnd;
	Integer nameStart, nameEnd;
	Integer valueStart, valueEnd;
	Integer kindStart, kindEnd;
	Integer publicIdStart, publicIdEnd;
	Integer systemIdStart, systemIdEnd; 

	
	public DTDEntityDecl(int start, int end, DOMDocumentType parentDocumentType) {
		super(start, end, parentDocumentType);
	}

	public String getPercent() {
		if(percentStart != null && percentEnd != null) {
			return "%";
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getNodeName()
	 */
	@Override
	public String getNodeName() {
		name = getValueFromOffsets(parentDocumentType, name, nameStart, nameEnd);
		return name;
	}

	public String getValue() {
		value = getValueFromOffsets(parentDocumentType, value, valueStart, valueEnd);
		return value;
	}

	public String getKind() {
		kind = getValueFromOffsets(parentDocumentType, kind, kindStart, kindEnd);
		return kind;
	}

	@Override
	public short getNodeType() {
		return Node.ENTITY_NODE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Entity#getInputEncoding()
	 */
	@Override
	public String getInputEncoding() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Entity#getNotationName()
	 */
	@Override
	public String getNotationName() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Entity#getPublicId()
	 */
	@Override
	public String getPublicId() {
		publicId = getValueFromOffsets(parentDocumentType, publicId, publicIdStart, publicIdEnd);
		return publicId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Entity#getSystemId()
	 */
	@Override
	public String getSystemId() {
		systemId = getValueFromOffsets(parentDocumentType, systemId, systemIdStart, systemIdEnd);
		return systemId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Entity#getXmlEncoding()
	 */
	@Override
	public String getXmlEncoding() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Entity#getXmlVersion()
	 */
	@Override
	public String getXmlVersion() {
		throw new UnsupportedOperationException();
	}

}
