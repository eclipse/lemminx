package org.eclipse.xml.languageserver.services;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.xml.languageserver.extensions.ICompletionResponse;

class CompletionResponse extends CompletionList implements ICompletionResponse {

	public CompletionResponse() {
		super.setIsIncomplete(false);
	}

	@Override
	public void addCompletionItem(CompletionItem completionItem) {
		super.getItems().add(completionItem);
	}
}
