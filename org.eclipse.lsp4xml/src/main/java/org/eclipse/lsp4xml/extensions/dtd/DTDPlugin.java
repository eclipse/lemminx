/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.dtd;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelProvider;
import org.eclipse.lsp4xml.extensions.dtd.contentmodel.CMDTDContentModelProvider;
import org.eclipse.lsp4xml.extensions.dtd.participants.DTDDefinitionParticipant;
import org.eclipse.lsp4xml.extensions.dtd.participants.diagnostics.DTDDiagnosticsParticipant;
import org.eclipse.lsp4xml.services.extensions.IDefinitionParticipant;
import org.eclipse.lsp4xml.services.extensions.IXMLExtension;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lsp4xml.services.extensions.save.ISaveContext;

/**
 * DTD plugin.
 */
public class DTDPlugin implements IXMLExtension {

	private final IDiagnosticsParticipant diagnosticsParticipant;
	private final IDefinitionParticipant definitionParticipant;

	public DTDPlugin() {
		diagnosticsParticipant = new DTDDiagnosticsParticipant();
		definitionParticipant = new DTDDefinitionParticipant();
	}

	@Override
	public void doSave(ISaveContext context) {

	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		// register DTD content model provider
		ContentModelProvider modelProvider = new CMDTDContentModelProvider(registry.getResolverExtensionManager());
		ContentModelManager modelManager = registry.getComponent(ContentModelManager.class);
		modelManager.registerModelProvider(modelProvider);
		// register diagnostic participant
		registry.registerDiagnosticsParticipant(diagnosticsParticipant);
		// register definition participant
		registry.registerDefinitionParticipant(definitionParticipant);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		// unregister diagnostic participant
		registry.unregisterDiagnosticsParticipant(diagnosticsParticipant);
		// unregister definition participant
		registry.unregisterDefinitionParticipant(definitionParticipant);
	}
}
