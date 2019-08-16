/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.extensions.xsd;

import static org.eclipse.lsp4xml.XMLAssert.d;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.ContentModelSettings;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.XMLValidationSettings.SchemaVersion;
import org.eclipse.lsp4xml.extensions.xsd.participants.XSDErrorCode;
import org.eclipse.lsp4xml.extensions.xsd.participants.diagnostics.XSDValidator;
import org.junit.Test;

/**
 * XSD diagnostics tests which test the {@link XSDValidator} with XSD 1.1.
 *
 */
public class XSD11ValidationExtensionsTest {

	@Test
	public void minVersion_WithXSD1_0() throws BadLocationException {
		// This test use XSD 1.0 processor

		// Here minVersion is set to 1.0, there are 2 errors:
		// - because maxOccurs="unbound" on xs:element is not allowed
		// - <a /> is not allow in a XSD
		String xml = getXMLForXSD1_1Test("1.0");
		testDiagnosticsFor(xml, SchemaVersion.V10, d(15, 2, 15, 3, XSDErrorCode.s4s_elt_invalid_content_1), // <- error
																											// with <a/>
				d(11, 54, 11, 65, XSDErrorCode.cos_all_limited_2)); // <- error with maxOccurs="unbound"

		// Here minVersion is set to 1.1, Xerces doesn't validate the XSD file
		xml = getXMLForXSD1_1Test("1.1");
		testDiagnosticsFor(xml, SchemaVersion.V10);
	}

	@Test
	public void minVersion_WithXSD1_1() throws BadLocationException {
		// This test use XSD 1.1 processor

		// Here minVersion is set to 1.0, there are 1 error:
		// - <a /> is not allow in a XSD
		String xml = getXMLForXSD1_1Test("1.0");
		testDiagnosticsFor(xml, SchemaVersion.V11, d(15, 2, 15, 3, XSDErrorCode.s4s_elt_invalid_content_1)); // <- error
																												// with
																												// <a/>

		// Here minVersion is set to 1.1, there are 1 error:
		// - <a /> is not allow in a XSD
		xml = getXMLForXSD1_1Test("1.1");
		testDiagnosticsFor(xml, SchemaVersion.V11, d(15, 2, 15, 3, XSDErrorCode.s4s_elt_invalid_content_1)); // <- error
																												// with
																												// <a/>
	}

	private static String getXMLForXSD1_1Test(String minVersion) {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<xs:schema \r\n" + //
				"	xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" \r\n" + //
				"	xmlns:t=\"test\" \r\n" + //
				"	xmlns:vc=\"http://www.w3.org/2007/XMLSchema-versioning\" \r\n" + //
				"	targetNamespace=\"test\" \r\n" + //
				"	elementFormDefault=\"qualified\" \r\n" + //
				"	vc:minVersion=\"" + minVersion + "\">\r\n" + //
				"	\r\n" + //
				"	<xs:complexType name=\"testType\">\r\n" + //
				"		<xs:all>\r\n" + "			<xs:element name=\"testEle\" minOccurs=\"1\" "
				+ "maxOccurs=\"unbounded\" type=\"xs:string\"/>\r\n" // <- error with maxOccurs="unbound" XSD 1.0 but
																		// not with XSD 1.1
				+ //
				"			<xs:element name=\"testEleTwo\" type=\"xs:string\"/>\r\n" + //
				"		</xs:all>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"	<a />\r\n" + // <-- we add an error here to check
				"</xs:schema>";
	}

	private static void testDiagnosticsFor(String xml, Diagnostic... expected) throws BadLocationException {
		XMLAssert.testDiagnosticsFor(xml, null, null, "test.xsd", expected);
	}

	private static void testDiagnosticsFor(String xml, SchemaVersion schemaVersion, Diagnostic... expected)
			throws BadLocationException {
		XMLAssert.testDiagnosticsFor(xml, null, ls -> {
			ContentModelSettings settings = new ContentModelSettings();
			XMLValidationSettings problems = new XMLValidationSettings();
			problems.setSchemaVersion(schemaVersion.getVersion());
			settings.setValidation(problems);
			ls.doSave(new XMLAssert.SettingsSaveContext(settings));
		}, "test.xsd", expected);
	}
}
