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

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import static java.io.File.separator;

/**
 * FilesUtilsTest
 */
public class FilesUtilsTest {

	@Test
	public void testFilesCachePathPreference() throws Exception {	
		System.clearProperty(FilesUtils.LSP4XML_WORKDIR_KEY);
		String newBasePathString = System.getProperty("user.home");
		String newSubPathString = Paths.get("New", "Sub", "Path").toString();
		Path newSubPath = Paths.get(newSubPathString);
		FilesUtils.setCachePathSetting(newBasePathString);
		Path finalPath = FilesUtils.getDeployedPath(newSubPath);
		assertEquals(Paths.get(newBasePathString, newSubPathString).toString(), finalPath.toString());
	}

	@Test
	public void normalizePathTest() {
		assertEquals(Paths.get(System.getProperty("user.home"), "Test", "Folder").toString(), FilesUtils.normalizePath("~/Test/Folder"));
		assertEquals(Paths.get(separator + "Test", "~", "Folder").toString(), FilesUtils.normalizePath("/Test/~/Folder"));
		assertEquals(Paths.get("~", "Test", "Folder").toString(), FilesUtils.normalizePath("./~/Test/Folder"));
		assertEquals(Paths.get(separator +  "Folder").toString(), FilesUtils.normalizePath("/Test/../Folder"));
		assertEquals(Paths.get(separator + "Users", "Nikolas").toString(), FilesUtils.normalizePath("\\Users\\Nikolas\\"));
	}
}
