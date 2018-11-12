/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.contentmodel;

import static org.eclipse.lsp4xml.XMLAssert.c;
import static org.eclipse.lsp4xml.XMLAssert.te;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.junit.Ignore;
import org.junit.Test;

/**
 * XSD completion tests.
 *
 */
public class XMLSchemaCompletionExtensionsTest {

	@Test
	public void completionInRoot() throws BadLocationException {
		// completion on <|
		String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\r\n" + //
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\r\n"
				+ //
				"	<|" + //
				"</project>";
		testCompletionFor(xml, c("modelVersion", te(3, 1, 3, 2, "<modelVersion></modelVersion>"), "<modelVersion"), //
				c("parent", "<parent></parent>", "<parent"));
		// completion on <| >
		xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\r\n" + //
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\r\n"
				+ //
				"	<|   >" + //
				"</project>";
		testCompletionFor(xml, c("modelVersion", te(3, 1, 3, 6, "<modelVersion></modelVersion>"), "<modelVersion"), //
				c("parent", "<parent></parent>", "<parent"));
		// completion on |
		xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\r\n" + //
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\r\n"
				+ //
				"	|" + //
				"</project>";
		testCompletionFor(xml, c("modelVersion", te(3, 1, 3, 1, "<modelVersion></modelVersion>"), "modelVersion"), //
				c("parent", "<parent></parent>", "parent"));
		// completion on mod
		xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\r\n" + //
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\r\n"
				+ //
				"	mod|" + //
				"</project>";
		testCompletionFor(xml, c("modelVersion", te(3, 1, 3, 4, "<modelVersion></modelVersion>"), "modelVersion"), //
				c("parent", "<parent></parent>", "parent"));
	}

	@Test
	public void completionInRootWithCloseBracket() throws BadLocationException {
		String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\r\n" + //
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\r\n"
				+ //
				"	<|  >" + // here last '<' is replaced with <modelVersion></modelVersion>
				"</project>";
		testCompletionFor(xml, c("modelVersion", te(3, 1, 3, 5, "<modelVersion></modelVersion>"), "<modelVersion"), //
				c("parent", "<parent></parent>", "<parent"));
	}

	@Test
	public void completionInChildElement() throws BadLocationException {
		String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\r\n" + //
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\r\n"
				+ //
				"	<parent><|" + //
				"</project>";
		testCompletionFor(xml, c("groupId", "<groupId></groupId>", "<groupId"), //
				c("artifactId", "<artifactId></artifactId>", "<artifactId"), //
				c("version", "<version></version>", "<version"));
	}

	@Test
	@Ignore
	public void completionInChildElementNoOpenBracket() throws BadLocationException {
		String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\r\n" + //
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\r\n"
				+ //
				"	<parent>|" + //
				"</project>";
		testCompletionFor(xml, c("groupId", "<groupId></groupId>", "groupId"), //
				c("artifactId", "<artifactId></artifactId>", "artifactId"), //
				c("version", "<version></version>", "version"));
	}

