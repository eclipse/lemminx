/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.services;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.FoldingRange;
import org.eclipse.lsp4j.FoldingRangeCapabilities;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.customservice.AutoCloseTagResponse;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lsp4xml.services.extensions.CompletionSettings;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.settings.SharedSettings;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;
import org.eclipse.lsp4xml.uriresolver.CacheResourceDownloadingException;
import org.eclipse.lsp4xml.utils.XMLPositionUtility;

/**
 * XML Language service.
 *
 */
public class XMLLanguageService extends XMLExtensionsRegistry {

	private final XMLFormatter formatter;
	private final XMLHighlighting highlighting;
	private final XMLSymbolsProvider symbolsProvider;
	private final XMLCompletions completions;
	private final XMLHover hover;
	private final XMLDiagnostics diagnostics;
	private final XMLFoldings foldings;
	private final XMLDocumentLink documentLink;
	private XMLDefinition definition;
	private XMLReference reference;
	private final XMLCodeActions codeActions;

	public XMLLanguageService() {
		this.formatter = new XMLFormatter(this);
		this.highlighting = new XMLHighlighting(this);
		this.symbolsProvider = new XMLSymbolsProvider(this);
		this.completions = new XMLCompletions(this);
		this.hover = new XMLHover(this);
		this.diagnostics = new XMLDiagnostics(this);
		this.foldings = new XMLFoldings(this);
		this.documentLink = new XMLDocumentLink(this);
		this.definition = new XMLDefinition(this);
		this.reference = new XMLReference(this);
		this.codeActions = new XMLCodeActions(this);
	}

	public List<? extends TextEdit> format(TextDocument document, Range range, XMLFormattingOptions options) {
		return formatter.format(document, range, options);
	}

	public List<DocumentHighlight> findDocumentHighlights(DOMDocument xmlDocument, Position position) {
		return highlighting.findDocumentHighlights(xmlDocument, position);
	}

	public List<SymbolInformation> findSymbolInformations(DOMDocument xmlDocument) {
		return symbolsProvider.findSymbolInformations(xmlDocument);
	}

	public List<DocumentSymbol> findDocumentSymbols(DOMDocument xmlDocument) {
		return symbolsProvider.findDocumentSymbols(xmlDocument);
	}

	public CompletionList doComplete(DOMDocument xmlDocument, Position position, SharedSettings settings) {
		return completions.doComplete(xmlDocument, position, settings);
	}

	public Hover doHover(DOMDocument xmlDocument, Position position) {
		return hover.doHover(xmlDocument, position);
	}

	public List<Diagnostic> doDiagnostics(DOMDocument xmlDocument, CancelChecker monitor, XMLValidationSettings validationSettings) {
		return diagnostics.doDiagnostics(xmlDocument, monitor, validationSettings);
	}

	public CompletableFuture<Path> publishDiagnostics(DOMDocument xmlDocument,
			Consumer<PublishDiagnosticsParams> publishDiagnostics, BiConsumer<String, Integer> triggerValidation,
			CancelChecker monitor, XMLValidationSettings validationSettings) {
		String uri = xmlDocument.getDocumentURI();
		int version = xmlDocument.getTextDocument().getVersion();
		try {
			List<Diagnostic> diagnostics = this.doDiagnostics(xmlDocument, monitor, validationSettings);
			monitor.checkCanceled();
			
			publishDiagnostics.accept(new PublishDiagnosticsParams(uri, diagnostics));
			return null;
		} catch (CacheResourceDownloadingException e) {
			// An XML Schema or DTD is being downloaded by the cache manager, but it takes
			// too long.
			// In this case:
			// - 1) we add an information message to the document element to explain that
			// validation
			// cannot be performed because the XML Schema/DTD is downloading.
			publishOneDiagnosticInRoot(xmlDocument, e.getMessage(), DiagnosticSeverity.Information, publishDiagnostics);
			// - 2) we restart the validation only once the XML Schema/DTD is downloaded.
			e.getFuture() //
					.exceptionally(downloadException -> {
						// Error while downloading the XML Schema/DTD
						publishOneDiagnosticInRoot(xmlDocument, downloadException.getCause().getMessage(),
								DiagnosticSeverity.Error, publishDiagnostics);
						return null;
					}) //
					.thenAccept((path) -> {
						if (path != null) {
							triggerValidation.accept(uri, version);
						}
					});
			return e.getFuture();
		}
	}

	private static void publishOneDiagnosticInRoot(DOMDocument document, String message, DiagnosticSeverity severity,
			Consumer<PublishDiagnosticsParams> publishDiagnostics) {
		String uri = document.getDocumentURI();
		DOMElement documentElement = document.getDocumentElement();
		Range range = XMLPositionUtility.selectStartTag(documentElement);
		List<Diagnostic> diagnostics = new ArrayList<>();
		diagnostics.add(new Diagnostic(range, message, severity, "XML"));
		publishDiagnostics.accept(new PublishDiagnosticsParams(uri, diagnostics));
	}

	public List<FoldingRange> getFoldingRanges(TextDocument document, FoldingRangeCapabilities context) {
		return foldings.getFoldingRanges(document, context);
	}

	public WorkspaceEdit doRename(DOMDocument xmlDocument, Position position, String newText) {
		List<TextEdit> textEdits = findDocumentHighlights(xmlDocument, position).stream()
				.map(h -> new TextEdit(h.getRange(), newText)).collect(Collectors.toList());
		Map<String, List<TextEdit>> changes = new HashMap<>();
		changes.put(xmlDocument.getDocumentURI(), textEdits);
		return new WorkspaceEdit(changes);
	}

	public List<DocumentLink> findDocumentLinks(DOMDocument document) {
		return documentLink.findDocumentLinks(document);
	}

	public List<? extends Location> findDefinition(DOMDocument xmlDocument, Position position) {
		return definition.findDefinition(xmlDocument, position);
	}

	public List<? extends Location> findReferences(DOMDocument xmlDocument, Position position,
			ReferenceContext context) {
		return reference.findReferences(xmlDocument, position, context);
	}

	public List<CodeAction> doCodeActions(CodeActionContext context, Range range, DOMDocument document,
			XMLFormattingOptions formattingSettings) {
		return codeActions.doCodeActions(context, range, document, formattingSettings);
	}

	public AutoCloseTagResponse doTagComplete(DOMDocument xmlDocument, Position position) {
		return completions.doTagComplete(xmlDocument, position);
	}

	public AutoCloseTagResponse doAutoClose(DOMDocument xmlDocument, Position position) {
		try {
			int offset = xmlDocument.offsetAt(position);
			String text = xmlDocument.getText();
			if (offset > 0) {
				char c = text.charAt(offset - 1);
				if (c == '>' || c == '/') {
					return doTagComplete(xmlDocument, position);
				}
			}
			return null;
		} catch (BadLocationException e) {
			return null;
		}
	}
}
