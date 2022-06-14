/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.client;

/**
 * Extended client capabilities not defined by the LSP.
 *
 * @author Angelo ZERR
 *
 * @see https://github.com/microsoft/language-server-protocol/issues/788
 */
public class ExtendedClientCapabilities {

	private boolean actionableNotificationSupport;

	private boolean shouldLanguageServerExitOnShutdown;

	private CommandCapabilities commands;

	private ExtendedCodeLensCapabilities codeLens;

	public CommandCapabilities getCommands() {
		return commands;
	}

	public void setCommands(CommandCapabilities commands) {
		this.commands = commands;
	}

	public ExtendedCodeLensCapabilities getCodeLens() {
		return codeLens;
	}

	public void setCodeLens(ExtendedCodeLensCapabilities codeLens) {
		this.codeLens = codeLens;
	}

	/**
	 * Returns true if the client supports actionable notifications and false otherwise
	 *
	 * See {@link org.eclipse.lemminx.customservice.ActionableNotification} and
	 * {@link org.eclipse.lemminx.customservice.XMLLanguageClientAPI#actionableNotification}
	 *
	 * @return true if the client supports actionable notifications and false otherwise
	 */
	public boolean isActionableNotificationSupport() {
		return actionableNotificationSupport;
	}

	/**
	 * Sets the actionableNotificationSupport boolean
	 *
	 * @param actionableNotificationSupport
	 */
	public void setActionableNotificationSupport(boolean actionableNotificationSupport) {
		this.actionableNotificationSupport = actionableNotificationSupport;
	}

		/**
	 * Sets the boolean permitting language server to exit on client
	 * shutdown() request, without waiting for client to call exit()
	 *
	 * @param shouldLanguageServerExitOnShutdown
	 */
	public void setShouldLanguageServerExitOnShutdown(boolean shouldLanguageServerExitOnShutdown) {
		this.shouldLanguageServerExitOnShutdown = shouldLanguageServerExitOnShutdown;
	}

	/**
	 * Returns true if the client should exit on shutdown() request and
	 * avoid waiting for an exit() request
	 *
	 * @return true if the language server should exit on shutdown() request
	 */
	public boolean shouldLanguageServerExitOnShutdown() {
		return shouldLanguageServerExitOnShutdown;
	}

}
