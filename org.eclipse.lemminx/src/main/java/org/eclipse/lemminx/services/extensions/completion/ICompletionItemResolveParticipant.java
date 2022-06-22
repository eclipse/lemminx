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
package org.eclipse.lemminx.services.extensions.completion;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Represents a class that can resolve certain kinds of completion items.
 */
public interface ICompletionItemResolveParticipant {

	/**
	 * Returns the completion item with the remaining fields resolved.
	 *
	 * @param request       the completion item resolve request
	 * @param cancelChecker the cancel checker
	 * @return the completion item with the remaining fields resolved
	 */
	CompletionItem resolveCompletionItem(ICompletionItemResolverRequest request, CancelChecker cancelChecker);

}
