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

import static org.eclipse.lemminx.XMLAssert.assertHighlights;

import org.eclipse.lemminx.commons.BadLocationException;
import org.junit.jupiter.api.Test;
/**
 * XML highlighting services tests
 *
 */
public class XMLHighlightingTest {

	@Test
	public void single() throws BadLocationException {
		assertHighlights("|<html></html>", new int[] {}, null);
		assertHighlights("<|html></html>", new int[] { 1, 8 }, "html");
		assertHighlights("<h|tml></html>", new int[] { 1, 8 }, "html");
		assertHighlights("<htm|l></html>", new int[] { 1, 8 }, "html");
		assertHighlights("<html|></html>", new int[] { 1, 8 }, "html");
		assertHighlights("<html>|</html>", new int[] {}, null);
		assertHighlights("<html><|/html>", new int[] {}, null);
		assertHighlights("<html></|html>", new int[] { 1, 8 }, "html");
		assertHighlights("<html></h|tml>", new int[] { 1, 8 }, "html");
		assertHighlights("<html></ht|ml>", new int[] { 1, 8 }, "html");
		assertHighlights("<html></htm|l>", new int[] { 1, 8 }, "html");
		assertHighlights("<html></html|>", new int[] { 1, 8 }, "html");
		assertHighlights("<html></html>|", new int[] {}, null);
	}

	@Test
	public void nested() throws BadLocationException {
		assertHighlights("<html>|<div></div></html>", new int[] {}, null);
		assertHighlights("<html><|div></div></html>", new int[] { 7, 13 }, "div");
		assertHighlights("<html><div>|</div></html>", new int[] {}, null);
		assertHighlights("<html><div></di|v></html>", new int[] { 7, 13 }, "div");
		assertHighlights("<html><div><div></div></di|v></html>", new int[] { 7, 24 }, "div");
		assertHighlights("<html><div><div></div|></div></html>", new int[] { 12, 18 }, "div");
		assertHighlights("<html><div><div|></div></div></html>", new int[] { 12, 18 }, "div");
		assertHighlights("<html><div><div></div></div></h|tml>", new int[] { 1, 30 }, "html");
		assertHighlights("<html><di|v></div><div></div></html>", new int[] { 7, 13 }, "div");
		assertHighlights("<html><div></div><div></d|iv></html>", new int[] { 18, 24 }, "div");
	}

	@Test
	public void selfclosed() throws BadLocationException {
		assertHighlights("<html><|div/></html>", new int[] { 7 }, "div");
		assertHighlights("<html><|br></html>", new int[] { 7 }, "br");
		assertHighlights("<html><div><d|iv/></div></html>", new int[] { 12 }, "div");
	}

	@Test
	public void caseSensitive() throws BadLocationException {
		assertHighlights("<HTML><diV><Div></dIV></dI|v></html>", new int[] { 24 }, "div");
		assertHighlights("<HTML><diV|><Div></dIV></dIv></html>", new int[] { 7 }, "div");
	}
	
	@Test
	public void insideEndTag() throws BadLocationException {		
		assertHighlights("<html|></meta></html>", new int[] { 1, 15 }, "html");
	}

}
