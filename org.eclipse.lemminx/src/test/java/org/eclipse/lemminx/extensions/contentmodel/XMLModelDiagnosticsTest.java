/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.contentmodel;

import static org.eclipse.lemminx.XMLAssert.d;

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.extensions.contentmodel.participants.DTDErrorCode;
import org.eclipse.lemminx.extensions.contentmodel.participants.XMLSchemaErrorCode;
import org.eclipse.lsp4j.Diagnostic;
import org.junit.jupiter.api.Test;

/**
 * XML Validation test with xml-model processing instruction association.
 *
 */
public class XMLModelDiagnosticsTest {

	@Test
	public void xmlModelWithBadDTD() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> \r\n" + //
				"<?xml-model href=\"BAD.dtd\"?>\r\n" + //
				"<root><item /></root>";
		testDiagnosticsFor(xml, d(1, 17, 26, DTDErrorCode.dtd_not_found));
	}

	@Test
	public void xmlModelWithDTD() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> \r\n" + //
				"<?xml-model href=\"src/test/resources/dtd/web-app_2_3.dtd\"?>\r\n" + //
				"<web-app>\r\n" + //
				"	<XXX></XXX>\r\n" + //
				"</web-app>";
		testDiagnosticsFor(xml, d(3, 2, 5, DTDErrorCode.MSG_ELEMENT_NOT_DECLARED),
				d(2, 1, 8, DTDErrorCode.MSG_CONTENT_INVALID));
	}

	@Test
	public void xmlModelWithBadXSD() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> \r\n" + //
				"<?xml-model href=\"BAD.xsd\"?>\r\n" + //
				"<root><item /></root>";
		testDiagnosticsFor(xml, d(1, 17, 26, XMLSchemaErrorCode.schema_reference_4), //
				d(2, 1, 5, XMLSchemaErrorCode.cvc_elt_1_a));
	}

	@Test
	public void xmlModelWithXSD() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<?xml-model href=\"src/test/resources/xsd/tag.xsd\"?>\r\n" + //
				"<root>\r\n" + //
				"  <tag></tag>\r\n" + //
				"  <optional></optional>\r\n" + //
				"  <optional></optional>\r\n" + //
				"  <optional></optional>\r\n" + //
				"</root>";
		testDiagnosticsFor(xml, d(6, 3, 6, 11, XMLSchemaErrorCode.cvc_complex_type_2_4_f));
	}

	@Test
	public void xmlModelWithXSDAndNamespace() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<?xml-model href=\"http://www.springframework.org/schema/beans/spring-beans-3.0.xsd\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" >\r\n" + //
				"	<bean>\r\n" + //
				"		XXXXXXXXXXXXX\r\n" + // <-- error
				"	</bean>\r\n" + //
				"</beans>";
		Diagnostic d = d(4, 2, 4, 15, XMLSchemaErrorCode.cvc_complex_type_2_3,
				"Element \'bean\' cannot contain text content.\nThe content type is defined as element-only.\n\nCode:");
		testDiagnosticsFor(xml, d);
	}

	private static void testDiagnosticsFor(String xml, Diagnostic... expected) {
		XMLAssert.testDiagnosticsFor(xml, "src/test/resources/catalogs/catalog.xml", expected);
	}
}
