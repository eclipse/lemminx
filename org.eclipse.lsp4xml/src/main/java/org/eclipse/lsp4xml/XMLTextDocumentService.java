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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
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
import org.eclipse.lsp4j.FoldingRangeRequestParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4xml.commons.ModelTextDocument;
import org.eclipse.lsp4xml.commons.ModelTextDocuments;
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
import org.eclipse.lsp4xml.settings.XMLIncrementalSupportSettings;
import org.eclipse.lsp4xml.settings.XMLSymbolSettings;
import org.eclipse.lsp4xml.utils.XMLPositionUtility;

/**
 * XML text document service.
 *
 */
public class XMLTextDocumentService implements TextDocumentService {

	private final XMLLanguageServer xmlLanguageServer;
	private final TextDocuments<ModelTextDocument<DOMDocument>> documents;
	private SharedSettings sharedSettings;

	/**
	 * Save context.
	 */
	class SaveContext extends AbstractSaveContext {

		private final Collection<ModelTextDocument<DOMDocument>> documentsToValidate;

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
				DOMDocument xmlDocument = document.getModel().getNow(null);
				if (xmlDocument != null && !documentsToValidate.contains(document)
						&& validateDocumentPredicate.test(xmlDocument)) {
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
	private boolean codeActionLiteralSupport;
	private boolean hierarchicalDocumentSymbolSupport;
	private boolean definitionLinkSupport;

	public XMLTextDocumentService(XMLLanguageServer xmlLanguageServer) {
		this.xmlLanguageServer = xmlLanguageServer;
		DOMParser parser = DOMParser.getInstance();
		this.documents = new ModelTextDocuments<DOMDocument>((document, cancelChecker) -> {
			return parser.parse(document, getXMLLanguageService().getResolverExtensionManager(), true, cancelChecker);
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
			definitionLinkSupport = textDocumentClientCapabilities.getDefinition() != null
					&& textDocumentClientCapabilities.getDefinition().getLinkSupport() != null
					&& textDocumentClientCapabilities.getDefinition().getLinkSupport();
		}
	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
		return computeDOMAsync(params.getTextDocument(), (cancelChecker, xmlDocument) -> {
			CompletionList list = getXMLLanguageService().doComplete(xmlDocument, params.getPosition(), sharedSettings,
					cancelChecker);
			return Either.forRight(list);
		});
	}

	@Override
	public CompletableFuture<Hover> hover(TextDocumentPositionParams params) {
		return computeDOMAsync(params.getTextDocument(), (cancelChecker, xmlDocument) -> {
			return getXMLLanguageService().doHover(xmlDocument, params.getPosition(), cancelChecker);
		});
	}

	private XMLFormattingOptions getFormattingSettings(String uri) {
		// TODO: manage formattings per document URI (to support .editorconfig for
		// instance).
		return sharedSettings.formattingSettings;
	}

	@Override
	public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams params) {
		return computeDOMAsync(params.getTextDocument(), (cancelChecker, xmlDocument) -> {
			return getXMLLanguageService().findDocumentHighlights(xmlDocument, params.getPosition(), cancelChecker);
		});
	}

	@Override
	public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(
			DocumentSymbolParams params) {

		TextDocument document = getDocument(params.getTextDocument().getUri());

		if (!sharedSettings.symbolSettings.isEnabled() || sharedSettings.symbolSettings.isExcluded(document.getUri())) {
			return CompletableFuture.completedFuture(Collections.emptyList());
		}

		return computeDOMAsync(params.getTextDocument(), (cancelChecker, xmlDocument) -> {
			if (hierarchicalDocumentSymbolSupport) {
				return getXMLLanguageService().findDocumentSymbols(xmlDocument, cancelChecker) //
						.stream() //
						.map(s -> {
							Either<SymbolInformation, DocumentSymbol> e = Either.forRight(s);
							return e;
						}) //
						.collect(Collectors.toList());
			}
			return getXMLLanguageService().findSymbolInformations(xmlDocument, cancelChecker) //
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
		return computeAsync((cancelChecker) -> {
			String uri = params.getTextDocument().getUri();
			TextDocument document = getDocument(uri);
			return getXMLLanguageService().format(document, null,
					XMLFormattingOptions.create(params.getOptions(), getFormattingSettings(uri)));
		});
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
		return computeAsync((cancelChecker) -> {
			String uri = params.getTextDocument().getUri();
			TextDocument document = getDocument(uri);
			return getXMLLanguageService().format(document, params.getRange(),
					XMLFormattingOptions.create(params.getOptions(), getFormattingSettings(uri)));
		});
	}

	@Override
	public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
		return computeDOMAsync(params.getTextDocument(), (cancelChecker, xmlDocument) -> {
			return getXMLLanguageService().doRename(xmlDocument, params.getPosition(), params.getNewName());
		});
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		TextDocument document = documents.onDidOpenTextDocument(params);
		triggerValidationFor(document);
	}

	/**
	 * This method is triggered when the user types on an XML document.
	 */
	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		TextDocument document = documents.onDidChangeTextDocument(params);
		triggerValidationFor(document);
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		documents.onDidCloseTextDocument(params);
		TextDocumentIdentifier document = params.getTextDocument();
		String uri = document.getUri();
		xmlLanguageServer.getLanguageClient()
				.publishDiagnostics(new PublishDiagnosticsParams(uri, new ArrayList<Diagnostic>()));
	}

	@Override
	public CompletableFuture<List<FoldingRange>> foldingRange(FoldingRangeRequestParams params) {
		return computeDOMAsync(params.getTextDocument(), (cancelChecker, xmlDocument) -> {
			return getXMLLanguageService().getFoldingRanges(xmlDocument, sharedSettings.foldingSettings, cancelChecker);
		});
	}

	@Override
	public CompletableFuture<List<DocumentLink>> documentLink(DocumentLinkParams params) {
		return computeDOMAsync(params.getTextDocument(), (cancelChecker, xmlDocument) -> {
			return getXMLLanguageService().findDocumentLinks(xmlDocument);
		});
	}

	@Override
	public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(
			TextDocumentPositionParams params) {
		return computeDOMAsync(params.getTextDocument(), (cancelChecker, xmlDocument) -> {
			if (definitionLinkSupport) {
				return Either.forRight(getXMLLanguageService().findDefinition(xmlDocument, params.getPosition(), cancelChecker));
			}
			List<? extends Location> locations = getXMLLanguageService()
					.findDefinition(xmlDocument, params.getPosition(), cancelChecker) //
					.stream() //
					.map(locationLink -> XMLPositionUtility.toLocation(locationLink)) //
					.collect(Collectors.toList());
			return Either.forLeft(locations);
		});
	}

	@Override
	public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
		return computeDOMAsync(params.getTextDocument(), (cancelChecker, xmlDocument) -> {
			return getXMLLanguageService().findReferences(xmlDocument, params.getPosition(), params.getContext());
		});
	}

