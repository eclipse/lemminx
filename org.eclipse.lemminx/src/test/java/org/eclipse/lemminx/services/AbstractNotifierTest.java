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

/**
 * Test class for notification tests
 */
public abstract class AbstractNotifierTest {

	protected XMLLanguageServer languageServer;

	private MutableInt actionableNotificationCount;

	private MutableInt showMessageCount;

	private StringBuilder message;

	public void before() {
		this.actionableNotificationCount = new MutableInt(0);
		this.showMessageCount = new MutableInt(0);
		this.message = new StringBuilder();
		this.languageServer = createServer(message, actionableNotificationCount, showMessageCount);
	}

	private static XMLLanguageServer createServer(StringBuilder message,
			MutableInt actionableNotificationCount, MutableInt showMessageCount) {

		XMLLanguageServer languageServer = new XMLLanguageServer();
		XMLLanguageClientAPI client = new XMLLanguageClientAPI() {

			@Override
			public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
				return null;
			}

			@Override
			public void showMessage(MessageParams messageParams) {
				showMessageCount.increment();
				message.setLength(0);
				message.append(messageParams.getMessage());
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
				message.setLength(0);
				message.append(notification.getMessage());
			}
		};
		languageServer.setClient(client);
		return languageServer;
	}

	protected void setSupportCapabilities(boolean supportActionableNotification, boolean supportOpenSettings) {
		ExtendedClientCapabilities capabilities = new ExtendedClientCapabilities();
		capabilities.setActionableNotificationSupport(supportActionableNotification);
		capabilities.setOpenSettingsCommandSupport(supportOpenSettings);
		getTextDocumentService().updateClientCapabilities(new ClientCapabilities(), capabilities);
	}

	protected XMLTextDocumentService getTextDocumentService() {
		return (XMLTextDocumentService) languageServer.getTextDocumentService();
	}

	/**
	 * Asserts "actionable notification" and "send message" counts
	 * 
	 * @param actionableNotification expected "actionable notification" count
	 * @param sendMessage            expected "send message" count
	 */
	protected void assertCounts(int actionableNotification, int sendMessage) {
		assertEquals(actionableNotification, actionableNotificationCount.intValue());
		assertEquals(sendMessage, showMessageCount.intValue());
	}

	/**
	 * Asserts message
	 * 
	 * @param expectedMessage expected message
	 */
	protected void assertMessage(String expectedMessage) {
		assertEquals(expectedMessage, message.toString());
	}
} 