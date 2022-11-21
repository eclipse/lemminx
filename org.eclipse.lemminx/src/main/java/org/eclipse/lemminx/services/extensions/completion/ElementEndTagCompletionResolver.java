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

import static org.eclipse.lemminx.services.extensions.completion.AbstractElementCompletionItem.UPDATE_END_TAG_NAME_FIELD;
import static org.eclipse.lemminx.services.extensions.completion.AbstractElementCompletionItem.updateEndTagName;

import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Resolves the completion item of an element completion with an additional text
 * edits to update end tag.
 * 
 */
public class ElementEndTagCompletionResolver extends AbstractCompletionResolver {

	public static final String PARTICIPANT_ID = ElementEndTagCompletionResolver.class.getName();

	@Override
	protected void resolveCompletionItem(DOMNode node, CompletionItem toResolve, ICompletionItemResolverRequest request,
			CancelChecker cancelChecker) {
		resolveEndTagNameTextEdit(node, request, toResolve);
	}

	private static void resolveEndTagNameTextEdit(DOMNode node, ICompletionItemResolverRequest request,
			CompletionItem toResolve) {
		// Resolve aditionalTextEdits to update end tag name
		if (request.isResolveAdditionalTextEditsSupported()) {
			Boolean updateEndTagName = request.getDataPropertyAsBoolean(UPDATE_END_TAG_NAME_FIELD);
			if (updateEndTagName == null || !updateEndTagName) {
				return;
			}
			String tagName = toResolve.getLabel();
			updateEndTagName(node, request.getCompletionOffset(), request, tagName, toResolve);
		}
	}

}
