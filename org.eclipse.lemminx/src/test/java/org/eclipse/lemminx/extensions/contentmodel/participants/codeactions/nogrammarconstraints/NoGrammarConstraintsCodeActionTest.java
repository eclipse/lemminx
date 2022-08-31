/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.contentmodel.participants.codeactions.nogrammarconstraints;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.junit.jupiter.api.Test;

/**
 * Generate grammar URI tests.
 *
 */
public class NoGrammarConstraintsCodeActionTest extends AbstractCacheBasedTest {

	@Test
	public void generateGrammarURI() {
		String actual = NoGrammarConstraintsCodeAction.getGrammarURI("file:///C:/test.xml", "xsd");
		assertEquals("file:///C:/test.xsd", actual);
	}

	@Test
	public void generateGrammarURIWithDot() {
		String actual = NoGrammarConstraintsCodeAction.getGrammarURI("file:///C:/.project", "xsd");
		assertEquals("file:///C:/.project.xsd", actual);
	}

}
