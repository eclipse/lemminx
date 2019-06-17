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
package org.eclipse.lsp4xml.extensions.contentmodel.participants.diagnostics;

import java.util.List;

import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lsp4xml.utils.DOMUtils;

/**
 * Validate XML files with Xerces for general SYNTAX validation and XML Schema,
 * DTD.
 *
 */
public class ContentModelDiagnosticsParticipant implements IDiagnosticsParticipant {
	private final XMLExtensionsRegistry registry;

	public ContentModelDiagnosticsParticipant(XMLExtensionsRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void doDiagnostics(DOMDocument xmlDocument, List<Diagnostic> diagnostics, CancelChecker monitor) {
		if (xmlDocument.isDTD() || DOMUtils.isXSD(xmlDocument)) {
			// Don't validate DTD / XML Schema with XML validator
			return;
		}
		// Get entity resolver (XML catalog resolver, XML schema from the file
		// associations settings., ...)
		XMLEntityResolver entityResolver = xmlDocument.getResolverExtensionManager();
		// Process validation
		ContentModelManager manager = registry.getComponent(ContentModelManager.class);
		XMLValidator.doDiagnostics(xmlDocument, entityResolver, diagnostics, manager.getSettings(), monitor);
	}

}
