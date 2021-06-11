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

import static org.eclipse.lemminx.client.ClientCommands.OPEN_BINDING_WIZARD;

import java.util.Arrays;
import java.util.List;

import org.eclipse.lemminx.client.CodeLensKind;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.extensions.contentmodel.commands.AssociateGrammarCommand;
import org.eclipse.lemminx.services.extensions.codelens.ICodeLensParticipant;
import org.eclipse.lemminx.services.extensions.codelens.ICodeLensRequest;
import org.eclipse.lemminx.utils.DOMUtils;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * At first this participant is enabled only when LSP client can support the
 * client command "xml.open.binding.wizard" to open the wizard to bind a XML to
 * a grammar/schema.
 * 
 * In this case, when XML file is not associated to a grammar/schema (DTD, XSD),
 * this class generates [Bind to grammar/schema...] CodeLens on the root of the
 * DOM Document:
 * 
 * On client side, click on this Codelens should open a wizard:
 * 
 * <ul>
 * <li>page1 : display a combo to select the binding type ("standard",
 * "xml-model").</li>
 * <li>page2: open a file dialog to select the grammar/schema (XSD/DTD) to
 * bind.</li>
 * <li>the finish wizard should consume the "xml.associate.grammar.insert"
 * command {@link AssociateGrammarCommand} to generate the proper syntax for
 * binding with following parameters:
 * <ul>
 * <li>the document uri.</li>
 * <li>the selected grammar/schema file uri.</li>
 * <li>the binding type.</li>
 * </ul>
 * In other words the "xml.associate.grammar.insert" returns the
 * {@link TextDocumentEdit} which must be applied on the LSP client side.
 * </p>
 *
 */
public class ContentModelCodeLensParticipant implements ICodeLensParticipant {

	@Override
	public void doCodeLens(ICodeLensRequest request, List<CodeLens> lenses, CancelChecker cancelChecker) {
		if (!canSupport(request)) {
			return;
		}

		// The LSP client can support Association, when DOM document is not bound to a
		// grammar, [Bind to grammar/schema...] CodeLens appears:

		// [Bind to grammar/schema...]
		// <foo />

		DOMDocument document = request.getDocument();
		DOMElement documentElement = document.getDocumentElement();
		if (documentElement == null || document.hasGrammar()) {
			return;
		}
		String documentURI = document.getDocumentURI();
		Range range = XMLPositionUtility.selectRootStartTag(document);

		lenses.add(createAssociateLens(documentURI, "Bind to grammar/schema...", range));
	}

	private static boolean canSupport(ICodeLensRequest request) {
		if (!request.isSupportedByClient(CodeLensKind.Association)) {
			return false;
		}
		String uri = request.getDocument().getDocumentURI();
		return !DOMUtils.isXSD(uri) && !DOMUtils.isDTD(uri);
	}

	private static CodeLens createAssociateLens(String documentURI, String title, Range range) {
		Command command = new Command(title, OPEN_BINDING_WIZARD, Arrays.asList(documentURI));
		return new CodeLens(range, command, null);
	}

}
