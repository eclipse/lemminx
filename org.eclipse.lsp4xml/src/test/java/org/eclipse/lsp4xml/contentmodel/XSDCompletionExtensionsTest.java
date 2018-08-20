/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.contentmodel;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.contentmodel.settings.ContentModelSettings;
import org.eclipse.lsp4xml.internal.parser.XMLParser;
import org.eclipse.lsp4xml.model.XMLDocument;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.eclipse.lsp4xml.services.extensions.CompletionSettings;
import org.junit.Assert;
import org.junit.Test;

/**
 * XSD completion tests.
 *
 */
public class XSDCompletionExtensionsTest {

	private static void testCompletionFor(String value, List<ItemDescription> expectedItems, Integer expectedCount)
			throws BadLocationException {
		int offset = value.indexOf('|');
		value = value.substring(0, offset) + value.substring(offset + 1);

		TextDocument document = new TextDocument(value, "test://test/test.html");
		Position position = document.positionAt(offset);
		XMLDocument htmlDoc = XMLParser.getInstance().parse(document);

		XMLLanguageService xmlLanguageService = new XMLLanguageService();
		// Configure XML catalog for XML schema
		ContentModelSettings settings = new ContentModelSettings();
		settings.setCatalogs(new String[] { "src/test/resources/catalog.xml" });
		xmlLanguageService.updateSettings(settings);
		CompletionList list = xmlLanguageService.doComplete(htmlDoc, position, new CompletionSettings(),
				new FormattingOptions(4, false));

		// no duplicate labels
		List<String> labels = list.getItems().stream().map(i -> i.getLabel()).sorted().collect(Collectors.toList());
		String previous = null;
		for (String label : labels) {
			Assert.assertTrue(
					"Duplicate label " + label + " in " + labels.stream().collect(Collectors.joining(",")) + "}",
					previous != label);
			previous = label;
		}
		if (expectedCount != null) {
			Assert.assertEquals(list.getItems().size(), expectedCount.intValue());
		}
		if (expectedItems != null) {
			for (ItemDescription item : expectedItems) {
				assertCompletion(list, item, document, offset);
			}
		}
	}

	private static void assertCompletion(CompletionList completions, ItemDescription expected, TextDocument document,
			int offset) {
		List<CompletionItem> matches = completions.getItems().stream().filter(completion -> {
			return expected.label.equals(completion.getLabel());
		}).collect(Collectors.toList());

		Assert.assertEquals(
				expected.label + " should only existing once: Actual: "
						+ completions.getItems().stream().map(c -> c.getLabel()).collect(Collectors.joining(",")),
				1, matches.size());

	}

	private static ItemDescription r(String label, String resultText) {
		return new ItemDescription(label, resultText);
	}

	private void testCompletionFor(String value, List<ItemDescription> expectedItems) throws BadLocationException {
		testCompletionFor(value, expectedItems, null);
	}

	private static class ItemDescription {
		public final String label;

		public final String resultText;

		public ItemDescription(String label, String resultText) {
			this.label = label;
			this.resultText = resultText;
		}
	}
}
