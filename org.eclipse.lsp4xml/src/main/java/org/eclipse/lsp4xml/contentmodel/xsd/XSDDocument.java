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
import org.eclipse.lsp4xml.dom.Element;

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

	@Override
	public Collection<CMElementDeclaration> getElements() {
		if (elements == null) {
			elements = new ArrayList<>();
			XSNamedMap map = model.getComponents(XSConstants.ELEMENT_DECLARATION);
			for (int j = 0; j < map.getLength(); j++) {
				XSElementDeclaration elementDeclaration = (XSElementDeclaration) map.item(j);
				CMElementDeclaration cmElement = getXSDElement(elementDeclaration);
				// check element declaration is not already added (ex: xs:annotation)
				if (!elements.contains(cmElement)) {					
					elements.add(cmElement);
				}
			}
		}
		return elements;
	}

	@Override
	public CMElementDeclaration findCMElement(Element node, String namespace) {
		List<Element> paths = new ArrayList<>();
		Element element = node;
		while (element != null && (namespace == null || namespace.equals(element.getNamespaceURI()))) {
			paths.add(0, element);
			element = element.getParent() instanceof Element ? (Element) element.getParent() : null;
		}
		CMElementDeclaration declaration = null;
		for (int i = 0; i < paths.size(); i++) {
			Element elt = paths.get(i);
			if (i == 0) {
				declaration = findElementDeclaration(elt.getLocalName(), namespace);
			} else {
				declaration = declaration.findCMElement(elt.getLocalName(), namespace);
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
