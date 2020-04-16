/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.client;

/**
 * Extended client capabilities not defined by the LSP.
 * 
 * @author Angelo ZERR
 * 
 * @see https://github.com/microsoft/language-server-protocol/issues/788
 */
public class ExtendedClientCapabilities {

	private ExtendedCodeLensCapabilities codeLens;

	private ExtendedSymbolCapabilities symbol;

	public ExtendedCodeLensCapabilities getCodeLens() {
		return codeLens;
	}

	public void setCodeLens(ExtendedCodeLensCapabilities codeLens) {
		this.codeLens = codeLens;
	}

	public ExtendedSymbolCapabilities getSymbol() {
		return symbol;
	}

	public void setSymbol(ExtendedSymbolCapabilities symbol) {
		this.symbol = symbol;
	}

}
