/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.xsd;

import static org.eclipse.lsp4xml.XMLAssert.c;
import static org.eclipse.lsp4xml.XMLAssert.te;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.junit.Test;

/**
 * XSD completion tests which test the {@link XDLURIResolverExtension}.
 *
 */
public class XSDCompletionExtensionsTest {

	@Test
	public void completion() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=\"1.1\"?>\r\n"
				+ "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">   \r\n"
				+ //
				"|";
		testCompletionFor(xml, c("xs:annotation", te(2, 0, 2, 0, "<xs:annotation></xs:annotation>"), "xs:annotation"),
				c("xs:attribute", te(2, 0, 2, 0, "<xs:attribute name=\"\"></xs:attribute>"), "xs:attribute"));
	}

	@Test
	public void completionWithSourceDetail() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=\"1.1\"?>\r\n"
				+ "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">   \r\n"
				+ //
				"|";
		testCompletionFor(xml,
				c("xs:annotation", te(2, 0, 2, 0, "<xs:annotation></xs:annotation>"), "xs:annotation",
						"Source: XMLSchema.xsd"),
				c("xs:attribute", te(2, 0, 2, 0, "<xs:attribute name=\"\"></xs:attribute>"), "xs:attribute",
						"Source: XMLSchema.xsd"));
	}

	@Test
	public void completionWithSourceDescriptionAndDetail() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoice xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ " xsi:schemaLocation=\"http://invoice xsd/invoice.xsd \">\r\n" + //
				"  <|";
		String lineSeparator = System.getProperty("line.separator");
		XMLAssert.testCompletionFor(xml, null, "src/test/resources/invoice.xml", null,
				c("date", te(3, 2, 3, 3, "<date></date>"), "<date",
						"Date Description" + lineSeparator + lineSeparator + "Source: invoice.xsd"));
	}

	@Test
	public void completionOnElementType() throws BadLocationException {
		// completion on | xs:element/@type -> xs:complexType/@name, xs:simpleType/@name
		String xml = "<?xml version=\"1.1\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://www.w3.org/2001/XMLSchema\">\r\n"
				+ //
				"	<xs:element name=\"elt\" type=\"|\" />\r\n" + //
				"	<xs:complexType name=\"aComplexType\" />\r\n" + //
				"	<xs:simpleType name=\"aSimpleType\" />\r\n" + //
				"</xs:schema>";
		testCompletionFor(xml, c("xs:aComplexType", te(2, 30, 2, 30, "xs:aComplexType"), "xs:aComplexType"),
				c("xs:aSimpleType", te(2, 30, 2, 30, "xs:aSimpleType"), "xs:aSimpleType"),
				c("xs:string", te(2, 30, 2, 30, "xs:string"), "xs:string"));
	}

	@Test
	public void completionOnAttributeType() throws BadLocationException {
		// completion on | xs:attribute/@type -> xs:simpleType/@name
		String xml = "<?xml version=\"1.1\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://www.w3.org/2001/XMLSchema\">\r\n"
				+ //
				"	<xs:attribute name=\"attr\" type=\"|\" />\r\n" + //
				"	<xs:complexType name=\"aComplexType\" />\r\n" + //
				"	<xs:simpleType name=\"aSimpleType\" />\r\n" + //
				"</xs:schema>";
		testCompletionFor(xml, c("xs:aSimpleType", te(2, 33, 2, 33, "xs:aSimpleType"), "xs:aSimpleType"),
				c("xs:string", te(2, 33, 2, 33, "xs:string"), "xs:string"));
	}

	@Test
	public void completionOnElementRef() throws BadLocationException {
		// completion on | xs:element/@ref -> xs:element/@name
		String xml = "<?xml version=\"1.1\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://www.w3.org/2001/XMLSchema\" version=\"1.0\">\r\n"
				+ //
				"	\r\n" + //
				"	<xs:group name=\"schemaTop\">\r\n" + //
				"		<xs:choice>\r\n" + //
				"			<xs:group ref=\"xs:redefinable\" />\r\n" + //
				"			<xs:element ref=\"xs:element\" />\r\n" + //
				"			<xs:element ref=\"xs:attribute\" />\r\n" + //
				"			<xs:element ref=\"|\" />\r\n" + // here completion shows xs:element, xs:attribute,
															// xs:notation
				"		</xs:choice>\r\n" + //
				"	</xs:group>\r\n" + //
				"	\r\n" + //
				"	<xs:group name=\"redefinable\">\r\n" + //
				"		<xs:choice></xs:choice>\r\n" + //
				"	</xs:group>\r\n" + //
				"	\r\n" + //
				"	<xs:element name=\"element\" />\r\n" + //
				"	<xs:element name=\"attribute\" />\r\n" + //
				"	<xs:element name=\"notation\" />\r\n" + //
				"</xs:schema>";
		XMLAssert.testCompletionFor(xml, 3, c("xs:element", te(8, 20, 8, 20, "xs:element"), "xs:element"),
				c("xs:attribute", te(8, 20, 8, 20, "xs:attribute"), "xs:attribute"),
				c("xs:notation", te(8, 20, 8, 20, "xs:notation"), "xs:notation"));
	}

	@Test
	public void completionOnGroupRef() throws BadLocationException {
		// completion on | xs:groupt/@ref -> xs:group/@name
		String xml = "<?xml version=\"1.1\" ?>\r\n" + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://www.w3.org/2001/XMLSchema\" version=\"1.0\">\r\n"
				+ //
				"	\r\n" + //
				"	<xs:group name=\"schemaTop\">\r\n" + //
				"		<xs:choice>\r\n" + //
				"			<xs:group ref=\"|\" />\r\n" + // here completion shows xs:schemaTop, xs:redefinable
				"			<xs:element ref=\"xs:element\" />\r\n" + //
				"			<xs:element ref=\"xs:attribute\" />\r\n" + //
				"			<xs:element ref=\"xs:notation\" />\r\n" + //
				"		</xs:choice>\r\n" + //
				"	</xs:group>\r\n" + //
				"	\r\n" + //
				"	<xs:group name=\"redefinable\">\r\n" + //
				"		<xs:choice></xs:choice>\r\n" + //
				"	</xs:group>\r\n" + //
				"	\r\n" + //
				"	<xs:element name=\"element\" />\r\n" + //
				"	<xs:element name=\"attribute\" />\r\n" + //
				"	<xs:element name=\"notation\" />\r\n" + //
				"</xs:schema>";
		XMLAssert.testCompletionFor(xml, 2, c("xs:redefinable", te(5, 18, 5, 18, "xs:redefinable"), "xs:redefinable"),
				c("xs:schemaTop", te(5, 18, 5, 18, "xs:schemaTop"), "xs:schemaTop"));
	}

	private void testCompletionFor(String xml, CompletionItem... expectedItems) throws BadLocationException {
		XMLAssert.testCompletionFor(xml, null, expectedItems);
	}
}
