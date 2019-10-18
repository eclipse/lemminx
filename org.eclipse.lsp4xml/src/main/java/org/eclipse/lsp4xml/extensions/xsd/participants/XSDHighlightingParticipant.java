/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.extensions.xsd.participants;

import java.util.List;

import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.extensions.xsd.utils.XSDUtils;
import org.eclipse.lsp4xml.extensions.xsd.utils.XSDUtils.BindingType;
import org.eclipse.lsp4xml.services.extensions.IHighlightingParticipant;
import org.eclipse.lsp4xml.utils.DOMUtils;
import org.eclipse.lsp4xml.utils.XMLPositionUtility;

/**
 * XSD highlight participant
 * 
 * @author Angelo ZERR
 *
 */
public class XSDHighlightingParticipant implements IHighlightingParticipant {

	@Override
	public void findDocumentHighlights(DOMNode node, Position position, int offset, List<DocumentHighlight> highlights,
			CancelChecker cancelChecker) {
		// XSD highlight applicable only for XSD file
		DOMDocument document = node.getOwnerDocument();
		if (!DOMUtils.isXSD(document)) {
			return;
		}
		// Highlight works only when attribute is selected (origin or target attribute)
		DOMAttr attr = node.findAttrAt(offset);
		if (attr == null || attr.getNodeAttrValue() == null) {
			return;
		}
		// Try to get the binding from the origin attribute
		BindingType bindingType = XSDUtils.getBindingType(attr);
		if (bindingType != BindingType.NONE) {
			String documentURI = document.getDocumentURI();
			// It's an origin attribute, highlight the origin and target attribute
			DOMAttr originAttr = attr;
			highlights
					.add(new DocumentHighlight(XMLPositionUtility.createRange(originAttr.getNodeAttrValue().getStart(),
							originAttr.getNodeAttrValue().getEnd(), document), DocumentHighlightKind.Read));
			// Search target attributes
			XSDUtils.searchXSTargetAttributes(originAttr, bindingType, true, (targetNamespacePrefix, targetAttr) -> {
				DOMDocument targetDocument = targetAttr.getOwnerDocument();
				if (!documentURI.equals(targetDocument.getDocumentURI())) {
					// The target not to highlight is not the same origin XML Schema document,
					// ignore it.
					return;
				}
				highlights
						.add(new DocumentHighlight(
								XMLPositionUtility.createRange(targetAttr.getNodeAttrValue().getStart(),
										targetAttr.getNodeAttrValue().getEnd(), targetDocument),
								DocumentHighlightKind.Write));
			});

		} else if (XSDUtils.isXSTargetElement(attr.getOwnerElement())) {
			// It's an target attribute, highlight all origin attributes linked to this
			// target attribute
			DOMAttr targetAttr = attr;
			highlights.add(new DocumentHighlight(
					XMLPositionUtility.createRange(targetAttr.getNodeAttrValue().getStart(),
							targetAttr.getNodeAttrValue().getEnd(), targetAttr.getOwnerDocument()),
					DocumentHighlightKind.Write));
			XSDUtils.searchXSOriginAttributes(targetAttr,
					(origin, target) -> highlights.add(new DocumentHighlight(
							XMLPositionUtility.createRange(origin.getNodeAttrValue().getStart(),
									origin.getNodeAttrValue().getEnd(), origin.getOwnerDocument()),
							DocumentHighlightKind.Read)),
					cancelChecker);
		}
	}

}
