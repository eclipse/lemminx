/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.relaxng.grammar.rng;

import java.util.List;

import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.extensions.contentmodel.participants.diagnostics.XMLValidator;
import org.eclipse.lemminx.extensions.contentmodel.settings.SchemaEnabled;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLSchemaSettings;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.extensions.relaxng.RelaxNGPlugin;
import org.eclipse.lemminx.extensions.xerces.LSPXMLEntityResolver;
import org.eclipse.lemminx.services.extensions.diagnostics.DiagnosticsResult;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.utils.DOMUtils;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Validate RNG file with Jing.
 *
 */
public class RNGDiagnosticsParticipant implements IDiagnosticsParticipant {

	private final RelaxNGPlugin rngPlugin;

	public RNGDiagnosticsParticipant(RelaxNGPlugin rngPlugin) {
		this.rngPlugin = rngPlugin;
	}

	@Override
	public void doDiagnostics(DOMDocument xmlDocument, List<Diagnostic> diagnostics,
			XMLValidationSettings validationSettings, CancelChecker monitor) {
		if (!DOMUtils.isRelaxNGXMLSyntax(xmlDocument)) {
			// Don't use the RNG validator, if the XML document is not a RNG Schema.
			return;
		}

		// Get entity resolver (XML catalog resolver, XML schema from the file
		// associations settings., ...)
		XMLEntityResolver entityResolver = xmlDocument.getResolverExtensionManager();
		LSPXMLEntityResolver entityResolverWrapper = new LSPXMLEntityResolver(entityResolver,
				(DiagnosticsResult) diagnostics);
		ContentModelManager contentModelManager = rngPlugin.getContentModelManager();
		if (!isSchemaEnabled(validationSettings)) {
			// Validate only XML syntax for RNG
			// Process validation
			XMLValidator.doDiagnostics(xmlDocument, entityResolverWrapper, diagnostics, validationSettings,
					contentModelManager, monitor);
			return;
		}

		// Process RNG validation
		RNGValidator.doDiagnostics(xmlDocument, entityResolverWrapper, diagnostics, validationSettings,
				rngPlugin.getContentModelManager(), monitor);
	}

	private static boolean isSchemaEnabled(XMLValidationSettings validationSettings) {
		if (validationSettings == null) {
			return true;
		}
		XMLSchemaSettings schemaSettings = validationSettings.getSchema();
		if (schemaSettings == null) {
			return true;
		}
		return !SchemaEnabled.never.equals(schemaSettings.getEnabled());
	}

}
