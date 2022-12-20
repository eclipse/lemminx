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
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.extensions.references.XMLReferencesPlugin;
import org.eclipse.lemminx.extensions.references.settings.XMLReferenceExpression;
import org.eclipse.lemminx.extensions.references.utils.XMLReferencesUtils;
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
		DOMNode node = request.getNode();
		if (!node.isAttribute()) {
			return;
		}
		DOMAttr attr = (DOMAttr) node;
		List<XMLReferenceExpression> references = XMLReferencesUtils.findExpressionsWhichMatcheFrom(attr,
				plugin.getReferencesSettings());
		if (references != null && !references.isEmpty()) {
			XMLReferencesUtils.searchToAttributes(attr, references, true, true,
					(targetNamespacePrefix, targetAttr, expression) -> {
						LocationLink location = XMLPositionUtility.createLocationLink(attr.getNodeAttrValue(),
								targetAttr.getNodeAttrValue());
						locations.add(location);
					});
		}
	}

}
