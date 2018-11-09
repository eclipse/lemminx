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
package org.eclipse.lsp4xml.extensions.xsd;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4xml.extensions.xsd.participants.XSDCompletionParticipant;
import org.eclipse.lsp4xml.extensions.xsd.participants.diagnostics.XSDDiagnosticsParticipant;
import org.eclipse.lsp4xml.services.extensions.ICompletionParticipant;
import org.eclipse.lsp4xml.services.extensions.IXMLExtension;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lsp4xml.services.extensions.save.ISaveContext;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtensionManager;

/**
 * XSD plugin.
 */
public class XSDPlugin implements IXMLExtension {

	private final ICompletionParticipant completionParticipant;

	private final IDiagnosticsParticipant diagnosticsParticipant;

	private XSDURIResolverExtension uiResolver;

	public XSDPlugin() {
		completionParticipant = new XSDCompletionParticipant();
		diagnosticsParticipant = new XSDDiagnosticsParticipant();
	}

	@Override
	public void doSave(ISaveContext context) {
	
	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		uiResolver = new XSDURIResolverExtension(registry.getDocumentProvider());
		URIResolverExtensionManager.getInstance().registerResolver(uiResolver);
		registry.registerCompletionParticipant(completionParticipant);
		registry.registerDiagnosticsParticipant(diagnosticsParticipant);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		URIResolverExtensionManager.getInstance().unregisterResolver(uiResolver);
		registry.unregisterDiagnosticsParticipant(diagnosticsParticipant);
	}
}
