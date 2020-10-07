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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lemminx.services.extensions.commands.IXMLCommandService;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.eclipse.lsp4j.services.WorkspaceService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
/**
 * XML workspace service.
 *
 */
public class XMLWorkspaceService implements WorkspaceService, IXMLCommandService {
	
	private static final String WORKSPACE_EXECUTE_COMMAND = "workspace/executeCommand";

	private final XMLLanguageServer xmlLanguageServer;
	
	private final Map<String, IDelegateCommandHandler> commands;
	
	private final Map<String, String> commandRegistrations;
	
	public XMLWorkspaceService(XMLLanguageServer xmlLanguageServer) {
		this.xmlLanguageServer = xmlLanguageServer;
		this.commands = new HashMap<>();
		this.commandRegistrations = new HashMap<>();
	}

	@Override
	public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
		return null;
	}
	
	
	@Override
	public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
		synchronized (commands) {
			IDelegateCommandHandler handler = commands.get(params.getCommand());
			if (handler == null) {
				throw new ResponseErrorException(new ResponseError(ResponseErrorCode.InternalError, "No command handler for the command: " + params.getCommand(), null));
			}
			return CompletableFutures.computeAsync(cancelChecker -> {
				try {
					return handler.executeCommand(params, cancelChecker);
				} catch (Exception e) {
					if (e instanceof ResponseErrorException) {
						throw (ResponseErrorException) e;
					} else if (e instanceof CancellationException) {
						throw (CancellationException) e;
					}
					throw new ResponseErrorException(
							new ResponseError(ResponseErrorCode.UnknownErrorCode, e.getMessage(), e));
				}
			});
		}
	}
	
	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params) {
		xmlLanguageServer.updateSettings(params.getSettings());
		xmlLanguageServer.getCapabilityManager().syncDynamicCapabilitiesWithPreferences();
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

	@Override
	public void registerCommand(String commandId, IDelegateCommandHandler handler) {
		synchronized (commands) {
			if (commands.containsKey(commandId)) {
				throw new IllegalArgumentException("Command with id '" + commandId + "' is already registered");
			}
			String registrationId = UUID.randomUUID().toString();
			xmlLanguageServer.getCapabilityManager().registerCapability(registrationId, WORKSPACE_EXECUTE_COMMAND, ImmutableMap.of("commands", ImmutableList.of(commandId)));
			commandRegistrations.put(commandId, registrationId);
			commands.put(commandId, handler);
		}
	}

	@Override
	public void unregisterCommand(String commandId) {
		synchronized (commands) {
			commands.remove(commandId);
			String registrationId = commandRegistrations.remove(commandId);
			if (registrationId != null) {
				xmlLanguageServer.getCapabilityManager().unregisterCapability(registrationId, WORKSPACE_EXECUTE_COMMAND);
			}
		}
	}

	@Override
	public CompletableFuture<Object> executeClientCommand(ExecuteCommandParams command) {
		return xmlLanguageServer.getLanguageClient().executeClientCommand(command);
	}
}
