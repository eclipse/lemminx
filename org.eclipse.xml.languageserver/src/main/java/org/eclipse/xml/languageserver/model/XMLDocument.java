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
package org.eclipse.xml.languageserver.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Position;
import org.eclipse.xml.languageserver.internal.parser.BadLocationException;

/**
 * XML document.
 *
 */
public class XMLDocument extends Node {

	private final ListLineTracker lineTracker;
	private SchemaLocation schemaLocation;

	public XMLDocument(String text) {
		super(0, text.length(), new ArrayList<>(), null, null);
		lineTracker = new ListLineTracker();
		lineTracker.set(text);		
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

	public SchemaLocation getSchemaLocation() {
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

	public void updateSchemaLocation() {
		schemaLocation = createSchemaLocation();
	}

}