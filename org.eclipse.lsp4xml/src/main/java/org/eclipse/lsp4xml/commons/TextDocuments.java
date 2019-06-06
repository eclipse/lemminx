/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.commons;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;

/**
 * A manager for simple text documents
 */
public class TextDocuments implements ITextDocumentFactory {

	private boolean incremental;

	private final Map<String, TextDocument> documents;

	public TextDocuments() {
		documents = new HashMap<>();
	}

	/**
	 * Set the incremental support.
	 * 
	 * @param incremental
	 */
	public void setIncremental(boolean incremental) {
		this.incremental = incremental;
		synchronized (documents) {
			documents.values().forEach(document -> document.setIncremental(incremental));
		}
	}

	/**
	 * Returns true if text document is managed in incremental mode and false
	 * otherwise.
	 * 
	 * @return true if text document is managed in incremental mode and false
	 *         otherwise.
	 */
	public boolean isIncremental() {
		return incremental;
	}

	/**
	 * Returns the document for the given URI. Returns undefined if the document is
	 * not mananged by this instance.
	 *
	 * @param uri The text document's URI to retrieve.
	 * @return the text document or `undefined`.
	 */
	public TextDocument get(String uri) {
		synchronized (documents) {
			return documents.get(uri);
		}
	}

	@Override
	public TextDocument createDocument(TextDocumentItem document) {
		TextDocument doc = new TextDocument(document);
		doc.setIncremental(incremental);
		return doc;
	}

	public TextDocument onDidChangeTextDocument(DidChangeTextDocumentParams params) {
		synchronized (documents) {
			TextDocument document = getDocument(params.getTextDocument());
			if (document != null) {
				document.setVersion(params.getTextDocument().getVersion());
				document.update(params.getContentChanges());
				return document;
			}
		}
		return null;
	}

	public TextDocument onDidOpenTextDocument(DidOpenTextDocumentParams params) {
		TextDocumentItem item = params.getTextDocument();
		synchronized (documents) {
			TextDocument document = createDocument(item);
			documents.put(document.getUri(), document);
			return document;
		}
	}

	public TextDocument onDidCloseTextDocument(DidCloseTextDocumentParams params) {
		synchronized (documents) {
			TextDocument document = getDocument(params.getTextDocument());
			if (document != null) {
				documents.remove(params.getTextDocument().getUri());
			}
			return document;
		}
	}

	private TextDocument getDocument(TextDocumentIdentifier identifier) {
		return documents.get(identifier.getUri());
	}

	/**
	 * Returns the all opened documents.
	 * 
	 * @return the all opened documents.
	 */
	public Collection<TextDocument> all() {
		synchronized (documents) {
			return documents.values();
		}
	}
}
