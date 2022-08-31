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

import static org.eclipse.lemminx.XMLAssert.l;
import static org.eclipse.lemminx.XMLAssert.r;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lsp4j.Location;
import org.junit.jupiter.api.Test;

/**
 * XSD references tests
 *
 */
public class XSDReferenceExtensionsTest extends AbstractCacheBasedTest {

	@Test
	public void referenceOnElementName() throws BadLocationException {
		String xml = "<?xml version=\"1.1\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://www.w3.org/2001/XMLSchema\">\r\n"
				+ //
				"	<xs:element name=\"e|lt\" />\r\n" + // <-- find references from this xs:element/@name (referenced by
														// xs:element/@ref + xs:element/@substitutionGroup)
				"	<xs:complexType name=\"aComplexType\" >\r\n" + //
				"		<xs:sequence>\r\n" + //
				"			<xs:element ref=\"xs:elt\" />\r\n" + //
				"		</xs:sequence>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"	<xs:element name=\"elt2\" substitutionGroup=\"xs:elt\" />\r\n" + //
				"	<xs:group name=\"elt\" />\r\n" + //
				"</xs:schema>";
		testReferencesFor(xml, l("test.xsd", r(5, 19, 5, 27)), l("test.xsd", r(8, 43, 8, 51)));
	}

	@Test
	public void referenceOnGroupName() throws BadLocationException {
		String xml = "<?xml version=\"1.1\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://www.w3.org/2001/XMLSchema\">\r\n"
				+ //
				"	<xs:element name=\"elt\" />\r\n" + //
				"	<xs:complexType name=\"aComplexType\" >\r\n" + //
				"		<xs:sequence>\r\n" + //
				"			<xs:element ref=\"xs:elt\" />\r\n" + //
				"			<xs:group ref=\"xs:elt\"></xs:group>\r\n" + //
				"		</xs:sequence>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"	<xs:element name=\"elt2\" substitutionGroup=\"xs:elt\" />\r\n" + //
				"	<xs:group name=\"e|lt\" />\r\n" + // <-- find references from this xs:group/@name (referenced by
														// xs:group/@ref)
				"</xs:schema>";
		testReferencesFor(xml, l("test.xsd", r(6, 17, 6, 25)));
	}

	@Test
	public void referenceOnComplexTypeName() throws BadLocationException {
		String xml = "<?xml version=\"1.1\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://www.w3.org/2001/XMLSchema\">\r\n"
				+ //
				"	<xs:element name=\"elt\" type=\"xs:aComplexType\" />\r\n" + //
				"	<xs:complexType name=\"aCompl|exType\">\r\n" + // <-- find references from xs:complexType/@name
																	// (referenced by xs:element/@type +
																	// xs:extension/@base)
				"	</xs:complexType>\r\n" + //
				"	<xs:complexType name=\"aComplexType2\">\r\n" + //
				"		<xs:complexContent>\r\n" + //
				"			<xs:extension base=\"xs:aComplexType\"></xs:extension>\r\n" + //
				"		</xs:complexContent>\r\n" + //
				"	</xs:complexType>\r\n" + //
				"</xs:schema>";
		testReferencesFor(xml, l("test.xsd", r(2, 29, 2, 46)), l("test.xsd", r(7, 22, 7, 39)));
	}

	@Test
	public void referenceOnSimpleTypeName() throws BadLocationException {
		String xml = "<?xml version=\"1.1\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://www.w3.org/2001/XMLSchema\">\r\n"
				+ //
				"	<xs:element name=\"elt\" type=\"xs:aSimpleType\" />\r\n" + //
				"	<xs:simpleType name=\"aSi|mpleType\">\r\n" + // <-- find references from xs:simpleType/@name
				// (referenced by xs:element/@type +
				// xs:restriction/@base)
				"		<xs:restriction base=\"xs:boolean\"></xs:restriction>\r\n" + //
				"	</xs:simpleType>\r\n" + //
				"	<xs:simpleType name=\"aSimpleType2\">\r\n" + //
				"		<xs:restriction base=\"xs:aSimpleType\"></xs:restriction>\r\n" + //
				"	</xs:simpleType>\r\n" + //
				"</xs:schema>";
		testReferencesFor(xml, l("test.xsd", r(2, 29, 2, 45)), l("test.xsd", r(7, 23, 7, 39)));
	}

	private void testReferencesFor(String xml, Location... expectedItems) throws BadLocationException {
		XMLAssert.testReferencesFor(xml, "test.xsd", expectedItems);
	}
}
