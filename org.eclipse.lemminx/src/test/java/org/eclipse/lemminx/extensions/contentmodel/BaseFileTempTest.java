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
package org.eclipse.lemminx.extensions.contentmodel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;

public class BaseFileTempTest {

	private static final Path tempDirPath = Paths.get("target/temp/");
	protected static final URI tempDirUri = tempDirPath.toAbsolutePath().toUri();

	@BeforeClass
	public static void setup() throws FileNotFoundException, IOException {
		deleteTempDirIfExists();
		createTempDir();
	}

	@AfterClass
	public static void tearDown() throws IOException {
		deleteTempDirIfExists();
	}

	private static void deleteTempDirIfExists() throws IOException {
		File tempDir = new File(tempDirUri);
		if (tempDir.exists()) {
			MoreFiles.deleteRecursively(tempDir.toPath(), RecursiveDeleteOption.ALLOW_INSECURE);
		}
	}

	private static void createTempDir() {
		File tempDir = new File(tempDirUri);
		tempDir.mkdir();
	}

	protected static void createFile(String fileName, String contents) throws IOException {
		try {
			URI uri = new URI("file://" + fileName);
			Path path = Paths.get(uri);
			Files.write(path, contents.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	protected static void updateFile(String fileName, String contents) throws IOException {
		// For Mac OS, Linux OS, the call of Files.getLastModifiedTime is working for 1
		// second.
		// Here we wait for > 1s to be sure that call of Files.getLastModifiedTime will
		// work.
		try {
			Thread.sleep(1050);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		createFile(fileName, contents);
	}
}
