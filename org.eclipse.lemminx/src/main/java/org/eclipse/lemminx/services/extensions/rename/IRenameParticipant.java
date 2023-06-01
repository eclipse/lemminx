/*******************************************************************************
* Copyright (c) 2019, 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services.extensions.rename;

import java.util.List;
import java.util.concurrent.CancellationException;

import org.eclipse.lsp4j.PrepareRenameResult;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * Rename participant API.
 *
 */
public interface IRenameParticipant {

	/**
	 * Creates the list of document changes for the rename operation.
	 * 
	 * @param request A rename request
	 * @param documentChanges A list to collect either text document edits or rename operations
	 * @param cancelChecker Cancel checker
	 * @throws CancellationException if the computation was cancelled
	 * @since 0.26
	 */
	void doRename(IRenameRequest request, IRenameResponse renameResponse, CancelChecker cancelChecker) throws CancellationException;
	
	/**
	 * Checks if rename operation can be executed for a given prepare rename request
	 * 
	 * @param request A prepare rename request
	 * @param cancelChecker Cancel checker
	 * @throws CancellationException if the computation was cancelled
	 * @return Either range or rename operation result of prepare rename operation
	 */
	Either<Range, PrepareRenameResult> prepareRename(IPrepareRenameRequest request, CancelChecker cancelChecker) throws CancellationException;
}