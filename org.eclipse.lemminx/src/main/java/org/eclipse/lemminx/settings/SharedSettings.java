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
	private final XMLPreferences preferences;
	private final XMLWorkspaceSettings workspaceSettings;
	private final XMLCommandCapabilities commandCapabilities;

	private boolean actionableNotificationSupport;

	public SharedSettings() {
		this.completionSettings = new XMLCompletionSettings();
		this.foldingSettings = new XMLFoldingSettings();
		this.formattingSettings = new XMLFormattingOptions(true);
		this.validationSettings = new XMLValidationSettings();
		this.symbolSettings = new XMLSymbolSettings();
		this.codeLensSettings = new XMLCodeLensSettings();
		this.hoverSettings = new XMLHoverSettings();
		this.preferences = new XMLPreferences();
		this.workspaceSettings = new XMLWorkspaceSettings();
		this.commandCapabilities = new XMLCommandCapabilities();
		this.actionableNotificationSupport = false;
	}

	public SharedSettings(SharedSettings newSettings) {
		this();
		this.completionSettings.merge(newSettings.getCompletionSettings());
		this.foldingSettings.merge(newSettings.getFoldingSettings());
		this.formattingSettings.merge(newSettings.getFormattingSettings());
		this.validationSettings.merge(newSettings.getValidationSettings());
		this.symbolSettings.merge(newSettings.getSymbolSettings());
		this.codeLensSettings.merge(newSettings.getCodeLensSettings());
		this.preferences.merge(newSettings.getPreferences());
		this.actionableNotificationSupport = newSettings.isActionableNotificationSupport();
	}

	/**
	 * Returns the completion settings.
	 *
	 * @return the completion settings.
	 */
	public XMLCompletionSettings getCompletionSettings() {
		return completionSettings;
	}

	/**
	 * Returns the folding settings.
	 *
	 * @return the folding settings.
	 */
	public XMLFoldingSettings getFoldingSettings() {
		return foldingSettings;
	}

	/**
	 * Returns the formatting settings.
	 *
	 * @return the formatting settings.
	 */
	public XMLFormattingOptions getFormattingSettings() {
		return formattingSettings;
	}

	/**
	 * Returns the validation settings.
	 *
	 * @return the validation settings.
	 */
	public XMLValidationSettings getValidationSettings() {
		return validationSettings;
	}

	/**
	 * Returns the symbol settings.
	 *
	 * @return the symbol settings.
	 */
	public XMLSymbolSettings getSymbolSettings() {
		return symbolSettings;
	}

	/**
	 * Returns the CodeLens settings.
	 *
	 * @return the CodeLens settings.
	 */
	public XMLCodeLensSettings getCodeLensSettings() {
		return codeLensSettings;
	}

	public XMLHoverSettings getHoverSettings() {
		return hoverSettings;
	}

	/**
	 * Returns the preferences.
	 *
	 * @return the preferences.
	 */
	public XMLPreferences getPreferences() {
		return preferences;
	}

	/**
	 * Returns the workspace settings.
	 *
	 * @return the workspace settings.
	 */
	public XMLWorkspaceSettings getWorkspaceSettings() {
		return workspaceSettings;
	}

	/**
	 * Returns the command capabilities.
	 *
	 * @return the command capabilities.
	 */
	public XMLCommandCapabilities getCommandCapabilities() {
		return commandCapabilities;
	}

	/**
	 * Returns true if the client supports actionable notifications and false
	 * otherwise
	 * Returns the symbol settings.
	 *
	 * See {@link org.eclipse.lemminx.customservice.ActionableNotification} and
	 * {@link org.eclipse.lemminx.customservice.XMLLanguageClientAPI}
	 *
	 * @return true if the client supports actionable notifications and false
	 *         otherwise
	 * @return the symbol settings.
	 */
	public boolean isActionableNotificationSupport() {
		return actionableNotificationSupport;
	}

	/**
	 * Sets the actionableNotificationSupport boolean
	 * Returns the CodeLens settings.
	 *
	 * @param actionableNotificationSupport
	 * @return the CodeLens settings.
	 */
	public void setActionableNotificationSupport(boolean actionableNotificationSupport) {
		this.actionableNotificationSupport = actionableNotificationSupport;
	}

}