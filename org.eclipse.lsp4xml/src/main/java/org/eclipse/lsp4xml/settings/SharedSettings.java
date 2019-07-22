/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4xml.settings;

import org.eclipse.lsp4xml.extensions.contentmodel.settings.XMLValidationSettings;

/**
 * SharedSettings
 */
public class SharedSettings {

	private final XMLCompletionSettings completionSettings;
	private final XMLFoldingSettings foldingSettings;
	private final XMLFormattingOptions formattingSettings;
	private final XMLValidationSettings validationSettings;
	private final XMLSymbolSettings symbolSettings;
	private final XMLCodeLensSettings codeLensSettings;
	private final XMLHoverSettings hoverSettings;

	public SharedSettings() {
		this.completionSettings = new XMLCompletionSettings();
		this.foldingSettings = new XMLFoldingSettings();
		this.formattingSettings = new XMLFormattingOptions(true);
		this.validationSettings = new XMLValidationSettings();
		this.symbolSettings = new XMLSymbolSettings();
		this.codeLensSettings = new XMLCodeLensSettings();
		this.hoverSettings = new XMLHoverSettings();
	}

	public XMLCompletionSettings getCompletionSettings() {
		return completionSettings;
	}

	public XMLFoldingSettings getFoldingSettings() {
		return foldingSettings;
	}

	public XMLFormattingOptions getFormattingSettings() {
		return formattingSettings;
	}

	public XMLValidationSettings getValidationSettings() {
		return validationSettings;
	}

	public XMLSymbolSettings getSymbolSettings() {
		return symbolSettings;
	}

	public XMLCodeLensSettings getCodeLensSettings() {
		return codeLensSettings;
	}

	public XMLHoverSettings getHoverSettings() {
		return hoverSettings;
	}

}