/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.commons.TextDocument;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMParser;
import org.eclipse.lemminx.services.extensions.semantictokens.SemanticTokensData;
import org.eclipse.lsp4j.SemanticTokens;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the SemanticTokensData builder and XMLSemanticTokens service.
 */
public class XMLSemanticTokensTest extends AbstractCacheBasedTest {

	// ------------------- SemanticTokensData builder tests -------------------

	@Test
	public void emptyBuilder() {
		DOMDocument doc = parse("<root />");
		SemanticTokensData builder = new SemanticTokensData(doc);

		assertTrue(builder.isEmpty());
		SemanticTokens result = builder.build();
		assertNotNull(result);
		assertEquals(0, result.getData().size());
	}

	@Test
	public void singleToken() {
		// <root attr="value" />
		// 0123456789...
		String xml = "<root attr=\"value\" />";
		DOMDocument doc = parse(xml);
		SemanticTokensData builder = new SemanticTokensData(doc);

		// "attr" starts at offset 6, length 4
		builder.addToken(6, 4, 0, 0);

		SemanticTokens result = builder.build();
		List<Integer> data = result.getData();
		assertEquals(5, data.size());
		assertEquals(0, data.get(0)); // deltaLine
		assertEquals(6, data.get(1)); // deltaStartChar
		assertEquals(4, data.get(2)); // length
		assertEquals(0, data.get(3)); // tokenType
		assertEquals(0, data.get(4)); // tokenModifiers
	}

	@Test
	public void twoTokensSameLine() {
		// <root a="1" b="2" />
		// 0123456789012345678
		String xml = "<root a=\"1\" b=\"2\" />";
		DOMDocument doc = parse(xml);
		SemanticTokensData builder = new SemanticTokensData(doc);

		// "a" at offset 6, length 1
		builder.addToken(6, 1, 0, 0);
		// "b" at offset 12, length 1
		builder.addToken(12, 1, 0, 0);

		SemanticTokens result = builder.build();
		List<Integer> data = result.getData();
		assertEquals(10, data.size());

		// Token 1: "a" at line 0, char 6
		assertEquals(0, data.get(0)); // deltaLine
		assertEquals(6, data.get(1)); // deltaStartChar
		assertEquals(1, data.get(2)); // length

		// Token 2: "b" at line 0, char 12
		assertEquals(0, data.get(5)); // deltaLine (same line)
		assertEquals(6, data.get(6)); // deltaStartChar = 12 - 6
		assertEquals(1, data.get(7)); // length
	}

	@Test
	public void twoTokensDifferentLines() {
		String xml = "<root\n  a=\"1\"\n  b=\"2\" />";
		DOMDocument doc = parse(xml);
		SemanticTokensData builder = new SemanticTokensData(doc);

		// "a" at line 1, char 2
		int aOffset = xml.indexOf("a=");
		builder.addToken(aOffset, 1, 0, 0);
		// "b" at line 2, char 2
		int bOffset = xml.indexOf("b=");
		builder.addToken(bOffset, 1, 0, 0);

		SemanticTokens result = builder.build();
		List<Integer> data = result.getData();
		assertEquals(10, data.size());

		// Token 1: "a" at line 1, char 2
		assertEquals(1, data.get(0)); // deltaLine
		assertEquals(2, data.get(1)); // deltaStartChar
		assertEquals(1, data.get(2)); // length

		// Token 2: "b" at line 2
		assertEquals(1, data.get(5)); // deltaLine = 2 - 1
		assertEquals(2, data.get(6)); // deltaStartChar = 2 (new line, reset)
		assertEquals(1, data.get(7)); // length
	}

	@Test
	public void tokenModifiers() {
		String xml = "<root attr=\"value\" />";
		DOMDocument doc = parse(xml);
		SemanticTokensData builder = new SemanticTokensData(doc);

		builder.addToken(6, 4, 0, 1); // tokenModifiers = 1 (readonly)

		SemanticTokens result = builder.build();
		List<Integer> data = result.getData();
		assertEquals(0, data.get(3)); // tokenType
		assertEquals(1, data.get(4)); // tokenModifiers
	}

	@Test
	public void multipleTokenTypes() {
		String xml = "<root a=\"1\" b=\"2\" />";
		DOMDocument doc = parse(xml);
		SemanticTokensData builder = new SemanticTokensData(doc);

		builder.addToken(6, 1, 0, 1); // type=0, mod=1
		builder.addToken(12, 1, 1, 0); // type=1, mod=0

		SemanticTokens result = builder.build();
		List<Integer> data = result.getData();
		assertEquals(0, data.get(3)); // first token type
		assertEquals(1, data.get(4)); // first token modifiers
		assertEquals(1, data.get(8)); // second token type
		assertEquals(0, data.get(9)); // second token modifiers
	}

	@Test
	public void isEmptyAfterAddingToken() {
		String xml = "<root attr=\"value\" />";
		DOMDocument doc = parse(xml);
		SemanticTokensData builder = new SemanticTokensData(doc);

		assertTrue(builder.isEmpty());
		builder.addToken(6, 4, 0, 0);
		assertTrue(!builder.isEmpty());
	}

	// ------------------- XMLLanguageService integration -------------------

	@Test
	public void findSemanticTokensReturnsEmptyForNoGrammar() {
		XMLLanguageService service = new XMLLanguageService();
		DOMDocument doc = parse("<root />", service);

		SemanticTokens tokens = service.findSemanticTokens(doc, () -> {});
		assertNotNull(tokens);
		assertEquals(0, tokens.getData().size());
	}

	@Test
	public void findSemanticTokensWithCancelledChecker() {
		XMLLanguageService service = new XMLLanguageService();
		DOMDocument doc = parse("<root />", service);

		assertThrows(java.util.concurrent.CancellationException.class, () -> {
			service.findSemanticTokens(doc, () -> {
				throw new java.util.concurrent.CancellationException("cancelled");
			});
		});
	}

	// ------------------- Helper methods -------------------

	private DOMDocument parse(String xml) {
		TextDocument textDocument = new TextDocument(xml, "test://test/test.xml");
		return DOMParser.getInstance().parse(textDocument, null);
	}

	private DOMDocument parse(String xml, XMLLanguageService service) {
		TextDocument textDocument = new TextDocument(xml, "test://test/test.xml");
		DOMDocument doc = DOMParser.getInstance().parse(textDocument,
				service.getResolverExtensionManager());
		service.setDocumentProvider(uri -> doc);
		return doc;
	}
}
