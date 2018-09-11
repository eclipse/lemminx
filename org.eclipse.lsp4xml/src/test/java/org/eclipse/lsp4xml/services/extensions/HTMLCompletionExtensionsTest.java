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
package org.eclipse.lsp4xml.services.extensions;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.dom.XMLParser;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;
import org.junit.Assert;
import org.junit.Test;

/**
 * XML completion tests which uses the {@link ICompletionParticipant} to emulate
 * HTML language.
 *
 */
public class HTMLCompletionExtensionsTest {

	@Test
	public void testHTMLElementCompletion() throws BadLocationException {

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

	@Test
	public void testHTMLElementCompletionWithoutOpenStart() throws BadLocationException {

		/*testCompletionFor("|", Arrays.asList(r("iframe", "<iframe"), //
				r("h1", "<h1"), //
				r("div", "<div")));
	*/
		testCompletionFor(" |", Arrays.asList(r("iframe", "<iframe"), //
				r("h1", "<h1"), //
				r("div", "<div")));

		testCompletionFor("h|", Arrays.asList(r("html", "<html"), //
				r("h1", "<h1"), //
				r("header", "<header")));

		testCompletionFor("input|", Arrays.asList(r("input", "<input")));

		testCompletionFor("inp|ut", Arrays.asList(r("input", "<input")));

		testCompletionFor("|inp", Arrays.asList(r("input", "<input")));
	}
	@Test
	public void testHTMLAttributeNameCompletion() throws BadLocationException {

		testCompletionFor("<input |", Arrays.asList(r("type", "<input type=\"$1\""), //
				r("style", "<input style=\"$1\""), //
				r("onmousemove", "<input onmousemove=\"$1\"")));

		testCompletionFor("<input t|", Arrays.asList(r("type", "<input type=\"$1\""), //
				r("style", "<input tabindex=\"$1\"")));

		testCompletionFor("<input t|ype", Arrays.asList(r("type", "<input type=\"$1\""), //
				r("style", "<input tabindex=\"$1\"")));

		testCompletionFor("<input t|ype=\"text\"", Arrays.asList(r("type", "<input type=\"text\""), //
				r("style", "<input tabindex=\"text\"")));

		testCompletionFor("<input type=\"text\" |", Arrays.asList(r("style", "<input type=\"text\" style=\"$1\""), //
				r("type", "<input type=\"text\" style=\"$1\""), //
				r("size", "<input type=\"text\" size=\"$1\"")));

		testCompletionFor("<input type=\"text\" s|", Arrays.asList(r("type", "<input type=\"text\""), //
				r("src", "<input type=\"text\" src=\"$1\""), //
				r("size", "<input type=\"text\" size=\"$1\"")));

		testCompletionFor("<input di| type=\"text\"",
				Arrays.asList(r("disabled", "<input disabled=\"$1\" type=\"text\""), //
						r("dir", "<input dir=\"$1\" type=\"text\"")));

		testCompletionFor("<input disabled | type=\"text\"",
				Arrays.asList(r("dir", "<input disabled dir=\"$1\" type=\"text\""), //
						r("style", "<input disabled style=\"$1\" type=\"text\"")));

	}

	@Test
	public void testHTMLAttributeValueCompletion() throws BadLocationException {

		testCompletionFor("<input type=|", Arrays.asList(r("text", "<input type=\"text\""), //
				r("checkbox", "<input type=\"checkbox\"")));

	}

	private static void testCompletionFor(String value, List<ItemDescription> expectedItems, Integer expectedCount)
			throws BadLocationException {
		int offset = value.indexOf('|');
		value = value.substring(0, offset) + value.substring(offset + 1);

		TextDocument document = new TextDocument(value, "test://test/test.html");
		Position position = document.positionAt(offset);
		XMLDocument htmlDoc = XMLParser.getInstance().parse(document);

		XMLLanguageService htmlLanguageService = new HTMLLanguageService();
		CompletionList list = htmlLanguageService.doComplete(htmlDoc, position, new CompletionSettings(),
				new XMLFormattingOptions(4, false));

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

	private static class HTMLLanguageService extends XMLLanguageService {

		public HTMLLanguageService() {
			super.registerCompletionParticipant(new HTMLCompletionParticipant());
		}

		class HTMLCompletionParticipant extends CompletionParticipantAdapter {

			@Override
			public void onTagOpen(ICompletionRequest completionRequest, ICompletionResponse completionResponse)
					throws Exception {
				Range range = completionRequest.getReplaceRange();
				HTMLTag.HTML_TAGS.forEach(t -> {
					String tag = t.getTag();
					String label = t.getLabel();
					CompletionItem item = new CompletionItem();
					item.setLabel(tag);
					item.setKind(CompletionItemKind.Property);
					item.setDocumentation(Either.forLeft(label));
					item.setTextEdit(new TextEdit(range, tag));
					item.setInsertTextFormat(InsertTextFormat.PlainText);
					completionResponse.addCompletionItem(item);
				});
			}

			@Override
			public void onAttributeName(String value, Range replaceRange, ICompletionRequest completionRequest,
					ICompletionResponse completionResponse) {
				String tag = completionRequest.getCurrentTag();
				HTMLTag htmlTag = HTMLTag.getHTMLTag(tag);
				if (htmlTag != null) {
					String[] attributes = htmlTag.getAttributes();
					if (attributes != null) {
						for (String attribute : attributes) {
							int index = attribute.indexOf(":");
							if (index != -1) {
								attribute = attribute.substring(0, index);
							}
							if (!completionResponse.hasAttribute(attribute)) {
								CompletionItem item = new CompletionItem();
								item.setLabel(attribute);
								item.setKind(CompletionItemKind.Value);
								item.setTextEdit(new TextEdit(replaceRange, attribute + value));
								item.setInsertTextFormat(InsertTextFormat.Snippet);
								completionResponse.addCompletionAttribute(item);
							}
						}
					}
				}
			}

			@Override
			public void onAttributeValue(String valuePrefix, Range fullRange, boolean addQuotes,
					ICompletionRequest completionRequest, ICompletionResponse completionResponse) {
				String tag = completionRequest.getCurrentTag();
				String attributeName = completionRequest.getCurrentAttributeName();
				HTMLTag htmlTag = HTMLTag.getHTMLTag(tag);
				if (htmlTag != null) {
					String[] attributes = htmlTag.getAttributes();
					if (attributes != null) {
						for (String attribute : attributes) {
							String attrName = attribute;
							String attrType = null;
							int index = attribute.indexOf(":");
							if (index != -1) {
								attrName = attribute.substring(0, index);
								attrType = attribute.substring(index + 1, attribute.length());
							}
							if (attrType != null && attributeName.equals(attrName)) {
								String[] values = HTMLTag.getAttributeValues(attrType);
								for (String value : values) {
									String insertText = addQuotes ? '"' + value + '"' : value;

									CompletionItem item = new CompletionItem();
									item.setLabel(value);
									item.setFilterText(insertText);
									item.setKind(CompletionItemKind.Unit);
									item.setTextEdit(new TextEdit(fullRange, insertText));
									item.setInsertTextFormat(InsertTextFormat.PlainText);
									completionResponse.addCompletionAttribute(item);
								}
								break;
							}
						}
					}
				}
			}
		}
	}
}
