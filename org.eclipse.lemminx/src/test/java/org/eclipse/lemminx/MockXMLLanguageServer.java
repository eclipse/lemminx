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
package org.eclipse.lemminx;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lemminx.customservice.ActionableNotification;
import org.eclipse.lemminx.services.extensions.commands.IXMLCommandService.IDelegateCommandHandler;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;

/**
 * Mock XML Language server which helps to track show messages, actionable
 * notification and commands.
 * 
 * @author Angelo ZERR
 *
 */
public class MockXMLLanguageServer extends XMLLanguageServer {

	public MockXMLLanguageServer() {
		super.setClient(new MockXMLLanguageClient());
	}

	public List<PublishDiagnosticsParams> getPublishDiagnostics() {
		return getLanguageClient().getPublishDiagnostics();
	}

	@Override
	public MockXMLLanguageClient getLanguageClient() {
		return (MockXMLLanguageClient) super.getLanguageClient();
	}

	public List<MessageParams> getShowMessages() {
		return getLanguageClient().getShowMessages();
	}

	public List<ActionableNotification> getActionableNotifications() {
		return getLanguageClient().getActionableNotifications();
	}

	public void registerCommand(String commandId, IDelegateCommandHandler handler) {
		getXMLLanguageService().getCommandService().registerCommand(commandId, handler);
	}

	public void unregisterCommand(String commandId) {
		getXMLLanguageService().getCommandService().unregisterCommand(commandId);
	}

	public CompletableFuture<Object> executeCommand(String command, Object... arguments) {
		getXMLLanguageService().initializeIfNeeded();
		ExecuteCommandParams params = new ExecuteCommandParams(command, Arrays.asList(arguments));
		return getWorkspaceService().executeCommand(params);
	}

	public TextDocumentIdentifier didOpen(String fileURI, String xml) {
		TextDocumentIdentifier xmlIdentifier = new TextDocumentIdentifier(fileURI);
		DidOpenTextDocumentParams params = new DidOpenTextDocumentParams(
				new TextDocumentItem(xmlIdentifier.getUri(), "xml", 1, xml));
		getTextDocumentService().didOpen(params);
		return xmlIdentifier;
	}
}
