/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *  Red Hat Inc. - Dynamic Server capabilities
 */
package org.eclipse.lemminx;

import static org.eclipse.lemminx.utils.VersionHelper.getVersion;
import static org.eclipse.lsp4j.jsonrpc.CompletableFutures.computeAsync;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.eclipse.lemminx.client.ExtendedClientCapabilities;
import org.eclipse.lemminx.commons.ModelTextDocument;
import org.eclipse.lemminx.commons.ParentProcessWatcher.ProcessLanguageServer;
import org.eclipse.lemminx.customservice.AutoCloseTagResponse;
import org.eclipse.lemminx.customservice.XMLCustomService;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.settings.ContentModelSettings;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.logs.LogHelper;
import org.eclipse.lemminx.services.IXMLDocumentProvider;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lemminx.settings.AllXMLSettings;
import org.eclipse.lemminx.settings.InitializationOptionsSettings;
import org.eclipse.lemminx.settings.LogsSettings;
import org.eclipse.lemminx.settings.ServerSettings;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lemminx.settings.XMLCodeLensSettings;
import org.eclipse.lemminx.settings.XMLCompletionSettings;
import org.eclipse.lemminx.settings.XMLFormattingOptions;
import org.eclipse.lemminx.settings.XMLGeneralClientSettings;
import org.eclipse.lemminx.settings.XMLSymbolSettings;
import org.eclipse.lemminx.settings.capabilities.InitializationOptionsExtendedClientCapabilities;
import org.eclipse.lemminx.settings.capabilities.ServerCapabilitiesInitializer;
import org.eclipse.lemminx.settings.capabilities.XMLCapabilityManager;
import org.eclipse.lemminx.utils.FilesUtils;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

/**
 * XML language server.
 *
 */
