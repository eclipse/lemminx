/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package org.eclipse.lemminx.extensions.catalog;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.utils.DOMUtils;
import org.eclipse.lemminx.utils.FilesUtils;
import org.eclipse.lemminx.utils.URIUtils;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Validate XML catalog.
 *
 */
public class XMLCatalogDiagnosticsParticipant implements IDiagnosticsParticipant {

	private static final String ERROR_STRING = "The file ''{0}'' cannot be found.";

	@Override
	public void doDiagnostics(DOMDocument xmlDocument, List<Diagnostic> diagnostics,
			XMLValidationSettings validationSettings, CancelChecker monitor) {
		if (!DOMUtils.isCatalog(xmlDocument)) {
			return;
		}

		for (CatalogEntry catalogEntry : CatalogUtils.getCatalogEntries(xmlDocument)) {
			// CatalogUtils.getResolvedLocation() always returns path with 'file://' scheme,
			// appending it in the case when original URI does not start with 'file://'.
			// Ex: originalURI ="foo/bar.xsd" -> path ="file://foo/bar.xsd"
			String path = CatalogUtils.getResolvedLocation(xmlDocument, catalogEntry);
			if (!FilesUtils.isValidPath(FilesUtils.getPath(path)) && URIUtils.isFileResource(path)) {
				Range range = XMLPositionUtility.selectValueWithoutQuote(catalogEntry.getLinkRange());
				String msg = MessageFormat.format(ERROR_STRING, catalogEntry.getResolvedURI());

				diagnostics
						.add(new Diagnostic(range, msg, DiagnosticSeverity.Error, xmlDocument.getDocumentURI(),
								XMLCatalogErrorCode.catalog_uri.name()));
			}
		}
	}
}
