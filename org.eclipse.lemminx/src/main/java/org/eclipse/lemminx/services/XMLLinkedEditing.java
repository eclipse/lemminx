/*******************************************************************************
* Copyright (c) 2021, 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.LinkedEditingRanges;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * XML linked editing support.
 *
 */
class XMLLinkedEditing {

	private static Logger LOGGER = Logger.getLogger(XMLLinkedEditing.class.getName());

	// Full XML Element name pattern
	public static final String XML_ELEMENT_WORD_PATTERN =
			"[:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6" //$NON-NLS-1$
			+ "\\u00F8-\\u02ff\\u0370-\\u037d\\u037f-\\u1fff\\u200c\\u200d\\u2070-\\u218f" //$NON-NLS-1$
			+ "\\u2c00-\\u2fef\\u3001-\\udfff\\uf900-\\ufdcf\\ufdf0-\\ufffd\\u10000-\\uEFFFF]" //$NON-NLS-1$
			+ "[:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6" //$NON-NLS-1$
			+ "\\u00F8-\\u02ff\\u0370-\\u037d\\u037f-\\u1fff\\u200c\\u200d\\u2070-\\u218f" //$NON-NLS-1$
			+ "\\u2c00-\\u2fef\\u3001-\\udfff\\uf900-\\ufdcf\\ufdf0-\\ufffd\\u10000-\\uEFFFF\\-\\.0-9" //$NON-NLS-1$
			+ "\\u00b7\\u0300-\\u036f\\u203f-\\u2040]*"; //$NON-NLS-1$
	
	/**
	 * Returns the linked editing ranges for the given <code>xmlDocument</code> at
	 * the given <code>position</code> and null otherwise.
	 *
	 * @param xmlDocument   the DOM document.
	 * @param position      the position.
	 * @param cancelChecker the cancel checker.
	 * @return the linked editing ranges for the given <code>xmlDocument</code> at
	 *         the given <code>position</code> and null otherwise.
	 */
	public LinkedEditingRanges findLinkedEditingRanges(DOMDocument document, Position position,
			CancelChecker cancelChecker) {
		try {
			cancelChecker.checkCanceled();
			
			int offset = document.offsetAt(position);
			DOMNode node = document.findNodeAt(offset);
			if (node == null || !node.isElement()) {
				return null;
			}
			DOMElement element = (DOMElement) node;
			if (element.isOrphanEndTag() || !element.hasEndTag()) {
				return null;
			}

			if (element.isInStartTag(offset) || element.isInEndTag(offset, true)) {
				List<Range> ranges = Arrays.asList(XMLPositionUtility.selectStartTagName(element),
						XMLPositionUtility.selectEndTagName(element));
				
				cancelChecker.checkCanceled();
				
				return new LinkedEditingRanges(ranges, XML_ELEMENT_WORD_PATTERN);
			}
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In XMLLinkedEditing, position error", e);
		}
		return null;
	}
}
