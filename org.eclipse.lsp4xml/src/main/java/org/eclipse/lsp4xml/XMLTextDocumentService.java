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
package org.eclipse.lsp4xml;

import static org.eclipse.lsp4j.jsonrpc.CompletableFutures.computeAsync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
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
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ReferenceParams;
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
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMParser;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.eclipse.lsp4xml.services.extensions.CompletionSettings;
import org.eclipse.lsp4xml.services.extensions.save.AbstractSaveContext;
import org.eclipse.lsp4xml.settings.SharedSettings;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;

/**
 * XML text document service.
 *
 */
public class XMLTextDocumentService implements TextDocumentService {

	private final XMLLanguageServer xmlLanguageServer;
	private final TextDocuments documents;
	private final LanguageModelCache<DOMDocument> xmlDocuments;
	private SharedSettings sharedSettings;

	class BasicCancelChecker implements CancelChecker {

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

	/**
	 * Save context.
	 */
	class SaveContext extends AbstractSaveContext {

		private final Collection<TextDocument> documentsToValidate;

		public SaveContext(Object settings) {
			super(settings);
			this.documentsToValidate = new ArrayList<>();
		}

		public SaveContext(String uri) {
			super(uri);
			this.documentsToValidate = new ArrayList<>();
		}

		@Override
		public void collectDocumentToValidate(Predicate<DOMDocument> validateDocumentPredicate) {
			documents.all().stream().forEach(document -> {
				DOMDocument xmlDocument = getXMLDocument(document);
				if (!documentsToValidate.contains(document) && validateDocumentPredicate.test(xmlDocument)) {
					documentsToValidate.add(document);
				}
			});
		}

		@Override
		public DOMDocument getDocument(String uri) {
			return xmlLanguageServer.getDocument(uri);
		}

		public void triggerValidationIfNeeded() {
			triggerValidationFor(documentsToValidate);
		}
	}

	final ScheduledExecutorService delayer = Executors.newScheduledThreadPool(2);
	private ScheduledFuture<?> future;
	private BasicCancelChecker monitor;
	private boolean codeActionLiteralSupport;
	private boolean hierarchicalDocumentSymbolSupport;
	
	public XMLTextDocumentService(XMLLanguageServer xmlLanguageServer) {
		this.xmlLanguageServer = xmlLanguageServer;
		this.documents = new TextDocuments();
		DOMParser parser = DOMParser.getInstance();
		this.xmlDocuments = new LanguageModelCache<DOMDocument>(10, 60, documents, document -> {
			return parser.parse(document, getXMLLanguageService().getResolverExtensionManager());
		});

		this.sharedSettings = new SharedSettings();
	}

	public void updateClientCapabilities(ClientCapabilities capabilities) {
		TextDocumentClientCapabilities textDocumentClientCapabilities = capabilities.getTextDocument();
		if (textDocumentClientCapabilities != null) {
			// Completion settings
			sharedSettings.completionSettings.setCapabilities(textDocumentClientCapabilities.getCompletion());
			codeActionLiteralSupport = textDocumentClientCapabilities.getCodeAction() != null
					&& textDocumentClientCapabilities.getCodeAction().getCodeActionLiteralSupport() != null;
			hierarchicalDocumentSymbolSupport = textDocumentClientCapabilities.getDocumentSymbol() != null
					&& textDocumentClientCapabilities.getDocumentSymbol().getHierarchicalDocumentSymbolSupport() != null
					&& textDocumentClientCapabilities.getDocumentSymbol().getHierarchicalDocumentSymbolSupport();
		}
	}

	public TextDocument getDocument(String uri) {
		return documents.get(uri);
	}

	public DOMDocument getXMLDocument(TextDocumentItem document) {
		return xmlDocuments.get(document);
	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
		return computeAsync((monitor) -> {
			String uri = params.getTextDocument().getUri();
			TextDocument document = getDocument(uri);
			DOMDocument xmlDocument = getXMLDocument(document);
			CompletionList list = getXMLLanguageService().doComplete(xmlDocument, params.getPosition(),
					sharedSettings);
			return Either.forRight(list);
		});
	}

	@Override
	public CompletableFuture<Hover> hover(TextDocumentPositionParams params) {
		return computeAsync((monitor) -> {
			TextDocument document = getDocument(params.getTextDocument().getUri());
			DOMDocument xmlDocument = getXMLDocument(document);
			return getXMLLanguageService().doHover(xmlDocument, params.getPosition());
		});
	}

	private XMLFormattingOptions getFormattingSettings(String uri) {
		// TODO: manage formattings per document URI (to support .editorconfig for
		// instance).
		return sharedSettings.formattingSettings;
	}

	@Override
	public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams params) {
		return computeAsync((monitor) -> {
			TextDocument document = getDocument(params.getTextDocument().getUri());
			DOMDocument xmlDocument = getXMLDocument(document);
			return getXMLLanguageService().findDocumentHighlights(xmlDocument, params.getPosition());
		});
	}

