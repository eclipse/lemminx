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
package org.eclipse.lemminx.extensions.xsd.participants.diagnostics;

import java.util.List;

import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.extensions.contentmodel.participants.diagnostics.XMLValidator;
import org.eclipse.lemminx.extensions.contentmodel.settings.SchemaEnabled;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLSchemaSettings;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.extensions.xerces.LSPXMLEntityResolver;
import org.eclipse.lemminx.extensions.xsd.XSDPlugin;
import org.eclipse.lemminx.services.extensions.diagnostics.DiagnosticsResult;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.utils.DOMUtils;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Validate XSD file with Xerces.
 *
 */
public class XSDDiagnosticsParticipant implements IDiagnosticsParticipant {

	private final XSDPlugin xsdPlugin;

	public XSDDiagnosticsParticipant(XSDPlugin xsdPlugin) {
		this.xsdPlugin = xsdPlugin;
	}

	@Override
	public void doDiagnostics(DOMDocument xmlDocument, List<Diagnostic> diagnostics,
			XMLValidationSettings validationSettings, CancelChecker monitor) {
		if (!DOMUtils.isXSD(xmlDocument)) {
			// Don't use the XSD validator, if the XML document is not a XML Schema.
			return;
		}

		// Get entity resolver (XML catalog resolver, XML schema from the file
		// associations settings., ...)
		XMLEntityResolver entityResolver = xmlDocument.getResolverExtensionManager();
		LSPXMLEntityResolver entityResolverWrapper = new LSPXMLEntityResolver(entityResolver,
				(DiagnosticsResult) diagnostics);
		ContentModelManager contentModelManager = xsdPlugin.getContentModelManager();
		if (!isSchemaEnabled(validationSettings)) {
			// Validate only XML syntax for XSD
			// Process validation
			XMLValidator.doDiagnostics(xmlDocument, entityResolverWrapper, diagnostics, validationSettings,
					contentModelManager, monitor);
			return;
		}

		// Process XSD validation
		XSDValidator.doDiagnostics(xmlDocument, entityResolverWrapper, diagnostics, validationSettings,
				xsdPlugin.getContentModelManager(), monitor);
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
