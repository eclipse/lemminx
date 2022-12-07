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

import java.util.function.Consumer;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLFileAssociation;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

/**
 * XML file associations completion tests with RelaxNG compact syntax.
 */
public class XMLFileAssociationCompletionBasedOnRelaxNGCompactSyntaxTest extends AbstractCacheBasedTest {

	@Test
	public void completionInRoot() throws BadLocationException {
		// completion on <|
		String xml = "<|";
		testCompletionFor(xml, //
				c("addressBook", te(0, 0, 0, 1, "<addressBook></addressBook>"), "<addressBook"));
	}

	@Test
	public void completionForElements() throws BadLocationException {
		// completion on <|
		String xml = "<addressBook>\r\n" + //
				"<|\r\n" + //
				"</addressBook>";
		testCompletionFor(xml, //
				c("card", te(1, 0, 1, 1, "<card></card>"), "<card"));
	}

	@Test
	public void completionForAttributeNames() throws BadLocationException {
		// completion on <|
		String xml = "<addressBook>\r\n" + //
				"<card |></card>\r\n" + //
				"</addressBook>";
		testCompletionFor(xml, //
				c("id", te(1, 6, 1, 6, "id=\"\""), "id"));
	}

	private static void testCompletionFor(String value, CompletionItem... expectedItems) throws BadLocationException {
		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			contentModelManager
					.setFileAssociations(createXMLFileAssociation("src/test/resources/relaxng/"));
		};
		XMLAssert.testCompletionFor(new XMLLanguageService(), value, null, configuration,
				"file:///test/addressBook.xml",
				null, true, expectedItems);
	}

	private static XMLFileAssociation[] createXMLFileAssociation(String baseSystemId) {
		XMLFileAssociation addressBook = new XMLFileAssociation();
		addressBook.setPattern("**/addressBook.xml");
		addressBook.setSystemId(baseSystemId + "addressBook.rnc");
		return new XMLFileAssociation[] { addressBook };
	}

}
