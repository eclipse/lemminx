package org.eclipse.xml.languageserver.services;

import java.util.List;

import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.xml.languageserver.model.XMLDocument;

public class XMLLanguageService {

	private XMLCompletions completions;

	public XMLLanguageService() {
		completions = new XMLCompletions();
	}

	public List<SymbolInformation> findDocumentSymbols(TextDocumentItem document, XMLDocument fmDocument) {
		return XMLSymbolsProvider.findDocumentSymbols(document, fmDocument);
	}

	public List<DocumentHighlight> findDocumentHighlights(TextDocumentItem document, Position position,
			XMLDocument fmDocument) {
		return XMLHighlighting.findDocumentHighlights(document, position, fmDocument);
	}

	public CompletionList doComplete(TextDocumentItem document, Position position, XMLDocument fmDocument,
			CompletionConfiguration settings) {
		return completions.doComplete(document, position, fmDocument, settings);
	}

	public void setCompletionParticipants(ICompletionParticipant completionParticipants) {
		//this.completions.setCompletionParticipants(completionParticipants);
	}
}
