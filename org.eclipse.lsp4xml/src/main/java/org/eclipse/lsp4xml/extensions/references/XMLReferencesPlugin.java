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
package org.eclipse.lsp4xml.extensions.references;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4xml.extensions.references.participants.XMLReferencesCompletionParticipant;
import org.eclipse.lsp4xml.extensions.references.participants.XMLReferencesDefinitionParticipant;
import org.eclipse.lsp4xml.services.extensions.ICompletionParticipant;
import org.eclipse.lsp4xml.services.extensions.IDefinitionParticipant;
import org.eclipse.lsp4xml.services.extensions.IXMLExtension;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.services.extensions.save.ISaveContext;

public class XMLReferencesPlugin implements IXMLExtension {

	private final ICompletionParticipant completionParticipant;
	private final IDefinitionParticipant definitionParticipant;

	public XMLReferencesPlugin() {
		completionParticipant = new XMLReferencesCompletionParticipant();
		definitionParticipant = new XMLReferencesDefinitionParticipant();
	}

	@Override
	public void doSave(ISaveContext context) {
		
	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		registry.registerCompletionParticipant(completionParticipant);
		registry.registerDefinitionParticipant(definitionParticipant);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		registry.unregisterCompletionParticipant(completionParticipant);
		registry.unregisterDefinitionParticipant(definitionParticipant);
	}

}
