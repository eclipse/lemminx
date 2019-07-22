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

import static org.eclipse.lsp4xml.XMLAssert.ll;
import static org.eclipse.lsp4xml.XMLAssert.r;

import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.junit.Test;

/**
 * XSD highlighting tests.
 *
 * @author Angelo ZERR
 */
public class XSDDefinitionExtensionsTest {

	@Test
	public void noXSDefinition() throws BadLocationException {
		// definition on |
		String xml = "<?xml version='1.0'?>\r\n" + //
				"<xs:sche|ma xmlns:xs='http://www.w3.org/2001/XMLSchema'>\r\n" + //
				"</xs:schema>"; //
		testDefinitionFor(xml, ll("test.xsd", r(1, 1, 1, 10), r(2, 2, 2, 11)));
	}
	
	@Test
	public void definitionOnElementType() throws BadLocationException {
		// definition on |
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
		testDefinitionFor(xml, ll("test.xsd", r(3, 34, 3, 50), r(5, 22, 5, 38)));
	}

	@Test
	public void definitionOnExtensionBase() throws BadLocationException {
		// definition on |
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
		testDefinitionFor(xml, ll("test.xsd", r(9, 22, 9, 34), r(3, 22, 3, 34)));
	}

	@Test
	public void definitionWithTargetNamespace() throws BadLocationException {
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
		testDefinitionFor(xml, ll("test.xsd", r(3, 35, 3, 60), r(6, 22, 6, 43)));
	}

	@Test
	public void definitionOnElementRef() throws BadLocationException {
		// definition on | xs:element/@ref -> xs:element/@name
		String xml = "<?xml version=\"1.1\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://www.w3.org/2001/XMLSchema\" version=\"1.0\">\r\n"
				+ //
				"	\r\n" + //
				"	<xs:group name=\"schemaTop\">\r\n" + //
				"		<xs:choice>\r\n" + //
				"			<xs:group ref=\"xs:redefinable\" />\r\n" + //
				"			<xs:element ref=\"xs:element\" />\r\n" + //
				"			<xs:element ref=\"xs:attribute\" />\r\n" + //
				"			<xs:element ref=\"|xs:notation\" />\r\n" + // here definition go to the
																		// elmeent/@name='notation'
				"		</xs:choice>\r\n" + //
				"	</xs:group>\r\n" + //
				"	\r\n" + //
				"	<xs:group name=\"redefinable\">\r\n" + //
				"		<xs:choice></xs:choice>\r\n" + //
				"	</xs:group>\r\n" + //
				"	\r\n" + //
				"	<xs:element name=\"element\" />\r\n" + //
				"	<xs:element name=\"attribute\" />\r\n" + //
				"	<xs:element name=\"notation\" />\r\n" + //
				"</xs:schema>";
		testDefinitionFor(xml, ll("test.xsd", r(8, 19, 8, 32), r(18, 18, 18, 28)));
	}

	@Test
	public void definitionOnGroupRef() throws BadLocationException {
		// definition on | xs:groupt/@ref -> xs:group/@name
		String xml = "<?xml version=\"1.1\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://www.w3.org/2001/XMLSchema\" version=\"1.0\">\r\n"
				+ //
				"	\r\n" + //
				"	<xs:group name=\"schemaTop\">\r\n" + //
				"		<xs:choice>\r\n" + //
				"			<xs:group ref=\"|xs:redefinable\" />\r\n" + // here definition go to the
																		// group/@name='redefinable'
				"			<xs:element ref=\"xs:element\" />\r\n" + //
				"			<xs:element ref=\"xs:attribute\" />\r\n" + //
				"			<xs:element ref=\"xs:notation\" />\r\n" + //
				"		</xs:choice>\r\n" + //
				"	</xs:group>\r\n" + //
				"	\r\n" + //
				"	<xs:group name=\"redefinable\">\r\n" + //
				"		<xs:choice></xs:choice>\r\n" + //
				"	</xs:group>\r\n" + //
				"	\r\n" + //
				"	<xs:element name=\"element\" />\r\n" + //
				"	<xs:element name=\"attribute\" />\r\n" + //
				"	<xs:element name=\"notation\" />\r\n" + //
				"</xs:schema>";
		testDefinitionFor(xml, ll("test.xsd", r(5, 17, 5, 33), r(12, 16, 12, 29)));
	}

	private static void testDefinitionFor(String xml, LocationLink... expectedItems) throws BadLocationException {
		XMLAssert.testDefinitionFor(xml, "test.xsd", expectedItems);
	}
}
