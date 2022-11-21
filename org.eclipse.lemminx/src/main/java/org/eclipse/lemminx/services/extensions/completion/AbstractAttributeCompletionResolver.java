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

import org.eclipse.lemminx.dom.DOMElement;
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
 * 
 * for a given DOM attribute.
 *
 */
public abstract class AbstractAttributeCompletionResolver extends AbstractCompletionResolver {

	@Override
	protected void resolveCompletionItem(DOMNode node, CompletionItem toResolve, ICompletionItemResolverRequest request,
			CancelChecker cancelChecker) {
		if (!node.isElement()) {
			return;
		}
		DOMElement element = (DOMElement) node;
		resolveCompletionItem(element, toResolve, request, cancelChecker);
	}

	/**
	 * Resolve the completion item from the content model to the given completion
	 * request.
	 *
	 * @param request       the completion resolve request
	 * @param toResolve     the unresolved completion item to resolve
	 * @param parentElement the parent element to where completion was opened on
	 * @param attr          the attribute that completion was opened on, may be null
	 */
	protected abstract void resolveCompletionItem(DOMElement element, CompletionItem toResolve,
			ICompletionItemResolverRequest request, CancelChecker cancelChecker);
}
