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
import java.nio.file.Paths;
import java.util.UUID;

import org.eclipse.lemminx.utils.FilesUtils;
import org.eclipse.lemminx.utils.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;

/**
 * AbstractCacheBasedTest
 */
public abstract class AbstractCacheBasedTest {

	protected Path testWorkDirectory = null;

	private final String uuid = UUID.randomUUID().toString();

	private static Path parentDir;

	static {
		try {
			String cacheInRepoDir = System.getProperty("lemminx.cacheInRepoDir");
			if (StringUtils.isEmpty(cacheInRepoDir)) {
				parentDir = Files.createTempDirectory("lemminx");
			} else {
				parentDir = Paths.get(System.getProperty("user.home"), "lemminx_test");
				Files.createDirectories(parentDir);
			}
		} catch (IOException e) {
			parentDir = null;
		}
	}

	@BeforeEach
	public final void setupCache() throws Exception {
		clearCache();
		FilesUtils.resetDeployPath();
		Assertions.assertNotNull(parentDir);
		Path childDir = parentDir.resolve(uuid);
		testWorkDirectory = Files.createDirectory(childDir);
		System.setProperty(FilesUtils.LEMMINX_WORKDIR_KEY, testWorkDirectory.toAbsolutePath().toString());
	}

	@AfterEach
	public final void clearCache() throws IOException {
		if (testWorkDirectory != null && Files.exists(testWorkDirectory)) {
			MoreFiles.deleteDirectoryContents(testWorkDirectory, RecursiveDeleteOption.ALLOW_INSECURE);
			Files.delete(testWorkDirectory);
		}
		System.clearProperty(FilesUtils.LEMMINX_WORKDIR_KEY);
		FilesUtils.resetDeployPath();
	}
}