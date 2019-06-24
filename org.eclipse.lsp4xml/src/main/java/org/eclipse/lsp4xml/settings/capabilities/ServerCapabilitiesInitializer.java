/**
 *  Copyright (c) 2018 Red Hat, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */

package org.eclipse.lsp4xml.settings.capabilities;

import static org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesConstants.DEFAULT_COMPLETION_OPTIONS;
import static org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesConstants.DEFAULT_LINK_OPTIONS;

import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;

/**
 * All default capabilities of this server
 */
public class ServerCapabilitiesInitializer {

	private ServerCapabilitiesInitializer() {
	}

	/**
	 * Returns all server capabilities (with default values) that aren't dynamic.
	 * 
	 * A service's dynamic capability is indicated by the client.
	 * 
	 * @param clientCapabilities
	 * @return ServerCapabilities object
	 */
	public static ServerCapabilities getNonDynamicServerCapabilities(ClientCapabilitiesWrapper clientCapabilities,
			boolean isIncremental) {
		ServerCapabilities serverCapabilities = new ServerCapabilities();

		serverCapabilities.setTextDocumentSync(isIncremental ? TextDocumentSyncKind.Incremental : TextDocumentSyncKind.Full);

		serverCapabilities.setDocumentSymbolProvider(!clientCapabilities.isDocumentSymbolDynamicRegistrationSupported());
		serverCapabilities.setDocumentHighlightProvider(!clientCapabilities.isDocumentHighlightDynamicRegistered());
		serverCapabilities.setCodeActionProvider(!clientCapabilities.isCodeActionDynamicRegistered());
		serverCapabilities.setDocumentFormattingProvider(!clientCapabilities.isFormattingDynamicRegistrationSupported());
		serverCapabilities.setDocumentRangeFormattingProvider(!clientCapabilities.isRangeFormattingDynamicRegistrationSupported());
		serverCapabilities.setHoverProvider(!clientCapabilities.isHoverDynamicRegistered());
		serverCapabilities.setRenameProvider(!clientCapabilities.isRenameDynamicRegistrationSupported());
		serverCapabilities.setFoldingRangeProvider(!clientCapabilities.isRangeFoldingDynamicRegistrationSupported());
		serverCapabilities.setDefinitionProvider(!clientCapabilities.isDefinitionDynamicRegistered());
		serverCapabilities.setReferencesProvider(!clientCapabilities.isReferencesDynamicRegistrationSupported());
		
		if (!clientCapabilities.isLinkDynamicRegistrationSupported()) {
			serverCapabilities.setDocumentLinkProvider(DEFAULT_LINK_OPTIONS);
		}
		if (!clientCapabilities.isCompletionDynamicRegistrationSupported()) {
			serverCapabilities.setCompletionProvider(DEFAULT_COMPLETION_OPTIONS);
		}
		return serverCapabilities;
	}
}