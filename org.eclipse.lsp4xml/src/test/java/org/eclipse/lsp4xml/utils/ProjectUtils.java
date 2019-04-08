/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4xml.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ProjectUtils
 */
public class ProjectUtils {

	/**
	 * @return the current lsp4xml project directory
	 */
	public static Path getProjectDirectory() {
		String currPath = new File(ProjectUtils.class.getClassLoader().getResource("").getPath()).toString();
		Path dir = Paths.get(currPath);
		while (!Files.exists(dir.resolve("pom.xml")) && dir.getParent() != null) {
			dir = dir.getParent();
		}
		return dir;
	}
	
}