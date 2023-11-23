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

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

/**
 * File path support completion test with xsi:schemaLocation.
 *
 * Test folders are in
 * org.eclipse.lemminx/src/test/resources/filePathCompletion/
 */
public class FilePathCompletionWithSchemaLocationTest extends AbstractFilePathCompletionTest {

	@Test
	public void empty() throws BadLocationException {
		String xml = "<root-element xmlns=\"https://github.com/eclipse/lemminx\"\n"
				+ "	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "	xsi:schemaLocation=\"\n"
				+ "		https://github.com/eclipse/lemminx |\">\n"
				+ "</root-element>";
		CompletionItem[] items = getCompletionItemList(3, 37, 37, "folderA", "folderB", "folderC", "NestedA",
				"main.xsd");
		testCompletionFor(xml, 5, items);
	}

}