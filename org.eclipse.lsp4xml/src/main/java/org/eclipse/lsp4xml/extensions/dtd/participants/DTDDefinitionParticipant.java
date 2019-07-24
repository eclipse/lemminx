/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.extensions.dtd.participants;

import java.util.List;

import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.dom.DTDDeclNode;
import org.eclipse.lsp4xml.dom.DTDDeclParameter;
import org.eclipse.lsp4xml.extensions.dtd.utils.DTDUtils;
import org.eclipse.lsp4xml.services.extensions.AbstractDefinitionParticipant;
import org.eclipse.lsp4xml.services.extensions.IDefinitionRequest;
import org.eclipse.lsp4xml.utils.DOMUtils;
import org.eclipse.lsp4xml.utils.XMLPositionUtility;

/**
 * DTD definition participant
 * 
 * @author Angelo ZERR
 *
 */
public class DTDDefinitionParticipant extends AbstractDefinitionParticipant {

	@Override
	protected boolean match(DOMDocument document) {
		// Not applicable for XML Schema
		return !DOMUtils.isXSD(document);
	}

	@Override
	protected void doFindDefinition(IDefinitionRequest request, List<LocationLink> locations,
			CancelChecker cancelChecker) {
		DOMNode node = request.getNode();
		int offset = request.getOffset();
		// DTD definition is applicable only for <!ELEMENT and <!ATTLIST
		if (!(node.isDTDElementDecl() || node.isDTDAttListDecl())) {
			return;
		}
		// Get the parameter which defines the name which references an <!ELEMENT
		// - <!ATTLIST elt -> we search the 'elt' in <!ELEMENT elt
		// - <!ELEMENT elt (child1 -> we search the 'child1' in <!ELEMENT child1
		DTDDeclParameter originName = ((DTDDeclNode) node).getReferencedElementNameAt(offset);
		if (originName != null) {
			DTDUtils.searchDTDTargetElementDecl(originName, true, targetElementName -> {
				LocationLink location = XMLPositionUtility.createLocationLink(originName, targetElementName);
				locations.add(location);
			});
		}
	}

}
