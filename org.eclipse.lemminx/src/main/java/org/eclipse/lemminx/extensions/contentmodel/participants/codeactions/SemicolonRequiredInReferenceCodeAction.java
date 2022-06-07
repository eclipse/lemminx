/**
 *  Copyright (c) 2022 Red Hat Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lemminx.extensions.contentmodel.participants.codeactions;

import java.util.List;

import org.eclipse.lemminx.commons.CodeActionFactory;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.IComponentProvider;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;

/**
 * Code action to fix SemicolonRequiredInReference error.
 *
 *	 Given this XML:
 *	    <root>
 *	      &mdash	 -> Error: The reference to entity "mdash" must end with the ';' delimiter
 *	    </root>
 *
 *	 To fix the error, the code action will suggest adding ';'
 *	    <root>
 *	      &mdash;
 *	    </root>
 */
public class SemicolonRequiredInReferenceCodeAction implements ICodeActionParticipant {

	@Override
	public void doCodeAction(Diagnostic diagnostic, Range range, DOMDocument document, List<CodeAction> codeActions,
			SharedSettings sharedSettings, IComponentProvider componentProvider) {
		Range diagnosticRange = diagnostic.getRange();

		// Close with ';'
		CodeAction closeAction = CodeActionFactory.insert("Close with ';'", diagnosticRange.getEnd(), ";",
				document.getTextDocument(), diagnostic);
		codeActions.add(closeAction);
	}

}