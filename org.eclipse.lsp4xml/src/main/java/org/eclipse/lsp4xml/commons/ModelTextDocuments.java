package org.eclipse.lsp4xml.commons;

import java.util.function.BiFunction;

import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

public class ModelTextDocuments<T> extends TextDocuments<ModelTextDocument<T>> {

	private final BiFunction<TextDocument, CancelChecker, T> parse;

	public ModelTextDocuments(BiFunction<TextDocument, CancelChecker, T> parse) {
		this.parse = parse;
	}

	@Override
	public ModelTextDocument<T> createDocument(TextDocumentItem document) {
		ModelTextDocument<T> doc = new ModelTextDocument<T>(document, parse);
		doc.setIncremental(isIncremental());
		return doc;
	}
}