public class XMLLanguageServer
		implements LanguageServer, ProcessLanguageServer, XMLCustomService, IXMLDocumentProvider {

	private static final Logger LOGGER = Logger.getLogger(XMLLanguageServer.class.getName());

	private final XMLLanguageService xmlLanguageService;
	private final XMLTextDocumentService xmlTextDocumentService;
	private final XMLWorkspaceService xmlWorkspaceService;
	private LanguageClient languageClient;
	private final ScheduledExecutorService delayer;
	private Integer parentProcessId;
	public XMLCapabilityManager capabilityManager;

	public XMLLanguageServer() {
		xmlLanguageService = new XMLLanguageService();
		xmlLanguageService.setDocumentProvider(this);
		xmlTextDocumentService = new XMLTextDocumentService(this);
		xmlWorkspaceService = new XMLWorkspaceService(this);
		delayer = Executors.newScheduledThreadPool(1);
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		LOGGER.info("Initializing XML Language server " + getVersion() + " with " + System.getProperty("java.home"));
		this.parentProcessId = params.getProcessId();

		// Update XML language service extensions with InitializeParams
		xmlLanguageService.initializeParams(params);

		ExtendedClientCapabilities extendedClientCapabilities = InitializationOptionsExtendedClientCapabilities
				.getExtendedClientCapabilities(params);
		capabilityManager.setClientCapabilities(params.getCapabilities(), extendedClientCapabilities);
		updateSettings(InitializationOptionsSettings.getSettings(params));

		xmlTextDocumentService.updateClientCapabilities(capabilityManager.getClientCapabilities().capabilities,
				capabilityManager.getClientCapabilities().getExtendedCapabilities());
		ServerCapabilities nonDynamicServerCapabilities = ServerCapabilitiesInitializer.getNonDynamicServerCapabilities(
				capabilityManager.getClientCapabilities(), xmlTextDocumentService.isIncrementalSupport());

		return CompletableFuture.completedFuture(new InitializeResult(nonDynamicServerCapabilities));
	}

	/*
	 * Registers all capabilities that do not support client side preferences to
	 * turn on/off
	 *
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.lsp4j.services.LanguageServer#initialized(org.eclipse.lsp4j.
	 * InitializedParams)
	 */
	@Override
	public void initialized(InitializedParams params) {
		capabilityManager.initializeCapabilities();
	}

	/**
	 * Update XML settings configured from the client.
	 * 
	 * @param initializationOptionsSettings the XML settings
	 */
	public synchronized void updateSettings(Object initializationOptionsSettings) {
		if (initializationOptionsSettings == null) {
			return;
		}
		// Update client settings
		initializationOptionsSettings = AllXMLSettings.getAllXMLSettings(initializationOptionsSettings);
		XMLGeneralClientSettings xmlClientSettings = XMLGeneralClientSettings
				.getGeneralXMLSettings(initializationOptionsSettings);
		if (xmlClientSettings != null) {
			// Update logs settings
			LogsSettings logsSettings = xmlClientSettings.getLogs();
			if (logsSettings != null) {
				LogHelper.initializeRootLogger(languageClient, logsSettings);
			}
			// Update format settings
			XMLFormattingOptions formatterSettings = xmlClientSettings.getFormat();
			if (formatterSettings != null) {
				xmlTextDocumentService.getSharedFormattingSettings().merge(formatterSettings);
			}

			XMLCompletionSettings newCompletions = xmlClientSettings.getCompletion();
			if (newCompletions != null) {
				xmlTextDocumentService.updateCompletionSettings(newCompletions);
			}

			XMLSymbolSettings newSymbols = xmlClientSettings.getSymbols();
			if (newSymbols != null) {
				xmlTextDocumentService.updateSymbolSettings(newSymbols);
			}

			XMLCodeLensSettings newCodeLens = xmlClientSettings.getCodeLens();
			if (newCodeLens != null) {
				xmlTextDocumentService.updateCodeLensSettings(newCodeLens);
			}

			ServerSettings serverSettings = xmlClientSettings.getServer();
			if (serverSettings != null) {
				String workDir = serverSettings.getNormalizedWorkDir();
				FilesUtils.setCachePathSetting(workDir);
			}
		}
		ContentModelSettings cmSettings = ContentModelSettings
				.getContentModelXMLSettings(initializationOptionsSettings);
		if (cmSettings != null) {
			XMLValidationSettings validationSettings = cmSettings.getValidation();
			xmlTextDocumentService.getValidationSettings().merge(validationSettings);

		}
		// Update XML language service extensions
		xmlTextDocumentService.updateSettings(initializationOptionsSettings);
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		xmlLanguageService.dispose();
		return computeAsync(cc -> new Object());
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
		capabilityManager = new XMLCapabilityManager(this.languageClient, xmlTextDocumentService);
	}

	public LanguageClient getLanguageClient() {
		return languageClient;
	}

	public XMLLanguageService getXMLLanguageService() {
		return xmlLanguageService;
	}

	public SharedSettings getSettings() {
		return xmlTextDocumentService.getSharedSettings();
	}

	public ScheduledFuture<?> schedule(Runnable command, int delay, TimeUnit unit) {
		return delayer.schedule(command, delay, unit);
	}

	@Override
	public long getParentProcessId() {
		return parentProcessId != null ? parentProcessId : 0;
	}

	@Override
	public CompletableFuture<AutoCloseTagResponse> closeTag(TextDocumentPositionParams params) {
		return xmlTextDocumentService.computeDOMAsync(params.getTextDocument(), (cancelChecker, xmlDocument) -> {
			return getXMLLanguageService().doAutoClose(xmlDocument, params.getPosition(), cancelChecker);
		});
	}

	@Override
	public CompletableFuture<Position> matchingTagPosition(TextDocumentPositionParams params) {
		return xmlTextDocumentService.computeDOMAsync(params.getTextDocument(), (cancelChecker, xmlDocument) -> {
			return getXMLLanguageService().getMatchingTagPosition(xmlDocument, params.getPosition(), cancelChecker);
		});
	}

	@Override
	public DOMDocument getDocument(String uri) {
		ModelTextDocument<DOMDocument> document = xmlTextDocumentService.getDocument(uri);
		return document != null ? document.getModel().getNow(null) : null;
	}
}
