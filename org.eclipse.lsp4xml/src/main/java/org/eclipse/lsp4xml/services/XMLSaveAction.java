/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.services;

import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.services.extensions.save.ISaveContext;
import org.eclipse.lsp4xml.services.extensions.save.ISaveParticipant;

/**
 * Save action support.
 */
class XMLSaveAction {

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLSaveAction(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public void doSave(ISaveContext context) {
		for (ISaveParticipant saveParticipant : extensionsRegistry.getSaveParticipants()) {
			saveParticipant.doSave(context);
		}

	}
}
