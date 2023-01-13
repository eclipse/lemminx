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

import java.util.List;

import org.eclipse.lemminx.extensions.references.XMLReferencesPlugin;
import org.eclipse.lemminx.extensions.references.search.SearchEngine;
import org.eclipse.lemminx.extensions.references.search.SearchQuery;
import org.eclipse.lemminx.extensions.references.search.SearchQueryFactory;
import org.eclipse.lemminx.services.extensions.ILinkedEditingRangesParticipant;
import org.eclipse.lemminx.services.extensions.ILinkedEditingRangesRequest;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * XML references linked editing ranges.
 * 
 * @author Angelo ZERR
 *
 */
public class XMLReferencesLinkedEditingRangesParticipant implements ILinkedEditingRangesParticipant {

	private final XMLReferencesPlugin plugin;

	public XMLReferencesLinkedEditingRangesParticipant(XMLReferencesPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void findLinkedEditingRanges(ILinkedEditingRangesRequest request, List<Range> ranges,
			CancelChecker cancelChecker) {
		if (request.getNode() == null || request.getNode().isOwnerDocument()) {
			// Linked editing range should work only for attribute or text node
			return;
		}
		
		SearchQuery query = SearchQueryFactory.createToQuery(request.getNode(), request.getOffset(),
				plugin.getReferencesSettings());
		if (query == null) {
			// The query cannot be created because:
			// - the node is neither a text nor an attribute
			// - it doesn't exists some expressions for the DOM document of the node.
			// - there are none expressions which matches the node.
			return;
		}
		query.setMatchNode(true);
		query.setSearchInIncludedFiles(false);

		int previousSize = ranges.size();
		SearchEngine.getInstance().search(query,
				(fromSearchNode, toSearchNode, expression) -> {
					ranges.add(fromSearchNode.createRange(true));
				},
				cancelChecker);
		if (ranges.size() == previousSize) {
			// There are no referenced nodes, ignore the linked editing range
			return;
		}
		
		// Insert at first, the text edit for the node which was updated
		ranges.add(query.getSearchNode().createRange(true));
	}

}
