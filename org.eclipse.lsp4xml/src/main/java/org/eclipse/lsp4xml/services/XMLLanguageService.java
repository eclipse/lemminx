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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.FoldingRange;
import org.eclipse.lsp4j.FoldingRangeCapabilities;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.services.extensions.CompletionSettings;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;

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
	private XMLDocumentLink documentLink;
	private XMLCodeActions codeActions;

	public XMLLanguageService() {
		this.formatter = new XMLFormatter(this);
		this.highlighting = new XMLHighlighting(this);
		this.symbolsProvider = new XMLSymbolsProvider(this);
		this.completions = new XMLCompletions(this);
		this.hover = new XMLHover(this);
		this.diagnostics = new XMLDiagnostics(this);
		this.foldings = new XMLFoldings(this);
		this.documentLink = new XMLDocumentLink(this);
		this.codeActions = new XMLCodeActions(this);
	}

	public List<? extends TextEdit> format(TextDocument document, Range range, XMLFormattingOptions options) {
		return formatter.format(document, range, options);
	}

	public List<DocumentHighlight> findDocumentHighlights(XMLDocument xmlDocument, Position position) {
		return highlighting.findDocumentHighlights(xmlDocument, position);
	}

	public List<SymbolInformation> findDocumentSymbols(XMLDocument xmlDocument) {
		return symbolsProvider.findDocumentSymbols(xmlDocument);
	}

	public CompletionList doComplete(XMLDocument xmlDocument, Position position, CompletionSettings completionSettings,
			XMLFormattingOptions formattingSettings) {
		return completions.doComplete(xmlDocument, position, completionSettings, formattingSettings);
	}

	public Hover doHover(XMLDocument xmlDocument, Position position) {
		return hover.doHover(xmlDocument, position);
	}

	public List<Diagnostic> doDiagnostics(XMLDocument xmlDocument, CancelChecker monitor) {
		return diagnostics.doDiagnostics(xmlDocument, monitor);
	}

	public List<FoldingRange> getFoldingRanges(TextDocument document, FoldingRangeCapabilities context) {
		return foldings.getFoldingRanges(document, context);
	}

	public WorkspaceEdit doRename(XMLDocument xmlDocument, Position position, String newText) {
		List<TextEdit> textEdits = findDocumentHighlights(xmlDocument, position).stream()
				.map(h -> new TextEdit(h.getRange(), newText)).collect(Collectors.toList());
		Map<String, List<TextEdit>> changes = new HashMap<>();
		changes.put(xmlDocument.getUri(), textEdits);
		return new WorkspaceEdit(changes);
	}

	public List<DocumentLink> findDocumentLinks(XMLDocument document) {
		return documentLink.findDocumentLinks(document);
	}

	public List<CodeAction> doCodeActions(CodeActionContext context, Range range, XMLDocument document,
			XMLFormattingOptions formattingSettings) {
		return codeActions.doCodeActions(context, range, document, formattingSettings);
	}

	public String doTagComplete(XMLDocument xmlDocument, Position position) {
		return completions.doTagComplete(xmlDocument, position);
	}

	public String doAutoClose(XMLDocument xmlDocument, Position position) {
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
