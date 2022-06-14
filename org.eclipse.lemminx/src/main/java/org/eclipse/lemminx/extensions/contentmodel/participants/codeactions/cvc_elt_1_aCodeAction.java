/**
 * Copyright (c) 2022 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lemminx.extensions.contentmodel.participants.codeactions;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.commons.CodeActionFactory;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.extensions.contentmodel.model.CMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.CMElementDeclaration;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionRequest;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Code action to find the expected element name defined in the given xsd and
 * replace it
 * 
 * Given this XML where 'root' is defined as the root element in the XSD
 * 
 * <test></test> -> Error: Cannot find declaration of 'test'
 * 
 * To fix the error, the code action will suggest replacing 'test' with 'root'
 * in both the opening and closing tag
 * 
 */
public class cvc_elt_1_aCodeAction implements ICodeActionParticipant {

	@Override
	public void doCodeAction(ICodeActionRequest request, List<CodeAction> codeActions, CancelChecker cancelChecker) {
		DOMDocument document = request.getDocument();
		Diagnostic diagnostic = request.getDiagnostic();
		try {
			DOMNode unexpectedElement = document.getDocumentElement();
			if (unexpectedElement == null) {
				return;
			}
			String unexpectedElementText = unexpectedElement.getNodeName();

			// get range of text that needs to be replaced
			Range diagnosticRange = diagnostic.getRange();
			int startOffset = document.offsetAt(diagnosticRange.getStart()) + 1;
			DOMNode node = document.findNodeAt(startOffset);

			if (node == null || !node.isElement()) {
				return;
			}

			DOMElement element = (DOMElement) node;
			ContentModelManager contentModelManager = request.getComponent(ContentModelManager.class);
			Collection<String> expectedElements = new TreeSet<String>(Collator.getInstance());

			for (CMDocument cmDocument : contentModelManager.findCMDocument(element)) {
				Collection<CMElementDeclaration> elementDeclaration = cmDocument.getElements();
				for (CMElementDeclaration expectedElement : elementDeclaration) {
					expectedElements.add(expectedElement.getName());
				}
			}

			List<Range> ranges = new ArrayList<>();
			Range startRange = XMLPositionUtility.selectStartTagName(node);
			Range endRange = XMLPositionUtility.selectEndTagName(element);

			ranges.add(startRange);

			if (endRange != null) {
				ranges.add(endRange);
			}

			for (String expectedElementText : expectedElements) {
				CodeAction addReplaceRootElement = CodeActionFactory.replaceAt(
						"Replace '" + unexpectedElementText + "' with '" + expectedElementText + "'", expectedElementText,
						document.getTextDocument(), diagnostic, ranges);

				codeActions.add(addReplaceRootElement);
			}
		} catch (BadLocationException e) {
			// do nothing
		}

	}
}