	@Test
	public void completionInRootWithAndWithoutPrefixes() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
				"       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"       xmlns:camel=\"http://camel.apache.org/schema/spring\"\r\n" + //
				"       xmlns:cxf=\"http://camel.apache.org/schema/cxf\"\r\n" + //
				"       xsi:schemaLocation=\"\r\n" + //
				"         http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"         http://camel.apache.org/schema/spring http://camel.apache.org/schema/spring/camel-spring.xsd\r\n"
				+ //
				"         http://camel.apache.org/schema/cxf http://camel.apache.org/schema/cxf/camel-cxf.xsd\">\r\n" + //
				"\r\n" + //
				"  <| ";
		testCompletionFor(xml, c("bean", "<bean></bean>", "<bean"),
				c("camel:camelContext", "<camel:camelContext></camel:camelContext>", "<camel:camelContext"));
	}

	@Test
	public void completionInRootWithAndWithoutPrefixesNoOpenBracket() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
				"       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"       xmlns:camel=\"http://camel.apache.org/schema/spring\"\r\n" + //
				"       xmlns:cxf=\"http://camel.apache.org/schema/cxf\"\r\n" + //
				"       xsi:schemaLocation=\"\r\n" + //
				"         http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"         http://camel.apache.org/schema/spring http://camel.apache.org/schema/spring/camel-spring.xsd\r\n"
				+ //
				"         http://camel.apache.org/schema/cxf http://camel.apache.org/schema/cxf/camel-cxf.xsd\">\r\n" + //
				"\r\n" + //
				"  | ";
		testCompletionFor(xml, c("bean", "<bean></bean>", "bean"),
				c("camel:camelContext", "<camel:camelContext></camel:camelContext>", "camel:camelContext"));
	}

	@Test
	public void completionInRootWithOnlyPrefix() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
				"       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"       xmlns:camel=\"http://camel.apache.org/schema/spring\"\r\n" + //
				"       xmlns:cxf=\"http://camel.apache.org/schema/cxf\"\r\n" + //
				"       xsi:schemaLocation=\"\r\n" + //
				"         http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"         http://camel.apache.org/schema/spring http://camel.apache.org/schema/spring/camel-spring.xsd\r\n"
				+ //
				"         http://camel.apache.org/schema/cxf http://camel.apache.org/schema/cxf/camel-cxf.xsd\">\r\n" + //
				"\r\n" + //
				"  <camel:| ";
		testCompletionFor(xml, c("camel:camelContext", "<camel:camelContext></camel:camelContext>"));
	}

	@Test
	public void completionInChildElementWithPrefixes() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
				"       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"       xmlns:camel=\"http://camel.apache.org/schema/spring\"\r\n" + //
				"       xmlns:cxf=\"http://camel.apache.org/schema/cxf\"\r\n" + //
				"       xsi:schemaLocation=\"\r\n" + //
				"         http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"         http://camel.apache.org/schema/spring http://camel.apache.org/schema/spring/camel-spring.xsd\r\n"
				+ //
				"         http://camel.apache.org/schema/cxf http://camel.apache.org/schema/cxf/camel-cxf.xsd\">\r\n" + //
				"\r\n" + //
				"  <camel:camelContext><| ";
		testCompletionFor(xml, c("camel:route", "<camel:route></camel:route>"),
				c("camel:template", "<camel:template />"));
	}

	@Test
	public void completionInChildElementWithElementWhichDeclareNS() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<root>\r\n" + //
				"  <camelContext id=\"camel\" xmlns=\"http://camel.apache.org/schema/spring\">\r\n" + "\r\n" + //
				"    <|";
		testCompletionFor(xml, c("route", "<route></route>"), c("template", "<template />"));
	}

	@Test
	public void noNamespaceSchemaLocationCompletion() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<Configuration xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"xsd/Format.xsd\">\r\n"
				+ //
				"  <ViewDefinitions>\r\n" + //
				"    <View><|";
		XMLAssert.testCompletionFor(xml, null, "src/test/resources/Format.xml", null, c("Name", "<Name></Name>"),
				c("ViewSelectedBy", "<ViewSelectedBy></ViewSelectedBy>"));
	}

	@Test
	public void schemaLocationWithXSDFileSystemCompletion() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoice xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ " xsi:schemaLocation=\"http://invoice xsd/invoice.xsd \">\r\n" + //
				"  <|";
		XMLAssert.testCompletionFor(xml, null, "src/test/resources/invoice.xml", null, c("date", "<date></date>"),
				c("number", "<number></number>"));
	}

	@Test
	public void completionOnAttributeName() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ //
				"	<bean |/>";
		testCompletionFor(xml, c("abstract", "abstract=\"false\""), c("autowire", "autowire=\"default\""),
				c("class", "class=\"\""));
	}

	@Test
	public void completionOnAttributeValue() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ //
				"	<bean autowire=\"|\"/>";
		testCompletionFor(xml, c("byName", "byName"), c("byType", "byType"), c("constructor", "constructor"));
	}

	@Test
	public void completionOnAttributeValue2() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoice xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ " xsi:schemaLocation=\"http://invoice xsd/invoice.xsd \">\r\n" + //
				"  <payments>\r\n" + //
				"    <payment method=\"|\"/>\r\n" + //
				"  </payments>";
		XMLAssert.testCompletionFor(xml, null, "src/test/resources/invoice.xml", null, c("credit", "credit"),
				c("debit", "debit"), c("cash", "cash"));
	}

	@Test
	public void schemaLocationWithElementAndAttributeCompletion() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoice xmlns=\"http://simpleAttribute\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ " xsi:schemaLocation=\"http://simpleAttribute xsd/simpleAttribute.xsd \">\r\n" + //
				"  <pro|</invoice>";
		XMLAssert.testCompletionFor(xml, null, "src/test/resources/simpleAttribute.xml", null,
				c("product", "<product description=\"\" />"));
	}

	@Test
	public void completionWithoutStartBracket() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ //
				"	|";
		testCompletionFor(xml, c("bean", "<bean></bean>"));

		xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ //
				"	|" + "</beans>";
		testCompletionFor(xml, c("bean", "<bean></bean>"));

		xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ //
				"	<bean />|" + "</beans>";
		testCompletionFor(xml, c("bean", "<bean></bean>"));

		xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ //
				"	<bean />|";
		testCompletionFor(xml, c("bean", "<bean></bean>"));

		xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ //
				"	<bean ></bean>|" + "</beans>";
		testCompletionFor(xml, c("bean", "<bean></bean>"));

		xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ //
				"	<bean ></bean>|";
		testCompletionFor(xml, c("bean", "<bean></bean>"));

		xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ //
				"	<bean>|</bean>";
		testCompletionFor(xml, c("constructor-arg", "<constructor-arg></constructor-arg>"));

		xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ //
				"	<bean>\r\n   |      \r\n</bean>";
		testCompletionFor(xml, c("constructor-arg", "<constructor-arg></constructor-arg>"));

	}

	@Test
	public void completionWithXMLSchemaContentChanged() throws Exception {
		// This https://github.com/angelozerr/lsp4xml/issues/194 for the test scenario
		Path dir = Paths.get("target/xsd/");
		if (!Files.isDirectory(dir)) {
			Files.createDirectory(dir);
		}
		Files.deleteIfExists(Paths.get(dir.toString(), "resources.xsd"));

		XMLLanguageService xmlLanguageService = new XMLLanguageService();

		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				+ "<resources | xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " //
				+ "xsi:noNamespaceSchemaLocation=\"xsd/resources.xsd\">\r\n" + //
				"    <resource name=\"res00\" >\r\n" + //
				"        <property name=\"propA\" value=\"...\" />\r\n" + //
				"        <property name=\"propB\" value=\"...\" />\r\n" + //
				"    </resource>\r\n" + //
				"    <resource name=\"\" >\r\n" + //
				"        <property name=\"\" value=\"...\" />\r\n" + //
				"        <property name=\"\" value=\"...\" />\r\n" + //
				"    </resource> \r\n" + "</resources>";

		// Schema defines variant attribute -> completion for @variant
		String schema = "<?xml version=\"1.0\"?>\r\n" + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\r\n"
				+ "\r\n" + "    <xs:complexType name=\"property\">\r\n"
				+ "        <xs:attribute name=\"name\" type=\"xs:string\" />\r\n"
				+ "        <xs:attribute name=\"value\" type=\"xs:string\" />\r\n" + "    </xs:complexType>\r\n"
				+ "\r\n" + "    <xs:complexType name=\"resource\">\r\n" + "        <xs:sequence>\r\n"
				+ "            <xs:element name=\"property\" type=\"property\" minOccurs=\"0\" maxOccurs=\"unbounded\" />\r\n"
				+ "        </xs:sequence>\r\n"
				+ "        <xs:attribute name=\"name\" type=\"xs:string\" use=\"required\" />\r\n"
				+ "    </xs:complexType>\r\n" + "\r\n" + "    <xs:element name=\"resources\">\r\n"
				+ "        <xs:complexType>\r\n" + "            <xs:sequence>\r\n"
				+ "                <xs:element name=\"resource\" type=\"resource\" minOccurs=\"0\" maxOccurs=\"unbounded\" />\r\n"
				+ "            </xs:sequence>\r\n"
				+ "            <xs:attribute name=\"variant\" type=\"xs:string\" use=\"required\"/>\r\n"
				+ "        </xs:complexType>\r\n" + "    </xs:element>\r\n" + "</xs:schema>";
		Files.write(Paths.get("target/xsd/resources.xsd"), schema.getBytes());
		XMLAssert.testCompletionFor(xmlLanguageService, xml, null, null, "target/resources.xml", 1, false,
				c("variant", "variant=\"\""));

		// Update resources.xsd, Schema doesn't define variant attribute -> no
		// completion
		schema = "<?xml version=\"1.0\"?>\r\n" + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\r\n"
				+ "\r\n" + "    <xs:complexType name=\"property\">\r\n"
				+ "        <xs:attribute name=\"name\" type=\"xs:string\" />\r\n"
				+ "        <xs:attribute name=\"value\" type=\"xs:string\" />\r\n" + "    </xs:complexType>\r\n"
				+ "\r\n" + "    <xs:complexType name=\"resource\">\r\n" + "        <xs:sequence>\r\n"
				+ "            <xs:element name=\"property\" type=\"property\" minOccurs=\"0\" maxOccurs=\"unbounded\" />\r\n"
				+ "        </xs:sequence>\r\n"
				+ "        <xs:attribute name=\"name\" type=\"xs:string\" use=\"required\" />\r\n"
				+ "    </xs:complexType>\r\n" + "\r\n" + "    <xs:element name=\"resources\">\r\n"
				+ "        <xs:complexType>\r\n" + "            <xs:sequence>\r\n"
				+ "                <xs:element name=\"resource\" type=\"resource\" minOccurs=\"0\" maxOccurs=\"unbounded\" />\r\n"
				+ "            </xs:sequence>\r\n"
				// + " <xs:attribute name=\"variant\" type=\"xs:string\" use=\"required\"/>\r\n"
				+ "        </xs:complexType>\r\n" + "    </xs:element>\r\n" + "</xs:schema>";
		Files.write(Paths.get("target/xsd/resources.xsd"), schema.getBytes());
		XMLAssert.testCompletionFor(xmlLanguageService, xml, null, null, "target/resources.xml", 0, false);

	}

	private void testCompletionFor(String xml, CompletionItem... expectedItems) throws BadLocationException {
		XMLAssert.testCompletionFor(xml, "src/test/resources/catalogs/catalog.xml", expectedItems);
	}
}
