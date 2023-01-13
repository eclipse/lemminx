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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.lemminx.client.CodeLensKind;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.extensions.references.XMLReferencesPlugin;
import org.eclipse.lemminx.extensions.references.search.ReferenceLink;
import org.eclipse.lemminx.extensions.references.search.SearchEngine;
import org.eclipse.lemminx.extensions.references.search.SearchNode;
import org.eclipse.lemminx.services.extensions.codelens.ICodeLensParticipant;
import org.eclipse.lemminx.services.extensions.codelens.ICodeLensRequest;
import org.eclipse.lemminx.services.extensions.codelens.ReferenceCommand;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * XML references codelens support.
 *
 */
public class XMLReferencesCodeLensParticipant implements ICodeLensParticipant {

	private final XMLReferencesPlugin plugin;

	public XMLReferencesCodeLensParticipant(XMLReferencesPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void doCodeLens(ICodeLensRequest request, List<CodeLens> lenses, CancelChecker cancelChecker) {
		DOMDocument document = request.getDocument();
		Collection<ReferenceLink> links = SearchEngine.getInstance().searchLinks(document,
				plugin.getReferencesSettings(),
				cancelChecker);
		if (links.isEmpty()) {
			return;
		}
		boolean supportedByClient = request.isSupportedByClient(CodeLensKind.References);
		Map<DOMElement, CodeLens> cache = new HashMap<>();
		for (ReferenceLink link : links) {
			for (SearchNode to : link.getTos()) {
				if (document != to.getOwnerDocument()) {
					// The 'to' search node belongs to an included document, ignore it.
					continue;
				}
				// Increment references count Codelens for the given target element
				DOMNode toNode = to.getNode();
				DOMElement toElement = toNode.isAttribute() ? ((DOMAttr) toNode).getOwnerElement()
						: toNode.getParentElement();
				if (toElement != null) {
					for (SearchNode from : link.getFroms()) {
						if (from.matchesValue(to)) {

							CodeLens codeLens = cache.get(toElement);
							if (codeLens == null) {
								Range range = XMLPositionUtility.createRange(toNode);
								codeLens = new CodeLens(range);
								codeLens.setCommand(
										new ReferenceCommand(document.getDocumentURI(), range.getStart(),
												supportedByClient));
								cache.put(toElement, codeLens);
								lenses.add(codeLens);
							} else {
								((ReferenceCommand) codeLens.getCommand()).increment();
							}

						}
					}
				}
			}
		}
	}

}
