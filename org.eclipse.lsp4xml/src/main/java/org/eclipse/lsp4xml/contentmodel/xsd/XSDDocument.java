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
import org.eclipse.lsp4xml.contentmodel.model.CMDocument;
import org.eclipse.lsp4xml.contentmodel.model.CMElementDeclaration;
import org.eclipse.lsp4xml.model.Node;
import org.eclipse.lsp4xml.model.XMLDocument;

/**
 * XSD document implementation.
 *
 */
public class XSDDocument implements CMDocument {

	private final XSModel model;

	private final Map<XSElementDeclaration, XSDElementDeclaration> elementMappings;

	private Collection<CMElementDeclaration> elements;

	public XSDDocument(XSModel model) {
		this.model = model;
		this.elementMappings = new HashMap<>();
	}

	private Collection<CMElementDeclaration> getElements() {
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
	public CMElementDeclaration findCMElement(Node node) {
		String namespace = node.getOwnerDocument().getNamespaceURI();
		List<Node> paths = new ArrayList<>();
		Node element = node;
		while (element != null && !(element instanceof XMLDocument)) {
			paths.add(0, element);
			element = element.parent;
		}
		CMElementDeclaration declaration = null;
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

	private CMElementDeclaration findElementDeclaration(String tag, String namespace) {
		for (CMElementDeclaration cmElement : getElements()) {
			if (cmElement.getName().equals(tag)) {
				return cmElement;
			}
		}
		return null;
	}

	CMElementDeclaration getXSDElement(XSElementDeclaration elementDeclaration) {
		XSDElementDeclaration element = elementMappings.get(elementDeclaration);
		if (element == null) {
			element = new XSDElementDeclaration(this, elementDeclaration);
			elementMappings.put(elementDeclaration, element);
		}
		return element;
	}
}
