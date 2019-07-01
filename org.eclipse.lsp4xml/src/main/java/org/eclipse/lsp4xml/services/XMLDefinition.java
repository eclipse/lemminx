/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.services.extensions.IDefinitionParticipant;
import org.eclipse.lsp4xml.services.extensions.IDefinitionRequest;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;

/**
 * XML definition support.
 *
 */
class XMLDefinition {

	private static final Logger LOGGER = Logger.getLogger(XMLTypeDefinition.class.getName());

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLDefinition(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public List<? extends LocationLink> findDefinition(DOMDocument document, Position position,
			CancelChecker cancelChecker) {
		IDefinitionRequest request = null;
		try {
			request = new DefinitionRequest(document, position, extensionsRegistry);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Failed creating TypeDefinitionRequest", e);
			return Collections.emptyList();
		}
		List<LocationLink> locations = new ArrayList<>();
		for (IDefinitionParticipant participant : extensionsRegistry.getDefinitionParticipants()) {
			participant.findDefinition(request, locations, cancelChecker);
		}
		return locations;
	}

}
