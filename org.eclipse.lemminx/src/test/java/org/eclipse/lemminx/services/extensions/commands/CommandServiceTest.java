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
package org.eclipse.lemminx.services.extensions.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Collectors;

import org.eclipse.lemminx.MockXMLLanguageServer;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.junit.jupiter.api.Test;

/**
 * Test the XML Command service
 * 
 * @author Alex Boyko
 *
 */
public class CommandServiceTest {

	private final MockXMLLanguageServer languageServer;

	public CommandServiceTest() {
		languageServer = new MockXMLLanguageServer();
	}

	private void registerCommand(String command) {
		languageServer.registerCommand(command, (params, sharedSettings, cancelChecker) -> {
			return params.getCommand() + (params.getArguments().isEmpty() ? ""
					: ": " + params.getArguments().stream().map(a -> a.toString()).collect(Collectors.joining(" ")));
		});
	}

	@Test
	public void testCommandExecution() throws Exception {
		registerCommand("my-cmd");
		assertEquals("my-cmd: arg1 arg2", languageServer.executeCommand("my-cmd", "arg1", "arg2").get());
	}

	@Test
	public void testCommandRegistration() throws Exception {
		assertThrows(ResponseErrorException.class, () -> languageServer.executeCommand("my-cmd", "arg1", "arg2").get());
		registerCommand("my-cmd");
		assertEquals("my-cmd", languageServer.executeCommand("my-cmd").get());
		assertThrows(IllegalArgumentException.class, () -> registerCommand("my-cmd"));
		// Nothing should happen - no such command registered
		languageServer.unregisterCommand("cmd");
		languageServer.unregisterCommand("my-cmd");
		assertThrows(ResponseErrorException.class, () -> languageServer.executeCommand("my-cmd").get());
	}

}
