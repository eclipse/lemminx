/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.relaxng.xml.diagnostics;

import static org.eclipse.lemminx.XMLAssert.d;

import java.net.URI;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.extensions.contentmodel.settings.ContentModelSettings;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationRootSettings;
import org.eclipse.lemminx.extensions.relaxng.xml.validator.RelaxNGErrorCode;
import org.eclipse.lsp4j.Diagnostic;
import org.junit.jupiter.api.Test;

/**
 * XML Validation tests with RelaxNG with XInclude .
 *
 */
public class RelaxNGDiagnosticsWithXIncludeTest extends AbstractCacheBasedTest {

	@Test
	public void xincludeInRNGEnabled() throws Exception {
		ContentModelSettings settings = new ContentModelSettings();
		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		validation.getXInclude().setEnabled(true);
		settings.setValidation(validation);

		String xml = "<?xml-model href=\"src/test/resources/relaxng/xinclude/foo.rng\"?>\r\n"
				+ "<foo>\r\n"
				+ "	<bar></bar>\r\n"
				+ "</foo>";
		testDiagnosticsFor(xml, settings);

		xml = "<?xml-model href=\"src/test/resources/relaxng/xinclude/foo.rng\"?>\r\n"
				+ "<foo>\r\n"
				+ "	<bara></bara>\r\n" // <-- error
				+ "</foo>";
		testDiagnosticsFor(xml, settings, //
				d(2, 2, 6, RelaxNGErrorCode.unknown_element), //
				d(1, 1, 4, RelaxNGErrorCode.incomplete_element_required_element_missing));
	}

	@Test
	public void xincludeInRNGDisabled() throws Exception {
		ContentModelSettings settings = new ContentModelSettings();
		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		validation.getXInclude().setEnabled(false);
		settings.setValidation(validation);

		String xml = "<?xml-model href=\"src/test/resources/relaxng/xinclude/foo.rng\"?>\r\n" // <-- error with foo.rng
																								// because xi:include is
																								// not processed and
																								// element can not be
																								// empty
				+ "<foo>\r\n"
				+ "	<bar></bar>\r\n"
				+ "</foo>";
		testDiagnosticsFor(xml, settings, //
				d(0, 17, 62, null));
	}

	private static void testDiagnosticsFor(String xml, ContentModelSettings settings,
			Diagnostic... expected) {
		XMLAssert.testDiagnosticsFor(xml, null, null, null, true, settings, expected);
	}
}
