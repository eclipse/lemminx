/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.contentmodel.xsd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSNamedMap;
import org.eclipse.lsp4xml.contentmodel.CMDocument;
import org.eclipse.lsp4xml.contentmodel.CMElement;
import org.eclipse.lsp4xml.model.Node;
import org.eclipse.lsp4xml.model.XMLDocument;

/**
 * XSD document implementation.
 *
 */
public class XSDDocument implements CMDocument {

	private final XSModel model;

	private final Map<XSElementDeclaration, XSDElement> elementMappings;

	private Collection<CMElement> elements;

	public XSDDocument(XSModel model) {
		this.model = model;
		this.elementMappings = new HashMap<>();
	}

	private Collection<CMElement> getElements() {
		if (elements == null) {
			elements = new ArrayList<>();
			XSNamedMap map = model.getComponents(XSConstants.ELEMENT_DECLARATION);
			for (int j = 0; j < map.getLength(); j++) {
				XSElementDeclaration elementDeclaration = (XSElementDeclaration) map.item(j);
				elements.add(getXSDElement(elementDeclaration));
			}
		}
		return elements;
	}

	@Override
	public CMElement findCMElement(Node node) {
		String namespace = node.getOwnerDocument().getNamespaceURI();
		List<Node> paths = new ArrayList<>();
		Node element = node;
		while (element != null && !(element instanceof XMLDocument)) {
			paths.add(0, element);
			element = element.parent;
		}
		CMElement declaration = null;
		for (int i = 0; i < paths.size(); i++) {
			Node elt = paths.get(i);
			if (i == 0) {
				declaration = findElementDeclaration(elt.tag, namespace);
			} else {
				declaration = declaration.findCMElement(elt.tag, namespace);
			}
			if (declaration == null) {
				break;
			}
		}
		return declaration;
	}

	private CMElement findElementDeclaration(String tag, String namespace) {
		for (CMElement cmElement : getElements()) {
			if (cmElement.getName().equals(tag)) {
				return cmElement;
			}
		}
		return null;
	}

	CMElement getXSDElement(XSElementDeclaration elementDeclaration) {
		XSDElement element = elementMappings.get(elementDeclaration);
		if (element == null) {
			element = new XSDElement(this, elementDeclaration);
			elementMappings.put(elementDeclaration, element);
		}
		return element;
	}
}
