package org.eclipse.lemminx.services;

import java.util.ArrayList;

import org.eclipse.lsp4j.DocumentSymbol;

public class DocumentSymbolsResult extends ArrayList<DocumentSymbol> {

	private static final long serialVersionUID = 1L;

	private transient boolean resultLimitExceeded;

	public boolean isResultLimitExceeded() {
		return resultLimitExceeded;
	}

	public void setResultLimitExceeded(boolean resultLimitExceeded) {
		this.resultLimitExceeded = resultLimitExceeded;
	}

}
