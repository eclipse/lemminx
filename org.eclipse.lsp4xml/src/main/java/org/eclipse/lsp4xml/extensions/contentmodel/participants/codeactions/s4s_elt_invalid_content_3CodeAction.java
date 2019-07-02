/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

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
import org.eclipse.lsp4xml.utils.XMLPositionUtility;

/**
 * Code action to fix cvc-attribute-3 error.
 *
 */
public class s4s_elt_invalid_content_3CodeAction implements ICodeActionParticipant {

	@Override
	public void doCodeAction(Diagnostic diagnostic, Range range, DOMDocument document, List<CodeAction> codeActions,
			XMLFormattingOptions formattingSettings, IComponentProvider componentProvider) {
		try {
			int offset = document.offsetAt(range.getStart());
			DOMNode node = document.findNodeAt(offset);
			if (node != null && node.isElement()) {
				DOMElement element = (DOMElement) node;
				int startOffset = element.getStartTagOpenOffset();
				int endOffset;
				if (element.isSelfClosed()) {
					endOffset = element.getEnd();
				} else { 
					endOffset = element.getEndTagCloseOffset() + 1;
				}

				Range diagnosticRange = XMLPositionUtility.createRange(startOffset, endOffset, document);
				CodeAction removeContentAction = CodeActionFactory.remove("Remove element", diagnosticRange, document.getTextDocument(), diagnostic);
				codeActions.add(removeContentAction);
			}

		} catch (BadLocationException e) {
			// Do nothing
		}
	}

}
