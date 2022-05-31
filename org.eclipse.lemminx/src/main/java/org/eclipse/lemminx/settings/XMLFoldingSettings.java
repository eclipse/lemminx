/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
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

import org.eclipse.lsp4j.FoldingRangeCapabilities;

/**
 * A wrapper around LSP {@link FoldingRangeCapabilities}.
 *
 */
public class XMLFoldingSettings {

	private FoldingRangeCapabilities capabilities;

	public void setCapabilities(FoldingRangeCapabilities capabilities) {
		this.capabilities = capabilities;
	}

	public FoldingRangeCapabilities getCapabilities() {
		return capabilities;
	}

	public Integer getRangeLimit() {
		return capabilities != null ? capabilities.getRangeLimit() : null;
	}

	private static final boolean DEFAULT_INCLUDE_CLOSING_TAG_IN_FOLD = false;

	private boolean includeClosingTagInFold;

	public XMLFoldingSettings() {
		this(DEFAULT_INCLUDE_CLOSING_TAG_IN_FOLD);
	}

	public XMLFoldingSettings(boolean includeClosingTagInFold) {
		this.includeClosingTagInFold = includeClosingTagInFold;
	}

	public void setIncludeClosingTagInFold(boolean includeClosingTagInFold) {
		this.includeClosingTagInFold = includeClosingTagInFold;
	}

	public boolean isIncludeClosingTagInFold() {
		return includeClosingTagInFold;
	}

	/**
	 * Merge only the given completion settings (and not the capability) 
	 * in the settings.
	 *
	 * @param newSettings the new settings to merge.
	 */

	public void merge(XMLFoldingSettings newSettings) {
		this.setIncludeClosingTagInFold(newSettings.isIncludeClosingTagInFold());
	}
}
