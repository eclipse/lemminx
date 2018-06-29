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
package org.eclipse.xml.languageserver.services;

import java.util.List;

import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.xml.languageserver.model.XMLDocument;

/**
 * XML Language service.
 *
 */
public class XMLLanguageService {

	private final XMLFormatter formatter;
	private final XMLHighlighting highlighting;
	private final XMLSymbolsProvider symbolsProvider;
	private final XMLCompletions completions;
	private final XMLDiagnostics diagnostics;

	public XMLLanguageService() {
		this(new XMLExtensionsRegistry());
	}

	public XMLLanguageService(XMLExtensionsRegistry extensionsRegistry) {
		this.formatter = new XMLFormatter(extensionsRegistry);
		this.highlighting = new XMLHighlighting(extensionsRegistry);
		this.symbolsProvider = new XMLSymbolsProvider(extensionsRegistry);
		this.completions = new XMLCompletions(extensionsRegistry);
		this.diagnostics = new XMLDiagnostics(extensionsRegistry);
	}

	public List<? extends TextEdit> format(TextDocumentItem document, Range range, FormattingOptions options,
			XMLDocument xmlDocument) {
		return formatter.format(document, range, options, xmlDocument);
	}

	public List<DocumentHighlight> findDocumentHighlights(TextDocumentItem document, Position position,
			XMLDocument xmlDocument) {
		return highlighting.findDocumentHighlights(document, position, xmlDocument);
	}

	public List<SymbolInformation> findDocumentSymbols(TextDocumentItem document, XMLDocument xmlDocument) {
		return symbolsProvider.findDocumentSymbols(document, xmlDocument);
	}

	public CompletionList doComplete(TextDocumentItem document, Position position, XMLDocument xmlDocument,
			CompletionConfiguration settings) {
		return completions.doComplete(document, position, xmlDocument, settings);
	}

	public List<Diagnostic> validateXML(String uri, String text) {
		return diagnostics.validateXML(uri, text);
	}

}
