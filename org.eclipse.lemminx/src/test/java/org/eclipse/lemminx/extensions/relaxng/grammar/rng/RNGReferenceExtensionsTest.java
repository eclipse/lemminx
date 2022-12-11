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
package org.eclipse.lemminx.extensions.relaxng.grammar.rng;

import static org.eclipse.lemminx.XMLAssert.l;
import static org.eclipse.lemminx.XMLAssert.r;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lsp4j.Location;
import org.junit.jupiter.api.Test;

/**
 * RNG references tests
 *
 */
public class RNGReferenceExtensionsTest extends AbstractCacheBasedTest {

	@Test
	public void referenceOnDefineName() throws BadLocationException {
		// rename on define/@name
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<grammar xmlns=\"http://relaxng.org/ns/structure/1.0\">\r\n" + //
				"    <start>\r\n" + //
				"        <element name=\"addressBook\">\r\n" + //
				"            <zeroOrMore>\r\n" + //
				"                <element name=\"card\">\r\n" + //
				"                    <ref name=\"cardContent\"/>\r\n" + //
				"                </element>\r\n" + //
				"            </zeroOrMore>\r\n" + //
				"        </element>\r\n" + //
				"    </start>\r\n" + //
				"    <define name=\"cardCo|ntent\">\r\n" + // references here should should report
				// cardContent (ref/@name) reference
				"        <element name=\"name\">\r\n" + //
				"            <text/>\r\n" + //
				"        </element>\r\n" + //
				"        <element name=\"email\">\r\n" + //
				"            <text/>\r\n" + //
				"        </element>\r\n" + //
				"    </define>\r\n" + //
				"</grammar>";
		testReferencesFor(xml, l("test.rng", r(6, 30, 6, 43)));
	}

	private void testReferencesFor(String xml, Location... expectedItems) throws BadLocationException {
		XMLAssert.testReferencesFor(xml, "test.rng", expectedItems);
	}
}
