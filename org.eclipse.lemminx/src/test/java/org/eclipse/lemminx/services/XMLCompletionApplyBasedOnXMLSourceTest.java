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

package org.eclipse.lemminx.services;

import static org.eclipse.lemminx.XMLAssert.c;
import static org.eclipse.lemminx.XMLAssert.te;
import static org.eclipse.lemminx.XMLAssert.testCompletionApply;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMParser;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lemminx.settings.capabilities.CompletionResolveSupportProperty;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemCapabilities;
import org.eclipse.lsp4j.CompletionItemResolveSupportCapabilities;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.MarkupKind;
import org.junit.jupiter.api.Test;

/**
 * XML completion tests based on XML source.
 *
 */
public class XMLCompletionApplyBasedOnXMLSourceTest {

	@Test
	public void inEmptyText() throws BadLocationException {
		String xml = "<foo>\r\n" + //
				"  <bar>a</bar>\r\n" + //
				"  <baz>a</baz>\r\n" + //
				"  | \r\n" + //
				"</foo>";
		CompletionList list = //
				testCompletionSnippetSupportFor(xml, 2, //
						c("bar", te(3, 2, 3, 2, "<bar>$1</bar>$0"), Collections.emptyList(), "bar"), //
						c("baz", te(3, 2, 3, 2, "<baz>$1</baz>$0"), Collections.emptyList(), "baz"));

		// apply 'bar' completion
		CompletionItem barItem = findItemByLabel(list.getItems(), "bar");
		testCompletionApply(xml, barItem, //
				"<foo>\r\n" + //
						"  <bar>a</bar>\r\n" + //
						"  <baz>a</baz>\r\n" + //
						"  <bar>$1</bar>$0 \r\n" + //
						"</foo>");

		// apply 'baz' completion
		CompletionItem bazItem = findItemByLabel(list.getItems(), "baz");
		testCompletionApply(xml, bazItem, //
				"<foo>\r\n" + //
						"  <bar>a</bar>\r\n" + //
						"  <baz>a</baz>\r\n" + //
						"  <baz>$1</baz>$0 \r\n" + //
						"</foo>");
	}

	@Test
	public void inText() throws BadLocationException {
		String xml = "<foo>\r\n" + //
				"  <bar>a</bar>\r\n" + //
				"  <baz>a</baz>\r\n" + //
				"  f| \r\n" + //
				"</foo>";
		CompletionList list = //
				testCompletionSnippetSupportFor(xml, 2, //
						c("bar", te(3, 2, 3, 3, "<bar>$1</bar>$0"), Collections.emptyList(), "bar"), //
						c("baz", te(3, 2, 3, 3, "<baz>$1</baz>$0"), Collections.emptyList(), "baz"));

		// apply 'bar' completion
		CompletionItem barItem = findItemByLabel(list.getItems(), "bar");
		testCompletionApply(xml, barItem, //
				"<foo>\r\n" + //
						"  <bar>a</bar>\r\n" + //
						"  <baz>a</baz>\r\n" + //
						"  <bar>$1</bar>$0 \r\n" + //
						"</foo>");

		// apply 'baz' completion
		CompletionItem bazItem = findItemByLabel(list.getItems(), "baz");
		testCompletionApply(xml, bazItem, //
				"<foo>\r\n" + //
						"  <bar>a</bar>\r\n" + //
						"  <baz>a</baz>\r\n" + //
						"  <baz>$1</baz>$0 \r\n" + //
						"</foo>");
	}

	@Test
	public void inTextWithOrphanEndTag() throws BadLocationException {
		String xml = "<foo>\r\n" + //
				"  <bar>a</bar>\r\n" + //
				"  <baz>a</baz>\r\n" + //
				"  f|</buz> \r\n" + //
				"</foo>";
		CompletionList list = //
				testCompletionSnippetSupportFor(xml, 2, //
						c("bar", te(3, 2, 3, 3, "<bar$0>"), Arrays.asList(te(3, 5, 3, 8, "bar")), "bar"), //
						c("baz", te(3, 2, 3, 3, "<baz$0>"), Arrays.asList(te(3, 5, 3, 8, "baz")), "baz"));

		// apply 'bar' completion
		CompletionItem barItem = findItemByLabel(list.getItems(), "bar");
		testCompletionApply(xml, barItem, //
				"<foo>\r\n" + //
						"  <bar>a</bar>\r\n" + //
						"  <baz>a</baz>\r\n" + //
						"  <bar$0></bar> \r\n" + //
						"</foo>");

		// apply 'baz' completion
		CompletionItem bazItem = findItemByLabel(list.getItems(), "baz");
		testCompletionApply(xml, bazItem, //
				"<foo>\r\n" + //
						"  <bar>a</bar>\r\n" + //
						"  <baz>a</baz>\r\n" + //
						"  <baz$0></baz> \r\n" + //
						"</foo>");
	}

