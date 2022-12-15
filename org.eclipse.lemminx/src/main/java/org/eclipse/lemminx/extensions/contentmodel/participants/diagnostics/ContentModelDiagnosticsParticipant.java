/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.extensions.contentmodel.participants.diagnostics;

import java.util.List;
import java.util.Map;

import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.ContentModelPlugin;
import org.eclipse.lemminx.extensions.contentmodel.participants.codeactions.DownloadDisabledResourceCodeAction;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.extensions.xerces.LSPXMLEntityResolver;
import org.eclipse.lemminx.services.extensions.diagnostics.DiagnosticsResult;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.utils.DOMUtils;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Validate XML files with Xerces for general SYNTAX validation and XML Schema,
 * DTD.
 *
 */
public class ContentModelDiagnosticsParticipant implements IDiagnosticsParticipant {

	private final ContentModelPlugin contentModelPlugin;

	public ContentModelDiagnosticsParticipant(ContentModelPlugin contentModelPlugin) {
		this.contentModelPlugin = contentModelPlugin;
	}

	@Override
	public void doDiagnostics(DOMDocument xmlDocument, List<Diagnostic> diagnostics,
			XMLValidationSettings validationSettings, CancelChecker monitor) {
		downloadExternalResourcesIfNeeded(diagnostics);
		if (xmlDocument.isDTD() || DOMUtils.isXSD(xmlDocument) || DOMUtils.isRelaxNG(xmlDocument)) {
			// Don't validate DTD / XML Schema with XML validator
			return;
		}
		// Get entity resolver (XML catalog resolver, XML schema from the file
		// associations settings., ...)
		XMLEntityResolver entityResolver = xmlDocument.getResolverExtensionManager();
		LSPXMLEntityResolver entityResolverWrapper = new LSPXMLEntityResolver(entityResolver,
				(DiagnosticsResult) diagnostics);

		// Process validation
		XMLValidator.doDiagnostics(xmlDocument, entityResolverWrapper, diagnostics, validationSettings,
				contentModelPlugin.getContentModelManager(), monitor);

	}

	private void downloadExternalResourcesIfNeeded(List<Diagnostic> diagnostics) {
		Map<String, Object> validationArgs = ((DiagnosticsResult) diagnostics).getValidationArgs();
		String url = DownloadDisabledResourceCodeAction.getUrlToForceToDownload(validationArgs);
		if (url != null) {
			contentModelPlugin.getContentModelManager().forceDownloadExternalResource(url);
		}
	}

}
