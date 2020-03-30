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
import static org.eclipse.lemminx.utils.OSUtils.SLASH;
import static org.eclipse.lemminx.utils.OSUtils.isWindows;

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

/**
 * FilePathCompletionTest
 * 
 * Test folders are in
 * org.eclipse.lemminx/src/test/resources/filePathCompletion/
 */
public class FilePathCompletionTest {

	private static final String userDir = System.getProperty("user.dir"); // C:..\..\folderName || /bin/.../java
	private static final String userDirBackSlash = userDir.replace("/", "\\");
	private static final String userDirForwardSlash = userDir.replace("\\", "/");
	private static final String fileScheme = "file://";
	private static final String fileSchemeWithRoot = fileScheme + "/";

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
		CompletionItem[] items = getCompletionItemList("/", 0, 16, 17, "xsdB1.xsd", "xmlB1.xml");
		testCompletionFor(xml, 2, items);
	}

	@Test
	public void testFilePathCompletionFolderBBackSlash() throws BadLocationException {
		String xml = "<a path=\"folderB\\|\">";
		CompletionItem[] items = getCompletionItemList("\\", 0, 16, 17, "xsdB1.xsd", "xmlB1.xml");
		testCompletionFor(xml, 2, items);
	}

	@Test
	public void testFilePathCompletionFolderBAbsolutePath() throws BadLocationException {
		String filePath = userDirForwardSlash + "/src/test/resources/filePathCompletion/folderB/"; // C:/.../src/test...
		int filePathLength = filePath.length();
		String xml = "<a path=\"" + filePath + "|\">";
		CompletionItem[] items = getCompletionItemList("/", 0, 9 + filePathLength - 1, 9 + filePathLength, "xsdB1.xsd", "xmlB1.xml");
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
		CompletionItem[] items = getCompletionItemList("\\", 0, 9 + filePathLength - 1, 9 + filePathLength, "xsdB1.xsd", "xmlB1.xml");
		testCompletionFor(xml, 2, items);
	}

	@Test
	public void testFilePathCompletionFolderBAbsolutePathWithFileScheme() throws BadLocationException {
		String filePath = (isWindows ? fileSchemeWithRoot : fileScheme) + userDirForwardSlash + "/src/test/resources/filePathCompletion/folderB/";
		int filePathLength = filePath.length();
		String xml = "<a path=\"" + filePath + "|\">";
		CompletionItem[] items = getCompletionItemList("/", 0, 9 + filePathLength - 1, 9 + filePathLength, "xsdB1.xsd", "xmlB1.xml");
		testCompletionFor(xml, 2, items);
	}

	@Test
	public void testFilePathCompletionNestedA() throws BadLocationException {
		String xml = "<a path=\"NestedA/|\">";
		CompletionItem[] items = getCompletionItemList("/", 0, 16, 17, "NestedB");
		testCompletionFor(xml, 1, items);
	}

	@Test
	public void testFilePathCompletionNestedABackSlash() throws BadLocationException {
		String xml = "<a path=\"NestedA\\|\">";
		CompletionItem[] items = getCompletionItemList("\\", 0, 16, 17, "NestedB");
		testCompletionFor(xml, 1, items);
	}

	@Test
	public void testFilePathCompletionNestedBIncomplete() throws BadLocationException {
		String xml = "<a path=\"NestedA/NestedB/ZZZ|\">";
		CompletionItem[] items = getCompletionItemList("/", 0, 24, 28, "nestedXSD.xsd");
		testCompletionFor(xml, 1, items);
	}
	
	@Test
	public void testFilePathCompletionNestedBIncompleteBackSlash() throws BadLocationException {
		String xml = "<a path=\"NestedA\\NestedB\\ZZZ|\">";
		CompletionItem[] items = getCompletionItemList("\\", 0, 24, 28, "nestedXSD.xsd");
		testCompletionFor(xml, 1, items);
	}

	@Test
	public void testFilePathCompletionExtraTextInValue() throws BadLocationException {
		String xml = "<a path=\"NAMESPACE_IGNORE_ME NestedA/NestedB/|\">";
		CompletionItem[] items = getCompletionItemList("/", 0, 44, 45, "nestedXSD.xsd");
		testCompletionFor(xml, 1, items);
	}
	
	@Test
	public void testFilePathCompletionExtraTextInValueBackSlash() throws BadLocationException {
		String xml = "<a path=\"NAMESPACE_IGNORE_ME NestedA\\NestedB\\|\">";
		CompletionItem[] items = getCompletionItemList("\\", 0, 44, 45, "nestedXSD.xsd");
		testCompletionFor(xml, 1, items);
	}

	@Test
	public void testFilePathCompletionExtraTextInValueAbsolute() throws BadLocationException {
		String filePath = userDirForwardSlash + "/src/test/resources/filePathCompletion/NestedA/NestedB/";
		int filePathLength = filePath.length();
		String xml = "<a path=\"NAMESPACE_IGNORE_ME " +  filePath + "|\">";
		CompletionItem[] items = getCompletionItemList("/", 0, 29 + filePathLength - 1, 29 + filePathLength, "nestedXSD.xsd");
		testCompletionFor(xml, 1, items);
	}

	@Test
	public void testFilePathCompletionExtraTextInValueAbsoluteBackSlash() throws BadLocationException {
		String filePath = userDirBackSlash + "\\src\\test\\resources\\filePathCompletion\\NestedA\\NestedB\\";
		int filePathLength = filePath.length();
		String xml = "<a path=\"NAMESPACE_IGNORE_ME " +  filePath + "|\">";
		CompletionItem[] items = getCompletionItemList("\\", 0, 29 + filePathLength - 1, 29 + filePathLength, "nestedXSD.xsd");
		testCompletionFor(xml, 1, items);
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

	private void testCompletionFor(String xml, CompletionItem... expectedItems) throws BadLocationException {
		testCompletionFor(xml, null, expectedItems);
	}

	private void testCompletionFor(String xml, Integer expectedItemCount, CompletionItem... expectedItems)
			throws BadLocationException {
		String userDir = System.getProperty("user.dir").replace("\\", "/");
		String fileURI = "file://" + userDir + "/src/test/resources/filePathCompletion/main.xml";
		XMLAssert.testCompletionFor(xml, null, fileURI, expectedItemCount,
				expectedItems);
	}

	private CompletionItem[] getCompletionItemList(int line, int startChar, int endChar, String... fileOrFolderNames) {
		return getCompletionItemList(SLASH, line, startChar, endChar, fileOrFolderNames);

	}
	
	private CompletionItem[] getCompletionItemList(String slash, int line, int startChar, int endChar, String... fileOrFolderNames) {
		String s = slash;
		int fOfSize = fileOrFolderNames.length;
		CompletionItem[] items = new CompletionItem[fOfSize];

		for(int i = 0; i < fOfSize; i++) {
			String fOf = s + fileOrFolderNames[i];
			items[i] = c(fOf, te(line, startChar, line, endChar, fOf), fOf);
		}
		
		return items;

	}

}