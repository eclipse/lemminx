/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.emmet.participants;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMParser;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.eclipse.lsp4xml.services.extensions.ICompletionParticipant;
import org.eclipse.lsp4xml.settings.SharedSettings;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;
import org.junit.Assert;
import org.junit.Test;

/**
 * Emmet completion tests which uses the {@link ICompletionParticipant}.
 *
 */
public class EmmetCompletionTest {

	@Test
	public void testHTMLElementCompletion() throws BadLocationException {

		testCompletionFor("a>b|", Arrays.asList(r("a>b", "<a><b></b></a>")));
	}

	@Test
	public void testHTMLAttributeNameCompletion() throws BadLocationException {

		testCompletionFor("a>b[c]|", Arrays.asList(r("a>b[c]", "<a><b c=\"\"></b></a>")));

	}

	@Test
	public void testHTMLAttributeValueCompletion() throws BadLocationException {

	}

	private static void testCompletionFor(String value, List<ItemDescription> expectedItems, Integer expectedCount)
			throws BadLocationException {
		int offset = value.indexOf('|');
		value = value.substring(0, offset) + value.substring(offset + 1);

		TextDocument document = new TextDocument(value, "test://test/test.html");
		Position position = document.positionAt(offset);
		DOMDocument htmlDoc = DOMParser.getInstance().parse(document, null);

		XMLLanguageService xmlLanguageService = new XMLLanguageService();

		SharedSettings sharedSettings = new SharedSettings();
		
		CompletionList list = xmlLanguageService.doComplete(htmlDoc, position, sharedSettings);

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
