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
package org.eclipse.lsp4xml.commons;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
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
		documents.values().forEach(document -> document.setIncremental(incremental));
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
		return documents.get(uri);
	}

	public void onDidOpenTextDocument(DidOpenTextDocumentParams params) {
		TextDocumentItem document = params.getTextDocument();
		documents.put(document.getUri(), createDocument(document));
	}

	@Override
	public TextDocument createDocument(TextDocumentItem document) {
		TextDocument doc = new TextDocument(document);
		doc.setIncremental(incremental);
		return doc;
	}

	public void onDidChangeTextDocument(DidChangeTextDocumentParams params) {
		List<TextDocumentContentChangeEvent> changes = params.getContentChanges();
		TextDocument document = get(params.getTextDocument().getUri());
		if (document != null) {
			document.setVersion(params.getTextDocument().getVersion());
			document.update(changes);
		}
	}

	public void onDidCloseTextDocument(DidCloseTextDocumentParams params) {
		documents.remove(params.getTextDocument().getUri());
	}
}
