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
package org.eclipse.lsp4xml.extensions.contentmodel.participants.diagnostics;

import java.util.List;

import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.services.extensions.IDiagnosticsParticipant;
import org.eclipse.lsp4xml.uriresolver.IExternalSchemaLocationProvider;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtensionManager;

/**
 * Validate XML file with Xerces for syntax validation and XML Schema, DTD.
 *
 */
public class ContentModelDiagnosticsParticipant implements IDiagnosticsParticipant {

	@Override
	public void doDiagnostics(XMLDocument xmlDocument, List<Diagnostic> diagnostics, CancelChecker monitor) {
		// Get entity resolver (XML catalog resolver, XML schema from the file
		// associations settings., ...)
		XMLEntityResolver entityResolver = URIResolverExtensionManager.getInstance();
		IExternalSchemaLocationProvider externalSchemaLocationProvider = URIResolverExtensionManager.getInstance();
		// Process validation
		XMLValidator.doDiagnostics(xmlDocument, entityResolver, externalSchemaLocationProvider, diagnostics, monitor);
	}

}
