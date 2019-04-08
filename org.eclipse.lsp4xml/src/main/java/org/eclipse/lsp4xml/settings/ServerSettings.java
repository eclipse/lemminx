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

import org.eclipse.lsp4xml.utils.FilesUtils;

/**
 * ServerSettings
 */
public class ServerSettings {

	public static final String DEFAULT_WORK_DIR = "~/.lsp4xml";

	private String workDir;

	/**
	 * @return the workDir
	 */
	public String getWorkDir() {
		return workDir;
	}

	/**
	 * @param workDir the workDir to set
	 */
	public void setWorkDir(String workDir) {
		this.workDir = workDir;
	}

	/**
	 * Returns a normalized workDir that was defined in the client preferences.
	 * 
	 * If null or empty, returns a default path.
	 * 
	 */
	public String getNormalizedWorkDir() {
		if(workDir == null || workDir.isEmpty()) {
			workDir = DEFAULT_WORK_DIR;
		}
		return FilesUtils.normalizePath(workDir);
	}

}