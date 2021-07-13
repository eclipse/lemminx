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

import java.io.File;
import java.util.concurrent.ExecutionException;

import org.eclipse.lemminx.MockXMLLanguageServer;

import org.eclipse.lemminx.utils.platform.Platform;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.junit.jupiter.api.Test;

/**
 * Test for checking if a document contains an associated grammar/schema using a XML command.
 *
 * @author Alexander Chen
 */
public class CheckBoundGrammarCommandTest {

	@Test
	public void checkDocumentWithoutBoundGrammar() throws Exception {
		MockXMLLanguageServer languageServer = new MockXMLLanguageServer();

		String xml = "<foo/>";
		String xmlPath = getFileURI("src/test/resources/tag.xml");
		TextDocumentIdentifier xmlIdentifier = languageServer.didOpen(xmlPath, xml);
		String xsdPath = getFileURI("src/test/resources/xsd/tag.xsd");

		Boolean actual = (Boolean) languageServer.executeCommand(CheckBoundGrammarCommand.COMMAND_ID, xmlIdentifier, xsdPath).get();
		assertNotNull(actual);
		assertEquals(true, actual);
	}

	@Test
	public void checkDocumentWithXSIDocTypeBoundGrammar() throws Exception {
		MockXMLLanguageServer languageServer = new MockXMLLanguageServer();

		String xml = "<foo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"tag.xsd\"/>";
		String xmlPath = getFileURI("src/test/resources/tag.xml");
		TextDocumentIdentifier xmlIdentifier = languageServer.didOpen(xmlPath, xml);
		String xsdPath = getFileURI("src/test/resources/xsd/tag.xsd");

		Boolean actual = (Boolean) languageServer.executeCommand(CheckBoundGrammarCommand.COMMAND_ID, xmlIdentifier, xsdPath).get();
		assertNotNull(actual);
		assertEquals(false, actual);
	}

	@Test
	public void checkDocumentWithXMLModelBoundGrammar() throws Exception {
		MockXMLLanguageServer languageServer = new MockXMLLanguageServer();

		String xml = "<?xml-model href=\"tag1.xsd\"?>\r\n" +
						"<foo/>";
		String xmlPath = getFileURI("src/test/resources/tag.xml");
		TextDocumentIdentifier xmlIdentifier = languageServer.didOpen(xmlPath, xml);
		String xsdPath = getFileURI("src/test/resources/xsd/tag.xsd");

		Boolean actual = (Boolean) languageServer.executeCommand(CheckBoundGrammarCommand.COMMAND_ID, xmlIdentifier, xsdPath).get();
		assertNotNull(actual);
		assertEquals(false, actual);
	}

	private static String getFileURI(String fileName) {
		String uri = new File(fileName).toURI().toString();
		if (Platform.isWindows && !uri.startsWith("file://")) {
			uri = uri.replace("file:/", "file:///");
		}
		return uri;
	}
}
