package org.eclipse.lsp4xml;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.dom.XMLParser;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.diagnostics.IXMLErrorCode;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.ContentModelSettings;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.eclipse.lsp4xml.services.extensions.CompletionSettings;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;
import org.junit.Assert;

public class XMLAssert {

	// ------------------- Completion assert

	private static final String FILE_URI = "test.xml";

	public static void testCompletionFor(String value, CompletionItem... expectedItems) throws BadLocationException {
		testCompletionFor(value, null, expectedItems);
	}

	public static void testCompletionFor(String value, String catalogPath, CompletionItem... expectedItems)
			throws BadLocationException {
		testCompletionFor(value, catalogPath, null, null, expectedItems);
	}

	public static void testCompletionFor(String value, int expectedCount, CompletionItem... expectedItems)
			throws BadLocationException {
		testCompletionFor(value, null, null, expectedCount, expectedItems);
	}

	public static void testCompletionFor(String value, String catalogPath, String fileURI, Integer expectedCount,
			CompletionItem... expectedItems) throws BadLocationException {
		testCompletionFor(new XMLLanguageService(), value, catalogPath, fileURI, expectedCount, true, expectedItems);
	}

	public static void testCompletionFor(String value, boolean autoCloseTags, CompletionItem... expectedItems)
			throws BadLocationException {
		testCompletionFor(new XMLLanguageService(), value, null, null, null, autoCloseTags, expectedItems);
	}

	public static void testCompletionFor(XMLLanguageService xmlLanguageService, String value, String catalogPath,
			String fileURI, Integer expectedCount, boolean autoCloseTags, CompletionItem... expectedItems)
			throws BadLocationException {
		int offset = value.indexOf('|');
		value = value.substring(0, offset) + value.substring(offset + 1);

		TextDocument document = new TextDocument(value, fileURI != null ? fileURI : "test://test/test.html");
		Position position = document.positionAt(offset);
		XMLDocument htmlDoc = XMLParser.getInstance().parse(document);
		xmlLanguageService.setDocumentProvider((uri) -> htmlDoc);
		
		// Configure XML catalog for XML schema
		if (catalogPath != null) {
			ContentModelSettings settings = new ContentModelSettings();
			settings.setCatalogs(new String[] { catalogPath });
			xmlLanguageService.updateSettings(settings);
		}
		CompletionList list = xmlLanguageService.doComplete(htmlDoc, position, new CompletionSettings(autoCloseTags),
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
			Assert.assertEquals(expectedCount.intValue(), list.getItems().size());
		}
		if (expectedItems != null) {
			for (CompletionItem item : expectedItems) {
				assertCompletion(list, item, document, offset);
			}
		}
	}

	private static void assertCompletion(CompletionList completions, CompletionItem expected, TextDocument document,
			int offset) {
		List<CompletionItem> matches = completions.getItems().stream().filter(completion -> {
			return expected.getLabel().equals(completion.getLabel());
		}).collect(Collectors.toList());

		Assert.assertEquals(
				expected.getLabel() + " should only exist once: Actual: "
						+ completions.getItems().stream().map(c -> c.getLabel()).collect(Collectors.joining(",")),
				1, matches.size());

		CompletionItem match = matches.get(0);
		/*
		 * if (expected.documentation != null) {
		 * Assert.assertEquals(match.getDocumentation().getRight().getValue(),
		 * expected.getd); } if (expected.kind) { Assert.assertEquals(match.kind,
		 * expected.kind); }
		 */
		if (expected.getTextEdit() != null && match.getTextEdit() != null) {
			if (expected.getTextEdit().getNewText() != null) {
				Assert.assertEquals(expected.getTextEdit().getNewText(), match.getTextEdit().getNewText());
			}
			if (expected.getTextEdit().getRange() != null) {
				Assert.assertEquals(expected.getTextEdit().getRange(), match.getTextEdit().getRange());
			}
		}
		if (expected.getFilterText() != null && match.getFilterText() != null) {
			Assert.assertEquals(expected.getFilterText(), match.getFilterText());
		}

	}

	public static CompletionItem c(String label, TextEdit textEdit, String filterText) {
		CompletionItem item = new CompletionItem();
		item.setLabel(label);
		item.setFilterText(filterText);
		item.setTextEdit(textEdit);
		return item;
	}

	public static CompletionItem c(String label, String newText) {
		return c(label, newText, null);
	}

	public static CompletionItem c(String label, String newText, String filterText) {
		return c(label, newText, null, filterText);
	}

	public static CompletionItem c(String label, String newText, Range range, String filterText) {
		return c(label, new TextEdit(range, newText), filterText);
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
		XMLDocument xmlDocument = XMLParser.getInstance().parse(document);
		XMLLanguageService xmlLanguageService = new XMLLanguageService();

		if (catalogPath != null) {
			// Configure XML catalog for XML schema
			ContentModelSettings settings = new ContentModelSettings();
			settings.setCatalogs(new String[] { catalogPath });
			xmlLanguageService.updateSettings(settings);
		}

		List<Diagnostic> actual = xmlLanguageService.doDiagnostics(xmlDocument, () -> {
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
		List<CodeAction> actual = xmlLanguageService.doCodeActions(context, range, xmlDoc,
				new XMLFormattingOptions(4, false));
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

	// ------------------- Hover assert

	public static void assertHover(String value) throws BadLocationException {
		assertHover(value, null, null);
	}

	public static void assertHover(String value, String expectedHoverLabel, Integer expectedHoverOffset)
			throws BadLocationException {
		assertHover(new XMLLanguageService(), value, null, expectedHoverLabel, expectedHoverOffset);
	}

	public static void assertHover(XMLLanguageService xmlLanguageService, String value, String catalogPath,
			String expectedHoverLabel, Integer expectedHoverOffset) throws BadLocationException {
		int offset = value.indexOf("|");
		value = value.substring(0, offset) + value.substring(offset + 1);

		TextDocument document = new TextDocument(value, "test://test/test.html");

		Position position = document.positionAt(offset);

		XMLDocument htmlDoc = XMLParser.getInstance().parse(document);
		// Configure XML catalog for XML schema
		if (catalogPath != null) {
			ContentModelSettings settings = new ContentModelSettings();
			settings.setCatalogs(new String[] { catalogPath });
			xmlLanguageService.updateSettings(settings);
		}

		Hover hover = xmlLanguageService.doHover(htmlDoc, position);
		if (expectedHoverLabel == null) {
			Assert.assertNull(hover);
		} else {
			String actualHoverLabel = getHoverLabel(hover);
			Assert.assertEquals(expectedHoverLabel, actualHoverLabel);
			if (expectedHoverOffset != null) {
				Assert.assertNotNull(hover.getRange());
				Assert.assertNotNull(hover.getRange().getStart());
				Assert.assertEquals(expectedHoverOffset.intValue(), hover.getRange().getStart().getCharacter());
			}
		}
	}

	private static String getHoverLabel(Hover hover) {
		Either<List<Either<String, MarkedString>>, MarkupContent> contents = hover.getContents();
		if (contents == null) {
			return null;
		}
		return contents.getRight().getValue();
	}

}