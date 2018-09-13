/**
 *  Copyright (c) 2018 Red Hat, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */

package org.eclipse.lsp4xml.settings.capabilities;

import static org.junit.Assert.assertEquals;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeActionCapabilities;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.DocumentHighlightCapabilities;
import org.eclipse.lsp4j.DocumentLinkCapabilities;
import org.eclipse.lsp4j.DocumentSymbolCapabilities;
import org.eclipse.lsp4j.FoldingRangeCapabilities;
import org.eclipse.lsp4j.FormattingCapabilities;
import org.eclipse.lsp4j.HoverCapabilities;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.RangeFormattingCapabilities;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.RenameCapabilities;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4xml.XMLTextDocumentService;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesInitializer;
import static org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesConstants.*;

/**
 * XMLCapabilityManagerTest
 */
public class XMLCapabilitiesTest {

	private LanguageClient languageClient = new LanguageClientMock();
	private XMLCapabilityManager manager;
	private ClientCapabilities clientCapabilities;
	private TextDocumentClientCapabilities textDocument;
	private XMLTextDocumentService textDocumentService;
	private Set<String> capabilityIDs;

	@Before
	public void startup() {
		XMLFormattingOptions formattingOptions = new XMLFormattingOptions();
		formattingOptions.setEnabled(true);

		textDocumentService = new XMLTextDocumentService(null);
		textDocumentService.setSharedFormattingOptions(formattingOptions);

		textDocument = new TextDocumentClientCapabilities();
		manager = new XMLCapabilityManager(languageClient, textDocumentService);
		clientCapabilities = new ClientCapabilities();
		capabilityIDs = null;

	}

	@Test
	public void testAllDynamicCapabilities() {
		setAllCapabilities(true);
		setAndInitializeCapabilities();

		assertEquals(10, capabilityIDs.size());

		ServerCapabilities serverCapabilities = ServerCapabilitiesInitializer
				.getNonDynamicServerCapabilities(manager.getClientCapabilities(), false);
		assertEquals(false, serverCapabilities.getDocumentRangeFormattingProvider());
		assertEquals(false, serverCapabilities.getDocumentFormattingProvider());
		assertEquals(false, serverCapabilities.getDocumentSymbolProvider());
		assertEquals(false, serverCapabilities.getHoverProvider());
		assertEquals(false, serverCapabilities.getDocumentHighlightProvider());
		assertEquals(false, serverCapabilities.getRenameProvider());
		assertEquals(false, serverCapabilities.getFoldingRangeProvider().getLeft());
		assertEquals(false, serverCapabilities.getCodeActionProvider());
		assertEquals(null, serverCapabilities.getCompletionProvider());
		assertEquals(null, serverCapabilities.getDocumentLinkProvider());
	}

	@Test
	public void testNoDynamicCapabilities() {
		setAllCapabilities(false);
		setAndInitializeCapabilities();

		assertEquals(0, capabilityIDs.size());

		ServerCapabilities serverCapabilities = ServerCapabilitiesInitializer
				.getNonDynamicServerCapabilities(manager.getClientCapabilities(), false);
		assertEquals(true, serverCapabilities.getDocumentRangeFormattingProvider());
		assertEquals(true, serverCapabilities.getDocumentFormattingProvider());
		assertEquals(true, serverCapabilities.getDocumentSymbolProvider());
		assertEquals(true, serverCapabilities.getHoverProvider());
		assertEquals(true, serverCapabilities.getDocumentHighlightProvider());
		assertEquals(true, serverCapabilities.getRenameProvider());
		assertEquals(true, serverCapabilities.getFoldingRangeProvider().getLeft());
		assertEquals(true, serverCapabilities.getCodeActionProvider());
		assertEquals(DEFAULT_COMPLETION_OPTIONS, serverCapabilities.getCompletionProvider());
		assertEquals(DEFAULT_LINK_OPTIONS, serverCapabilities.getDocumentLinkProvider());
	}

