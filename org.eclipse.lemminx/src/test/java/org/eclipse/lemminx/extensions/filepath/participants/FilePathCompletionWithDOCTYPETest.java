/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
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

import static org.eclipse.lemminx.utils.platform.Platform.isWindows;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

/**
 * File path support completion test with DOCTYPE.
 *
 * Test folders are in
 * org.eclipse.lemminx/src/test/resources/filePathCompletion/
 */
public class FilePathCompletionWithDOCTYPETest extends AbstractFilePathCompletionTest {

	@Test
	public void empty() throws BadLocationException {
		String xml = "<!DOCTYPE foo SYSTEM \"|\">";
		CompletionItem[] items = getCompletionItemList(0, 22, 22, "folderA", "folderB", "folderC", "NestedA",
				"main.dtd");
		testCompletionFor(xml, 5, items);
	}

	@Test
	public void dotSlash() throws BadLocationException {
		String xml = "<!DOCTYPE foo SYSTEM \"./|\">";
		CompletionItem[] items = getCompletionItemList(0, 24, 24, "folderA", "folderB", "folderC", "NestedA",
				"main.dtd");
		testCompletionFor(xml, 5, items);
	}

	@Test
	public void dotSlashFollowingBySlash() throws BadLocationException {
		String xml = "<!DOCTYPE foo SYSTEM \"./|/\">";
		CompletionItem[] items = getCompletionItemList(0, 24, 25, "folderA", "folderB", "folderC", "NestedA",
				"main.dtd");
		testCompletionFor(xml, 5, items);
	}

	@Test
	public void backSlash() throws BadLocationException {
		if (!isWindows) {
			return;
		}
		String xml = "<!DOCTYPE foo SYSTEM \".\\|\">";
		CompletionItem[] items = getCompletionItemList(0, 24, 24, "folderA", "folderB", "folderC", "NestedA",
				"main.dtd");
		testCompletionFor(xml, 5, items);
	}

	@Test
	public void afterFolderA() throws BadLocationException {
		String xml = "<!DOCTYPE foo SYSTEM \"./folderA/|\">";
		CompletionItem[] items = getCompletionItemList(0, 32, 32, "dtdA1.dtd", "dtdA2.dtd");
		testCompletionFor(xml, 2, items);
	}

	@Test
	public void afterFolderABackSlash() throws BadLocationException {
		if (!isWindows) {
			return;
		}
		String xml = "<!DOCTYPE foo SYSTEM \".\\folderA\\|\">";
		CompletionItem[] items = getCompletionItemList(0, 32, 32, "dtdA1.dtd", "dtdA2.dtd");
		testCompletionFor(xml, 2, items);
	}

	@Test
	public void afterFolderB() throws BadLocationException {
		String xml = "<!DOCTYPE foo SYSTEM \"./folderB/|\">";
		CompletionItem[] items = getCompletionItemList(0, 32, 32, "dtdB1.dtd");
		testCompletionFor(xml, 1, items);
	}

	@Test
	public void afterFolderBBackSlash() throws BadLocationException {
		if (!isWindows) {
			return;
		}
		String xml = "<!DOCTYPE foo SYSTEM \"./folderB\\|\">";
		CompletionItem[] items = getCompletionItemList(0, 32, 32, "dtdB1.dtd");
		testCompletionFor(xml, 1, items);
	}

	@Test
	public void testFilePathNoCompletionMissingSystemId() throws BadLocationException {
		String xml = "<!DOCTYPE foo \"./|\">";
		testCompletionFor(xml, 0);
	}

}