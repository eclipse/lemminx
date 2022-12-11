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
package org.eclipse.lemminx.extensions.relaxng.grammar.rng;

import java.util.List;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.extensions.relaxng.utils.RelaxNGUtils;
import org.eclipse.lemminx.services.extensions.AbstractReferenceParticipant;
import org.eclipse.lemminx.utils.DOMUtils;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * RelaxNG rng reference
 * 
 * @author Angelo ZERR
 *
 */
public class RNGReferenceParticipant extends AbstractReferenceParticipant {

	@Override
	protected boolean match(DOMDocument document) {
		return DOMUtils.isRelaxNGXMLSyntax(document);
	}

	@Override
	protected void findReferences(DOMNode node, Position position, int offset, ReferenceContext context,
			List<Location> locations, CancelChecker cancelChecker) {
		DOMAttr attr = node.findAttrAt(offset);
		if (attr != null) {
			node = attr;
		}
		RelaxNGUtils.searchRNGOriginAttributes(node,
				(origin, target) -> locations.add(XMLPositionUtility.createLocation(origin.getNodeAttrValue())),
				cancelChecker);
	}

}
