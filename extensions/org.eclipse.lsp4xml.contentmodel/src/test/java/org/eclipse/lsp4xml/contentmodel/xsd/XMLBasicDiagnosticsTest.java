package org.eclipse.lsp4xml.contentmodel.xsd;
/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.junit.Assert;
import org.junit.Test;

/**
 * XML diagnostics services tests
 *
 */
public class XMLBasicDiagnosticsTest {

	/**
	 * ElementUnterminated tests
	 * 
	 * @see https://wiki.xmldation.com/Support/Validator/ElementUnterminated
	 * @throws Exception
	 */
	@Test
	public void testElementUnterminated() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<Id>\r\n" + //
				"          <OrgId\r\n" + //
				"            <Othr>\r\n" + //
				"              <Id> 222010012</Id>\r\n" + //
				"            </Othr>\r\n" + //
				"          </OrgId>\r\n" + //
				"        </Id>") //
						.toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(1, 11, 1, 16, "ElementUnterminated"));
	}

	@Test
	public void testAttributeNotUnique() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<Id>\r\n" + //
				"          <OrgId att=\"val\" att=\"val2\">\r\n" + //
				"            <Othr>\r\n" + //
				"              <Id> 222010012</Id>\r\n" + //
				"            </Othr>\r\n" + //
				"          </OrgId>\r\n" + //
				"        </Id>") //
						.toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(1, 38, 1, 38, "AttributeNotUnique"));
	}

	@Test
	public void testContentIllegalInPrologContentBefore() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("aa<>xml version=\"1.0\" encoding=\"UTF-8\"?>").toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(0, 0, 0, 0, "ContentIllegalInProlog"));
	}

	@Test
	public void testDashDashInComment() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<Id>\r\n" + //
				"					<!-- comment -- text -->\r\n" +
				"        </Id>") //
						.toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(1, 20, 1, 20, "DashDashInComment"));
	}

	@Test
	public void testEmptyPrefixedAttName() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<Id att=\"\"value\" att2=\"value2\">\r\n" + //
				"        </Id>") //
						.toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(1, 8, 1, 10, "emptyprefixedattname"));
	}

	@Test
	public void testEncodingDeclRequired() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" lang=\"en\" ?><a></a>")
			.toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(0, 0, 0, 0, "EncodingDeclRequired"));
	}

	@Test
	public void testEqRequiredInAttribute() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<a Ccy>123.456</a>")
			.toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(0, 6, 0, 6, "EqRequiredInAttribute"));
	}

	@Test
	public void testEqRequiredInXMLDecl() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<?xml version:\"1.0\" encoding=\"UTF-8\"?><a></a>")
			.toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(0, 13, 0, 13, "EqRequiredInXMLDecl"));
	}

	@Test
	public void testETagRequired() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<UltmtDbtr>\r\n" +
		"  		<Nm>Name\r\n" +
		"		</UltmtDbtr> \r\n" +
		"			</Nm>  ")
			.toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(2, 4, 2, 4, "ETagRequired"));
	}

	@Test
	public void testETagRequired2() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<UltmtDbtr>\r\n" +
		"  		Nm>Name</Nm>\r\n" +
		"		</UltmtDbtr>").toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(1, 13, 1, 13, "ETagRequired"));
	}

	@Test
	public void testETagUnterminated() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<UltmtDbtr>\r\n" +
		"  		<Nm>Name</Nm\r\n" +
		"		</UltmtDbtr>").toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(2, 2, 2, 2, "ETagUnterminated"));
	}

	@Test
	public void testIllegalQName() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<a Ccy:\"JPY\">100</a>").toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(0, 7, 0, 7, "IllegalQName"));
	}

	@Test
	public void testInvalidCommentStart() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<!- gdfgdfg -- starts here -->").toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(0, 3, 0, 3, "InvalidCommentStart"));
	}

	@Test
	public void testLessThanAttValue() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<InstdAmt Ccy=\"<EUR\">123.45</InstdAmt> ").toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(0, 15, 0, 15, "LessthanInAttValue"));
	}

	
	@Test
	public void testMarkupEntityMismatch() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
		"<Document xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.03\">\r\n" +
			"<CstmrCdtTrfInitn>\r\n" +
			"</CstmrCdtTrfInitn>").toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(3, 19, 3, 19, "MarkupEntityMismatch"));
	}

	@Test
	public void testMarkupNotRecognizedInContent() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<GrpHdr>\r\n" +
		"<- almost a comment-->\r\n" +
			"<MsgId>2.012.001</MsgId>").toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(1, 1, 1, 1, "MarkupNotRecognizedInContent"));
	}

	@Test
	public void testNameRequiredInReference() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<Nm>Virgay & Co</Nm>").toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(0, 12, 0, 12, "NameRequiredInReference"));
	}

	@Test
	public void testOpenQuoteExpected() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder(" <InstdAmt Ccy==\"JPY\">10000000</InstdAmt>").toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(0, 15, 0, 15, "OpenQuoteExpected"));
	}

	@Test
	public void testPITargetRequired() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<? encoding=\"UTF-8\"?>").toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(0, 2, 0, 2, "PITargetRequired"));
	}

	@Test
	public void testPseudoAttrNameExpected() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"><a></a>")
			.toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(0, 36, 0, 36, "PseudoAttrNameExpected"));
	}

	@Test
	public void testQuoteRequiredInXMLDecl() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<?xml version= encoding=\"UTF-8\"?>")
			.toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(0, 14, 0, 14, "QuoteRequiredInXMLDecl"));
	}

	@Test
	public void testSDDeclInvalid() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"en\"?>")
			.toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(0, 52, 0, 52, "SDDeclInvalid"));
	}

	@Test
	public void testSpaceRequiredBeforeEncodingInXMLDecl() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<?xml version=\"1.0\"encoding=\"UTF-8\"?>")
			.toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(0, 35, 0, 35, "SpaceRequiredBeforeEncodingInXMLDecl"));
	}

	@Test
	public void testSpaceRequiredBeforeStandalone() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"standalone=\"no\"?>")
			.toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(0, 51, 0, 51, "SpaceRequiredBeforeStandalone"));
	}

	@Test
	public void testSpaceRequiredInPI() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<?xmlversion=\"1.0\" encoding=\"UTF-8\"?>")
			.toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(0, 12, 0, 12, "SpaceRequiredInPI"));
	}

	@Test
	public void testTheElementTypeLmsg() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<Issr>ADE</Lssr>")
			.toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(0, 12, 0, 12, "the-element-type-lmsg"));
	}

	@Test
	public void testVersionInfoRequired() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<?xml encoding=\"UTF-8\"?>")
			.toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(0, 22, 0, 22, "VersionInfoRequired"));
	}

	@Test
	public void testVersionNotSupported() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<?xml version=\"5000.0\"encoding=\"UTF-8\"?>")
			.toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(0, 22, 0, 22, "VersionNotSupported"));
	}

	@Test
	public void testXMLDeclUnterminated() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?")
			.toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(0, 37, 0, 37, "XMLDeclUnterminated"));
	}
	


	


	// @Test
	// public void testCVCAttribute3() throws Exception {
	// 	XMLLanguageService languageService = new XMLLanguageService();
	// 	String xml = new StringBuilder("<project xsi:schemalocation=\"https://www.w3.org/TR/xmlschema11-1/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n" + //
	// 			"          <InstdAmt Ccy=\"EURO\">3.14</InstdAmt>\r\n" + //
	// 			"        </project>") //
	// 					.toString();
	// 	TextDocument document = new TextDocument(xml.toString(), "test.xml");
	// 	List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
	// 	});
	// 	assertDiagnostics(diagnostics, d(0, 0, 0, 0, "cvc-attribute-3"));
	// }

	

	private static void assertDiagnostics(List<Diagnostic> actual, Diagnostic... expected) {
		actual.stream().forEach(d -> {
			// we don't want to compare severity, message, etc
			d.setSeverity(null);
			d.setMessage(null);
			d.setSource(null);
		});
		Assert.assertEquals(expected.length, actual.size());
		Assert.assertArrayEquals(expected, actual.toArray());
	}

	private Diagnostic d(int startLine, int startCharacter, int endLine, int endCharacter, String code) {
		return new Diagnostic(new Range(new Position(startLine, startCharacter), new Position(endLine, endCharacter)),
				null, null, null, code);
	}

}
