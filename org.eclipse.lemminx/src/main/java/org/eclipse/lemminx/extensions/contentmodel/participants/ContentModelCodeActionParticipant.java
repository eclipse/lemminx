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
package org.eclipse.lemminx.extensions.contentmodel.participants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.lemminx.extensions.xsd.participants.XSDErrorCode;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionRequest;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionResolvesParticipant;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Extension to support XML code actions based on content model (XML Schema
 * completion, etc)
 */
public class ContentModelCodeActionParticipant implements ICodeActionParticipant {

	private final Map<String, ICodeActionParticipant> codeActionParticipants;

	public ContentModelCodeActionParticipant() {
		super();
		codeActionParticipants = new HashMap<>();
	}

	@Override
	public void doCodeAction(ICodeActionRequest request, List<CodeAction> codeActions, CancelChecker cancelChecker) {
		Diagnostic diagnostic = request.getDiagnostic();
		if (diagnostic == null || diagnostic.getCode() == null || !diagnostic.getCode().isLeft()) {
			return;
		}
		SharedSettings sharedSettings = request.getSharedSettings();
		registerCodeActionsIfNeeded(sharedSettings);
		ICodeActionParticipant participant = codeActionParticipants.get(diagnostic.getCode().getLeft());
		if (participant != null) {
			participant.doCodeAction(request, codeActions, cancelChecker);
		}
	}

	@Override
	public ICodeActionResolvesParticipant getResolveCodeActionParticipant(String participantId) {
		// Loop for each code action participant to retrieve the proper resolver with
		// the given participant ID.
		for (ICodeActionParticipant participant : codeActionParticipants.values()) {
			ICodeActionResolvesParticipant resolveParticipant = participant
					.getResolveCodeActionParticipant(participantId);
			if (resolveParticipant != null) {
				return resolveParticipant;
			}
		}
		return null;
	}

	/**
	 * Register code action if needed.
	 * 
	 * @param sharedSettings the shared settings.
	 */
	private void registerCodeActionsIfNeeded(SharedSettings sharedSettings) {
		if (codeActionParticipants.isEmpty()) {
			synchronized (codeActionParticipants) {
				if (!codeActionParticipants.isEmpty()) {
					return;
				}
				XMLSyntaxErrorCode.registerCodeActionParticipants(codeActionParticipants, sharedSettings);
				DTDErrorCode.registerCodeActionParticipants(codeActionParticipants, sharedSettings);
				XMLSchemaErrorCode.registerCodeActionParticipants(codeActionParticipants, sharedSettings);
				XSDErrorCode.registerCodeActionParticipants(codeActionParticipants);
				ExternalResourceErrorCode.registerCodeActionParticipants(codeActionParticipants);
			}
		}
	}
}
