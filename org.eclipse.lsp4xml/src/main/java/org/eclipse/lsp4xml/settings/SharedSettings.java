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

import org.eclipse.lsp4j.FoldingRangeCapabilities;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lsp4xml.services.extensions.CompletionSettings;

/**
 * SharedSettings
 */
public class SharedSettings {

	public CompletionSettings completionSettings;
	public final FoldingRangeCapabilities foldingSettings;
	public XMLFormattingOptions formattingSettings;
	public final XMLValidationSettings validationSettings;
	public final XMLSymbolSettings symbolSettings;

	public SharedSettings() {
		this.completionSettings = new CompletionSettings();
		this.foldingSettings = new FoldingRangeCapabilities();
		this.formattingSettings = new XMLFormattingOptions(true);
		this.validationSettings = new XMLValidationSettings();
		this.symbolSettings = new XMLSymbolSettings();
	}

	public void setFormattingSettings(XMLFormattingOptions formattingOptions) {
		formattingSettings = formattingOptions;
	}

	public void setCompletionSettings(CompletionSettings completionSettings) {
		this.completionSettings = completionSettings;
	}
}