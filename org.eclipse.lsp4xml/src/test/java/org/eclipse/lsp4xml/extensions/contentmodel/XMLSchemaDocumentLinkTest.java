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

import static org.eclipse.lsp4xml.XMLAssert.dl;
import static org.eclipse.lsp4xml.XMLAssert.r;

import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.junit.Test;

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
}
