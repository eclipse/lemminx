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
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.CodeActionFactory;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMElementDeclaration;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.extensions.contentmodel.utils.XMLGenerator;
import org.eclipse.lsp4xml.services.extensions.ICodeActionParticipant;
import org.eclipse.lsp4xml.services.extensions.IComponentProvider;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;

/**
 * Code action to fix cvc-complex-type.4 error.
 *
 */
public class cvc_complex_type_4CodeAction implements ICodeActionParticipant {

	@Override
	public void doCodeAction(Diagnostic diagnostic, Range range, DOMDocument document, List<CodeAction> codeActions,
			XMLFormattingOptions formattingSettings, IComponentProvider componentProvider) {
		Range diagnosticRange = diagnostic.getRange();
		try {
			int offset = document.offsetAt(range.getStart());
			DOMNode node = document.findNodeAt(offset);
			if (!node.isElement()) {
				return;
			}
			DOMElement element = (DOMElement) node;
			ContentModelManager contentModelManager = componentProvider.getComponent(ContentModelManager.class);
			CMElementDeclaration elementDeclaration = contentModelManager.findCMElement(element);
			if (elementDeclaration == null) {
				return;
			}

			List<CMAttributeDeclaration> requiredAttributes = elementDeclaration.getAttributes().stream()
					.filter(CMAttributeDeclaration::isRequired) //
					.filter(cmAttr -> !element.hasAttribute(cmAttr.getName())) //
					.collect(Collectors.toList());

			// CodeAction doesn't support snippet ->
			// https://github.com/Microsoft/language-server-protocol/issues/592
			boolean supportSnippet = false;
			XMLGenerator generator = new XMLGenerator(null, "", "", supportSnippet, 0);
			String xmlAttributes = generator.generate(requiredAttributes, element.getTagName());

			// Insert required attributes
			CodeAction insertRequiredAttributesAction = CodeActionFactory.insert("Insert required attributes",
					diagnosticRange, xmlAttributes, document.getTextDocument(), diagnostic);
			codeActions.add(insertRequiredAttributesAction);
		} catch (Exception e) {
			// Do nothing
		}
	}

}
