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

	public XMLTextDocumentService(XMLLanguageServer xmlLanguageServer) {
		this.xmlLanguageServer = xmlLanguageServer;
		this.documents = new TextDocuments();
		XMLParser parser = XMLParser.getInstance();
		this.xmlDocuments = new LanguageModelCache<XMLDocument>(10, 60, document -> parser.parse(document));
		this.sharedCompletionSettings = new CompletionSettings();
		this.sharedFoldingsSettings = new FoldingRangeCapabilities();
		this.sharedFormattingOptions = new XMLFormattingOptions(true); // to be sure that formattings options is not null.
	}

	public void updateClientCapabilities(ClientCapabilities capabilities) {
		TextDocumentClientCapabilities textDocumentClientCapabilities = capabilities.getTextDocument();
		if (textDocumentClientCapabilities != null) {
			// Completion settings
			sharedCompletionSettings.update(textDocumentClientCapabilities.getCompletion());
		}

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
			TextDocument document = getDocument(params.getTextDocument().getUri());
			XMLDocument xmlDocument = getXMLDocument(document);
			CompletionList list = getXMLLanguageService().doComplete(xmlDocument, params.getPosition(),
					sharedCompletionSettings, sharedFormattingOptions);
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
			TextDocument document = getDocument(params.getTextDocument().getUri());
			return getXMLLanguageService().format(document, null,
					XMLFormattingOptions.create(params.getOptions(), sharedFormattingOptions));
		});
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
		return computeAsync((monitor) -> {
			TextDocument document = getDocument(params.getTextDocument().getUri());
			return getXMLLanguageService().format(document, params.getRange(),
					XMLFormattingOptions.create(params.getOptions(), sharedFormattingOptions));
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
		TextDocument document = getDocument(params.getTextDocument().getUri());
		triggerValidation(document, params.getTextDocument().getVersion());
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		documents.onDidChangeTextDocument(params);
		TextDocument document = getDocument(params.getTextDocument().getUri());
		if (document != null) {
			triggerValidation(document, params.getTextDocument().getVersion());
		}
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
			return getXMLLanguageService().findDocumentLinks(document);
		});
	}

	@Override
	public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
		return computeAsync((monitor) -> {
			TextDocument document = getDocument(params.getTextDocument().getUri());
			XMLDocument xmlDocument = getXMLDocument(document);
			return getXMLLanguageService().doCodeActions(params.getContext(), params.getRange(), xmlDocument) //
					.stream() //
					.map(s -> {
						Either<Command, CodeAction> e = Either.forRight(s);
						return e;
					}) //
					.collect(Collectors.toList());
		});
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {

	}

	private void triggerValidation(TextDocument document, int version) {
		if (future != null && !future.isCancelled()) {
			future.cancel(true);
		}
		if (monitor != null) {
			monitor.setCanceled(true);
		}
		monitor = new BasicCancelChecker();
		triggerValidation(document, version, monitor);
	}

	private void triggerValidation(TextDocument document, int version, BasicCancelChecker monitor) {
		future = xmlLanguageServer.schedule(() -> {
			TextDocument currDocument = getDocument(document.getUri());
			if (currDocument != null && currDocument.getVersion() == version) {
				List<Diagnostic> diagnostics = getXMLLanguageService().doDiagnostics(document, monitor);
				monitor.checkCanceled();
				xmlLanguageServer.getLanguageClient()
						.publishDiagnostics(new PublishDiagnosticsParams(document.getUri(), diagnostics));
			}
		}, 500, TimeUnit.MILLISECONDS);
	}

	private XMLLanguageService getXMLLanguageService() {
		return xmlLanguageServer.getXMLLanguageService();
	}

	public void setSharedFormattingOptions(XMLFormattingOptions formattingOptions) {
		this.sharedFormattingOptions = formattingOptions;
	}

}
