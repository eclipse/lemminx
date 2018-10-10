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
package org.eclipse.lsp4xml;

import static org.eclipse.lsp4j.jsonrpc.CompletableFutures.computeAsync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.DocumentLinkParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.FoldingRange;
import org.eclipse.lsp4j.FoldingRangeCapabilities;
import org.eclipse.lsp4j.FoldingRangeRequestParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4xml.commons.LanguageModelCache;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.commons.TextDocuments;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.dom.XMLParser;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.eclipse.lsp4xml.services.extensions.CompletionSettings;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;

/**
 * XML text document service.
 *
 */
public class XMLTextDocumentService implements TextDocumentService {

	private final XMLLanguageServer xmlLanguageServer;
	private final TextDocuments documents;
	private final LanguageModelCache<XMLDocument> xmlDocuments;
	private final CompletionSettings sharedCompletionSettings;
	private final FoldingRangeCapabilities sharedFoldingsSettings;
	private XMLFormattingOptions sharedFormattingOptions;

	private class BasicCancelChecker implements CancelChecker {

		private boolean canceled;

		@Override
		public void checkCanceled() {
			if (canceled) {
				throw new CancellationException();
			}
		}

		public void setCanceled(boolean canceled) {
			this.canceled = canceled;
		}

	}

	final ScheduledExecutorService delayer = Executors.newScheduledThreadPool(2);
	private ScheduledFuture<?> future;
	private BasicCancelChecker monitor;
	private boolean codeActionLiteralSupport;

	public XMLTextDocumentService(XMLLanguageServer xmlLanguageServer) {
		this.xmlLanguageServer = xmlLanguageServer;
		this.documents = new TextDocuments();
		XMLParser parser = XMLParser.getInstance();
		this.xmlDocuments = new LanguageModelCache<XMLDocument>(10, 60, documents, document -> parser.parse(document));
		this.sharedCompletionSettings = new CompletionSettings();
		this.sharedFoldingsSettings = new FoldingRangeCapabilities();
		this.sharedFormattingOptions = new XMLFormattingOptions(true); // to be sure that formattings options is not
																		// null.
	}

	public void updateClientCapabilities(ClientCapabilities capabilities) {
		TextDocumentClientCapabilities textDocumentClientCapabilities = capabilities.getTextDocument();
		if (textDocumentClientCapabilities != null) {
			// Completion settings
			sharedCompletionSettings.setCapabilities(textDocumentClientCapabilities.getCompletion());
			codeActionLiteralSupport = textDocumentClientCapabilities.getCodeAction() != null
					&& textDocumentClientCapabilities.getCodeAction().getCodeActionLiteralSupport() != null;
		}
	}

	public void updateCompletionSettings(CompletionSettings newCompletion) {
		sharedCompletionSettings.setAutoCloseTags(newCompletion.isAutoCloseTags());
	}

	public TextDocument getDocument(String uri) {
		return documents.get(uri);
	}

	public XMLDocument getXMLDocument(TextDocumentItem document) {
		return xmlDocuments.get(document);
	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
		return computeAsync((monitor) -> {
			String uri = params.getTextDocument().getUri();
			TextDocument document = getDocument(uri);
			XMLDocument xmlDocument = getXMLDocument(document);
			CompletionList list = getXMLLanguageService().doComplete(xmlDocument, params.getPosition(),
					sharedCompletionSettings, getFormattingSettings(uri));
			return Either.forRight(list);
		});
	}

	@Override
	public CompletableFuture<Hover> hover(TextDocumentPositionParams params) {
		return computeAsync((monitor) -> {
			TextDocument document = getDocument(params.getTextDocument().getUri());
			XMLDocument xmlDocument = getXMLDocument(document);
			return getXMLLanguageService().doHover(xmlDocument, params.getPosition());
		});
	}

	private XMLFormattingOptions getFormattingSettings(String uri) {
		// TODO: manage formattings per document URI (to support .editorconfig for
		// instance).
		return sharedFormattingOptions;
	}

