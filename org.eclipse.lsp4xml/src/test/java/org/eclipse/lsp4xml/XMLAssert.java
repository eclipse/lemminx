package org.eclipse.lsp4xml;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.contentmodel.participants.diagnostics.IXMLErrorCode;
import org.eclipse.lsp4xml.contentmodel.settings.ContentModelSettings;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.dom.XMLParser;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.eclipse.lsp4xml.services.extensions.CompletionSettings;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;
import org.junit.Assert;

public class XMLAssert {

	// ------------------- Completion assert

	private static final String FILE_URI = "test.xml";

	public static void testCompletionFor(String value, ItemDescription... expectedItems) throws BadLocationException {
		testCompletionFor(value, null, expectedItems);
	}

	public static void testCompletionFor(String value, String catalogPath, ItemDescription... expectedItems)
			throws BadLocationException {
		testCompletionFor(value, catalogPath, null, null, expectedItems);
	}

	public static void testCompletionFor(String value, String catalogPath, String fileURI, Integer expectedCount,
			ItemDescription... expectedItems) throws BadLocationException {
		int offset = value.indexOf('|');
		value = value.substring(0, offset) + value.substring(offset + 1);

		TextDocument document = new TextDocument(value, fileURI != null ? fileURI : "test://test/test.html");
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
		TextDocument document = new TextDocument(xml.toString(), FILE_URI);

		XMLLanguageService xmlLanguageService = new XMLLanguageService();

		if (catalogPath != null) {
			// Configure XML catalog for XML schema
			ContentModelSettings settings = new ContentModelSettings();
			settings.setCatalogs(new String[] { catalogPath });
			xmlLanguageService.updateSettings(settings);
		}

		List<Diagnostic> actual = xmlLanguageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(actual, expected);

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
		return new Diagnostic(r(startLine, startCharacter, endLine, endCharacter), null, null, null, code.getCode());
	}

	public static Range r(int startLine, int startCharacter, int endLine, int endCharacter) {
		return new Range(new Position(startLine, startCharacter), new Position(endLine, endCharacter));
	}

	// ------------------- CodeAction assert

	public static void testCodeActionsFor(String xml, Diagnostic diagnostic, CodeAction... expected) {
		testCodeActionsFor(xml, diagnostic, null, expected);
	}

	public static void testCodeActionsFor(String xml, Diagnostic diagnostic, String catalogPath,
			CodeAction... expected) {
		TextDocument document = new TextDocument(xml.toString(), FILE_URI);

		XMLLanguageService xmlLanguageService = new XMLLanguageService();

		if (catalogPath != null) {
			// Configure XML catalog for XML schema
			ContentModelSettings settings = new ContentModelSettings();
			settings.setCatalogs(new String[] { catalogPath });
			xmlLanguageService.updateSettings(settings);
		}

		CodeActionContext context = new CodeActionContext();
		context.setDiagnostics(Arrays.asList(diagnostic));
		Range range = diagnostic.getRange();
		XMLDocument xmlDoc = XMLParser.getInstance().parse(document);
		List<CodeAction> actual = xmlLanguageService.doCodeActions(context, range, xmlDoc);
		assertCodeActions(actual, expected);
	}

	public static void assertCodeActions(List<CodeAction> actual, CodeAction... expected) {
		actual.stream().forEach(ca -> {
			// we don't want to compare title, etc
			ca.setCommand(null);
			ca.setKind(null);
			ca.setTitle(null);
			if (ca.getDiagnostics() != null) {
				ca.getDiagnostics().forEach(d -> {
					d.setSeverity(null);
					d.setMessage(null);
					d.setSource(null);
				});
			}
		});
		Assert.assertEquals(expected.length, actual.size());
		Assert.assertArrayEquals(expected, actual.toArray());
	}

	public static CodeAction ca(Diagnostic d, TextEdit te) {
		CodeAction codeAction = new CodeAction();
		codeAction.setDiagnostics(Arrays.asList(d));

		VersionedTextDocumentIdentifier versionedTextDocumentIdentifier = new VersionedTextDocumentIdentifier(FILE_URI,
				0);

		WorkspaceEdit workspaceEdit = new WorkspaceEdit(
				Arrays.asList(new TextDocumentEdit(versionedTextDocumentIdentifier, Arrays.asList(te))));
		codeAction.setEdit(workspaceEdit);
		return codeAction;
	}

	public static TextEdit te(int startLine, int startCharacter, int endLine, int endCharacter, String newText) {
		TextEdit textEdit = new TextEdit();
		textEdit.setNewText(newText);
		textEdit.setRange(r(startLine, startCharacter, endLine, endCharacter));
		return textEdit;
	}
}