/**
 *  Copyright (c) 2019 Red Hat, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.contentmodel;

import static org.eclipse.lsp4xml.XMLAssert.d;

import java.io.IOException;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.DTDErrorCode;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.XMLSchemaErrorCode;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.ContentModelSettings;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.junit.Test;

/**
 * Test to check the LSP XML Grammar Pool.
 * 
 * @author Angelo ZERR
 *
 */
public class XMLValidationPoolCacheTest extends BaseFileTempTest {

	@Test
	public void twoNoNamespaceSchemaLocation() {
		XMLLanguageService xmlLanguageService = new XMLLanguageService();

		// Validate 2 xml bound with noNamespaceSchemaLocation
		// By default Xerces XMLGrammarPool can store only the first XML Schema bound
		// with noNamespaceSchemaLocation
		// This test validate 2 XML file bound with 2 different XML Schema using
		// noNamespaceSchemaLocation

		String xml = "<money xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"src/test/resources/xsd/money.xsd\" currency=\"euros\"> </money>";
		Diagnostic d = d(0, 143, 0, 144, XMLSchemaErrorCode.cvc_complex_type_2_1);
		testDiagnosticsFor(xmlLanguageService, xml, d);
		// validate a second time (to use cached Grammar)
		testDiagnosticsFor(xmlLanguageService, xml, d);

		xml = "<Annotation\r\n" + "	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"	xsi:noNamespaceSchemaLocation=\"src/test/resources/xsd/pattern.xsd\"\r\n" + //
				"	Term=\"X\"></Annotation>";
		Diagnostic patternValid = d(3, 6, 3, 9, XMLSchemaErrorCode.cvc_pattern_valid);
		Diagnostic cvcAttribute3 = d(3, 6, 3, 9, XMLSchemaErrorCode.cvc_attribute_3);
		testDiagnosticsFor(xmlLanguageService, xml, patternValid, cvcAttribute3);
	}

