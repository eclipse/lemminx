/*******************************************************************************
* Copyright (c) 2019-2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.settings;

import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;

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

	private int maxItemsComputed;
	private boolean actionableNotificationSupport;
	private boolean openSettingsCommandSupport;

	public SharedSettings() {
		this.completionSettings = new XMLCompletionSettings();
		this.foldingSettings = new XMLFoldingSettings();
		this.formattingSettings = new XMLFormattingOptions(true);
		this.validationSettings = new XMLValidationSettings();
		this.symbolSettings = new XMLSymbolSettings();
		this.codeLensSettings = new XMLCodeLensSettings();
		this.hoverSettings = new XMLHoverSettings();
		this.actionableNotificationSupport = false;
		this.openSettingsCommandSupport = false;
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

	public boolean isActionableNotificationSupport() {
		return actionableNotificationSupport;
	}

	public void setActionableNotificationSupport(boolean actionableNotificationSupport) {
		this.actionableNotificationSupport = actionableNotificationSupport;
	}

	public boolean isOpenSettingsCommandSupport() {
		return openSettingsCommandSupport;
	}

	public void setOpenSettingsCommandSupport(boolean openSettingsCommandSupport) {
		this.openSettingsCommandSupport = openSettingsCommandSupport;
	}

	public int getMaxItemsComputed() {
		return maxItemsComputed;
	}

	public void setMaxItemsComputed(int maxItemsComputed) {
		this.maxItemsComputed = maxItemsComputed;
	}

}