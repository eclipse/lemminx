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
package org.eclipse.lemminx.extensions.contentmodel.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.lemminx.MockXMLLanguageServer;
import org.junit.jupiter.api.Test;

/**
 * Test for checking if a file URI matches a given file pattern using an XML command.
 */
public class CheckFilePatternCommandTest {

	@Test
	public void checkPatternMatchSucceeds() throws Exception {
		MockXMLLanguageServer languageServer = new MockXMLLanguageServer();

		String xmlPath = "file:/path/to/src/test/resources/tag.xml";
		String [] patterns = new String [] { "**/*.xml",  "**/tag.xml", "tag.xml", "**/test/*/*.xml"};

		for (String pattern : patterns) {
			Boolean actual = (Boolean) languageServer.executeCommand(CheckFilePatternCommand.COMMAND_ID, pattern, xmlPath).get();
			assertNotNull(actual);
			assertEquals(true, actual);
		}
	}

	@Test
	public void checkPatternMatchFails() throws Exception {
		MockXMLLanguageServer languageServer = new MockXMLLanguageServer();

		String xmlPath = "file:/path/to/src/test/resources/tag.xml";
		String [] patterns = new String [] { "**/res/tag.xml",  "**/element.xml", "**/tag.html"};

		for (String pattern : patterns) {
			Boolean actual = (Boolean) languageServer.executeCommand(CheckFilePatternCommand.COMMAND_ID, pattern, xmlPath).get();
			assertNotNull(actual);
			assertEquals(false, actual);
		}
	}
}
