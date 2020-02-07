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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.IReferenceParticipant;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * XML reference support.
 *
 */
class XMLReference {

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLReference(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public List<? extends Location> findReferences(DOMDocument document, Position position, ReferenceContext context,
			CancelChecker cancelChecker) {
		List<Location> locations = new ArrayList<>();
		for (IReferenceParticipant participant : extensionsRegistry.getReferenceParticipants()) {
			participant.findReference(document, position, context, locations, cancelChecker);
		}
		return locations;
	}

}
