/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
*  are made available under the terms of the Eclipse Public License v2.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services.extensions.format;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.extensions.contentmodel.model.CMDocument;
import org.eclipse.lemminx.services.format.FormatElementCategory;
import org.eclipse.lemminx.services.format.XMLFormatterDocument;
import org.eclipse.lemminx.services.format.XMLFormattingConstraints;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lemminx.settings.XMLFormattingOptions;
import org.eclipse.lemminx.utils.XMLBuilder;
import org.eclipse.lsp4j.TextEdit;

/**
 * XML formatter participant.
 * 
 * @author Angelo ZERR
 *
 */
public interface IFormatterParticipant {

	/**
	 * Format the given attribute value.
	 * 
	 * <p>
	 * The formatter must take care of to generate attribute value with quote.
	 * </p>
	 * 
	 * @param name              the attribute name.
	 * @param valueWithoutQuote the attribute value without quote.
	 * @param quote             the quote and null otherwise. null quote means that
	 *                          the formatter must not generate a quote in the xml
	 *                          builder.
	 * @param attr              the DOM attribute and null otherwise.
	 * @param xml               the XML builder.
	 * @return true if the given attribute can be formatted and false otherwise.
	 */
	default boolean formatAttributeValue(String name, String valueWithoutQuote, Character quote, DOMAttr attr,
			XMLBuilder xml) {
		return false;
	}

	/**
	 * Format the given attribute value.
	 * 
	 * @param formatterDocument the formatter document
	 * @param indentLevel       the current indent level of the attribute
	 * @param formattingOptions the formatting options that contain the settings
	 * @param attr              the attribute node
	 * @param edits             the text edit list
	 * @return true if the given attribute can be formatted and false otherwise.
	 */
	default boolean formatAttributeValue(DOMAttr attr, XMLFormatterDocument formatterDocument,
			XMLFormattingConstraints parentConstraints, XMLFormattingOptions formattingOptions, List<TextEdit> edits) {
		return false;
	}

	/**
	 * Returns the format element category for the given DOM element and null
	 * otherwise.
	 * 
	 * @param element           the DOM element.
	 * @param parentConstraints the parent constraints.
	 * @param sharedSettings    the shared settings.
	 * 
	 * @return the format element category for the given DOM element and null
	 *         otherwise.
	 */
	default FormatElementCategory getFormatElementCategory(DOMElement element,
			XMLFormattingConstraints parentConstraints, Map<String, Collection<CMDocument>> formattingContext,
			SharedSettings sharedSettings) {
		return null;
	}

	/**
	 * Returns true if the given element can be collapsed according to grammar
	 * constraints.
	 * 
	 * @param element        the DOM element.
	 * @param sharedSettings the shared settings.
	 * 
	 * @return true if the given element can be collapsed according to grammar
	 *         constraints.
	 */
	default boolean shouldCollapseEmptyElement(DOMElement element, SharedSettings sharedSettings) {
		return true;
	}
}
