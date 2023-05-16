/**
 *  Copyright (c) 2018, 2023 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.services.extensions.codeaction;

import java.util.List;
import java.util.concurrent.CancellationException;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Code action participant API.
 * 
 * @author Angelo ZERR
 *
 */
public interface ICodeActionParticipant {

	/**
	 * Collect the code action in the given <code>codeActions</code> for the given
	 * code action request <code>request</code>.
	 * 
	 * @param request       the code action request.
	 * @param codeActions   list of code actions to fill.
	 * @param cancelChecker the cancel checker.
	 * @throws CancellationException if the computation was cancelled
	 */
	default void doCodeAction(ICodeActionRequest request, List<CodeAction> codeActions, CancelChecker cancelChecker)
			throws CancellationException {
	}

	/**
	 * Collect the code action in the given <code>codeActions</code> for the given
	 * code action request <code>request</code> independently of diagnostic provided.
	 * 
	 * @param request       the code action request.
	 * @param codeActions   list of code actions to fill.
	 * @param cancelChecker the cancel checker.
	 * @throws CancellationException if the computation was cancelled
	 * 
	 * @since 0.26
	 */
	default void doCodeActionUnconditional(ICodeActionRequest request, List<CodeAction> codeActions,
			CancelChecker cancelChecker) throws CancellationException {
	}

	/**
	 * Returns the codeAction resolver participant identified by the given
	 * <code>participantId</code> and null otherwise.
	 * 
	 * @param participantId the code action resolver participant ID.
	 * 
	 * @return the codeAction resolver participant identified by the given
	 *         <code>participantId</code> and null otherwise.
	 */
	default ICodeActionResolvesParticipant getResolveCodeActionParticipant(String participantId) {
		return null;
	}
}
