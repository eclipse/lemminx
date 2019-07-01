/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.services;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.services.extensions.IDefinitionRequest;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;

/**
 * Definition request implementation.
 *
 */
class DefinitionRequest extends AbstractPositionRequest implements IDefinitionRequest {

	private final XMLExtensionsRegistry extensionsRegistry;

	public DefinitionRequest(DOMDocument xmlDocument, Position position, XMLExtensionsRegistry extensionsRegistry)
			throws BadLocationException {
		super(xmlDocument, position);
		this.extensionsRegistry = extensionsRegistry;
	}

	@Override
	protected DOMNode findNodeAt(DOMDocument xmlDocument, int offset) {
		DOMNode node = xmlDocument.findNodeAt(offset);
		if (node != null && node.isElement()) {
			DOMAttr attr = xmlDocument.findAttrAt(node, offset);
			if (attr != null) {
				return attr;
			}
		}
		return node;
	}

	@Override
	public <T> T getComponent(Class clazz) {
		return extensionsRegistry.getComponent(clazz);
	}
}
