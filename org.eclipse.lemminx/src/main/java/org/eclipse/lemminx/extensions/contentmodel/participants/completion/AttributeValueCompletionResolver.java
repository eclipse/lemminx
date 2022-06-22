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
package org.eclipse.lemminx.extensions.contentmodel.participants.completion;

import java.util.Collection;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.extensions.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lemminx.extensions.contentmodel.model.CMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.CMElementDeclaration;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.extensions.contentmodel.utils.XMLGenerator;
import org.eclipse.lemminx.services.extensions.completion.ICompletionItemResolverRequest;
import org.eclipse.lemminx.uriresolver.CacheResourceDownloadingException;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.MarkupContent;

/**
 * Resolves the documentation for the completion of the attribute value from the
 * content model.
 *
 */
public class AttributeValueCompletionResolver extends AbstractCMCompletionResolver {

	public static final String PARTICIPANT_ID = AttributeValueCompletionResolver.class.getName();

	protected void addDocumentationToCompletion(ICompletionItemResolverRequest request, CompletionItem toResolve,
			DOMElement parentElement, DOMAttr attr) {
		if (attr == null) {
			return;
		}
		String attributeName = attr.getName();
		String attributeValue = toResolve.getLabel();
		try {
			ContentModelManager contentModelManager = request.getComponent(ContentModelManager.class);
			Collection<CMDocument> cmDocuments = contentModelManager.findCMDocument(parentElement);
			for (CMDocument cmDocument : cmDocuments) {
				CMElementDeclaration cmElement = cmDocument.findCMElement(parentElement,
						parentElement.getNamespaceURI());
				if (cmElement != null) {
					MarkupContent documentation = getDocumentationForAttributeValue(cmElement, attributeName,
							attributeValue, request);
					if (documentation != null) {
						toResolve.setDocumentation(documentation);
						return;
					}
				}
			}
		} catch (CacheResourceDownloadingException e) {
		}
	}

	private static MarkupContent getDocumentationForAttributeValue(CMElementDeclaration cmElement, String attributeName,
			String attributeValue, ICompletionItemResolverRequest request) {
		CMAttributeDeclaration cmAttribute = cmElement.findCMAttribute(attributeName);
		if (cmAttribute != null) {
			return XMLGenerator.createMarkupContent(cmAttribute, attributeValue, cmElement,
					request);
		}
		return null;
	}

}