	@Override
	public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams params) {
		return computeAsync((monitor) -> {
			TextDocument document = getDocument(params.getTextDocument().getUri());
			XMLDocument xmlDocument = getXMLDocument(document);
			return getXMLLanguageService().findDocumentHighlights(xmlDocument, params.getPosition());
		});
	}

	@Override
	public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(
			DocumentSymbolParams params) {
		return computeAsync((monitor) -> {
			TextDocument document = getDocument(params.getTextDocument().getUri());
			XMLDocument xmlDocument = getXMLDocument(document);
			return getXMLLanguageService().findDocumentSymbols(xmlDocument) //
					.stream() //
					.map(s -> {
						Either<SymbolInformation, DocumentSymbol> e = Either.forLeft(s);
						return e;
					}) //
					.collect(Collectors.toList());
		});
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
		return computeAsync((monitor) -> {
			String uri = params.getTextDocument().getUri();
			TextDocument document = getDocument(uri);
			return getXMLLanguageService().format(document, null,
					XMLFormattingOptions.create(params.getOptions(), getFormattingSettings(uri)));
		});
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
		return computeAsync((monitor) -> {
			String uri = params.getTextDocument().getUri();
			TextDocument document = getDocument(uri);
			return getXMLLanguageService().format(document, params.getRange(),
					XMLFormattingOptions.create(params.getOptions(), getFormattingSettings(uri)));
		});
	}

	@Override
	public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
		return computeAsync((monitor) -> {
			TextDocument document = getDocument(params.getTextDocument().getUri());
			XMLDocument xmlDocument = getXMLDocument(document);
			return getXMLLanguageService().doRename(xmlDocument, params.getPosition(), params.getNewName());
		});
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		documents.onDidOpenTextDocument(params);
		triggerValidation(params.getTextDocument().getUri(), params.getTextDocument().getVersion());
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		documents.onDidChangeTextDocument(params);
		triggerValidation(params.getTextDocument().getUri(), params.getTextDocument().getVersion());
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		documents.onDidCloseTextDocument(params);
		xmlDocuments.onDocumentRemoved(params.getTextDocument().getUri());
		TextDocumentIdentifier document = params.getTextDocument();
		String uri = document.getUri();
		xmlLanguageServer.getLanguageClient()
				.publishDiagnostics(new PublishDiagnosticsParams(uri, new ArrayList<Diagnostic>()));

	}

	@Override
	public CompletableFuture<List<FoldingRange>> foldingRange(FoldingRangeRequestParams params) {
		return computeAsync((monitor) -> {
			TextDocument document = getDocument(params.getTextDocument().getUri());
			return getXMLLanguageService().getFoldingRanges(document, sharedFoldingsSettings);
		});
	}

	@Override
	public CompletableFuture<List<DocumentLink>> documentLink(DocumentLinkParams params) {
		return computeAsync((monitor) -> {
			TextDocument document = getDocument(params.getTextDocument().getUri());
			XMLDocument xmlDocument = getXMLDocument(document);
			return getXMLLanguageService().findDocumentLinks(xmlDocument);
		});
	}

	@Override
	public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
		return computeAsync((monitor) -> {
			String uri = params.getTextDocument().getUri();
			TextDocument document = getDocument(uri);
			XMLDocument xmlDocument = getXMLDocument(document);
			return getXMLLanguageService()
					.doCodeActions(params.getContext(), params.getRange(), xmlDocument, getFormattingSettings(uri)) //
					.stream() //
					.map(ca -> {
						if (codeActionLiteralSupport) {
							Either<Command, CodeAction> e = Either.forRight(ca);
							return e;
						} else {
							List<Object> arguments = Arrays.asList(uri, document.getVersion(),
									ca.getEdit().getDocumentChanges().get(0).getEdits());
							Command command = new Command(ca.getTitle(), "_xml.applyCodeAction", arguments);
							Either<Command, CodeAction> e = Either.forLeft(command);
							return e;
						}
					}) //
					.collect(Collectors.toList());
		});
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {

	}

	private void triggerValidation(String uri, int version) {
		if (future != null && !future.isCancelled()) {
			future.cancel(true);
		}
		if (monitor != null) {
			monitor.setCanceled(true);
		}
		monitor = new BasicCancelChecker();
		triggerValidation(uri, version, monitor);
	}

	private void triggerValidation(String uri, int version, BasicCancelChecker monitor) {
		future = xmlLanguageServer.schedule(() -> {
			TextDocument currDocument = getDocument(uri);
			if (currDocument != null && currDocument.getVersion() == version) {
				XMLDocument xmlDocument = getXMLDocument(currDocument);
				List<Diagnostic> diagnostics = getXMLLanguageService().doDiagnostics(xmlDocument, monitor);
				monitor.checkCanceled();
				xmlLanguageServer.getLanguageClient()
						.publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
			}
		}, 500, TimeUnit.MILLISECONDS);
	}

	private XMLLanguageService getXMLLanguageService() {
		return xmlLanguageServer.getXMLLanguageService();
	}

	public void setSharedFormattingOptions(XMLFormattingOptions formattingOptions) {
		this.sharedFormattingOptions = formattingOptions;
	}

	public boolean isIncrementalSupport() {
		return documents.isIncremental();
	}

	public XMLFormattingOptions getSharedFormattingOptions() {
		return this.sharedFormattingOptions;
	}

	public void setIncrementalSupport(boolean incrementalSupport) {
		this.documents.setIncremental(incrementalSupport);
	}

}
