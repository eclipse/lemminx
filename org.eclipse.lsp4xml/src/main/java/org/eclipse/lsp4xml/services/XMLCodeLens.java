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

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.services.extensions.ICodeLensParticipant;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;

/**
 * XML Code Lens support.
 *
 */
class XMLCodeLens {

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLCodeLens(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public List<? extends CodeLens> getCodelens(DOMDocument xmlDocument, CancelChecker cancelChecker) {
		List<CodeLens> lenses = new ArrayList<>();
		for (ICodeLensParticipant participant : extensionsRegistry.getCodeLensParticipants()) {
			participant.doCodeLens(xmlDocument, lenses, cancelChecker);
		}
		return lenses;
	}

}
