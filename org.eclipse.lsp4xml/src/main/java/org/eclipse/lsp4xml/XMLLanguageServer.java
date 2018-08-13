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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DocumentLinkOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.eclipse.lsp4xml.utils.JSONUtility;
import org.eclipse.lsp4xml.utils.LogHelper;

import toremove.org.eclipse.lsp4j.ExtendedLanguageServer;
import toremove.org.eclipse.lsp4j.ExtendedServerCapabilities;
import toremove.org.eclipse.lsp4j.FoldingRange;
import toremove.org.eclipse.lsp4j.FoldingRangeRequestParams;

/**
 * XML language server.
 *
 */
public class XMLLanguageServer implements LanguageServer, ExtendedLanguageServer, WorkspaceService {

	/**
	 * Exit code returned when XML Language Server is forced to exit.
	 */
	private static final int FORCED_EXIT_CODE = 1;
	private static final Logger LOGGER = Logger.getLogger(XMLLanguageServer.class.getName());
	private static final String DEFAULT_LOG_PATH_KEY = "lsp4xml.logPath";

	private final XMLLanguageService xmlLanguageService;
	private final XMLTextDocumentService xmlTextDocumentService;
	private final XMLWorkspaceService xmlWorkspaceService;
	private LanguageClient languageClient;

	public XMLLanguageServer() {
		xmlLanguageService = new XMLLanguageService();
		xmlTextDocumentService = new XMLTextDocumentService(this);
		xmlWorkspaceService = new XMLWorkspaceService(this);
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		xmlTextDocumentService.updateClientCapabilities(params.getCapabilities());
		// FIXME: use ServerCapabilities when
		// https://github.com/eclipse/lsp4j/issues/169 will be ready
		ExtendedServerCapabilities capabilities = new ExtendedServerCapabilities();
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
		InitializeResult result = new InitializeResult(capabilities);
		setupInitializationOptions(getInitializationOptions(params));

		return CompletableFuture.completedFuture(result);
	}


	private void setupInitializationOptions(Map<?, ?> initializationOptions) {

		//Set default log path
		String newDefaultLogPath = (String) initializationOptions.get(DEFAULT_LOG_PATH_KEY);
		LogHelper.setDefaultLogPath(newDefaultLogPath);

		//Configure other initial settings
	}

	public static Map<?, ?> getInitializationOptions(InitializeParams params) {
		Map<?, ?> initOptions = JSONUtility.toModel(params.getInitializationOptions(), Map.class);
		return initOptions == null ? Collections.emptyMap() : initOptions;
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		return computeAsync((cc) -> {
			return new Object();
		});
	}

	@Override
	public void exit() {
		Executors.newSingleThreadScheduledExecutor().schedule(() -> {
			LOGGER.warning("Force exiting after 1 minute");
			System.exit(FORCED_EXIT_CODE);
		}, 1, TimeUnit.MINUTES);
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

	// FIXME: remove this method when https://github.com/eclipse/lsp4j/issues/169
	// will be ready
	@Override
	public CompletableFuture<List<? extends FoldingRange>> foldingRanges(FoldingRangeRequestParams params) {
		return xmlTextDocumentService.foldingRanges(params);
	}
	

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params) {
		Object settings = JSONUtility.toModel(params.getSettings(), JsonObject.class);
		if(settings instanceof JsonObject) {
			JsonObject newParams = (JsonObject) settings;

			/**
			 * Settings with name similar to eg: xml.log.path cannot be retrieved normally 
			 * since each part is considered a key, use getJsonPrimitiveFromJson
			 */

			//Updates the logger output path -------
			JsonPrimitive jsonLogPath = getJsonPrimitiveFromJson(newParams, "xml", "log", "location");
			String logPath = jsonLogPath.getAsString();
			LogHelper.updatePath(logPath);
			
			//--------------------------

			//If the client should get logged to in process output
			JsonPrimitive jsonLogToClient = getJsonPrimitiveFromJson(newParams, "xml", "log", "client");
			LogHelper.setShouldLogToClient(jsonLogToClient.getAsBoolean());
			//---------------------------------------------------

			LogHelper.initializeRootLogger(languageClient);
		}
		
	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
		
	}

	public static JsonPrimitive getJsonPrimitiveFromJson(JsonObject object, String... keys) {
		if(object == null || keys == null) {
			return null;
		}
		JsonObject currentJson = object;
		int i;
		for(i = 0; i < keys.length - 1; i++) {
			currentJson = currentJson.getAsJsonObject(keys[i]);
		}
		return currentJson.getAsJsonPrimitive(keys[i]);
	}

	@Override
	public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
		return null;
	}
}
