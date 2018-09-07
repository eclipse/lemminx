/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.contentmodel.participants.codeactions;

import java.util.List;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.CodeActionFactory;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.services.extensions.ICodeActionParticipant;

/**
 * Code action to fix ElementUnterminated error.
 *
 */
public class ElementUnterminatedCodeAction implements ICodeActionParticipant {

	@Override
	public void doCodeAction(Diagnostic diagnostic, Range range, XMLDocument document, List<CodeAction> codeActions) {
		// Add close '>'
		CodeAction closeAction = CodeActionFactory.insert("Close element", range, ">", document.getTextDocument(),
				diagnostic);
		codeActions.add(closeAction);

		// Add Auto close '/>'
		CodeAction autoCloseAction = CodeActionFactory.insert("Auto-close element", range, "/>",
				document.getTextDocument(), diagnostic);
		codeActions.add(autoCloseAction);
	}

}
