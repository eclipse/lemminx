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

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.extensions.references.XMLReferencesPlugin;
import org.eclipse.lemminx.extensions.references.settings.XMLReferenceExpression;
import org.eclipse.lemminx.extensions.references.utils.XMLReferencesUtils;
import org.eclipse.lemminx.services.extensions.IHighlightingParticipant;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * XML references highlight participant
 * 
 * @author Angelo ZERR
 *
 */
public class XMLReferencesHighlightingParticipant implements IHighlightingParticipant {

	private final XMLReferencesPlugin plugin;

	public XMLReferencesHighlightingParticipant(XMLReferencesPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void findDocumentHighlights(DOMNode node, Position position, int offset, List<DocumentHighlight> highlights,
			CancelChecker cancelChecker) {
		// Highlight works only when attribute is selected (origin or target attribute)
		DOMAttr fromAttr = node.findAttrAt(offset);
		if (fromAttr == null || fromAttr.getNodeAttrValue() == null) {
			return;
		}

		List<XMLReferenceExpression> references = XMLReferencesUtils.findExpressionsWhichMatcheFrom(fromAttr,
				plugin.getReferencesSettings());
		if (references != null && !references.isEmpty()) {
			highlights
					.add(new DocumentHighlight(XMLPositionUtility.createRange(fromAttr.getNodeAttrValue().getStart(),
							fromAttr.getNodeAttrValue().getEnd(), fromAttr.getOwnerDocument()),
							DocumentHighlightKind.Read));
			XMLReferencesUtils.searchToAttributes(fromAttr, references, true, false,
					(targetNamespacePrefix, toAttr, expression) -> {
						highlights.add(new DocumentHighlight(
								XMLPositionUtility.createRange(toAttr.getNodeAttrValue().getStart(),
										toAttr.getNodeAttrValue().getEnd(), toAttr.getOwnerDocument()),
								DocumentHighlightKind.Write));
					});
		}
	}

}
