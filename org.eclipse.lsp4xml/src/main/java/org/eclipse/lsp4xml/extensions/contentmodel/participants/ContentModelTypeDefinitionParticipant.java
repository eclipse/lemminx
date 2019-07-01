/**
 *  Copyright (c) 2019 Red Hat, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.contentmodel.participants;

import java.util.List;

import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.services.extensions.AbstractTypeDefinitionParticipant;
import org.eclipse.lsp4xml.services.extensions.ITypeDefinitionRequest;

/**
 * Extension to support XML type definition based on content model (XML Schema
 * type definition, etc)
 */
public class ContentModelTypeDefinitionParticipant extends AbstractTypeDefinitionParticipant {

	@Override
	protected boolean match(DOMDocument document) {
		return true;
	}

	@Override
	protected void doFindTypeDefinition(ITypeDefinitionRequest request, List<LocationLink> locations,
			CancelChecker cancelChecker) {
		ContentModelManager contentModelManager = request.getComponent(ContentModelManager.class);
		DOMNode node = request.getNode();
		if (node == null) {
			return;
		}
		DOMElement element = null;
		if (node.isElement()) {
			element = (DOMElement) node;
		} else if (node.isAttribute()) {
			element = ((DOMAttr) node).getOwnerElement();
		}
		if (element != null) {
			CMDocument cmDocument = contentModelManager.findCMDocument(element.getOwnerDocument(),
					element.getNamespaceURI());
			if (cmDocument != null) {
				LocationLink location = cmDocument.findTypeLocation(node);
				if (location != null) {
					locations.add(location);
				}
			}

		}
	}

}
