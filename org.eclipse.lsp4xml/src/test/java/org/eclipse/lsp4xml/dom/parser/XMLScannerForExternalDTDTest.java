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
package org.eclipse.lsp4xml.dom.parser;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * XML scanner test to parse external DTD.
 *
 */
public class XMLScannerForExternalDTDTest {

	public Scanner scanner;

	@Test
	public void testDocumentTypeExternalOnly() {
		String dtd = "                \n" + //
				"  <!ENTITY nbsp \"&#xA0;\"> \n" + //
				"  <!ENTITY writer \"Writer: Donald Duck.\">\n" + //
				"";
		scanner = XMLScanner.createScanner(dtd, true);
		// assertOffsetAndToken(0, TokenType.DTDStartDoctypeTag);
		assertOffsetAndToken(0, TokenType.Whitespace);
		// assertOffsetAndToken(19, TokenType.DTDDoctypeName);
		// assertOffsetAndToken(14, TokenType.Whitespace);
		// assertOffsetAndToken(15, TokenType.DTDStartInternalSubset);
		// assertOffsetAndToken(16, TokenType.Whitespace);
		assertOffsetAndToken(19, TokenType.DTDStartEntity);
		assertOffsetAndToken(27, TokenType.Whitespace);
		assertOffsetAndToken(28, TokenType.DTDEntityName);
		assertOffsetAndToken(32, TokenType.Whitespace);
		assertOffsetAndToken(33, TokenType.DTDEntityValue);
		assertOffsetAndToken(41, TokenType.DTDEndTag);
		assertOffsetAndToken(42, TokenType.Whitespace);
		assertOffsetAndToken(46, TokenType.DTDStartEntity);
		assertOffsetAndToken(54, TokenType.Whitespace);
		assertOffsetAndToken(55, TokenType.DTDEntityName);
		assertOffsetAndToken(61, TokenType.Whitespace);
		assertOffsetAndToken(62, TokenType.DTDEntityValue);
		assertOffsetAndToken(84, TokenType.DTDEndTag);
		assertOffsetAndToken(85, TokenType.Whitespace);
		// assertOffsetAndToken(86, TokenType.DTDEndInternalSubset);
		// assertOffsetAndToken(87, TokenType.DTDEndDoctypeTag);

	}

	public void assertOffsetAndToken(int tokenOffset, TokenType tokenType) {
		TokenType token = scanner.scan();
		assertEquals(tokenOffset, scanner.getTokenOffset());
		assertEquals(tokenType, token);
	}

	public void assertOffsetAndToken(int tokenOffset, TokenType tokenType, String tokenText) {
		TokenType token = scanner.scan();
		assertEquals(tokenOffset, scanner.getTokenOffset());
		assertEquals(tokenType, token);
		assertEquals(tokenText, scanner.getTokenText());
	}
}
