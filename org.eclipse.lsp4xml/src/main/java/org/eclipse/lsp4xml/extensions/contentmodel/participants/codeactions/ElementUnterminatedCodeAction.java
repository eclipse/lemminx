/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.contentmodel.participants.codeactions;

import java.util.List;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.CodeActionFactory;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.services.extensions.ICodeActionParticipant;
import org.eclipse.lsp4xml.services.extensions.IComponentProvider;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;

/**
 * Code action to fix ElementUnterminated error.
 *
 */
public class ElementUnterminatedCodeAction implements ICodeActionParticipant {

	@Override
	public void doCodeAction(Diagnostic diagnostic, Range range, DOMDocument document, List<CodeAction> codeActions,
			XMLFormattingOptions formattingSettings, IComponentProvider componentProvider) {
		Range diagnosticRange = diagnostic.getRange();
		// Close with '/>
		CodeAction autoCloseAction = CodeActionFactory.insert("Close with '/>'", diagnosticRange.getEnd(), "/>",
				document.getTextDocument(), diagnostic);
		codeActions.add(autoCloseAction);

		// Close with '>
		CodeAction closeAction = CodeActionFactory.insert("Close with '>'", diagnosticRange.getEnd(), ">",
				document.getTextDocument(), diagnostic);
		codeActions.add(closeAction);

		// Close with '$tag>
		try {
			int offset = document.offsetAt(range.getStart());
			DOMNode node = document.findNodeAt(offset);
			if (node != null && node.isElement()) {
				String tagName = ((DOMElement) node).getTagName();
				if (tagName != null) {
					String insertText = "></" + tagName + ">";
					CodeAction closeEndTagAction = CodeActionFactory.insert("Close with '" + insertText + "'",
							diagnosticRange.getEnd(), insertText, document.getTextDocument(), diagnostic);
					codeActions.add(closeEndTagAction);
				}
			}
		} catch (BadLocationException e) {
			// do nothing
		}
	}

}
