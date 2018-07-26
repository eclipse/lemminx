/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.services;

import java.util.List;

import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.extensions.CompletionSettings;
import org.eclipse.lsp4xml.model.XMLDocument;

import toremove.org.eclipse.lsp4j.FoldingRange;
import toremove.org.eclipse.lsp4j.FoldingRangeCapabilities;

/**
 * XML Language service.
 *
 */
public class XMLLanguageService {

	private final XMLFormatter formatter;
	private final XMLHighlighting highlighting;
	private final XMLSymbolsProvider symbolsProvider;
	private final XMLCompletions completions;
	private final XMLHover hover;
	private final XMLDiagnostics diagnostics;
	private XMLFoldings foldings;

	public XMLLanguageService() {
		this(new XMLExtensionsRegistry());
	}

	public XMLLanguageService(XMLExtensionsRegistry extensionsRegistry) {
		this.formatter = new XMLFormatter(extensionsRegistry);
		this.highlighting = new XMLHighlighting(extensionsRegistry);
		this.symbolsProvider = new XMLSymbolsProvider(extensionsRegistry);
		this.completions = new XMLCompletions(extensionsRegistry);
		this.hover = new XMLHover(extensionsRegistry);
		this.diagnostics = new XMLDiagnostics(extensionsRegistry);
		this.foldings = new XMLFoldings(extensionsRegistry);
	}

	public List<? extends TextEdit> format(XMLDocument xmlDocument, Range range, FormattingOptions options) {
		return formatter.format(xmlDocument, range, options);
	}

	public List<DocumentHighlight> findDocumentHighlights(XMLDocument xmlDocument, Position position) {
		return highlighting.findDocumentHighlights(xmlDocument, position);
	}

	public List<SymbolInformation> findDocumentSymbols(XMLDocument xmlDocument) {
		return symbolsProvider.findDocumentSymbols(xmlDocument);
	}

	public CompletionList doComplete(XMLDocument xmlDocument, Position position, CompletionSettings completionSettings,
			FormattingOptions formattingSettings) {
		return completions.doComplete(xmlDocument, position, completionSettings, formattingSettings);
	}

	public Hover doHover(XMLDocument xmlDocument, Position position) {
		return hover.doHover(xmlDocument, position);
	}

	public List<Diagnostic> doDiagnostics(TextDocumentItem document, String xmlSchemaFile, CancelChecker monitor) {
		return diagnostics.doDiagnostics(document, xmlSchemaFile, monitor);
	}

	public List<FoldingRange> getFoldingRanges(XMLDocument xmlDocument, FoldingRangeCapabilities context) {
		return foldings.getFoldingRanges(xmlDocument, context);
	}
}
