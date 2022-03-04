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
package org.eclipse.lemminx.services;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lemminx.services.extensions.diagnostics.DiagnosticsResult;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.uriresolver.CacheResourceDownloadingException;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * XML diagnostics support.
 *
 */
class XMLDiagnostics {

	private final XMLExtensionsRegistry extensionsRegistry;
	private static final Logger LOGGER = Logger.getLogger(XMLDiagnostics.class.getName());

	public XMLDiagnostics(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public DiagnosticsResult doDiagnostics(DOMDocument xmlDocument, XMLValidationSettings validationSettings,
			Map<String, Object> validationArgs, CancelChecker cancelChecker) {
		if (validationSettings != null && !validationSettings.isEnabled()) {
			return DiagnosticsResult.EMPTY;
		}
		DiagnosticsResult diagnostics = new DiagnosticsResult(validationArgs);
		doExtensionsDiagnostics(xmlDocument, diagnostics, validationSettings, validationArgs, cancelChecker);
		return diagnostics;
	}

	/**
	 * Do validation with extension (XML Schema, etc)
	 *
	 * @param xmlDocument
	 * @param diagnostics
	 * @param validationSettings
	 * @param validationArgs
	 * @param monitor
	 */
	private void doExtensionsDiagnostics(DOMDocument xmlDocument, List<Diagnostic> diagnostics,
			XMLValidationSettings validationSettings, Map<String, Object> validationArgs, CancelChecker monitor) {
		for (IDiagnosticsParticipant diagnosticsParticipant : extensionsRegistry.getDiagnosticsParticipants()) {
			monitor.checkCanceled();
			try {
				diagnosticsParticipant.doDiagnostics(xmlDocument, diagnostics, validationSettings, monitor);
			} catch (CancellationException | CacheResourceDownloadingException e) {
				throw e;
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error while processing diagnostics for the participant '"
						+ diagnosticsParticipant.getClass().getName() + "'.", e);
			}
		}
	}

}
