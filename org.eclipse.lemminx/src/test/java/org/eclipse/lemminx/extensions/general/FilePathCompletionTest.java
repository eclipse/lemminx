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
package org.eclipse.lemminx.extensions.general;

import static org.eclipse.lemminx.XMLAssert.c;
import static org.eclipse.lemminx.XMLAssert.te;
import static org.eclipse.lemminx.utils.platform.Platform.isWindows;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.utils.FilesUtils;
import org.eclipse.lemminx.utils.platform.Platform;
import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

/**
 * FilePathCompletionTest
 *
 * Test folders are in
 * org.eclipse.lemminx/src/test/resources/filePathCompletion/
 */
public class FilePathCompletionTest extends AbstractCacheBasedTest {

	private static final String userDir = FilesUtils.encodePath(System.getProperty("user.dir")); // C:..\..\folderName
																									// || /bin/.../java
	private static final String userDirBackSlash = userDir.replace("/", "\\");
	private static final String userDirForwardSlash = userDir.replace("\\", "/");

	@Test
	public void testFilePathCompletion() throws BadLocationException {
		String xml = "<a path=\"./|\">";
		CompletionItem[] items = getCompletionItemList("/", 0, 10, 11, "folderA", "folderB", "NestedA");
		testCompletionFor(xml, items);
	}

	@Test
	public void testFilePathCompletionBackSlash() throws BadLocationException {
		String xml = "<a path=\".\\|\">";
		CompletionItem[] items = getCompletionItemList("\\", 0, 10, 11, "folderA", "folderB", "NestedA");
		testCompletionFor(xml, items);
	}

	@Test
	public void testFilePathCompletionFolderA() throws BadLocationException {
		String xml = "<a path=\"./folderA/|\">";
		CompletionItem[] items = getCompletionItemList("/", 0, 18, 19, "xsdA1.xsd", "xsdA2.xsd");
		testCompletionFor(xml, items);
	}

	@Test
	public void testFilePathCompletionFolderABackSlash() throws BadLocationException {
		String xml = "<a path=\".\\folderA\\|\">";
		CompletionItem[] items = getCompletionItemList("\\", 0, 18, 19, "xsdA1.xsd", "xsdA2.xsd");
		testCompletionFor(xml, items);
	}

	@Test
	public void testFilePathCompletionFolderB() throws BadLocationException {
		String xml = "<a path=\"folderB/|\">";
		testCompletionFor(xml, 0);
	}

	@Test
	public void testFilePathCompletionFolderBBackSlash() throws BadLocationException {
		String xml = "<a path=\"folderB\\|\">";
		testCompletionFor(xml, 0);
	}

	@Test
	public void testFilePathCompletionFolderBAbsolutePath() throws BadLocationException {
		String filePath = userDirForwardSlash + "/src/test/resources/filePathCompletion/folderB/"; // C:/.../src/test...
		int filePathLength = filePath.length();
		String xml = "<a path=\"" + filePath + "|\">";
		CompletionItem[] items = getCompletionItemList("/", 0, 9 + filePathLength - 1, 9 + filePathLength, "xsdB1.xsd",
				"xmlB1.xml");
		testCompletionFor(xml, 2, items);
	}

	@Test
	public void testFilePathCompletionFolderBAbsolutePathBackSlash() throws BadLocationException {
		if (!isWindows) {
			return;
		}
		String filePath = userDirBackSlash + "\\src\\test\\resources\\filePathCompletion\\folderB\\"; // C:\...\src\test...
		int filePathLength = filePath.length();
		String xml = "<a path=\"" + filePath + "|\">";
		CompletionItem[] items = getCompletionItemList("\\", 0, 9 + filePathLength - 1, 9 + filePathLength, "xsdB1.xsd",
				"xmlB1.xml");
		testCompletionFor(xml, 2, items);
	}

	@Test
	public void testFilePathCompletionFolderBAbsolutePathWithFileScheme() throws BadLocationException {
		String filePath = (isWindows ? FilesUtils.FILE_SCHEME + "/" : FilesUtils.FILE_SCHEME) + userDirForwardSlash
				+ "/src/test/resources/filePathCompletion/folderB/";
		int filePathLength = filePath.length();
		String xml = "<a path=\"" + filePath + "|\">";
		CompletionItem[] items = getCompletionItemList("/", 0, 9 + filePathLength - 1, 9 + filePathLength, "xsdB1.xsd",
				"xmlB1.xml");
		testCompletionFor(xml, 2, items);
	}

	@Test
	public void testFilePathCompletionNestedA() throws BadLocationException {
		String xml = "<a path=\"./NestedA/|\">";
		CompletionItem[] items = getCompletionItemList("/", 0, 18, 19, "NestedB");
		testCompletionFor(xml, 1, items);
	}

