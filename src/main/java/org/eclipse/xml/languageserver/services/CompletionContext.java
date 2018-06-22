package org.eclipse.xml.languageserver.services;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.xml.languageserver.internal.parser.BadLocationException;
import org.eclipse.xml.languageserver.model.XMLDocument;

public class CompletionContext {

	private final TextDocumentItem document;
	private final Position position;
	private final XMLDocument xmlDocument;
	private final int offset;

	private String currentTag;
	private String currentAttributeName;

	public CompletionContext(TextDocumentItem document, Position position, XMLDocument xmlDocument)
			throws BadLocationException {
		this.document = document;
		this.position = position;
		this.xmlDocument = xmlDocument;
		offset = xmlDocument.offsetAt(position);
	}

	public TextDocumentItem getDocument() {
		return document;
	}

	public Position getPosition() {
		return position;
	}

	public XMLDocument getXMLDocument() {
		return xmlDocument;
	}

	public int getOffset() {
		return offset;
	}

	public String getCurrentTag() {
		return currentTag;
	}

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
