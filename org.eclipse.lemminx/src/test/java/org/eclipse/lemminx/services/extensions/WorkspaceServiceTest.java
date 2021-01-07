/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*********************************************************************************/
package org.eclipse.lemminx.services.extensions;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.lemminx.XMLLanguageServer;
import org.eclipse.lemminx.customservice.XMLLanguageClientAPI;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.WorkspaceFoldersChangeEvent;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WorkspaceServiceTest {

	private static class CaptureWokspaceServiceCalls implements WorkspaceService {

		public DidChangeConfigurationParams didChangeConfigurationParams;
		public DidChangeWatchedFilesParams didChangeWatchedFilesParams;
		public DidChangeWorkspaceFoldersParams didChangeWorkspaceFolders;

		@Override
		public void didChangeConfiguration(DidChangeConfigurationParams params) {
			this.didChangeConfigurationParams = params;
		}

		@Override
		public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
			this.didChangeWatchedFilesParams = params;
		}

		@Override
		public void didChangeWorkspaceFolders(DidChangeWorkspaceFoldersParams params) {
			this.didChangeWorkspaceFolders = params;
		}

		@Override
		public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
			return CompletableFuture.completedFuture(Collections.emptyList());
		}
	}
	
	private CaptureWokspaceServiceCalls workspaceService;
	private XMLLanguageServer server;

	@BeforeEach
	public void initializeLanguageService() {
		this.server = new XMLLanguageServer();
		server.setClient(new XMLLanguageClientAPI() {
			@Override public void telemetryEvent(Object object) { }
			@Override public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
				return null;
			}	
			@Override public void showMessage(MessageParams messageParams) { }
			@Override public void publishDiagnostics(PublishDiagnosticsParams diagnostics) { }
			@Override public void logMessage(MessageParams message) { }
			@Override public CompletableFuture<Void> registerCapability(RegistrationParams params) {
				return null;
			}
		});
		this.workspaceService = new CaptureWokspaceServiceCalls();
		server.getXMLLanguageService().registerWorkspaceServiceParticipant(this.workspaceService);
	}

	@Test
	public void testWorkspaceFolders() {
		DidChangeWorkspaceFoldersParams params = new DidChangeWorkspaceFoldersParams(new WorkspaceFoldersChangeEvent(Collections.singletonList(new WorkspaceFolder("added")), Collections.singletonList(new WorkspaceFolder("removed"))));
		server.getWorkspaceService().didChangeWorkspaceFolders(params);
		assertArrayEquals(new String[] { "added" }, workspaceService.didChangeWorkspaceFolders.getEvent().getAdded().stream().map(WorkspaceFolder::getUri).toArray(String[]::new));
		assertArrayEquals(new String[] { "removed" }, workspaceService.didChangeWorkspaceFolders.getEvent().getRemoved().stream().map(WorkspaceFolder::getUri).toArray(String[]::new));
	}

	@Test
	public void testConfiguration() {
		DidChangeConfigurationParams params = new DidChangeConfigurationParams("hello");
		server.getWorkspaceService().didChangeConfiguration(params);
		assertEquals("hello", workspaceService.didChangeConfigurationParams.getSettings());
	}

	@Test
	public void testWatchedFiles() {
		DidChangeWatchedFilesParams params = new DidChangeWatchedFilesParams(Collections.singletonList(new FileEvent("hello", FileChangeType.Changed)));
		server.getWorkspaceService().didChangeWatchedFiles(params);
		assertArrayEquals(new String[] {"hello"}, workspaceService.didChangeWatchedFilesParams.getChanges().stream().map(FileEvent::getUri).toArray(String[]::new));
	}

	@Test
	public void testExecuteCommand() throws InterruptedException, ExecutionException {
		server.getXMLLanguageService().registerWorkspaceServiceParticipant(new CaptureWokspaceServiceCalls() {
			@Override
			public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
				if ("sayHello".equals(params.getCommand())) {
					return CompletableFuture.completedFuture("hello");
				}
				return null;
			}
		});
		ExecuteCommandParams params = new ExecuteCommandParams("sayHello", Collections.emptyList());
		assertEquals("hello", server.getWorkspaceService().executeCommand(params).get());
		//
		params.setCommand("unknownCommand");
		try {
			server.getWorkspaceService().executeCommand(params);
			fail("Exception expected when requesting unknown command");
		} catch (ResponseErrorException ex) {
			// OK
		} catch (Exception ex) {
			assertEquals(ResponseErrorException.class, ex.getClass());
		}
	}

	@Test
	public void textSymbols() throws InterruptedException, ExecutionException {
		Location dummyLocation = new Location("blah", new Range(new Position(0, 0), new Position(0, 1)));
		server.getXMLLanguageService().registerWorkspaceServiceParticipant(new CaptureWokspaceServiceCalls() {
			@Override
			public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
				return CompletableFuture.completedFuture(Arrays.asList(
						new SymbolInformation("hi", SymbolKind.Array, dummyLocation),
						new SymbolInformation("hello", SymbolKind.Array, dummyLocation)));
			}
		});
		server.getXMLLanguageService().registerWorkspaceServiceParticipant(new CaptureWokspaceServiceCalls() {
			@Override
			public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
				return CompletableFuture.completedFuture(Arrays.asList(
						new SymbolInformation("salut", SymbolKind.Array, dummyLocation),
						new SymbolInformation("bonjour", SymbolKind.Array, dummyLocation)));
			}
		});
		List<? extends SymbolInformation> symbols = server.getWorkspaceService().symbol(new WorkspaceSymbolParams()).get();
		assertArrayEquals(new String[] { "bonjour", "hello", "hi", "salut" }, symbols.stream().map(SymbolInformation::getName).sorted().toArray(String[]::new));
	}
}
