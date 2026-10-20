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
package org.eclipse.lemminx.settings;

import org.eclipse.lsp4j.WorkspaceClientCapabilities;

/**
 * A wrapper around LSP {@link WorkspaceClientCapabilities}.
 *
 */
public class XMLWorkspaceSettings {

	private WorkspaceClientCapabilities workspace;

	public void setCapabilities(WorkspaceClientCapabilities workspace) {
		this.workspace = workspace;
	}

	public WorkspaceClientCapabilities getCapabilities() {
		return workspace;
	}

	/**
	 * Returns true if the client can support the given resource operation kind and
	 * false otherwise.
	 * 
	 * @param kind the resource operation kind.
	 * @return true if the client can support the given resource operation kind and
	 *         false otherwise.
	 */
	public boolean isResourceOperationSupported(String kind) {
		return workspace != null && workspace.getWorkspaceEdit() != null
				&& workspace.getWorkspaceEdit().getResourceOperations() != null
				&& workspace.getWorkspaceEdit().getResourceOperations().contains(kind);
	}

	/**
	 * Returns true if the client supports LSP 3.18 SnippetTextEdit in workspace
	 * edits (capability {@code workspace.workspaceEdit.snippetEditSupport}) and
	 * false otherwise.
	 *
	 * <p>
	 * When supported, code actions can use snippet tabstops ({@code $1},
	 * {@code $0}) to position the cursor after applying the edit, instead of
	 * leaving it at the end of the inserted text.
	 * </p>
	 *
	 * @return true if the client supports SnippetTextEdit and false otherwise.
	 */
	public boolean isSnippetEditSupported() {
		return workspace != null && workspace.getWorkspaceEdit() != null
				&& workspace.getWorkspaceEdit().getSnippetEditSupport() != null
				&& workspace.getWorkspaceEdit().getSnippetEditSupport();
	}

}