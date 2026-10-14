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
package org.eclipse.lemminx.extensions.contentmodel.participants;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.dom.DOMRange;
import org.eclipse.lemminx.extensions.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lemminx.extensions.contentmodel.model.CMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.CMElementDeclaration;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.services.extensions.semantictokens.ISemanticTokensParticipant;
import org.eclipse.lemminx.services.extensions.semantictokens.ISemanticTokensRequest;
import org.eclipse.lemminx.services.extensions.semantictokens.SemanticTokensData;
import org.eclipse.lemminx.settings.capabilities.ServerCapabilitiesConstants;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Semantic tokens participant that highlights element tag names and required
 * attributes based on the bound grammar (XSD, DTD, etc.).
 */
public class ContentModelSemanticTokensParticipant implements ISemanticTokensParticipant {

	private static final Logger LOGGER = Logger.getLogger(ContentModelSemanticTokensParticipant.class.getName());

	@Override
	public void findSemanticTokens(ISemanticTokensRequest request, SemanticTokensData builder,
			CancelChecker cancelChecker) {
		DOMDocument document = request.getDocument();
		ContentModelManager contentModelManager = request.getComponent(ContentModelManager.class);
		if (contentModelManager == null) {
			return;
		}
		findSemanticTokensInNode(document, contentModelManager, builder, cancelChecker);
	}

	private void findSemanticTokensInNode(DOMNode node, ContentModelManager contentModelManager,
			SemanticTokensData builder, CancelChecker cancelChecker) {
		cancelChecker.checkCanceled();
		if (node.isElement()) {
			DOMElement element = (DOMElement) node;
			try {
				highlightElement(element, contentModelManager, builder);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Error while computing semantic tokens for element '"
						+ element.getTagName() + "'.", e);
			}
		}
		List<DOMNode> children = node.getChildren();
		if (children != null) {
			for (DOMNode child : children) {
				findSemanticTokensInNode(child, contentModelManager, builder, cancelChecker);
			}
		}
	}

	private void highlightElement(DOMElement element, ContentModelManager contentModelManager,
			SemanticTokensData builder) {
		Collection<CMDocument> cmDocuments = contentModelManager.findCMDocument(element);
		if (cmDocuments.isEmpty()) {
			return;
		}
		for (CMDocument cmDocument : cmDocuments) {
			CMElementDeclaration cmElement = cmDocument.findCMElement(element);
			if (cmElement != null) {
				if (isRequiredElement(element, cmDocument)) {
					highlightElementTagName(element, builder);
				}
				highlightRequiredAttributes(element, cmElement, builder);
				return;
			}
		}
	}

	private boolean isRequiredElement(DOMElement element, CMDocument cmDocument) {
		DOMNode parent = element.getParentNode();
		if (parent == null || !parent.isElement()) {
			return true;
		}
		CMElementDeclaration parentCmElement = cmDocument.findCMElement((DOMElement) parent);
		if (parentCmElement == null) {
			return false;
		}
		return !parentCmElement.isOptional(element.getLocalName());
	}

	private void highlightElementTagName(DOMElement element, SemanticTokensData builder) {
		if (!element.hasTagName()) {
			return;
		}
		String tagName = element.getTagName();
		int tagNameLength = tagName.length();
		if (element.hasStartTag()) {
			int startTagNameOffset = element.getStartTagOpenOffset() + 1; // skip '<'
			builder.addToken(startTagNameOffset, tagNameLength,
					ServerCapabilitiesConstants.TOKEN_TYPE_TYPE, 0);
		}
		if (element.hasEndTag()) {
			int endTagNameOffset = element.getEndTagOpenOffset() + 2; // skip '</'
			builder.addToken(endTagNameOffset, tagNameLength,
					ServerCapabilitiesConstants.TOKEN_TYPE_TYPE, 0);
		}
	}

	private void highlightRequiredAttributes(DOMElement element, CMElementDeclaration cmElement,
			SemanticTokensData builder) {
		List<DOMAttr> attributes = element.getAttributeNodes();
		if (attributes == null || attributes.isEmpty()) {
			return;
		}
		for (DOMAttr attr : attributes) {
			CMAttributeDeclaration cmAttr = cmElement.findCMAttribute(attr);
			if (cmAttr != null && cmAttr.isRequired()) {
				DOMRange nameRange = attr.getNodeAttrName();
				if (nameRange == null) {
					continue;
				}
				int nameStart = nameRange.getStart();
				int nameLength = nameRange.getEnd() - nameStart;
				builder.addToken(nameStart, nameLength,
						ServerCapabilitiesConstants.TOKEN_TYPE_PROPERTY,
						ServerCapabilitiesConstants.TOKEN_MODIFIER_READONLY);
			}
		}
	}
}
