/**
 *  Copyright (c) 2018 Angelo ZERR.
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
package org.eclipse.lemminx.extensions.xsd;

import static org.eclipse.lemminx.XMLAssert.ca;
import static org.eclipse.lemminx.XMLAssert.d;
import static org.eclipse.lemminx.XMLAssert.te;
import static org.eclipse.lemminx.XMLAssert.testCodeActionsFor;

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.xsd.participants.XSDErrorCode;
import org.eclipse.lemminx.extensions.xsd.participants.diagnostics.XSDValidator;
import org.eclipse.lsp4j.Diagnostic;
import org.junit.jupiter.api.Test;

/**
 * XSD diagnostics tests which test the {@link XSDValidator}.
 *
 */
public class XSDValidationExtensionsTest {

	@Test
	public void cos_all_limited_2() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<xs:schema \r\n" + //
				"	xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\r\n" + //
				"	\r\n" + //
				"	<xs:complexType name=\"testType\">\r\n" + //
				"		<xs:all>\r\n" + //
				"			<xs:element name=\"testEle1\" minOccurs=\"2\" maxOccurs=\"unbounded\" type=\"xs:string\"/>\r\n"
				+ //
				"		</xs:all>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"</xs:schema>";
		testDiagnosticsFor(xml, d(6, 55, 6, 66, XSDErrorCode.cos_all_limited_2));
	}

	@Test
	public void cos_all_limited_2_multiple() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<xs:schema \r\n" + //
				"	xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\r\n" + //
				"	\r\n" + //
				"	<xs:complexType name=\"testType\">\r\n" + //
				"		<xs:all>\r\n" + //
				"			<xs:element name=\"testEle1\" minOccurs=\"2\" maxOccurs=\"unbounded\" type=\"xs:string\"/>\r\n"
				+ //
				"			<xs:element name=\"testEle2\" minOccurs=\"2\" maxOccurs=\"unbounded\" type=\"xs:string\"/>\r\n"
				+ //
				"			<xs:element name=\"test3\" minOccurs=\"2\" maxOccurs=\"unbounded\" type=\"xs:string\"/>\r\n"
				+ //
				"		</xs:all>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"</xs:schema>";

		Diagnostic first = d(6, 55, 6, 66, XSDErrorCode.cos_all_limited_2);
		Diagnostic second = d(7, 55, 7, 66, XSDErrorCode.cos_all_limited_2);
		Diagnostic third = d(8, 52, 8, 63, XSDErrorCode.cos_all_limited_2);
		testDiagnosticsFor(xml, first, second, third);
	}

	@Test
	public void ct_props_correct_3_1() throws BadLocationException {
		String xml = "<?xml version=\"1.1\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\r\n"
				+ //
				"	<xs:complexType name=\"fullpersoninfo\">\r\n" + //
				"		<xs:complexContent>\r\n" + //
				"			<xs:extension base=\"fullpersoninfo\">\r\n" + //
				"			</xs:extension>\r\n" + //
				"		</xs:complexContent>\r\n" + //
				"	</xs:complexType>\r\n" + "</xs:schema>";
		testDiagnosticsFor(xml, d(4, 22, 4, 38, XSDErrorCode.ct_props_correct_3));
	}

	@Test
	public void ct_props_correct_3_2() throws BadLocationException {
		String xml = "<?xml version=\"1.1\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://www.w3.org/2001/XMLSchema\">\r\n"
				+ //
				"	<xs:complexType name=\"aComplexType\">\r\n" + //
				"		<xs:complexContent>\r\n" + //
				"			<xs:extension base=\"xs:aComplexType\"></xs:extension>\r\n" + //
				"		</xs:complexContent>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"</xs:schema>";
		testDiagnosticsFor(xml, d(4, 22, 4, 39, XSDErrorCode.ct_props_correct_3));
	}

	@Test
	public void emptyTargetNamespace() throws BadLocationException {
		String xml = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\r\n" + //
				"	elementFormDefault=\"qualified\" xml:lang=\"EN\"\r\n" + //
				"	targetNamespace=\"\"\r\n" + //
				"	version=\"1.0\">\r\n" + //
				"</xs:schema>";
		testDiagnosticsFor(xml, d(2, 17, 2, 19, XSDErrorCode.EmptyTargetNamespace));
	}

	@Test
	public void p_props_correct_2_1() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\r\n" + //
				"	<xs:complexType name=\"testType\">\r\n" + //
				"		<xs:all>\r\n" + //
				"			<xs:element name=\"testEle\" minOccurs=\"1\" maxOccurs=\"0\" type=\"xs:string\"/>\r\n" + //
				"		</xs:all>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"</xs:schema>";
		testDiagnosticsFor(xml, d(4, 30, 4, 43, XSDErrorCode.p_props_correct_2_1));
	}

	@Test
	public void p_props_correct_2_1_multiple() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\r\n" + //
				"	<xs:complexType name=\"testType\">\r\n" + //
				"		<xs:all>\r\n" + //
				"			<xs:element name=\"testEle\" minOccurs=\"1\" maxOccurs=\"0\" type=\"xs:string\"/>\r\n" + //
				"			<xs:element name=\"test\" minOccurs=\"5\" maxOccurs=\"0\" type=\"xs:string\"/>\r\n" + //
				"		</xs:all>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"</xs:schema>";

		Diagnostic first = d(4, 30, 4, 43, XSDErrorCode.p_props_correct_2_1);
		Diagnostic second = d(5, 27, 5, 40, XSDErrorCode.p_props_correct_2_1);
		testDiagnosticsFor(xml, first, second);
	}

	@Test
	public void s4s_elt_invalid_content_1() throws BadLocationException {
		String xml = "<?xml version=\"1.1\"?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\r\n" + //
				"	elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\r\n" + //
				"	<foo></foo>\r\n" + // <- error foo doesn't exist
				"</xs:schema>";
		testDiagnosticsFor(xml, d(3, 2, 3, 5, XSDErrorCode.s4s_elt_invalid_content_1));
	}

	@Test
	public void s4s_elt_character() throws BadLocationException {
		String xml = "<?xml version=\"1.1\"?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\r\n" + //
				"	elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\r\n" + //
				"	<xs:element name=\"foo\">bar</xs:element>\r\n" + // <- error with bar text
				"</xs:schema>";
		testDiagnosticsFor(xml, d(3, 24, 3, 27, XSDErrorCode.s4s_elt_character));
	}

	@Test
	public void s4s_elt_must_match_2() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" version=\"1.0\">\r\n" + //
				"	<xs:simpleType name=\"X\"></xs:simpleType>\r\n" + //
				"</xs:schema>";
		testDiagnosticsFor(xml, d(2, 2, 2, 15, XSDErrorCode.s4s_elt_must_match_2));
	}

	@Test
	public void s4s_att_must_appear() throws BadLocationException {
		String xml = "<?xml version=\"1.1\"?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\r\n" + //
				"	elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\r\n" + //
				"	<xs:element></xs:element>\r\n" + // <- error with @name missing
				"</xs:schema>";
		testDiagnosticsFor(xml, d(3, 2, 3, 12, XSDErrorCode.s4s_att_must_appear));
	}

	@Test
	public void s4s_att_not_allowed() throws BadLocationException {
		String xml = "<?xml version=\"1.1\"?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\r\n" + //
				"	elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\r\n" + //
				"	<xs:element foo=\"bar\" ></xs:element>\r\n" + // <- error with foo attribute which is not allowed
				"</xs:schema>";
		testDiagnosticsFor(xml, d(3, 13, 3, 16, XSDErrorCode.s4s_att_not_allowed),
				d(3, 2, 3, 12, XSDErrorCode.s4s_att_must_appear));
	}

	@Test
	public void s4s_att_invalid_value() throws BadLocationException {
		String xml = "<?xml version=\"1.1\"?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\r\n" + //
				"	elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\r\n" + //
				"	<xs:element name=\"\" ></xs:element>\r\n" + // <- error with @name which is empty
				"</xs:schema>";
		testDiagnosticsFor(xml, d(3, 18, 3, 20, XSDErrorCode.s4s_att_invalid_value),
				d(3, 2, 3, 12, XSDErrorCode.s4s_att_must_appear));
	}

	@Test
	public void s4s_elt_invalid_content_3WithClosingTag() throws BadLocationException {
		String xml = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\">\r\n" + //
				"	<xs:element name=\"project\" type=\"xs:string\"></xs:element>\r\n" + //
				"	<xs:import></xs:import>\r\n" + //
				"</xs:schema>";
		Diagnostic d = d(2, 2, 2, 11, XSDErrorCode.s4s_elt_invalid_content_3);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(2, 1, 2, 24, "")));
	}

	@Test
	public void s4s_elt_invalid_content_3WithSelfClosingTag() throws BadLocationException {
		String xml = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\">\r\n" + //
				"	<xs:element name=\"project\" type=\"xs:string\"></xs:element>\r\n" + //
				"	<xs:import/>\r\n" + //
				"</xs:schema>";
		Diagnostic d = d(2, 2, 2, 11, XSDErrorCode.s4s_elt_invalid_content_3);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(2, 1, 2, 13, "")));
	}

	@Test
	public void sch_props_correct_2() throws BadLocationException {
		String xml = "<?xml version=\"1.1\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\r\n" + //
				"	<xs:element name=\"elt1\" />\r\n" + //
				"	<xs:element name=\"elt1\" />\r\n" + //
				"</xs:schema>";
		testDiagnosticsFor(xml, d(3, 18, 3, 24, XSDErrorCode.sch_props_correct_2));
	}

	@Test
	public void src_ct_1() throws BadLocationException {
		String xml = "<?xml version=\"1.1\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\r\n"
				+ //
				"	<xs:complexType name=\"fullpersoninfo\">\r\n" + //
				"		<xs:complexContent>\r\n" + //
				"			<xs:extension base=\"xs:string\">\r\n" + //
				"			</xs:extension>\r\n" + //
				"		</xs:complexContent>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"</xs:schema>";
		testDiagnosticsFor(xml, d(4, 22, 4, 33, XSDErrorCode.src_ct_1));
	}

	@Test
	public void src_resolve() throws BadLocationException {
		String xml = "<?xml version=\"1.1\"?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\r\n" + //
				"	elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\r\n" + //
				"	<xs:element name=\"A\">\r\n" + //
				"		<xs:complexType>\r\n" + //
				"			<xs:sequence>\r\n" + //
				"				<xs:element name=\"A.1\" type=\"xs:string\" />\r\n" + //
				"				<xs:element name=\"A.2\" type=\"XXXXX\" /> \r\n" + // <- error with XXXXX
				"			</xs:sequence>\r\n" + //
				"		</xs:complexType>\r\n" + //
				"	</xs:element> \r\n" + //
				"</xs:schema>";
		testDiagnosticsFor(xml, d(7, 32, 7, 39, XSDErrorCode.src_resolve));
	}

	@Test
	public void src_resolve2() throws BadLocationException {
		String xml = "<?xml version=\"1.1\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\r\n" + //
				"	elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\r\n" + //
				"\r\n" + //
				"	<xs:simpleType name=\"carType\">\r\n" + //
				"		<xs:restriction base=\"xs:string\">\r\n" + //
				"			<xs:enumeration value=\"Audi\" />\r\n" + //
				"			<xs:enumeration value=\"Golf\" />\r\n" + //
				"			<xs:enumeration value=\"BMW\" />\r\n" + //
				"		</xs:restriction>\r\n" + //
				"	</xs:simpleType>\r\n" + //
				"\r\n" + //
				"	<xs:element name=\"car\" type=\"carType\" />\r\n" + //
				"	<xs:element name=\"foo\" type=\"fooType\" />\r\n" + // <- error with fooType which doesn't exists
				"\r\n" + //
				"</xs:schema>";
		testDiagnosticsFor(xml, d(13, 29, 13, 38, XSDErrorCode.src_resolve));
	}

	@Test
	public void src_element_2_1() throws BadLocationException {
		String xml = "<?xml version='1.0'?>\r\n" + //
				"<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>\r\n" + //
				"    <xs:element name='note'>\r\n" + //
				"        <xs:complexType>\r\n" + //
				"            <xs:sequence>\r\n" + //
				"                <xs:element nhame='to' type='xs:string' nillable='false' />\r\n" + // <- error nhame
																									// doesn't exists
				"                <xs:element name='from' type='xs:string' />\r\n" + //
				"                <xs:element name='heading' type='xs:string' />\r\n" + //
				"                <xs:element name='body' type='xs:string' nillable='false' />\r\n" + //
				"            </xs:sequence>\r\n" + //
				"        </xs:complexType>\r\n" + //
				"    </xs:element>\r\n" + //
				"</xs:schema>";
		testDiagnosticsFor(xml, d(5, 28, 5, 33, XSDErrorCode.s4s_att_not_allowed),
				d(5, 17, 5, 27, XSDErrorCode.src_element_2_1));
	}

	@Test
	public void src_element_3() throws BadLocationException {
		String xml = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\r\n" + //
				"  <xs:element name=\"a\" type=\"xs:integer\">\r\n" + //
				"    <xs:complexType>\r\n" + //
				"      <xs:sequence>\r\n" + //
				"        <xs:element name=\"b\"></xs:element>\r\n" + //
				"      </xs:sequence>\r\n" + //
				"    </xs:complexType>\r\n" + //
				"  </xs:element>\r\n" + //
				"</xs:schema>";
		testDiagnosticsFor(xml, d(1, 3, 1, 13, XSDErrorCode.src_element_3));
	}

	@Test
	public void src_import_1_2xs() throws BadLocationException {
		String xml = "<?xml version=\'1.0\'?>\r\n" + //
				"<xs:schema xmlns:xs=\'http://www.w3.org/2001/XMLSchema\'>\r\n" + //
				"	<xs:import></xs:import>\r\n" + //
				"</xs:schema>";

		Diagnostic d = d(2, 2, 2, 11, XSDErrorCode.src_import_1_2);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(2, 11, 2, 11, " namespace=\"\"")),
				ca(d, te(1, 54, 1, 54, " targetNamespace=\"\"")));
	}

	@Test
	public void src_import_1_2() throws BadLocationException {
		String xml = "<?xml version=\'1.0\'?>\r\n" + //
				"<schema xmlns=\'http://www.w3.org/2001/XMLSchema\'>\r\n" + //
				"	<import></import>\r\n" + //
				"</schema>";

		Diagnostic d = d(2, 2, 2, 8, XSDErrorCode.src_import_1_2);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(2, 8, 2, 8, " namespace=\"\"")),
				ca(d, te(1, 48, 1, 48, " targetNamespace=\"\"")));
	}

	@Test
	public void src_import_1_2_different_range() throws BadLocationException {
		String xml = "<?xml version=\'1.0\'?>\r\n" + //
				"<xs:schema xmlns:xs=\'http://www.w3.org/2001/XMLSchema\'>\r\n" + //
				"	<xs:imp|ort></xs:import>\r\n" + //
				"</xs:schema>";

		Diagnostic d = d(2, 2, 2, 11, XSDErrorCode.src_import_1_2);
		testCodeActionsFor(xml, d, ca(d, te(2, 11, 2, 11, " namespace=\"\"")),
				ca(d, te(1, 54, 1, 54, " targetNamespace=\"\"")));
	}

	@Test
	public void schema_reference_4_BadSchemaLocation() throws BadLocationException {
		String xsd = "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>\r\n" + //
				"<xs:import namespace='http://foo' schemaLocation='bad.xsd' />\r\n" + //
				"<xs:element name='foo'></xs:element>\r\n" + //
				"</xs:schema>";

		Diagnostic d = d(1, 49, 1, 58, XSDErrorCode.schema_reference_4);
		testDiagnosticsFor(xsd, d);
	}

	@Test
	public void schema_reference_4_GoodSchemaLocation() throws BadLocationException {
		String xsd = "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>\r\n" + //
				"<xs:import namespace='team_namespace' schemaLocation='src/test/resources/xsd/team.xsd' />\r\n" + //
				"<xs:element name='foo'></xs:element>\r\n" + //
				"</xs:schema>";
		testDiagnosticsFor(xsd);
	}

	@Test
	public void schema_reference_4_IncludeErrorRange() throws BadLocationException {
		String xsd = "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>\r\n" + //
				"<xs:include schemaLocation='bad.xsd' />\r\n" + //
				"<xs:element name='foo'></xs:element>\r\n" + //
				"</xs:schema>";

		Diagnostic d = d(1, 27, 1, 36, XSDErrorCode.schema_reference_4);
		testDiagnosticsFor(xsd, d);
	}

	@Test
	public void src_import_3_1_BadNamespaceWithEmptySchema() throws BadLocationException {
		String xsd = "<xs:schema xmlns:xs=\'http://www.w3.org/2001/XMLSchema\'>\n" + //
				"<xs:import namespace=\'BAD_NAMESPACE\' schemaLocation=\'src/test/resources/xsd/empty.xsd\'/>\n" + //
				"<xs:element name=\'foo\'></xs:element>\n" + //
				"</xs:schema>";

		Diagnostic d = d(1, 21, 1, 36, XSDErrorCode.src_import_3_1);
		testDiagnosticsFor(xsd, d);
	}

	@Test
	public void src_import_3_1_BadNamespaceWithSchemaContent() throws BadLocationException {
		String xsd = "<xs:schema xmlns:xs=\'http://www.w3.org/2001/XMLSchema\'>\n" + //
				"<xs:import namespace=\'BAD_NAMESPACE\' schemaLocation=\'src/test/resources/xsd/baseSchema.xsd\'/>\n" + //
				"<xs:element name=\'foo\'></xs:element>\n" + //
				"</xs:schema>";

		Diagnostic d = d(1, 21, 1, 36, XSDErrorCode.src_import_3_1);
		testDiagnosticsFor(xsd, d);
	}

	@Test
	public void src_import_3_2_NoNamespaceFound() throws BadLocationException {
		String xsd = "<xs:schema\n" + //
				"xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n" + //
				"targetNamespace=\"http://example.org/my-example\"\n" + //
				"xmlns:NS=\"http://example.org/my-example\">\n" + //
				"<xs:import schemaLocation=\"src/test/resources/xsd/baseSchema.xsd\"/>\n" + //
				"</xs:schema>";

		Diagnostic d = d(4, 26, 4, 65, XSDErrorCode.src_import_3_2);
		testDiagnosticsFor(xsd, d);
	}

	@Test
	public void schema_reference_4() throws BadLocationException {
		String xsd = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\r\n" + //
				"  <xs:import\r\n" + //
				"      namespace=\"abcd\"\r\n" + //
				"      schemaLocation=\"unkown1.xsd\" />\r\n" + // <-- error with unkown1.xsd
				"  <xs:include schemaLocation=\"unkown2.xsd\" />\r\n" + // <-- error with unkown2.xsd
				"</xs:schema>";

		Diagnostic d1 = d(3, 21, 3, 34, XSDErrorCode.schema_reference_4);
		Diagnostic d2 = d(4, 29, 4, 42, XSDErrorCode.schema_reference_4);
		testDiagnosticsFor(xsd, d1, d2);
	}

	@Test
	public void aggregateErrorFromReferencedSchema() throws BadLocationException {
		String xsd = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://java.sun.com/xml/ns/j2ee\">\r\n"
				+ //
				"  <xs:import\r\n" + //
				"      schemaLocation=\"src/test/resources/xsd/foo-invalid-schema.xsd\" />\r\n" + // <-- error base.xml
																									// is not a XSD
				"  <xs:include schemaLocation=\"src/test/resources/xsd/srcElement3.xsd\" />\r\n" + // error
				"</xs:schema>";

		Diagnostic d1 = d(2, 21, 2, 68, null);
		Diagnostic d2 = d(3, 29, 3, 69, null);
		testDiagnosticsFor(xsd, d1, d2);
	}

	private static void testDiagnosticsFor(String xml, Diagnostic... expected) throws BadLocationException {
		XMLAssert.testDiagnosticsFor(xml, null, null, "test.xsd", expected);
	}
}
