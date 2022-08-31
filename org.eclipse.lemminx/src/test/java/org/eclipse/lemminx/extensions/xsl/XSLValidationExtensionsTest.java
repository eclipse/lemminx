/**
 *  Copyright (c) 2018 Angelo ZERR.
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
package org.eclipse.lemminx.extensions.xsl;

import static org.eclipse.lemminx.XMLAssert.d;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.contentmodel.participants.XMLSchemaErrorCode;
import org.eclipse.lsp4j.Diagnostic;
import org.junit.jupiter.api.Test;

/**
 * XSL completion tests which test the {@link XSLURIResolverExtension}.
 *
 */
public class XSLValidationExtensionsTest extends AbstractCacheBasedTest {

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
