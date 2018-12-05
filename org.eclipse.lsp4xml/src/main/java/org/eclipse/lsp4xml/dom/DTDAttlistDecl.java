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

import java.util.ArrayList;

/**
 * DTD Attribute List declaration <!ATTLIST
 * 
 * @see https://www.w3.org/TR/REC-xml/#attdecls
 *
 */
public class DTDAttlistDecl extends DTDDeclNode {

	/**
	 * Format:
	 * 
	 * <!ATTLIST element-name attribute-name attribute-type "attribute-value>""
	 * 
	 * or
	 * 
	 * <!ATTLIST element-name 
	 * 			 attribute-name1 attribute-type1 "attribute-value1"
	 * 			 attribute-name2 attribute-type2 "attribute-value2"
	 * 			 ...
	 * >
	 */

	Integer elementNameStart, elementNameEnd;
	Integer attributeNameStart, attributeNameEnd;
	Integer attributeTypeStart, attributeTypeEnd;
	Integer attributeValueStart, attributeValueEnd;

	String elementName;
	String attributeName;
	String attributeType;
	String attributeValue;

	ArrayList<DTDAttlistDecl> internalChildren;
	
	public DTDAttlistDecl(int start, int end, DOMDocumentType parentDocumentType) {
		super(start, end, parentDocumentType);
	}

	public DOMDocumentType getParentDocumentType() {
		return parentDocumentType;
	}

	@Override
	public String getNodeName() {
		return getAttributeName();
	}

	/**
	 * Returns the element name
	 * 
	 * @return the element name
	 */
	public String getElementName() {
		elementName = getValueFromOffsets(parentDocumentType, elementName, elementNameStart, elementNameEnd);
		return elementName;
	}

	/**
	 * Returns the attribute name
	 * 
	 * @return the attribute name
	 */
	public String getAttributeName() {
		attributeName = getValueFromOffsets(parentDocumentType, attributeName, attributeNameStart, attributeNameEnd);
		return attributeName;
	}
	
	public String getAttributeType() {
		attributeType = getValueFromOffsets(parentDocumentType, attributeType, attributeTypeStart, attributeTypeEnd);
		return attributeType;
	}

	public String getAttributeValue() {
		attributeValue = getValueFromOffsets(parentDocumentType, attributeValue, attributeValueStart, attributeValueEnd);
		return attributeValue;
	}

	@Override
	public short getNodeType() {
		return DOMNode.DTD_ATT_LIST_NODE;
	}

	/**
	 * Add another internal attlist declaration to the list of children.
	 * 
	 * An ATTLIST decl can internally declare multiple declarations, see top of file.
	 * This will add another one to its list of additional declarations.
	 */
	void addAdditionalAttDecl(DTDAttlistDecl child) {
		if(internalChildren == null) {
			internalChildren = new ArrayList<DTDAttlistDecl>();
		}
		internalChildren.add(child);
	}

	public ArrayList<DTDAttlistDecl> getInternalChildren() {
		return internalChildren;
	}

	/**
	 * Returns true if this node's parent is the Doctype node.  
	 * 
	 * 
	 * This is used because an Attlist declaration can have multiple
	 * attribute declarations within a tag that are each represented
	 * by this class.
	 */
	public boolean isRootAttlist() {
		return this.parent.isDoctype();
	}

}
