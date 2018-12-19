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

	DTDDeclParameter name;
	DTDDeclParameter category;
	DTDDeclParameter content;
	

	public DTDElementDecl(int start, int end, DOMDocumentType parentDocumentType) {
		super(start, end, parentDocumentType);
		declType = new DTDDeclParameter(parentDocumentType, start + 2, start + 9);
	}

	public DOMDocumentType getParentDocumentType() {
		return parentDocumentType;
	}

	@Override
	public String getNodeName() {
		return getName();
	}

	public String getName() {
		return name != null ? name.getParameter() : null;
	}

	public void setName(int start, int end) {
		name = addNewParameter(start, end);
	}

	public String getCategory() {
		return category != null ? category.getParameter() : null;
	}

	public void setCategory(int start, int end) {
		category = addNewParameter(start, end);
	}

	public String getContent() {
		return content != null ? content.getParameter() : null;
	}

	public void setContent(int start, int end) {
		content = addNewParameter(start, end);
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
