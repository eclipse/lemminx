/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services;

import static org.eclipse.lemminx.XMLAssert.le;
import static org.eclipse.lemminx.XMLAssert.r;
import static org.eclipse.lemminx.XMLAssert.testLinkedEditingFor;
import static org.eclipse.lemminx.services.XMLLinkedEditing.XML_ELEMENT_WORD_PATTERN;
import org.eclipse.lemminx.commons.BadLocationException;
import org.junit.jupiter.api.Test;

/**
 * Test for XML linked editing.
 * 
 * @author Angelo ZERR
 *
 */
public class XMLLinkedEditingTest {

	@Test
	public void linkedEditingWithOrpheanEndTag() throws BadLocationException {
		testLinkedEditingFor("<div></|", null);
		testLinkedEditingFor("<di|v></", null);
		testLinkedEditingFor("<|></", null);
	}

	@Test
	public void linkedEditing() throws BadLocationException {
		testLinkedEditingFor("|<div></div>", null);
		testLinkedEditingFor("<|div></div>", le(XML_ELEMENT_WORD_PATTERN,r(0, 1, 0, 4), r(0, 7, 0, 10)));
		testLinkedEditingFor("<d|iv></div>", le(XML_ELEMENT_WORD_PATTERN,r(0, 1, 0, 4), r(0, 7, 0, 10)));
		testLinkedEditingFor("<di|v></div>", le(XML_ELEMENT_WORD_PATTERN,r(0, 1, 0, 4), r(0, 7, 0, 10)));
		testLinkedEditingFor("<div|></div>", le(XML_ELEMENT_WORD_PATTERN,r(0, 1, 0, 4), r(0, 7, 0, 10)));

		testLinkedEditingFor("<div>|</div>", null);
		testLinkedEditingFor("<div><|/div>", null);

		testLinkedEditingFor("<div></|div>", le(XML_ELEMENT_WORD_PATTERN,r(0, 1, 0, 4), r(0, 7, 0, 10)));
		testLinkedEditingFor("<div></d|iv>", le(XML_ELEMENT_WORD_PATTERN,r(0, 1, 0, 4), r(0, 7, 0, 10)));
		testLinkedEditingFor("<div></di|v>", le(XML_ELEMENT_WORD_PATTERN,r(0, 1, 0, 4), r(0, 7, 0, 10)));
		testLinkedEditingFor("<div></div|>", le(XML_ELEMENT_WORD_PATTERN,r(0, 1, 0, 4), r(0, 7, 0, 10)));

		testLinkedEditingFor("<div></div>|", null);
		testLinkedEditingFor("<div><div|</div>", le(XML_ELEMENT_WORD_PATTERN,r(0, 6, 0, 9), r(0, 11, 0, 14)));
		testLinkedEditingFor("<div><div><div|</div></div>", le(XML_ELEMENT_WORD_PATTERN,r(0, 11, 0, 14), r(0, 16, 0, 19)));

		testLinkedEditingFor("<div| ></div>", le(XML_ELEMENT_WORD_PATTERN,r(0, 1, 0, 4), r(0, 8, 0, 11)));
		testLinkedEditingFor("<div| id='foo'></div>", le(XML_ELEMENT_WORD_PATTERN,r(0, 1, 0, 4), r(0, 16, 0, 19)));
	}
}
