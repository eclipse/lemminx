/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.extensions.contentmodel.participants.codeactions;

import java.util.Collection;
import java.util.List;
import java.text.Collator;
import java.util.TreeSet;

import org.eclipse.lemminx.commons.CodeActionFactory;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.extensions.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lemminx.extensions.contentmodel.model.CMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.services.extensions.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.IComponentProvider;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import static org.eclipse.lemminx.utils.StringUtils.isSimilar;

/**
 * Code action to fix cvc-attribute-3 error.
 *
 */
public class cvc_attribute_3CodeAction implements ICodeActionParticipant {

	@Override
	public void doCodeAction(Diagnostic diagnostic, Range range, DOMDocument document, List<CodeAction> codeActions,
			SharedSettings sharedSettings, IComponentProvider componentProvider, CancelChecker cancelChecker) {
		try {
			Range diagnosticRange = diagnostic.getRange();
			int offset = document.offsetAt(range.getStart());
			DOMAttr attr = document.findAttrAt(offset);
			if (attr != null) {
				DOMElement element = attr.getOwnerElement();
				String attributeName = attr.getName();
				ContentModelManager contentModelManager = componentProvider.getComponent(ContentModelManager.class);
				Collection<CMDocument> cmDocuments = contentModelManager.findCMDocument(element);
				String attributeValue = attr.getValue();
				for (CMDocument cmDocument : cmDocuments) {
					CMAttributeDeclaration cmAttribute = cmDocument.findCMAttribute(element, attributeName);
					if (cmAttribute != null) {
						Range rangeValue = new Range(
								new Position(diagnosticRange.getStart().getLine(),
										diagnosticRange.getStart().getCharacter() + 1),
								new Position(diagnosticRange.getEnd().getLine(),
										diagnosticRange.getEnd().getCharacter() - 1));
						Collection<String> similarValues = new TreeSet<String>(Collator.getInstance());
						Collection<String> otherValues = new TreeSet<String>(Collator.getInstance());

						for (String enumValue : cmAttribute.getEnumerationValues()) {
							if (isSimilar(enumValue, attributeValue)) {
								similarValues.add(enumValue);
							} else {
								otherValues.add(enumValue);
							}
						}
						if (!similarValues.isEmpty()) {
							// Add code actions for each similar value
							for (String similarValue : similarValues) {
								CodeAction similarCodeAction = CodeActionFactory.replace(
										"Did you mean '" + similarValue + "'?", rangeValue, similarValue, document.getTextDocument(),
										diagnostic);
								codeActions.add(similarCodeAction);
							}
						} else {
							// Add code actions for each possible elements
							for (String otherValue : otherValues) {
								CodeAction otherCodeAction = CodeActionFactory.replace(
										"Replace with '" + otherValue + "'", rangeValue, otherValue, document.getTextDocument(),
										diagnostic);
								codeActions.add(otherCodeAction);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// do nothing
		}
	}

}
