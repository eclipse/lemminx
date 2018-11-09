/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.contentmodel;

import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.junit.Before;
import org.junit.Test;

/**
 * XML hover tests with XML Schema.
 *
 */
public class XMLSchemaHoverExtensionsTest {

	@Before
	public void tearDown() {
		ContentModelManager.getInstance().setRootURI(null);
		ContentModelManager.getInstance().setUseCache(false);
		ContentModelManager.getInstance().setCatalogs(null);
	}

	@Test
	public void testTagHover() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ "	<bea|n>";
		assertHover(xml,
				"Defines a single (usually named) bean. A bean definition may contain nested tags for constructor arguments, property values, lookup methods, and replaced methods. Mixing constructor injection and setter injection on the same bean is explicitly supported. ",
				2);

	};

	@Test
	public void testAttributeNameHover() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ "	<bean clas|s=>";
		assertHover(xml,
				"The fully qualified name of the bean's class, except if it serves only as a parent definition for child bean definitions. ",
				null);

	};

	@Test
	public void testTagHoverFromXSType() throws BadLocationException {
		// web.xml servlet, servlet-name declares their xs:annotation not in the element
		// declaration but in type (servletType),
		// this test check that
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoi|ce xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ " xsi:schemaLocation=\"http://invoice xsd/invoice.xsd \">\r\n";
		XMLAssert.assertHover(new XMLLanguageService(), xml, null, "src/test/resources/invoice.xml",
				"An invoice type...", null);

	};

	private static void assertHover(String value, String expectedHoverLabel, Integer expectedHoverOffset)
			throws BadLocationException {
		XMLAssert.assertHover(new XMLLanguageService(), value, "src/test/resources/catalogs/catalog.xml", null,
				expectedHoverLabel, expectedHoverOffset);
	}

}
