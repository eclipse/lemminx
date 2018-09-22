/**
 *  Copyright (c) 2018 Red Hat, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */

package org.eclipse.lsp4xml.settings.capabilities;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.DynamicRegistrationCapabilities;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;

/**
 * Determines if a client supports a specific capability dynamically
 */
public class ClientCapabilitiesWrapper {
	private boolean v3Supported;

	public ClientCapabilities capabilities;

	public ClientCapabilitiesWrapper() {
		this.capabilities = new ClientCapabilities();
		this.v3Supported = false;
	}

	public ClientCapabilitiesWrapper(ClientCapabilities capabilities) {
		this.capabilities = capabilities;
		this.v3Supported = capabilities != null ? capabilities.getTextDocument() != null : false;
	}

	/**
	 * IMPORTANT
	 * 
	 * This should be up to date with all Server supported capabilities
	 * 
	 */

	public boolean isCompletionDynamicRegistrationSupported() {
		return v3Supported && isDynamicRegistrationSupported(getTextDocument().getCompletion());
	}

	public boolean isLinkDynamicRegistrationSupported() {
		return v3Supported && isDynamicRegistrationSupported(getTextDocument().getDocumentLink());
	}

	public boolean isRangeFoldingDynamicRegistrationSupported() {
		return v3Supported && isDynamicRegistrationSupported(getTextDocument().getFoldingRange());
	}

	public boolean isDocumentSyncDynamicRegistrationSupported() {
		return v3Supported && isDynamicRegistrationSupported(getTextDocument().getSynchronization());
	}

	public boolean isFormattingDynamicRegistrationSupported() {
		return v3Supported && isDynamicRegistrationSupported(getTextDocument().getFormatting());
	}

	public boolean isRangeFormattingDynamicRegistrationSupported() {
		return v3Supported && isDynamicRegistrationSupported(getTextDocument().getRangeFormatting());
	}

	public boolean isRenameDynamicRegistrationSupported() {
		return v3Supported && isDynamicRegistrationSupported(getTextDocument().getRename());
	}

	public boolean isDocumentSymbolDynamicRegistered() {
		return v3Supported && isDynamicRegistrationSupported(getTextDocument().getDocumentSymbol());
	}

	public boolean isCodeActionDynamicRegistered() {
		return v3Supported && isDynamicRegistrationSupported(getTextDocument().getCodeAction());
	}

	public boolean isHoverDynamicRegistered() {
		return v3Supported && isDynamicRegistrationSupported(getTextDocument().getHover());
	}

	public boolean isDocumentHighlightDynamicRegistered() {
		return v3Supported && isDynamicRegistrationSupported(getTextDocument().getDocumentHighlight());
	}

	private boolean isDynamicRegistrationSupported(DynamicRegistrationCapabilities capability) {
		return capability != null && capability.getDynamicRegistration() != null
				&& capability.getDynamicRegistration().booleanValue();
	}

	public TextDocumentClientCapabilities getTextDocument() {
		return this.capabilities.getTextDocument();
	}

}