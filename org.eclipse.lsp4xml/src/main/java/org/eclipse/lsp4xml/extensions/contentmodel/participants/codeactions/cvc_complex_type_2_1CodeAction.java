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
package org.eclipse.lsp4xml.extensions.contentmodel.participants.codeactions;

import java.util.List;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.CodeActionFactory;
import org.eclipse.lsp4xml.dom.Element;
import org.eclipse.lsp4xml.dom.Node;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.services.extensions.ICodeActionParticipant;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;
import org.eclipse.lsp4xml.utils.XMLPositionUtility;

/**
 * Code action to fix cvc-complex-type.2.1 error.
 *
 */
public class cvc_complex_type_2_1CodeAction implements ICodeActionParticipant {

	@Override
	public void doCodeAction(Diagnostic diagnostic, Range range, XMLDocument document, List<CodeAction> codeActions,
			XMLFormattingOptions formattingSettings) {
		try {
			int offset = document.offsetAt(range.getStart());
			Node node = document.findNodeAt(offset);
			if (node != null && node.isElement()) {
				Element element = (Element) node;
				int startOffset = element.getStartTagCloseOffset();
				int endOffset = element.getEnd();
				Range diagnosticRange = XMLPositionUtility.createRange(startOffset, endOffset, document);
				CodeAction removeContentAction = CodeActionFactory.replace("Set element as empty", diagnosticRange,
						"/>", document.getTextDocument(), diagnostic);
				codeActions.add(removeContentAction);
			}

		} catch (BadLocationException e) {
			// DO nothing
		}
	}

}
