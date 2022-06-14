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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionResolvesParticipant;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lemminx.utils.StringUtils;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Code action support.
 *
 */
public class XMLCodeActions {

	private static final Logger LOGGER = Logger.getLogger(XMLCompletions.class.getName());

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLCodeActions(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public List<CodeAction> doCodeActions(CodeActionContext context, Range range, DOMDocument document,
			SharedSettings sharedSettings, CancelChecker cancelChecker) {
		cancelChecker.checkCanceled();

		List<CodeAction> codeActions = new ArrayList<>();
		List<Diagnostic> diagnostics = context.getDiagnostics();
		if (diagnostics != null) {
			for (Diagnostic diagnostic : context.getDiagnostics()) {
				for (ICodeActionParticipant codeActionParticipant : extensionsRegistry.getCodeActionsParticipants()) {
					try {
						cancelChecker.checkCanceled();
						CodeActionRequest request = new CodeActionRequest(diagnostic, range, document,
								extensionsRegistry, sharedSettings);
						codeActionParticipant.doCodeAction(request, codeActions, cancelChecker);
					} catch (CancellationException e) {
						throw e;
					} catch (Exception e) {
						LOGGER.log(Level.SEVERE, "Error while processing code actions for the participant '"
								+ codeActionParticipant.getClass().getName() + "'.", e);
					}
				}
			}
		}

		cancelChecker.checkCanceled();
		return codeActions;
	}

	public CodeAction resolveCodeAction(CodeAction unresolved, DOMDocument document, SharedSettings sharedSettings,
			CancelChecker cancelChecker) {
		ResolveCodeActionRequest request = new ResolveCodeActionRequest(unresolved, document, extensionsRegistry,
				sharedSettings);
		String participantId = request.getParticipantId();
		if (StringUtils.isEmpty(participantId)) {
			return null;
		}
		for (ICodeActionParticipant codeActionParticipant : extensionsRegistry.getCodeActionsParticipants()) {
			try {
				cancelChecker.checkCanceled();
				ICodeActionResolvesParticipant resolveCodeActionParticipant = codeActionParticipant
						.getResolveCodeActionParticipant(participantId);
				if (resolveCodeActionParticipant != null) {
					return resolveCodeActionParticipant.resolveCodeAction(request, cancelChecker);
				}
			} catch (CancellationException e) {
				throw e;
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error while processing resolve code action for the participant '"
						+ codeActionParticipant.getClass().getName() + "'.", e);
			}
		}
		return null;
	}
}
