/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.mutable.MutableInt;
import org.eclipse.lemminx.XMLLanguageServer;
import org.eclipse.lemminx.XMLTextDocumentService;
import org.eclipse.lemminx.client.ExtendedClientCapabilities;
import org.eclipse.lemminx.customservice.ActionableNotification;
import org.eclipse.lemminx.customservice.XMLLanguageClientAPI;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for LimitExceededWarnings
 */
public class LimitExceededWarningsTest {

	private final String TEST_FEATURE = "";
	private final int TEST_LIMIT = 0;

	private XMLLanguageServer languageServer;
	private XMLTextDocumentService textDocumentService;
	private MutableInt actionableNotificationCount;
	private MutableInt showMessageCount;

	@BeforeEach
	public void before() {
		actionableNotificationCount = new MutableInt(0);
		showMessageCount = new MutableInt(0);
		languageServer = createServer(actionableNotificationCount, showMessageCount);
		textDocumentService = (XMLTextDocumentService) languageServer.getTextDocumentService();
	}

	@Test
	public void testSendActionableNotification() {
		String uri1 = "file:///uri1.xml";
		String uri2 = "file:///uri2.xml";
		setSupportCapabilities(true, true);

		sendNotification(uri1);
		assertCounts(1, 0);

		sendNotification(uri1);
		assertCounts(1, 0);

		sendNotification(uri2);
		assertCounts(2, 0);

		sendNotification(uri2);
		assertCounts(2, 0);
	}

	@Test
	public void testActionableNotificationEvict() {
		String uri1 = "file:///uri1.xml";
		String uri2 = "file:///uri2.xml";
		setSupportCapabilities(true, true);

		sendNotification(uri1);
		assertCounts(1, 0);
		sendNotification(uri2);
		assertCounts(2, 0);

		textDocumentService.getLimitExceededWarnings().evict(uri1);

		sendNotification(uri1);
		assertCounts(3, 0);
		sendNotification(uri2);
		assertCounts(3, 0);

		textDocumentService.getLimitExceededWarnings().evict(uri2);

		sendNotification(uri1);
		assertCounts(3, 0);
		sendNotification(uri2);
		assertCounts(4, 0);
	}

	@Test
	public void testSendMessage() {
		String uri1 = "file:///uri1.xml";
		String uri2 = "file:///uri2.xml";
		setSupportCapabilities(false, false);

		sendNotification(uri1);
		assertCounts(0, 1);

		sendNotification(uri1);
		assertCounts(0, 1);

		sendNotification(uri2);
		assertCounts(0, 2);

		sendNotification(uri2);
		assertCounts(0, 2);
	}

	@Test
	public void testSendMessage2() {
		String uri = "file:///uri.xml";
		setSupportCapabilities(true, false);
		sendNotification(uri);
		assertCounts(0, 1);
	}

	@Test
	public void testSendMessage3() {
		String uri = "file:///uri.xml";
		setSupportCapabilities(false, true);
		sendNotification(uri);
		assertCounts(0, 1);
	}

	@Test
	public void testSendMessageEvict() {
		String uri1 = "file:///uri1.xml";
		String uri2 = "file:///uri2.xml";
		setSupportCapabilities(false, false);

		sendNotification(uri1);
		assertCounts(0, 1);
		sendNotification(uri2);
		assertCounts(0, 2);

		textDocumentService.getLimitExceededWarnings().evict(uri1);

		sendNotification(uri1);
		assertCounts(0, 3);
		sendNotification(uri2);
		assertCounts(0, 3);

		textDocumentService.getLimitExceededWarnings().evict(uri2);

		sendNotification(uri1);
		assertCounts(0, 3);
		sendNotification(uri2);
		assertCounts(0, 4);
	}

	private static XMLLanguageServer createServer(MutableInt actionableNotificationCount, MutableInt showMessageCount) {

		XMLLanguageServer languageServer = new XMLLanguageServer();
		XMLLanguageClientAPI client = new XMLLanguageClientAPI() {

			@Override
			public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
				return null;
			}

			@Override
			public void showMessage(MessageParams messageParams) {
				showMessageCount.increment();
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
			public void actionableNotification(ActionableNotification notification) {
				actionableNotificationCount.increment();
			}
		};
		languageServer.setClient(client);
		return languageServer;
	}

	private void setSupportCapabilities(boolean supportActionableNotification, boolean supportOpenSettings) {
		ExtendedClientCapabilities capabilities = new ExtendedClientCapabilities();
		capabilities.setActionableNotificationSupport(supportActionableNotification);
		capabilities.setOpenSettingsCommandSupport(supportOpenSettings);
		textDocumentService.updateClientCapabilities(new ClientCapabilities(), capabilities);
	}

	private void sendNotification(String uri) {
		textDocumentService.getLimitExceededWarnings()
				.onResultLimitExceeded(uri, TEST_LIMIT, TEST_FEATURE);
	}

	/**
	 * Assert "actionable notification" and "send message" counts
	 * 
	 * @param actionableNotification expected "actionable notification" count
	 * @param sendMessage            expected "send message" count
	 */
	private void assertCounts(int actionableNotification, int sendMessage) {
		assertEquals(actionableNotification, actionableNotificationCount.intValue());
		assertEquals(sendMessage, showMessageCount.intValue());
	}
}