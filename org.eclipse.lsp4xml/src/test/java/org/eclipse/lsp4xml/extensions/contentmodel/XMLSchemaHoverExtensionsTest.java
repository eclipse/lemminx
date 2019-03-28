/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.contentmodel;

import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.extensions.xsi.XSISchemaModel;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.junit.Test;

/**
 * XML hover tests with XML Schema.
 *
 */
public class XMLSchemaHoverExtensionsTest {

	@Test
	public void testTagHover() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ "	<bea|n>";
		assertHover(xml,
				"Defines a single (usually named) bean. A bean definition may contain nested tags for constructor arguments, property values, lookup methods, and replaced methods. Mixing constructor injection and setter injection on the same bean is explicitly supported.",
				2);
	};

	@Test
	public void testAttributeNameHover() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ "	<bean clas|s=>";
		assertHover(xml,
				"The fully qualified name of the bean's class, except if it serves only as a parent definition for child bean definitions.",
				null);
	};

	@Test
	public void testTagHoverFromXSType() throws BadLocationException {
		// web.xml servlet, servlet-name declares their xs:annotation not in the element
		// declaration but in type (servletType),
		// this test checks that
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoi|ce xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ " xsi:schemaLocation=\"http://invoice xsd/invoice.xsd \">\r\n";
		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/invoice.xml",
				"An invoice type...", null);
	};

	@Test
	public void testTagHoverForSchemaLocation() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoice xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ " xsi:schema|Location=\"http://invoice xsd/invoice.xsd \">\r\n";
		
		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/invoice.xml",
				XSISchemaModel.SCHEMA_LOCATION_DOC, null);
	};

	@Test
	public void testTagHoverForNoNamespaceSchemaLocation() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoice xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ " xsi:noNamespace|SchemaLocation=\"http://invoice xsd/invoice.xsd \">\r\n";
	
		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/invoice.xml",
				XSISchemaModel.NO_NAMESPACE_SCHEMA_LOCATION_DOC, null);
	};

	@Test
	public void testTagHoverForXSInil() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoice xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ " xsi:n|il=\"http://invoice xsd/invoice.xsd \">\r\n";

		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/invoice.xml",
				XSISchemaModel.NIL_DOC, null);
	};

	@Test
	public void testTagHoverForXSIType() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoice xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ " xsi:ty|pe=\"http://invoice xsd/invoice.xsd \">\r\n";
		
		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/invoice.xml",
				XSISchemaModel.TYPE_DOC, null);
	};

	@Test
	public void testTagHoverForXSIType2() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoice xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ " x|si:type=\"http://invoice xsd/invoice.xsd \">\r\n";
		
		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/invoice.xml",
				XSISchemaModel.TYPE_DOC, null);
	};

	@Test
	public void testTagHoverForXSIBadPrefix() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoice xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ " BAD:t|ype=\"http://invoice xsd/invoice.xsd \">\r\n";
		
		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/invoice.xml",
				null, null);
	};

	@Test
	public void testTagHoverForXSITypeNotRoot() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoice xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ ">\r\n" +
				"<a xsi:ty|pe=\"\"></a>";
		
		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/invoice.xml",
				XSISchemaModel.TYPE_DOC, null);
	};

	@Test
	public void testTagHoverForXSINILNotRoot() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoice xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ ">\r\n" +
				"<a xsi:n|il=\"\"></a>";
		
		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/invoice.xml",
				XSISchemaModel.NIL_DOC, null);
	};

	@Test
	public void testTagHoverForXSISchemaNotRoot() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoice xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ ">\r\n" +
				"<a xsi:noNamespa|ceSchemaLocation\"\"></a>";
		
		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/invoice.xml",
				null, null);
	};

	@Test
	public void testHoverAttributeValueEuro() throws BadLocationException {
		String xml = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
		"<money xmlns=\"http://money\" currency=\"eu|ros\"\r\n" + // <- Hover
		"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" +
		"	xsi:schemaLocation=\"http://money xsd/money.xsd\"></money>";
		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/money.xml",
				"Euro Hover", null);
	};

	@Test
	public void testHoverAttributeValuePound() throws BadLocationException {
		String xml = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
		"<money xmlns=\"http://money\" currency=\"pou|nds\"\r\n" + // <- Hover
		"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" +
		"	xsi:schemaLocation=\"http://money xsd/money.xsd\"></money>";
		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/money.xml",
				"Pound Hover", null);
	};


	@Test
	public void testHoverAttributeValueNonExistent() throws BadLocationException {
		String xml = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
		"<money xmlns=\"http://money\" curr|ency=\"pounds\"\r\n" + // <- Hover
		"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" +
		"	xsi:schemaLocation=\"http://money xsd/money.xsd\"></money>";
		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/money.xml",
				"Currency name Hover", null);
	};

	private static void assertHover(String value, String expectedHoverLabel, Integer expectedHoverOffset)
			throws BadLocationException {
		XMLAssert.assertHover(new XMLLanguageService(), value, "src/test/resources/catalogs/catalog.xml", null,
				expectedHoverLabel, expectedHoverOffset);
	}

}
