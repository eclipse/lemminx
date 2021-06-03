/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package org.eclipse.lemminx.extensions.contentmodel.participants;

import static org.eclipse.lemminx.client.ClientCommands.SELECT_FILE;

import java.util.Arrays;
import java.util.List;

import org.eclipse.lemminx.client.CodeLensKind;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.extensions.contentmodel.commands.AssociateGrammarCommand;
import org.eclipse.lemminx.extensions.contentmodel.commands.AssociateGrammarCommand.GrammarBindingType;
import org.eclipse.lemminx.services.extensions.codelens.ICodeLensParticipant;
import org.eclipse.lemminx.services.extensions.codelens.ICodeLensRequest;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * At first this participant is enabled only when LSP client can support the
 * client command "xml.file.select" to open a file dialog and
 * select it.
 * 
 * In this case, when XML file is not associated to a grammar (XSD, DTD), this
 * class generates several CodeLenses on the root of the DOM Document:
 * 
 * <ul>
 * <li>[Bind with XSD] : click on this Codelens open a file dialog to select the
 * XSD to bind.</li>
 * <li>[Bind with DTD] : click on this Codelens open a file dialog to select the
 * DTD to bind.</li>
 * <li>[Bind with xml-model] : click on this Codelens open a file dialog to
 * select the XSD, DTD to bind.</li>
 * </ul>
 * 
 * <p>
 * Once the LSP client select the DTD, XSD, it should call the
 * {@link AssociateGrammarCommand} to generate the proper syntax for binding.
 * </p>
 *
 */
public class ContentModelCodeLensParticipant implements ICodeLensParticipant {

	@Override
	public void doCodeLens(ICodeLensRequest request, List<CodeLens> lenses, CancelChecker cancelChecker) {
		if (!request.isSupportedByClient(CodeLensKind.Association)) {
			// The LSP client can support Association, when DOM document is not bound to a
			// grammar, code lenses appears:

			// [Bind with XSD] [Bind with DTD] [Bind with xml-model]
			// <foo />

			// A click on codelens consume the LSP client command
			// "xml.file.select" which should open a file dialog to select
			// a XSD, DTD
			// Once the file is selected, the LSP client must consume the XML language
			// server command "xml.associate.grammar.insert" by passing as parameters
			// - the document uri
			// - the selected file uri
			// - the binding type coming from code lens arguments.

			// The XML language server command return a TextDocumentEdit which must be
			// applied on LSP client side.
			return;
		}
		DOMDocument document = request.getDocument();
		DOMElement documentElement = document.getDocumentElement();
		if (documentElement == null || document.hasGrammar()) {
			return;
		}
		String documentURI = document.getDocumentURI();
		Range range = XMLPositionUtility.selectRootStartTag(document);

		lenses.add(createAssociateLens(documentURI, "Bind with XSD", GrammarBindingType.XSD.getName(), range));
		lenses.add(createAssociateLens(documentURI, "Bind with DTD", GrammarBindingType.DTD.getName(), range));
		lenses.add(
				createAssociateLens(documentURI, "Bind with xml-model", GrammarBindingType.XML_MODEL.getName(), range));
	}

	private static CodeLens createAssociateLens(String documentURI, String title, String bindingType, Range range) {
		Command command = new Command(title, SELECT_FILE, Arrays.asList(documentURI, bindingType));
		return new CodeLens(range, command, null);
	}

}
