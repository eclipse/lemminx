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

import static org.eclipse.lemminx.XMLAssert.hl;
import static org.eclipse.lemminx.XMLAssert.r;
import static org.eclipse.lemminx.XMLAssert.testHighlightsFor;
import static org.eclipse.lsp4j.DocumentHighlightKind.Read;
import static org.eclipse.lsp4j.DocumentHighlightKind.Write;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.commons.BadLocationException;
import org.junit.jupiter.api.Test;

/**
 * RNG Highlighting tests.
 *
 * @author Angelo ZERR
 */
public class RNGHighlightingExtensionsTest extends AbstractCacheBasedTest {

	@Test
	public void highlightingOnDefineName() throws BadLocationException {
		// highlighting on define/@name
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
				"    <define name=\"cardCo|ntent\">\r\n" + // highlighting here should should highlight
				// cardContent (ref/@name)
				"        <element name=\"name\">\r\n" + //
				"            <text/>\r\n" + //
				"        </element>\r\n" + //
				"        <element name=\"email\">\r\n" + //
				"            <text/>\r\n" + //
				"        </element>\r\n" + //
				"    </define>\r\n" + //
				"</grammar>";
		testHighlightsFor(xml, hl(r(11, 17, 11, 30), Write), hl(r(6, 30, 6, 43), Read));
	}

	@Test
	public void highlightingOnRefName() throws BadLocationException {
		// highlighting on ref/@name
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<grammar xmlns=\"http://relaxng.org/ns/structure/1.0\">\r\n" + //
				"    <start>\r\n" + //
				"        <element name=\"addressBook\">\r\n" + //
				"            <zeroOrMore>\r\n" + //
				"                <element name=\"card\">\r\n" + //
				"                    <ref name=\"cardCon|tent\"/>\r\n" + // highlighting here should should highlight
																			// cardContent (define/@name)
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
		testHighlightsFor(xml, hl(r(6, 30, 6, 43), Read), hl(r(11, 17, 11, 30), Write));
	}
}
