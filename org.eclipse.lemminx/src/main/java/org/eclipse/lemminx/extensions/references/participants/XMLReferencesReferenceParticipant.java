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

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.extensions.references.XMLReferencesPlugin;
import org.eclipse.lemminx.extensions.references.search.SearchEngine;
import org.eclipse.lemminx.extensions.references.search.SearchQuery;
import org.eclipse.lemminx.extensions.references.search.SearchQueryFactory;
import org.eclipse.lemminx.services.extensions.AbstractReferenceParticipant;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * XML references reference support.
 *
 */
public class XMLReferencesReferenceParticipant extends AbstractReferenceParticipant {

	private final XMLReferencesPlugin plugin;

	public XMLReferencesReferenceParticipant(XMLReferencesPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	protected boolean match(DOMDocument document) {
		return true;
	}

	@Override
	protected void findReferences(DOMNode node, Position position, int offset, ReferenceContext context,
			List<Location> locations, CancelChecker cancelChecker) {
		SearchQuery query = SearchQueryFactory.createToQuery(node, offset, plugin.getReferencesSettings());
		if (query == null) {
			// The query cannot be created because:
			// - the node is neither a text nor an attribute
			// - it doesn't exists some expressions for the DOM document of the node.
			// - there are none expressions which matches the node.
			return;
		}
		query.setMatchNode(true);
		query.setSearchInIncludedFiles(true);

		SearchEngine.getInstance().search(query,
				(fromSearchNode, toSearchNode, expression) -> locations
						.add(XMLPositionUtility.createLocation(fromSearchNode)),
				cancelChecker);
	}

}