	@Test
	public void testFilePathCompletionNestedABackSlash() throws BadLocationException {
		String xml = "<a path=\"./NestedA\\|\">";
		CompletionItem[] items = getCompletionItemList("\\", 0, 18, 19, "NestedB");
		testCompletionFor(xml, 1, items);
	}

	@Test
	public void testFilePathCompletionNestedBIncomplete() throws BadLocationException {
		String xml = "<a path=\"./NestedA/NestedB/ZZZ|\">";
		CompletionItem[] items = getCompletionItemList("/", 0, 26, 30, "nestedXSD.xsd");
		testCompletionFor(xml, 1, items);
	}

	@Test
	public void testFilePathCompletionNestedBIncompleteBackSlash() throws BadLocationException {
		String xml = "<a path=\".\\NestedA\\NestedB\\ZZZ|\">";
		CompletionItem[] items = getCompletionItemList("\\", 0, 26, 30, "nestedXSD.xsd");
		testCompletionFor(xml, 1, items);
	}

	@Test
	public void testFilePathCompletionExtraTextInValue() throws BadLocationException {
		String xml = "<a path=\"NAMESPACE_IGNORE_ME NestedA/NestedB/|\">";
		testCompletionFor(xml, 0);
	}

	@Test
	public void testFilePathCompletionExtraTextInValueBackSlash() throws BadLocationException {
		String xml = "<a path=\"NAMESPACE_IGNORE_ME NestedA\\NestedB\\|\">";
		testCompletionFor(xml, 0);
	}

	@Test
	public void testFilePathCompletionExtraTextInValueAbsolute() throws BadLocationException {
		String filePath = userDirForwardSlash + "/src/test/resources/filePathCompletion/NestedA/NestedB/";
		int filePathLength = filePath.length();
		String xml = "<a path=\"NAMESPACE_IGNORE_ME " + filePath + "|\">";
		CompletionItem[] items = getCompletionItemList("/", 0, 29 + filePathLength - 1, 29 + filePathLength,
				"nestedXSD.xsd");
		testCompletionFor(xml, 1, items);
	}

	@Test
	public void testFilePathCompletionExtraTextInValueAbsoluteBackSlash() throws BadLocationException {
		String filePath = userDirBackSlash + "\\src\\test\\resources\\filePathCompletion\\NestedA\\NestedB\\";
		String xml = "<a path=\"NAMESPACE_IGNORE_ME " + filePath + "|\">";
		testCompletionFor(xml, Platform.isWindows ? 1 : 0);
	}

	@Test
	public void testFilePathCompletionBadFolder() throws BadLocationException {
		String xml = "<a path=\"NestedA/BAD_FOLDER/|\">";
		testCompletionFor(xml, 0);
	}

	@Test
	public void testFilePathCompletionBadFolderBackSlash() throws BadLocationException {
		String xml = "<a path=\"NestedA\\BAD_FOLDER\\|\">";
		testCompletionFor(xml, 0);
	}

	@Test
	public void testFilePathCompletionStartWithDotDot() throws BadLocationException {
		String xml = "<a path=\"../filePathCompletion/folderA/|\">";
		CompletionItem[] items = getCompletionItemList("/", 0, 38, 39, "xsdA1.xsd", "xsdA2.xsd");
		testCompletionFor(xml, 2, items);
	}

	@Test
	public void testFilePathCompletionStartWithDotDotBackSlash() throws BadLocationException {
		String xml = "<a path=\"..\\filePathCompletion\\folderA\\|\">";
		CompletionItem[] items = getCompletionItemList("\\", 0, 38, 39, "xsdA1.xsd", "xsdA2.xsd");
		testCompletionFor(xml, 2, items);
	}

	@Test
	public void testFilePathCompletionStartWithDot() throws BadLocationException {
		String xml = "<a path=\"./folderA/|\">";
		CompletionItem[] items = getCompletionItemList("/", 0, 18, 19, "xsdA1.xsd", "xsdA2.xsd");
		testCompletionFor(xml, 2, items);
	}

	@Test
	public void testFilePathCompletionStartWithDotBackSlash() throws BadLocationException {
		String xml = "<a path=\".\\folderA\\|\">";
		CompletionItem[] items = getCompletionItemList("\\", 0, 18, 19, "xsdA1.xsd", "xsdA2.xsd");
		testCompletionFor(xml, 2, items);
	}

	@Test
	public void testFilePathCompletionEndsWithFileAndSlash() throws BadLocationException {
		String xml = "<a path=\"./randomFile.aaa/|\">";
		testCompletionFor(xml, 0);
	}

	@Test
	public void testFilePathCompletionEndsWithFileAndBackSlash() throws BadLocationException {
		String xml = "<a path=\".\\randomFile.aaa\\|\">";
		testCompletionFor(xml, 0);
	}

	@Test
	public void testFilePathCompletionNotValue() throws BadLocationException {
		String xml = "<a path=\"|\">";
		testCompletionFor(xml, 0);
	}

