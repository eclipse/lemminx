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

import static org.eclipse.lsp4j.DocumentHighlightKind.Read;
import static org.eclipse.lsp4j.DocumentHighlightKind.Write;
import static org.eclipse.lsp4xml.XMLAssert.hl;
import static org.eclipse.lsp4xml.XMLAssert.r;
import static org.eclipse.lsp4xml.XMLAssert.testHighlightsFor;

import org.eclipse.lsp4xml.commons.BadLocationException;
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
}