	@Test
	public void testBothCapabilityTypes() {
		// Dynamic capabilities
		textDocument.setRangeFormatting(new RangeFormattingCapabilities(true));
		textDocument.setFormatting(new FormattingCapabilities(true));
		CompletionCapabilities completion = new CompletionCapabilities();
		completion.setDynamicRegistration(true);
		textDocument.setCompletion(completion);
		textDocument.setDocumentSymbol(new DocumentSymbolCapabilities(true));

		// Non dynamic capabilities
		textDocument.setHover(new HoverCapabilities(false));
		textDocument.setDocumentHighlight(new DocumentHighlightCapabilities(false));
		textDocument.setRename(new RenameCapabilities(false));
		FoldingRangeCapabilities folding = new FoldingRangeCapabilities();
		folding.setDynamicRegistration(false);
		textDocument.setFoldingRange(folding);
		textDocument.setDocumentLink(new DocumentLinkCapabilities(false));
		textDocument.setCodeAction(new CodeActionCapabilities(false));

		setAndInitializeCapabilities();

		assertEquals(4, capabilityIDs.size());
		assertEquals(true, capabilityIDs.contains(FORMATTING_ID));
		assertEquals(true, capabilityIDs.contains(FORMATTING_RANGE_ID));
		assertEquals(true, capabilityIDs.contains(COMPLETION_ID));
		assertEquals(true, capabilityIDs.contains(DOCUMENT_SYMBOL_ID));

		ServerCapabilities serverCapabilities = ServerCapabilitiesInitializer
				.getNonDynamicServerCapabilities(manager.getClientCapabilities(), false);
		assertEquals(false, serverCapabilities.getDocumentRangeFormattingProvider());
		assertEquals(false, serverCapabilities.getDocumentFormattingProvider());
		assertEquals(false, serverCapabilities.getDocumentSymbolProvider());
		assertEquals(true, serverCapabilities.getHoverProvider());
		assertEquals(true, serverCapabilities.getDocumentHighlightProvider());
		assertEquals(true, serverCapabilities.getRenameProvider());
		assertEquals(true, serverCapabilities.getFoldingRangeProvider().getLeft());
		assertEquals(true, serverCapabilities.getCodeActionProvider());
		assertEquals(null, serverCapabilities.getCompletionProvider());
		assertEquals(DEFAULT_LINK_OPTIONS, serverCapabilities.getDocumentLinkProvider());
	}

	@Test
	public void testDynamicFormattingWithPreferenceFalse() {
		textDocumentService.getSharedFormattingOptions().setEnabled(false);
		// Non Dynamic capabilities
		textDocument.setRangeFormatting(new RangeFormattingCapabilities(true));
		textDocument.setFormatting(new FormattingCapabilities(true));

		setAndInitializeCapabilities();

		Set<String> capabilityIDs = manager.getRegisteredCapabilities();
		assertEquals(0, capabilityIDs.size());

		ServerCapabilities serverCapabilities = ServerCapabilitiesInitializer
				.getNonDynamicServerCapabilities(manager.getClientCapabilities(), false);
		assertEquals(false, serverCapabilities.getDocumentRangeFormattingProvider());
		assertEquals(false, serverCapabilities.getDocumentFormattingProvider());
	}

	@Test
	public void testDynamicFormattingWithPreferenceTrue() {
		textDocumentService.getSharedFormattingOptions().setEnabled(true);
		// Dynamic capabilities
		textDocument.setRangeFormatting(new RangeFormattingCapabilities(true));
		textDocument.setFormatting(new FormattingCapabilities(true));

		setAndInitializeCapabilities();

		Set<String> capabilityIDs = manager.getRegisteredCapabilities();
		assertEquals(2, capabilityIDs.size());
		assertEquals(true, capabilityIDs.contains(FORMATTING_ID));
		assertEquals(true, capabilityIDs.contains(FORMATTING_RANGE_ID));

		ServerCapabilities serverCapabilities = ServerCapabilitiesInitializer
				.getNonDynamicServerCapabilities(manager.getClientCapabilities(), false);
		assertEquals(false, serverCapabilities.getDocumentRangeFormattingProvider());
		assertEquals(false, serverCapabilities.getDocumentFormattingProvider());
	}

	private void setAllCapabilities(boolean areAllDynamic) {
		textDocument.setRangeFormatting(new RangeFormattingCapabilities(areAllDynamic));
		textDocument.setFormatting(new FormattingCapabilities(areAllDynamic));
		CompletionCapabilities completion = new CompletionCapabilities();
		completion.setDynamicRegistration(areAllDynamic);
		textDocument.setCompletion(completion);
		textDocument.setDocumentSymbol(new DocumentSymbolCapabilities(areAllDynamic));
		textDocument.setHover(new HoverCapabilities(areAllDynamic));
		textDocument.setDocumentHighlight(new DocumentHighlightCapabilities(areAllDynamic));
		textDocument.setRename(new RenameCapabilities(areAllDynamic));
		FoldingRangeCapabilities folding = new FoldingRangeCapabilities();
		folding.setDynamicRegistration(areAllDynamic);
		textDocument.setFoldingRange(folding);
		textDocument.setDocumentLink(new DocumentLinkCapabilities(areAllDynamic));
		textDocument.setCodeAction(new CodeActionCapabilities(areAllDynamic));
	}

	private void setAndInitializeCapabilities() {
		clientCapabilities.setTextDocument(textDocument);
		manager.setClientCapabilities(clientCapabilities);
		manager.initializeCapabilities();
		capabilityIDs = manager.getRegisteredCapabilities();
	}

	class LanguageClientMock implements LanguageClient {
		@Override
		public void telemetryEvent(Object object) {
		}

		@Override
		public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
		}

		@Override
		public void showMessage(MessageParams messageParams) {
		}

		@Override
		public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
			return null;
		}

		@Override
		public void logMessage(MessageParams message) {
		}

		@Override
		public CompletableFuture<Void> registerCapability(RegistrationParams params) {
			return null;
		}
	}
}