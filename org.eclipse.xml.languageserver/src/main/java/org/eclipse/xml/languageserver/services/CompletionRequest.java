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
package org.eclipse.xml.languageserver.services;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.xml.languageserver.extensions.ICompletionRequest;
import org.eclipse.xml.languageserver.internal.parser.BadLocationException;
import org.eclipse.xml.languageserver.model.Node;
import org.eclipse.xml.languageserver.model.XMLDocument;

/**
 * Completion request implementation.
 *
 */
class CompletionRequest implements ICompletionRequest {

	private final TextDocumentItem document;
	private final Position position;
	private final XMLDocument xmlDocument;
	private final int offset;

	private String currentTag;
	private String currentAttributeName;
	private final Node node;

	public CompletionRequest(TextDocumentItem document, Position position, XMLDocument xmlDocument)
			throws BadLocationException {
		this.document = document;
		this.position = position;
		this.xmlDocument = xmlDocument;
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
	public TextDocumentItem getDocument() {
		return document;
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

}
