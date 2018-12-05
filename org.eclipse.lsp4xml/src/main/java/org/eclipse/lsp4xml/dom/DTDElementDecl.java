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
 * DTD Element Declaration <!ELEMENT
 * 
 * @see https://www.w3.org/TR/REC-xml/#dt-eldecl
 *
 */
public class DTDElementDecl extends DTDDeclNode {

	/**
	 Formats:
	  
	 <!ELEMENT element-name category>
		or
	 <!ELEMENT element-name (element-content)>	 
	 
	 */

	Integer nameStart, nameEnd; // <!ELEMENT |element-name| category>
	Integer categoryStart, categoryEnd; // <!ELEMENT element-name |category|>
	Integer contentStart,contentEnd; // <!ELEMENT element-name |(element-content)|>

	String name;
	String category;
	String content;
	

	public DTDElementDecl(int start, int end, DOMDocumentType parentDocumentType) {
		super(start, end, parentDocumentType);
	}

	public DOMDocumentType getParentDocumentType() {
		return parentDocumentType;
	}

	@Override
	public String getNodeName() {
		return getName();
	}

	public String getName() {
		name = getValueFromOffsets(parentDocumentType, name, nameStart, nameEnd);
		return name;
	}

	public String getCategory() {
		category = getValueFromOffsets(parentDocumentType, category, categoryStart, categoryEnd);
		return category;
	}

	public String getContent() {
		content = getValueFromOffsets(parentDocumentType, content, contentStart, contentEnd);
		return content;
	}

	@Override
	public short getNodeType() {
		return DOMNode.DTD_ELEMENT_DECL_NODE;
	}

	/**
	 * Returns the offset of the end of tag <!ELEMENT
	 * 
	 * @return the offset of the end of tag <!ELEMENT
	 */
	public int getEndElementTag() {
		return getStart() + "<!ELEMENT".length();
	}

}
