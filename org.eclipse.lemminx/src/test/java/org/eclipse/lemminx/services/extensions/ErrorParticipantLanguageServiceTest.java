/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services.extensions;

import static org.eclipse.lemminx.XMLAssert.assertFormat;
import static org.eclipse.lemminx.XMLAssert.assertHighlights;
import static org.eclipse.lemminx.XMLAssert.assertHover;
import static org.eclipse.lemminx.XMLAssert.assertRename;
import static org.eclipse.lemminx.XMLAssert.c;
import static org.eclipse.lemminx.XMLAssert.ca;
import static org.eclipse.lemminx.XMLAssert.cl;
import static org.eclipse.lemminx.XMLAssert.d;
import static org.eclipse.lemminx.XMLAssert.dl;
import static org.eclipse.lemminx.XMLAssert.ds;
import static org.eclipse.lemminx.XMLAssert.hl;
import static org.eclipse.lemminx.XMLAssert.ll;
import static org.eclipse.lemminx.XMLAssert.r;
import static org.eclipse.lemminx.XMLAssert.si;
import static org.eclipse.lemminx.XMLAssert.te;
import static org.eclipse.lemminx.XMLAssert.testCodeActionsFor;
import static org.eclipse.lemminx.XMLAssert.testCodeLensFor;
import static org.eclipse.lemminx.XMLAssert.testCompletionFor;
import static org.eclipse.lemminx.XMLAssert.testDefinitionFor;
import static org.eclipse.lemminx.XMLAssert.testDiagnosticsFor;
import static org.eclipse.lemminx.XMLAssert.testDocumentLinkFor;
import static org.eclipse.lemminx.XMLAssert.testDocumentSymbolsFor;
import static org.eclipse.lemminx.XMLAssert.testReferencesFor;
import static org.eclipse.lemminx.XMLAssert.testSymbolInformationsFor;
import static org.eclipse.lemminx.XMLAssert.testTypeDefinitionFor;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.extensions.contentmodel.participants.XMLSyntaxErrorCode;
import org.eclipse.lemminx.extensions.contentmodel.settings.ContentModelSettings;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.services.DocumentSymbolsResult;
import org.eclipse.lemminx.services.SymbolInformationResult;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionRequest;
import org.eclipse.lemminx.services.extensions.codelens.ICodeLensParticipant;
import org.eclipse.lemminx.services.extensions.codelens.ICodeLensRequest;
import org.eclipse.lemminx.services.extensions.completion.ICompletionParticipant;
import org.eclipse.lemminx.services.extensions.completion.ICompletionRequest;
import org.eclipse.lemminx.services.extensions.completion.ICompletionResponse;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.services.extensions.format.IFormatterParticipant;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lemminx.settings.XMLSymbolFilter;
import org.eclipse.lemminx.settings.XMLSymbolSettings;
import org.eclipse.lemminx.utils.XMLBuilder;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.junit.jupiter.api.Test;

/**
 * Tests to ensure that if a participant throws an exception, the language
 * server still functions
 *
 * @author datho7561
 */
public class ErrorParticipantLanguageServiceTest {

	/**
	 * Adds participants that throw runtime exceptions, as well as simple
	 * participants that always return the same result. Does not include all the
	 * default participants.
	 */
	static class ErrorParticipantLanguageService extends XMLLanguageService {

		public static final CodeLens TEST_CODE_LENS = cl(r(0, 0, 0, 0), "a", "a");
		public static final LocationLink TEST_LOCATION_LINK = ll("src/hello", r(0, 0, 0, 2), r(0, 0, 0, 1));
		public static final Diagnostic TEST_DIAGNOSTIC = d(0, 0, 0, XMLSyntaxErrorCode.ElementUnterminated);
		public static final DocumentLink TEST_DOCLINK = dl(r(0, 0, 0, 0), "src/hello");
		public static final DocumentHighlight TEST_HIGHLIGHT = hl(r(0, 2, 0, 3));
		public static final Hover TEST_HOVER = new Hover(new MarkupContent("markdown", "contents"), r(0, 0, 0, 2));
		public static final Location TEST_LOCATION = new Location("src/hello", r(0, 0, 0, 1));
		private static final SymbolInformation TEST_SI = si("hello", SymbolKind.Array, TEST_LOCATION, "hi");
		protected static final CompletionItem TEST_COMPLETION_ITEM = c("aaa", "bbb");
		protected static final DocumentSymbol TEST_DS = ds("hello", SymbolKind.Array, r(0, 0, 0, 1), r(0, 0, 0, 1),
				"detail", Collections.emptyList());

