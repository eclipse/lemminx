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
package org.eclipse.lemminx.extensions.contentmodel.participants.codeactions.nogrammarconstraints;

import static org.eclipse.lemminx.extensions.contentmodel.participants.codeactions.nogrammarconstraints.NoGrammarConstraintsCodeAction.getFileName;
import static org.eclipse.lemminx.extensions.contentmodel.participants.codeactions.nogrammarconstraints.NoGrammarConstraintsDataConstants.DATA_FILE_FIELD;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.commons.CodeActionFactory;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.generators.FileContentGeneratorManager;
import org.eclipse.lemminx.extensions.generators.FileContentGeneratorSettings;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionResolvesParticipant;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionResolverRequest;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * Abstract class to:
 * 
 * <ul>
 * <li>insert the grammar association in the XML (ex :
 * xsi:noNamespaceSchemaLocation)</li>
 * <li>generate the grammar XSD/DTD file</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public abstract class AbstractGenerateGrammarAndAssociationResolveCodeActionParticipant
		implements ICodeActionResolvesParticipant {

	private static final Logger LOGGER = Logger
			.getLogger(AbstractGenerateGrammarAndAssociationResolveCodeActionParticipant.class.getName());

	@Override
	public final CodeAction resolveCodeAction(ICodeActionResolverRequest request, CancelChecker cancelChecker) {
		try {
			DOMDocument document = request.getDocument();
			CodeAction resolved = request.getUnresolved();
			String grammarURI = request.getDataProperty(DATA_FILE_FIELD);
			String grammarFileName = getFileName(grammarURI);
			SharedSettings sharedSettings = request.getSharedSettings();

			// Generate edit to insert grammar association in the DOM document (ex :
			// xsi:noNamespaceShemaLocation)
			TextDocumentEdit noNamespaceSchemaLocationEdit = createFileEdit(grammarFileName, document, sharedSettings);

			// Generate grammar content
			FileContentGeneratorManager generator = request.getComponent(FileContentGeneratorManager.class);
			String schemaTemplate = generator.generate(document, sharedSettings, getFileContentGeneratorSettings(),
					cancelChecker);

			// Update the unresolved code action
			updateGrammarFileAndBindIt(resolved, grammarURI, schemaTemplate, noNamespaceSchemaLocationEdit);
			return resolved;
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In NoGrammarConstraintsCodeAction position error", e);
			return null;
		}
	}

	private static CodeAction updateGrammarFileAndBindIt(CodeAction codeAction, String grammarURI,
			String grammarContent, TextDocumentEdit boundEdit) {
		WorkspaceEdit createAndAddContentEdit = CodeActionFactory.createFileEdit(grammarURI, grammarContent);
		codeAction.setEdit(createAndAddContentEdit);
		codeAction.getEdit().getDocumentChanges().add(Either.forLeft(boundEdit));
		return codeAction;
	}

	protected abstract TextDocumentEdit createFileEdit(String grammarFileName, DOMDocument document,
			SharedSettings sharedSettings) throws BadLocationException;

	protected abstract FileContentGeneratorSettings getFileContentGeneratorSettings();
}
