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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.lemminx.services.extensions.commands.IXMLCommandService;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.eclipse.lsp4j.services.WorkspaceService;

/**
 * XML workspace service.
 *
 */
public class XMLWorkspaceService implements WorkspaceService, IXMLCommandService {

	private final XMLLanguageServer xmlLanguageServer;

	private final Map<String, IDelegateCommandHandler> commands;

	public XMLWorkspaceService(XMLLanguageServer xmlLanguageServer) {
		this.xmlLanguageServer = xmlLanguageServer;
		this.commands = new HashMap<>();
	}

	@Override
	public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
		List<SymbolInformation> res = Collections.synchronizedList(new ArrayList<>());
		return CompletableFuture.allOf(xmlLanguageServer.getXMLLanguageService().getWorkspaceServiceParticipants()
				.stream()
				.map(service -> service.symbol(params))
				.filter(Objects::nonNull)
				.map(future -> future.thenAccept(res::addAll))
				.toArray(CompletableFuture[]::new))
			.thenApply(theVoid -> res);
	}

	@Override
	public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
		synchronized (commands) {
			IDelegateCommandHandler handler = commands.get(params.getCommand());
			if (handler != null) {
				return CompletableFutures.computeAsync(cancelChecker -> {
					try {
						return handler.executeCommand(params, cancelChecker);
					} catch (ResponseErrorException | CancellationException e) {
						throw e;
					} catch (Exception e) {
						throw new ResponseErrorException(
								new ResponseError(ResponseErrorCode.UnknownErrorCode, e.getMessage(), e));
					}
				});
			}
		}
		List<Exception> participantExceptions = new ArrayList<>();
		for (WorkspaceService participant : xmlLanguageServer.getXMLLanguageService().getWorkspaceServiceParticipants()) {
			try {
				CompletableFuture<Object> particpantFuture = participant.executeCommand(params);
				if (particpantFuture != null) {
					return particpantFuture;
				}
			} catch (Exception ex) {
				participantExceptions.add(ex);
			}
		}
		throw new ResponseErrorException(new ResponseError(ResponseErrorCode.InternalError, participantExceptions.stream().map(Throwable::getMessage).collect(Collectors.joining("\n")), null));
	}

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params) {
		xmlLanguageServer.updateSettings(params.getSettings());
		xmlLanguageServer.getXMLLanguageService().getWorkspaceServiceParticipants().forEach(participant -> participant.didChangeConfiguration(params));
		xmlLanguageServer.getCapabilityManager().syncDynamicCapabilitiesWithPreferences();
	}

	@Override
	public void didChangeWorkspaceFolders(DidChangeWorkspaceFoldersParams params) {
		xmlLanguageServer.getXMLLanguageService().getWorkspaceServiceParticipants().forEach(participant -> participant.didChangeWorkspaceFolders(params));
	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
		XMLTextDocumentService xmlTextDocumentService = (XMLTextDocumentService) xmlLanguageServer
				.getTextDocumentService();
		List<FileEvent> changes = params.getChanges();
		for (FileEvent change : changes) {
			if (!xmlTextDocumentService.documentIsOpen(change.getUri())) {
				xmlTextDocumentService.doSave(change.getUri());
			}
		}
		xmlLanguageServer.getXMLLanguageService().getWorkspaceServiceParticipants().forEach(participant -> participant.didChangeWatchedFiles(params));
	}

	@Override
	public void registerCommand(String commandId, IDelegateCommandHandler handler) {
		synchronized (commands) {
			if (commands.containsKey(commandId)) {
				throw new IllegalArgumentException("Command with id '" + commandId + "' is already registered");
			}
			commands.put(commandId, handler);
		}
	}

	@Override
	public void unregisterCommand(String commandId) {
		synchronized (commands) {
			commands.remove(commandId);
		}
	}

	@Override
	public CompletableFuture<Object> executeClientCommand(ExecuteCommandParams command) {
		return xmlLanguageServer.getLanguageClient().executeClientCommand(command);
	}

	@Override
	public void endCommandsRegistration() {
		if (!commands.isEmpty()) {
			xmlLanguageServer.getCapabilityManager().registerExecuteCommand(new ArrayList<>(commands.keySet()));
		}
	}
}
