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
package org.eclipse.lemminx.extensions.contentmodel.participants.codeactions.missinggrammar;

import static org.eclipse.lemminx.extensions.contentmodel.participants.codeactions.nogrammarconstraints.NoGrammarConstraintsDataConstants.DATA_FILE_FIELD;

import org.eclipse.lemminx.commons.CodeActionFactory;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.generators.FileContentGeneratorManager;
import org.eclipse.lemminx.extensions.generators.FileContentGeneratorSettings;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionResolverRequest;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionResolvesParticipant;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Base class of the code action resolver participant used to generate the
 * missing grammar (DTD / XSD) which is declared in the XML as association (ex :
 * via xsi:noNamespaceSchemaLocation)
 * 
 * @author Angelo ZERR
 *
 */
public abstract class AbstractGenerateGrammarCodeActionResolver implements ICodeActionResolvesParticipant {

	@Override
	public final CodeAction resolveCodeAction(ICodeActionResolverRequest request, CancelChecker cancelChecker) {

		String missingFilePath = request.getDataProperty(DATA_FILE_FIELD);
		CodeAction resolved = request.getUnresolved();
		DOMDocument document = request.getDocument();
		SharedSettings sharedSettings = request.getSharedSettings();

		// Generate XSD / DTD from the DOM document
		FileContentGeneratorManager generator = request.getComponent(FileContentGeneratorManager.class);
		String schemaTemplate = generator.generate(document, sharedSettings, getFileContentGeneratorSettings(),
				cancelChecker);
		WorkspaceEdit edit = CodeActionFactory.createFileEdit(missingFilePath, schemaTemplate);
		resolved.setEdit(edit);
		return resolved;
	}

	/**
	 * Returns the grammar settings used to generate the missing grammar file (XSD,
	 * DTD).
	 * 
	 * @return the grammar settings used to generate the missing grammar file (XSD,
	 *         DTD).
	 */
	protected abstract FileContentGeneratorSettings getFileContentGeneratorSettings();
}
