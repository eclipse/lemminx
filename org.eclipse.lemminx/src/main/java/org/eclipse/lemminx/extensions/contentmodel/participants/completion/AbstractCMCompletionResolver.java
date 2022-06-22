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
package org.eclipse.lemminx.extensions.contentmodel.participants.completion;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.completion.ICompletionItemResolveParticipant;
import org.eclipse.lemminx.services.extensions.completion.ICompletionItemResolverRequest;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Represents a completion item resolver that resolves the completion item using
 * information from the content model
 */
public abstract class AbstractCMCompletionResolver implements ICompletionItemResolveParticipant {

	public static final String OFFSET_KEY = "OFFSET";

	@Override
	public CompletionItem resolveCompletionItem(ICompletionItemResolverRequest request, CancelChecker cancelChecker) {
		CompletionItem toResolve = request.getUnresolved();

		DOMDocument document = request.getDocument();

		Integer offset = request.getDataPropertyAsInt(OFFSET_KEY);
		if (document == null || offset == null) {
			return toResolve;
		}

		DOMAttr attr = document.findAttrAt(offset);

		DOMNode parentNode = document.findNodeAt(offset);
		if (parentNode == null || !parentNode.isElement()) {
			return toResolve;
		}
		DOMElement parentElement = (DOMElement) parentNode;

		addDocumentationToCompletion(request, toResolve, parentElement, attr);
		return toResolve;
	}

	/**
	 * Add the documentation from the content model to the given completion request.
	 *
	 * @param request       the completion resolve request
	 * @param toResolve     the unresolved completion item to resolve
	 * @param parentElement the parent element to where completion was opened on
	 * @param attr          the attribute that completion was opened on, may be null
	 */
	protected abstract void addDocumentationToCompletion(ICompletionItemResolverRequest request,
			CompletionItem toResolve,
			DOMElement parentElement, DOMAttr attr);

}
