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
package org.eclipse.xml.languageserver;

import static org.eclipse.lsp4j.jsonrpc.CompletableFutures.computeAsync;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.xml.languageserver.commons.LanguageModelCache;
import org.eclipse.xml.languageserver.commons.TextDocuments;
import org.eclipse.xml.languageserver.internal.parser.XMLParser;
import org.eclipse.xml.languageserver.model.IXMLParser;
import org.eclipse.xml.languageserver.model.XMLDocument;
import org.eclipse.xml.languageserver.services.XMLLanguageService;

/**
 * Freemarker text document service.
 *
 */
public class XMLTextDocumentService implements TextDocumentService {

	private final XMLLanguageServer xmlLanguageServer;
	private final TextDocuments documents;
	private final XMLLanguageService languageService;
	private LanguageModelCache<XMLDocument> xmlDocuments;

	public XMLTextDocumentService(XMLLanguageServer fmLanguageServer) {
		this.xmlLanguageServer = fmLanguageServer;
		this.languageService = new XMLLanguageService();
		this.documents = new TextDocuments();
		IXMLParser parser = XMLParser.getInstance();
		this.xmlDocuments = new LanguageModelCache<XMLDocument>(10, 60, document -> parser.parse(document.getText()));
	}

	private XMLDocument getXMLDocument(TextDocumentItem document) {
		return xmlDocuments.get(document);
	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(
			TextDocumentPositionParams params) {
		return computeAsync((monitor) -> {
			TextDocumentItem document = documents.get(params.getTextDocument().getUri());
			XMLDocument xmlDocument = getXMLDocument(document);
			CompletionList list = languageService.doComplete(document, params.getPosition(), xmlDocument, null);
			return Either.forRight(list);
		});
	}

	@Override
	public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
		return null;
	}

	@Override
	public CompletableFuture<Hover> hover(TextDocumentPositionParams position) {
		return null;
	}

	@Override
	public CompletableFuture<SignatureHelp> signatureHelp(TextDocumentPositionParams position) {
		return null;
	}

	@Override
	public CompletableFuture<List<? extends Location>> definition(TextDocumentPositionParams position) {
		return null;
	}

	@Override
	public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
		return null;
	}

	@Override
	public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams params) {
		return computeAsync((monitor) -> {
			TextDocumentItem document = documents.get(params.getTextDocument().getUri());
			XMLDocument xmlDocument = getXMLDocument(document);
			return languageService.findDocumentHighlights(document, params.getPosition(), xmlDocument);
		});
	}

	@Override
	public CompletableFuture<List<? extends SymbolInformation>> documentSymbol(DocumentSymbolParams params) {
		return computeAsync((monitor) -> {
			TextDocumentItem document = documents.get(params.getTextDocument().getUri());
			XMLDocument xmlDocument = getXMLDocument(document);
			return languageService.findDocumentSymbols(document, xmlDocument);
		});
	}

	@Override
	public CompletableFuture<List<? extends Command>> codeAction(CodeActionParams params) {
		return null;
	}

	@Override
	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
		return null;
	}

	@Override
	public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
		return null;
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
		return null;
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
		return null;
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
		return null;
	}

	@Override
	public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
		return null;
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		documents.onDidOpenTextDocument(params);
		triggerValidation(params.getTextDocument());
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		documents.onDidChangeTextDocument(params);
		TextDocumentItem document = documents.get(params.getTextDocument().getUri());
		if (document != null) {
			triggerValidation(document);
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
	public void didSave(DidSaveTextDocumentParams params) {

	}

	private void triggerValidation(TextDocumentItem document) {
		String uri = document.getUri();
		List<Diagnostic> diagnostics = languageService.validateXML(uri, document.getText());
		xmlLanguageServer.getLanguageClient().publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
	}

	public void validateOpenDocuments() {

	}
}
