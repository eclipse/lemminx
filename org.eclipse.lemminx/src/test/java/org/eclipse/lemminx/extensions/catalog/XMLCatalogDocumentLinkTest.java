/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package org.eclipse.lemminx.extensions.catalog;

import org.junit.jupiter.api.Test;

import static org.eclipse.lemminx.XMLAssert.testDocumentLinkFor;
import static org.eclipse.lemminx.XMLAssert.dl;
import static org.eclipse.lemminx.XMLAssert.r;

/**
 * Tests for the document links in XML catalog files
 */
public class XMLCatalogDocumentLinkTest {

	private static String CATALOG_PATH = "src/test/resources/catalog.xml";

	@Test
	public void testPublicEntryDocumentLink() {
		String xml = "<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">\n" + //
				"  <public id=\"http://example.org\" uri=\"mySchema.xsd\" />\n" + //
				"</catalog>";
		testDocumentLinkFor(xml, CATALOG_PATH, //
				dl(r(1, 39, 1, 51), "src/test/resources/mySchema.xsd"));
	}

	@Test
	public void testSystemEntryDocumentLink() {
		String xml = "<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">\n" + //
				"  <system id=\"http://example.org\"\n" + //
				"    uri=\"otherSchema.xsd\" />\n" + //
				"</catalog>";
		testDocumentLinkFor(xml, CATALOG_PATH, //
				dl(r(2, 9, 2, 24), "src/test/resources/otherSchema.xsd"));
	}

	@Test
	public void testURIEntryDocumentLink() {
		String xml = "<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">\n" + //
				"  <uri\n" + //
				"    id=\"http://example.org\"\n" + //
				"    uri=\"neatSchema.xsd\" />\n" + //
				"</catalog>";
		testDocumentLinkFor(xml, CATALOG_PATH, //
				dl(r(3, 9, 3, 23), "src/test/resources/neatSchema.xsd"));
	}

	@Test
	public void testSystemSuffixEntryDocumentLink() {
		String xml = "<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">\n" + //
				"  <systemSuffix id=\"http://example.org\" uri=\"mySchema.xsd\"></systemSuffix>\n" + //
				"</catalog>";
		testDocumentLinkFor(xml, CATALOG_PATH, //
				dl(r(1, 45, 1, 57), "src/test/resources/mySchema.xsd"));
	}

	@Test
	public void testURISuffixEntryDocumentLink() {
		String xml = "<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">\n" + //
				"  <uriSuffix id=\"http://example.org\" uri=\"mySchema.xsd\"></uriSuffix>\n" + //
				"</catalog>";
		testDocumentLinkFor(xml, CATALOG_PATH, //
				dl(r(1, 42, 1, 54), "src/test/resources/mySchema.xsd"));
	}

	@Test
	public void testMustBeCatalog1() {
		String xml = "<catalog><public url=\"document.xsd\" /></catalog>";
		testDocumentLinkFor(xml, CATALOG_PATH);
	}

	@Test
	public void testMustBeCatalog2() {
		String xml = "<aaa xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">\n" + //
				"  <public url=\"document.xsd\" />\n" + //
				"</aaa>";
		testDocumentLinkFor(xml, CATALOG_PATH);
	}

	@Test
	public void testOnlyEntriesHaveLinks() {
		String xml = "<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">\n" + //
				"  <aaa id=\"http://example.org\" uri=\"mySchema.xsd\"></aaa>\n" + //
				"</catalog>";
		testDocumentLinkFor(xml, CATALOG_PATH);
	}

	@Test
	public void testPublicEntryWithSpacesDocumentLink() {
		String xml = "<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">\n" + //
				"  <public id=\"http://example.org\" uri=\"my%20schema.xsd\" />\n" + //
				"</catalog>";
		testDocumentLinkFor(xml, CATALOG_PATH, //
				dl(r(1, 39, 1, 54), "src/test/resources/my schema.xsd"));
	}

}