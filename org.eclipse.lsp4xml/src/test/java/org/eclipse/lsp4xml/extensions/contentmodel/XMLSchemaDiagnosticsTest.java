/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.contentmodel;

import static org.eclipse.lsp4xml.XMLAssert.ca;
import static org.eclipse.lsp4xml.XMLAssert.d;
import static org.eclipse.lsp4xml.XMLAssert.te;
import static org.eclipse.lsp4xml.XMLAssert.testCodeActionsFor;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.XMLSchemaErrorCode;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.ContentModelSettings;
import org.junit.Test;

/**
 * XML diagnostics services tests
 *
 */
public class XMLSchemaDiagnosticsTest {

	@Test
	public void prematureEOFNoErrorReported() throws Exception {
		String xml = " ";
		testDiagnosticsFor(xml);
	}

	@Test
	public void prematureEOFWithPrologNoErrorReported() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?> ";
		testDiagnosticsFor(xml);
	}

	@Test
	public void cvc_complex_type_2_3() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ //
				"	<bean>\r\n" + //
				"		XXXXXXXXXXXXX\r\n" + // <-- error
				"	</bean>\r\n" + //
				"</beans>";
		Diagnostic d = d(3, 2, 3, 15, XMLSchemaErrorCode.cvc_complex_type_2_3, "Element \'bean\' cannot contain text content.\nThe content type is defined as element-only.\n\nCode:");
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(3, 2, 3, 15, "")));
	}

	@Test
	public void cvc_complex_type_4() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ //
				"	<bean>\r\n" + //
				"		<property></property>\r\n" + //
				"	</bean>\r\n" + //
				"</beans>";
		Diagnostic d = d(3, 3, 3, 11, XMLSchemaErrorCode.cvc_complex_type_4, "Attribute:\n - name\nis required in element:\n - property\n\nCode:");
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(3, 11, 3, 11, " name=\"\"")));
	}

	@Test
	public void cvc_type_4_Multiple_attributes() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<invoice xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				" xsi:noNamespaceSchemaLocation=\"src/test/resources/xsd/invoice.xsd\">\r\n" + //
				"  <date>2017-11-30</date>\r\n" + // 
				"  <number>2</number>\r\n" + //
				"  <products>\r\n" + //
				"  	<product />\r\n" + // <- error
				"  </products>\r\n" + //
				"  <payments>\r\n" + //
				"  	<payment amount=\"1\" method=\"credit\"/>\r\n" + //
				"  </payments>\r\n" + //
				"</invoice>";
		Diagnostic d2 = d(6, 4, 6, 11, XMLSchemaErrorCode.cvc_complex_type_4, "Attribute:\n - description\nis required in element:\n - product\n\nCode:");
		Diagnostic d1 = d(6, 4, 6, 11, XMLSchemaErrorCode.cvc_complex_type_4, "Attribute:\n - price\nis required in element:\n - product\n\nCode:");
		testDiagnosticsFor(xml, d1, d2);

		testCodeActionsFor(xml, d1, ca(d1, te(6, 11, 6, 11, " price=\"\" description=\"\"")));
	}

	@Test
	public void cvc_complex_type_2_4_a() throws Exception {
		String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\r\n" + //
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\r\n"
				+ //
				"	<XXX></XXX>\r\n" + // <- error
				"</project>";

		String message = "Invalid element name:\n - XXX\n\nOne of the following is expected:\n - modelVersion\n - parent\n - groupId\n - artifactId\n - version\n - packaging\n - name\n - description\n - url\n - inceptionYear\n - organization\n - licenses\n - developers\n - contributors\n - mailingLists\n - prerequisites\n - modules\n - scm\n - issueManagement\n - ciManagement\n - distributionManagement\n - properties\n - dependencyManagement\n - dependencies\n - repositories\n - pluginRepositories\n - build\n - reports\n - reporting\n - profiles\n\nError indicated by:\n {http://maven.apache.org/POM/4.0.0}\nwith code:";
		testDiagnosticsFor(xml, d(3, 2, 3, 5, XMLSchemaErrorCode.cvc_complex_type_2_4_a, message));
	}

	@Test
	public void cvc_complex_type_2_4_a_Disabled_Validation() throws Exception {
		String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\r\n" + //
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\r\n"
				+ //
				"	<XXX></XXX>\r\n" + // <- error
				"</project>";
		testDiagnosticsDisabledValidation(xml);
	}

	@Test
	public void cvc_complex_type_2_4_d() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				+ "<beans xmlns=\"http://www.springframework.org/schema/beans\" " + //
				"xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ //
				"	<bean>\r\n" + //
				"		<description>\r\n" + //
				"			<XXXX />\r\n" + // <- error
				"		</description>\r\n" + //
				"	</bean>\r\n" + //
				"</beans>";
		testDiagnosticsFor(xml, d(4, 4, 4, 8, XMLSchemaErrorCode.cvc_complex_type_2_4_d));
	}

	@Test
	public void cvc_type_3_1_1() throws Exception {
		String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\r\n" + //
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\r\n"
				+ //
				"<modelVersion XXXX=\"\" ></modelVersion>" + "</project>";
		Diagnostic d = d(3, 14, 3, 21, XMLSchemaErrorCode.cvc_type_3_1_1);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(3, 14, 3, 21, "")));
	}

	@Test
	public void cvc_complex_type_3_2_2() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ //
				"	<bean XXXX=\"\" >\r\n" + // <- error
				"	</bean>              \r\n" + //
				"</beans>";
		Diagnostic d = d(2, 7, 2, 11, XMLSchemaErrorCode.cvc_complex_type_3_2_2);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(2, 7, 2, 14, "")));
	}

	@Test
	public void cvc_attribute_3() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ //
				"	<bean autowire=\"ERROR\" />\r\n" + // <- error
				"</beans>";
		testDiagnosticsFor(xml, d(2, 16, 2, 23, XMLSchemaErrorCode.cvc_enumeration_valid),
				d(2, 16, 2, 23, XMLSchemaErrorCode.cvc_attribute_3));
	}

	@Test
	public void cvc_type_3_1_3() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<invoice xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				" xsi:noNamespaceSchemaLocation=\"src/test/resources/xsd/invoice.xsd\">\r\n" + //
				"  <date>2017-11-30_INVALID</date>\r\n" + // <- error
				"  <number>0</number>\r\n" + //
				"  <products>\r\n" + //
				"  	<product price=\"1\" description=\"\"/>\r\n" + //
				"  </products>\r\n" + //
				"  <payments>\r\n" + //
				"  	<payment amount=\"1\" method=\"credit\"/>\r\n" + //
				"  </payments>\r\n" + //
				"</invoice>";
		testDiagnosticsFor(xml, d(3, 8, 3, 26, XMLSchemaErrorCode.cvc_datatype_valid_1_2_1),
				d(3, 8, 3, 26, XMLSchemaErrorCode.cvc_type_3_1_3));
	}

	@Test
	public void cvc_enumeration_validOnAttribute() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				+ "<invoice xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ " xsi:noNamespaceSchemaLocation=\"src/test/resources/xsd/invoice.xsd\">\r\n" + //
				"  <date>2017-11-30</date>\r\n" + //
				"  <number>0</number>\r\n" + //
				"  <products>\r\n" + //
				"  	<product price=\"1\" description=\"\"/>\r\n" + //
				"  </products>\r\n" + //
				"  <payments>\r\n" + //
				"  	<payment amount=\"1\" method=\"credit_invalid\"/>\r\n" + //
				"  </payments>\r\n" + //
				"</invoice>";
		testDiagnosticsFor(xml, d(9, 30, 9, 46, XMLSchemaErrorCode.cvc_enumeration_valid),
				d(9, 30, 9, 46, XMLSchemaErrorCode.cvc_attribute_3));
	}

	@Test
	public void cvc_enumeration_validOnText() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<team\r\n" + //
				"     xmlns=\"team_namespace\"\r\n" + //
				"     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"     xsi:schemaLocation=\"team_namespace src/test/resources/xsd/team.xsd \">\r\n" + //
				"	<member\r\n" + //
				"	       name=\"John\"\r\n" + //
				"	       badgeNumber=\"1\"\r\n" + //
				"	       role=\"architect\">\r\n" + //
				"		<skills>\r\n" + //
				"			<skill>XXXXX</skill>\r\n" + // <- error
				"		</skills> \r\n" + //
				"		<focus>\r\n" + //
				"			<server\r\n" + //
				"			       language=\"Java\" />\r\n" + //
				"		</focus>\r\n" + //
				"	</member>\r\n" + //
				"</team>";
		Diagnostic d = d(10, 10, 10, 15, XMLSchemaErrorCode.cvc_enumeration_valid);
		testDiagnosticsFor(xml, d, d(10, 10, 10, 15, XMLSchemaErrorCode.cvc_type_3_1_3));
		testCodeActionsFor(xml, d, ca(d, te(10, 10, 10, 15, "Java")), ca(d, te(10, 10, 10, 15, "Node")),
				ca(d, te(10, 10, 10, 15, "XML")));
	}

	@Test
	public void cvc_datatype_valid_1_2_1OnAttributeValue() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				+ "<invoice xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ " xsi:noNamespaceSchemaLocation=\"src/test/resources/xsd/invoice.xsd\">\r\n" + //
				"  <date>2017-11-30</date>\r\n" + //
				"  <number>5235</number> \r\n" + //
				"  <products> \r\n" + //
				"    <product description=\"laptop\" price=\"700.00_INVALID\"/>  \r\n" + // <- error
				"    <product description=\"mouse\" price=\"30.00\" />\r\n" + //
				"  </products> \r\n" + //
				"  <payments>\r\n" + //
				"    <payment amount=\"770.00\" method=\"credit\"/>\r\n" + //
				"  </payments>\r\n" + //
				"</invoice> ";
		testDiagnosticsFor(xml, d(6, 40, 6, 56, XMLSchemaErrorCode.cvc_datatype_valid_1_2_1),
				d(6, 40, 6, 56, XMLSchemaErrorCode.cvc_attribute_3));
	}

	@Test
	public void cvc_datatype_valid_1_2_1OnText() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				+ "<invoice xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ " xsi:noNamespaceSchemaLocation=\"src/test/resources/xsd/invoice.xsd\">\r\n" + //
				"  <date>2017-11-30</date>\r\n" + //
				"  <number>5235_INVALID</number> \r\n" + // <- error
				"  <products> \r\n" + //
				"    <product description=\"laptop\" price=\"700.00\"/>  \r\n" + //
				"    <product description=\"mouse\" price=\"30.00\" />\r\n" + //
				"  </products> \r\n" + //
				"  <payments>\r\n" + //
				"    <payment amount=\"770.00\" method=\"credit\"/>\r\n" + //
				"  </payments>\r\n" + //
				"</invoice> ";
		testDiagnosticsFor(xml, d(4, 10, 4, 22, XMLSchemaErrorCode.cvc_datatype_valid_1_2_1),
				d(4, 10, 4, 22, XMLSchemaErrorCode.cvc_type_3_1_3));
	}

	@Test
	public void cvc_maxLength_validOnAttribute() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<team\r\n" + //
				"     name=\"too long a string\"\r\n" + // <- error
				"     xmlns=\"team_namespace\"\r\n" + //
				"     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"     xsi:schemaLocation=\"team_namespace src/test/resources/xsd/team.xsd \">\r\n" + //
				"	<member\r\n" + //
				"	       name=\"John\"\r\n" + //
				"	       badgeNumber=\"1\"\r\n" + //
				"	       role=\"architect\">\r\n" + //
				"		<skills>\r\n" + //
				"			<skill>Java</skill>\r\n" + "		</skills> \r\n" + //
				"		<focus>\r\n" + //
				"			<server\r\n" + //
				"			       language=\"Java\" />\r\n" + //
				"		</focus>\r\n" + //
				"	</member>\r\n" + //
				"</team>";
		testDiagnosticsFor(xml, d(2, 10, 2, 29, XMLSchemaErrorCode.cvc_maxlength_valid),
				d(2, 10, 2, 29, XMLSchemaErrorCode.cvc_attribute_3));

	}

	@Test
	public void cvc_complex_type_2_1() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"      xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
				"      xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd\"\r\n"
				+ //
				"      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n" + //
				"	<alias name=\"\" alias=\"\" >XXXX</alias>\r\n" + // <- error
				"</beans>";
		Diagnostic d = d(5, 26, 5, 30, XMLSchemaErrorCode.cvc_complex_type_2_1);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(5, 25, 5, 38, "/>")));
	}

	@Test
	public void cvc_complex_type_2_1_SelfClosing() throws Exception {
		String xml = "<money xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"src/test/resources/xsd/money.xsd\" currency=\"euros\"> </money>";
		
		Diagnostic d = d(0, 143, 0, 144, XMLSchemaErrorCode.cvc_complex_type_2_1);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(0, 142, 0, 152, "/>")));
	}
	
	@Test
	public void cvc_complex_type_2_1WithLinefeed() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"      xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
				"      xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd\"\r\n"
				+ //
				"      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n" + //
				"	<alias name=\"\" alias=\"\" >\r\n   \r\n</alias>\r\n" + // <- error
				"</beans>";
		Diagnostic d = d(5, 26, 7, 0, XMLSchemaErrorCode.cvc_complex_type_2_1);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(5, 25, 7, 8, "/>")));
	}

	/**
	 * @see https://github.com/angelozerr/lsp4xml/issues/217
	 */
	@Test
	public void issue217() {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Version=\"4.0\">\r\n"
				+ //
				"  \r\n" + //
				"</edmx:Edmx>";
		Diagnostic d = d(1, 1, 1, 10, XMLSchemaErrorCode.cvc_complex_type_2_4_b, "Child elements are missing from element:\n - edmx:Edmx\n\nThe following elements are expected:\n - Reference\n - DataServices\n\nError indicated by\n {http://docs.oasis-open.org/odata/ns/edmx\":Reference, \"http://docs.oasis-open.org/odata/ns/edmx}\nwith code:");
		testDiagnosticsFor(xml, d);
	}

	@Test
	public void cvc_elt_3_1() throws Exception {
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"<xs:schema\r\n" + //
				"		elementFormDefault=\"qualified\"\r\n" + //
				"		xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\r\n" + //
				"		xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n" + //
				"	<xs:complexType\r\n" + //
				"			name=\"property\"\r\n" + //
				"			xsi:nil=\"true\"></xs:complexType>\r\n" + //
				"</xs:schema>";
		XMLAssert.testDiagnosticsFor(xml, d(7, 3, 7, 17, XMLSchemaErrorCode.cvc_elt_3_1));

	}

	@Test
	public void cvc_type_3_2_1() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<invoice xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				" xsi:noNamespaceSchemaLocation=\"src/test/resources/xsd/invoice.xsd\">\r\n" + //
				"  <date xsi:nil=\"true\">2017-11-30</date>\r\n" + // <- error
				"  <number>0</number>\r\n" + //
				"  <products>\r\n" + //
				"  	<product price=\"1\" description=\"\"/>\r\n" + //
				"  </products>\r\n" + //
				"  <payments>\r\n" + //
				"  	<payment amount=\"1\" method=\"credit\"/>\r\n" + //
				"  </payments>\r\n" + //
				"</invoice>";
		testDiagnosticsFor(xml,	d(3, 23, 3, 33, XMLSchemaErrorCode.cvc_elt_3_2_1));
	}

	@Test
	public void cvc_type_3_1_2() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<invoice xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				" xsi:noNamespaceSchemaLocation=\"src/test/resources/xsd/invoice.xsd\">\r\n" + //
				"  <date>2017-11-30</date>\r\n" + // 
				"  <number><a></a></number>\r\n" + //<- error
				"  <products>\r\n" + //
				"  	<product price=\"1\" description=\"\"/>\r\n" + //
				"  </products>\r\n" + //
				"  <payments>\r\n" + //
				"  	<payment amount=\"1\" method=\"credit\"/>\r\n" + //
				"  </payments>\r\n" + //
				"</invoice>";
		testDiagnosticsFor(xml,	
					d(4, 3, 4, 9, XMLSchemaErrorCode.cvc_type_3_1_2),
					d(4,10,4,17, XMLSchemaErrorCode.cvc_datatype_valid_1_2_1),
					d(4,10,4,17, XMLSchemaErrorCode.cvc_type_3_1_3));
	}

	private static void testDiagnosticsFor(String xml, Diagnostic... expected) {
		XMLAssert.testDiagnosticsFor(xml, "src/test/resources/catalogs/catalog.xml", expected);
	}

	private static void testDiagnosticsDisabledValidation(String xml) {
		ContentModelSettings settings = XMLAssert.getContentModelSettings(true, false);
		XMLAssert.testDiagnosticsFor(xml, "src/test/resources/catalogs/catalog.xml", null, null, true, settings);
	}

}
