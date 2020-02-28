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

/**
 * XMLSymbolPreferences for Document Symbols
 */
public class XMLSymbolSettings {

	private transient XMLExcludedSymbolFile[] excludedFiles;

	private boolean enabled = true;

	private String[] excluded;

	public XMLExcludedSymbolFile[] getExcludedFiles() {
		return excludedFiles;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String[] getExcluded() {
		return excluded;
	}

	/**
	 * Will use the excluded pattern strings to create a list of
	 * {@link XMLExcludedSymbolFile} objects within this object.
	 * @param excluded
	 */
	public void setExcluded(String[] excluded) {
		XMLExcludedSymbolFile[] exclusions = new XMLExcludedSymbolFile[excluded.length];

		for(int i = 0; i < excluded.length; i++) {
			exclusions[i] = new XMLExcludedSymbolFile(excluded[i]);
		}

		excludedFiles = exclusions;
	}

	/**
	 * Given a file URI, this will check if it matches any of the given
	 * file patterns.
	 * 
	 * A uri is 'excluded' if it matches any of the given patterns.
	 * 
	 * **Important:** Set the excluded file patterns before calling this using 'setExcluded()'.
	 * @param uri
	 * @return
	 */
	public boolean isExcluded(String uri) {
		if (excludedFiles == null) {
			return false;
		}
		for (XMLExcludedSymbolFile excludedFile : excludedFiles) {
			if(excludedFile.matches(uri)) {
				return true;
			}
		}
		return false;
	}

}