	@Test
	public void inEmptyTextWithOrphanEndTag() throws BadLocationException {
		String xml = "<foo>\r\n" + //
				"  <bar>a</bar>\r\n" + //
				"  <baz>a</baz>\r\n" + //
				"  |</buz> \r\n" + //
				"</foo>";
		CompletionList list = //
				testCompletionSnippetSupportFor(xml, 2, //
						c("bar", te(3, 2, 3, 2, "<bar$0>"), Arrays.asList(te(3, 4, 3, 7, "bar")), "bar"), //
						c("baz", te(3, 2, 3, 2, "<baz$0>"), Arrays.asList(te(3, 4, 3, 7, "baz")), "baz"));

		// apply 'bar' completion
		CompletionItem barItem = findItemByLabel(list.getItems(), "bar");
		testCompletionApply(xml, barItem, //
				"<foo>\r\n" + //
						"  <bar>a</bar>\r\n" + //
						"  <baz>a</baz>\r\n" + //
						"  <bar$0></bar> \r\n" + //
						"</foo>");

		// apply 'baz' completion
		CompletionItem bazItem = findItemByLabel(list.getItems(), "baz");
		testCompletionApply(xml, bazItem, //
				"<foo>\r\n" + //
						"  <bar>a</bar>\r\n" + //
						"  <baz>a</baz>\r\n" + //
						"  <baz$0></baz> \r\n" + //
						"</foo>");
	}

	@Test
	public void inElementWithNoTag() throws BadLocationException {
		String xml = "<foo>\r\n" + //
				"  <bar>a</bar>\r\n" + //
				"  <baz>a</baz>\r\n" + //
				"  <| \r\n" + //
				"</foo>";
		CompletionList list = //
				testCompletionSnippetSupportFor(xml, 2, //
						c("bar", te(3, 2, 3, 3, "<bar>$1</bar>$0"), Collections.emptyList(), "<bar"), //
						c("baz", te(3, 2, 3, 3, "<baz>$1</baz>$0"), Collections.emptyList(), "<baz"));

		// apply 'bar' completion
		CompletionItem barItem = findItemByLabel(list.getItems(), "bar");
		testCompletionApply(xml, barItem, //
				"<foo>\r\n" + //
						"  <bar>a</bar>\r\n" + //
						"  <baz>a</baz>\r\n" + //
						"  <bar>$1</bar>$0 \r\n" + //
						"</foo>");

		// apply 'baz' completion
		CompletionItem bazItem = findItemByLabel(list.getItems(), "baz");
		testCompletionApply(xml, bazItem, //
				"<foo>\r\n" + //
						"  <bar>a</bar>\r\n" + //
						"  <baz>a</baz>\r\n" + //
						"  <baz>$1</baz>$0 \r\n" + //
						"</foo>");
	}

	@Test
	public void inElement() throws BadLocationException {
		String xml = "<foo>\r\n" + //
				"  <bar>a</bar>\r\n" + //
				"  <baz>a</baz>\r\n" + //
				"  <f| \r\n" + //
				"</foo>";
		CompletionList list = //
				testCompletionSnippetSupportFor(xml, 2, //
						c("bar", te(3, 2, 3, 4, "<bar>$1</bar>$0"), Collections.emptyList(), "<bar"), //
						c("baz", te(3, 2, 3, 4, "<baz>$1</baz>$0"), Collections.emptyList(), "<baz"));

		// apply 'bar' completion
		CompletionItem barItem = findItemByLabel(list.getItems(), "bar");
		testCompletionApply(xml, barItem, //
				"<foo>\r\n" + //
						"  <bar>a</bar>\r\n" + //
						"  <baz>a</baz>\r\n" + //
						"  <bar>$1</bar>$0 \r\n" + //
						"</foo>");

		// apply 'baz' completion
		CompletionItem bazItem = findItemByLabel(list.getItems(), "baz");
		testCompletionApply(xml, bazItem, //
				"<foo>\r\n" + //
						"  <bar>a</bar>\r\n" + //
						"  <baz>a</baz>\r\n" + //
						"  <baz>$1</baz>$0 \r\n" + //
						"</foo>");
	}

