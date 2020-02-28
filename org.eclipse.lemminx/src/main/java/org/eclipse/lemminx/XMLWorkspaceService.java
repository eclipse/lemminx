/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lemminx.XMLTextDocumentService;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.WorkspaceService;
/**
 * XML workspace service.
 *
 */
public class XMLWorkspaceService implements WorkspaceService {

	private final XMLLanguageServer xmlLanguageServer;

	public XMLWorkspaceService(XMLLanguageServer xmlLanguageServer) {
		this.xmlLanguageServer = xmlLanguageServer;
	}

	@Override
	public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
		return null;
	}

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params) {
		xmlLanguageServer.updateSettings(params.getSettings());
		xmlLanguageServer.capabilityManager.syncDynamicCapabilitiesWithPreferences();
	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
		XMLTextDocumentService xmlTextDocumentService = (XMLTextDocumentService) xmlLanguageServer.getTextDocumentService();
		List<FileEvent> changes = params.getChanges();
		for (FileEvent change: changes) {
			if (!xmlTextDocumentService.documentIsOpen(change.getUri())) {
				xmlTextDocumentService.doSave(change.getUri());
			}
		}
	}
}
