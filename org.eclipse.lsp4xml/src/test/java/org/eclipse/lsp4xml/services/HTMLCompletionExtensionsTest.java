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
package org.eclipse.lsp4xml.services;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.extensions.CompletionSettings;
import org.eclipse.lsp4xml.extensions.ICompletionParticipant;
import org.eclipse.lsp4xml.extensions.ICompletionRequest;
import org.eclipse.lsp4xml.extensions.ICompletionResponse;
import org.eclipse.lsp4xml.extensions.XMLExtensionAdapter;
import org.eclipse.lsp4xml.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.internal.parser.XMLParser;
import org.eclipse.lsp4xml.model.XMLDocument;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link ICompletionParticipant} extension. The tests use
 * {@link HTMLPluginCompletion} to manage HTML completion.
 *
 */
public class HTMLCompletionExtensionsTest {

	@Test
	public void testHTMLCompletion() throws BadLocationException {

		testCompletionFor("<|", Arrays.asList(r("iframe", "<iframe"), //
				r("h1", "<h1"), //
				r("div", "<div")));

		testCompletionFor("< |", Arrays.asList(r("iframe", "<iframe"), //
				r("h1", "<h1"), //
				r("div", "<div")));

		testCompletionFor("<h|", Arrays.asList(r("html", "<html"), //
				r("h1", "<h1"), //
				r("header", "<header")));

		testCompletionFor("<input|", Arrays.asList(r("input", "<input")));

		testCompletionFor("<inp|ut", Arrays.asList(r("input", "<input")));

		testCompletionFor("<|inp", Arrays.asList(r("input", "<input")));
	}

	private static ItemDescription r(String label, String resultText) {
		return new ItemDescription(label, resultText);
	}

	private void testCompletionFor(String value, List<ItemDescription> expectedItems) throws BadLocationException {
		testCompletionFor(value, expectedItems, null);
	}

	private static void testCompletionFor(String value, List<ItemDescription> expectedItems, Integer expectedCount)
			throws BadLocationException {
		int offset = value.indexOf('|');
		value = value.substring(0, offset) + value.substring(offset + 1);

		TextDocument document = new TextDocument(value, "test://test/test.html");
		Position position = document.positionAt(offset);
		XMLDocument htmlDoc = XMLParser.getInstance().parse(document);

		XMLExtensionsRegistry registry = new XMLExtensionsRegistry();
		registry.registerExtension(new HTMLPluginCompletion());
		XMLLanguageService htmlLanguageService = new XMLLanguageService(registry);
		CompletionList list = htmlLanguageService.doComplete(htmlDoc, position, new CompletionSettings(),
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

	private static class ItemDescription {
		public final String label;

		public final String resultText;

		public ItemDescription(String label, String resultText) {
			this.label = label;
			this.resultText = resultText;
		}
	}

	private static class HTMLPluginCompletion extends XMLExtensionAdapter implements ICompletionParticipant {

		@Override
		public ICompletionParticipant getCompletionParticipant() {
			return this;
		}

		@Override
		public void onAttributeName(String namePrefix, Range fullRange, ICompletionRequest request,
				ICompletionResponse response) {

		}

		@Override
		public void onAttributeValue(String valuePrefix, Range fullRange, ICompletionRequest request,
				ICompletionResponse response) {
		}

		@Override
		public void onXMLContent(ICompletionRequest request, ICompletionResponse response) {
		}

		@Override
		public void onTagOpen(ICompletionRequest completionRequest, ICompletionResponse completionResponse)
				throws Exception {
			HTMLTag.HTML_TAGS.forEach(t -> {
				String tag = t.getTag();
				String label = t.getLabel();
				Range range = completionRequest.getReplaceRange();
				CompletionItem item = new CompletionItem();
				item.setLabel(tag);
				item.setKind(CompletionItemKind.Property);
				item.setDocumentation(Either.forLeft(label));
				item.setTextEdit(new TextEdit(range, tag));
				item.setInsertTextFormat(InsertTextFormat.PlainText);
				completionResponse.addCompletionItem(item);
			});

		}
	}
}
