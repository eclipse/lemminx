/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.dtd.contentmodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.xerces.impl.dtd.XMLElementDecl;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMElementDeclaration;

/**
 * DTD element declaration.
 *
 */
public class CMDTDElementDeclaration extends XMLElementDecl implements CMElementDeclaration {

	private final int index;
	private final CMDTDDocument document;
	private List<CMElementDeclaration> elements;
	private List<CMAttributeDeclaration> attributes;

	public CMDTDElementDeclaration(CMDTDDocument document, int index) {
		this.document = document;
		this.index = index;
	}

	@Override
	public String getName() {
		return super.name.localpart;
	}

	@Override
	public String getNamespace() {
		return null;
	}

	@Override
	public Collection<CMAttributeDeclaration> getAttributes() {
		if (attributes == null) {
			attributes = new ArrayList<>();
			document.collectAttributesDeclaration(this, attributes);
		}
		return attributes;
	}

	@Override
	public Collection<CMElementDeclaration> getElements() {
		if (elements == null) {
			elements = new ArrayList<>();
			document.collectElementsDeclaration(getName(), elements);
		}
		return elements;
	}

	@Override
	public Collection<CMElementDeclaration> getPossibleElements(DOMElement parentElement, int offset) {
		// TODO: support valid element declaration for DTD
		return getElements();
	}

	@Override
	public CMElementDeclaration findCMElement(String tag, String namespace) {
		for (CMElementDeclaration cmElement : getElements()) {
			if (cmElement.getName().equals(tag)) {
				return cmElement;
			}
		}
		return null;
	}

	@Override
	public CMAttributeDeclaration findCMAttribute(String attributeName) {
		for (CMAttributeDeclaration cmAttribute : getAttributes()) {
			if (cmAttribute.getName().equals(attributeName)) {
				return cmAttribute;
			}
		}
		return null;
	}

	@Override
	public String getDocumentation() {
		return null;
	}

	@Override
	public boolean isEmpty() {
		return super.type == XMLElementDecl.TYPE_EMPTY;
	}

	@Override
	public Collection<String> getEnumerationValues() {
		return null;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public String getDocumentURI() {
		return document.getURI();
	}
}
