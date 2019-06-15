package org.eclipse.lsp4xml.commons;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;

public class ModelTextDocument<T> extends TextDocument {

	private final BiFunction<TextDocument, CancelChecker, T> parse;

	private CompletableFuture<T> model;

	public ModelTextDocument(TextDocumentItem document, BiFunction<TextDocument, CancelChecker, T> parse) {
		super(document);
		this.parse = parse;
	}

	public ModelTextDocument(String text, String uri, BiFunction<TextDocument, CancelChecker, T> parse) {
		super(text, uri);
		this.parse = parse;
	}

	public CompletableFuture<T> getModel() {
		if (model == null) {
			int version = super.getVersion();
			model = CompletableFutures.computeAsync((requestCancelChecker) -> {
				long start = System.currentTimeMillis();
				try {
					System.err.println("Start parse " + version);
					MultiCancelChecker cancelChecker = new MultiCancelChecker(requestCancelChecker,
							new TextDocumentVersionChecker(this, version));
					return parse.apply(this, cancelChecker);
				} catch (CancellationException e) {
					System.err.println("STOPPED " + version + " in " + (System.currentTimeMillis() - start) + " ms");
					throw e;
				} finally {
					System.err.println("End parse " + version + " in " + (System.currentTimeMillis() - start) + " ms");
				}
			});
		}
		return model;
	}

	@Override
	public void setText(String text) {
		super.setText(text);
		cancelModel();
	}

	@Override
	public void setVersion(int version) {
		super.setVersion(version);
		cancelModel();
	}
	
	private void cancelModel() {
		if (model != null) {
			model.cancel(true);
			model = null;
		}
	}

}
