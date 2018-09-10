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
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.CodeActionFactory;
import org.eclipse.lsp4xml.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lsp4xml.contentmodel.model.CMElementDeclaration;
import org.eclipse.lsp4xml.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.contentmodel.utils.XMLGenerator;
import org.eclipse.lsp4xml.dom.Element;
import org.eclipse.lsp4xml.dom.Node;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.services.extensions.ICodeActionParticipant;

/**
 * Code action to fix cvc-complex-type.2.3 error.
 *
 */
public class cvc_complex_type_4CodeAction implements ICodeActionParticipant {

	@Override
	public void doCodeAction(Diagnostic diagnostic, Range range, XMLDocument document, List<CodeAction> codeActions) {
		try {
			int offset = document.offsetAt(range.getStart());
			Node node = document.findNodeAt(offset);
			if (!node.isElement()) {
				return;
			}
			Element element = (Element) node;
			CMElementDeclaration elementDeclaration = ContentModelManager.getInstance().findCMElement(element);
			if (elementDeclaration == null) {
				return;
			}
			
			List<CMAttributeDeclaration> requiredAttributes = elementDeclaration.getAttributes().stream().filter(CMAttributeDeclaration::isRequired).collect(Collectors.toList());
			if (requiredAttributes.isEmpty()) {
				
			}
			XMLGenerator generator = new XMLGenerator(null, "", "", true, 0);
			String xmlAttributes = generator.generate(requiredAttributes, node.tag);
			
			// Insert content
			CodeAction removeContentAction = CodeActionFactory.insert("Insert required attributes", range, 
					xmlAttributes,
					document.getTextDocument(), diagnostic);
			codeActions.add(removeContentAction);
		} catch (Exception e) {
			// Do nothing
		}
	}

}
