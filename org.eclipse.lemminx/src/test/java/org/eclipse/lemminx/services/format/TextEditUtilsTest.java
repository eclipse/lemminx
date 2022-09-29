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
package org.eclipse.lemminx.services.format;

import static org.eclipse.lemminx.XMLAssert.te;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.commons.TextDocument;
import org.eclipse.lsp4j.TextEdit;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link TextEditUtils}.
 *
 * @author Angelo ZERR
 *
 */
public class TextEditUtilsTest extends AbstractCacheBasedTest {

	@Test
	public void textEdit() {
		TextDocument document = new TextDocument("<a/>", "test.xml");
		TextEdit edit = TextEditUtils.createTextEditIfNeeded(1, 2, " ", document);
		assertNotNull(edit);
		assertEquals(te(0, 1, 0, 2, " "), edit);
	}

	@Test
	public void noTextEdit() {
		TextDocument document = new TextDocument("<a />", "test.xml");
		TextEdit edit = TextEditUtils.createTextEditIfNeeded(2, 3, " ", document);
		assertNull(edit);
	}

	@Test
	public void textEdit2() {
		TextDocument document = new TextDocument("<a  />", "test.xml");
		TextEdit edit = TextEditUtils.createTextEditIfNeeded(1, 4, " ", document);
		assertNotNull(edit);
		assertEquals(te(0, 1, 0, 4, " "), edit);
	}

	@Test
	public void textEditQuote() {
		TextDocument document = new TextDocument("<a name=\'value \'> </a>", "test.xml");
		TextEdit edit = TextEditUtils.createTextEditIfNeeded(8, 9, "\"", document);
		assertNotNull(edit);
		assertEquals(te(0, 8, 0, 9, "\""), edit);
	}

}
