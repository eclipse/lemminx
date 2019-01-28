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

import static org.eclipse.lsp4xml.XMLAssert.d;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.XMLSchemaErrorCode;
import org.junit.Test;

/**
 * XSD diagnostics tests which test the {@link XSDURIResolverExtension}.
 *
 */
public class XSDValidationExtensionsTest {

	@Test
	public void xsdInvalid() throws BadLocationException {
		String xml = "<?xml version=\"1.1\"?>\r\n"
				+ "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">   \r\n"
				+ //
				"   <foo>bar</foo>\r\n" + // <- error foo doesn't exist
				"</xs:schema>";
		testDiagnosticsFor(xml, d(2, 4, 2, 7, XMLSchemaErrorCode.cvc_complex_type_2_4_a));
	}

	private void testDiagnosticsFor(String xml, Diagnostic... expected) throws BadLocationException {
		XMLAssert.testDiagnosticsFor(xml, expected);
	}
}
