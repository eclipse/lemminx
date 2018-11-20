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

import org.apache.xerces.impl.dtd.XMLElementDecl;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMElementDeclaration;

/**
 * DTD element declaration.
 * 
 *
 */
public class CMDTDElementDeclaration extends XMLElementDecl implements CMElementDeclaration {

	private final CMDTDDocument document;
	private List<CMElementDeclaration> elements;
	private List<CMAttributeDeclaration> attributes;

	public CMDTDElementDeclaration(CMDTDDocument document) {
		this.document = document;
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
			document.collectAttributesDeclaration(getName(), attributes);
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
		return null;
	}

	@Override
	public String getDocumentation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<String> getEnumerationValues() {
		// TODO Auto-generated method stub
		return null;
	}

}
