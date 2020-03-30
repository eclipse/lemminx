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
package org.eclipse.lemminx.services;

import static org.eclipse.lemminx.XMLAssert.assertRename;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.junit.jupiter.api.Test;

/**
 * XML rename services tests
 *
 */
public class XMLRenameTest {

	@Test
	public void single() throws BadLocationException {
		assertRename("|<html></html>", "newText");
		assertRename("<|html></html>", "newText", edits("newText", r(0, 1, 5), r(0, 8, 12)));
		assertRename("<h|tml></html>", "newText", edits("newText", r(0, 1, 5), r(0, 8, 12)));
		assertRename("<htm|l></html>", "newText", edits("newText", r(0, 1, 5), r(0, 8, 12)));
		assertRename("<html|></html>", "newText", edits("newText", r(0, 1, 5), r(0, 8, 12)));
		assertRename("<html>|</html>", "newText");
		assertRename("<html><|/html>", "newText");
		assertRename("<html></|html>", "newText", edits("newText", r(0, 1, 5), r(0, 8, 12)));
		assertRename("<html></h|tml>", "newText", edits("newText", r(0, 1, 5), r(0, 8, 12)));
		assertRename("<html></ht|ml>", "newText", edits("newText", r(0, 1, 5), r(0, 8, 12)));
		assertRename("<html></htm|l>", "newText", edits("newText", r(0, 1, 5), r(0, 8, 12)));
		assertRename("<html></html|>", "newText", edits("newText", r(0, 1, 5), r(0, 8, 12)));
		assertRename("<html></html>|", "newText");
	}

	@Test
	public void nested() throws BadLocationException {
		assertRename("<html>|<div></div></html>", "newText");
		assertRename("<html><|div></div></html>", "newText", edits("newText", r(0, 7, 10), r(0, 13, 16)));
		assertRename("<html><div>|</div></html>", "newText");
		assertRename("<html><div></di|v></html>", "newText", edits("newText", r(0, 7, 10), r(0, 13, 16)));
		assertRename("<html><div><div></div></di|v></html>", "newText", edits("newText", r(0, 7, 10), r(0, 24, 27)));
		assertRename("<html><div><div></div|></div></html>", "newText", edits("newText", r(0, 12, 15), r(0, 18, 21)));
		assertRename("<html><div><div|></div></div></html>", "newText", edits("newText", r(0, 12, 15), r(0, 18, 21)));
		assertRename("<html><div><div></div></div></h|tml>", "newText", edits("newText", r(0, 1, 5), r(0, 30, 34)));
		assertRename("<html><di|v></div><div></div></html>", "newText", edits("newText", r(0, 7, 10), r(0, 13, 16)));
		assertRename("<html><div></div><div></d|iv></html>", "newText", edits("newText", r(0, 18, 21), r(0, 24, 27)));
	}

	@Test
	public void selfclosed() throws BadLocationException {
		assertRename("<html><|div/></html>", "newText", edits("newText", r(0, 7, 10)));
		assertRename("<html><|br></html>", "newText", edits("newText", r(0, 7, 9)));
		assertRename("<html><div><d|iv/></div></html>", "newText", edits("newText", r(0, 12, 15)));
	}

	@Test
	public void caseSensitive() throws BadLocationException {
		assertRename("<HTML><diV><Div></dIV></dI|v></html>", "newText", edits("newText", r(0, 24, 27)));
		assertRename("<HTML><diV|><Div></dIV></dIv></html>", "newText", edits("newText", r(0, 7, 10)));
	}

	@Test
	public void insideEndTag() throws BadLocationException {
		assertRename("<html|></meta></html>", "newText", edits("newText", r(0, 1, 5), r(0, 15, 19)));
	}

	@Test
	public void testNamespaceRename() throws BadLocationException {
		String xml = 
			"<aa:a xmlns:a|a=\"aa.com\" xmlns:qq=\"qq.com\">\n" +
			"  <aa:b></aa:b>\n" +
			"  <aa:b></aa:b>\n" +
			"  <aa:c/>\n" +
			"  <t type=\"aa:b\"/>\n" +
			"  <qq:b></qq:b>\n" +
			"</aa:a>";
		assertRename(xml, "ns", edits("ns", r(0, 12, 14), r(0, 1, 3), r(6, 2, 4), //root attribute, start tag, end tag
																					r(1, 3, 5), r(1, 10, 12), 
																					r(2, 3, 5), r(2, 10, 12), 
																					r(3, 3, 5), //<aa:c
																					r(4, 11, 13))); // attribute value
	}

	@Test
	public void testNamespaceRenameEndTagPrefix() throws BadLocationException {
		String xml = 
			"<aa:a xmlns:aa=\"aa.com\" xmlns:qq=\"qq.com\">\n" +
			"  <aa:b></aa:b>\n" +
			"  <aa:b></a|a:b>\n" +
			"  <aa:c/>\n" +
			"  <t type=\"aa:b\"/>\n" +
			"  <qq:b></qq:b>\n" +
			"</aa:a>";
		assertRename(xml, "ns", edits("ns", r(2, 3, 5), r(2, 10, 12))); 
	}

	@Test
	public void testNamespaceRenameStartTagPrefix() throws BadLocationException {
		String xml = 
			"<aa:a xmlns:aa=\"aa.com\" xmlns:qq=\"qq.com\">\n" +
			"  <aa:b></aa:b>\n" +
			"  <a|a:b></aa:b>\n" +
			"  <aa:c/>\n" +
			"  <t type=\"aa:b\"/>\n" +
			"  <qq:b></qq:b>\n" +
			"</aa:a>";
		assertRename(xml, "ns", edits("ns", r(2, 3, 5), r(2, 10, 12))); 
	}

	@Test
	public void testNamespaceRenameEndTagSuffix() throws BadLocationException {
		String xml = 
			"<aa:a xmlns:aa=\"aa.com\" xmlns:qq=\"qq.com\">\n" +
			"  <aa:b></aa:b>\n" +
			"  <aa:|b></aa:b>\n" +
			"  <aa:c/>\n" +
			"  <t type=\"aa:b\"/>\n" +
			"  <qq:b></qq:b>\n" +
			"</aa:a>";
		assertRename(xml, "BB", edits("BB", r(2, 6, 7), r(2, 13, 14))); 
	}

	@Test
	public void testNamespaceRenameStartTagSuffix() throws BadLocationException {
		String xml = 
			"<aa:a xmlns:aa=\"aa.com\" xmlns:qq=\"qq.com\">\n" +
			"  <aa:b></aa:b>\n" +
			"  <aa:b></aa:b|>\n" +
			"  <aa:c/>\n" +
			"  <t type=\"aa:b\"/>\n" +
			"  <qq:b></qq:b>\n" +
			"</aa:a>";
			assertRename(xml, "BB", edits("BB", r(2, 6, 7), r(2, 13, 14))); 
	}

	@Test
	public void testTryToRenameXMLNS() throws BadLocationException {
		String xml = 
			"<aa:a xml|ns:aa=\"aa.com\" xmlns:qq=\"qq.com\">\n" +
			"  <aa:b></aa:b>\n" +
			"</aa:a>";
			assertRename(xml, "BBBB"); 
	}



	private static Range r(int line, int startCharacter, int endCharacter) {
		return new Range(new Position(line, startCharacter), new Position(line, endCharacter));
	}

	private static List<TextEdit> edits(String newText, Range... ranges) {
		return Stream.of(ranges).map(r -> new TextEdit(r, newText)).collect(Collectors.toList());
	}

}
