package org.eclipse.lemminx.services.extensions.documentSymbol;

import org.eclipse.lemminx.client.ClientCommands;

public class SymbolsLimitExceededCommand {

	private final String commandId = ClientCommands.SYMBOLS_LIMIT_EXCEEDED;
	private final String message;

	public SymbolsLimitExceededCommand(String message) {
		this.message = message;
	}
}