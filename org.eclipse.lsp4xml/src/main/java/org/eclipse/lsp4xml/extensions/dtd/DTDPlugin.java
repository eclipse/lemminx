/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.dtd;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelProvider;
import org.eclipse.lsp4xml.extensions.dtd.contentmodel.DTDContentModelProvider;
import org.eclipse.lsp4xml.extensions.dtd.diagnostics.DTDDiagnosticsParticipant;
import org.eclipse.lsp4xml.services.extensions.IXMLExtension;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lsp4xml.services.extensions.save.ISaveContext;

/**
 * DTD plugin.
 */
public class DTDPlugin implements IXMLExtension {

	private final IDiagnosticsParticipant diagnosticsParticipant;

	public DTDPlugin() {
		diagnosticsParticipant = new DTDDiagnosticsParticipant();
	}

	@Override
	public void doSave(ISaveContext context) {

	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		// register DTD content model provider
		ContentModelProvider modelProvider = new DTDContentModelProvider(registry.getResolverExtensionManager());
		ContentModelManager modelManager = registry.getComponent(ContentModelManager.class);
		modelManager.registerModelProvider(modelProvider);
		// register diagnostic participant
		registry.registerDiagnosticsParticipant(diagnosticsParticipant);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		// unregister diagnostic participant
		registry.unregisterDiagnosticsParticipant(diagnosticsParticipant);
	}
}
