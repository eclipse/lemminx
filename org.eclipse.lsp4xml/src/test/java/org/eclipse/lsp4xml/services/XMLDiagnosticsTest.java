package org.eclipse.lsp4xml.services;

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.internal.parser.XMLParser;
import org.eclipse.lsp4xml.model.XMLDocument;
import org.junit.Test;

public class XMLDiagnosticsTest {

	@Test
	public void testName() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		
		TextDocument document = new TextDocument("<a", "test.xml");
		List<Diagnostic> dia = languageService.doDiagnostics(document, null, null);
		System.err.println(dia);
	}
	
	@Test
	public void t() {
		XMLLanguageService languageService = new XMLLanguageService();
		
		XMLDocument document = XMLParser.getInstance().parse("<a x=\"><b/></a>", "");
		
		System.err.println(document);
	}
}