	@Override
	public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(
			DocumentSymbolParams params) {
		return computeAsync((monitor) -> {
			TextDocument document = getDocument(params.getTextDocument().getUri());
			DOMDocument xmlDocument = getXMLDocument(document);
			if (hierarchicalDocumentSymbolSupport) {
				return getXMLLanguageService().findDocumentSymbols(xmlDocument) //
						.stream() //
						.map(s -> {
							Either<SymbolInformation, DocumentSymbol> e = Either.forRight(s);
							return e;
						}) //
						.collect(Collectors.toList());
			}
			return getXMLLanguageService().findSymbolInformations(xmlDocument) //
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
			DOMDocument xmlDocument = getXMLDocument(document);
			return getXMLLanguageService().doRename(xmlDocument, params.getPosition(), params.getNewName());
		});
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		documents.onDidOpenTextDocument(params);
		triggerValidation(params.getTextDocument().getUri(), params.getTextDocument().getVersion());
	}

	@Override
	/**
	 * This method is triggered when the user types on an XML document.
	 */
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
			return getXMLLanguageService().getFoldingRanges(document, sharedSettings.foldingSettings);
		});
	}

	@Override
	public CompletableFuture<List<DocumentLink>> documentLink(DocumentLinkParams params) {
		return computeAsync((monitor) -> {
			TextDocument document = getDocument(params.getTextDocument().getUri());
			DOMDocument xmlDocument = getXMLDocument(document);
			return getXMLLanguageService().findDocumentLinks(xmlDocument);
		});
	}

	@Override
	public CompletableFuture<List<? extends Location>> definition(TextDocumentPositionParams params) {
		return computeAsync((monitor) -> {
			TextDocument document = getDocument(params.getTextDocument().getUri());
			DOMDocument xmlDocument = getXMLDocument(document);
			return getXMLLanguageService().findDefinition(xmlDocument, params.getPosition());
		});
	}

	@Override
	public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
		return computeAsync((monitor) -> {
			TextDocument document = getDocument(params.getTextDocument().getUri());
			DOMDocument xmlDocument = getXMLDocument(document);
			return getXMLLanguageService().findReferences(xmlDocument, params.getPosition(), params.getContext());
		});
	}

	@Override
	public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
		return computeAsync((monitor) -> {
			String uri = params.getTextDocument().getUri();
			TextDocument document = getDocument(uri);
			DOMDocument xmlDocument = getXMLDocument(document);
			return getXMLLanguageService()
					.doCodeActions(params.getContext(), params.getRange(), xmlDocument, getFormattingSettings(uri)) //
					.stream() //
					.map(ca -> {
						if (codeActionLiteralSupport) {
							Either<Command, CodeAction> e = Either.forRight(ca);
							return e;
						} else {
							List<Object> arguments = Arrays.asList(uri, document.getVersion(),
									ca.getEdit().getDocumentChanges().get(0).getLeft().getEdits());
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
		computeAsync((monitor) -> {
			// A document was saved, collect documents to revalidate
			SaveContext context = new SaveContext(params.getTextDocument().getUri());
			doSave(context);
			return null;
		});
	}

	/**
	 * Update settings of the language service.
	 * 
	 * @param settings
	 */
	public void updateSettings(Object settings) {
		SaveContext context = new SaveContext(settings);
		doSave(context);
	}

	/**
	 * Save settings or XML file.
	 * 
	 * @param context
	 */
	void doSave(SaveContext context) {
		getXMLLanguageService().doSave(context);
		context.triggerValidationIfNeeded();
	}

	private void triggerValidationFor(Collection<TextDocument> documents) {
		if (!documents.isEmpty()) {
			xmlLanguageServer.schedule(() -> {
				documents.forEach(document -> {
					String uri = document.getUri();
					int version = document.getVersion();
					doTriggerValidation(uri, version, monitor);
				});
			}, 500, TimeUnit.MILLISECONDS);
		}
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

	private void triggerValidation(String uri, int version, CancelChecker monitor) {
		future = xmlLanguageServer.schedule(() -> {
			doTriggerValidation(uri, version, monitor);
		}, 500, TimeUnit.MILLISECONDS);
	}

	private void doTriggerValidation(String uri, int version, CancelChecker monitor) {
		TextDocument currDocument = getDocument(uri);
		if (currDocument != null && currDocument.getVersion() == version) {
			DOMDocument xmlDocument = getXMLDocument(currDocument);
			getXMLLanguageService().publishDiagnostics(xmlDocument,
					params -> xmlLanguageServer.getLanguageClient().publishDiagnostics(params),
					(u, v) -> triggerValidation(u, v), monitor, sharedSettings.validationSettings);
		}
	}

	private XMLLanguageService getXMLLanguageService() {
		return xmlLanguageServer.getXMLLanguageService();
	}

	public void updateCompletionSettings(CompletionSettings newCompletion) {
		sharedSettings.completionSettings.setAutoCloseTags(newCompletion.isAutoCloseTags());
	}

	public boolean isIncrementalSupport() {
		return documents.isIncremental();
	}

	public XMLFormattingOptions getSharedFormattingSettings() {
		return sharedSettings.formattingSettings;
	}

	public void setIncrementalSupport(boolean incrementalSupport) {
		this.documents.setIncremental(incrementalSupport);
	}

	public XMLValidationSettings getValidationSettings() {
		
		return sharedSettings.validationSettings;
	}

	public SharedSettings getSharedSettings() {
		return this.sharedSettings;
	}

}
