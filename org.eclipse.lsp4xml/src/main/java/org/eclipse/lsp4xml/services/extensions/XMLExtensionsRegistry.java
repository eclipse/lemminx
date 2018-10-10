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
package org.eclipse.lsp4xml.services.extensions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4xml.services.IXMLDocumentProvider;

/**
 * XML extensions registry.
 *
 */
public class XMLExtensionsRegistry {

	private static final Logger LOGGER = Logger.getLogger(XMLExtensionsRegistry.class.getName());

	private final Collection<IXMLExtension> extensions;
	private final List<ICompletionParticipant> completionParticipants;
	private final List<IHoverParticipant> hoverParticipants;
	private final List<IDiagnosticsParticipant> diagnosticsParticipants;
	private final List<ICodeActionParticipant> codeActionsParticipants;
	private final List<IDocumentLinkParticipant> documentLinkParticipants;

	private IXMLDocumentProvider documentProvider;

	private InitializeParams params;

	private Object settings;

	private boolean initialized;

	public XMLExtensionsRegistry() {
		extensions = new ArrayList<>();
		completionParticipants = new ArrayList<>();
		hoverParticipants = new ArrayList<>();
		diagnosticsParticipants = new ArrayList<>();
		codeActionsParticipants = new ArrayList<>();
		documentLinkParticipants = new ArrayList<>();
	}

	public void initializeParams(InitializeParams params) {
		if (initialized) {
			extensions.stream().forEach(extension -> extension.start(params, this));
		} else {
			this.params = params;
		}
	}

	public void updateSettings(Object settings) {
		if (initialized) {
			extensions.stream().forEach(extension -> extension.updateSettings(settings));
		} else {
			this.settings = settings;
		}
	}

	public Collection<IXMLExtension> getExtensions() {
		initializeIfNeeded();
		return extensions;
	}

	public Collection<ICompletionParticipant> getCompletionParticipants() {
		initializeIfNeeded();
		return completionParticipants;
	}

	public Collection<IHoverParticipant> getHoverParticipants() {
		initializeIfNeeded();
		return hoverParticipants;
	}

	public Collection<IDiagnosticsParticipant> getDiagnosticsParticipants() {
		initializeIfNeeded();
		return diagnosticsParticipants;
	}

	public List<ICodeActionParticipant> getCodeActionsParticipants() {
		initializeIfNeeded();
		return codeActionsParticipants;
	}

	public Collection<IDocumentLinkParticipant> getDocumentLinkParticipants() {
		initializeIfNeeded();
		return documentLinkParticipants;
	}

	private void initializeIfNeeded() {
		if (initialized) {
			return;
		}
		initialize();
	}

	private synchronized void initialize() {
		if (initialized) {
			return;
		}
		ServiceLoader<IXMLExtension> extensions = ServiceLoader.load(IXMLExtension.class);
		extensions.forEach(extension -> {
			registerExtension(extension);
		});
		initialized = true;
	}

	void registerExtension(IXMLExtension extension) {
		try {
			extensions.add(extension);
			extension.start(params, this);
			extension.updateSettings(settings);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while initializing extension <" + extension.getClass().getName() + ">", e);
		}
	}

	void unregisterExtension(IXMLExtension extension) {
		extensions.remove(extension);
		extension.stop(this);
	}

	public void registerCompletionParticipant(ICompletionParticipant completionParticipant) {
		completionParticipants.add(completionParticipant);
	}

	public void unregisterCompletionParticipant(ICompletionParticipant completionParticipant) {
		completionParticipants.add(completionParticipant);
	}

	public void registerHoverParticipant(IHoverParticipant hoverParticipant) {
		hoverParticipants.add(hoverParticipant);
	}

	public void unregisterHoverParticipant(IHoverParticipant hoverParticipant) {
		hoverParticipants.add(hoverParticipant);
	}

	public void registerDiagnosticsParticipant(IDiagnosticsParticipant diagnosticsParticipant) {
		diagnosticsParticipants.add(diagnosticsParticipant);
	}

	public void unregisterDiagnosticsParticipant(IDiagnosticsParticipant diagnosticsParticipant) {
		diagnosticsParticipants.add(diagnosticsParticipant);
	}

	public void registerCodeActionParticipant(ICodeActionParticipant codeActionsParticipant) {
		codeActionsParticipants.add(codeActionsParticipant);
	}

	public void unregisterCodeActionParticipant(ICodeActionParticipant codeActionsParticipant) {
		codeActionsParticipants.add(codeActionsParticipant);
	}

	public void registerDocumentLinkParticipant(IDocumentLinkParticipant documentLinkParticipant) {
		documentLinkParticipants.add(documentLinkParticipant);
	}

	public void unregisterDocumentLinkParticipant(IDocumentLinkParticipant documentLinkParticipant) {
		documentLinkParticipants.add(documentLinkParticipant);
	}

	/**
	 * Returns the XML Document provider and null otherwise.
	 * 
	 * @return the XML Document provider and null otherwise.
	 */
	public IXMLDocumentProvider getDocumentProvider() {
		return documentProvider;
	}

	/**
	 * Set the XML Document provider
	 * 
	 * @param documentProvider XML Document provider
	 */
	public void setDocumentProvider(IXMLDocumentProvider documentProvider) {
		this.documentProvider = documentProvider;
	}
}