		public ErrorParticipantLanguageService() {
			super();

			this.registerCodeActionParticipant(new ICodeActionParticipant() {
				@Override
				public void doCodeAction(ICodeActionRequest request, List<CodeAction> codeActions,
						CancelChecker cancelChecker) {
					throw new RuntimeException("This participant is broken");
				}
			});
			this.registerCodeActionParticipant(new ICodeActionParticipant() {
				@Override
				public void doCodeAction(ICodeActionRequest request, List<CodeAction> codeActions,
						CancelChecker cancelChecker) {
					Diagnostic diagnostic = request.getDiagnostic();
					codeActions.add(ca(diagnostic, te(0, 0, 0, 0, "a")));
				}
			});

			this.registerCodeLensParticipant(new ICodeLensParticipant() {
				@Override
				public void doCodeLens(ICodeLensRequest request, List<CodeLens> lenses, CancelChecker cancelChecker) {
					throw new RuntimeException("This participant is broken");
				}
			});
			this.registerCodeLensParticipant(new ICodeLensParticipant() {
				@Override
				public void doCodeLens(ICodeLensRequest request, List<CodeLens> lenses, CancelChecker cancelChecker) {
					lenses.add(TEST_CODE_LENS);
				}
			});

			this.registerCompletionParticipant(new ICompletionParticipant() {

				@Override
				public void onTagOpen(ICompletionRequest completionRequest, ICompletionResponse completionResponse,
						CancelChecker cancelChecker) throws Exception {
					throw new RuntimeException("This participant is broken");
				}

				@Override
				public void onXMLContent(ICompletionRequest request, ICompletionResponse response,
						CancelChecker cancelChecker) throws Exception {
					throw new RuntimeException("This participant is broken");
				}

				@Override
				public void onAttributeName(boolean generateValue, ICompletionRequest request,
						ICompletionResponse response, CancelChecker cancelChecker) throws Exception {
					throw new RuntimeException("This participant is broken");
				}

				@Override
				public void onAttributeValue(String valuePrefix, ICompletionRequest request,
						ICompletionResponse response, CancelChecker cancelChecker) throws Exception {
					throw new RuntimeException("This participant is broken");
				}

				@Override
				public void onDTDSystemId(String valuePrefix, ICompletionRequest request, ICompletionResponse response,
						CancelChecker cancelChecker) throws Exception {
					throw new RuntimeException("This participant is broken");
				}

			});
			this.registerCompletionParticipant(new ICompletionParticipant() {

				@Override
				public void onTagOpen(ICompletionRequest completionRequest, ICompletionResponse completionResponse,
						CancelChecker cancelChecker) throws Exception {
					completionResponse.addCompletionAttribute(TEST_COMPLETION_ITEM);
				}

				@Override
				public void onXMLContent(ICompletionRequest request, ICompletionResponse response,
						CancelChecker cancelChecker) throws Exception {
					response.addCompletionAttribute(TEST_COMPLETION_ITEM);
				}

				@Override
				public void onAttributeName(boolean generateValue, ICompletionRequest request,
						ICompletionResponse response, CancelChecker cancelChecker) throws Exception {
					response.addCompletionAttribute(TEST_COMPLETION_ITEM);
				}

				@Override
				public void onAttributeValue(String valuePrefix, ICompletionRequest request,
						ICompletionResponse response, CancelChecker cancelChecker) throws Exception {
					response.addCompletionAttribute(TEST_COMPLETION_ITEM);
				}

				@Override
				public void onDTDSystemId(String valuePrefix, ICompletionRequest request, ICompletionResponse response,
						CancelChecker cancelChecker) throws Exception {
					response.addCompletionAttribute(TEST_COMPLETION_ITEM);
				}

			});

			this.registerDefinitionParticipant(new IDefinitionParticipant() {
				@Override
				public void findDefinition(IDefinitionRequest request, List<LocationLink> locations,
						CancelChecker cancelChecker) {
					throw new RuntimeException("This participant is broken");
				}
			});
			this.registerDefinitionParticipant(new IDefinitionParticipant() {
				@Override
				public void findDefinition(IDefinitionRequest request, List<LocationLink> locations,
						CancelChecker cancelChecker) {
					locations.add(TEST_LOCATION_LINK);
				}
			});

			this.registerDiagnosticsParticipant(new IDiagnosticsParticipant() {
				@Override
				public void doDiagnostics(DOMDocument xmlDocument, List<Diagnostic> diagnostics,
						XMLValidationSettings validationSettings, CancelChecker cancelChecker) {
					throw new RuntimeException("This participant is broken");
				}
			});
			this.registerDiagnosticsParticipant(new IDiagnosticsParticipant() {
				@Override
				public void doDiagnostics(DOMDocument xmlDocument, List<Diagnostic> diagnostics,
						XMLValidationSettings validationSettings, CancelChecker cancelChecker) {
					diagnostics.add(TEST_DIAGNOSTIC);
				}
			});

			this.registerDocumentLinkParticipant(new IDocumentLinkParticipant() {
				@Override
				public void findDocumentLinks(DOMDocument document, List<DocumentLink> links) {
					throw new RuntimeException("This participant is broken");
				}
			});
			this.registerDocumentLinkParticipant(new IDocumentLinkParticipant() {
				@Override
				public void findDocumentLinks(DOMDocument document, List<DocumentLink> links) {
					links.add(new DocumentLink(TEST_DOCLINK.getRange(),
							Paths.get(TEST_DOCLINK.getTarget()).toUri().toString()));

				}
			});

			this.registerFormatterParticipant(new IFormatterParticipant() {
				@Override
				public boolean formatAttributeValue(String name, String valueWithoutQuote, Character quote,
						DOMAttr attr, XMLBuilder xml) {
					throw new RuntimeException("This participant is broken");
				}
			});

			this.registerHighlightingParticipant(new IHighlightingParticipant() {
				@Override
				public void findDocumentHighlights(DOMNode node, Position position, int offset,
						List<DocumentHighlight> highlights, CancelChecker cancelChecker) {
					throw new RuntimeException("This participant is broken");
				}
			});
			this.registerHighlightingParticipant(new IHighlightingParticipant() {
				@Override
				public void findDocumentHighlights(DOMNode node, Position position, int offset,
						List<DocumentHighlight> highlights, CancelChecker cancelChecker) {
					highlights.add(TEST_HIGHLIGHT);
				}
			});

			this.registerHoverParticipant(new IHoverParticipant() {

				@Override
				public Hover onTag(IHoverRequest request) throws Exception {
					throw new RuntimeException("This participant is broken");
				}

				@Override
				public Hover onAttributeName(IHoverRequest request) throws Exception {
					throw new RuntimeException("This participant is broken");
				}

				@Override
				public Hover onAttributeValue(IHoverRequest request) throws Exception {
					throw new RuntimeException("This participant is broken");
				}

				@Override
				public Hover onText(IHoverRequest request) throws Exception {
					throw new RuntimeException("This participant is broken");
				}

			});
			this.registerHoverParticipant(new IHoverParticipant() {

				@Override
				public Hover onTag(IHoverRequest request) throws Exception {
					return TEST_HOVER;
				}

				@Override
				public Hover onAttributeName(IHoverRequest request) throws Exception {
					return TEST_HOVER;
				}

				@Override
				public Hover onAttributeValue(IHoverRequest request) throws Exception {
					return TEST_HOVER;
				}

				@Override
				public Hover onText(IHoverRequest request) throws Exception {
					return TEST_HOVER;
				}

			});

			this.registerReferenceParticipant(new IReferenceParticipant() {
				@Override
				public void findReference(DOMDocument document, Position position, ReferenceContext context,
						List<Location> locations, CancelChecker cancelChecker) {
					throw new RuntimeException("This participant is broken");
				}
			});
			this.registerReferenceParticipant(new IReferenceParticipant() {
				@Override
				public void findReference(DOMDocument document, Position position, ReferenceContext context,
						List<Location> locations, CancelChecker cancelChecker) {
					locations.add(TEST_LOCATION);
				}
			});

			this.registerRenameParticipant(new IRenameParticipant() {
				@Override
				public void doRename(IRenameRequest request, List<TextEdit> locations) {
					throw new RuntimeException("This participant is broken");
				}
			});

			this.registerSymbolsProviderParticipant(new ISymbolsProviderParticipant() {

				@Override
				public SymbolStrategy applyFor(DOMDocument document) {
					return SymbolStrategy.INSERT;
				}

				@Override
				public void findSymbolInformations(DOMDocument document, SymbolInformationResult symbols,
						XMLSymbolFilter filter, CancelChecker cancelChecker) {
					throw new RuntimeException("This participant is broken");
				}

				@Override
				public void findDocumentSymbols(DOMDocument document, DocumentSymbolsResult symbols,
						XMLSymbolFilter filter, CancelChecker cancelChecker) {
					throw new RuntimeException("This participant is broken");
				}

			});
			this.registerSymbolsProviderParticipant(new ISymbolsProviderParticipant() {

				@Override
				public SymbolStrategy applyFor(DOMDocument document) {
					return SymbolStrategy.INSERT;
				}

				@Override
				public void findSymbolInformations(DOMDocument document, SymbolInformationResult symbols,
						XMLSymbolFilter filter, CancelChecker cancelChecker) {
					symbols.add(TEST_SI);
				}

				@Override
				public void findDocumentSymbols(DOMDocument document, DocumentSymbolsResult symbols,
						XMLSymbolFilter filter, CancelChecker cancelChecker) {
					symbols.add(TEST_DS);
				}

			});
			this.registerSymbolsProviderParticipant(new ISymbolsProviderParticipant() {

				@Override
				public SymbolStrategy applyFor(DOMDocument document) {
					return SymbolStrategy.REPLACE;
				}

				@Override
				public void findSymbolInformations(DOMDocument document, SymbolInformationResult symbols,
						XMLSymbolFilter filter, CancelChecker cancelChecker) {
					throw new RuntimeException("This participant is broken");
				}

				@Override
				public void findDocumentSymbols(DOMDocument document, DocumentSymbolsResult symbols,
						XMLSymbolFilter filter, CancelChecker cancelChecker) {
					throw new RuntimeException("This participant is broken");
				}

			});

			this.registerTypeDefinitionParticipant(new ITypeDefinitionParticipant() {
				@Override
				public void findTypeDefinition(ITypeDefinitionRequest request, List<LocationLink> locations,
						CancelChecker cancelChecker) {
					throw new RuntimeException("This participant is broken");
				}
			});
			this.registerTypeDefinitionParticipant(new ITypeDefinitionParticipant() {
				@Override
				public void findTypeDefinition(ITypeDefinitionRequest request, List<LocationLink> locations,
						CancelChecker cancelChecker) {
					locations.add(TEST_LOCATION_LINK);
				}
			});

		}

	}

