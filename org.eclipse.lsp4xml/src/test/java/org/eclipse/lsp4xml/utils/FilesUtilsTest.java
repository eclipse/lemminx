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

/**
 * FilesUtilsTest
 */
public class FilesUtilsTest {

	@Test
	public void testFilesCachePathPreference() throws Exception {	
		System.clearProperty(FilesUtils.LSP4XML_WORKDIR_KEY);
		String newBasePathString = System.getProperty("user.home");
		String newSubPathString = "New/Sub/Path";
		Path newSubPath = Paths.get(newSubPathString);
		FilesUtils.setCachePathSetting(newBasePathString);
		Path finalPath = FilesUtils.getDeployedPath(newSubPath);
		assertEquals(newBasePathString + "/" + newSubPathString, finalPath.toString());
	}

	@Test
	public void normalizePathTest() {
		assertEquals(System.getProperty("user.home") + "/Test/Folder", FilesUtils.normalizePath("~/Test/Folder"));
		assertEquals("/Test/~/Folder", FilesUtils.normalizePath("/Test/~/Folder"));
		assertEquals(System.getProperty("user.home") + "/Test/Folder", FilesUtils.normalizePath("./~/Test/Folder"));
		assertEquals("/Folder", FilesUtils.normalizePath("/Test/../Folder"));
	}
}