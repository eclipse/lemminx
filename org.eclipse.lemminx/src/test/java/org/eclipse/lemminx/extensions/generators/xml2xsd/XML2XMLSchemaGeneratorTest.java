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
package org.eclipse.lemminx.extensions.generators.xml2xsd;

import static java.lang.System.lineSeparator;
import static org.eclipse.lemminx.XMLAssert.assertGrammarGenerator;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Tests for generating XSD (XML Schema) from XML source.
 * 
 */
public class XML2XMLSchemaGeneratorTest {

	@Test
	public void schema() throws IOException {
		String xml = "<note>\r\n" + //
				"	<to>Tove</to>\r\n" + //
				"	<from>Jani</from>\r\n" + //
				"	<heading>Reminder</heading>\r\n" + //
				"	<body>Don't forget me this weekend!</body>\r\n" + //
				"</note>";
		String xsd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lineSeparator() + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" + lineSeparator() + //
				"  <xs:element name=\"note\">" + lineSeparator() + //
				"    <xs:complexType>" + lineSeparator() + //
				"      <xs:sequence>" + lineSeparator() + //
				"        <xs:element name=\"to\" type=\"xs:string\" />" + lineSeparator() + //
				"        <xs:element name=\"from\" type=\"xs:string\" />" + lineSeparator() + //
				"        <xs:element name=\"heading\" type=\"xs:string\" />" + lineSeparator() + //
				"        <xs:element name=\"body\" type=\"xs:string\" />" + lineSeparator() + //
				"      </xs:sequence>" + lineSeparator() + //
				"    </xs:complexType>" + lineSeparator() + //
				"  </xs:element>" + lineSeparator() + //
				"</xs:schema>";
		assertGrammarGenerator(xml, new XMLSchemaGeneratorSettings(), xsd);
	}

	@Test
	public void schemaWithNS() throws IOException {
		String xml = "<note xmlns=\"https://www.w3schools.com\">\r\n" + //
				"	<to>Tove</to>\r\n" + //
				"	<from>Jani</from>\r\n" + //
				"	<heading>Reminder</heading>\r\n" + //
				"	<body>Don't forget me this weekend!</body>\r\n" + //
				"</note>";
		String xsd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lineSeparator() + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"https://www.w3schools.com\">"
				+ lineSeparator() + //
				"  <xs:element name=\"note\">" + lineSeparator() + //
				"    <xs:complexType>" + lineSeparator() + //
				"      <xs:sequence>" + lineSeparator() + //
				"        <xs:element name=\"to\" type=\"xs:string\" />" + lineSeparator() + //
				"        <xs:element name=\"from\" type=\"xs:string\" />" + lineSeparator() + //
				"        <xs:element name=\"heading\" type=\"xs:string\" />" + lineSeparator() + //
				"        <xs:element name=\"body\" type=\"xs:string\" />" + lineSeparator() + //
				"      </xs:sequence>" + lineSeparator() + //
				"    </xs:complexType>" + lineSeparator() + //
				"  </xs:element>" + lineSeparator() + //
				"</xs:schema>";
		assertGrammarGenerator(xml, new XMLSchemaGeneratorSettings(), xsd);
	}

	@Test
	public void schemaWithAttr() throws IOException {
		String xml = "<note version=\"1.2\" >\r\n" + //
				"	<to attr1=\"abcd\" attr2=\"efgh\">Tove</to>\r\n" + //
				"	<from>Jani</from>\r\n" + //
				"	<heading>Reminder</heading>\r\n" + //
				"	<body>Don't forget me this weekend!</body>\r\n" + //
				"</note>";
		String xsd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lineSeparator() + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" + lineSeparator() + //
				"  <xs:element name=\"note\">" + lineSeparator() + //
				"    <xs:complexType>" + lineSeparator() + //
				"      <xs:sequence>" + lineSeparator() + //
				"        <xs:element name=\"to\">" + lineSeparator() + //
				"          <xs:complexType>" + lineSeparator() + //
				"            <xs:simpleContent>" + lineSeparator() + //
				"              <xs:extension base=\"xs:string\">" + lineSeparator() + //
				"                <xs:attribute name=\"attr2\" />" + lineSeparator() + //
				"                <xs:attribute name=\"attr1\" />" + lineSeparator() + //
				"              </xs:extension>" + lineSeparator() + //
				"            </xs:simpleContent>" + lineSeparator() + //
				"          </xs:complexType>" + lineSeparator() + //
				"        </xs:element>" + lineSeparator() + //
				"        <xs:element name=\"from\" type=\"xs:string\" />" + lineSeparator() + //
				"        <xs:element name=\"heading\" type=\"xs:string\" />" + lineSeparator() + //
				"        <xs:element name=\"body\" type=\"xs:string\" />" + lineSeparator() + //
				"      </xs:sequence>" + lineSeparator() + //
				"      <xs:attribute name=\"version\" />" + lineSeparator() + //
				"    </xs:complexType>" + lineSeparator() + //
				"  </xs:element>" + lineSeparator() + //
				"</xs:schema>";
		assertGrammarGenerator(xml, new XMLSchemaGeneratorSettings(), xsd);
	}

