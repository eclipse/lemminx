package org.eclipse.lsp4xml;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.contentmodel.participants.diagnostics.IXMLErrorCode;
import org.eclipse.lsp4xml.contentmodel.settings.ContentModelSettings;
import org.eclipse.lsp4xml.internal.parser.XMLParser;
import org.eclipse.lsp4xml.model.XMLDocument;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.eclipse.lsp4xml.services.extensions.CompletionSettings;
import org.junit.Assert;

public class XMLAssert {

	// ------------------- Completion assert

	public static void testCompletionFor(String value, ItemDescription... expectedItems) throws BadLocationException {
		testCompletionFor(value, null, expectedItems);
	}

	public static void testCompletionFor(String value, String catalogPath, ItemDescription... expectedItems)
			throws BadLocationException {
		testCompletionFor(value, catalogPath, null, expectedItems);
	}

	public static void testCompletionFor(String value, String catalogPath, Integer expectedCount,
			ItemDescription... expectedItems) throws BadLocationException {
		int offset = value.indexOf('|');
		value = value.substring(0, offset) + value.substring(offset + 1);

		TextDocument document = new TextDocument(value, "test://test/test.html");
		Position position = document.positionAt(offset);
		XMLDocument htmlDoc = XMLParser.getInstance().parse(document);

		XMLLanguageService xmlLanguageService = new XMLLanguageService();

		// Configure XML catalog for XML schema
		if (catalogPath != null) {
			ContentModelSettings settings = new ContentModelSettings();
			settings.setCatalogs(new String[] { catalogPath });
			xmlLanguageService.updateSettings(settings);
		}
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

	public static ItemDescription r(String label, String resultText) {
		return new ItemDescription(label, resultText);
	}

	public static class ItemDescription {
		public final String label;

		public final String resultText;

		public ItemDescription(String label, String resultText) {
			this.label = label;
			this.resultText = resultText;
		}
	}

	public static void testTagCompletion(String value, String expected) throws BadLocationException {
		int offset = value.indexOf('|');
		value = value.substring(0, offset) + value.substring(offset + 1);

		XMLLanguageService ls = new XMLLanguageService();

		TextDocument document = new TextDocument(value, "test://test/test.html");
		Position position = document.positionAt(offset);
		XMLDocument htmlDoc = XMLParser.getInstance().parse(document);

		String actual = ls.doTagComplete(htmlDoc, position);
		Assert.assertEquals(expected, actual);
	}
	
	// ------------------- Diagnostics assert

	public static void testDiagnosticsFor(String xml, Diagnostic... expected) {
		testDiagnosticsFor(xml, null, expected);
	}

	public static void testDiagnosticsFor(String xml, String catalogPath, Diagnostic... expected) {
		TextDocument document = new TextDocument(xml.toString(), "test.xml");

		XMLLanguageService xmlLanguageService = new XMLLanguageService();

		if (catalogPath != null) {
			// Configure XML catalog for XML schema
			ContentModelSettings settings = new ContentModelSettings();
			settings.setCatalogs(new String[] { catalogPath });
			xmlLanguageService.updateSettings(settings);
		}

		List<Diagnostic> actulal = xmlLanguageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(actulal, expected);

	}

	public static void assertDiagnostics(List<Diagnostic> actual, Diagnostic... expected) {
		actual.stream().forEach(d -> {
			// we don't want to compare severity, message, etc
			d.setSeverity(null);
			d.setMessage(null);
			d.setSource(null);
		});
		Assert.assertEquals(expected.length, actual.size());
		Assert.assertArrayEquals(expected, actual.toArray());
	}

	public static Diagnostic d(int startLine, int startCharacter, int endLine, int endCharacter, IXMLErrorCode code) {
		return new Diagnostic(new Range(new Position(startLine, startCharacter), new Position(endLine, endCharacter)),
				null, null, null, code.getCode());
	}
}
