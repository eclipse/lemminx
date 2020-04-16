/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
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

import org.eclipse.lsp4j.DynamicRegistrationCapabilities;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 */
@SuppressWarnings("all")
public class ExtendedSymbolCapabilities extends DynamicRegistrationCapabilities {

	private boolean symbolsLimitExceededSupported;

	public ExtendedSymbolCapabilities() {
	}

	public ExtendedSymbolCapabilities(final Boolean dynamicRegistration) {
		super(dynamicRegistration);
	}

	public ExtendedSymbolCapabilities(final boolean symbolsLimitExceededSupported) {
		this.symbolsLimitExceededSupported = symbolsLimitExceededSupported;
	}

	public ExtendedSymbolCapabilities(final boolean symbolsLimitExceededSupported,
			final Boolean dynamicRegistration) {
		super(dynamicRegistration);
		this.symbolsLimitExceededSupported = symbolsLimitExceededSupported;
	}

	public boolean isSymbolsLimitExceededSupported() {
		return symbolsLimitExceededSupported;
	}

	public void setSymbolsLimitExceededSupported(boolean symbolsMaxExceededSupported) {
		this.symbolsLimitExceededSupported = symbolsMaxExceededSupported;
	}
}