	// Actual tests

	@Test
	public void testCodeAction() throws BadLocationException {
		Diagnostic diagnostic = d(0, 0, 2, XMLSyntaxErrorCode.ElementUnterminated);
		testCodeActionsFor("", diagnostic, (String) null, null, new ErrorParticipantLanguageService(),
				ca(diagnostic, te(0, 0, 0, 0, "a")));
	}

	@Test
	public void testCodeLens() throws BadLocationException {
		testCodeLensFor("", null, new ErrorParticipantLanguageService(),
				ErrorParticipantLanguageService.TEST_CODE_LENS);
	}

	@Test
	public void testCompletion() throws BadLocationException {
		testCompletionFor(new ErrorParticipantLanguageService(), "<|aa bb=\"cc\">dd</aa>", null, (a) -> {
		}, null, 1, new SharedSettings(), ErrorParticipantLanguageService.TEST_COMPLETION_ITEM);
		testCompletionFor(new ErrorParticipantLanguageService(), "<aa bb=\"cc|\">dd</aa>", null, (a) -> {
		}, null, 1, new SharedSettings(), ErrorParticipantLanguageService.TEST_COMPLETION_ITEM);
		testCompletionFor(new ErrorParticipantLanguageService(), "<aa bb|=\"cc\">dd</aa>", null, (a) -> {
		}, null, 3, new SharedSettings(), //
				ErrorParticipantLanguageService.TEST_COMPLETION_ITEM, //
				c("xmlns", "xmlns", r(0, 4, 0, 6), "xmlns"), //
				c("xmlns:xsi", "xmlns:xsi", r(0, 4, 0, 6), "xmlns:xsi"));
		testCompletionFor(new ErrorParticipantLanguageService(), "<aa bb=\"cc\">dd|</aa>", null, (a) -> {
		}, null, 4, new SharedSettings(), //
				ErrorParticipantLanguageService.TEST_COMPLETION_ITEM);
		testCompletionFor(new ErrorParticipantLanguageService(), "<!DOCTYPE foo SYSTEM \"./zrb|\">", null, (a) -> {
		}, null, 1, new SharedSettings(), //
				ErrorParticipantLanguageService.TEST_COMPLETION_ITEM);
	}

