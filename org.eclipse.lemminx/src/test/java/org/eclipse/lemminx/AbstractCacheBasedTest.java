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
package org.eclipse.lemminx;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;

import org.eclipse.lemminx.utils.FilesUtils;
import org.eclipse.lemminx.utils.ProjectUtils;
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
		System.setProperty(FilesUtils.LEMMINX_WORKDIR_KEY, TEST_WORK_DIRECTORY.toAbsolutePath().toString());
	}

	@After
	public final void clearCache() throws IOException {
		if (Files.exists(TEST_WORK_DIRECTORY)) {
			MoreFiles.deleteDirectoryContents(TEST_WORK_DIRECTORY,RecursiveDeleteOption.ALLOW_INSECURE);
		}
		System.clearProperty(FilesUtils.LEMMINX_WORKDIR_KEY);
		FilesUtils.resetDeployPath();
	}
}