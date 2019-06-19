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
import java.util.List;

import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.services.extensions.IDefinitionParticipant;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;

/**
 * XML definition support.
 *
 */
class XMLDefinition {

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLDefinition(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public List<? extends LocationLink> findDefinition(DOMDocument document, Position position, CancelChecker cancelChecker) {
		List<LocationLink> locations = new ArrayList<>();
		for (IDefinitionParticipant participant : extensionsRegistry.getDefinitionParticipants()) {
			participant.findDefinition(document, position, locations, cancelChecker);
		}
		return locations;
	}

}
