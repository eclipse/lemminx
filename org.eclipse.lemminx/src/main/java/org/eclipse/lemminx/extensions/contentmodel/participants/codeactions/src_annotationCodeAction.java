/**
 *  Copyright (c) 2023 Omar Farag.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Omar Farag <omarfarag74@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.extensions.contentmodel.participants.codeactions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.commons.CodeActionFactory;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionRequest;

/**
 * Code action to fix invalid src-annotation elements
 */
public class src_annotationCodeAction implements ICodeActionParticipant {

	private static List<String> potentialTags = new ArrayList<String>() {
		{
			add("xs:appinfo");
			add("xs:documentation");
		}
	};

	@Override
	public void doCodeAction(ICodeActionRequest request, List<CodeAction> codeActions, CancelChecker cancelChecker) {
		Diagnostic diagnostic = request.getDiagnostic();
		DOMDocument document = request.getDocument();
		Range diagnosticRange = diagnostic.getRange();
		String codeActionText;
		Range closeRange = null;
		// Attempt to get tag name
		try {
			int startOffset = document.offsetAt(diagnosticRange.getStart()) + 1;
			DOMNode node = document.findNodeAt(startOffset);
			DOMElement element = (DOMElement) node;
			String tagName = element.getTagName();
			codeActionText = "Replace '" + tagName + "' with ";
			closeRange = XMLPositionUtility.selectEndTagName(element);
		} catch (BadLocationException e) {
			codeActionText = "Replace with ";
		}

		for (String potentialTag : potentialTags) {
			List<TextEdit> edits = new ArrayList<>();
			TextEdit replaceOpen = new TextEdit(diagnosticRange, potentialTag);
			edits.add(replaceOpen);
			if (closeRange != null) {
				TextEdit replaceClose = new TextEdit(closeRange, potentialTag);
				edits.add(replaceClose);
			}
			CodeAction replaceAction = CodeActionFactory.replace(codeActionText + "'" + potentialTag + "'", edits,
					document.getTextDocument(), diagnostic);
			codeActions.add(replaceAction);
		}
	}
}