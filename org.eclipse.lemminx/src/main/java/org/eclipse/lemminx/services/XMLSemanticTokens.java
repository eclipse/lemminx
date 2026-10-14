/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services;

import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lemminx.services.extensions.semantictokens.ISemanticTokensParticipant;
import org.eclipse.lemminx.services.extensions.semantictokens.ISemanticTokensRequest;
import org.eclipse.lemminx.services.extensions.semantictokens.SemanticTokensData;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * XML semantic tokens support.
 */
class XMLSemanticTokens {

	private static final Logger LOGGER = Logger.getLogger(XMLSemanticTokens.class.getName());

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLSemanticTokens(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public SemanticTokens findSemanticTokens(DOMDocument xmlDocument, CancelChecker cancelChecker) {
		cancelChecker.checkCanceled();

		SemanticTokensData builder = new SemanticTokensData(xmlDocument);
		ISemanticTokensRequest request = new SemanticTokensRequest(xmlDocument, extensionsRegistry);
		for (ISemanticTokensParticipant participant : extensionsRegistry.getSemanticTokensParticipants()) {
			try {
				participant.findSemanticTokens(request, builder, cancelChecker);
			} catch (CancellationException e) {
				throw e;
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error while processing semantic tokens for the participant '"
						+ participant.getClass().getName() + "'.", e);
			}
		}

		cancelChecker.checkCanceled();

		return builder.build();
	}

	private static class SemanticTokensRequest implements ISemanticTokensRequest {

		private final DOMDocument document;
		private final XMLExtensionsRegistry extensionsRegistry;

		public SemanticTokensRequest(DOMDocument document, XMLExtensionsRegistry extensionsRegistry) {
			this.document = document;
			this.extensionsRegistry = extensionsRegistry;
		}

		@Override
		public DOMDocument getDocument() {
			return document;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getComponent(Class clazz) {
			return (T) extensionsRegistry.getComponent(clazz);
		}
	}
}
