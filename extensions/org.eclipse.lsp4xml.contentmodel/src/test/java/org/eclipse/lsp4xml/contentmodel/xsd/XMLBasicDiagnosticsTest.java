package org.eclipse.lsp4xml.contentmodel.xsd;
/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.junit.Assert;
import org.junit.Test;

/**
 * XML diagnostics services tests
 *
 */
public class XMLBasicDiagnosticsTest {

	/**
	 * ElementUnterminated tests
	 * 
	 * @see https://wiki.xmldation.com/Support/Validator/ElementUnterminated
	 * @throws Exception
	 */
	@Test
	public void testElementUnterminated() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder("<Id>\r\n" + //
				"          <OrgId\r\n" + //
				"            <Othr>\r\n" + //
				"              <Id> 222010012</Id>\r\n" + //
				"            </Othr>\r\n" + //
				"          </OrgId>\r\n" + //
				"        </Id>") //
						.toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(1, 11, 1, 16, "ElementUnterminated"));
	}

	private static void assertDiagnostics(List<Diagnostic> actual, Diagnostic... expected) {
		actual.stream().forEach(d -> {
			// we don't want to compare severity, message, etc
			d.setSeverity(null);
			d.setMessage(null);
			d.setSource(null);
		});
		Assert.assertEquals(actual.size(), expected.length);
		Assert.assertArrayEquals(expected, actual.toArray());
	}

	private Diagnostic d(int startLine, int startCharacter, int endLine, int endCharacter, String code) {
		return new Diagnostic(new Range(new Position(startLine, startCharacter), new Position(endLine, endCharacter)),
				null, null, null, code);
	}

}
