/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.dom;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lemminx.extensions.xerces.xmlmodel.XMLModelDeclaration;
import org.w3c.dom.ProcessingInstruction;

/**
 * XML model processing instruction.
 * 
 * <pre>
 * 	&lt;?xml-model href="http://www.docbook.org/xml/5.0/xsd/docbook.xsd"?&gt;
 * </pre>
 * 
 * 
 * @see https://www.w3.org/TR/xml-model/
 */
public class XMLModel {

	private static final String XML_MODEL_PI = "xml-model";

	private final DOMProcessingInstruction processingInstruction;
	private XMLModelDeclaration declaration;

	public XMLModel(DOMProcessingInstruction processingInstruction) {
		this.processingInstruction = processingInstruction;
	}

	/**
	 * Returns the location of the referenced schema
	 * 
	 * @return the location of the referenced schema
	 */
	public String getHref() {
		String data = processingInstruction.getData();
		if (data == null) {
			return null;
		}
		if (declaration == null) {
			declaration = XMLModelDeclaration.parse(data.toCharArray(), 0, data.length());
		}
		return declaration.getHref();
	}

	/**
	 * Returns the declared xml-model list.
	 * 
	 * @param document the DOM document.
	 * 
	 * @return the declared xml-model list.
	 */
	static List<XMLModel> createXMLModels(DOMDocument document) {
		List<DOMNode> children = document.getChildren();
		if (children != null && !children.isEmpty()) {
			return children.stream().filter(XMLModel::isXMLModel)
					.map(node -> new XMLModel((DOMProcessingInstruction) node)).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	/**
	 * Returns true if the given node is a xml-model processing instruction and
	 * false otherwise.
	 * 
	 * @param node the DOM node.
	 * @return true if the given node is a xml-model processing instruction and
	 *         false otherwise.
	 */
	public static boolean isXMLModel(DOMNode node) {
		return node.isProcessingInstruction() && XML_MODEL_PI.equals(((ProcessingInstruction) node).getTarget());
	}

}
