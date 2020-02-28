/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.xsd;

import static org.eclipse.lemminx.XMLAssert.hl;
import static org.eclipse.lemminx.XMLAssert.r;
import static org.eclipse.lemminx.XMLAssert.testHighlightsFor;
import static org.eclipse.lsp4j.DocumentHighlightKind.Read;
import static org.eclipse.lsp4j.DocumentHighlightKind.Write;

import org.eclipse.lemminx.commons.BadLocationException;
import org.junit.Test;

/**
 * XSD Highlighting tests.
 *
 * @author Angelo ZERR
 */
public class XSDHighlightingExtensionsTest {

	@Test
	public void highlightingOnElementType() throws BadLocationException {
		// highlighting on xs:element/@type
		String xml = "<?xml version=\"1.1\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://www.w3.org/2001/XMLSchema\">\r\n"
				+ //
				"	<xs:element name=\"elt\" type=\"x|s:aComplexType\" />\r\n" + // <- set cursor at xs:element/@type
																					// attribute
				"	<xs:complexType name=\"aComplexType\">\r\n" + // <- xs:complexType/@name is highlighted
				"		<xs:simpleContent>\r\n" + //
				"			<xs:extension base=\"xs:aSimpleTypeA\"></xs:extension>\r\n" + //
				"		</xs:simpleContent>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"	\r\n" + //
				"	<xs:complexType name=\"aComplexType2\">\r\n" + //
				"		<xs:complexContent>\r\n" + //
				"			<xs:extension base=\"xs:aComplexType\"></xs:extension>\r\n" + //
				"		</xs:complexContent>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"\r\n" + //
				"	<xs:simpleType name=\"aSimpleTypeA\">\r\n" + //
				"		<xs:restriction base=\"xs:ENTITIES\">\r\n" + //
				"		</xs:restriction>\r\n" + //
				"	</xs:simpleType>\r\n" + //
				"</xs:schema>";
		testHighlightsFor(xml, hl(r(2, 29, 2, 46), Read), hl(r(3, 22, 3, 36), Write));
	}

	@Test
	public void highlightingOnElementType2() throws BadLocationException {
		// highlighting on xs:element/@type
		String xml = "<?xml version=\"1.1\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://www.w3.org/2001/XMLSchema\">\r\n"
				+ //
				"	<xs:element name=\"elt\" type=\"x|s:aComplexType\" />\r\n" + // <- set cursor at xs:element/@type
																					// attribute
				"	<xs:complexType name=\"aComplexType\">\r\n" + // <- xs:complexType/@name is highlighted
				"		<xs:simpleContent>\r\n" + //
				"			<xs:extension base=\"xs:aSimpleTypeA\"></xs:extension>\r\n" + //
				"		</xs:simpleContent>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"	\r\n" + //
				"	<xs:complexType name=\"aComplexType2\">\r\n" + //
				"		<xs:complexContent>\r\n" + //
				"			<xs:extension base=\"xs:aComplexType\"></xs:extension>\r\n" + //
				"		</xs:complexContent>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"\r\n" + //
				"	<xs:simpleType name=\"aSimpleTypeA\">\r\n" + //
				"		<xs:restriction base=\"xs:ENTITIES\">\r\n" + //
				"		</xs:restriction>\r\n" + //
				"	</xs:simpleType>\r\n" + //
				"</xs:schema>";
		testHighlightsFor(xml, hl(r(2, 29, 2, 46), Read), hl(r(3, 22, 3, 36), Write));
	}

	@Test
	public void highlightingOnComplexTypeName() throws BadLocationException {
		// highlighting on xs:complexType/@name
		String xml = "<?xml version=\"1.1\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://www.w3.org/2001/XMLSchema\">\r\n"
				+ //
				"	<xs:element name=\"elt\" type=\"xs:aComplexType\" />\r\n" + // <-
				"	<xs:complexType name=\"aC|omplexType\">\r\n" + // <- set cursor at xs:complexType/@name
				// attribute
				"		<xs:simpleContent>\r\n" + //
				"			<xs:extension base=\"xs:aSimpleTypeA\"></xs:extension>\r\n" + //
				"		</xs:simpleContent>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"	\r\n" + //
				"	<xs:complexType name=\"aComplexType2\">\r\n" + //
				"		<xs:complexContent>\r\n" + //
				"			<xs:extension base=\"xs:aComplexType\"></xs:extension>\r\n" + //
				"		</xs:complexContent>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"\r\n" + //
				"	<xs:simpleType name=\"aSimpleTypeA\">\r\n" + //
				"		<xs:restriction base=\"xs:ENTITIES\">\r\n" + //
				"		</xs:restriction>\r\n" + //
				"	</xs:simpleType>\r\n" + //
				"</xs:schema>";
		testHighlightsFor(xml, hl(r(3, 22, 3, 36), Write), hl(r(2, 29, 2, 46), Read), hl(r(11, 22, 11, 39), Read));
	}

	@Test
	public void highlightingOnSimpleTypeName() throws BadLocationException {
		// highlighting on xs:simpleType/@name
		String xml = "<?xml version=\"1.1\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://www.w3.org/2001/XMLSchema\">\r\n"
				+ //
				"	<xs:element name=\"elt\" type=\"xs:aComplexType\" />\r\n" + // <-
				"	<xs:complexType name=\"aComplexType\">\r\n" + //
				// attribute
				"		<xs:simpleContent>\r\n" + //
				"			<xs:extension base=\"xs:aSimpleTypeA\"></xs:extension>\r\n" + //
				"		</xs:simpleContent>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"	\r\n" + //
				"	<xs:complexType name=\"aComplexType2\">\r\n" + //
				"		<xs:complexContent>\r\n" + //
				"			<xs:extension base=\"xs:aComplexType\"></xs:extension>\r\n" + //
				"		</xs:complexContent>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"\r\n" + //
				"	<xs:simpleType name=\"a|SimpleTypeA\">\r\n" + // <- set cursor at xs:simpleType/@name
				"		<xs:restriction base=\"xs:ENTITIES\">\r\n" + //
				"		</xs:restriction>\r\n" + //
				"	</xs:simpleType>\r\n" + //
				"</xs:schema>";
		testHighlightsFor(xml, hl(r(15, 21, 15, 35), Write), hl(r(5, 22, 5, 39), Read));
	}

	@Test
	public void noHighlightOnRefWithoutValue() throws BadLocationException {
		// highlighting on xs:simpleType/@name
		String xml = "<?xml version=\"1.0\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\r\n" + //
				"\r\n" + //
				"	<xs:element name=\"resources\">\r\n" + //
				"		<xs:complexType>\r\n" + //
				"			<xs:sequence>\r\n" + //
				"				<xs:element re|f"; //
		testHighlightsFor(xml);
	}

	@Test
	public void highlightWithXSInclude() throws BadLocationException {
		// - SchemaA includes SchemaB (which defines 'TypeFromB' xs:element)
		// - SchemaB includes SchemaC (which defines 'TypeFromC' xs:element)

		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<xs:schema id=\"tns\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\r\n"
				+ //
				"	<xs:include schemaLocation=\"src/test/resources/xsd/SchemaB.xsd\" />\r\n" + //
				"  \r\n" + //
				"	<xs:complexType name=\"Bar\">\r\n" + //
				"		<xs:sequence>\r\n" + //
				"			<xs:element ref=\"TypeFr|omB\" />\r\n" + //
				"		</xs:sequence>\r\n" + //
				"	</xs:complexType>";
		// TypeFromB is defined in the SchemaB, don't highlight it
		testHighlightsFor(xml, hl(r(6, 19, 6, 30), Read));

	}
}
