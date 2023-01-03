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

import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.extensions.references.XMLReferencesPlugin;
import org.eclipse.lemminx.extensions.references.settings.XMLReferenceExpression;
import org.eclipse.lemminx.extensions.references.utils.XMLReferencesSearchContext;
import org.eclipse.lemminx.extensions.references.utils.XMLReferencesUtils;
import org.eclipse.lemminx.services.extensions.completion.CompletionParticipantAdapter;
import org.eclipse.lemminx.services.extensions.completion.ICompletionRequest;
import org.eclipse.lemminx.services.extensions.completion.ICompletionResponse;
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
		DOMNode fromNode = request.getNode();
		if (fromNode.isElement()) {
			fromNode = ((DOMElement) fromNode).findTextAt(request.getOffset());
		}
		searchToNodes(fromNode, request, response);
	}

	@Override
	public void onAttributeValue(String valuePrefix, ICompletionRequest request, ICompletionResponse response,
			CancelChecker cancelChecker) throws Exception {
		DOMNode node = request.getNode();
		DOMNode fromNode = node.findAttrAt(request.getOffset());
		searchToNodes(fromNode, request, response);
	}

	private void searchToNodes(DOMNode fromNode, ICompletionRequest request, ICompletionResponse response) {
		XMLReferencesSearchContext searchContext = XMLReferencesUtils.findExpressionsWhichMatchFrom(fromNode,
				plugin.getReferencesSettings());
		if (searchContext != null) {
			XMLReferencesUtils.searchToNodes(fromNode, searchContext, false, true,
					(toNamespacePrefix, toNode, expression) -> {
						CompletionItem item = new CompletionItem();
						String value = createReferenceValue(toNode, toNamespacePrefix, expression);
						String insertText = request.getInsertAttrValue(value);
						item.setLabel(value);
						item.setKind(CompletionItemKind.Value);
						item.setFilterText(insertText);
						Range fullRange = request.getReplaceRange();
						item.setTextEdit(Either.forLeft(new TextEdit(fullRange, insertText)));
						response.addCompletionItem(item);
					});
		}
	}

	private static String createReferenceValue(DOMNode toNode, String toNamespacePrefix,
			XMLReferenceExpression expression) {
		StringBuilder value = new StringBuilder();
		if (expression.getPrefix() != null) {
			value.append(expression.getPrefix());
		}
		if (toNamespacePrefix != null) {
			value.append(toNamespacePrefix);
			value.append(":");
		}
		value.append(XMLReferencesUtils.getNodeValue(toNode));
		return value.toString();
	}

}
