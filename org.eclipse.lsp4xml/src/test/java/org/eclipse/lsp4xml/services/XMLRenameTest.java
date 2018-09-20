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
package org.eclipse.lsp4xml.services;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.dom.XMLParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XML rename services tests
 *
 */
public class XMLRenameTest {

	private XMLLanguageService languageService;

	@Before
	public void initializeLanguageService() {
		languageService = new XMLLanguageService();
	}

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
	public void caseInsensivity() throws BadLocationException {
		assertRename("<HTML><diV><Div></dIV></dI|v></html>", "newText", edits("newText", r(0, 7, 10), r(0, 24, 27)));
		assertRename("<HTML><diV|><Div></dIV></dIv></html>", "newText", edits("newText", r(0, 7, 10), r(0, 24, 27)));
	}

	@Test
	public void insideEndTag() throws BadLocationException {
		assertRename("<html|></meta></html>", "newText", edits("newText", r(0, 1, 5), r(0, 15, 19)));
	}

	private void assertRename(String value, String newText) throws BadLocationException {
		assertRename(value, newText, Collections.emptyList());
	}

	private void assertRename(String value, String newText, List<TextEdit> expectedEdits) throws BadLocationException {
		int offset = value.indexOf("|");
		value = value.substring(0, offset) + value.substring(offset + 1);

		XMLDocument document = XMLParser.getInstance().parse(value, "test://test/test.html");

		Position position = document.positionAt(offset);

		WorkspaceEdit workspaceEdit = languageService.doRename(document, position, newText);
		List<TextEdit> actualEdits = workspaceEdit.getChanges().get("test://test/test.html");
		Assert.assertArrayEquals(expectedEdits.toArray(), actualEdits.toArray());
	}

	private static Range r(int line, int startCharacter, int endCharacter) {
		return new Range(new Position(line, startCharacter), new Position(line, endCharacter));
	}

	private static List<TextEdit> edits(String newText, Range... ranges) {
		return Stream.of(ranges).map(r -> new TextEdit(r, newText)).collect(Collectors.toList());
	}

}
