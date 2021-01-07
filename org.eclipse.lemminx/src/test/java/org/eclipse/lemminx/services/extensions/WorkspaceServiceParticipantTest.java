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

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lemminx.XMLLanguageServer;
import org.eclipse.lemminx.customservice.XMLLanguageClientAPI;
import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.WorkspaceFoldersChangeEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WorkspaceServiceParticipantTest {

	private static class CaptureWokspaceServiceCalls implements IWorkspaceServiceParticipant {

		public DidChangeWorkspaceFoldersParams didChangeWorkspaceFolders;

		@Override
		public void didChangeWorkspaceFolders(DidChangeWorkspaceFoldersParams params) {
			this.didChangeWorkspaceFolders = params;
		}
	}
	
	private CaptureWokspaceServiceCalls workspaceServiceParticipant;
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
		this.workspaceServiceParticipant = new CaptureWokspaceServiceCalls();
		server.getXMLLanguageService().registerWorkspaceServiceParticipant(this.workspaceServiceParticipant);
	}

	@Test
	public void testWorkspaceFolders() {
		DidChangeWorkspaceFoldersParams params = new DidChangeWorkspaceFoldersParams(new WorkspaceFoldersChangeEvent(Collections.singletonList(new WorkspaceFolder("added")), Collections.singletonList(new WorkspaceFolder("removed"))));
		server.getWorkspaceService().didChangeWorkspaceFolders(params);
		assertArrayEquals(new String[] { "added" }, workspaceServiceParticipant.didChangeWorkspaceFolders.getEvent().getAdded().stream().map(WorkspaceFolder::getUri).toArray(String[]::new));
		assertArrayEquals(new String[] { "removed" }, workspaceServiceParticipant.didChangeWorkspaceFolders.getEvent().getRemoved().stream().map(WorkspaceFolder::getUri).toArray(String[]::new));
	}
}
