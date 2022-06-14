/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services.extensions.codeaction;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * The code action resolver participant API.
 * 
 * @author Angelo ZERR
 *
 */
public interface ICodeActionResolvesParticipant {

	/**
	 * Returns the resolved codeAction coming from the unresolved codeAction of the
	 * given <code>request</code>.
	 * 
	 * @param request       the resolve code action request.
	 * @param cancelChecker the cancel checker.
	 * 
	 * @return the resolved codeAction coming from the unresolved codeAction of the
	 *         given <code>request</code>.
	 */
	CodeAction resolveCodeAction(ICodeActionResolverRequest request, CancelChecker cancelChecker);

}
