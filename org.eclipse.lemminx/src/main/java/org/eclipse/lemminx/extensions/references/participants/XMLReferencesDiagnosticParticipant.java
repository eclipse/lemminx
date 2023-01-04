/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.references.participants;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.extensions.references.XMLReferencesPlugin;
import org.eclipse.lemminx.extensions.references.search.ReferenceLink;
import org.eclipse.lemminx.extensions.references.search.SearchEngine;
import org.eclipse.lemminx.extensions.references.search.SearchNode;
import org.eclipse.lemminx.extensions.references.search.SearchNode.ValidationStatus;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * XML references diagnostics which reports undefined references.
 *
 */
public class XMLReferencesDiagnosticParticipant implements IDiagnosticsParticipant {

	private final static String UNDEFINED_REFERENCE_MESSSAGE = "Undefined reference ''{0}'': nothing that matches the expression ''{1}'' defines ''{2}''.";

	private final static String INVALID_PREFIX_MESSSAGE = "Invalid reference ''{0}'': references to declarations that match the expression ''{1}'' require the ''{2}'' prefix.";

	private final XMLReferencesPlugin plugin;

	public XMLReferencesDiagnosticParticipant(XMLReferencesPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void doDiagnostics(DOMDocument document, List<Diagnostic> diagnostics,
			XMLValidationSettings validationSettings, CancelChecker cancelChecker) {
		Collection<ReferenceLink> links = SearchEngine.getInstance().searchLinks(document,
				plugin.getReferencesSettings(),
				cancelChecker);
		if (links.isEmpty()) {
			return;
		}

		for (ReferenceLink link : links) {
			for (SearchNode from : link.getFroms()) {
				// Validate the syntax of from node.
				if (!from.isValid()) {
					if (from.getValidationStatus() == ValidationStatus.INVALID_PREFIX) {
						// Invalid prefix error
						Range range = XMLPositionUtility.createRange(from);
						String message = MessageFormat.format(INVALID_PREFIX_MESSSAGE, from.getValue(null),
								link.getExpression().getTo(), link.getExpression().getPrefix());
						Diagnostic diagnostic = new Diagnostic(range, message, DiagnosticSeverity.Warning, "xml",
								XMLReferencesErrorCode.InvalidPrefix.getCode());
						diagnostics.add(diagnostic);
					}
				} else {
					boolean validReference = false;
					for (SearchNode to : link.getTos()) {
						if (from.matchesValue(to)) {
							validReference = true;
							break;
						}
					}
					if (!validReference) {
						// Undefined reference error
						Range range = XMLPositionUtility.createRange(from);
						String value = from.getValue(null);
						String valueWithoutPrefix = value;
						if (from.getPrefix() != null) {
							valueWithoutPrefix = value.substring(from.getPrefix().length(), value.length());
						}
						String message = MessageFormat.format(UNDEFINED_REFERENCE_MESSSAGE, value,
								link.getExpression().getTo(), valueWithoutPrefix);
						Diagnostic diagnostic = new Diagnostic(range, message, DiagnosticSeverity.Warning, "xml",
								XMLReferencesErrorCode.UndefinedReference.getCode());
						diagnostics.add(diagnostic);
					}
				}
			}
		}

	}

}
