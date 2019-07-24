/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.extensions.dtd.utils;

import java.util.function.Consumer;

import org.eclipse.lsp4xml.dom.DOMDocumentType;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.dom.DTDDeclParameter;
import org.eclipse.lsp4xml.dom.DTDElementDecl;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * DTD utilities.
 * 
 * @author Angelo ZERR
 *
 */
public class DTDUtils {

	/**
	 * Collect <!ELEMENT target name declared in the DTD according the origin name
	 * node.
	 * 
	 * @param originNameNode the origin name node (<ex :<!ATTLIST name).
	 * @param matchName      true if the attribute value must match the value of
	 *                       target attribute value and false otherwise.
	 * @param collector      collector to collect DTD <!ELEMENT target name.
	 */
	public static void searchDTDTargetElementDecl(DTDDeclParameter originNameNode, boolean matchName,
			Consumer<DTDDeclParameter> collector) {
		DOMDocumentType docType = originNameNode.getOwnerDocType();
		if (docType.hasChildNodes()) {
			// Loop for each <!ELEMENT.
			NodeList children = docType.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node node = children.item(i);
				if (node.getNodeType() == DOMNode.DTD_ELEMENT_DECL_NODE) {
					DTDElementDecl targetElementDecl = (DTDElementDecl) node;
					if (isValid(targetElementDecl)) {
						// node is <!ELEMENT which defines a name, collect if if it matches the origin
						// name node
						DTDDeclParameter targetElementName = targetElementDecl.getNameParameter();
						if ((!matchName || (matchName
								&& originNameNode.getParameter().equals(targetElementName.getParameter())))) {
							collector.accept(targetElementName);
						}
					}
				}
			}
		}
	}

	/**
	 * Returns true if the given <!ELEMENT defines a name and false otherwise.
	 * 
	 * @param elementDecl the <!ELEMENT
	 * @return true if the given <!ELEMENT defines a name and false otherwise.
	 */
	private static boolean isValid(DTDElementDecl elementDecl) {
		if (elementDecl == null) {
			return false;
		}
		// check <!ELEMENT defines a name
		return elementDecl.getNameParameter() != null;
	}

}
