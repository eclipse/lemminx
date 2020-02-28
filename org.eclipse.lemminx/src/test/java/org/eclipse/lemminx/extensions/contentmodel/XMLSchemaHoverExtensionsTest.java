/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.extensions.contentmodel;

import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.util.URI.MalformedURIException;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.xsi.XSISchemaModel;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.junit.Test;

/**
 * XML hover tests with XML Schema.
 *
 */
public class XMLSchemaHoverExtensionsTest {

	@Test
	public void testTagHover() throws BadLocationException, MalformedURIException {
		String schemaURI = getXMLSchemaFileURI("spring-beans-3.0.xsd");
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ "	<bea|n>";
		assertHover(xml,
				"Defines a single (usually named) bean. A bean definition may contain nested tags for constructor arguments, property values, lookup methods, and replaced methods. Mixing constructor injection and setter injection on the same bean is explicitly supported."
						+ //
						System.lineSeparator() + //
						System.lineSeparator() + "Source: [spring-beans-3.0.xsd](" + schemaURI + ")",
				2);
	};

	@Test
	public void testAttributeNameHover() throws BadLocationException, MalformedURIException {
		String schemaURI = getXMLSchemaFileURI("spring-beans-3.0.xsd");
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ "	<bean clas|s=>";
		assertHover(xml,
				"The fully qualified name of the bean's class, except if it serves only as a parent definition for child bean definitions."
						+ //
						System.lineSeparator() + //
						System.lineSeparator() + "Source: [spring-beans-3.0.xsd](" + schemaURI + ")",
				null);
	};

	@Test
	public void testTagHoverFromXSType() throws BadLocationException, MalformedURIException {
		String schemaURI = getXMLSchemaFileURI("invoice.xsd");
		// web.xml servlet, servlet-name declares their xs:annotation not in the element
		// declaration but in type (servletType),
		// this test checks that
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoi|ce xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				" xsi:schemaLocation=\"http://invoice xsd/invoice.xsd \">\r\n";
		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/invoice.xml",
				"An invoice type..." + //
						System.lineSeparator() + //
						System.lineSeparator() + "Source: [invoice.xsd](" + schemaURI + ")",
				null);
	};

	@Test
	public void testTagHoverForSchemaLocation() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoice xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				" xsi:schema|Location=\"http://invoice xsd/invoice.xsd \">\r\n";
		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/invoice.xml",
				XSISchemaModel.SCHEMA_LOCATION_DOC, null);
	};

	@Test
	public void testTagHoverForNoNamespaceSchemaLocation() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoice xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				" xsi:noNamespace|SchemaLocation=\"http://invoice xsd/invoice.xsd \">\r\n";
		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/invoice.xml",
				XSISchemaModel.NO_NAMESPACE_SCHEMA_LOCATION_DOC, null);
	};

	@Test
	public void testTagHoverForXSInil() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoice xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				" xsi:n|il=\"http://invoice xsd/invoice.xsd \">\r\n";
		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/invoice.xml",
				XSISchemaModel.NIL_DOC, null);
	};

	@Test
	public void testTagHoverForXSIType() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoice xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				" xsi:ty|pe=\"http://invoice xsd/invoice.xsd \">\r\n";
		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/invoice.xml",
				XSISchemaModel.TYPE_DOC, null);
	};

	@Test
	public void testTagHoverForXSIType2() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoice xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				" x|si:type=\"http://invoice xsd/invoice.xsd \">\r\n";
		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/invoice.xml",
				XSISchemaModel.TYPE_DOC, null);
	};

	@Test
	public void testTagHoverForXSIBadPrefix() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoice xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				" BAD:t|ype=\"http://invoice xsd/invoice.xsd \">\r\n";
		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/invoice.xml", null, null);
	};

	@Test
	public void testTagHoverForXSITypeNotRoot() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoice xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				">\r\n" + //
				"<a xsi:ty|pe=\"\"></a>";
		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/invoice.xml",
				XSISchemaModel.TYPE_DOC, null);
	};

	@Test
	public void testTagHoverForXSINILNotRoot() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoice xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				">\r\n" + //
				"<a xsi:n|il=\"\"></a>";

		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/invoice.xml",
				XSISchemaModel.NIL_DOC, null);
	};

	@Test
	public void testTagHoverForXSISchemaNotRoot() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoice xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				">\r\n" + //
				"<a xsi:noNamespa|ceSchemaLocation\"\"></a>";

		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/invoice.xml", null, null);
	};

	@Test
	public void testHoverAttributeValueEuro() throws BadLocationException, MalformedURIException {
		String schemaURI = getXMLSchemaFileURI("money.xsd");
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<money xmlns=\"http://money\" currency=\"eu|ros\"\r\n" + // <- Hover
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"	xsi:schemaLocation=\"http://money xsd/money.xsd\"></money>";
		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/money.xml", "Euro Hover" + //
				System.lineSeparator() + //
				System.lineSeparator() + "Source: [money.xsd](" + schemaURI + ")", null);
	};

	@Test
	public void testHoverAttributeValuePound() throws BadLocationException, MalformedURIException {
		String schemaURI = getXMLSchemaFileURI("money.xsd");
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<money xmlns=\"http://money\" currency=\"pou|nds\"\r\n" + // <- Hover
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"	xsi:schemaLocation=\"http://money xsd/money.xsd\"></money>";
		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/money.xml", "Pound Hover" + //
				System.lineSeparator() + //
				System.lineSeparator() + "Source: [money.xsd](" + schemaURI + ")", null);
	};

	@Test
	public void testHoverAttributeValueNonExistent() throws BadLocationException, MalformedURIException {
		String schemaURI = getXMLSchemaFileURI("money.xsd");
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<money xmlns=\"http://money\" curr|ency=\"pounds\"\r\n" + // <- Hover
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"	xsi:schemaLocation=\"http://money xsd/money.xsd\"></money>";
		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/money.xml",
				"Currency name Hover" + //
						System.lineSeparator() + //
						System.lineSeparator() + "Source: [money.xsd](" + schemaURI + ")",
				null);
	};

	private static void assertHover(String value, String expectedHoverLabel, Integer expectedHoverOffset)
			throws BadLocationException {
		XMLAssert.assertHover(new XMLLanguageService(), value, "src/test/resources/catalogs/catalog.xml", null,
				expectedHoverLabel, expectedHoverOffset);
	}

	private static String getXMLSchemaFileURI(String schemaURI) throws MalformedURIException {
		return XMLEntityManager.expandSystemId("xsd/" + schemaURI, "src/test/resources/test.xml", true).replace("///",
				"/");
	}
}
