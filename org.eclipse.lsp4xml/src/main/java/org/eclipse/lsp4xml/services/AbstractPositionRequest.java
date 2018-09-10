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
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.Node;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.services.extensions.IPositionRequest;

/**
 * Abstract class for position request.
 *
 */
abstract class AbstractPositionRequest implements IPositionRequest {

	private final XMLDocument xmlDocument;
	private final Position position;
	private final int offset;

	private String currentAttributeName;
	private final Node node;

	public AbstractPositionRequest(XMLDocument xmlDocument, Position position) throws BadLocationException {
		this.xmlDocument = xmlDocument;
		this.position = position;
		offset = xmlDocument.offsetAt(position);
		this.node = findNodeAt(xmlDocument, offset);
		if (node == null) {
			throw new BadLocationException("node is null at offset " + offset);
		}
	}

	protected abstract Node findNodeAt(XMLDocument xmlDocument, int offset);

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public Node getParentNode() {
		Node currentNode = getNode();
		if (currentNode.tag == null) {
			return currentNode.parent;
		}
		int startTagEndOffset = currentNode.start + currentNode.tag.length() + ">".length();
		if (!(offset > startTagEndOffset && offset < currentNode.end)) {
			return currentNode.parent;
		}
		return currentNode;
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public XMLDocument getXMLDocument() {
		return xmlDocument;
	}

	@Override
	public int getOffset() {
		return offset;
	}

	@Override
	public String getCurrentTag() {
		if (node != null && node.tag != null) {
			return node.tag;
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

}
