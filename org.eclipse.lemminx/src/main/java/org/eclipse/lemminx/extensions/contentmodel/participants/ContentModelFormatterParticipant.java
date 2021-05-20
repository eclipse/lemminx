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

import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.extensions.contentmodel.model.CMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.CMElementDeclaration;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.services.extensions.format.IFormatterParticipant;
import org.eclipse.lemminx.services.format.FormatElementCategory;
import org.eclipse.lemminx.services.format.XMLFormattingConstraints;
import org.eclipse.lemminx.settings.SharedSettings;

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
			XMLFormattingConstraints parentConstraints, SharedSettings sharedSettings) {
		boolean enabled = sharedSettings.getFormattingSettings().isGrammarAwareFormatting();
		if (!enabled) {
			return null;
		}

		Collection<CMDocument> cmDocuments = contentModelManager.findCMDocument(element);
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

}
