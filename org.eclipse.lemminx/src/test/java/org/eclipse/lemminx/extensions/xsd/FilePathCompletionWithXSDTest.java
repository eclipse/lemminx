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
package org.eclipse.lemminx.extensions.xsd;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.filepath.participants.AbstractFilePathCompletionTest;
import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

/**
 * File path support completion test with
 * 
 * <ul>
 * <li>include/@schemaLocation</li>
 * <li>import/@schemaLocation</li>
 * </ul>
 *
 * Test folders are in
 * org.eclipse.lemminx/src/test/resources/filePathCompletion/
 */
public class FilePathCompletionWithXSDTest extends AbstractFilePathCompletionTest {

	@Test
	public void includeSchemaLocation() throws BadLocationException {
		String xml = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
				+ "	<xs:include schemaLocation=\"|\"/>\n"
				+ "</xs:schema>";
		CompletionItem[] items = getCompletionItemList(1, 29, 29, "folderA", "folderB", "folderC", "NestedA",
				"main.xsd");
		testCompletionFor(xml, 5, items);
	}

	@Test
	public void importSchemaLocation() throws BadLocationException {
		String xml = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
				+ "	<xs:import schemaLocation=\"|\"/>\n"
				+ "</xs:schema>";
		CompletionItem[] items = getCompletionItemList(1, 28, 28, "folderA", "folderB", "folderC", "NestedA",
				"main.xsd");
		testCompletionFor(xml, 5, items);
	}
}