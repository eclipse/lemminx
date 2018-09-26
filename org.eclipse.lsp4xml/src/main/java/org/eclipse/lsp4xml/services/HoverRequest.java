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
package org.eclipse.lsp4xml.services;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.Attr;
import org.eclipse.lsp4xml.dom.Node;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.services.extensions.IHoverRequest;

/**
 * Hover request implementation.
 *
 */
class HoverRequest extends AbstractPositionRequest implements IHoverRequest {

	private Range tagRange;

	private boolean open;

	public HoverRequest(XMLDocument xmlDocument, Position position) throws BadLocationException {
		super(xmlDocument, position);
	}

	@Override
	protected Node findNodeAt(XMLDocument xmlDocument, int offset) {
		Node node = xmlDocument.findNodeAt(offset);
		if (node != null && node.isElement()) {
			Attr attr = xmlDocument.findAttrAt(node, offset);
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

}
