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
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.eclipse.lemminx.AbstractCacheBasedTest;

public abstract class BaseFileTempTest extends AbstractCacheBasedTest {

	protected static void createFile(String fileName, String contents) throws IOException {
		URI fileURI = new File(fileName).toURI();
		createFile(fileURI, contents);
	}

	protected static void createFile(URI fileURI, String contents) throws IOException {
		Path path = Paths.get(fileURI);
		Files.write(path, contents.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE,
				StandardOpenOption.TRUNCATE_EXISTING);
	}

	protected static void updateFile(String fileName, String contents) throws IOException {
		URI fileURI = new File(fileName).toURI();
		updateFile(fileURI, contents);
	}
	protected static void updateFile(URI fileURI, String contents) throws IOException {
		// For Mac OS, Linux OS, the call of Files.getLastModifiedTime is working for 1
		// second.
		// Here we wait for > 1s to be sure that call of Files.getLastModifiedTime will
		// work.
		try {
			Thread.sleep(1050);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		createFile(fileURI, contents);
	}

	protected Path getTempDirPath() {
		return testWorkDirectory;
	}
}
