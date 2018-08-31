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
package org.eclipse.lsp4xml.contentmodel.participants.diagnostics;

import java.util.List;

import javax.xml.validation.Schema;

import org.apache.xml.resolver.tools.CatalogResolver;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.services.extensions.IDiagnosticsParticipant;
import org.xml.sax.SAXException;

/**
 * Validate XML file with Xerces for syntax validation and XML Schema, DTD.
 *
 */
public class ContentModelDiagnosticsParticipant implements IDiagnosticsParticipant {

	@Override
	public void doDiagnostics(TextDocument document, List<Diagnostic> diagnostics, CancelChecker monitor) {
		// Try to find XML schema from the file associations settings.
		Schema schema = null;
		try {
			schema = ContentModelManager.getInstance().findSchemaFromFileAssociations(document.getUri());
		} catch (SAXException e) {
			e.printStackTrace();
		}
		// Get XML catalog resolver
		CatalogResolver catalogResolver = ContentModelManager.getInstance().getCatalogResolver();
		// Process validation
		XMLValidator.doDiagnostics(document, schema, catalogResolver, diagnostics, monitor);
	}

}
