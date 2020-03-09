/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.extensions.contentmodel;

import static org.eclipse.lemminx.XMLAssert.ca;
import static org.eclipse.lemminx.XMLAssert.d;
import static org.eclipse.lemminx.XMLAssert.te;
import static org.eclipse.lemminx.XMLAssert.testCodeActionsFor;
import static org.eclipse.lemminx.XMLAssert.testDiagnosticsFor;

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.extensions.contentmodel.participants.XMLSyntaxErrorCode;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lemminx.settings.XMLFormattingOptions;
import org.eclipse.lsp4j.Diagnostic;
import org.junit.Test;

/**
 * XML diagnostics services tests
 *
 */
public class XMLSyntaxDiagnosticsTest {
	
	/**
	 * AttributeNotUnique tests
	 * 
	 * @see https://wiki.xmldation.com/Support/Validator/AttributeNotUnique
	 * @throws Exception
	 */
	@Test
	public void testAttributeNotUnique() throws Exception {
		String xml = "<InstdAmt Ccy=\"JPY\" Ccy=\"JPY\" >10000000</InstdAmt>";
		testDiagnosticsFor(xml, d(0, 10, 0, 13, XMLSyntaxErrorCode.AttributeNotUnique));
	}

	@Test
	public void testAttributeNotUnique2() throws Exception {
		String xml = "<a attr=\"\" attr=\"\" attr2=\"\" />";
		testDiagnosticsFor(xml, d(0, 3, 0, 7, XMLSyntaxErrorCode.AttributeNotUnique));
	}

	/**
	 * AttributeNSNotUnique tests
	 * 
	 * @see https://wiki.xmldation.com/Support/Validator/AttributeNSNotUnique
	 * @throws Exception
	 */
	@Test
	public void testAttributeNSNotUnique() throws Exception {
		String xml = "<Document xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.03\"\r\n"
				+ "\r\n" + //
				"xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.03\"> ";
		testDiagnosticsFor(xml, d(0, 64, 0, 69, XMLSyntaxErrorCode.AttributeNSNotUnique));
	}

