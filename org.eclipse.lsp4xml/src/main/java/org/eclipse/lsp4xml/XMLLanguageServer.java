/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml;

import static org.eclipse.lsp4j.jsonrpc.CompletableFutures.computeAsync;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.DocumentLinkOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.eclipse.lsp4xml.commons.ParentProcessWatcher.ProcessLanguageServer;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.model.XMLDocument;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.eclipse.lsp4xml.settings.XMLClientSettings;
import org.eclipse.lsp4xml.utils.JSONUtility;
import org.eclipse.lsp4xml.utils.LogHelper;

/**
 * XML language server.
 *
 */
public class XMLLanguageServer implements LanguageServer, ProcessLanguageServer, XMLCustomService {

	private static final Logger LOGGER = Logger.getLogger(XMLLanguageServer.class.getName());

	private final XMLLanguageService xmlLanguageService;
	private final XMLTextDocumentService xmlTextDocumentService;
	private final XMLWorkspaceService xmlWorkspaceService;
	private LanguageClient languageClient;
	private final ScheduledExecutorService delayer;
	private Integer parentProcessId;

	public XMLLanguageServer() {
		xmlLanguageService = new XMLLanguageService();
		xmlTextDocumentService = new XMLTextDocumentService(this);
		xmlWorkspaceService = new XMLWorkspaceService(this);
		delayer = Executors.newScheduledThreadPool(1);
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		LOGGER.info("Initializing LSP4XML server");
		updateSettings(params.getInitializationOptions());
		xmlTextDocumentService.updateClientCapabilities(params.getCapabilities());
		this.parentProcessId = params.getProcessId();
		ServerCapabilities capabilities = new ServerCapabilities();
		capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
		capabilities.setDocumentSymbolProvider(true);
		capabilities.setDocumentHighlightProvider(true);
		capabilities.setCompletionProvider(new CompletionOptions(false, Arrays.asList("<", ">")));
		capabilities.setDocumentFormattingProvider(true);
		capabilities.setDocumentRangeFormattingProvider(true);
		capabilities.setHoverProvider(true);
		capabilities.setRenameProvider(true);
		capabilities.setFoldingRangeProvider(true);
		capabilities.setDocumentLinkProvider(new DocumentLinkOptions(true));
		capabilities.setCodeActionProvider(true);
		return CompletableFuture.completedFuture(new InitializeResult(capabilities));
	}

	/**
	 * Update XML settings configured from the client.
	 * 
	 * @param settings the XML settings
	 */
	public void updateSettings(Object settings) {
		if (settings == null) {
			return;
		}
		// Update client settings
		XMLClientSettings clientSettings = JSONUtility.toModel(settings, XMLClientSettings.class);
		if (clientSettings != null && clientSettings.getLogs() != null) {
			LogHelper.initializeRootLogger(languageClient, clientSettings.getLogs());
		}
		// Update XML language service extensions
		xmlLanguageService.updateSettings(settings);
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		return computeAsync((cc) -> {
			return new Object();
		});
	}

	@Override
	public void exit() {
		exit(0);
	}

	@Override
	public void exit(int exitCode) {
		delayer.shutdown();
		System.exit(exitCode);
	}

	@Override
	public TextDocumentService getTextDocumentService() {
		return xmlTextDocumentService;
	}

	@Override
	public WorkspaceService getWorkspaceService() {
		return xmlWorkspaceService;
	}

	public void setClient(LanguageClient languageClient) {
		this.languageClient = languageClient;
	}

	public LanguageClient getLanguageClient() {
		return languageClient;
	}

	public XMLLanguageService getXMLLanguageService() {
		return xmlLanguageService;
	}

	public ScheduledFuture<?> schedule(Runnable command, int delay, TimeUnit unit) {
		return delayer.schedule(command, delay, unit);
	}

	@Override
	public long getParentProcessId() {
		return parentProcessId != null ? parentProcessId : 0;
	}

	@Override
	public CompletableFuture<String> closeTag(TextDocumentPositionParams params) {
		return computeAsync((monitor) -> {
			TextDocument document = xmlTextDocumentService.getDocument(params.getTextDocument().getUri());
			XMLDocument xmlDocument = xmlTextDocumentService.getXMLDocument(document);
			return getXMLLanguageService().doAutoClose(xmlDocument, params.getPosition());
		});
	}
}
