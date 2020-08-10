/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.relaxng.xml.completion;

import static org.eclipse.lemminx.XMLAssert.c;
import static org.eclipse.lemminx.XMLAssert.te;

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.contentmodel.BaseFileTempTest;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

/**
 * XML completion tests based on RelaxNG.
 *
 */
public class XMLCompletionBasedOnRelaxNGCompactSyntaxTest extends BaseFileTempTest {

	@Test
	public void completionInRoot() throws BadLocationException {
		// completion on <|
		String xml = "<?xml-model href=\"addressBook.rnc\" ?>\r\n" + //
				"<|";
		testCompletionFor(xml, //
				c("addressBook", te(1, 0, 1, 1, "<addressBook></addressBook>"), "<addressBook"));
	}

	@Test
	public void completionForElements() throws BadLocationException {
		// completion on <|
		String xml = "<?xml-model href=\"addressBook.rnc\" ?>\r\n" + //
				"<addressBook>\r\n" + //
				"<|\r\n" + //
				"</addressBook>";
		testCompletionFor(xml, //
				c("card", te(2, 0, 2, 1, "<card></card>"), "<card"));
	}

	@Test
	public void completionForAttributeNames() throws BadLocationException {
		// completion on <|
		String xml = "<?xml-model href=\"addressBook.rnc\" ?>\r\n" + //
				"<addressBook>\r\n" + //
				"<card |></card>\r\n" + //
				"</addressBook>";
		testCompletionFor(xml, //
				c("id", te(2, 6, 2, 6, "id=\"\""), "id"));
	}

	public static void testCompletionFor(String value, CompletionItem... expectedItems) throws BadLocationException {
		XMLAssert.testCompletionFor(new XMLLanguageService(), value, null, null, "src/test/resources/relaxng/test.xml",
				null, true, expectedItems);
	}
}