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
package org.eclipse.lsp4xml.extensions.xsd;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelProvider;
import org.eclipse.lsp4xml.extensions.xsd.contentmodel.CMXSDContentModelProvider;
import org.eclipse.lsp4xml.extensions.xsd.participants.XSDCodeLensParticipant;
import org.eclipse.lsp4xml.extensions.xsd.participants.XSDCompletionParticipant;
import org.eclipse.lsp4xml.extensions.xsd.participants.XSDDefinitionParticipant;
import org.eclipse.lsp4xml.extensions.xsd.participants.XSDHighlightingParticipant;
import org.eclipse.lsp4xml.extensions.xsd.participants.XSDReferenceParticipant;
import org.eclipse.lsp4xml.extensions.xsd.participants.XSDRenameParticipant;
import org.eclipse.lsp4xml.extensions.xsd.participants.diagnostics.XSDDiagnosticsParticipant;
import org.eclipse.lsp4xml.services.extensions.ICompletionParticipant;
import org.eclipse.lsp4xml.services.extensions.IDefinitionParticipant;
import org.eclipse.lsp4xml.services.extensions.IHighlightingParticipant;
import org.eclipse.lsp4xml.services.extensions.IReferenceParticipant;
import org.eclipse.lsp4xml.services.extensions.IRenameParticipant;
import org.eclipse.lsp4xml.services.extensions.IXMLExtension;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.services.extensions.codelens.ICodeLensParticipant;
import org.eclipse.lsp4xml.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lsp4xml.services.extensions.save.ISaveContext;
import org.eclipse.lsp4xml.utils.DOMUtils;

/**
 * XSD plugin.
 */
public class XSDPlugin implements IXMLExtension {

	private final ICompletionParticipant completionParticipant;

	private final IDefinitionParticipant definitionParticipant;

	private final IDiagnosticsParticipant diagnosticsParticipant;

	private final IReferenceParticipant referenceParticipant;
	private final ICodeLensParticipant codeLensParticipant;
	private final IHighlightingParticipant highlightingParticipant;
	private final IRenameParticipant renameParticipant;
	private XSDURIResolverExtension uiResolver;

	private ContentModelManager modelManager;

	public XSDPlugin() {
		completionParticipant = new XSDCompletionParticipant();
		definitionParticipant = new XSDDefinitionParticipant();
		diagnosticsParticipant = new XSDDiagnosticsParticipant();
		referenceParticipant = new XSDReferenceParticipant();
		codeLensParticipant = new XSDCodeLensParticipant();
		highlightingParticipant = new XSDHighlightingParticipant();
		renameParticipant = new XSDRenameParticipant();
	}

	@Override
	public void doSave(ISaveContext context) {
		String documentURI = context.getUri();
		DOMDocument document = context.getDocument(documentURI);
		if (DOMUtils.isXSD(document)) {
			context.collectDocumentToValidate(d -> {
				DOMDocument xml = context.getDocument(d.getDocumentURI());
				return modelManager.dependsOnGrammar(xml, context.getUri());
			});
		}
	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		// Register resolver
		uiResolver = new XSDURIResolverExtension(registry.getDocumentProvider());
		registry.getResolverExtensionManager().registerResolver(uiResolver);
		// register XSD content model provider
		ContentModelProvider modelProvider = new CMXSDContentModelProvider(registry.getResolverExtensionManager());
		modelManager = registry.getComponent(ContentModelManager.class);
		modelManager.registerModelProvider(modelProvider);
		// register completion, diagnostic participant
		registry.registerCompletionParticipant(completionParticipant);
		registry.registerDefinitionParticipant(definitionParticipant);
		registry.registerDiagnosticsParticipant(diagnosticsParticipant);
		registry.registerReferenceParticipant(referenceParticipant);
		registry.registerCodeLensParticipant(codeLensParticipant);
		registry.registerHighlightingParticipant(highlightingParticipant);
		registry.registerRenameParticipant(renameParticipant);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		registry.getResolverExtensionManager().unregisterResolver(uiResolver);
		registry.unregisterCompletionParticipant(completionParticipant);
		registry.unregisterDefinitionParticipant(definitionParticipant);
		registry.unregisterDiagnosticsParticipant(diagnosticsParticipant);
		registry.unregisterReferenceParticipant(referenceParticipant);
		registry.unregisterCodeLensParticipant(codeLensParticipant);
		registry.unregisterHighlightingParticipant(highlightingParticipant);
		registry.unregisterRenameParticipant(renameParticipant);
	}
}