	@Test
	public void mixedContent() {
		String xml = "<a><b/>text</a>";
		String xsd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lineSeparator() + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" + lineSeparator() + //
				"  <xs:element name=\"a\">" + lineSeparator() + //
				"    <xs:complexType mixed=\"true\">" + lineSeparator() + //
				"      <xs:sequence>" + lineSeparator() + //
				"        <xs:element name=\"b\" />" + lineSeparator() + //
				"      </xs:sequence>" + lineSeparator() + //
				"    </xs:complexType>" + lineSeparator() + //
				"  </xs:element>" + lineSeparator() + //
				"</xs:schema>";
		assertGrammarGenerator(xml, new XMLSchemaGeneratorSettings(), xsd);
	}

	@Test
	public void threeLevel() {
		String xml = "<a>\r\n" + //
				"	<b>\r\n" + //
				"		<c/>\r\n" + //
				"	</b>\r\n" + //
				"</a>";
		String xsd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lineSeparator() + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" + lineSeparator() + //
				"  <xs:element name=\"a\">" + lineSeparator() + //
				"    <xs:complexType>" + lineSeparator() + //
				"      <xs:sequence>" + lineSeparator() + //
				"        <xs:element name=\"b\">" + lineSeparator() + //
				"          <xs:complexType>" + lineSeparator() + //
				"            <xs:sequence>" + lineSeparator() + //
				"              <xs:element name=\"c\" />" + lineSeparator() + //
				"            </xs:sequence>" + lineSeparator() + //
				"          </xs:complexType>" + lineSeparator() + //
				"        </xs:element>" + lineSeparator() + //
				"      </xs:sequence>" + lineSeparator() + //
				"    </xs:complexType>" + lineSeparator() + //
				"  </xs:element>" + lineSeparator() + //
				"</xs:schema>";
		assertGrammarGenerator(xml, new XMLSchemaGeneratorSettings(), xsd);
	}

	@Test
	public void threeLevelAndText() {
		String xml = "<a>\r\n" + //
				"	<b>\r\n" + //
				"		<c />\r\n" + //
				"		<d>X</d>\r\n" + //
				"	</b>\r\n" + //
				"</a>";
		String xsd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lineSeparator() + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" + lineSeparator() + //
				"  <xs:element name=\"a\">" + lineSeparator() + //
				"    <xs:complexType>" + lineSeparator() + //
				"      <xs:sequence>" + lineSeparator() + //
				"        <xs:element name=\"b\">" + lineSeparator() + //
				"          <xs:complexType>" + lineSeparator() + //
				"            <xs:sequence>" + lineSeparator() + //
				"              <xs:element name=\"c\" />" + lineSeparator() + //
				"              <xs:element name=\"d\" type=\"xs:string\" />" + lineSeparator() + //
				"            </xs:sequence>" + lineSeparator() + //
				"          </xs:complexType>" + lineSeparator() + //
				"        </xs:element>" + lineSeparator() + //
				"      </xs:sequence>" + lineSeparator() + //
				"    </xs:complexType>" + lineSeparator() + //
				"  </xs:element>" + lineSeparator() + //
				"</xs:schema>";
		assertGrammarGenerator(xml, new XMLSchemaGeneratorSettings(), xsd);
	}

