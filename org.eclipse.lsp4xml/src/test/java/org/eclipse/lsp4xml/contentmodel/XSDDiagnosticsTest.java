package org.eclipse.lsp4xml.contentmodel;
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
import org.eclipse.lsp4xml.contentmodel.settings.ContentModelSettings;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.junit.Assert;
import org.junit.Test;

/**
 * XML diagnostics services tests
 *
 */
public class XSDDiagnosticsTest {

	@Test
	public void testCVCAttribute3() throws Exception {
		String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\r\n" + //
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\r\n"
				+ //
				"<p></p>" + "</project>";
		testDiagnosticsFor(xml, d(3, 3, 3, 3, "cvc-complex-type.2.4.a"));
	}

	private static void testDiagnosticsFor(String xml, Diagnostic... expected) {
		TextDocument document = new TextDocument(xml.toString(), "test.xml");

		XMLLanguageService xmlLanguageService = new XMLLanguageService();

		// Configure XML catalog for XML schema
		String catalogPath = "src/test/resources/catalogs/catalog.xml";
		ContentModelSettings settings = new ContentModelSettings();
		settings.setCatalogs(new String[] { catalogPath });
		xmlLanguageService.updateSettings(settings);

		List<Diagnostic> diagnostics = xmlLanguageService.doDiagnostics(document, () -> {
		});
		assertDiagnostics(diagnostics, expected);

	}

	private XMLLanguageService createXMLLanguageService(String catalogPath) {
		ContentModelSettings settings = new ContentModelSettings();
		settings.setCatalogs(new String[] { catalogPath });
		XMLLanguageService languageService = new XMLLanguageService();
		languageService.updateSettings(settings);
		return languageService;
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
		Assert.assertArrayEquals(actual.toArray(), expected);
	}

	private Diagnostic d(int startLine, int startCharacter, int endLine, int endCharacter, String code) {
		return new Diagnostic(new Range(new Position(startLine, startCharacter), new Position(endLine, endCharacter)),
				null, null, null, code);
	}

}
