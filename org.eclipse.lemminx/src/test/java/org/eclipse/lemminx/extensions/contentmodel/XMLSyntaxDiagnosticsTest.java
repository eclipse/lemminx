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
import org.eclipse.lemminx.extensions.contentmodel.settings.ContentModelSettings;
import org.eclipse.lemminx.extensions.contentmodel.settings.NamespacesEnabled;
import org.eclipse.lemminx.extensions.contentmodel.settings.SchemaEnabled;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLNamespacesSettings;
import org.eclipse.lemminx.settings.EnforceQuoteStyle;
import org.eclipse.lemminx.settings.QuoteStyle;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.Diagnostic;
import org.junit.jupiter.api.Test;

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
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" \r\n" + //
				"	xmlns:tns=\"http://camel.apache.org/schema/spring\"\r\n" + //
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
	public void elementUnterminated() throws Exception {
		String xml = "<Id>\r\n" + //
				"          <OrgId\r\n" + //
				"            <Othr>\r\n" + //
				"              <Id> 222010012</Id>\r\n" + //
				"            </Othr>\r\n" + //
				"          </OrgId>\r\n" + //
				"        </Id>";
		Diagnostic d = d(1, 11, 1, 16, XMLSyntaxErrorCode.ElementUnterminated);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, //
				ca(d, te(1, 16, 1, 16, ">")));
	}

	@Test
	public void testElementUnterminatedEndsWithSlash() throws Exception {
		String xml = "<foo>\r\n" + //
				"  <bar att=\"\" /\r\n" + //
				"</foo>";
		Diagnostic d = d(1, 3, 1, 15, XMLSyntaxErrorCode.ElementUnterminated);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, //
				ca(d, te(1, 15, 1, 15, ">")));
	}

	@Test
	public void testElementUnterminatedEndsWithSlashAndSpaces() throws Exception {
		String xml = "<foo>\r\n" + //
				"  <bar att=\"\" /     \r\n" + //
				"</foo>";
		Diagnostic d = d(1, 3, 1, 15, XMLSyntaxErrorCode.ElementUnterminated);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, //
				ca(d, te(1, 15, 1, 15, ">")));
	}

	@Test
	public void testElementUnterminatedEndsWithAttributes() throws Exception {
		String xml = "<foo>\r\n" + //
				"  <bar att=\"\"\r\n" + //
				"</foo>";
		Diagnostic d = d(1, 3, 1, 13, XMLSyntaxErrorCode.ElementUnterminated);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, //
				ca(d, te(1, 13, 1, 13, "/>")), //
				ca(d, te(1, 13, 1, 13, "></bar>")));
	}

	@Test
	public void testElementUnterminatedEndsWithAttributesAndEndSlash() throws Exception {
		String xml = "<foo>\r\n" + //
				"  <bar att=\"\"          /\r\n" + //
				"</foo>";
		Diagnostic d = d(1, 3, 1, 24, XMLSyntaxErrorCode.ElementUnterminated);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(1, 24, 1, 24, ">")));
	}

	@Test
	public void testElementUnterminatedEndsAndSpaces() throws Exception {
		String xml = "<foo>\r\n" + //
				"  <bar att=\"\"    \r\n" + //
				"</foo>";
		Diagnostic d = d(1, 3, 1, 13, XMLSyntaxErrorCode.ElementUnterminated);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, //
				ca(d, te(1, 13, 1, 13, "/>")), //
				ca(d, te(1, 13, 1, 13, "></bar>")));
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
		Diagnostic d = d(1, 5, 2, 2, XMLSyntaxErrorCode.ETagRequired);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(1, 12, 1, 12, "</Nm>")));
	}

	@Test
	public void testETagRequired2() throws Exception {
		String xml = "<UltmtDbtr>\r\n" + //
				"  		Nm>Name</Nm>\r\n" + //
				"		</UltmtDbtr>";
		testDiagnosticsFor(xml, d(0, 1, 1, 11, XMLSyntaxErrorCode.ETagRequired));
	}

	@Test
	public void testETagRequired3() throws Exception {
		String xml = "<UltmtDbtr>\r\n" + //
				"    <Nm>Name</Nm>\r\n" + //
				"    <Ad>\r\n" + //
				"    <Ph>\r\n" + //
				"</UltmtDbtr>";
		Diagnostic d = d(3, 5, 4, 0, XMLSyntaxErrorCode.ETagRequired);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(3, 8, 3, 8, "</Ph>")));
	}

	@Test
	public void testETagRequiredWithReplace() throws Exception {
		String xml = "<a>\r\n" + //
				"	<b>\r\n" + //
				"		</c>";
		Diagnostic d = d(1, 2, 2, 2, XMLSyntaxErrorCode.ETagRequired);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, //
				ca(d, te(2, 4, 2, 5, "b")), ca(d, te(2, 6, 2, 6, "\r\n	</b>")));
	}

	@Test
	public void testETagRequiredWithText() throws Exception {
		String xml = "<root>\r\n" + //
				"<ABC>def\r\n" + //
				"</root>";
		Diagnostic d = d(1, 1, 2, 0, XMLSyntaxErrorCode.ETagRequired);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(1, 8, 1, 8, "</ABC>")));
	}

	@Test
	public void testETagRequiredWithOrpheanEndTag() throws Exception {
		String xml = "<root>\r\n" + //
				"	<foo>\r\n" + //
				"		</\r\n" + //
				"</root>";
		Diagnostic d = d(1, 2, 2, 2, XMLSyntaxErrorCode.ETagRequired);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(2, 4, 2, 4, "foo>")));
	}

	@Test
	public void testETagRequiredClosedWithOrpheanEndTag() throws Exception {
		String xml = "<root>\r\n" + //
				"	<foo>\r\n" + //
				"		</\r\n" + //
				"	</foo>\r\n" + //
				"</root>";
		Diagnostic d = d(1, 2, 2, 2, XMLSyntaxErrorCode.ETagRequired);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(2, 2, 2, 4, "")));
	}

	@Test
	public void testETagRequiredClosedWithOrpheanEndTag2() throws Exception {
		String xml = "<root>\r\n" + //
				"	<foo>\r\n" + //
				"		</bar>\r\n" + //
				"	</foo>\r\n" + //
				"</root>";
		Diagnostic d = d(1, 2, 2, 2, XMLSyntaxErrorCode.ETagRequired);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(2, 2, 2, 8, "")));
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
		Diagnostic d = d(0, 26, 0, 31, XMLSyntaxErrorCode.ETagUnterminated);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(0, 31, 0, 31, ">")));
	}

	/**
	 * Test ETagUnterminated
	 * 
	 * @see https://wiki.xmldation.com/Support/Validator/ETagUnterminated
	 * @throws Exception
	 */
	@Test
	public void testETagUnterminated2() throws Exception {
		String xml = "<a>\r\n" + //
				"  <b>\r\n" + //
				"    <c></c>\r\n" + //
				"  </b\r\n" + // <- error
				"</a>";
		Diagnostic d = d(3, 4, 3, 5, XMLSyntaxErrorCode.ETagUnterminated);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(3, 5, 3, 5, ">")));
	}

	/**
	 * Test ETagUnterminated
	 * 
	 * @see https://wiki.xmldation.com/Support/Validator/ETagUnterminated
	 * @throws Exception
	 */
	@Test
	public void testETagUnterminated3() throws Exception {
		String xml = "<foo>\r\n" + //
				"  <ba><ABCD></ABCD></bar>\r\n" + // // <-- error
				"</foo>";
		testDiagnosticsFor(xml, d(1, 21, 1, 24, XMLSyntaxErrorCode.ETagUnterminated));
	}

	/**
	 * Test ETagUnterminated
	 * 
	 * @see https://wiki.xmldation.com/Support/Validator/ETagUnterminated
	 * @throws Exception
	 */
	@Test
	public void testETagUnterminated4() throws Exception {
		String xml = "<project>\r\n" + //
				"  <dependencies>\r\n" + //
				"    <dependency>\r\n" + //
				"      <scope>test</scope>\r\n" + //
				"    </dependency\r\n" + // <-- error
				"    <dependency>\r\n" + //
				"      <scope>test</scope>\r\n" + //
				"    </dependency>\r\n" + //
				"  </dependencies>\r\n" + //
				"</project>";
		testDiagnosticsFor(xml, d(4, 6, 4, 16, XMLSyntaxErrorCode.ETagUnterminated));
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
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<Document xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.03\">\r\n"
				+ //
				"<CstmrCdtTrfInitn>\r\n" + //
				"</CstmrCdtTrfInitn>";
		Diagnostic d = d(1, 1, 3, 19, XMLSyntaxErrorCode.MarkupEntityMismatch);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(3, 19, 3, 19, "\r\n</Document>")));
	}

	@Test
	public void testMarkupEntityMismatch2() throws Exception {
		String xml = "<ABC>";
		Diagnostic d = d(0, 1, 0, 5, XMLSyntaxErrorCode.MarkupEntityMismatch);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(0, 5, 0, 5, "</ABC>")));
	}

	@Test
	public void testMarkupEntityMismatch3() throws Exception {
		String xml = "<";
		Diagnostic d = d(0, 0, 0, 1, XMLSyntaxErrorCode.MarkupEntityMismatch);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d); // no code actions
	}

	@Test
	public void testMarkupEntityMismatch4() throws Exception {
		String xml = "<?";
		Diagnostic d = d(0, 0, 0, 2, XMLSyntaxErrorCode.MarkupEntityMismatch);
		testDiagnosticsFor(xml, d);
	}

	@Test
	public void testMarkupEntityMismatchWithoutClose() throws Exception {
		String xml = "<ABC";
		Diagnostic d = d(0, 1, 0, 4, XMLSyntaxErrorCode.MarkupEntityMismatch);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, //
				ca(d, te(0, 4, 0, 4, "/>")), //
				ca(d, te(0, 4, 0, 4, "></ABC>")));
	}

	@Test
	public void testMarkupEntityMismatchWithoutCloseAndNewLine() throws Exception {
		String xml = "<ABC\r\n";
		Diagnostic d = d(0, 1, 0, 4, XMLSyntaxErrorCode.MarkupEntityMismatch);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, //
				ca(d, te(0, 4, 0, 4, "/>")), //
				ca(d, te(0, 4, 0, 4, "></ABC>")));
	}

	@Test
	public void testMarkupEntityMismatchWithoutCloseAndSpaces() throws Exception {
		String xml = "<ABC    ";
		Diagnostic d = d(0, 1, 0, 4, XMLSyntaxErrorCode.MarkupEntityMismatch);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, //
				ca(d, te(0, 4, 0, 4, "/>")), //
				ca(d, te(0, 4, 0, 4, "></ABC>")));
	}

	@Test
	public void testMarkupEntityMismatchWithAttributes() throws Exception {
		String xml = "<ABC a=''   ";
		Diagnostic d = d(0, 1, 0, 9, XMLSyntaxErrorCode.MarkupEntityMismatch);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, //
				ca(d, te(0, 9, 0, 9, "/>")), //
				ca(d, te(0, 9, 0, 9, "></ABC>")));
	}

	@Test
	public void testMarkupEntityMismatchWithAttributesAndSlash() throws Exception {
		String xml = "<ABC a='' /  ";
		Diagnostic d = d(0, 1, 0, 11, XMLSyntaxErrorCode.ElementUnterminated);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, //
				ca(d, te(0, 11, 0, 11, ">")));
	}

	@Test
	public void testMarkupEntityMismatchWithText() throws Exception {
		String xml = "<ABC>def";
		Diagnostic d = d(0, 1, 0, 8, XMLSyntaxErrorCode.MarkupEntityMismatch);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(0, 8, 0, 8, "</ABC>")));
	}

	@Test
	public void testMarkupEntityMismatchWithTextAndNewLine() throws Exception {
		String xml = "<ABC>def\r\n";
		Diagnostic d = d(0, 1, 0, 8, XMLSyntaxErrorCode.MarkupEntityMismatch);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(0, 8, 0, 8, "</ABC>")));
	}

	@Test
	public void testMarkupEntityMismatchMultiLine() throws Exception {
		String xml = "<foo action=\"toot\"\r\n" + //
				"		    /";
		Diagnostic d = d(0, 1, 1, 7, XMLSyntaxErrorCode.MarkupEntityMismatch);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, //
				te(1, 7, 1, 7, ">")));
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
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<foo>\r\n" + //
				"  <bar one= two=\"\">\r\n" + //
				"  </bar>\r\n" + //
				"</foo>";
		Diagnostic diagnostic1 = d(2, 7, 2, 10, XMLSyntaxErrorCode.OpenQuoteExpected);
		testDiagnosticsFor(xml, diagnostic1);
		testCodeActionsFor(xml, diagnostic1, ca(diagnostic1, te(2, 11, 2, 11, "\"\"")));
	}

	@Test
	public void testMissingQuotesForAttributeSelfClosing() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<foo>\r\n" + //
				"  <bar one= two=\"\"/>\r\n" + //
				"</foo>";
		Diagnostic diagnostic1 = d(2, 7, 2, 10, XMLSyntaxErrorCode.OpenQuoteExpected);
		testDiagnosticsFor(xml, diagnostic1);
		testCodeActionsFor(xml, diagnostic1, ca(diagnostic1, te(2, 11, 2, 11, "\"\"")));
	}

	@Test
	public void testMissingQuotesForAttributeSelfClosing2() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<foo>\r\n" + //
				"  <bar one=/>\r\n" + //
				"</foo>";
		Diagnostic diagnostic1 = d(2, 7, 2, 10, XMLSyntaxErrorCode.OpenQuoteExpected);
		testDiagnosticsFor(xml, diagnostic1);
		testCodeActionsFor(xml, diagnostic1, ca(diagnostic1, te(2, 11, 2, 11, "\"\"")));
	}

	@Test
	public void testMissingQuotesForAttributeUsingNextValue() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<foo>\r\n" + //
				"  <bar one= two/>\r\n" + //
				"</foo>";
		Diagnostic diagnostic1 = d(2, 7, 2, 10, XMLSyntaxErrorCode.OpenQuoteExpected);
		testDiagnosticsFor(xml, diagnostic1);
		testCodeActionsFor(xml, diagnostic1, ca(diagnostic1, te(2, 11, 2, 15, "\"two\"")));
	}

	@Test
	public void testMissingQuotesForAttributeSingleQuotes() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<foo>\r\n" + //
				"  <bar one= two=\"\"/>\r\n" + //
				"</foo>";
		Diagnostic diagnostic1 = d(2, 7, 2, 10, XMLSyntaxErrorCode.OpenQuoteExpected);
		testDiagnosticsFor(xml, diagnostic1);
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		testCodeActionsFor(xml, diagnostic1, null, settings, ca(diagnostic1, te(2, 11, 2, 11, "\'\'")));
	}

	@Test
	public void testOpenQuoteExpectedDisabledPreference() throws Exception {
		String xml = " <InstdAmt Ccy==\"JPY\">10000000</InstdAmt>";
		testDiagnosticsFor(xml, null, null, null, true, XMLAssert.getContentModelSettings(false, SchemaEnabled.always)); // validation
		// is
		// disabled
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
	public void testRootElementTypeMustMatchDoctypedecl() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE efgh [ \r\n" + //
				"<!ELEMENT abcd (#PCDATA)>\r\n" + //
				"<!ELEMENT efgh (#PCDATA)>\r\n" + //
				"]> \r\n" + //
				"<abcd/>";
		String expectedMessage = "Document root element \"abcd\", must match DOCTYPE root \"efgh\".";
		Diagnostic d = d(5, 1, 5, 5, XMLSyntaxErrorCode.RootElementTypeMustMatchDoctypedecl, expectedMessage);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(5, 1, 5, 5, "efgh")));
	}

	@Test
	public void testRootElementTypeMustMatchDoctypedecl2() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE efgh [ \r\n" + //
				"<!ELEMENT abcd (#PCDATA)>\r\n" + //
				"<!ELEMENT efgh (#PCDATA)>\r\n" + //
				"]> \r\n" + //
				"<abcd>test</abcd>";
		String expectedMessage = "Document root element \"abcd\", must match DOCTYPE root \"efgh\".";
		Diagnostic d = d(5, 1, 5, 5, XMLSyntaxErrorCode.RootElementTypeMustMatchDoctypedecl, expectedMessage);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(5, 1, 5, 5, "efgh"), te(5, 12, 5, 16, "efgh")));
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

	@Test
	public void testEntitySemicolonRequiredInReference() throws Exception {
		String xml = "<!DOCTYPE root [\n" + //
				"    <!ELEMENT root (#PCDATA)>\n" + //
				"    <!ENTITY mdash \"&#x2014;\">\n" + //
				"]>\n" + //
				"<root>\n" + //
				"    &mdash \n" + //
				"</root>";
		testDiagnosticsFor(xml, d(5, 4, 5, 10, XMLSyntaxErrorCode.SemicolonRequiredInReference));
	}

	@Test
	public void testEntitySemicolonRequiredInReferenceOddSpacing() throws Exception {
		String xml = "<!DOCTYPE root [\n" + //
				"    <!ELEMENT root (#PCDATA)>\n" + //
				"    <!ENTITY mdash \"&#x2014;\">\n" + //
				"]>\n" + //
				"<root>\n" + //
				"    &mdash</root>";
		testDiagnosticsFor(xml, d(5, 4, 5, 10, XMLSyntaxErrorCode.SemicolonRequiredInReference));
	}

	@Test
	public void testEntitySemicolonRequiredInReferenceShortName() throws Exception {
		String xml = "<!DOCTYPE root [\n" + //
				"    <!ELEMENT root (#PCDATA)>\n" + //
				"    <!ENTITY m \"&#x2014;\">\n" + //
				"]>\n" + //
				"<root>\n" + //
				"    &m \n" + //
				"</root>";
		testDiagnosticsFor(xml, d(5, 4, 5, 6, XMLSyntaxErrorCode.SemicolonRequiredInReference));
	}

	@Test
	public void closeTag() throws Exception {
		String xml = "<a";
		Diagnostic d = d(0, 1, 0, 2, XMLSyntaxErrorCode.MarkupEntityMismatch);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(0, 2, 0, 2, "/>")), //
				ca(d, te(0, 2, 0, 2, "></a>")));

		xml = "<a>";
		d = d(0, 1, 0, 3, XMLSyntaxErrorCode.MarkupEntityMismatch);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(0, 3, 0, 3, "</a>")));

		xml = "<a /";
		d = d(0, 1, 0, 4, XMLSyntaxErrorCode.MarkupEntityMismatch);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(0, 4, 0, 4, ">")));

		xml = "<a / ";
		d = d(0, 1, 0, 4, XMLSyntaxErrorCode.ElementUnterminated);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(0, 4, 0, 4, ">")));

		xml = "<a></";
		d = d(0, 1, 0, 3, XMLSyntaxErrorCode.MarkupEntityMismatch);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(0, 5, 0, 5, "a>")));
	}

	@Test
	public void namespacesSettingsWithoutXMLNS() throws Exception {
		String xml = "<foo>\r\n" + //
				"	<p:bar />\r\n" + //
				"</foo>";
		// always
		ContentModelSettings settings = getSettingsForNamespaces(NamespacesEnabled.always);
		testDiagnosticsFor(xml, null, null, null, true, settings, //
				d(1, 2, 1, 7, XMLSyntaxErrorCode.ElementPrefixUnbound));

		// never
		settings = getSettingsForNamespaces(NamespacesEnabled.never);
		testDiagnosticsFor(xml, null, null, null, true, settings);

		// onNamespaceEncountered
		settings = getSettingsForNamespaces(NamespacesEnabled.onNamespaceEncountered);
		testDiagnosticsFor(xml, null, null, null, true, settings);
	}

	@Test
	public void namespacesSettingsWithUnvalidXMLNS() throws Exception {
		String xml = "<foo xmlns=\"http:foo\" >\r\n" + //
				"	<p:bar />\r\n" + //
				"</foo>";
		// always
		ContentModelSettings settings = getSettingsForNamespaces(NamespacesEnabled.always);
		testDiagnosticsFor(xml, null, null, null, true, settings, //
				d(1, 2, 1, 7, XMLSyntaxErrorCode.ElementPrefixUnbound));

		// never
		settings = getSettingsForNamespaces(NamespacesEnabled.never);
		testDiagnosticsFor(xml, null, null, null, true, settings);

		// onNamespaceEncountered
		settings = getSettingsForNamespaces(NamespacesEnabled.onNamespaceEncountered);
		testDiagnosticsFor(xml, null, null, null, true, settings, //
				d(1, 2, 1, 7, XMLSyntaxErrorCode.ElementPrefixUnbound));
	}

	@Test
	public void namespacesSettingsWithValidXMLNS() throws Exception {
		String xml = "<foo xmlns:p=\"http:foo\" >\r\n" + //
				"	<p:bar />\r\n" + //
				"</foo>";
		// always
		ContentModelSettings settings = getSettingsForNamespaces(NamespacesEnabled.always);
		testDiagnosticsFor(xml, null, null, null, true, settings);

		// never
		settings = getSettingsForNamespaces(NamespacesEnabled.never);
		testDiagnosticsFor(xml, null, null, null, true, settings);

		// onNamespaceEncountered
		settings = getSettingsForNamespaces(NamespacesEnabled.onNamespaceEncountered);
		testDiagnosticsFor(xml, null, null, null, true, settings);
	}

	private static ContentModelSettings getSettingsForNamespaces(NamespacesEnabled namespacesEnabled) {
		ContentModelSettings settings = XMLAssert.getContentModelSettings(true, SchemaEnabled.never);
		settings.getValidation().setNoGrammar("ignore");
		XMLNamespacesSettings namespaces = new XMLNamespacesSettings();
		namespaces.setEnabled(namespacesEnabled);
		settings.getValidation().setNamespaces(namespaces);
		return settings;
	}
}
