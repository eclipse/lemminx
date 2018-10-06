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
import org.eclipse.lsp4xml.commons.CodeActionFactory;
import org.eclipse.lsp4xml.dom.Element;
import org.eclipse.lsp4xml.dom.Node;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMElementDeclaration;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.services.extensions.ICodeActionParticipant;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;

/**
 * Code action to fix cvc_enumeration_valid error only for text, because for
 * attribute cvc-attribute-3 error fix it.
 *
 */
public class cvc_enumeration_validCodeAction implements ICodeActionParticipant {

	@Override
	public void doCodeAction(Diagnostic diagnostic, Range range, XMLDocument document, List<CodeAction> codeActions,
			XMLFormattingOptions formattingSettings) {
		try {
			int offset = document.offsetAt(range.getStart());
			Node element = document.findNodeBefore(offset);
			if (element != null && element.isElement()) {
				CMElementDeclaration cmElement = ContentModelManager.getInstance().findCMElement((Element) element);
				if (cmElement != null) {
					cmElement.getEnumerationValues().forEach(value -> {
						// Replace text content
						CodeAction replaceTextContentAction = CodeActionFactory.replace("Replace with '" + value + "'",
								range, value, document.getTextDocument(), diagnostic);
						codeActions.add(replaceTextContentAction);
					});
				}
			}
		} catch (Exception e) {
			// do nothing
		}
	}

}
