/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;

import org.eclipse.lsp4xml.utils.FilesUtils;
import org.eclipse.lsp4xml.utils.ProjectUtils;
import org.junit.After;
import org.junit.Before;

/**
 * AbstractCacheBasedTest
 */
public abstract class AbstractCacheBasedTest {

	protected static Path TEST_WORK_DIRECTORY = ProjectUtils.getProjectDirectory().resolve("target/test-cache");

	@Before
	public final void setupCache() throws Exception {
		clearCache();
		System.setProperty(FilesUtils.LSP4XML_WORKDIR_KEY, TEST_WORK_DIRECTORY.toAbsolutePath().toString());
	}

	@After
	public final void clearCache() throws IOException {
		if (Files.exists(TEST_WORK_DIRECTORY)) {
			MoreFiles.deleteDirectoryContents(TEST_WORK_DIRECTORY,RecursiveDeleteOption.ALLOW_INSECURE);
		}
		System.clearProperty(FilesUtils.LSP4XML_WORKDIR_KEY);
		FilesUtils.resetDeployPath();
	}
}