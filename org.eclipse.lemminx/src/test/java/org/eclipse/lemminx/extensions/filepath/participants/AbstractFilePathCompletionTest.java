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
package org.eclipse.lemminx.extensions.filepath.participants;

import static org.eclipse.lemminx.XMLAssert.c;
import static org.eclipse.lemminx.XMLAssert.te;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.XMLAssert.SettingsSaveContext;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.filepath.FilePathSettingsUtils;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lemminx.utils.FilesUtils;
import org.eclipse.lsp4j.CompletionItem;

/**
 * FilePathCompletionTest
 *
 * Test folders are in
 * org.eclipse.lemminx/src/test/resources/filePathCompletion/
 */
public abstract class AbstractFilePathCompletionTest extends AbstractCacheBasedTest {

	private static final String userDir = FilesUtils.encodePath(System.getProperty("user.dir")); // C:..\..\folderName
																									// || /bin/.../java
	protected static final String userDirBackSlash = userDir.replace("/", "\\");
	protected static final String userDirForwardSlash = userDir.replace("\\", "/");

	protected static void testCompletionFor(String xml, CompletionItem... expectedItems) throws BadLocationException {
		testCompletionFor(xml, null, expectedItems);
	}

	protected static void testCompletionFor(String xml, Integer expectedItemCount, CompletionItem... expectedItems)
			throws BadLocationException {
		String fileURI = getFileUri("main.xml");
		testCompletionFor(xml, fileURI, expectedItemCount, expectedItems);
	}

	protected static String getFileUri(String fileName) {
		return "file://" + userDirForwardSlash + "/src/test/resources/filePathCompletion/" + fileName;
	}

	protected static void testCompletionFor(String xml, String fileURI, Integer expectedItemCount,
			CompletionItem... expectedItems)
			throws BadLocationException {
		XMLAssert.testCompletionFor(new XMLLanguageService(), xml, null, ls -> {
			ls.doSave(new SettingsSaveContext(FilePathSettingsUtils.createFilePathsSettings()));

		}, fileURI, expectedItemCount, true, expectedItems);
	}

	protected static CompletionItem[] getCompletionItemList(int line, int startChar, int endChar,
			String... fileOrFolderNames) {
		int fOfSize = fileOrFolderNames.length;
		CompletionItem[] items = new CompletionItem[fOfSize];

		for (int i = 0; i < fOfSize; i++) {
			String fOf = fileOrFolderNames[i];
			items[i] = c(fOf, te(line, startChar, line, endChar, fOf), fOf);
		}

		return items;

	}

}