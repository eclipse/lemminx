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
package org.eclipse.lemminx.extensions.contentmodel.participants;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.extensions.contentmodel.model.CMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.CMElementDeclaration;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.services.extensions.format.IFormatterParticipant;
import org.eclipse.lemminx.services.format.FormatElementCategory;
import org.eclipse.lemminx.services.format.XMLFormattingConstraints;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lemminx.uriresolver.CacheResourceDownloadingException;

/**
 * Formatter participant which uses XSD/DTD grammar information to know the
 * {@link FormatElementCategory} of a given element.
 *
 * <p>
 *
 * This participant is enabled when 'xml.format.grammarAwareFormatting' setting
 * is set to true.
 *
 * </p>
 *
 * @author Angelo ZERR
 *
 */
public class ContentModelFormatterParticipant implements IFormatterParticipant {

	private final ContentModelManager contentModelManager;

	public ContentModelFormatterParticipant(ContentModelManager contentModelManager) {
		this.contentModelManager = contentModelManager;
	}

	@Override
	public FormatElementCategory getFormatElementCategory(DOMElement element,
			XMLFormattingConstraints parentConstraints, Map<String, Collection<CMDocument>> formattingContext,
			SharedSettings sharedSettings) {
		boolean enabled = sharedSettings.getFormattingSettings().isGrammarAwareFormatting();
		if (!enabled) {
			return null;
		}

		String namespaceURI = element.getNamespaceURI() != null ? element.getNamespaceURI()
				: element.getOwnerDocument().getNamespaceURI();
		if (namespaceURI == null) {
			namespaceURI = "noNameSpace";
		}
		Collection<CMDocument> cmDocuments = formattingContext.get(namespaceURI);
		if (cmDocuments == null) {
			try {
				cmDocuments = contentModelManager.findCMDocument(element);
			} catch (CacheResourceDownloadingException e) {
				// block; we need the content model to format correctly.
				// if we don't block and instead return null here,
				// then some parts of the document will be
				// grammar-aware formatted and others won't be.
				try {
					e.getFuture().get(2000, TimeUnit.MILLISECONDS);
					cmDocuments = contentModelManager.findCMDocument(element);
				} catch (InterruptedException | ExecutionException | TimeoutException e1) {
					// If it takes longer than 2 seconds to download the schema, we should give up and proceed with formatting
					return null;
				}
			}
			formattingContext.put(namespaceURI, cmDocuments);
		}

		for (CMDocument cmDocument : cmDocuments) {
			CMElementDeclaration cmElement = cmDocument.findCMElement(element);
			if (cmElement != null) {
				if (cmElement.isStringType()) {
					return FormatElementCategory.PreserveSpace;
				}
				if (cmElement.isMixedContent()) {
					return FormatElementCategory.MixedContent;
				}
			}
		}
		return null;
	}

	@Override
	public boolean shouldCollapseEmptyElement(DOMElement element, SharedSettings sharedSettings) {
		boolean enabled = sharedSettings.getFormattingSettings().isGrammarAwareFormatting();
		if (!enabled) {
			return true;
		}
		if (!("true".equals(element.getAttribute("xsi:nil")))) {
			// Only check the schema for value of nillable when xsi:nil="true" in element
			return true;
		}
		Collection<CMDocument> cmDocuments = contentModelManager.findCMDocument(element);
		if (cmDocuments.isEmpty()) {
			// The DOM document is not linked to a grammar, the collapse can be done.
			return true;
		}
		for (CMDocument cmDocument : cmDocuments) {
			CMElementDeclaration cmElement = cmDocument.findCMElement(element);
			if (cmElement != null && !cmElement.isNillable()) {
				// Collapse is not allowed in the case where nillable="false" in xsd and
				// xsi:nil="true" for the element in xml
				return false;
			}
		}
		return true;
	}
}