	@Test
	public void testAttributeNSNotUnique2() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" +
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" \r\n" +
				"	xmlns:tns=\"http://camel.apache.org/schema/spring\"\r\n" +
				"	xmlns:tns=\"http://camel.apache.org/schema/spring\" version=\"1.0\">";
		testDiagnosticsFor(xml, d(2, 1, 2, 10, XMLSyntaxErrorCode.AttributeNSNotUnique));
	}

	@Test
	public void testAttributePrefixUnbound() throws Exception {
		String xml = "<a xsi:xxxxx=\"\"></a>";
		testDiagnosticsFor(xml, d(0, 3, 0, 6, XMLSyntaxErrorCode.AttributePrefixUnbound));
	}

	/**
	 * ContentIllegalInProlog tests
	 * 
	 * @see https://wiki.xmldation.com/Support/Validator/ContentIllegalInProlog
	 * @throws Exception
	 */
	@Test
	public void testBeforeContentIllegalInProlog() throws Exception {
		String xml = " ab?<xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
		testDiagnosticsFor(xml, d(0, 1, 0, 4, XMLSyntaxErrorCode.ContentIllegalInProlog));
	}

	/**
	 * ContentIllegalInProlog tests
	 * 
	 * @see https://wiki.xmldation.com/Support/Validator/ContentIllegalInProlog
	 * @throws Exception
	 */
	@Test
	public void testAfterContentIllegalInProlog() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>ab\ncd";
		testDiagnosticsFor(xml, d(0, 54, 1, 2, XMLSyntaxErrorCode.ContentIllegalInProlog));
	}

	/**
	 * ContentIllegalInProlog tests
	 * 
	 * @see https://wiki.xmldation.com/Support/Validator/ContentIllegalInProlog
	 * @throws Exception
	 */
	@Test
	public void testAfterContentIllegalInProlog2() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>ab\ncd<root>";
		testDiagnosticsFor(xml, d(0, 54, 1, 2, XMLSyntaxErrorCode.ContentIllegalInProlog));
	}

	@Test
	public void testEncodingUTF_16() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-16\" standalone=\"no\"?><root />";
		testDiagnosticsFor(xml);
	}
	
	/**
	 * DashDashInComment tests
	 * 
	 * @see https://wiki.xmldation.com/Support/Validator/DashDashInComment
	 * @throws Exception
	 */
	@Test
	public void testDashDashInComment() throws Exception {
		String xml = "<Id>\r\n" + //
				"					<!-- comment -- text -->\r\n" + //
				"        </Id>";
		testDiagnosticsFor(xml, d(1, 18, 1, 20, XMLSyntaxErrorCode.DashDashInComment));
	}

	/**
	 * ElementUnterminated tests
	 * 
	 * @see https://wiki.xmldation.com/Support/Validator/ElementUnterminated
	 * @throws Exception
	 */
	@Test
	public void testElementUnterminated() throws Exception {
		String xml = "<Id>\r\n" + //
				"          <OrgId\r\n" + //
				"            <Othr>\r\n" + //
				"              <Id> 222010012</Id>\r\n" + //
				"            </Othr>\r\n" + //
				"          </OrgId>\r\n" + //
				"        </Id>";
		Diagnostic d = d(1, 11, 1, 16, XMLSyntaxErrorCode.ElementUnterminated);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(1, 16, 1, 16, "/>")), ca(d, te(1, 16, 1, 16, ">")),
				ca(d, te(1, 16, 1, 16, "></OrgId>")));
	}

	/**
	 * ElementPrefixUnbound tests
	 * 
	 * @see https://wiki.xmldation.com/Support/Validator/ElementPrefixUnbound
	 * @throws Exception
	 */
	@Test
	public void testElementPrefixUnbound() throws Exception {
		String xml = "<xs:OrgId>\r\n" + //
				"  <xs:Othr>\r\n" + //
				"  </xs:Othr>\r\n" + //
				"</xs:OrgId>";
		testDiagnosticsFor(xml, d(0, 1, 0, 9, XMLSyntaxErrorCode.ElementPrefixUnbound));
	}

	/**
	 * EmptyPrefixedAttName tests
	 * 
	 * @see https://wiki.xmldation.com/Support/Validator/EmptyPrefixedAttName
	 * @throws Exception
	 */
	@Test
	public void testEmptyPrefixedAttName() throws Exception {
		String xml = "<Document xmlns:xsi=\"\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.03\">";
		testDiagnosticsFor(xml, d(0, 20, 0, 22, XMLSyntaxErrorCode.EmptyPrefixedAttName));
	}

	@Test
	public void testEqRequiredInAttribute() throws Exception {
		String xml = "<a Ccy>123.456</a>";
		Diagnostic d = d(0, 3, 0, 6, XMLSyntaxErrorCode.EqRequiredInAttribute);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(0, 6, 0, 6, "=\"\"")));
	}

	@Test
	public void testEqRequiredInXMLDecl() throws Exception {
		String xml = "<?xml version:\"1.0\" encoding=\"UTF-8\"?><a></a>";
		testDiagnosticsFor(xml, d(0, 6, 0, 14, XMLSyntaxErrorCode.EqRequiredInXMLDecl));
	}

	@Test
	public void testNoMorePseudoAttributes() throws Exception {
		String xml = "<?xml version=\"1.0\" standalone=\"yes\" encoding=\"UTF-8\"?><a></a>";
		testDiagnosticsFor(xml, d(0, 37, 0, 45, XMLSyntaxErrorCode.NoMorePseudoAttributes));
	}

	/**
	 * ETagRequired tests
	 * 
	 * @see https://wiki.xmldation.com/Support/Validator/ETagRequired * @throws
	 *      Exception
	 */
	@Test
	public void testETagRequired() throws Exception {
		String xml = "<UltmtDbtr>\r\n" + //
				"  		<Nm>Name\r\n" + //
				"		</UltmtDbtr> \r\n" + //
				"			</Nm>  ";
		Diagnostic d = d(1, 5, 1, 7, XMLSyntaxErrorCode.ETagRequired);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(1, 8, 1, 8, "</Nm>")));
	}

	@Test
	public void testETagRequired2() throws Exception {
		String xml = "<UltmtDbtr>\r\n" + //
				"  		Nm>Name</Nm>\r\n" + //
				"		</UltmtDbtr>";
		testDiagnosticsFor(xml, d(1, 13, 1, 15, XMLSyntaxErrorCode.ETagRequired));
	}

	@Test
	public void testETagRequired3() throws Exception {
		String xml = "<UltmtDbtr>\r\n" +
				"    <Nm>Name</Nm>\r\n" +
				"    <Ad>\r\n" +
				"    <Ph>\r\n" +
				"</UltmtDbtr>";
		Diagnostic d = d(3, 5, 3, 7, XMLSyntaxErrorCode.ETagRequired);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(3, 8, 3, 8, "</Ph>")));
	}

	/**
	 * Test ETagUnterminated
	 * 
	 * @see https://wiki.xmldation.com/Support/Validator/ETagUnterminated
	 * @throws Exception
	 */
	@Test
	public void testETagUnterminated() throws Exception {
		String xml = "<MsgId>ABC/090928/CCT001</MsgId\r\n" + //
				"  <CreDtTm>2009-09-28T14:07:00</CreDtTm>";
		testDiagnosticsFor(xml, d(0, 26, 0, 31, XMLSyntaxErrorCode.ETagUnterminated));
	}

	/**
	 * Test ETagUnterminated
	 * 
	 * @see https://wiki.xmldation.com/Support/Validator/ETagUnterminated
	 * @throws Exception
	 */
	@Test
	public void testETagUnterminated2() throws Exception {
		String xml = 
				"<a>\r\n" + //
				"  <b>\r\n" + //
				"    <c></c>\r\n" + //
				"  </b\r\n" + // <- error
				"</a>";
		testDiagnosticsFor(xml, d(3, 4, 3, 5, XMLSyntaxErrorCode.ETagUnterminated));
	}

	@Test
	public void testIllegalQName() throws Exception {
		String xml = "<a Ccy:\"JPY\">100</a>";
		testDiagnosticsFor(xml, d(0, 6, 0, 7, XMLSyntaxErrorCode.IllegalQName));
	}

	@Test
	public void testInvalidCommentStart() throws Exception {
		String xml = "<!- gdfgdfg -- starts here -->";
		testDiagnosticsFor(xml, d(0, 2, 0, 3, XMLSyntaxErrorCode.InvalidCommentStart));
	}

	@Test
	public void testLessThanAttValue() throws Exception {
		String xml = "<InstdAmt Ccy=\"<EUR\">123.45</InstdAmt> ";
		testDiagnosticsFor(xml, d(0, 14, 0, 20, XMLSyntaxErrorCode.LessthanInAttValue));
	}

	@Test
	public void testMarkupEntityMismatch() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				+ "<Document xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.03\">\r\n"
				+ "<CstmrCdtTrfInitn>\r\n"
				+ "</CstmrCdtTrfInitn>";
		
		Diagnostic d = d(1, 1, 1, 9, XMLSyntaxErrorCode.MarkupEntityMismatch);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(3, 0, 3, 0, "</Document>")));
	}

	@Test
	public void testMarkupEntityMismatch2() throws Exception {
		String xml = "<ABC>";
		
		Diagnostic d = d(0, 1, 0, 4, XMLSyntaxErrorCode.MarkupEntityMismatch);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(0, 5, 0, 5, "</ABC>")));
	}

	@Test
	public void testMarkupNotRecognizedInContent() throws Exception {
		String xml = "<GrpHdr>\r\n" + "<- almost a comment-->\r\n" + "<MsgId>2.012.001</MsgId>";
		testDiagnosticsFor(xml, d(1, 0, 1, 1, XMLSyntaxErrorCode.MarkupNotRecognizedInContent));
	}

	@Test
	public void testNameRequiredInReference() throws Exception {
		String xml = "<Nm>Virgay & Co</Nm>";
		testDiagnosticsFor(xml, d(0, 12, 0, 12, XMLSyntaxErrorCode.NameRequiredInReference));
	}

	@Test
	public void testOpenQuoteExpected() throws Exception {
		String xml = " <InstdAmt Ccy==\"JPY\">10000000</InstdAmt>";
		Diagnostic diagnostic1 = d(0, 11, 0, 14, XMLSyntaxErrorCode.OpenQuoteExpected);
		testDiagnosticsFor(xml, diagnostic1);
		testCodeActionsFor(xml, diagnostic1, ca(diagnostic1, te(0, 15, 0, 15, "\"\"")));
	}

	@Test
	public void testMissingQuotesForAttribute() throws Exception {
		String xml = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
		"<foo>\r\n" +
		"  <bar one= two=\"\">\r\n" +
		"  </bar>\r\n" +
		"</foo>";
		Diagnostic diagnostic1 = d(2, 7, 2, 10, XMLSyntaxErrorCode.OpenQuoteExpected);
		testDiagnosticsFor(xml, diagnostic1);
		testCodeActionsFor(xml, diagnostic1, ca(diagnostic1, te(2, 11, 2, 11, "\"\"")));
	}

	@Test
	public void testMissingQuotesForAttributeSelfClosing() throws Exception {
		String xml = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
		"<foo>\r\n" +
		"  <bar one= two=\"\"/>\r\n" +
		"</foo>";
		Diagnostic diagnostic1 = d(2, 7, 2, 10, XMLSyntaxErrorCode.OpenQuoteExpected);
		testDiagnosticsFor(xml, diagnostic1);
		testCodeActionsFor(xml, diagnostic1, ca(diagnostic1, te(2, 11, 2, 11, "\"\"")));
	}

	@Test
	public void testMissingQuotesForAttributeSelfClosing2() throws Exception {
		String xml = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
		"<foo>\r\n" +
		"  <bar one=/>\r\n" +
		"</foo>";
		Diagnostic diagnostic1 = d(2, 7, 2, 10, XMLSyntaxErrorCode.OpenQuoteExpected);
		testDiagnosticsFor(xml, diagnostic1);
		testCodeActionsFor(xml, diagnostic1, ca(diagnostic1, te(2, 11, 2, 11, "\"\"")));
	}

	@Test
	public void testMissingQuotesForAttributeUsingNextValue() throws Exception {
		String xml = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
		"<foo>\r\n" +
		"  <bar one= two/>\r\n" +
		"</foo>";
		Diagnostic diagnostic1 = d(2, 7, 2, 10, XMLSyntaxErrorCode.OpenQuoteExpected);
		testDiagnosticsFor(xml, diagnostic1);
		testCodeActionsFor(xml, diagnostic1, ca(diagnostic1, te(2, 11, 2, 15, "\"two\"")));
	}

	@Test
	public void testMissingQuotesForAttributeSingleQuotes() throws Exception {
		String xml = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
		"<foo>\r\n" +
		"  <bar one= two=\"\"/>\r\n" +
		"</foo>";
		Diagnostic diagnostic1 = d(2, 7, 2, 10, XMLSyntaxErrorCode.OpenQuoteExpected);
		testDiagnosticsFor(xml, diagnostic1);
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setQuotations(XMLFormattingOptions.SINGLE_QUOTES_VALUE);
		testCodeActionsFor(xml, diagnostic1, null, settings, ca(diagnostic1, te(2, 11, 2, 11, "\'\'")));
	}

	@Test
	public void testOpenQuoteExpectedDisabledPreference() throws Exception {
		String xml = " <InstdAmt Ccy==\"JPY\">10000000</InstdAmt>";
		testDiagnosticsFor(xml, null, null, null, true, XMLAssert.getContentModelSettings(false, true)); //validation is disabled
	}

	@Test
	public void testPITargetRequired() throws Exception {
		String xml = "<? encoding=\"UTF-8\"?>";
		testDiagnosticsFor(xml, d(0, 2, 0, 2, XMLSyntaxErrorCode.PITargetRequired));
	}

	@Test
	public void testPseudoAttrNameExpected() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"><a></a>";
		testDiagnosticsFor(xml, d(0, 36, 0, 36, XMLSyntaxErrorCode.PseudoAttrNameExpected));
	}

	@Test
	public void testQuoteRequiredInXMLDecl() throws Exception {
		String xml = "<?xml version= encoding=\"UTF-8\"?>";
		testDiagnosticsFor(xml, d(0, 13, 0, 14, XMLSyntaxErrorCode.QuoteRequiredInXMLDecl));
	}

	@Test
	public void testRootElementTypeMustMatchDoctypedecl() {
		String xml =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
		"<!DOCTYPE efgh [ \r\n" +
		"<!ELEMENT abcd (#PCDATA)>\r\n" +
		"<!ELEMENT efgh (#PCDATA)>\r\n" +
		"]> \r\n" +
		"<abcd/>";

		testDiagnosticsFor(xml, d(5, 1, 5, 5, XMLSyntaxErrorCode.RootElementTypeMustMatchDoctypedecl));
	}

	@Test
	public void testSDDeclInvalid() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"en\"?>";
		testDiagnosticsFor(xml, d(0, 48, 0, 52, XMLSyntaxErrorCode.SDDeclInvalid));
	}

	@Test
	public void testSpaceRequiredBeforeEncodingInXMLDecl() throws Exception {
		String xml = "<?xml version=\"1.0\"encoding=\"UTF-8\"?>";
		testDiagnosticsFor(xml, d(0, 1, 0, 5, XMLSyntaxErrorCode.SpaceRequiredBeforeEncodingInXMLDecl));
	}

	@Test
	public void testSpaceRequiredBeforeStandalone() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"standalone=\"no\"?>";
		testDiagnosticsFor(xml, d(0, 1, 0, 5, XMLSyntaxErrorCode.SpaceRequiredBeforeStandalone));
	}

	@Test
	public void testSpaceRequiredInPI() throws Exception {
		String xml = "<?xmlversion=\"1.0\" encoding=\"UTF-8\"?>";
		testDiagnosticsFor(xml, d(0, 1, 0, 12, XMLSyntaxErrorCode.SpaceRequiredInPI));
	}

	@Test
	public void testVersionInfoRequired() throws Exception {
		String xml = "<?xml encoding=\"UTF-8\"?>";
		testDiagnosticsFor(xml, d(0, 1, 0, 5, XMLSyntaxErrorCode.VersionInfoRequired));
	}

	@Test
	public void testVersionNotSupported() throws Exception {
		String xml = "<?xml version=\"5000.0\"encoding=\"UTF-8\"?>";
		testDiagnosticsFor(xml, d(0, 14, 0, 22, XMLSyntaxErrorCode.VersionNotSupported));
	}
}
