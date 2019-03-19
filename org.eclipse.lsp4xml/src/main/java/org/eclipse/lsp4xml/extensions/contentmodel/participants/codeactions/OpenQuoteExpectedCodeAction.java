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
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.CodeActionFactory;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.services.extensions.ICodeActionParticipant;
import org.eclipse.lsp4xml.services.extensions.IComponentProvider;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;

/**
 * OpenQuoteExpectedCodeAction
 */
public class OpenQuoteExpectedCodeAction implements ICodeActionParticipant {

	@Override
	public void doCodeAction(Diagnostic diagnostic, Range range, DOMDocument document, List<CodeAction> codeActions,
			XMLFormattingOptions formattingSettings, IComponentProvider componentProvider) {
		Range diagnosticRange = diagnostic.getRange();
		int offset;
		try {
			offset = document.offsetAt(diagnosticRange.getEnd());
		} catch (BadLocationException e) {
			return;
		}
		DOMAttr attr = document.findAttrAt(offset);
		if(attr == null || !attr.isAttribute()) {
			return;
		}
		String q = formattingSettings.getQuotationAsString();
		Position codeactionPosition;
		Position possibleEndPosition = null;
		String possibleValue = null;
		try {
			codeactionPosition = document.positionAt(attr.getEnd());
			DOMNode next = attr.getNextSibling();
			if(next instanceof DOMAttr) {
				DOMAttr nextAttr = (DOMAttr) next;
				if(!nextAttr.hasDelimiter()) {
					possibleEndPosition = document.positionAt(nextAttr.getEnd());
					possibleValue = nextAttr.getName();
				}
			}
		} catch (BadLocationException e) {
		  return;
		}
		CodeAction removeContentAction;
		if(possibleEndPosition != null && possibleValue != null) {
			removeContentAction = CodeActionFactory.replace("Insert quotations", new Range(codeactionPosition, possibleEndPosition), q + possibleValue + q, document.getTextDocument(), diagnostic);	
		}
		else {
			removeContentAction = CodeActionFactory.insert("Insert quotations", codeactionPosition, q + q, document.getTextDocument(), diagnostic);
		}
		codeActions.add(removeContentAction);
	}

	
}