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
package org.eclipse.lsp4xml.contentmodel;

import static org.eclipse.lsp4xml.XMLAssert.d;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.contentmodel.participants.XMLSchemaErrorCode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * XML diagnostics services tests
 *
 */
public class XMLSchemaDiagnosticsTest {

	@Test
	public void cvc_complex_type_2_3() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ //
				"	<bean>\r\n" + //
				"		XXXXXXXXXXXXX\r\n" + // <-- error
				"	</bean>\r\n" + //
				"</beans>";
		testDiagnosticsFor(xml, d(4, 8, 4, 8, XMLSchemaErrorCode.cvc_complex_type_2_3));
	}

	@Test
	public void cvc_complex_type_2_4_a() throws Exception {
		String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\r\n" + //
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\r\n"
				+ //
				"	<XXX></XXX>\r\n" + // <- error
				"</project>";
		testDiagnosticsFor(xml, d(3, 2, 3, 5, XMLSchemaErrorCode.cvc_complex_type_2_4_a));
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
		testDiagnosticsFor(xml, d(3, 23, 3, 23, XMLSchemaErrorCode.cvc_type_3_1_1));
	}

	@Ignore
	@Test
	public void testcvc_complex_type_3_2_2() throws Exception {
		String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\r\n" + //
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\r\n"
				+ //
				"<modelVersion XXXX=\"\" ></modelVersion>" + "</project>";
		testDiagnosticsFor(xml, d(3, 1, 3, 2, XMLSchemaErrorCode.cvc_complex_type_3_2_2));
	}

	private static void testDiagnosticsFor(String xml, Diagnostic... expected) {
		XMLAssert.testDiagnosticsFor(xml, "src/test/resources/catalogs/catalog.xml", expected);
	}

}
