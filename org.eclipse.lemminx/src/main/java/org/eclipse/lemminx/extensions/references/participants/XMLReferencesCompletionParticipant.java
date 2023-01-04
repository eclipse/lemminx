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
package org.eclipse.lemminx.extensions.references.participants;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.lemminx.extensions.references.XMLReferencesPlugin;
import org.eclipse.lemminx.extensions.references.search.SearchEngine;
import org.eclipse.lemminx.extensions.references.search.SearchQuery;
import org.eclipse.lemminx.extensions.references.search.SearchQueryFactory;
import org.eclipse.lemminx.services.extensions.completion.CompletionParticipantAdapter;
import org.eclipse.lemminx.services.extensions.completion.ICompletionRequest;
import org.eclipse.lemminx.services.extensions.completion.ICompletionResponse;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * XML references completion participant
 * 
 * @author Angelo ZERR
 *
 */
public class XMLReferencesCompletionParticipant extends CompletionParticipantAdapter {

	private final XMLReferencesPlugin plugin;

	public XMLReferencesCompletionParticipant(XMLReferencesPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void onXMLContent(ICompletionRequest request, ICompletionResponse response, CancelChecker cancelChecker)
			throws Exception {
		searchToNodes(request, response, cancelChecker);
	}

	@Override
	public void onAttributeValue(String valuePrefix, ICompletionRequest request, ICompletionResponse response,
			CancelChecker cancelChecker) throws Exception {
		searchToNodes(request, response, cancelChecker);
	}

	private void searchToNodes(ICompletionRequest request, ICompletionResponse response,
			CancelChecker cancelChecker) {
		// Create the from query for the node which needs to perform the search.
		SearchQuery query = SearchQueryFactory.createFromQuery(request.getNode(), request.getOffset(),
				plugin.getReferencesSettings());
		if (query == null) {
			// The query cannot be created because:
			// - the node is neither a text nor an attribute
			// - it doesn't exists some expressions for the DOM document of the node.
			// - there are none expressions which matches the node.
			return;
		}
		query.setMatchNode(false);
		query.setSearchInIncludedFiles(true);

		AtomicReference<Range> replaceRange = new AtomicReference<>(null);
		SearchEngine.getInstance().search(query,
				(fromSearchNode, toSearchNode, expression) -> {
					CompletionItem item = new CompletionItem();
					String value = toSearchNode.getValue(fromSearchNode.getPrefix());
					String insertText = request.getInsertAttrValue(value);
					item.setLabel(value);
					item.setKind(CompletionItemKind.Value);
					item.setFilterText(insertText);
					Range fullRange = replaceRange.get();
					if (fullRange == null) {
						replaceRange.set(XMLPositionUtility.createRange(fromSearchNode));
						fullRange = replaceRange.get();
					}
					item.setTextEdit(Either.forLeft(new TextEdit(fullRange, insertText)));
					response.addCompletionItem(item);
				}, cancelChecker);
	}

}
