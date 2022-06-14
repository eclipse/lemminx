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
// Taken from https://github.com/eclipse/lsp4mp/blob/master/microprofile.ls/org.eclipse.lsp4mp.ls/src/main/java/org/eclipse/lsp4mp/ls/commons/client/CommandCapabilities.java
package org.eclipse.lemminx.client;

import org.eclipse.lsp4j.DynamicRegistrationCapabilities;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * Extended capabilities for client commands.
 *
 * @author Angelo ZERR
 */
@SuppressWarnings("all")
public class CommandCapabilities extends DynamicRegistrationCapabilities {
	/**
	 * Specific capabilities for the `ClientCommands`
	 */
	private CommandKindCapabilities clientCommands;

	public CommandCapabilities() {
	}

	public CommandCapabilities(final Boolean dynamicRegistration) {
		super(dynamicRegistration);
	}

	public CommandCapabilities(final CommandKindCapabilities clientCommands) {
		this.clientCommands = clientCommands;
	}

	public CommandCapabilities(final CommandKindCapabilities clientCommands, final Boolean dynamicRegistration) {
		super(dynamicRegistration);
		this.clientCommands = clientCommands;
	}

	public boolean isSupported(String command) {
		return clientCommands.getValueSet().contains(command);
	}

	/**
	 * Specific capabilities for the `ClientCommand` in the `textDocument/commands`
	 * request.
	 */
	@Pure
	public CommandKindCapabilities getClientCommand() {
		return this.clientCommands;
	}

	/**
	 * Specific capabilities for the `ClientCommand` in the `textDocument/commands`
	 * request.
	 */
	public void setClientCommand(final CommandKindCapabilities clientCommands) {
		this.clientCommands = clientCommands;
	}

	@Override
	@Pure
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("commandsKind", this.clientCommands);
		b.add("dynamicRegistration", getDynamicRegistration());
		return b.toString();
	}

	@Override
	@Pure
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		if (!super.equals(obj))
			return false;
		CommandCapabilities other = (CommandCapabilities) obj;
		if (this.clientCommands == null) {
			if (other.clientCommands != null)
				return false;
		} else if (!this.clientCommands.equals(other.clientCommands))
			return false;
		return true;
	}

	@Override
	@Pure
	public int hashCode() {
		return 31 * super.hashCode() + ((this.clientCommands == null) ? 0 : this.clientCommands.hashCode());
	}
}