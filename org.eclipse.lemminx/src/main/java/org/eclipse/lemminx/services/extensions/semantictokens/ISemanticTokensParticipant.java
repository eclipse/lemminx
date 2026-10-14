/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services.extensions.semantictokens;

import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Semantic tokens participant API.
 */
public interface ISemanticTokensParticipant {

	/**
	 * Fill the given <code>builder</code> with semantic tokens for the given
	 * semantic tokens request.
	 *
	 * @param request       the semantic tokens request.
	 * @param builder       the semantic tokens data builder.
	 * @param cancelChecker the cancel checker.
	 */
	void findSemanticTokens(ISemanticTokensRequest request, SemanticTokensData builder, CancelChecker cancelChecker);
}
