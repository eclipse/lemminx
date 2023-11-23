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
 * File path support completion test with xsi:noNamespaceSchemaLocation.
 *
 * Test folders are in
 * org.eclipse.lemminx/src/test/resources/filePathCompletion/
 */
public class FilePathCompletionWithNoNamespaceSchemaLocationTest extends AbstractFilePathCompletionTest {

	@Test
	public void empty() throws BadLocationException {
		String xml = "<root-element\n"
				+ "	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "	xsi:noNamespaceSchemaLocation=\"|\">\n"
				+ "</root-element>";
		CompletionItem[] items = getCompletionItemList(2, 32, 32, "folderA", "folderB", "folderC", "NestedA",
				"main.xsd");
		testCompletionFor(xml, 5, items);
	}

	@Test
	public void dotSlash() throws BadLocationException {
		String xml = "<root-element\n"
				+ "	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "	xsi:noNamespaceSchemaLocation=\"./|\">\n"
				+ "</root-element>";
		CompletionItem[] items = getCompletionItemList(2, 34, 34, "folderA", "folderB", "folderC", "NestedA",
				"main.xsd");
		testCompletionFor(xml, 5, items);

	}

	@Test
	public void dotSlashFollowingBySlash() throws BadLocationException {
		String xml = "<root-element\n"
				+ "	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "	xsi:noNamespaceSchemaLocation=\"./|/\">\n"
				+ "</root-element>";
		CompletionItem[] items = getCompletionItemList(2, 34, 35, "folderA", "folderB", "folderC", "NestedA",
				"main.xsd");
		testCompletionFor(xml, 5, items);
	}

	@Test
	public void backSlash() throws BadLocationException {
		if (!isWindows) {
			return;
		}
		String xml = "<root-element\n"
				+ "	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "	xsi:noNamespaceSchemaLocation=\".\\|\">\n"
				+ "</root-element>";
		CompletionItem[] items = getCompletionItemList(2, 34, 34, "folderA", "folderB", "folderC", "NestedA",
				"main.xsd");
		testCompletionFor(xml, 5, items);
	}

	@Test
	public void afterFolderA() throws BadLocationException {
		String xml = "<root-element\n"
				+ "	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "	xsi:noNamespaceSchemaLocation=\"./folderA/|\">\n"
				+ "</root-element>";
		CompletionItem[] items = getCompletionItemList(2, 42, 42, "xsdA1.xsd", "xsdA2.xsd");
		testCompletionFor(xml, 2, items);
	}

	@Test
	public void afterFolderABackSlash() throws BadLocationException {
		if (!isWindows) {
			return;
		}
		String xml = "<root-element\n"
				+ "	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "	xsi:noNamespaceSchemaLocation=\".\\folderA\\|\">\n"
				+ "</root-element>";
		CompletionItem[] items = getCompletionItemList(2, 42, 42, "xsdA1.xsd", "xsdA2.xsd");
		testCompletionFor(xml, 2, items);
	}

	@Test
	public void afterFolderB() throws BadLocationException {
		String xml = "<root-element\n"
				+ "	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "	xsi:noNamespaceSchemaLocation=\"./folderB/|\">\n"
				+ "</root-element>";
		CompletionItem[] items = getCompletionItemList(2, 42, 42, "xsdB1.xsd");
		testCompletionFor(xml, 1, items);
	}

	@Test
	public void afterFolderBBackSlash() throws BadLocationException {
		if (!isWindows) {
			return;
		}
		String xml = "<root-element\n"
				+ "	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "	xsi:noNamespaceSchemaLocation=\"./folderB\\|\">\n"
				+ "</root-element>";
		CompletionItem[] items = getCompletionItemList(2, 42, 42, "xsdB1.xsd");
		testCompletionFor(xml, 1, items);
	}

}