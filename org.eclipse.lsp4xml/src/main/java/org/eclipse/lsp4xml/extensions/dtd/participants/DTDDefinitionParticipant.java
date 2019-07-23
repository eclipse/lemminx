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
import org.eclipse.lsp4xml.dom.DTDAttlistDecl;
import org.eclipse.lsp4xml.dom.DTDDeclParameter;
import org.eclipse.lsp4xml.dom.DTDElementDecl;
import org.eclipse.lsp4xml.extensions.dtd.utils.DTDUtils;
import org.eclipse.lsp4xml.services.extensions.AbstractDefinitionParticipant;
import org.eclipse.lsp4xml.services.extensions.IDefinitionRequest;
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
		return true;
	}

	@Override
	protected void doFindDefinition(IDefinitionRequest request, List<LocationLink> locations,
			CancelChecker cancelChecker) {
		DOMNode node = request.getNode();
		int offset = request.getOffset();
		DTDDeclParameter originName = getOriginName(node, offset);
		if (originName != null) {
			DTDUtils.searchDTDTargetElementDecl(originName, true, targetElementName -> {
				LocationLink location = XMLPositionUtility.createLocationLink(originName, targetElementName);
				locations.add(location);
			});
		}
	}

	private DTDDeclParameter getOriginName(DOMNode node, int offset) {
		switch (node.getNodeType()) {
		case DOMNode.DTD_ATT_LIST_NODE:
			DTDAttlistDecl attlist = (DTDAttlistDecl) node;
			if (attlist.isInElementName(offset)) {
				return attlist.getElementNameNode();
			}
			return null;
		case DOMNode.DTD_ELEMENT_DECL_NODE:
			DTDElementDecl elementDecl = (DTDElementDecl) node;
			return elementDecl.getParameterAt(offset);
		}
		return null;
	}

}
