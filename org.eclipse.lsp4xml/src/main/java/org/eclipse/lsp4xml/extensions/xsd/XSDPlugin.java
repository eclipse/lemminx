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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelProvider;
import org.eclipse.lsp4xml.extensions.references.XMLReferences;
import org.eclipse.lsp4xml.extensions.references.XMLReferencesManager;
import org.eclipse.lsp4xml.extensions.xsd.contentmodel.CMXSDContentModelProvider;
import org.eclipse.lsp4xml.extensions.xsd.participants.XSDCompletionParticipant;
import org.eclipse.lsp4xml.extensions.xsd.participants.diagnostics.XSDDiagnosticsParticipant;
import org.eclipse.lsp4xml.services.extensions.ICompletionParticipant;
import org.eclipse.lsp4xml.services.extensions.IXMLExtension;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lsp4xml.services.extensions.save.ISaveContext;
import org.eclipse.lsp4xml.utils.DOMUtils;

/**
 * XSD plugin.
 */
public class XSDPlugin implements IXMLExtension {

	private static Logger LOGGER = Logger.getLogger(XSDPlugin.class.getName());

	private final ICompletionParticipant completionParticipant;

	private final IDiagnosticsParticipant diagnosticsParticipant;

	private XSDURIResolverExtension uiResolver;

	public XSDPlugin() {
		completionParticipant = new XSDCompletionParticipant();
		diagnosticsParticipant = new XSDDiagnosticsParticipant();
	}

	@Override
	public void doSave(ISaveContext context) {
		String documentURI = context.getUri();
		DOMDocument document = context.getDocument(documentURI);
		if (DOMUtils.isXSD(document)) {
			context.collectDocumentToValidate(d -> {
				DOMDocument xml = context.getDocument(d.getDocumentURI());
				return xml.usesSchema(context.getUri());
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
		ContentModelManager modelManager = registry.getComponent(ContentModelManager.class);
		modelManager.registerModelProvider(modelProvider);
		// register completion, diagnostic participant
		registry.registerCompletionParticipant(completionParticipant);
		registry.registerDiagnosticsParticipant(diagnosticsParticipant);
		// Manage links between xs element, complexType for having completion, definition, etc
		try {
			XMLReferences links = XMLReferencesManager.getInstance() //
					.referencesFor(XSDPlugin::match);
			links.from("//*:element/@type") //
					.to("//*[local-name()='complexType']/@name");
			links.from("//*:extension/@base") //
			.to("//*[local-name()='complexType']/@name");			
		} catch (XPathExpressionException e) {
			LOGGER.log(Level.SEVERE, "Error while registering XML references for XML Schema (xsd)", e);
		}
	}

	public static boolean match(DOMDocument document) {
		return DOMUtils.isXSD(document);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		registry.getResolverExtensionManager().unregisterResolver(uiResolver);
		registry.unregisterDiagnosticsParticipant(diagnosticsParticipant);
	}
}
