/**
 * Copyright (c) 2020 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *      Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lemminx.extensions.xsd;

import static org.eclipse.lemminx.XMLAssert.dl;
import static org.eclipse.lemminx.XMLAssert.r;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.junit.jupiter.api.Test;

/**
 * Tests for the document links in .xsd provided by
 * <code>XSDDocumentLinkParticipant</code>
 *
 * @see org.eclipse.lemminx.extensions.xsd.participants.XSDDocumentLinkParticipant
 */
public class XSDDocumentLinkingExtensionsTest extends AbstractCacheBasedTest {

	@Test
	public void xsIncludeUsualNamespace() throws BadLocationException {
		String xml = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" + //
				"    <xs:include schemaLocation=\"choice.xsd\"></xs:include>\n" + //
				"    <xs:element name=\"int\">\n" + //
				"        <xs:simpleType>\n" + //
				"            <xs:restriction base=\"xs:integer\"/>\n" + //
				"        </xs:simpleType>\n" + //
				"    </xs:element>\n" + //
				"</xs:schema>";
		XMLAssert.testDocumentLinkFor(xml, "src/test/resources/xsd/unnamed-integer.xsd",
				dl(r(1, 32, 1, 42), "src/test/resources/xsd/choice.xsd"));
	}

	@Test
	public void xsIncludeDifferentNamespace() throws BadLocationException {
		String xml = "<schemanamespace:schema xmlns:schemanamespace=\"http://www.w3.org/2001/XMLSchema\">\n" + //
				"    <schemanamespace:include schemaLocation=\"choice.xsd\"></schemanamespace:include>\n" + //
				"    <schemanamespace:element name=\"int\">\n" + //
				"        <schemanamespace:simpleType>\n" + //
				"            <schemanamespace:restriction base=\"schemanamespace:integer\"/>\n" + //
				"        </schemanamespace:simpleType>\n" + //
				"    </schemanamespace:element>\n" + //
				"</schemanamespace:schema>";
		XMLAssert.testDocumentLinkFor(xml, "src/test/resources/xsd/unnamed-integer.xsd",
				dl(r(1, 45, 1, 55), "src/test/resources/xsd/choice.xsd"));
	}

	@Test
	public void xsIncludeEmptySchemaLocation() throws BadLocationException {
		String xml = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" + //
				"    <xs:include schemaLocation=\"\"></xs:include>\n" + //
				"    <xs:element name=\"int\">\n" + //
				"        <xs:simpleType>\n" + //
				"            <xs:restriction base=\"xs:integer\"/>\n" + //
				"        </xs:simpleType>\n" + //
				"    </xs:element>\n" + //
				"</xs:schema>";
		XMLAssert.testDocumentLinkFor(xml, "src/test/resources/xsd/unnamed-integer.xsd");
	}

	@Test
	public void xsIncludeManyOccurences() throws BadLocationException {
		String xml = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" + //
				"    <xs:include schemaLocation=\"choice.xsd\"></xs:include>\n" + //
				"    <xs:include schemaLocation=\"pattern.xsd\"></xs:include>\n" + //
				"    <xs:element name=\"int\">\n" + //
				"        <xs:simpleType>\n" + //
				"            <xs:restriction base=\"xs:integer\"/>\n" + //
				"        </xs:simpleType>\n" + //
				"    </xs:element>\n" + //
				"</xs:schema>";
		XMLAssert.testDocumentLinkFor(xml, "src/test/resources/xsd/unnamed-integer.xsd",
				dl(r(1, 32, 1, 42), "src/test/resources/xsd/choice.xsd"),
				dl(r(2, 32, 2, 43), "src/test/resources/xsd/pattern.xsd"));
	}

	@Test
	public void xsIncludeNoSchemaLocation() throws BadLocationException {
		String xml = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" + //
				"    <xs:include />\n" + //
				"    <xs:element name=\"int\">\n" + //
				"        <xs:simpleType>\n" + //
				"            <xs:restriction base=\"xs:integer\"/>\n" + //
				"        </xs:simpleType>\n" + //
				"    </xs:element>\n" + //
				"</xs:schema>";
		XMLAssert.testDocumentLinkFor(xml, "src/test/resources/xsd/unnamed-integer.xsd");
	}

