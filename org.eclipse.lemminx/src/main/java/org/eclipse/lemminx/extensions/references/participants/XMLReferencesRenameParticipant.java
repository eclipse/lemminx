/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.extensions.references.XMLReferencesPlugin;
import org.eclipse.lemminx.extensions.references.search.SearchEngine;
import org.eclipse.lemminx.extensions.references.search.SearchNode;
import org.eclipse.lemminx.extensions.references.search.SearchQuery;
import org.eclipse.lemminx.extensions.references.search.SearchQueryFactory;
import org.eclipse.lemminx.services.extensions.IPrepareRenameRequest;
import org.eclipse.lemminx.services.extensions.IRenameParticipant;
import org.eclipse.lemminx.services.extensions.IRenameRequest;
import org.eclipse.lsp4j.PrepareRenameResult;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * XML references rename support.
 * 
 */
public class XMLReferencesRenameParticipant implements IRenameParticipant {

	private final XMLReferencesPlugin plugin;

	public XMLReferencesRenameParticipant(XMLReferencesPlugin plugin) {
		this.plugin = plugin;
	}

	// --------------- Prepare rename

	@Override
	public Either<Range, PrepareRenameResult> prepareRename(IPrepareRenameRequest request,
			CancelChecker cancelChecker) {
		SearchQuery query = SearchQueryFactory.createQuery(request.getNode(), request.getOffset(),
				plugin.getReferencesSettings());
		if (query == null) {
			// The query cannot be created because:
			// - the node is neither a text nor an attribute
			// - it doesn't exists some expressions for the DOM document of the node.
			// - there are none expressions which matches the node.
			return null;
		}
		SearchNode searchNode = query.getSearchNode();
		if (searchNode == null) {
			return null;
		}
		DOMNode node = searchNode.getNode();
		if (node == null || node.isOwnerDocument()) {
			// node is a DOM document in the case of <foo></foo>|
			return null;
		}
		if (!searchNode.isValid()) {
			// The search node is not valid.
			return null;
		}

		Range range = searchNode.createRange(true);
		String placeholder = createPlaceHolder(searchNode);
		return Either.forRight(new PrepareRenameResult(range, placeholder));
	}

	private static String createPlaceHolder(SearchNode searchNode) {
		String placeholder = searchNode.getValue(null);
		if (searchNode.isNeedToAjdustWithPrefix()) {
			// In case of 'from' node, we need to remove the prefix
			String prefix = searchNode.getPrefix();
			return placeholder.substring(prefix.length(), placeholder.length());
		}
		return placeholder;
	}

	// --------------- Rename

	@Override
	public void doRename(IRenameRequest request, List<TextEdit> edits, CancelChecker cancelChecker) {
		edits.addAll(getRenameTextEdits(request, cancelChecker));
	}

	private List<TextEdit> getRenameTextEdits(IRenameRequest request, CancelChecker cancelChecker) {
		SearchQuery query = SearchQueryFactory.createToQueryByRetrievingToBefore(request.getNode(), request.getOffset(),
				plugin.getReferencesSettings(), cancelChecker);
		if (query == null) {
			// The query cannot be created because:
			// - the node is neither a text nor an attribute
			// - it doesn't exists some expressions for the DOM document of the node.
			// - there are none expressions which matches the node.
			return Collections.emptyList();
		}
		query.setMatchNode(true);
		query.setSearchInIncludedFiles(true);

		List<TextEdit> textEdits = new ArrayList<>();
		String newText = request.getNewText();
		SearchEngine.getInstance().search(query,
				(fromSearchNode, toSearchNode, expression) -> {
					Range range = fromSearchNode.createRange(true);
					TextEdit textEdit = new TextEdit(range, newText);
					textEdits.add(textEdit);
				},
				cancelChecker);
		// Insert at first, the text edit for the node which was updated
		Range range = query.getSearchNode().createRange(true);
		textEdits.add(0, new TextEdit(range, newText));
		return textEdits;
	}

}