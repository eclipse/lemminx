/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.dtd.participants;

import java.util.List;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMDocumentType;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.dom.DTDAttlistDecl;
import org.eclipse.lemminx.dom.DTDDeclParameter;
import org.eclipse.lemminx.dom.DTDEntityDecl;
import org.eclipse.lemminx.services.extensions.semantictokens.ISemanticTokensParticipant;
import org.eclipse.lemminx.services.extensions.semantictokens.ISemanticTokensRequest;
import org.eclipse.lemminx.services.extensions.semantictokens.SemanticTokensData;
import org.eclipse.lemminx.settings.capabilities.ServerCapabilitiesConstants;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Semantic tokens participant that highlights names in DTD declarations:
 * <!ENTITY>, <!ELEMENT>, <!ATTLIST>, and <!NOTATION>.
 */
public class DTDSemanticTokensParticipant implements ISemanticTokensParticipant {

	@Override
	public void findSemanticTokens(ISemanticTokensRequest request, SemanticTokensData builder,
			CancelChecker cancelChecker) {
		DOMDocument document = request.getDocument();
		// Handle standalone DTD files
		highlightDTDDecls(document.getChildren(), builder, cancelChecker);
		// Handle inline DTD in XML files
		DOMDocumentType doctype = document.getDoctype();
		if (doctype != null) {
			highlightDTDDecls(doctype.getChildren(), builder, cancelChecker);
		}
	}

	private void highlightDTDDecls(List<DOMNode> nodes, SemanticTokensData builder, CancelChecker cancelChecker) {
		if (nodes == null) {
			return;
		}
		for (DOMNode node : nodes) {
			cancelChecker.checkCanceled();
			if (node.isDTDEntityDecl()) {
				highlightEntityDecl((DTDEntityDecl) node, builder);
			} else if (node.isDTDElementDecl()) {
				highlightNameParameter(node, builder,
						ServerCapabilitiesConstants.TOKEN_TYPE_TYPE,
						ServerCapabilitiesConstants.TOKEN_MODIFIER_DECLARATION);
			} else if (node.isDTDAttListDecl()) {
				highlightAttlistDecl((DTDAttlistDecl) node, builder, cancelChecker);
			} else if (node.isDTDNotationDecl()) {
				highlightNameParameter(node, builder,
						ServerCapabilitiesConstants.TOKEN_TYPE_TYPE,
						ServerCapabilitiesConstants.TOKEN_MODIFIER_DECLARATION);
			}
		}
	}

	private void highlightEntityDecl(DTDEntityDecl entity, SemanticTokensData builder) {
		addTokenFromParameter(entity.getNameParameter(), builder,
				ServerCapabilitiesConstants.TOKEN_TYPE_VARIABLE,
				ServerCapabilitiesConstants.TOKEN_MODIFIER_DECLARATION);
	}

	private void highlightNameParameter(DOMNode node, SemanticTokensData builder, int tokenType, int tokenModifiers) {
		if (node instanceof org.eclipse.lemminx.dom.DTDDeclNode) {
			addTokenFromParameter(((org.eclipse.lemminx.dom.DTDDeclNode) node).getNameParameter(),
					builder, tokenType, tokenModifiers);
		}
	}

	private void highlightAttlistDecl(DTDAttlistDecl attlist, SemanticTokensData builder,
			CancelChecker cancelChecker) {
		// Highlight element name as type reference (no declaration modifier)
		addTokenFromParameter(attlist.getNameParameter(), builder,
				ServerCapabilitiesConstants.TOKEN_TYPE_TYPE, 0);
		// Highlight attribute name as property declaration
		addTokenFromParameter(attlist.attributeName, builder,
				ServerCapabilitiesConstants.TOKEN_TYPE_PROPERTY,
				ServerCapabilitiesConstants.TOKEN_MODIFIER_DECLARATION);
		// Handle additional attribute declarations within the same ATTLIST
		List<DTDAttlistDecl> internalChildren = attlist.getInternalChildren();
		if (internalChildren != null) {
			for (DTDAttlistDecl child : internalChildren) {
				cancelChecker.checkCanceled();
				addTokenFromParameter(child.attributeName, builder,
						ServerCapabilitiesConstants.TOKEN_TYPE_PROPERTY,
						ServerCapabilitiesConstants.TOKEN_MODIFIER_DECLARATION);
			}
		}
	}

	private void addTokenFromParameter(DTDDeclParameter param, SemanticTokensData builder,
			int tokenType, int tokenModifiers) {
		if (param == null) {
			return;
		}
		int start = param.getStart();
		int length = param.getEnd() - start;
		builder.addToken(start, length, tokenType, tokenModifiers);
	}
}
