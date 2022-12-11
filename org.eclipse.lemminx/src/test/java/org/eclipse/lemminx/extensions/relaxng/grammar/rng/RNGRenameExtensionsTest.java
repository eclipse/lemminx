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

import static org.eclipse.lemminx.XMLAssert.assertRename;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.junit.jupiter.api.Test;

/**
 * RNG rename tests.
 *
 */
public class RNGRenameExtensionsTest extends AbstractCacheBasedTest {

	@Test
	public void renameOnDefineName() throws BadLocationException {
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
				"    <define name=\"cardCo|ntent\">\r\n" + // rename here should should rename
				// cardContent (ref/@name)
				"        <element name=\"name\">\r\n" + //
				"            <text/>\r\n" + //
				"        </element>\r\n" + //
				"        <element name=\"email\">\r\n" + //
				"            <text/>\r\n" + //
				"        </element>\r\n" + //
				"    </define>\r\n" + //
				"</grammar>";
		assertRename(xml, "card-content", edits("card-content", r(11, 18, 29), r(6, 31, 42)));
	}

	private static Range r(int line, int startCharacter, int endCharacter) {
		return new Range(new Position(line, startCharacter), new Position(line, endCharacter));
	}

	private static List<TextEdit> edits(String newText, Range... ranges) {
		return Stream.of(ranges) //
				.map(r -> new TextEdit(r, newText)) //
				.collect(Collectors.toList());
	}

}