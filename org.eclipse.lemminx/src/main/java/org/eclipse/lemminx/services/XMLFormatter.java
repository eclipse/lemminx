/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.services;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lemminx.services.extensions.format.IFormatterParticipant;
import org.eclipse.lemminx.services.format.XMLFormatterDocumentOld;
import org.eclipse.lemminx.services.format.XMLFormatterDocument;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

/**
 * XML formatter support.
 *
 */
class XMLFormatter {
	private static final Logger LOGGER = Logger.getLogger(XMLFormatter.class.getName());

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLFormatter(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	/**
	 * Returns a List containing multiple TextEdits to remove, add,
	 * update spaces / indent.
	 *
	 * @param textDocument   document to perform formatting on
	 * @param range          specified range in which formatting will be done
	 * @param sharedSettings settings containing formatting preferences
	 * @return List containing a TextEdit with formatting changes
	 */
	public List<? extends TextEdit> format(DOMDocument xmlDocument, Range range, SharedSettings sharedSettings) {
		try {
			if (sharedSettings.getFormattingSettings().isLegacy()) {
				XMLFormatterDocumentOld formatterDocument = new XMLFormatterDocumentOld(xmlDocument.getTextDocument(),
						range, sharedSettings, getFormatterParticipants());
				return formatterDocument.format();
			}
			XMLFormatterDocument formatterDocument = new XMLFormatterDocument(xmlDocument, range,
					sharedSettings, getFormatterParticipants());
			return formatterDocument.format();
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Formatting failed due to BadLocation", e);
		}
		return null;
	}

	/**
	 * Returns list of {@link IFormatterParticipant}.
	 *
	 * @return list of {@link IFormatterParticipant}.
	 */
	private Collection<IFormatterParticipant> getFormatterParticipants() {
		return extensionsRegistry.getFormatterParticipants();
	}
}
