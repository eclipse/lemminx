/*******************************************************************************
* Copyright (c) 2022, 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.references;

import org.eclipse.lemminx.extensions.references.participants.XMLReferencesCodeLensParticipant;
import org.eclipse.lemminx.extensions.references.participants.XMLReferencesCompletionParticipant;
import org.eclipse.lemminx.extensions.references.participants.XMLReferencesDefinitionParticipant;
import org.eclipse.lemminx.extensions.references.participants.XMLReferencesDiagnosticParticipant;
import org.eclipse.lemminx.extensions.references.participants.XMLReferencesHighlightingParticipant;
import org.eclipse.lemminx.extensions.references.participants.XMLReferencesLinkedEditingRangesParticipant;
import org.eclipse.lemminx.extensions.references.participants.XMLReferencesReferenceParticipant;
import org.eclipse.lemminx.extensions.references.participants.XMLReferencesRenameParticipant;
import org.eclipse.lemminx.extensions.references.settings.XMLReferencesSettings;
import org.eclipse.lemminx.services.extensions.IDefinitionParticipant;
import org.eclipse.lemminx.services.extensions.IHighlightingParticipant;
import org.eclipse.lemminx.services.extensions.ILinkedEditingRangesParticipant;
import org.eclipse.lemminx.services.extensions.IReferenceParticipant;
import org.eclipse.lemminx.services.extensions.IXMLExtension;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lemminx.services.extensions.codelens.ICodeLensParticipant;
import org.eclipse.lemminx.services.extensions.completion.ICompletionParticipant;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.services.extensions.rename.IRenameParticipant;
import org.eclipse.lemminx.services.extensions.save.ISaveContext;
import org.eclipse.lsp4j.InitializeParams;

/**
 * XML references plugin.
 * 
 * This plugin provides the capability to support completion, definition, etc
 * for references between 2 attributes declared in a 'xml.references' settings.
 * 
 * Given this XML:
 * 
 * <code>
 * <div id="MyId" /><xref linkend="MyId" />
 * </code>
 * 
 * To benefit with:
 * 
 * - completion support when @linkend value will be trigger
 * - go to the definition of MyId from the @linked attribute
 * 
 * the following xml references settings must be configured:
 *
 * <code>
 * "xml.references": [
 * // references for docbook.xml files
 * {
 *   "pattern": "*.xml",
 *   "expressions": [
 *     {
 *       "from": "xref/@linkend",
 *       "to": "@id"
 *     }
 *   ]
 * }
 *]
 * 
 * </code>
 * 
 * 
 * @author Angelo ZERR
 *
 */
public class XMLReferencesPlugin implements IXMLExtension {

	private final ICompletionParticipant completionParticipant;
	private final IDefinitionParticipant definitionParticipant;
	private final IReferenceParticipant referenceParticipant;
	private final ICodeLensParticipant codeLensParticipant;
	private final IHighlightingParticipant highlightingParticipant;
	private final IRenameParticipant renameParticipant;
	private final ILinkedEditingRangesParticipant linkedEditingRangesParticipant;
	private final IDiagnosticsParticipant diagnosticsParticipant;

	private XMLReferencesSettings referencesSettings;

	public XMLReferencesPlugin() {
		completionParticipant = new XMLReferencesCompletionParticipant(this);
		definitionParticipant = new XMLReferencesDefinitionParticipant(this);
		referenceParticipant = new XMLReferencesReferenceParticipant(this);
		codeLensParticipant = new XMLReferencesCodeLensParticipant(this);
		highlightingParticipant = new XMLReferencesHighlightingParticipant(this);
		renameParticipant = new XMLReferencesRenameParticipant(this);
		linkedEditingRangesParticipant = new XMLReferencesLinkedEditingRangesParticipant(this);
		diagnosticsParticipant = new XMLReferencesDiagnosticParticipant(this);
	}

	@Override
	public void doSave(ISaveContext context) {
		if (context.getType() != ISaveContext.SaveContextType.DOCUMENT) {
			// Settings
			updateSettings(context);
		}
	}

	private void updateSettings(ISaveContext saveContext) {
		Object initializationOptionsSettings = saveContext.getSettings();
		XMLReferencesSettings referencesSettings = XMLReferencesSettings
				.getXMLReferencesSettings(initializationOptionsSettings);
		updateSettings(referencesSettings, saveContext);
	}

	private void updateSettings(XMLReferencesSettings settings, ISaveContext context) {
		this.referencesSettings = settings;
	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		registry.registerCompletionParticipant(completionParticipant);
		registry.registerDefinitionParticipant(definitionParticipant);
		registry.registerReferenceParticipant(referenceParticipant);
		registry.registerCodeLensParticipant(codeLensParticipant);
		registry.registerHighlightingParticipant(highlightingParticipant);
		registry.registerRenameParticipant(renameParticipant);
		registry.registerLinkedEditingRangesParticipants(linkedEditingRangesParticipant);
		registry.registerDiagnosticsParticipant(diagnosticsParticipant);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		registry.unregisterCompletionParticipant(completionParticipant);
		registry.unregisterDefinitionParticipant(definitionParticipant);
		registry.unregisterReferenceParticipant(referenceParticipant);
		registry.unregisterCodeLensParticipant(codeLensParticipant);
		registry.unregisterHighlightingParticipant(highlightingParticipant);
		registry.unregisterRenameParticipant(renameParticipant);
		registry.unregisterLinkedEditingRangesParticipants(linkedEditingRangesParticipant);
		registry.unregisterDiagnosticsParticipant(diagnosticsParticipant);
	}

	public XMLReferencesSettings getReferencesSettings() {
		return referencesSettings;
	}
}
