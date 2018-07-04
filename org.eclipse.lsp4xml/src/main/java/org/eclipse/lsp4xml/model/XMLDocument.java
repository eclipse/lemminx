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
import org.eclipse.lsp4xml.internal.parser.BadLocationException;

/**
 * XML document.
 *
 */
public class XMLDocument extends Node {

	private static String DEFAULT_DELIMTER = System.getProperty("line.separator");

	private final ListLineTracker lineTracker;
	private SchemaLocation schemaLocation;
	private boolean schemaLocationInitialized;
	private final String text;
	private final String uri;

	public XMLDocument(String text, String uri) {
		super(0, text.length(), new ArrayList<>(), null, null);
		lineTracker = new ListLineTracker();
		lineTracker.set(text);
		this.text = text;
		this.uri = uri;
		schemaLocationInitialized = false;
	}

	public List<Node> getRoots() {
		return super.children;
	}

	public Position positionAt(int position) throws BadLocationException {
		int lineNumber = lineTracker.getLineNumberOfOffset(position);
		Line line = lineTracker.getLineInformation(lineNumber);
		return new Position(lineNumber, position - line.offset);
	}

	public int offsetAt(Position position) throws BadLocationException {
		int lineNumber = position.getLine();
		Line line = lineTracker.getLineInformation(lineNumber);
		return line.offset + position.getCharacter();
	}

	public String lineText(int lineNumber) throws BadLocationException {
		Line line = lineTracker.getLineInformation(lineNumber);
		return text.substring(line.offset, line.offset + line.length);
	}

	public String lineDelimiter(int lineNumber) throws BadLocationException {
		String lineDelimiter = lineTracker.getLineDelimiter(lineNumber);
		if (lineDelimiter == null) {
			if (lineTracker.getNumberOfLines() > 0) {
				lineDelimiter = lineTracker.getLineInformation(0).delimiter;
			}
		}
		if (lineDelimiter == null) {
			lineDelimiter = DEFAULT_DELIMTER;
		}
		return lineDelimiter;
	}

	public SchemaLocation getSchemaLocation() {
		if (!schemaLocationInitialized) {
			schemaLocation = createSchemaLocation();
			schemaLocationInitialized = true;
		}
		return schemaLocation;
	}

	private SchemaLocation createSchemaLocation() {
		List<Node> roots = getRoots();
		if (roots == null || roots.size() < 1) {
			return null;
		}
		Node root = roots.get(0);
		String value = root.getAttributeValue("xsi:schemaLocation");
		if (value == null) {
			return null;
		}
		return new SchemaLocation(value);
	}

	public String getNamespaceURI() {
		List<Node> roots = getRoots();
		if (roots == null || roots.size() < 1) {
			return null;
		}
		Node root = roots.get(0);
		return root.getAttributeValue("xmlns");

	}

	public String getText() {
		return text;
	}

	public String getUri() {
		return uri;
	}

	@Override
	public XMLDocument getOwnerDocument() {
		return this;
	}

}