package org.eclipse.lsp4xml.commons;

import org.eclipse.lsp4j.jsonrpc.CancelChecker;

public class MultiCancelChecker implements CancelChecker {

	private CancelChecker[] checkers;

	public MultiCancelChecker(CancelChecker... checkers) {
		this.checkers = checkers;
	}

	@Override
	public void checkCanceled() {
		for (CancelChecker cancelChecker : checkers) {
			cancelChecker.checkCanceled();
		}
		
	}
}