	@Test
	public void xsImportUsualNamespace() throws BadLocationException {
		String xml = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" + //
				"    <xs:import namespace=\"\" schemaLocation=\"choice.xsd\"></xs:import>\n" + //
				"    <xs:element name=\"int\">\n" + //
				"        <xs:simpleType>\n" + //
				"            <xs:restriction base=\"xs:integer\"/>\n" + //
				"        </xs:simpleType>\n" + //
				"    </xs:element>\n" + //
				"</xs:schema>";
		XMLAssert.testDocumentLinkFor(xml, "src/test/resources/xsd/unnamed-integer.xsd",
				dl(r(1, 44, 1, 54), "src/test/resources/xsd/choice.xsd"));
	}

	@Test
	public void xsImportDifferentNamespace() throws BadLocationException {
		String xml = "<schemanamespace:schema xmlns:schemanamespace=\"http://www.w3.org/2001/XMLSchema\">\n" + //
				"    <schemanamespace:import namespace=\"\" schemaLocation=\"choice.xsd\" />\n" + //
				"    <schemanamespace:element name=\"int\">\n" + //
				"        <schemanamespace:simpleType>\n" + //
				"            <schemanamespace:restriction base=\"schemanamespace:integer\"/>\n" + //
				"        </schemanamespace:simpleType>\n" + //
				"    </schemanamespace:element>\n" + //
				"</schemanamespace:schema>";
		XMLAssert.testDocumentLinkFor(xml, "src/test/resources/xsd/unnamed-integer.xsd",
				dl(r(1, 57, 1, 67), "src/test/resources/xsd/choice.xsd"));
	}

	@Test
	public void xsImportEmptySchemaLocation() throws BadLocationException {
		String xml = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" + //
				"    <xs:import namespace=\"\" schemaLocation=\"\" />\n" + //
				"    <xs:element name=\"int\">\n" + //
				"        <xs:simpleType>\n" + //
				"            <xs:restriction base=\"xs:integer\"/>\n" + //
				"        </xs:simpleType>\n" + //
				"    </xs:element>\n" + //
				"</xs:schema>";
		XMLAssert.testDocumentLinkFor(xml, "src/test/resources/xsd/unnamed-integer.xsd");
	}

	@Test
	public void xsImportManyOccurences() throws BadLocationException {
		// TOFU:
		String xml = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" + //
				"    <xs:import namespace=\"\" schemaLocation=\"choice.xsd\" />\n" + //
				"    <xs:import namespace=\"\" schemaLocation=\"pattern.xsd\" />\n" + //
				"    <xs:element name=\"int\">\n" + //
				"        <xs:simpleType>\n" + //
				"            <xs:restriction base=\"xs:integer\"/>\n" + //
				"        </xs:simpleType>\n" + //
				"    </xs:element>\n" + //
				"</xs:schema>";
		XMLAssert.testDocumentLinkFor(xml, "src/test/resources/xsd/unnamed-integer.xsd",
				dl(r(1, 44, 1, 54), "src/test/resources/xsd/choice.xsd"),
				dl(r(2, 44, 2, 55), "src/test/resources/xsd/pattern.xsd"));
	}

	@Test
	public void xsImportNoSchemaLocation() throws BadLocationException {
		String xml = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" + //
				"    <xs:import namespace=\"\" />\n" + //
				"    <xs:element name=\"int\">\n" + //
				"        <xs:simpleType>\n" + //
				"            <xs:restriction base=\"xs:integer\"/>\n" + //
				"        </xs:simpleType>\n" + //
				"    </xs:element>\n" + //
				"</xs:schema>";
		XMLAssert.testDocumentLinkFor(xml, "src/test/resources/xsd/unnamed-integer.xsd");
	}

	@Test
	public void mixedIncludeImport() throws BadLocationException {
		String xml = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" + //
				"    <xs:import namespace=\"\" schemaLocation=\"pattern.xsd\" />\n" + //
				"    <xs:include schemaLocation=\"choice.xsd\" />\n" + //
				"    <xs:element name=\"int\">\n" + //
				"        <xs:simpleType>\n" + //
				"            <xs:restriction base=\"xs:integer\"/>\n" + //
				"        </xs:simpleType>\n" + //
				"    </xs:element>\n" + //
				"</xs:schema>";
		XMLAssert.testDocumentLinkFor(xml, "src/test/resources/xsd/unnamed-integer.xsd",
				dl(r(1, 44, 1, 55), "src/test/resources/xsd/pattern.xsd"),
				dl(r(2, 32, 2, 42), "src/test/resources/xsd/choice.xsd"));
	}

}
