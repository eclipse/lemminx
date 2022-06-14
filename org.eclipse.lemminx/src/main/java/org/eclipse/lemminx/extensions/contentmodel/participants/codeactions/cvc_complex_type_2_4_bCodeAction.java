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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.commons.CodeActionFactory;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.extensions.contentmodel.model.CMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.CMElementDeclaration;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.extensions.contentmodel.utils.XMLGenerator;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionRequest;
import org.eclipse.lemminx.utils.StringUtils;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * CodeAction to insert expected child elements within an element
 * 
 * Given this XML where the expected child elements are not present as defined
 * in the XSD:
 * 
 * <servlet></servlet>
 * Error:
 * Child elements are missing from element:
 * - servlet
 *
 * The following elements are expected:
 * - description
 * - display-name
 * - icon
 * - servlet-name
 * 
 * To fix the error, the code action will suggest inserting the expected
 * elements inside the parent tag
 * 
 */

public class cvc_complex_type_2_4_bCodeAction implements ICodeActionParticipant {

	@Override
	public void doCodeAction(ICodeActionRequest request, List<CodeAction> codeActions, CancelChecker cancelChecker) {
		DOMDocument document = request.getDocument();
		Diagnostic diagnostic = request.getDiagnostic();
		try {
			Range diagnosticRange = diagnostic.getRange();
			int startOffset = document.offsetAt(diagnosticRange.getStart()) + 1;
			DOMNode node = document.findNodeAt(startOffset);

			if (node == null || !node.isElement()) {
				return;
			}

			DOMElement element = (DOMElement) node;
			int elementOffset = element.getStartTagCloseOffset() + 1;
			Position childElementPosition = document.positionAt(elementOffset);

			StringBuilder insertText = new StringBuilder();

			XMLGenerator generator = request.getXMLGenerator();
			ContentModelManager contentModelManager = request.getComponent(ContentModelManager.class);

			// get list of all existing elements
			Set<String> existingElements = new TreeSet<String>();
			List<DOMNode> children = element.getChildren();
			for (DOMNode child : children) {
				if (child.isElement()) {
					existingElements.add(child.getNodeName());
				}
			}

			for (CMDocument cmDocument : contentModelManager.findCMDocument(element)) {
				CMElementDeclaration matchesCMElement = cmDocument.findCMElement(element);
				if (matchesCMElement != null) {
					insertText.append(generator.generate(matchesCMElement, null, true, true, existingElements));
				}
			}

			// When the parent start and end tag is on the same line: add new line, and
			// indent if needed
			if (document.positionAt(element.getStartTagCloseOffset()).getLine() == document
					.positionAt(element.getEndTagOpenOffset()).getLine()) {
				int lineNum = document.positionAt(element.getStartTagCloseOffset()).getLine();
				insertText.append(document.getLineIndentInfo(lineNum).getLineDelimiter());
				insertText.append(StringUtils.getStartWhitespaces(document.lineText(lineNum)));
			}

			String insertStr = insertText.toString();

			CodeAction insertExpectedElement = CodeActionFactory.insert(
					"Insert all expected elements", childElementPosition, insertStr,
					document.getTextDocument(), diagnostic);

			codeActions.add(insertExpectedElement);

		} catch (BadLocationException e) {
			// do nothing
		}
	}
}
