/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.contentmodel.participants.codeactions;

import java.util.List;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.commons.CodeActionFactory;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.dom.DTDDeclParameter;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionRequest;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * OpenQuoteExpectedCodeAction
 */
public class OpenQuoteExpectedCodeAction implements ICodeActionParticipant {

	@Override
	public void doCodeAction(ICodeActionRequest request, List<CodeAction> codeActions, CancelChecker cancelChecker) {
		Diagnostic diagnostic = request.getDiagnostic();
		DOMDocument document = request.getDocument();
		Range diagnosticRange = diagnostic.getRange();
		int offset;
		try {
			offset = document.offsetAt(diagnosticRange.getEnd());
		} catch (BadLocationException e) {
			return;
		}
		DOMNode attr = document.findAttrAt(offset);
		if (attr != null && attr.isAttribute()) {
			// ex : <foo bar=value > <-- error on value which must be quoted.
			insertQuotationForAttr(attr, document, request, diagnostic, codeActions);
		} else {
			DTDDeclParameter parameter = document.findDTDDeclParameterAt(offset);
			if (parameter != null) {
				// ex : <!ATTLIST dadesadministratives idinstitut ID > <-- error on idinstitut
				// which must be quoted.
				
				// We disable this code action, since it generates DTD code which is not valid
				// We need to study more usecases of https://www.w3.org/TR/REC-xml/#NT-AttlistDecl
				// to provide more relevant code actions.
				// insertQuotationForParameter(parameter, document, request, diagnostic, codeActions);
			}
		}
	}

	private static void insertQuotationForAttr(DOMNode attr, DOMDocument document, ICodeActionRequest request,
			Diagnostic diagnostic, List<CodeAction> codeActions) {
		SharedSettings sharedSettings = request.getSharedSettings();
		String q = sharedSettings.getPreferences().getQuotationAsString();
		Position codeactionPosition;
		Position possibleEndPosition = null;
		String possibleValue = null;
		try {
			codeactionPosition = document.positionAt(attr.getEnd());
			DOMNode next = attr.getNextSibling();
			if (next instanceof DOMAttr) {
				DOMAttr nextAttr = (DOMAttr) next;
				if (!nextAttr.hasDelimiter()) {
					possibleEndPosition = document.positionAt(nextAttr.getEnd());
					possibleValue = nextAttr.getName();
				}
			}
		} catch (BadLocationException e) {
			return;
		}
		CodeAction removeContentAction;
		if (possibleEndPosition != null && possibleValue != null) {
			removeContentAction = CodeActionFactory.replace("Insert quotations",
					new Range(codeactionPosition, possibleEndPosition), q + possibleValue + q,
					document.getTextDocument(), diagnostic);
		} else {
			removeContentAction = CodeActionFactory.insert("Insert quotations", codeactionPosition, q + q,
					document.getTextDocument(), diagnostic);
		}
		codeActions.add(removeContentAction);
	}

	private static void insertQuotationForParameter(DTDDeclParameter parameter, DOMDocument document,
			ICodeActionRequest request, Diagnostic diagnostic, List<CodeAction> codeActions) {
		SharedSettings sharedSettings = request.getSharedSettings();
		String q = sharedSettings.getPreferences().getQuotationAsString();
		CodeAction insertQuotationsAction = CodeActionFactory.replace("Insert quotations",
				parameter.getTargetRange(), q + parameter.getParameter() + q,
				document.getTextDocument(), diagnostic);
		codeActions.add(insertQuotationsAction);
	}

}