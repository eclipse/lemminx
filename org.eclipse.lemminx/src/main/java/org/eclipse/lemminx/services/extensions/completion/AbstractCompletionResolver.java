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

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Abstract class completion resolver to resolve :
 * 
 * <ul>
 * <li>{@link CompletionItem#setDocumentation(String)}</li>
 * <li>{@link CompletionItem#setAdditionalTextEdits(java.util.List)}</li>
 * <li>{@link CompletionItem#setDetail(String)}</li>
 * </ul>
 */
public abstract class AbstractCompletionResolver implements ICompletionItemResolveParticipant {

	@Override
	public CompletionItem resolveCompletionItem(ICompletionItemResolverRequest request, CancelChecker cancelChecker) {
		CompletionItem toResolve = request.getUnresolved();
		DOMDocument document = request.getDocument();
		Integer offset = request.getCompletionOffset();
		if (document == null || offset == null) {
			return toResolve;
		}
		DOMNode node = document.findNodeAt(offset);
		if (node == null) {
			return toResolve;
		}
		resolveCompletionItem(node, toResolve, request, cancelChecker);
		return toResolve;
	}

	/**
	 * Resolve the given completion item <code>toResolve</code> by using the given
	 * DOM node.
	 * 
	 * @param node          the DOM retrieved by the offset coming from the
	 *                      completion item data.
	 * @param toResolve     the completion item to resolve.
	 * @param request       the completion request.
	 * @param cancelChecker the cancel checker.
	 */
	protected abstract void resolveCompletionItem(DOMNode node, CompletionItem toResolve,
			ICompletionItemResolverRequest request,
			CancelChecker cancelChecker);
}
