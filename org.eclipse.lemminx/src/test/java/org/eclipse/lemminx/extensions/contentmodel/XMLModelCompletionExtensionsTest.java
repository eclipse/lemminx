/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.contentmodel;

import static org.eclipse.lemminx.XMLAssert.CDATA_SNIPPETS;
import static org.eclipse.lemminx.XMLAssert.COMMENT_SNIPPETS;
import static org.eclipse.lemminx.XMLAssert.c;
import static org.eclipse.lemminx.XMLAssert.te;

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemCapabilities;
import org.junit.jupiter.api.Test;

/**
 * XML completion tests based on xml-model processing instruction.
 *
 */
public class XMLModelCompletionExtensionsTest {

	@Test
	public void completionBasedOnDTDWithXMLModel() throws BadLocationException {
		// completion on <|
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"<?xml-model href=\"http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd\"?>\r\n" + //
				"\r\n" + //
				"  <catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\"\r\n" + //
				"           prefer=\"public\">\r\n" + //
				"\r\n" + //
				"    <|";
		testCompletionFor(xml,
				c("delegatePublic", te(6, 4, 6, 5, "<delegatePublic publicIdStartString=\"$1\" catalog=\"$2\" />$0"),
						"<delegatePublic"), //
				c("public", te(6, 4, 6, 5, "<public publicId=\"$1\" uri=\"$2\" />$0"), "<public"));
	}

	@Test
	public void completionBasedOnXSDWithXMLModel() throws BadLocationException {
		String xml = "<?xml-model href=\"http://maven.apache.org/xsd/maven-4.0.0.xsd\"?>\r\n" + //
				"<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\r\n" + //
				"	<|" + //
				"</project>";
		testCompletionFor(xml, c("modelVersion", te(2, 1, 2, 2, "<modelVersion>$1</modelVersion>$0"), "<modelVersion"), //
				c("parent", "<parent>$1</parent>$0", "<parent"));
	}

	@Test
	public void completionBasedOnDTDAndXSDBoth() throws BadLocationException {
		String xml = "<?xml-model href=\"grammar.dtd\" ?>\r\n" + //
				"<?xml-model href=\"grammar.xsd\" ?>\r\n" + //
				"<grammar>\r\n" + //
				"	<|" + //
				"</grammar>";
		XMLAssert.testCompletionFor(xml, null, "src/test/resources/xml-model/grammar.xml", //
				CDATA_SNIPPETS + COMMENT_SNIPPETS + 3, //
				c("from-xsd", "<from-xsd></from-xsd>"), //
				c("from-dtd", "<from-dtd></from-dtd>"));
	}

	private static void testCompletionFor(String xml, CompletionItem... expectedItems) throws BadLocationException {
		testCompletionFor(xml, true, null, expectedItems);
	}

	private static void testCompletionFor(String xml, boolean isSnippetsSupported, Integer expectedCount,
			CompletionItem... expectedItems) throws BadLocationException {
		CompletionCapabilities completionCapabilities = new CompletionCapabilities();
		CompletionItemCapabilities completionItem = new CompletionItemCapabilities(isSnippetsSupported); // activate
																											// snippets
		completionCapabilities.setCompletionItem(completionItem);

		SharedSettings sharedSettings = new SharedSettings();
		sharedSettings.getCompletionSettings().setCapabilities(completionCapabilities);
		XMLAssert.testCompletionFor(new XMLLanguageService(), xml, "src/test/resources/catalogs/catalog.xml", null,
				null, expectedCount, sharedSettings, expectedItems);
	}

}