	@Test
	public void inElementWithOrphanEndTag() throws BadLocationException {
		String xml = "<foo>\r\n" + //
				"  <bar>a</bar>\r\n" + //
				"  <baz>a</baz>\r\n" + //
				"  <f|</buz> \r\n" + //
				"</foo>";
		CompletionList list = //
				testCompletionSnippetSupportFor(xml, 2, //
						c("bar", te(3, 3, 3, 4, "bar$0>"), Arrays.asList(te(3, 6, 3, 9, "bar")), "<bar"), //
						c("baz", te(3, 3, 3, 4, "baz$0>"), Arrays.asList(te(3, 6, 3, 9, "baz")), "<baz"));

		// apply 'bar' completion
		CompletionItem barItem = findItemByLabel(list.getItems(), "bar");
		testCompletionApply(xml, barItem, //
				"<foo>\r\n" + //
						"  <bar>a</bar>\r\n" + //
						"  <baz>a</baz>\r\n" + //
						"  <bar$0></bar> \r\n" + //
						"</foo>");

		// apply 'baz' completion
		CompletionItem bazItem = findItemByLabel(list.getItems(), "baz");
		testCompletionApply(xml, bazItem, //
				"<foo>\r\n" + //
						"  <bar>a</bar>\r\n" + //
						"  <baz>a</baz>\r\n" + //
						"  <baz$0></baz> \r\n" + //
						"</foo>");
	}

	@Test
	public void inElementWithOrphanEndTagAndResolveSupport() throws BadLocationException {
		XMLLanguageService ls = new XMLLanguageService();
		SharedSettings sharedSettings = createSettings(true);
		String xml = "<foo>\r\n" + //
				"  <bar>a</bar>\r\n" + //
				"  <baz>a</baz>\r\n" + //
				"  <f|</buz> \r\n" + //
				"</foo>";
		CompletionList list = //
				testCompletionSnippetSupportFor(ls, xml, 2, //
						sharedSettings,
						c("bar", te(3, 3, 3, 4, "bar$0>"), Collections.emptyList(), "<bar"), //
						c("baz", te(3, 3, 3, 4, "baz$0>"), Collections.emptyList(), "<baz"));

		// apply 'bar' completion
		CompletionItem barItem = findItemByLabel(list.getItems(), "bar");
		testCompletionApply(xml, barItem, //
				"<foo>\r\n" + //
						"  <bar>a</bar>\r\n" + //
						"  <baz>a</baz>\r\n" + //
						"  <bar$0></buz> \r\n" + // As completion item is not resolved, at this step, it cannot update
													// the end tag
						"</foo>");

		// Resolve the 'bar' completion item
		String xmlWithoutCursor = xml;
		int offset = xmlWithoutCursor.indexOf('|');
		xmlWithoutCursor = xmlWithoutCursor.substring(0, offset) + xmlWithoutCursor.substring(offset + 1);
		DOMDocument document = DOMParser.getInstance().parse(xmlWithoutCursor, "test.xml", null);
		barItem = ls.resolveCompletionItem(barItem, document, sharedSettings, () -> {
		});

		testCompletionApply(xml, barItem, //
				"<foo>\r\n" + //
						"  <bar>a</bar>\r\n" + //
						"  <baz>a</baz>\r\n" + //
						"  <bar$0></bar> \r\n" + //
						"</foo>");
	}

	private static CompletionList testCompletionSnippetSupportFor(String xml, Integer expectedCount,
			CompletionItem... expectedItems) throws BadLocationException {
		return testCompletionSnippetSupportFor(new XMLLanguageService(), xml, expectedCount, createSettings(false),
				expectedItems);
	}

	private static CompletionList testCompletionSnippetSupportFor(XMLLanguageService ls, String xml,
			Integer expectedCount,
			SharedSettings sharedSettings, CompletionItem... expectedItems) throws BadLocationException {
		return XMLAssert.testCompletionFor(ls, xml, null, null, null, null, sharedSettings,
				expectedItems);
	}

	private static SharedSettings createSettings(boolean resolveSupport) {
		CompletionCapabilities completionCapabilities = new CompletionCapabilities();
		CompletionItemCapabilities completionItem = new CompletionItemCapabilities(true);
		completionItem.setDocumentationFormat(Arrays.asList(MarkupKind.MARKDOWN));
		if (resolveSupport) {
			CompletionItemResolveSupportCapabilities resolveSupportCapabilities = new CompletionItemResolveSupportCapabilities(
					Arrays.asList(CompletionResolveSupportProperty.documentation.name(),
							CompletionResolveSupportProperty.additionalTextEdits.name()));
			completionItem.setResolveSupport(resolveSupportCapabilities);
		}
		completionCapabilities.setCompletionItem(completionItem);
		SharedSettings sharedSettings = new SharedSettings();
		sharedSettings.getCompletionSettings().setCapabilities(completionCapabilities);
		return sharedSettings;
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