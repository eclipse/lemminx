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
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.ContentModelPlugin;
import org.eclipse.lsp4xml.services.extensions.diagnostics.IDiagnosticsParticipant;

/**
 * Validate XML files with Xerces for general SYNTAX validation and XML Schema, DTD.
 *
 */
public class ContentModelDiagnosticsParticipant implements IDiagnosticsParticipant {

	private final ContentModelPlugin contentModelPlugin;

	public ContentModelDiagnosticsParticipant(ContentModelPlugin contentModelPlugin) {
		this.contentModelPlugin = contentModelPlugin;
	}

	@Override
	public void doDiagnostics(DOMDocument xmlDocument, List<Diagnostic> diagnostics, CancelChecker monitor) {
		if (xmlDocument.isDTD()) {
			// Don't validate DTD with XML validator
			return;
		}
		// Get entity resolver (XML catalog resolver, XML schema from the file
		// associations settings., ...)
		XMLEntityResolver entityResolver = xmlDocument.getResolverExtensionManager();
		// Process validation
		XMLValidator.doDiagnostics(xmlDocument, entityResolver, diagnostics,
				contentModelPlugin.getContentModelSettings(), monitor);
	}

}
