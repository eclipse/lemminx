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

import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.dom.DTDAttlistDecl;
import org.eclipse.lsp4xml.dom.DTDDeclParameter;
import org.eclipse.lsp4xml.dom.DTDElementDecl;
import org.eclipse.lsp4xml.extensions.dtd.utils.DTDUtils;
import org.eclipse.lsp4xml.services.extensions.IHighlightingParticipant;
import org.eclipse.lsp4xml.utils.XMLPositionUtility;

/**
 * DTD highlight participant
 * 
 * @author Angelo ZERR
 *
 */

public class DTDHighlightingParticipant implements IHighlightingParticipant {

	@Override
	public void findDocumentHighlights(DOMNode node, Position position, int offset, List<DocumentHighlight> highlights,
			CancelChecker cancelChecker) {
		boolean findReferences = false;
		DTDDeclParameter parameter = null;
		DTDElementDecl elementDecl = null;
		if (node.isDTDElementDecl()) {
			elementDecl = (DTDElementDecl) node;
			if (elementDecl.isInElementName(offset)) {
				findReferences = true;
				parameter = elementDecl.getNameNode();
			} else {
				parameter = elementDecl.getParameterAt(offset);
			}
		} else if (node.isDTDAttListDecl()) {
			DTDAttlistDecl attlistDecl = (DTDAttlistDecl) node;
			if (attlistDecl.isInElementName(offset)) {
				parameter = attlistDecl.getElementNameNode();
			}
		}

		if (parameter == null) {
			return;
		}

		if (findReferences) {
			DTDDeclParameter originNode = parameter;
			highlights
					.add(new DocumentHighlight(XMLPositionUtility.createRange(originNode), DocumentHighlightKind.Write));
			DTDUtils.searchDTDOriginElementDecls(elementDecl, (origin, target) -> {
				highlights.add(
						new DocumentHighlight(XMLPositionUtility.createRange(origin), DocumentHighlightKind.Read));
			}, cancelChecker);
		} else {
			DTDDeclParameter targetNode = parameter;
			//
			highlights
					.add(new DocumentHighlight(XMLPositionUtility.createRange(parameter), DocumentHighlightKind.Read));
			// Search target DTD element declaration <!ELEMENT
			DTDUtils.searchDTDTargetElementDecl(parameter, true, targetName -> {
				highlights.add(
						new DocumentHighlight(XMLPositionUtility.createRange(targetName), DocumentHighlightKind.Write));
			});
		}
	}

}