	@Override
	public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
		return computeDOMAsync(params.getTextDocument(), (cancelChecker, xmlDocument) -> {
			String uri = params.getTextDocument().getUri();
			return getXMLLanguageService()
					.doCodeActions(params.getContext(), params.getRange(), xmlDocument, getFormattingSettings(uri)) //
					.stream() //
					.map(ca -> {
						if (codeActionLiteralSupport) {
							Either<Command, CodeAction> e = Either.forRight(ca);
							return e;
						} else {
							List<Object> arguments = Arrays.asList(uri, xmlDocument.getTextDocument().getVersion(),
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

	private void triggerValidationFor(Collection<ModelTextDocument<DOMDocument>> documents) {
		if (!documents.isEmpty()) {
			xmlLanguageServer.schedule(() -> {
				documents.forEach(document -> {
					try {
						validate(document.getModel().getNow(null));
					} catch (CancellationException e) {
						// Ignore the error and continue to validate other documents
					}
				});
			}, 500, TimeUnit.MILLISECONDS);
		}
	}

	@SuppressWarnings("unchecked")
	private void triggerValidationFor(TextDocument document) {
		((ModelTextDocument<DOMDocument>) document).getModel().thenAcceptAsync(xmlDocument -> {
			validate(xmlDocument);
		});
	}

	private void validate(DOMDocument xmlDocument) throws CancellationException {
		CancelChecker cancelChecker = xmlDocument.getCancelChecker();
		cancelChecker.checkCanceled();
		getXMLLanguageService().publishDiagnostics(xmlDocument,
				params -> xmlLanguageServer.getLanguageClient().publishDiagnostics(params),
				(doc) -> triggerValidationFor(doc), sharedSettings.validationSettings, cancelChecker);
	}

	private XMLLanguageService getXMLLanguageService() {
		return xmlLanguageServer.getXMLLanguageService();
	}

	public void updateCompletionSettings(CompletionSettings newCompletion) {
		sharedSettings.setCompletionSettings(newCompletion);
	}

	public void updateSymbolSettings(XMLSymbolSettings newSettings) {
		XMLSymbolSettings symbolSettings = sharedSettings.symbolSettings;
		symbolSettings.setEnabled(newSettings.isEnabled());
		String[] newPatterns = newSettings.getExcluded();
		if (newPatterns != null) {
			symbolSettings.setExcluded(newPatterns);
		}
	}

	public XMLSymbolSettings getSharedSymbolSettings() {
		return sharedSettings.symbolSettings;
	}

	public boolean isIncrementalSupport() {
		return documents.isIncremental();
	}

	public XMLFormattingOptions getSharedFormattingSettings() {
		return sharedSettings.formattingSettings;
	}

	public XMLValidationSettings getValidationSettings() {

		return sharedSettings.validationSettings;
	}

	public SharedSettings getSharedSettings() {
		return this.sharedSettings;
	}

	public void updateIncrementalSettings(XMLIncrementalSupportSettings settings) {
		sharedSettings.experimentalSettings.getIncrementalSupport().setEnabled(settings.getEnabled());
		this.documents.setIncremental(sharedSettings.experimentalSettings.getIncrementalSupport().getEnabled());
	}

	/**
	 * Returns the text document from the given uri.
	 * 
	 * @param uri the uri
	 * @return the text document from the given uri.
	 */
	public ModelTextDocument<DOMDocument> getDocument(String uri) {
		return documents.get(uri);
	}

	/**
	 * Compute the DOM Document for a given uri in a future and then apply the given
	 * function.
	 * 
	 * @param <R>
	 * @param documentIdentifier the document indetifier.
	 * @param code               a bi function that accepts a {@link CancelChecker}
	 *                           and parsed {@link DOMDocument} and returns the to
	 *                           be computed value
	 * @return the DOM Document for a given uri in a future and then apply the given
	 *         function.
	 */
	public <R> CompletableFuture<R> computeDOMAsync(TextDocumentIdentifier documentIdentifier,
			BiFunction<CancelChecker, DOMDocument, R> code) {
		return computeModelAsync(getDocument(documentIdentifier.getUri()).getModel(), code);
	}

	private static <R, M> CompletableFuture<R> computeModelAsync(CompletableFuture<M> loadModel,
			BiFunction<CancelChecker, M, R> code) {
		CompletableFuture<CancelChecker> start = new CompletableFuture<>();
		CompletableFuture<R> result = start.thenCombineAsync(loadModel, code);
		CancelChecker cancelIndicator = () -> {
			if (result.isCancelled())
				throw new CancellationException();
		};
		start.complete(cancelIndicator);
		return result;
	}
}
