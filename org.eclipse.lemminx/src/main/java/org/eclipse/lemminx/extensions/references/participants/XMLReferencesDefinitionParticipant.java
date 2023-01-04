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

import java.util.List;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.references.XMLReferencesPlugin;
import org.eclipse.lemminx.extensions.references.search.SearchEngine;
import org.eclipse.lemminx.extensions.references.search.SearchQuery;
import org.eclipse.lemminx.extensions.references.search.SearchQueryFactory;
import org.eclipse.lemminx.services.extensions.AbstractDefinitionParticipant;
import org.eclipse.lemminx.services.extensions.IDefinitionRequest;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * XML references definition participant.
 * 
 * @author Angelo ZERR
 *
 */
public class XMLReferencesDefinitionParticipant extends AbstractDefinitionParticipant {

	private final XMLReferencesPlugin plugin;

	public XMLReferencesDefinitionParticipant(XMLReferencesPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	protected boolean match(DOMDocument document) {
		return true;
	}

	@Override
	protected void doFindDefinition(IDefinitionRequest request, List<LocationLink> locations,
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
		query.setMatchNode(true);
		query.setSearchInIncludedFiles(true);

		SearchEngine.getInstance().search(query,
				(fromSearchNode, toSearchNode, expression) -> {
					LocationLink location = XMLPositionUtility.createLocationLink(
							XMLPositionUtility.createRange(fromSearchNode),
							toSearchNode);
					locations.add(location);
				}, cancelChecker);
	}

}
