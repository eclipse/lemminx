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
package org.eclipse.lemminx.extensions.contentmodel;

import static org.eclipse.lemminx.XMLAssert.c;
import static org.eclipse.lemminx.XMLAssert.te;
import static org.eclipse.lemminx.XMLAssert.testCompletionApply;

import java.util.Arrays;
import java.util.List;

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemCapabilities;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.MarkupKind;
import org.junit.jupiter.api.Test;

/**
 * XML completion tests based on XML Schema.
 *
 */
public class XMLCompletionApplyBasedOnXSDTest extends BaseFileTempTest {

	@Test
	public void emptyStartTagInText() throws BadLocationException {
		// completion on empty text
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<person xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"xsd/choice.xsd\">\r\n"
				+ //
				"|</employee>";
		CompletionList list = //
				testCompletionSnippetSupportFor(xml, "src/test/resources/choice.xml", 2, //
						c("member", te(2, 0, 2, 0, "<member$0>"), "member"), //
						c("employee", te(2, 0, 2, 0, "<employee$0>"), "employee"));

		// apply 'member' completion
		CompletionItem memberItem = findItemByLabel(list.getItems(), "member");
		testCompletionApply(xml, memberItem, //
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n"
						+ //
						"<person xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"xsd/choice.xsd\">\r\n"
						+ //
						"<member$0></member>");

		// apply 'employee' completion
		CompletionItem employeeItem = findItemByLabel(list.getItems(), "employee");
		testCompletionApply(xml, employeeItem, //
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n"
						+ //
						"<person xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"xsd/choice.xsd\">\r\n"
						+ //
						"<employee$0></employee>");

	}

	@Test
	public void emptyStartTagInElement() throws BadLocationException {
		// completion on empty text
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<person xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"xsd/choice.xsd\">\r\n"
				+ //
				"<|</employee>";
		CompletionList list = //
				testCompletionSnippetSupportFor(xml, "src/test/resources/choice.xml", 2, //
						c("member", te(2, 1, 2, 1, "member$0>"), "<member"), //
						c("employee", te(2, 1, 2, 1, "employee$0>"), "<employee"));

		// apply 'member' completion
		CompletionItem memberItem = findItemByLabel(list.getItems(), "member");
		testCompletionApply(xml, memberItem, //
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n"
						+ //
						"<person xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"xsd/choice.xsd\">\r\n"
						+ //
						"<member$0></member>");
		// apply 'employee' completion
		CompletionItem employeeItem = findItemByLabel(list.getItems(), "employee");
		testCompletionApply(xml, employeeItem, //
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n"
						+ //
						"<person xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"xsd/choice.xsd\">\r\n"
						+ //
						"<employee$0></employee>");
	}

	private CompletionList testCompletionSnippetSupportFor(String xml, String fileURI, Integer expectedCount,
			CompletionItem... expectedItems) throws BadLocationException {
		CompletionCapabilities completionCapabilities = new CompletionCapabilities();
		CompletionItemCapabilities completionItem = new CompletionItemCapabilities(true);
		completionItem.setDocumentationFormat(Arrays.asList(MarkupKind.MARKDOWN));
		completionCapabilities.setCompletionItem(completionItem);

		SharedSettings sharedSettings = new SharedSettings();
		sharedSettings.getCompletionSettings().setCapabilities(completionCapabilities);
		return XMLAssert.testCompletionFor(new XMLLanguageService(), xml, null, null, fileURI, null, sharedSettings,
				expectedItems);
	}

	private static CompletionItem findItemByLabel(List<CompletionItem> items, String label) {
		for (CompletionItem item : items) {
			if (label.equals(item.getLabel())) {
				return item;
			}
		}
		return null;
	}

}