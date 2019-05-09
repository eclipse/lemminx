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
		String xml = 
				"<?xml version=\"1.1\"?>\r\n" + 
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">   \r\n" + //
				"|";
		testCompletionFor(xml, c("xs:annotation", te(2, 0, 2, 0, "<xs:annotation></xs:annotation>"), "xs:annotation"),
				c("xs:attribute", te(2, 0, 2, 0, "<xs:attribute name=\"\"></xs:attribute>"), "xs:attribute"),
				c("xs:complexType", te(2, 0, 2, 0, "<xs:complexType name=\"\"></xs:complexType>"), "xs:complexType"));
	}

	@Test
	public void completionAlreadyComplexType() throws BadLocationException {
		// completion on |
		String xml = 
				"<?xml version=\"1.1\"?>\r\n" + 
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">   \r\n" + //
				"  <xs:complexType></xs:complexType>\r\n" +
				"  |";
		testCompletionFor(xml, c("xs:annotation", te(2, 0, 2, 0, "<xs:annotation></xs:annotation>"), "xs:annotation"),
				c("xs:attribute", te(2, 0, 2, 0, "<xs:attribute name=\"\"></xs:attribute>"), "xs:attribute"),
				c("xs:complexType", te(2, 0, 2, 0, "<xs:complexType name=\"\"></xs:complexType>"), "xs:complexType"));
	}

	private void testCompletionFor(String xml, CompletionItem... expectedItems) throws BadLocationException {
		XMLAssert.testCompletionFor(xml, null, expectedItems);
	}
}
