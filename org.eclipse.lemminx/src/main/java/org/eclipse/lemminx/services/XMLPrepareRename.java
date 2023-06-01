/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
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

import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lemminx.services.extensions.rename.IPrepareRenameRequest;
import org.eclipse.lemminx.services.extensions.rename.IRenameParticipant;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PrepareRenameResult;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * XML prepare rename support.
 *
 */
public class XMLPrepareRename {

	private static final Logger LOGGER = Logger.getLogger(XMLPrepareRename.class.getName());

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLPrepareRename(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public Either<Range, PrepareRenameResult> prepareRename(DOMDocument xmlDocument, Position position,
			CancelChecker cancelChecker) {

		IPrepareRenameRequest request = null;

		try {
			request = new PrepareRenameRequest(xmlDocument, position, extensionsRegistry);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Failed creating prperare rename request", e);
			return null;
		}

		for (IRenameParticipant participant : extensionsRegistry.getRenameParticipants()) {
			try {
				Either<Range, PrepareRenameResult> result = participant.prepareRename(request, cancelChecker);
				if (result != null) {
					return result;
				}
			} catch (CancellationException e) {
				throw e;
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE,
						"Error while processing prepare rename for the participant '" + participant.getClass().getName()
								+ "'.",
						e);
			}
		}

		DOMNode node = request.getNode();
		if (node == null) {
			return null;
		}
		int offset = request.getOffset();
		if (node.isElement()) {
			// By default rename can be applied to rename tag name
			DOMElement element = (DOMElement) node;
			if (element.isInStartTag(offset)) {
				return Either.forLeft(XMLPositionUtility.selectStartTagName(element));
			}
			if (element.isInEndTag(offset)) {
				return Either.forLeft(XMLPositionUtility.selectEndTagName(element));
			}
		}
		return null;
	}
}
