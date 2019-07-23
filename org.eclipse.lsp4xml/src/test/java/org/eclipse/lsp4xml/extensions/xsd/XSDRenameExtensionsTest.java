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

import static org.eclipse.lsp4xml.XMLAssert.assertRename;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.junit.Test;

/**
 * XSD rename tests.
 *
 */
public class XSDRenameExtensionsTest {

	@Test
	public void testRenameComplexTypeName() throws BadLocationException {

		String xml = 
			"<?xml version=\"1.1\" ?>\r\n" +
			"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\r\n" +
			"\r\n" +
			"  <xs:element name=\"employee\" type=\"personinfo\"></xs:element>\r\n" +
			"  <xs:complexType name=\"|personinfo\"></xs:complexType>\r\n" +
			"  <xs:complexType name=\"fullpersoninfo\">\r\n" +
			"    <xs:complexContent>\r\n" +
			"      <xs:extension base=\"personinfo\"></xs:extension>\r\n" +
			"    </xs:complexContent>\r\n" +
			"  </xs:complexType>\r\n" +
			"</xs:schema>";

		assertRename(xml, "newName", edits("newName", r(4, 24, 34), r(3, 36, 46), r(7, 26, 36))); 
	}

	@Test
	public void testRenameSimpleTypeName() throws BadLocationException {

		String xml = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" +
			"<xsd:schema elementFormDefault=\"qualified\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://invoice\" xmlns:i=\"http://invoice\">\r\n" +
			"	<xsd:simpleType name=\"paymentMe|thodType\">\r\n" +
			"		<xsd:restriction base=\"xsd:string\">\r\n" +
			"		</xsd:restriction>\r\n" +
			"	</xsd:simpleType>\r\n" +
			"</xsd:schema>";

		assertRename(xml, "newName", edits("newName", r(2, 23, 40))); 
	}

	@Test
	public void testRenameSimpleTypeNameWithPrefix() throws BadLocationException {

		String xml = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" +
			"<xsd:schema elementFormDefault=\"qualified\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://invoice\" xmlns:i=\"http://invoice\">\r\n" +
			"	<xsd:complexType name=\"paymentType\">\r\n" +
			"		<xsd:attribute name=\"method\" type=\"i:paymentMethodType\" use=\"required\"></xsd:attribute>\r\n" +
			"	</xsd:complexType>\r\n" +
			"	<xsd:simpleType name=\"paymentMetho|dType\">\r\n" +
			"		<xsd:restriction base=\"xsd:string\">\r\n" +
			"		</xsd:restriction>\r\n" +
			"	</xsd:simpleType>\r\n" +
			"</xsd:schema>";

		assertRename(xml, "newName", edits("newName", r(5, 23, 40), r(3, 39, 56))); 
	}

	private static Range r(int line, int startCharacter, int endCharacter) {
		return new Range(new Position(line, startCharacter), new Position(line, endCharacter));
	}

	private static List<TextEdit> edits(String newText, Range... ranges) {
		return Stream.of(ranges).map(r -> new TextEdit(r, newText)).collect(Collectors.toList());
	}

}