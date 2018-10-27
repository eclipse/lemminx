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
package org.eclipse.lsp4xml.extensions.references;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4xml.extensions.references.participants.XMLReferencesDefinitionParticipant;
import org.eclipse.lsp4xml.services.extensions.IDefinitionParticipant;
import org.eclipse.lsp4xml.services.extensions.IXMLExtension;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;

public class XMLReferencesPlugin implements IXMLExtension {

	private final IDefinitionParticipant definitionParticipant;

	public XMLReferencesPlugin() {
		definitionParticipant = new XMLReferencesDefinitionParticipant();
	}

	@Override
	public void updateSettings(Object settings) {

	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		registry.registerDefinitionParticipant(definitionParticipant);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		registry.unregisterDefinitionParticipant(definitionParticipant);
	}

}
