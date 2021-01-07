/**
 *  Copyright (c) 2021 Red Hat Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.lemminx.services.extensions;

import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams;

public interface IWorkspaceServiceParticipant {

	public void didChangeWorkspaceFolders(DidChangeWorkspaceFoldersParams params);
}