	@Test
	public void testFilePathCompletionDTD() throws BadLocationException {
		String xml = "<!DOCTYPE foo SYSTEM \"./|\">";
		CompletionItem[] items = getCompletionItemList("/", 0, 23, 24, "folderA", "folderB", "NestedA");
		testCompletionFor(xml, items);
	}

	@Test
	public void testFilePathCompletionDTDBackSlash() throws BadLocationException {

		String xml = "<!DOCTYPE foo SYSTEM \".\\|\">";
		CompletionItem[] items = getCompletionItemList("\\", 0, 23, 24, "folderA", "folderB", "NestedA");
		testCompletionFor(xml, items);
	}

	@Test
	public void testFilePathCompletionDTDFolderA() throws BadLocationException {
		String xml = "<!DOCTYPE foo SYSTEM \"./folderA/|\">";
		CompletionItem[] items = getCompletionItemList("/", 0, 31, 32, "xsdA1.xsd", "xsdA2.xsd");
		testCompletionFor(xml, items);
	}

	@Test
	public void testFilePathCompletionDTDFolderABackSlash() throws BadLocationException {
		String xml = "<!DOCTYPE foo SYSTEM \".\\folderA\\|\">";
		CompletionItem[] items = getCompletionItemList("\\", 0, 31, 32, "xsdA1.xsd", "xsdA2.xsd");
		testCompletionFor(xml, items);
	}

	@Test
	public void testFilePathCompletionDTDFolderB() throws BadLocationException {
		String xml = "<!DOCTYPE foo SYSTEM \"./folderB/|\">";
		CompletionItem[] items = getCompletionItemList("/", 0, 31, 32, "xsdB1.xsd", "xmlB1.xml");
		testCompletionFor(xml, 2, items);
	}

	@Test
	public void testFilePathCompletionDTDFolderBBackSlash() throws BadLocationException {
		String xml = "<!DOCTYPE foo SYSTEM \"./folderB\\|\">";
		CompletionItem[] items = getCompletionItemList("\\", 0, 31, 32, "xsdB1.xsd", "xmlB1.xml");
		testCompletionFor(xml, 2, items);
	}

	@Test
	public void testFilePathCompletionForEmptyDoctype() throws BadLocationException {
		String xml = "<!DOCTYPE foo SYSTEM \"|\">";
		testCompletionFor(xml, 9);
	}

	@Test
	public void testFilePathNoCompletionMissingSystemId() throws BadLocationException {
		String xml = "<!DOCTYPE foo \"./|\">";
		testCompletionFor(xml, 0);
	}

	@Test
	public void testFilePathCompletionWithSpacesFolder() throws BadLocationException {
		String xml = "<a path=\"./folderC/|\">";
		CompletionItem[] items = getCompletionItemList("/", 0, 18, 19, "a@b", "with%20spaces");
		testCompletionFor(xml, 2, items);
	}

	@Test
	public void testFilePathCompletionInsideSpecialChars() throws BadLocationException {
		String xml = "<a path=\"../|\">";
		CompletionItem[] items = getCompletionItemList("/", 0, 11, 12, "a@b", "with%20spaces");
		String fileURI = "file://" + userDirForwardSlash + "/src/test/resources/filePathCompletion/folderC/a@b/foo.xml";
		XMLAssert.testCompletionFor(xml, null, fileURI, 2, items);
	}

	@Test
	public void testFilePathCompletionWithBrokenAbsoluteWindowsPath() throws BadLocationException {
		String xml = "<a path=\"C|\">";
		testCompletionFor(xml, 0);
		xml = "<a path=\"C:|\">";
		testCompletionFor(xml, 0);
		xml = "<a path=\"C\\\\|\">";
		testCompletionFor(xml, 0);
		xml = "<a path=\"C::|\">";
		testCompletionFor(xml, 0);
	}

	private static void testCompletionFor(String xml, CompletionItem... expectedItems) throws BadLocationException {
		testCompletionFor(xml, null, expectedItems);
	}

	private static void testCompletionFor(String xml, Integer expectedItemCount, CompletionItem... expectedItems)
			throws BadLocationException {
		String fileURI = "file://" + userDirForwardSlash + "/src/test/resources/filePathCompletion/main.xml";
		XMLAssert.testCompletionFor(xml, null, fileURI, expectedItemCount, expectedItems);
	}

	private static CompletionItem[] getCompletionItemList(String slash, int line, int startChar, int endChar,
			String... fileOrFolderNames) {
		String s = slash;
		int fOfSize = fileOrFolderNames.length;
		CompletionItem[] items = new CompletionItem[fOfSize];

		for (int i = 0; i < fOfSize; i++) {
			String fOf = s + fileOrFolderNames[i];
			items[i] = c(fOf, te(line, startChar, line, endChar, fOf), fOf);
		}

		return items;

	}

}