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
package org.eclipse.lsp4xml.commons;

import org.eclipse.lsp4j.Position;
import org.junit.Assert;
import org.junit.Test;

/**
 * TextDocument tests
 *
 */
public class TextDocumentTest {

	@Test
	public void testEmptyDocument() throws BadLocationException {
		TextDocument document = new TextDocument("", null);

		Position position = document.positionAt(0);
		Assert.assertEquals(0, position.getLine());
		Assert.assertEquals(0, position.getCharacter());

		position = new Position(0, 0);
		int offset = document.offsetAt(position);
		Assert.assertEquals(0, offset);

	}

	@Test
	public void testPositionAt() throws BadLocationException {
		TextDocument document = new TextDocument("abcd\nefgh", null);

		Position position = document.positionAt(0);
		Assert.assertEquals(0, position.getLine());
		Assert.assertEquals(0, position.getCharacter());

		position = document.positionAt(4);
		Assert.assertEquals(0, position.getLine());
		Assert.assertEquals(4, position.getCharacter());

		position = document.positionAt(5);
		Assert.assertEquals(1, position.getLine());
		Assert.assertEquals(0, position.getCharacter());

		position = document.positionAt(9);
		Assert.assertEquals(1, position.getLine());
		Assert.assertEquals(4, position.getCharacter());

		BadLocationException ex = null;
		try {
			position = document.positionAt(10);
		} catch (BadLocationException e) {
			ex = e;
		}
		Assert.assertNotNull(ex);
	}

	@Test
	public void testPositionAtEndLine() throws BadLocationException {
		TextDocument document = new TextDocument("abcd\n", null);

		Position position = document.positionAt(4);
		Assert.assertEquals(0, position.getLine());
		Assert.assertEquals(4, position.getCharacter());

		position = document.positionAt(5);
		Assert.assertEquals(1, position.getLine());
		Assert.assertEquals(0, position.getCharacter());

		BadLocationException ex = null;
		try {
			position = document.positionAt(6);
		} catch (BadLocationException e) {
			ex = e;
		}
		Assert.assertNotNull(ex);

		document = new TextDocument("abcd\nefgh\n", null);

		position = document.positionAt(9);
		Assert.assertEquals(1, position.getLine());
		Assert.assertEquals(4, position.getCharacter());

		position = document.positionAt(10);
		Assert.assertEquals(2, position.getLine());
		Assert.assertEquals(0, position.getCharacter());

		ex = null;
		try {
			position = document.positionAt(11);
		} catch (BadLocationException e) {
			ex = e;
		}
		Assert.assertNotNull(ex);
	}

	@Test
	public void testOffsetAt() throws BadLocationException {
		TextDocument document = new TextDocument("abcd\nefgh", null);

		Position position = new Position(0, 0);
		int offset = document.offsetAt(position);
		Assert.assertEquals(0, offset);

		position = new Position(0, 4);
		offset = document.offsetAt(position);
		Assert.assertEquals(4, offset);

		position = new Position(1, 0);
		offset = document.offsetAt(position);
		Assert.assertEquals(5, offset);

		position = new Position(1, 4);
		offset = document.offsetAt(position);
		Assert.assertEquals(9, offset);

		BadLocationException ex = null;
		try {
			position = new Position(1, 5);
			document.offsetAt(position);
		} catch (BadLocationException e) {
			ex = e;
		}
		Assert.assertNotNull(ex);
	}

}
