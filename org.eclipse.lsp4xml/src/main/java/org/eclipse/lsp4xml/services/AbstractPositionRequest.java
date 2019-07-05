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
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.dom.LineIndentInfo;
import org.eclipse.lsp4xml.services.extensions.IPositionRequest;

/**
 * Abstract class for position request.
 *
 */
abstract class AbstractPositionRequest implements IPositionRequest {

	private final DOMDocument xmlDocument;
	private final Position position;
	private final int offset;

	private String currentAttributeName;
	private final DOMNode node;
	private LineIndentInfo indentInfo;

	public AbstractPositionRequest(DOMDocument xmlDocument, Position position) throws BadLocationException {
		this.xmlDocument = xmlDocument;
		this.position = position;
		offset = xmlDocument.offsetAt(position);
		this.node = findNodeAt(xmlDocument, offset);
		if (node == null) {
			throw new BadLocationException("node is null at offset " + offset);
		}
	}

	protected abstract DOMNode findNodeAt(DOMDocument xmlDocument, int offset);

	@Override
	public DOMNode getNode() {
		return node;
	}

	@Override
	public DOMElement getParentElement() {
		DOMNode currentNode = getNode();
		if (!currentNode.isElement()) {
			// Node is not an element, search parent element.
			return currentNode.getParentElement();
		}
		DOMElement element = (DOMElement) currentNode;
		// node is an element, there are 3 cases
		// - case 1: <|
		// - case 2: <bean | >
		// - case 3: <bean /> | or <bean></bean> |
		// --> in thoses cases we must search parent of bean element
		if (element.isInStartTag(offset) || element.isInEndTag(offset)
				|| (element.isEndTagClosed() && element.getEnd() <= offset)) {
			return element.getParentElement();
		}
		// case 2: <bean> | --> in this case, parent element is the bean
		return (DOMElement) currentNode;
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public DOMDocument getXMLDocument() {
		return xmlDocument;
	}

	@Override
	public int getOffset() {
		return offset;
	}

	@Override
	public String getCurrentTag() {
		if (node != null && node.isElement() && ((DOMElement) node).getTagName() != null) {
			return ((DOMElement) node).getTagName();
		}
		return null;
	}

	@Override
	public String getCurrentAttributeName() {
		return currentAttributeName;
	}

	void setCurrentAttributeName(String currentAttributeName) {
		this.currentAttributeName = currentAttributeName;
	}

	@Override
	public LineIndentInfo getLineIndentInfo() throws BadLocationException {
		if (indentInfo == null) {
			int lineNumber = getPosition().getLine();
			indentInfo = getXMLDocument().getLineIndentInfo(lineNumber);
		}
		return indentInfo;
	}
}
