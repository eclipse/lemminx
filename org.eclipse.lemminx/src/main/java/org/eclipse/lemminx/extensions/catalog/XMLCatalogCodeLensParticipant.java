/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.catalog;

import java.util.Arrays;
import java.util.List;

import org.eclipse.lemminx.client.ClientCommands;
import org.eclipse.lemminx.commons.config.ConfigurationItemEdit;
import org.eclipse.lemminx.commons.config.ConfigurationItemEditType;
import org.eclipse.lemminx.commons.config.ConfigurationItemValueKind;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.services.extensions.codelens.ICodeLensParticipant;
import org.eclipse.lemminx.services.extensions.codelens.ICodeLensRequest;
import org.eclipse.lemminx.utils.DOMUtils;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * This code lens participant creates the "(un)register catalog" code lens in
 * catalog files.
 *
 * When clicked, this catalog runs the client command
 * <code>"xml.update.configuration"</code>
 * to update the workspace settings to register the catalog for use.
 * The client needs to implement the <code>"xml.update.configuration"</code>
 * command in order for this to work.
 */
public class XMLCatalogCodeLensParticipant implements ICodeLensParticipant {

	private final ContentModelManager contentModelManager;

	public XMLCatalogCodeLensParticipant(ContentModelManager contentModelManager) {
		this.contentModelManager = contentModelManager;
	}

	@Override
	public void doCodeLens(ICodeLensRequest request, List<CodeLens> lenses, CancelChecker cancelChecker) {
		// Register/unregister catalog
		createRegisterCatalogLenses(request, lenses);
	}

	private void createRegisterCatalogLenses(ICodeLensRequest request, List<CodeLens> lenses) {
		DOMDocument document = request.getDocument();
		if (!DOMUtils.isCatalog(document)) {
			return;
		}
		String documentURI = contentModelManager.expandSystemId(document.getDocumentURI());
		Range range = XMLPositionUtility.selectRootStartTag(document);
		String[] catalogs = contentModelManager.getCatalogs();
		if (catalogs == null || !Arrays.asList(catalogs).contains(documentURI)) {
			// When a catalog is not registered in settings.json, [Register Catalog]
			// CodeLens appears:

			// [Register Catalog]
			// <catalog ...>
			ConfigurationItemEdit configurationItemEdit = new ConfigurationItemEdit("xml.catalogs",
					documentURI, ConfigurationItemEditType.Add, ConfigurationItemValueKind.File);

			Command command = new Command("Register Catalog", ClientCommands.UPDATE_CONFIGURATION,
					Arrays.asList(configurationItemEdit));
			lenses.add(new CodeLens(range, command, null));
		} else {
			// When a catalog is already registered in settings.json, [Unregister Catalog]
			// CodeLens appears:

			// [Unregister Catalog]
			// <catalog ...>
			ConfigurationItemEdit configurationItemEdit = new ConfigurationItemEdit("xml.catalogs",
					documentURI, ConfigurationItemEditType.Delete, ConfigurationItemValueKind.File);
			Command command = new Command("Unregister Catalog", ClientCommands.UPDATE_CONFIGURATION,
					Arrays.asList(configurationItemEdit));
			lenses.add(new CodeLens(range, command, null));
		}
	}

}
