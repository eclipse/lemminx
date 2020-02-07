/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.utils;

import static org.junit.Assert.assertEquals;

import org.eclipse.lemminx.utils.CompletionSortTextHelper;
import org.eclipse.lsp4j.CompletionItemKind;
import org.junit.Test;

/**
 * CompletionSortTextHelperTest
 */
public class CompletionSortTextHelperTest {

	@Test
	public void testCompletionSortTextHelperProperty() {
		CompletionSortTextHelper sort = new CompletionSortTextHelper(CompletionItemKind.Property);
		assertEquals("aa1", sort.next());
		assertEquals("aa2", sort.next());
		assertEquals("aa3", sort.next());
	}

	@Test
	public void testCompletionSortTextHelperFile() {
		CompletionSortTextHelper sort = new CompletionSortTextHelper(CompletionItemKind.File);
		assertEquals("ab1", sort.next());
		assertEquals("ab2", sort.next());
		assertEquals("ab3", sort.next());
	}
}