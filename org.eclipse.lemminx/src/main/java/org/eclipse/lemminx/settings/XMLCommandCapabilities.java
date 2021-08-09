/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
// Taken from https://github.com/eclipse/lsp4mp/blob/master/microprofile.ls/org.eclipse.lsp4mp.ls/src/main/java/org/eclipse/lsp4mp/settings/MicroProfileCommandCapabilities.java
/**
 * A wrapper around Lemminx {@link CommandCapabilities}.
 */
package org.eclipse.lemminx.settings;

import org.eclipse.lemminx.client.CommandCapabilities;

public class XMLCommandCapabilities {
	private CommandCapabilities capabilities;

	public void setCapabilities(CommandCapabilities capabilities) {
		this.capabilities = capabilities;
	}

	public CommandCapabilities getCapabilities() {
		return capabilities;
	}

	/**
	 * Returns <code>true</code> if the client supports the <code>clientCommand</code>
	 * command. Otherwise, returns <code>false</code>.
	 *
	 * See {@link org.eclipse.lemminx.client.ClientCommands}
	 *
	 * @param clientCommand the client command to check for
	 * @return <code>true</code> if the client supports the <code>clientCommand</code>
	 *         clientCommand. Otherwise, returns <code>false</code>
	 */
	public boolean isCommandSupported(String clientCommand) {
		return capabilities != null && capabilities.isSupported(clientCommand);
	}
}