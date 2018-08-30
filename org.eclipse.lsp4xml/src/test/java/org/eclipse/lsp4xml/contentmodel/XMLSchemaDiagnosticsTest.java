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
import org.eclipse.lsp4xml.contentmodel.participants.diagnostics.XMLSchemaErrorCode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * XML diagnostics services tests
 *
 */
public class XMLSchemaDiagnosticsTest {

	@Test
	public void cvc_complex_type_2_4_a() throws Exception {
		String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\r\n" + //
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\r\n"
				+ //
				"<p></p>" + "</project>";
		testDiagnosticsFor(xml, d(3, 1, 3, 2, XMLSchemaErrorCode.cvc_complex_type_2_4_a));
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
