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
import org.junit.Ignore;
import org.junit.Test;

/**
 * XML diagnostics services tests
 *
 */
public class XMLSchemaDiagnosticsTest {

	@Ignore
	@Test
	public void testCVCAttribute3() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		String xml = new StringBuilder(
				"<project xsi:schemalocation=\"https://www.w3.org/TR/xmlschema11-1/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
						+ //
						" <InstdAmt Ccy=\"EURO\">3.14</InstdAmt>\r\n" + //
						" </project>") //
								.toString();
		TextDocument document = new TextDocument(xml.toString(), "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, d(0, 0, 0, 0, "cvc-attribute-3"));
	}

	/*
	 * @Test public void testBad2() throws Exception { XMLLanguageService
	 * languageService = new XMLLanguageService();
	 * ContentModelDiagnosticsParticipant dp = (ContentModelDiagnosticsParticipant)
	 * ((List<IDiagnosticsParticipant>) languageService
	 * .getDiagnosticsParticipants()).get(0); ContentModelDiagnosticsConfiguration
	 * config = new ContentModelDiagnosticsConfiguration(); config.setCatalogs(new
	 * String[] { "src/test/resources/catalog.xml" }); dp.setConfiguration(config);
	 * 
	 * String xml = new
	 * StringBuilder("<person xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
	 * ) // .append(" xsi:noNamespaceSchemaLocation=\"http://person.xsd\">") //
	 * .append("<nameA>XXX</name>") // .append("</person>") // .toString();
	 * TextDocument document = new TextDocument(xml.toString(), "person.xml");
	 * List<Diagnostic> diagnostics = languageService.doDiagnostics(document,
	 * NULL_MONITOR); System.err.println(diagnostics); }
	 * 
	 * @Test public void testName() throws Exception { XMLLanguageService
	 * languageService = new XMLLanguageService();
	 * ContentModelDiagnosticsParticipant dp = (ContentModelDiagnosticsParticipant)
	 * ((List<IDiagnosticsParticipant>) languageService
	 * .getDiagnosticsParticipants()).get(0); ContentModelDiagnosticsConfiguration
	 * config = new ContentModelDiagnosticsConfiguration(); config.setCatalogs(new
	 * String[] { "src/test/resources/catalog.xml" }); dp.setConfiguration(config);
	 * 
	 * String xml = new
	 * StringBuilder("<person xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
	 * ) // .append(" xsi:noNamespaceSchemaLocation=\"http://person.xsd\">") //
	 * .append("<name>XXX</name>") // .append("</person>") // .toString();
	 * TextDocument document = new TextDocument(xml.toString(), "person.xml");
	 * List<Diagnostic> diagnostics = languageService.doDiagnostics(document,
	 * NULL_MONITOR); System.err.println(diagnostics); }
	 */

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
