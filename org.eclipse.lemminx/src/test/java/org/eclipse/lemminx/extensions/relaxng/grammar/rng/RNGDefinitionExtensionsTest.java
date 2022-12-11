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

import static org.eclipse.lemminx.XMLAssert.ll;
import static org.eclipse.lemminx.XMLAssert.r;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lsp4j.LocationLink;
import org.junit.jupiter.api.Test;

/**
 * RNG definition tests.
 *
 * @author Angelo ZERR
 */
public class RNGDefinitionExtensionsTest extends AbstractCacheBasedTest {

	@Test
	public void definitionOnRefName() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<grammar xmlns=\"http://relaxng.org/ns/structure/1.0\">\r\n" + //
				"    <start>\r\n" + //
				"        <element name=\"addressBook\">\r\n" + //
				"            <zeroOrMore>\r\n" + //
				"                <element name=\"card\">\r\n" + //
				"                    <ref name=\"cardCon|tent\"/>\r\n" + // definition here should jump to cardContent
																			// (define/@name)
				"                </element>\r\n" + //
				"            </zeroOrMore>\r\n" + //
				"        </element>\r\n" + //
				"    </start>\r\n" + //
				"    <define name=\"cardContent\">\r\n" + //
				"        <element name=\"name\">\r\n" + //
				"            <text/>\r\n" + //
				"        </element>\r\n" + //
				"        <element name=\"email\">\r\n" + //
				"            <text/>\r\n" + //
				"        </element>\r\n" + //
				"    </define>\r\n" + //
				"</grammar>";
		testDefinitionFor(xml, ll("test.rng", r(6, 30, 6, 43), r(11, 17, 11, 30)));
	}

	@Test
	public void definitionOnRefNameWithNestedDefine() throws BadLocationException {
		String xml = "<grammar xmlns=\"http://relaxng.org/ns/structure/1.0\"\r\n"
				+ "  datatypeLibrary=\"http://www.w3.org/2001/XMLSchema-datatypes\">\r\n"
				+ "  <start>\r\n"
				+ "    <ref name=\"root-element\"/>\r\n"
				+ "  </start>\r\n"
				+ "  <define name=\"root-element\">\r\n"
				+ "      <element name=\"root-element\">\r\n"
				+ "        <ref name=\"as|df\"></ref>\r\n" // definition here should jump to asdf
				// (define/@name)
				+ "      </element>\r\n"
				+ "  </define>\r\n"
				+ "  <div>\r\n"
				+ "    <div>\r\n"
				+ "      <define name=\"asdf\">\r\n"
				+ "        <attribute name=\"child\"></attribute>\r\n"
				+ "      </define>\r\n"
				+ "    </div>\r\n"
				+ "  </div>\r\n"
				+ "</grammar>";
		testDefinitionFor(xml, ll("test.rng", r(7, 18, 7, 24), r(12, 19, 12, 25)));
	}

	private static void testDefinitionFor(String xml, LocationLink... expectedItems) throws BadLocationException {
		XMLAssert.testDefinitionFor(xml, "test.rng", expectedItems);
	}
}
