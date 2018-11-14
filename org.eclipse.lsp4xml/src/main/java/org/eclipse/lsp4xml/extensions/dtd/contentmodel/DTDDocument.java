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
package org.eclipse.lsp4xml.extensions.dtd.contentmodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.xerces.impl.dtd.DTDGrammar;
import org.eclipse.lsp4xml.dom.Element;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMElementDeclaration;

/**
 * DTD document.
 * @author azerr
 *
 */
public class DTDDocument implements CMDocument {

	private final DTDGrammar grammar;
	private List<CMElementDeclaration> elements;

	public DTDDocument(DTDGrammar grammar) {
		this.grammar = grammar;
	}

	@Override
	public Collection<CMElementDeclaration> getElements() {
		elements = null;
		if (elements == null) {
			elements = new ArrayList<>();
			int index = grammar.getFirstElementDeclIndex();
			while (index != -1) {
				DTDElementDeclaration elementDecl = new DTDElementDeclaration();
				grammar.getElementDecl(index, elementDecl);
				elements.add(elementDecl);
				index = grammar.getNextElementDeclIndex(index);
			}
		}
		return elements;
	}

	@Override
	public CMElementDeclaration findCMElement(Element element, String namespace) {
		List<Element> paths = new ArrayList<>();
		while (element != null && (namespace == null || namespace.equals(element.getNamespaceURI()))) {
			paths.add(0, element);
			element = element.getParentNode() instanceof Element ? (Element) element.getParentNode() : null;
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


}
