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

import static org.eclipse.lemminx.XMLAssert.dl;
import static org.eclipse.lemminx.XMLAssert.r;

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.junit.jupiter.api.Test;

/**
 * XML Schema document links tests
 *
 */
public class XMLSchemaDocumentLinkTest {

	@Test
	public void noNamespaceSchemaLocation() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<Configuration xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"xsd/Format.xsd\">\r\n"
				+ //
				"  <ViewDefinitions>\r\n" + //
				"    <View>";
		XMLAssert.testDocumentLinkFor(xml, "src/test/resources/Format.xml",
				dl(r(1, 100, 1, 114), "src/test/resources/xsd/Format.xsd"));
	}

	@Test
	public void singleSchemaLocation() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<Configuration xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://example.org/schema/format xsd/Format.xsd\">\r\n"
				+ //
				"  <ViewDefinitions>\r\n" + //
				"    <View>";
		XMLAssert.testDocumentLinkFor(xml, "src/test/resources/Format.xml",
				dl(r(1, 122, 1, 136), "src/test/resources/xsd/Format.xsd"));
	}

	@Test
	public void manySchemaLocation() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
				"       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"       xmlns:camel=\"http://camel.apache.org/schema/spring\"\r\n" + //
				"       xmlns:cxf=\"http://camel.apache.org/schema/cxf\"\r\n" + //
				"       xsi:schemaLocation=\"http://www.example.org/schema/beans xsd/spring-beans.xsd http://www.example.org/schema/spring xsd/camel-spring.xsd http://www.example.org/schema/salad xsd/salad.xsd\">\r\n";
		XMLAssert.testDocumentLinkFor(xml, "src/test/resources/beans-and-salad.xml",
				dl(r(5, 63, 5, 83), "src/test/resources/xsd/spring-beans.xsd"),
				dl(r(5, 178, 5, 191), "src/test/resources/xsd/salad.xsd"),
				dl(r(5, 121, 5, 141), "src/test/resources/xsd/camel-spring.xsd")
				);
	}

	@Test
	public void lineBreakManySchemaLocation() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
				"       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"       xmlns:camel=\"http://camel.apache.org/schema/spring\"\r\n" + //
				"       xmlns:cxf=\"http://camel.apache.org/schema/cxf\"\r\n" + //
				"       xsi:schemaLocation=\"\r\n" + //
				"         http://www.example.org/schema/beans xsd/spring-beans.xsd\r\n" + //
				"         http://www.example.org/schema/spring xsd/camel-spring.xsd\r\n" + //
				"         http://www.example.org/schema/salad xsd/salad.xsd\">\r\n";
		XMLAssert.testDocumentLinkFor(xml, "src/test/resources/beans-and-salad.xml",
				dl(r(6, 45, 6, 65), "src/test/resources/xsd/spring-beans.xsd"),
				dl(r(8, 45, 8, 58), "src/test/resources/xsd/salad.xsd"),
				dl(r(7, 46, 7, 66), "src/test/resources/xsd/camel-spring.xsd"));
	}

	@Test
	public void blankSchemaLocation() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
		"<beans xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
		"       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
		"       xmlns:camel=\"http://camel.apache.org/schema/spring\"\r\n" + //
		"       xmlns:cxf=\"http://camel.apache.org/schema/cxf\"\r\n" + //
		"       xsi:schemaLocation=\"\" />";
		XMLAssert.testDocumentLinkFor(xml, "src/test/resources/xsd/bean.xml");

	}

	@Test
	public void incompleteSchemaLocation() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<Configuration xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://example.org/schema/format\">\r\n"
				+ //
				"  <ViewDefinitions>\r\n" + //
				"    <View>";
		XMLAssert.testDocumentLinkFor(xml, "src/test/resources/Format.xml");
	}

	@Test
	public void secondTokenIsIncompleteSchemaLocation() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
		"<beans xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
		"       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
		"       xmlns:camel=\"http://camel.apache.org/schema/spring\"\r\n" + //
		"       xmlns:cxf=\"http://camel.apache.org/schema/cxf\"\r\n" + //
		"       xsi:schemaLocation=\"\r\n" + //
		"         http://www.example.org/schema/beans xsd/spring-beans.xsd\r\n" + //
		"         http://www.example.org/schema/spring\" />\r\n";
		XMLAssert.testDocumentLinkFor(xml, "src/test/resources/beans-and-salad.xml",
				dl(r(6, 45, 6, 65), "src/test/resources/xsd/spring-beans.xsd"));
	}
}
