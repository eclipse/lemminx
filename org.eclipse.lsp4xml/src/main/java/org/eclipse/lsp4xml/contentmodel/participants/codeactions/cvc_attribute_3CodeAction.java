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
package org.eclipse.lsp4xml.contentmodel.participants.codeactions;

import java.util.List;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.CodeActionFactory;
import org.eclipse.lsp4xml.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lsp4xml.contentmodel.model.CMElementDeclaration;
import org.eclipse.lsp4xml.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.dom.Attr;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.services.extensions.ICodeActionParticipant;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;

/**
 * Code action to fix cvc-attribute-3 error.
 *
 */
public class cvc_attribute_3CodeAction implements ICodeActionParticipant {

	@Override
	public void doCodeAction(Diagnostic diagnostic, Range range, XMLDocument document, List<CodeAction> codeActions,
			XMLFormattingOptions formattingSettings) {
		try {
			int offset = document.offsetAt(range.getStart());
			Attr attr = document.findAttrAt(offset);
			if (attr != null) {
				String attributeName = attr.getName();
				CMElementDeclaration cmElement = ContentModelManager.getInstance()
						.findCMElement(attr.getOwnerElement());
				if (cmElement != null) {
					CMAttributeDeclaration cmAttribute = cmElement.findCMAttribute(attributeName);
					if (cmAttribute != null) {
						Range rangeValue = new Range(new Position(range.getStart().getLine(), range.getStart().getCharacter() + 1),
								new Position(range.getEnd().getLine(), range.getEnd().getCharacter() - 1));
						cmAttribute.getEnumerationValues().forEach(value -> {
							// Replace attribute value
							// value = "${1:" + value + "}";
							CodeAction replaceAttrValueAction = CodeActionFactory.replace(
									"Replace with '" + value + "'", rangeValue, value, document.getTextDocument(),
									diagnostic);
							codeActions.add(replaceAttrValueAction);
						});
					}
				}
			}
		} catch (Exception e) {
			// do nothing
		}
	}

}
