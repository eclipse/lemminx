/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.lemminx.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.lemminx.XMLLanguageServer;
import org.eclipse.lemminx.customservice.XMLLanguageClientAPI;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.UnregistrationParams;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test the XML Command service
 * 
 * @author Alex Boyko
 *
 */
public class CommandServiceTest {
	
	private static XMLLanguageServer createServer() {

		XMLLanguageServer languageServer = new XMLLanguageServer();
		XMLLanguageClientAPI client = new XMLLanguageClientAPI() {

			@Override
			public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
				return null;
			}

			@Override
			public void showMessage(MessageParams messageParams) {
				
			}

			@Override
			public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {

			}

			@Override
			public void logMessage(MessageParams message) {

			}

			@Override
			public void telemetryEvent(Object object) {

			}

			@Override
			public CompletableFuture<Void> registerCapability(RegistrationParams params) {
				return CompletableFuture.completedFuture(null);
			}

			@Override
			public CompletableFuture<Void> unregisterCapability(UnregistrationParams params) {
				return CompletableFuture.completedFuture(null);
			}
			
			@Override
			public CompletableFuture<Object> executeClientCommand(ExecuteCommandParams params) {
				return languageServer.getWorkspaceService().executeCommand(params);
			}

		};
		languageServer.setClient(client);
		return languageServer;
	}
	
	private void registerCommand(String command) {
		server.getXMLLanguageService().getCommandService().registerCommand("my-cmd", (params, cancelChecker) -> {
			return params.getCommand() + 
				(params.getArguments().isEmpty() ? "" : ": " + params.getArguments().stream().map(a -> a.toString()).collect(Collectors.joining( " " )));
		});
	}
	
	private void unregisterCommand(String command) {
		server.getXMLLanguageService().getCommandService().unregisterCommand(command);
	}
	
	private CompletableFuture<Object> executeCommand(String command, Object... arguments) {
		ExecuteCommandParams params = new ExecuteCommandParams(command, Arrays.asList(arguments));
		return server.getXMLLanguageService().getCommandService().executeClientCommand(params);
	}
	
	private XMLLanguageServer server;
	
	@BeforeEach
	public void setup() {
		this.server = createServer();
	}
	
	@Test
	public void testCommandExecution() throws Exception {
		registerCommand("my-cmd");
		assertEquals("my-cmd: arg1 arg2", executeCommand("my-cmd", "arg1", "arg2").get());		
	}
	
	@Test
	public void testCommandRegistration() throws Exception {
		assertThrows(ResponseErrorException.class, () -> executeCommand("my-cmd", "arg1", "arg2").get());
		registerCommand("my-cmd");
		assertEquals("my-cmd", executeCommand("my-cmd").get());
		assertThrows(IllegalArgumentException.class, () -> registerCommand("my-cmd"));
		// Nothing should happen - no such command registered
		unregisterCommand("cmd");
		unregisterCommand("my-cmd");
		assertThrows(ResponseErrorException.class, () -> executeCommand("my-cmd").get());
	}


}
