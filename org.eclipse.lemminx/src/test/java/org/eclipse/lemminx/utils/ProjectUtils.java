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
package org.eclipse.lemminx.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.lemminx.XMLAssert;

/**
 * ProjectUtils
 */
public class ProjectUtils {

	/**
	 * @return the current lemminx project directory
	 */
	public static Path getProjectDirectory() {
		String xmlAssertClass =  XMLAssert.class.getName().replace('.', '/') + ".class"; // "org/eclipse/lemminx/XMLAssert.class"
		String currPath = new File(ProjectUtils.class.getClassLoader().getResource(xmlAssertClass).getPath()).toString();
		Path dir = FilesUtils.getPath(currPath);
		while (!Files.exists(dir.resolve("pom.xml")) && dir.getParent() != null) {
			dir = dir.getParent();
		}
		return dir;
	}
	
}