	@Test
	public void testDefinition() throws BadLocationException {
		testDefinitionFor(new ErrorParticipantLanguageService(), "a|a", null,
				ErrorParticipantLanguageService.TEST_LOCATION_LINK);
	}

	@Test
	public void testDiagnostics() throws BadLocationException {
		testDiagnosticsFor(new ErrorParticipantLanguageService(), "<a", null, (a) -> {
		}, null, false, new ContentModelSettings(), //
				ErrorParticipantLanguageService.TEST_DIAGNOSTIC, //
				d(0, 1, 0, 2, XMLSyntaxErrorCode.NoGrammarConstraints, "No grammar constraints (DTD or XML Schema).",
						"test.xml", DiagnosticSeverity.Hint), //
				d(0, 1, 0, 2, XMLSyntaxErrorCode.MarkupEntityMismatch,
						"XML document structures must start and end within the same entity.", "xml",
						DiagnosticSeverity.Error));
	}

	@Test
	public void testDocLinks() throws BadLocationException {
		testDocumentLinkFor(new ErrorParticipantLanguageService(), "", null, null, //
				ErrorParticipantLanguageService.TEST_DOCLINK);
	}

	@Test
	public void testFormatting() throws BadLocationException {
		assertFormat(new ErrorParticipantLanguageService(), " <a b=\"\"></a>", "<a b=\"\"></a>", new SharedSettings(),
				"test://test.html", false);
	}

