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
package org.eclipse.lsp4xml.services;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.services.extensions.IHoverRequest;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.settings.XMLHoverSettings;

/**
 * Hover request implementation.
 *
 */
class HoverRequest extends AbstractPositionRequest implements IHoverRequest {

	private final XMLHoverSettings settings;

	private Range tagRange;

	private boolean open;

	public HoverRequest(DOMDocument xmlDocument, Position position, XMLHoverSettings settings,
			XMLExtensionsRegistry extensionsRegistry) throws BadLocationException {
		super(xmlDocument, position, extensionsRegistry);
		this.settings = settings;
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
	public Range getTagRange() {
		return tagRange;
	}

	public void setTagRange(Range tagRange) {
		this.tagRange = tagRange;
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	@Override
	public boolean canSupportMarkupKind(String kind) {
		return settings != null && settings.getCapabilities() != null
				&& settings.getCapabilities().getContentFormat() != null
				&& settings.getCapabilities().getContentFormat().contains(kind);

	}
}
