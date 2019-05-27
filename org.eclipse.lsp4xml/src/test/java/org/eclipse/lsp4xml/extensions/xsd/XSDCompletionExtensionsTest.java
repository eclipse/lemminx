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
		testCompletionFor(xml, c("xs:annotation", te(2, 0, 2, 0, "<xs:annotation></xs:annotation>"), "xs:annotation", "Source: XMLSchema.xsd"),
				c("xs:attribute", te(2, 0, 2, 0, "<xs:attribute name=\"\"></xs:attribute>"), "xs:attribute", "Source: XMLSchema.xsd"));
	}

	@Test
	public void completionWithSourceDescriptionAndDetail() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<invoice xmlns=\"http://invoice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ " xsi:schemaLocation=\"http://invoice xsd/invoice.xsd \">\r\n" + //
				"  <|";
		String lineSeparator = System.getProperty("line.separator");
		XMLAssert.testCompletionFor(xml, null, "src/test/resources/invoice.xml", null, c("date", te(3, 2, 3, 3, "<date></date>"), "<date", "Date Description" + lineSeparator + lineSeparator + "Source: invoice.xsd"),
				c("number", "<number></number>"));
	}

	private void testCompletionFor(String xml, CompletionItem... expectedItems) throws BadLocationException {
		XMLAssert.testCompletionFor(xml, null, expectedItems);
	}
}
