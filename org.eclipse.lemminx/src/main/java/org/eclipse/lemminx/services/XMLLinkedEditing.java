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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.ILinkedEditingRangesParticipant;
import org.eclipse.lemminx.services.extensions.ILinkedEditingRangesRequest;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
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

	private static final String WORD_PATTERN = "[^\\s>]+";

	private static Logger LOGGER = Logger.getLogger(XMLLinkedEditing.class.getName());

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLLinkedEditing(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

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

			ILinkedEditingRangesRequest request = new LinkedEditingRangesRequest(document, position,
					extensionsRegistry);
			DOMNode node = request.getNode();
			if (node == null) {
				return null;
			}

			final List<Range> ranges = new ArrayList<>();
			for (ILinkedEditingRangesParticipant participant : extensionsRegistry
					.getLinkedEditingRangesParticipants()) {
				try {
					participant.findLinkedEditingRanges(request, ranges, cancelChecker);
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE,
							"Error while processing linked editing ranges for the participant '"
									+ participant.getClass().getName() + "'.",
							e);
				}
			}

			if (node.isElement()) {
				DOMElement element = (DOMElement) node;
				if (!(element.isOrphanEndTag() || !element.hasEndTag())) {
					int offset = request.getOffset();
					if (element.isInStartTag(offset) || element.isInEndTag(offset, true)) {
						ranges.addAll(Arrays.asList(XMLPositionUtility.selectStartTagName(element),
								XMLPositionUtility.selectEndTagName(element)));

					}
				}
			}
			cancelChecker.checkCanceled();
			// Word pattern is defined here to have less restriction. Without this pattern
			// when you try to rename content which contains '.' the linked editing range
			// fails.
			// Ex : <foo.bar></foo.bar> fails without the word pattern which allows any
			// characters except spaces.
			return !ranges.isEmpty() ? new LinkedEditingRanges(ranges, WORD_PATTERN) : null;
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In XMLLinkedEditing, position error", e);
		}
		return null;
	}
}
