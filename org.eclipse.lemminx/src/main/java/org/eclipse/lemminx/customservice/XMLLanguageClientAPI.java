package org.eclipse.lemminx.customservice;

import org.eclipse.lemminx.services.extensions.documentSymbol.SymbolsLimitExceededCommand;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.LanguageClient;

@JsonSegment("xml")
public interface XMLLanguageClientAPI extends LanguageClient {

	@JsonNotification
	default void symbolsLimitExceeded(SymbolsLimitExceededCommand command) {
		throw new UnsupportedOperationException();
	}
}