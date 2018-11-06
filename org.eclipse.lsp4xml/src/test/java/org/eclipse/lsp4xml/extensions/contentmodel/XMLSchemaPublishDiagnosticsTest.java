package org.eclipse.lsp4xml.extensions.contentmodel;

import static org.eclipse.lsp4xml.XMLAssert.pd;
import static org.eclipse.lsp4xml.XMLAssert.r;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelManager;
import org.junit.Test;

public class XMLSchemaPublishDiagnosticsTest {

	String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
			"<invoice xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
			" xsi:noNamespaceSchemaLocation=\"http://invoice.xsd\">\r\n" + //
			"  <date>2017-11-30_INVALID</date>      \r\n" + // <- here is the error
			"  <number>5235</number> \r\n" + //
			"  <products>   \r\n" + //
			"    <product description=\"laptop\" price=\"700.00\"/>\r\n" + //
			"    <product description=\"mouse\" price=\"30.00\"  />\r\n" + //
			"  </products>\r\n" + //
			"  <payments> \r\n" + //
			"    <payment amount=\"770.00\" method=\"credit\"/>\r\n" + //
			"  </payments>\r\n" + //
			"</invoice> \r\n" + //
			"";

	@Test
	public void schemaWithUrlWithoutCache() throws Exception {
		// Here we test the following context:
		// - XML which have xsi:noNamespaceSchemaLocation="http://invoice.xsd"
		// - XMLCacheResolverExtension which is disabled
		// Result of test is to have one published diagnostics with several Xerces
		// errors (schema)

		// Don't use cache on file system
		ContentModelManager.getInstance().setUseCache(false);

		String fileURI = "test.xml";
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<invoice xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				" xsi:noNamespaceSchemaLocation=\"http://invoice.xsd\">\r\n" + //
				"</invoice> \r\n" + //
				"";
		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, pd(fileURI, //
				new Diagnostic(r(2, 52, 2, 52),
						"schema_reference.4: Failed to read schema document 'http://invoice.xsd', because 1) could not find the document; 2) the document could not be read; 3) the root element of the document is not <xsd:schema>.",
						DiagnosticSeverity.Warning, "xml", "schema_reference.4"), //
				new Diagnostic(r(1, 1, 1, 8), "cvc-elt.1.a: Cannot find the declaration of element 'invoice'.",
						DiagnosticSeverity.Error, "xml", "cvc-elt.1.a")));
	}

	@Test
	public void schemaWithUrlWithCache() throws Exception {
		// Here we test the following context:
		// - XML which have xsi:noNamespaceSchemaLocation="http://invoice.xsd"
		// - XMLCacheResolverExtension which is enabled
		// Result of test is to have 2 published diagnostics (resource downloading as
		// info and error downloading).

		// Activate cache resolver
		ContentModelManager.getInstance().setUseCache(true);

		String fileURI = "test.xml";
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<invoice xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				" xsi:noNamespaceSchemaLocation=\"http://invoice.xsd\">\r\n" + //
				"</invoice> \r\n" + //
				"";
		XMLAssert.testPublishDiagnosticsFor(xml, fileURI,
				pd(fileURI,
						new Diagnostic(r(1, 1, 1, 8), "The resource 'http://invoice.xsd' is downloading.",
								DiagnosticSeverity.Information, "XML")),
				pd(fileURI, new Diagnostic(r(1, 1, 1, 8), "Error while downloading 'http://invoice.xsd'.",
						DiagnosticSeverity.Error, "XML")));
	}
}
