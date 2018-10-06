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
import org.eclipse.lsp4xml.dom.Attr;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.services.extensions.ICodeActionParticipant;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;

/**
 * Code action to fix cvc-complex-type.3.2.2 error.
 *
 */
public class cvc_complex_type_3_2_2CodeAction implements ICodeActionParticipant {

	@Override
	public void doCodeAction(Diagnostic diagnostic, Range range, XMLDocument document, List<CodeAction> codeActions,
			XMLFormattingOptions formattingSettings) {
		Range diagnosticRange = diagnostic.getRange();
		try {
			int offset = document.offsetAt(diagnosticRange.getEnd());
			Attr attr = document.findAttrAt(offset);
			if (attr != null) {
				// Remove attribute
				int startOffset = attr.getStart();
				int endOffset = attr.getEnd();
				Range attrRange = new Range(document.positionAt(startOffset), document.positionAt(endOffset));
				CodeAction removeAttributeAction = CodeActionFactory.remove("Remove '" + attr.getName() + "' attribute",
						attrRange, document.getTextDocument(), diagnostic);
				codeActions.add(removeAttributeAction);
			}
		} catch (BadLocationException e) {
			// Do nothing
		}
	}

}
