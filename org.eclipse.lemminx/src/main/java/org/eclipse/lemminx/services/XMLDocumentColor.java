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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.IDocumentColorParticipant;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4j.ColorInformation;
import org.eclipse.lsp4j.ColorPresentation;
import org.eclipse.lsp4j.ColorPresentationParams;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * XML document color support.
 *
 */
class XMLDocumentColor {

	private static final Logger LOGGER = Logger.getLogger(XMLDocumentColor.class.getName());

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLDocumentColor(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public List<ColorInformation> findDocumentColors(DOMDocument xmlDocument, CancelChecker cancelChecker) {
		cancelChecker.checkCanceled();

		List<ColorInformation> colors = new ArrayList<>();
		for (IDocumentColorParticipant participant : extensionsRegistry.getDocumentColorParticipants()) {
			try {
				participant.doDocumentColor(xmlDocument, colors, cancelChecker);
			} catch (CancellationException e) {
				throw e;
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error while processing document color for the participant '"
						+ participant.getClass().getName() + "'.", e);
			}
		}

		cancelChecker.checkCanceled();

		return colors;
	}

	public List<ColorPresentation> getColorPresentations(DOMDocument xmlDocument, ColorPresentationParams params,
			CancelChecker cancelChecker) {
		cancelChecker.checkCanceled();

		List<ColorPresentation> presentations = new ArrayList<>();
		for (IDocumentColorParticipant participant : extensionsRegistry.getDocumentColorParticipants()) {
			try {
				participant.doColorPresentations(xmlDocument, params, presentations, cancelChecker);
			} catch (CancellationException e) {
				throw e;
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error while processing color presentation for the participant '"
						+ participant.getClass().getName() + "'.", e);
			}
		}

		cancelChecker.checkCanceled();

		return presentations;
	}

}
