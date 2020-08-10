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
import static org.eclipse.lemminx.XMLAssert.testDiagnosticsFor;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.extensions.relaxng.xml.validator.RelaxNGErrorCode;
import org.junit.jupiter.api.Test;

/**
 * XML Validation tests with RelaxNG by using XML catalog .
 *
 */
public class RelaxNGDiagnosticsWithCatalogTest extends AbstractCacheBasedTest {

	// With XML catalog

	@Test
	public void incomplete_element_required_element_missing() throws Exception {
		String xml = "<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">\r\n" + // <-- "element "TEI" incomplete; missing
																			// required element "teiHeader""
				"\r\n" + //
				"</TEI>";
		testDiagnosticsFor(xml, "src/test/resources/relaxng/catalog-relaxng.xml", //
				d(0, 1, 4, RelaxNGErrorCode.incomplete_element_required_element_missing));
	}

}
