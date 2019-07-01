/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.telemetry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4xml.Platform;
import org.eclipse.lsp4xml.Platform.JVM;
import org.eclipse.lsp4xml.Platform.OS;
import org.eclipse.lsp4xml.XMLLanguageServer;
import org.eclipse.lsp4xml.telemetry.TelemetryManager.TelementryEvent;
import org.eclipse.lsp4xml.utils.VersionHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * Telemetry test
 * 
 * @author Angelo ZERR
 *
 */
public class TelemetryTest {

	@Test
	public void telemetryEnabled() {

		List<TelementryEvent> actualTelementryEvents = new ArrayList<TelementryEvent>();
		XMLLanguageServer languageServer = createServer(actualTelementryEvents);
		// By default telemetry is disabled, enable it
		languageServer.getTelemetryManager().setEnabled(true);
		callServices(languageServer);

		Assert.assertEquals(5, actualTelementryEvents.size());

		Assert.assertTrue(actualTelementryEvents.get(0).properties instanceof ServerInfo);
		ServerInfo serverInfo = (ServerInfo) actualTelementryEvents.get(0).properties;
		Assert.assertEquals(VersionHelper.getVersion(), serverInfo.getVersion());
		JVM jvm = serverInfo.getJvm();
		Assert.assertTrue(jvm.getMemory().getFree() > 0);
		Assert.assertTrue(jvm.getMemory().getMax() > 0);
		Assert.assertTrue(jvm.getMemory().getTotal() > 0);
		OS os = serverInfo.getOs();
		Assert.assertEquals(Platform.getOS().getName(), os.getName());
		Assert.assertEquals(Platform.getOS().getVersion(), os.getVersion());
		Assert.assertEquals(Platform.getOS().getArch(), os.getArch());

		Assert.assertEquals("textDocument/rename", actualTelementryEvents.get(1).properties);
		Assert.assertEquals("textDocument/rename", actualTelementryEvents.get(2).properties);
		Assert.assertEquals("textDocument/formatting", actualTelementryEvents.get(3).properties);
		Assert.assertEquals("textDocument/rangeFormatting", actualTelementryEvents.get(4).properties);

	}

	@Test
	public void telemetryDisabled() {

		List<TelementryEvent> actualTelementryEvents = new ArrayList<TelementryEvent>();
		XMLLanguageServer languageServer = createServer(actualTelementryEvents);
		callServices(languageServer);

		Assert.assertEquals(0, actualTelementryEvents.size());

	}

	private static void callServices(XMLLanguageServer languageServer) {
		// initialize -> no telemetry
		InitializeParams params = new InitializeParams();
		languageServer.initialize(params);

		// initialized -> add telemetry
		InitializedParams initialized = new InitializedParams();
		languageServer.initialized(initialized);

		TextDocumentIdentifier id = new TextDocumentIdentifier("test.xml");

		// didOpen -> no telemetry
		DidOpenTextDocumentParams open = new DidOpenTextDocumentParams(
				new TextDocumentItem(id.getUri(), "xml", 1, "<foo />"));
		languageServer.getTextDocumentService().didOpen(open);

		// rename -> add telemetry
		RenameParams rename = new RenameParams();
		rename.setTextDocument(id);
		rename.setPosition(new Position(0, 1));
		rename.setNewName("bar");
		languageServer.getTextDocumentService().rename(rename);

		// rename -> add telemetry
		languageServer.getTextDocumentService().rename(rename);

		// formatting -> add telemetry
		DocumentFormattingParams formatting = new DocumentFormattingParams(id, new FormattingOptions());
		languageServer.getTextDocumentService().formatting(formatting);

		// formatting -> add telemetry
		DocumentRangeFormattingParams rangeFormatting = new DocumentRangeFormattingParams(
				new Range(new Position(0, 1), new Position(0, 2)));
		rangeFormatting.setTextDocument(id);
		languageServer.getTextDocumentService().rangeFormatting(rangeFormatting);
	}

	private static XMLLanguageServer createServer(List<TelementryEvent> actualTelementryEvents) {
		// Create XML Language Server
		XMLLanguageServer languageServer = new XMLLanguageServer();
		LanguageClient client = new LanguageClient() {

			@Override
			public void telemetryEvent(Object object) {
				actualTelementryEvents.add((TelementryEvent) object);
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