	@Test
	public void includedSchemaLocation() throws IOException {
		XMLLanguageService xmlLanguageService = new XMLLanguageService();

		String schemaAPath = tempDirUri.getPath() + "/SchemaA.xsd";
		String schemaA = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<xs:schema id=\"tns\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\r\n"
				+ //
				"	<xs:include schemaLocation=\"SchemaB.xsd\" />\r\n" + //
				"\r\n" + //
				"	<xs:complexType name=\"Bar\">\r\n" + //
				"		<xs:sequence>\r\n" + //
				"			<xs:element name=\"FooBar\" type=\"xs:string\" />\r\n" + //
				"			<xs:element ref=\"AType\" />\r\n" + //
				"		</xs:sequence>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"\r\n" + //
				"	<xs:complexType name=\"Root\">\r\n" + //
				"		<xs:sequence>\r\n" + //
				"			<xs:element name=\"Bar\" type=\"Bar\" minOccurs=\"1\" maxOccurs=\"unbounded\" />\r\n" + //
				"		</xs:sequence>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"\r\n" + //
				"	<xs:element name=\"Root\" type=\"Root\"></xs:element>\r\n" + //
				"</xs:schema>";
		createFile(schemaAPath, schemaA);

		String schemaBPath = tempDirUri.getPath() + "/SchemaB.xsd";
		String schemaB = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\r\n" + //
				"\r\n" + //
				"	<xs:element name=\"AType\">\r\n" + //
				"		<xs:complexType>\r\n" + //
				"			<xs:sequence>\r\n" + //
				"				<xs:element name=\"XMLElement\" type=\"xs:string\" />\r\n" + //
				"			</xs:sequence>\r\n" + //
				"		</xs:complexType>\r\n" + //
				"	</xs:element>\r\n" + //
				"\r\n" + //
				"</xs:schema>";
		createFile(schemaBPath, schemaB);

		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<Root xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\""
				+ schemaAPath + "\">\r\n" + //
				"	<Bar>\r\n" + //
				"		\r\n" + // <-- error FooBar missing
				"	</Bar>\r\n" + //
				"</Root>";
		Diagnostic d = d(2, 2, 5, XMLSchemaErrorCode.cvc_complex_type_2_4_b);
		testDiagnosticsFor(xmlLanguageService, xml, d);

		// Update Schema A -> remove sequence from Bar
		schemaA = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<xs:schema id=\"tns\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\r\n"
				+ //
				"	<xs:include schemaLocation=\"SchemaB.xsd\" />\r\n" + //
				"\r\n" + //
				"	<xs:complexType name=\"Bar\">\r\n" + //
				// " <xs:sequence>\r\n" + // <-- remove sequence from Bar
				// " <xs:element name=\"FooBar\" type=\"xs:string\" />\r\n" + //
				// " <xs:element ref=\"AType\" />\r\n" + //
				// " </xs:sequence>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"\r\n" + //
				"	<xs:complexType name=\"Root\">\r\n" + //
				"		<xs:sequence>\r\n" + //
				"			<xs:element name=\"Bar\" type=\"Bar\" minOccurs=\"1\" maxOccurs=\"unbounded\" />\r\n" + //
				"		</xs:sequence>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"\r\n" + //
				"	<xs:element name=\"Root\" type=\"Root\"></xs:element>\r\n" + //
				"</xs:schema>";
		createFile(schemaAPath, schemaA);
		xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<Root xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\""
				+ schemaAPath + "\">\r\n" + //
				"	<Bar>\r\n" + //
				"		\r\n" + // <-- error FooBar must have no character
				"	</Bar>\r\n" + //
				"</Root>";
		d = d(2, 6, 4, 1, XMLSchemaErrorCode.cvc_complex_type_2_1);
		testDiagnosticsFor(xmlLanguageService, xml, d);

		// Update Schema A as init
		schemaA = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<xs:schema id=\"tns\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\r\n"
				+ //
				"	<xs:include schemaLocation=\"SchemaB.xsd\" />\r\n" + //
				"\r\n" + //
				"	<xs:complexType name=\"Bar\">\r\n" + //
				"		<xs:sequence>\r\n" + //
				"			<xs:element name=\"FooBar\" type=\"xs:string\" />\r\n" + //
				"			<xs:element ref=\"AType\" />\r\n" + //
				"		</xs:sequence>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"\r\n" + //
				"	<xs:complexType name=\"Root\">\r\n" + //
				"		<xs:sequence>\r\n" + //
				"			<xs:element name=\"Bar\" type=\"Bar\" minOccurs=\"1\" maxOccurs=\"unbounded\" />\r\n" + //
				"		</xs:sequence>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"\r\n" + //
				"	<xs:element name=\"Root\" type=\"Root\"></xs:element>\r\n" + //
				"</xs:schema>";
		createFile(schemaAPath, schemaA);
		xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<Root xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\""
				+ schemaAPath + "\">\r\n" + //
				"	<Bar>\r\n" + //
				"		<FooBar></FooBar>\r\n" + //
				"		<AType>\r\n" + //
				"			\r\n" + // <-- error XMLElement from Schema A is not declared
				"		</AType>\r\n" + //
				"	</Bar>\r\n" + //
				"</Root> ";
		d = d(4, 3, 8, XMLSchemaErrorCode.cvc_complex_type_2_4_b);
		testDiagnosticsFor(xmlLanguageService, xml, d);

		// Update included Schema B
		schemaB = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\r\n" + //
				"\r\n" + //
				"	<xs:element name=\"AType\">\r\n" + //
				"		<xs:complexType>\r\n" + //
				// " <xs:sequence>\r\n" + //
				// " <xs:element name=\"XMLElement\" type=\"xs:string\" />\r\n" + //
				// " </xs:sequence>\r\n" + //
				"		</xs:complexType>\r\n" + //
				"	</xs:element>\r\n" + //
				"\r\n" + //
				"</xs:schema>";
		createFile(schemaBPath, schemaB);

		xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<Root xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\""
				+ schemaAPath + "\">\r\n" + //
				"	<Bar>\r\n" + //
				"		<FooBar></FooBar>\r\n" + //
				"		<AType>\r\n" + //
				"			\r\n" + // <-- error AType must have no character
				"		</AType>\r\n" + //
				"	</Bar>\r\n" + //
				"</Root> ";
		d = d(4, 9, 6, 2, XMLSchemaErrorCode.cvc_complex_type_2_1);
		testDiagnosticsFor(xmlLanguageService, xml, d);
	}

	@Test
	public void dtd() throws IOException {
		XMLLanguageService xmlLanguageService = new XMLLanguageService();

		String dtdPath = tempDirUri.getPath() + "/note.dtd";
		String dtd = "<!ELEMENT note (to)>\r\n" + //
				"<!ELEMENT to (#PCDATA)>\r\n" + //
				"";
		createFile(dtdPath, dtd);

		// One error
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<!DOCTYPE note SYSTEM \"" + dtdPath + "\">\r\n" + //
				"<note>\r\n" + //
				// " <to>Tove</to> \r\n" + //
				"</note>";

		Diagnostic d = d(2, 1, 5, DTDErrorCode.MSG_CONTENT_INCOMPLETE);
		testDiagnosticsFor(xmlLanguageService, xml, d);

		// No error
		xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<!DOCTYPE note SYSTEM \"" + dtdPath + "\">\r\n" + //
				"<note>\r\n" + //
				" <to>Tove</to> \r\n" + //
				"</note>";

		testDiagnosticsFor(xmlLanguageService, xml);

		// Update dtd -> add from
		dtd = "<!ELEMENT note (to,from)>\r\n" + //
				"<!ELEMENT to (#PCDATA)>\r\n" + //
				"";
		createFile(dtdPath, dtd);
		d = d(2, 1, 5, DTDErrorCode.MSG_CONTENT_INCOMPLETE);
		testDiagnosticsFor(xmlLanguageService, xml, d);
	}

	private static void testDiagnosticsFor(XMLLanguageService xmlLanguageService, String xml, Diagnostic... expected) {
		String catalogPath = "src/test/resources/catalogs/catalog.xml";
		ContentModelSettings settings = new ContentModelSettings();
		XMLAssert.testDiagnosticsFor(xmlLanguageService, xml, catalogPath, null, null, true, settings, expected);
	}
}
