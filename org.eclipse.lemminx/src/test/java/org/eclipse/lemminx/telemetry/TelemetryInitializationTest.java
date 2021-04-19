/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.telemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lemminx.XMLLanguageServer;
import org.eclipse.lemminx.customservice.XMLLanguageClientAPI;
import org.eclipse.lemminx.utils.platform.Platform;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.services.LanguageServer;
import org.junit.jupiter.api.Test;

/**
 * Tests for telemetry
 */
public class TelemetryInitializationTest {

	@Test
	public void telemetryEnabled() {

		List<TelemetryEvent> actualTelementryEvents = new ArrayList<>();
		XMLLanguageServer languageServer = createServer(actualTelementryEvents);
		// By default telemetry is disabled, enable it
		languageServer.getTelemetryManager().setEnabled(true);
		initializeServer(languageServer);

		assertEquals(1, actualTelementryEvents.size());
		assertTrue(actualTelementryEvents.get(0).properties instanceof Map);
		Map<String, Object> initTelemetry = (Map<String, Object>) actualTelementryEvents.get(0).properties;
		assertEquals(Platform.getVersion().getVersionNumber(), initTelemetry.get(InitializationTelemetryInfo.SERVER_VERSION_NUMBER));
		assertNotNull(initTelemetry.get(InitializationTelemetryInfo.JVM_NAME), "Name of JVM is present");
		assertNotNull(initTelemetry.get(InitializationTelemetryInfo.JVM_MEMORY_MAX), "Memory information is present");
		assertFalse((Boolean)initTelemetry.get(InitializationTelemetryInfo.JVM_IS_NATIVE_IMAGE), "Not running under native-image (tests are set up to only run with the Java server)");

	}

	@Test
	public void telemetryDisabled() {

		List<TelemetryEvent> actualTelementryEvents = new ArrayList<>();
		XMLLanguageServer languageServer = createServer(actualTelementryEvents);
		initializeServer(languageServer);

		assertEquals(0, actualTelementryEvents.size());

	}

	private static void initializeServer(LanguageServer languageServer) {
		// initialize -> no telemetry
		InitializeParams params = new InitializeParams();
		languageServer.initialize(params);

		// initialized -> add telemetry
		InitializedParams initialized = new InitializedParams();
		languageServer.initialized(initialized);
	}

	private static XMLLanguageServer createServer(List<TelemetryEvent> actualTelementryEvents) {
		// Create XML Language Server
		XMLLanguageServer languageServer = new XMLLanguageServer();
		XMLLanguageClientAPI client = new XMLLanguageClientAPI() {

			@Override
			public void telemetryEvent(Object object) {
				actualTelementryEvents.add((TelemetryEvent) object);
			}

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
		};
		languageServer.setClient(client);
		return languageServer;
	}

}
