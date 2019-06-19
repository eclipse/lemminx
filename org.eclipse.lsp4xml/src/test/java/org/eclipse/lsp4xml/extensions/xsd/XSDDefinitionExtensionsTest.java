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

import static org.eclipse.lsp4xml.XMLAssert.l;
import static org.eclipse.lsp4xml.XMLAssert.r;

import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.junit.Test;

/**
 * XSD definition tests.
 *
 */
public class XSDDefinitionExtensionsTest {

	@Test
	public void elementTypeDefinition() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=\"1.1\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\r\n"
				+ //
				"	\r\n" + //
				"	<xs:element name=\"employee\" type=\"|fullpersoninfo\" />\r\n" + // here the definition is declared
																						// in complexType/@name =
																						// 'fullpersoninfo'
				"	\r\n" + //
				"	<xs:complexType name=\"fullpersoninfo\">	\r\n" + //
				"	</xs:complexType>\r\n" + //
				"	\r\n" + //
				"</xs:schema>";
		testDefinitionFor(xml, l("test.xsd", r(3, 29, 3, 50), r(5, 17, 5, 38)));
	}

	@Test
	public void extensionBaseDefinition() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=\"1.1\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\r\n"
				+ //
				"	\r\n" + //
				"	<xs:complexType name=\"personinfo\">\r\n" + //
				"		\r\n" + //
				"	</xs:complexType>\r\n" + //
				"	\r\n" + //
				"	<xs:complexType name=\"fullpersoninfo\">\r\n" + //
				"		<xs:complexContent>\r\n" + //
				"			<xs:extension base=\"|personinfo\">\r\n" + // // here the definition is declared in
																		// complexType/@name ='personinfo'
				"		\r\n" + //
				"			</xs:extension>\r\n" + //
				"		</xs:complexContent>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"	\r\n" + //
				"</xs:schema>";
		testDefinitionFor(xml, l("test.xsd", r(9, 17, 9, 34), r(3, 17, 3, 34)));
	}

	@Test
	public void targetDefinition() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n"
				+ "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:tns=\"http://camel.apache.org/schema/spring\" elementFormDefault=\"qualified\" targetNamespace=\"http://camel.apache.org/schema/spring\" version=\"1.0\">\r\n"
				+ //
				"	\r\n" + //
				"	<xs:element name=\"aggregate\" type=\"|tns:aggregateDefinition\">\r\n" + // here the definition is
																								// declared in
																								// complexType/@name
																								// ='aggregateDefinition'
				"	</xs:element>\r\n" + //
				"	\r\n" + //
				"	<xs:complexType name=\"aggregateDefinition\">\r\n" + //
				"	</xs:complexType>\r\n" + //
				"	\r\n" + //
				"</xs:schema>";
		testDefinitionFor(xml, l("test.xsd", r(3, 30, 3, 60), r(6, 17, 6, 43)));
	}

	private static void testDefinitionFor(String xml, LocationLink... expectedItems) throws BadLocationException {
		XMLAssert.testDefinitionFor(xml, "test.xsd", expectedItems);
	}
}
