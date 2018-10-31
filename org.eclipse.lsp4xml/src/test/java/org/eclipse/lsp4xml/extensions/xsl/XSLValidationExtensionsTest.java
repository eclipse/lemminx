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
package org.eclipse.lsp4xml.extensions.xsl;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.XMLSchemaErrorCode;
import org.junit.Test;

import static org.eclipse.lsp4xml.XMLAssert.d;

/**
 * XSL completion tests which test the {@link XSLURIResolverExtension}.
 *
 */
public class XSLValidationExtensionsTest {

	@Test
	public void xslValid() throws BadLocationException {
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\r\n" + //
				"</xsl:stylesheet>";
		testDiagnosticsFor(xml);
	}

	@Test
	public void xslInvalid() throws BadLocationException {
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\r\n" + //
				"<xsl:bad-element />\r\n" + //
				"</xsl:stylesheet>";
		testDiagnosticsFor(xml, d(2, 1, 2, 16, XMLSchemaErrorCode.cvc_complex_type_2_4_a));
	}

	private void testDiagnosticsFor(String xml, Diagnostic... expected) throws BadLocationException {
		XMLAssert.testDiagnosticsFor(xml, expected);
	}
}
