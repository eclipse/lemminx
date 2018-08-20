/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;

/**
 * XML document.
 *
 */
public class XMLDocument extends Node {

	private SchemaLocation schemaLocation;
	private NoNamespaceSchemaLocation noNamespaceSchemaLocation;
	private boolean schemaLocationInitialized;

	private final TextDocument textDocument;

	public XMLDocument(TextDocument textDocument) {
		super(0, textDocument.getText().length(), new ArrayList<>(), null, null);
		this.textDocument = textDocument;
		schemaLocationInitialized = false;
	}

	public List<Node> getRoots() {
		return super.children;
	}

	public Position positionAt(int position) throws BadLocationException {
		return textDocument.positionAt(position);
	}

	public int offsetAt(Position position) throws BadLocationException {
		return textDocument.offsetAt(position);
	}

	public String lineText(int lineNumber) throws BadLocationException {
		return textDocument.lineText(lineNumber);
	}

	public String lineDelimiter(int lineNumber) throws BadLocationException {
		return textDocument.lineDelimiter(lineNumber);
	}

	public SchemaLocation getSchemaLocation() {
		if (!schemaLocationInitialized) {
			initializeSchemaLocation();
		}
		return schemaLocation;
	}

	public NoNamespaceSchemaLocation getNoNamespaceSchemaLocation() {
		if (!schemaLocationInitialized) {
			initializeSchemaLocation();
		}
		return noNamespaceSchemaLocation;
	}

	private void initializeSchemaLocation() {
		if (schemaLocationInitialized) {
			return;
		}
		List<Node> roots = getRoots();
		if (roots == null || roots.size() < 1) {
			return;
		}
		Node root = getDocumentElement();
		if (root == null) {
			return;
		}
		schemaLocation = createSchemaLocation(root);
		noNamespaceSchemaLocation = createNoNamespaceSchemaLocation(root);
		schemaLocationInitialized = true;
	}

	private Node getDocumentElement() {
		List<Node> roots = getRoots();
		if (roots != null) {
			for (Node node : roots) {
				if (!node.isProlog) {
					return node;
				}
			}
		}
		return null;
	}

	private SchemaLocation createSchemaLocation(Node root) {
		String value = root.getAttributeValue("xsi:schemaLocation");
		if (value == null) {
			return null;
		}
		return new SchemaLocation(value);
	}

	private NoNamespaceSchemaLocation createNoNamespaceSchemaLocation(Node root) {
		String value = root.getAttributeValue("xsi:noNamespaceSchemaLocation");
		if (value == null) {
			return null;
		}
		return new NoNamespaceSchemaLocation(value);
	}

	public String getNamespaceURI() {
		Node root = getDocumentElement();
		return root != null ? root.getAttributeValue("xmlns") : null;
	}

	public String getText() {
		return textDocument.getText();
	}

	public String getUri() {
		return textDocument.getUri();
	}

	public TextDocument getTextDocument() {
		return textDocument;
	}

	@Override
	public XMLDocument getOwnerDocument() {
		return this;
	}

	public boolean hasSchemaLocation() {
		return getSchemaLocation() != null || getNoNamespaceSchemaLocation() != null;
	}

}