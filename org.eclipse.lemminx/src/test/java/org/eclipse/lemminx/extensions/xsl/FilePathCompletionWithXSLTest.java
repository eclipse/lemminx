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
package org.eclipse.lemminx.extensions.xsl;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.filepath.participants.AbstractFilePathCompletionTest;
import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

/**
 * File path support completion test for XSL with
 * 
 * <ul>
 * <li>include/@href</li>
 * <li>import/@href</li>
 * </ul>
 *
 * Test folders are in
 * org.eclipse.lemminx/src/test/resources/filePathCompletion/
 */
public class FilePathCompletionWithXSLTest extends AbstractFilePathCompletionTest {

	@Test
	public void includeHref() throws BadLocationException {
		String xml = "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n"
				+ "	<xsl:include href=\"|\" />\n"
				+ "</xsl:stylesheet>";
		CompletionItem[] items = getCompletionItemList(1, 20, 20, "folderA", "folderB", "folderC", "NestedA",
				"main.xsl");
		testCompletionFor(xml, 5, items);
	}

	@Test
	public void importHref() throws BadLocationException {
		String xml = "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n"
				+ "	<xsl:import href=\"|\" />\n"
				+ "</xsl:stylesheet>";
		CompletionItem[] items = getCompletionItemList(1, 19, 19, "folderA", "folderB", "folderC", "NestedA",
				"main.xsl");
		testCompletionFor(xml, 5, items);
	}
}