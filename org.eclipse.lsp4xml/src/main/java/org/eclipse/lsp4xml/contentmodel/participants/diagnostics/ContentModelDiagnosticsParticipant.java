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

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.contentmodel.ContentModelPlugin;
import org.eclipse.lsp4xml.services.extensions.IDiagnosticsParticipant;

/**
 * Validate XML file with Xerces for syntax validation and XML Schema, DTD.
 *
 */
public class ContentModelDiagnosticsParticipant implements IDiagnosticsParticipant {

	private final ContentModelPlugin contentModelPlugin;

	public ContentModelDiagnosticsParticipant(ContentModelPlugin contentModelPlugin) {
		this.contentModelPlugin = contentModelPlugin;
	}

	@Override
	public void doDiagnostics(TextDocument document, List<Diagnostic> diagnostics, CancelChecker monitor) {
		XMLValidator.doDiagnostics(document, contentModelPlugin.getCatalogResolver(), diagnostics, monitor);
	}

}