	@Test
	public void testHighlight() throws BadLocationException {
		assertHighlights(new ErrorParticipantLanguageService(), "|a a", new int[] { 2 }, "a");
	}

	@Test
	public void testHover() throws BadLocationException {
		assertHover(new ErrorParticipantLanguageService(), "<a|a bb=\"cc\">text</a>", null, null, "contents",
				r(0, 0, 0, 2));
		assertHover(new ErrorParticipantLanguageService(), "<aa b|b=\"cc\">text</a>", null, null, "contents",
				r(0, 0, 0, 2));
		assertHover(new ErrorParticipantLanguageService(), "<aa bb=\"c|c\">text</a>", null, null, "contents",
				r(0, 0, 0, 2));
		assertHover(new ErrorParticipantLanguageService(), "<aa bb=\"cc\">te|xt</a>", null, null, "contents",
				r(0, 0, 0, 2));
	}

	@Test
	public void testReferences() throws BadLocationException {
		testReferencesFor(new ErrorParticipantLanguageService(), "|a", "src/hello",
				ErrorParticipantLanguageService.TEST_LOCATION);
	}

	@Test
	public void testRename() throws BadLocationException {
		assertRename(new ErrorParticipantLanguageService(), "<|aa></aa>", "b",
				Arrays.asList(te(0, 1, 0, 3, "b"), te(0, 6, 0, 8, "b")));
	}

	@Test
	public void testSymbols() throws BadLocationException {
		testSymbolInformationsFor(new ErrorParticipantLanguageService(), "e", null, new XMLSymbolSettings(), (a) -> {
		}, //
				ErrorParticipantLanguageService.TEST_SI);
		testDocumentSymbolsFor(new ErrorParticipantLanguageService(), "e", "src/hello", new XMLSymbolSettings(),
				(a) -> {
				}, //
				ErrorParticipantLanguageService.TEST_DS);
	}

	@Test
	public void testTypeDefinition() throws BadLocationException {
		testTypeDefinitionFor(new ErrorParticipantLanguageService(), null, (a) -> {
		}, "|a", null, ErrorParticipantLanguageService.TEST_LOCATION_LINK);
	}

}
