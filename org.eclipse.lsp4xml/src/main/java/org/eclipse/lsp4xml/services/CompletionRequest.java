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

import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4xml.extensions.ICompletionRequest;
import org.eclipse.lsp4xml.internal.parser.BadLocationException;
import org.eclipse.lsp4xml.model.Node;
import org.eclipse.lsp4xml.model.XMLDocument;

/**
 * Completion request implementation.
 *
 */
class CompletionRequest implements ICompletionRequest {

	private final XMLDocument xmlDocument;
	private final Position position;
	private final FormattingOptions settings;
	private final int offset;

	private String currentTag;
	private String currentAttributeName;
	private final Node node;

	public CompletionRequest(XMLDocument xmlDocument, Position position, FormattingOptions settings)
			throws BadLocationException {
		this.xmlDocument = xmlDocument;
		this.position = position;
		this.settings = settings;
		offset = xmlDocument.offsetAt(position);
		this.node = xmlDocument.findNodeBefore(offset);
		if (node == null) {
			throw new BadLocationException("node is null at offset " + offset);
		}
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public Node getParentNode() {
		Node currentNode = getNode();
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
		return currentTag;
	}

	@Override
	public String getCurrentAttributeName() {
		return currentAttributeName;
	}

	void setCurrentTag(String currentTag) {
		this.currentTag = currentTag;
	}

	void setCurrentAttributeName(String currentAttributeName) {
		this.currentAttributeName = currentAttributeName;
	}

	@Override
	public FormattingOptions getFormattingSettings() {
		return settings;
	}
}
