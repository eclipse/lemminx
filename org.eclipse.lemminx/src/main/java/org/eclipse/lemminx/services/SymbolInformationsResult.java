package org.eclipse.lemminx.services;

import java.util.ArrayList;

import org.eclipse.lsp4j.SymbolInformation;

public class SymbolInformationsResult extends ArrayList<SymbolInformation> {

	private static final long serialVersionUID = 1L;

	private transient boolean resultLimitExceeded;

	public boolean isResultLimitExceeded() {
		return resultLimitExceeded;
	}

	public void setResultLimitExceeded(boolean resultLimitExceeded) {
		this.resultLimitExceeded = resultLimitExceeded;
	}

}