	@Test
	public void occurrences() {
		String xml = "<invoice xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"  xsi:noNamespaceSchemaLocation=\"grocery-invoice.xsd\">\r\n" + //
				"  <item name=\"Rice\" price=\"4.99\" />\r\n" + //
				"  <item name=\"Baked Beans\" price=\"1.99\" />\r\n" + //
				"  <item name=\"Salad\" price=\"5.99\" />\r\n" + //
				"</invoice>";
		String xsd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lineSeparator() + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" + lineSeparator() + //
				"  <xs:element name=\"invoice\">" + lineSeparator() + //
				"    <xs:complexType>" + lineSeparator() + //
				"      <xs:sequence>" + lineSeparator() + //
				"        <xs:element name=\"item\" maxOccurs=\"unbounded\">" + lineSeparator() + //
				"          <xs:complexType>" + lineSeparator() + //
				"            <xs:simpleContent>" + lineSeparator() + //
				"              <xs:extension base=\"xs:string\">" + lineSeparator() + //
				"                <xs:attribute name=\"price\" />" + lineSeparator() + //
				"                <xs:attribute name=\"name\" />" + lineSeparator() + //
				"              </xs:extension>" + lineSeparator() + //
				"            </xs:simpleContent>" + lineSeparator() + //
				"          </xs:complexType>" + lineSeparator() + //
				"        </xs:element>" + lineSeparator() + //
				"      </xs:sequence>" + lineSeparator() + //
				"    </xs:complexType>" + lineSeparator() + //
				"  </xs:element>" + lineSeparator() + //
				"</xs:schema>";
		assertGrammarGenerator(xml, new XMLSchemaGeneratorSettings(), xsd);
	}
	
	@Test
	public void oneZeroOccurrences() {
		String xml = "<root>\r\n" + //
				"    <a>\r\n" + //
				"        <b />\r\n" + // 
				"    </a>\r\n" + //
				"    <a></a>\r\n" + // 
				"</root>";
		String xsd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lineSeparator() + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" + lineSeparator() + //
				"  <xs:element name=\"root\">" + lineSeparator() + //
				"    <xs:complexType>" + lineSeparator() + //
				"      <xs:sequence>" + lineSeparator() + //
				"        <xs:element name=\"a\" maxOccurs=\"unbounded\">" + lineSeparator() + //
				"          <xs:complexType>" + lineSeparator() + //				
				"            <xs:sequence minOccurs=\"0\">" + lineSeparator() + //
				"              <xs:element name=\"b\" minOccurs=\"0\" />" + lineSeparator() + //
				"            </xs:sequence>" + lineSeparator() + //				
				"          </xs:complexType>" + lineSeparator() + //
				"        </xs:element>" + lineSeparator() + //
				"      </xs:sequence>" + lineSeparator() + //
				"    </xs:complexType>" + lineSeparator() + //
				"  </xs:element>" + lineSeparator() + //
				"</xs:schema>";
		assertGrammarGenerator(xml, new XMLSchemaGeneratorSettings(), xsd);
	}
	
	@Test
	public void oneZeroOccurrences2() {
		String xml = "<root>\r\n" + //
				"    <a>\r\n" + //
				"        <b />\r\n" + // 
				"    </a>\r\n" + //
				"    <a>\r\n" + //
				"        <c />\r\n" + // 
				"    </a>\r\n" + //
				"    <a>\r\n" + //
				"        <b />\r\n" + // 				
				"        <c />\r\n" + // 
				"    </a>\r\n" + //				
				"    <a></a>\r\n" + // 
				"</root>";
		String xsd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lineSeparator() + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" + lineSeparator() + //
				"  <xs:element name=\"root\">" + lineSeparator() + //
				"    <xs:complexType>" + lineSeparator() + //
				"      <xs:sequence>" + lineSeparator() + //
				"        <xs:element name=\"a\" maxOccurs=\"unbounded\">" + lineSeparator() + //
				"          <xs:complexType>" + lineSeparator() + //				
				"            <xs:sequence minOccurs=\"0\">" + lineSeparator() + //
				"              <xs:element name=\"b\" minOccurs=\"0\" />" + lineSeparator() + //
				"              <xs:element name=\"c\" minOccurs=\"0\" />" + lineSeparator() + //				
				"            </xs:sequence>" + lineSeparator() + //				
				"          </xs:complexType>" + lineSeparator() + //
				"        </xs:element>" + lineSeparator() + //
				"      </xs:sequence>" + lineSeparator() + //
				"    </xs:complexType>" + lineSeparator() + //
				"  </xs:element>" + lineSeparator() + //
				"</xs:schema>";
		assertGrammarGenerator(xml, new XMLSchemaGeneratorSettings(), xsd);
	}
}
