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

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.contentmodel.BaseFileTempTest;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLFileAssociation;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

/**
 * XML completion tests based on RelaxNG with XML file association.
 *
 */
public class XMLCompletionBasedOnRelaxNGWithFileAssociationTest extends BaseFileTempTest {

	@Test
	public void completionInRoot() throws BadLocationException {
		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			contentModelManager.setFileAssociations(createRelaxNGAssociations("src/test/resources/relaxng/"));
		};
		String xml = "<|";
		testCompletionFor(xml, "file:///test/tei1.xml", configuration, //
				c("TEI", te(0, 0, 0, 1, "<TEI></TEI>"), "<TEI"), //
				c("teiCorpus", te(0, 0, 0, 1, "<teiCorpus></teiCorpus>"), "<teiCorpus"));
	}

	private static XMLFileAssociation[] createRelaxNGAssociations(String baseSystemId) {
		XMLFileAssociation tei = new XMLFileAssociation();
		tei.setPattern("tei*.xml");
		tei.setSystemId(baseSystemId + "tei_all.rng");
		return new XMLFileAssociation[] { tei };
	}

	private static void testCompletionFor(String xml, String fileURI, Consumer<XMLLanguageService> configuration,
			CompletionItem... expectedItems) throws BadLocationException {
		XMLAssert.testCompletionFor(new XMLLanguageService(), xml, null, configuration, fileURI, null, true,
				expectedItems);
	}
}