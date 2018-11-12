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
package org.eclipse.lsp4xml.extensions.contentmodel;

import static org.eclipse.lsp4xml.XMLAssert.pd;
import static org.eclipse.lsp4xml.XMLAssert.r;

import java.util.function.Consumer;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.XMLSchemaErrorCode;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.junit.Test;

/**
 * Test with published diagnostics.
 *
 */
public class XMLSchemaPublishDiagnosticsTest {

	@Test
	public void schemaWithUrlWithoutCache() throws Exception {
		// Here we test the following context:
		// - XML which have xsi:noNamespaceSchemaLocation="http://invoice.xsd"
		// - XMLCacheResolverExtension which is disabled
		// Result of test is to have one published diagnostics with several Xerces
		// errors (schema)

		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			// Use cache on file system
			contentModelManager.setUseCache(false);
		};

		String fileURI = "test.xml";
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<invoice xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				" xsi:noNamespaceSchemaLocation=\"http://invoice.xsd\">\r\n" + //
				"</invoice> \r\n" + //
				"";
		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, configuration, pd(fileURI, //
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

		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			// Use cache on file system
			contentModelManager.setUseCache(true);
		};

		String fileURI = "test.xml";
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<invoice xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				" xsi:noNamespaceSchemaLocation=\"http://invoice.xsd\">\r\n" + //
				"</invoice> \r\n" + //
				"";
		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, configuration,
				pd(fileURI,
						new Diagnostic(r(1, 1, 1, 8), "The resource 'http://invoice.xsd' is downloading.",
								DiagnosticSeverity.Information, "XML")),
				pd(fileURI, new Diagnostic(r(1, 1, 1, 8), "Error while downloading 'http://invoice.xsd'.",
						DiagnosticSeverity.Error, "XML")));
	}

	@Test
	public void schemaWithUrlWithCacheAndWithCatalog() throws Exception {
		// Here we test the following context:
		// - XML which have xsi:noNamespaceSchemaLocation="http://invoice.xsd"
		// - XMLCacheResolverExtension which is enabled
		// - Catalog using which resolves XML Schema of the http://invoice.xsd
		// Result of test is to validate the XML with XML Schema

		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			// Use cache on file system
			contentModelManager.setUseCache(true);
			// use catalog which defines bind src/test/xsd/invoice.xsd with
			// http://invoice.xsd namespace
			contentModelManager.setCatalogs(new String[] { "src/test/resources/catalogs/catalog.xml" });
		};

		String fileURI = "test.xml";
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
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

		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, configuration, pd(fileURI, //
				new Diagnostic(r(3, 8, 3, 26),
						"cvc-datatype-valid.1.2.1: '2017-11-30_INVALID' is not a valid value for 'date'.",
						DiagnosticSeverity.Error, "xml", XMLSchemaErrorCode.cvc_datatype_valid_1_2_1.getCode()), //
				new Diagnostic(r(3, 8, 3, 26),
						"cvc-type.3.1.3: The value '2017-11-30_INVALID' of element 'date' is not valid.",
						DiagnosticSeverity.Error, "xml", XMLSchemaErrorCode.cvc_type_3_1_3.getCode())));
	}

	@Test
	public void schemaWithUrlWithoutCacheAndWithCatalog() throws Exception {
		// Here we test the following context:
		// - XML which have xsi:noNamespaceSchemaLocation="http://invoice.xsd"
		// - XMLCacheResolverExtension which is disabled
		// - Catalog using which resolves XML Schema of the http://invoice.xsd
		// Result of test is to validate the XML with XML Schema

		// use catalog which defines bind src/test/xsd/invoice.xsd with
		// http://invoice.xsd namespace

		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			// Don't use cache on file system
			contentModelManager.setUseCache(false);
			// use catalog which defines bind src/test/xsd/invoice.xsd with
			// http://invoice.xsd namespace
			contentModelManager.setCatalogs(new String[] { "src/test/resources/catalogs/catalog.xml" });
		};
		String fileURI = "test.xml";
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
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

		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, configuration, pd(fileURI, //
				new Diagnostic(r(3, 8, 3, 26),
						"cvc-datatype-valid.1.2.1: '2017-11-30_INVALID' is not a valid value for 'date'.",
						DiagnosticSeverity.Error, "xml", XMLSchemaErrorCode.cvc_datatype_valid_1_2_1.getCode()), //
				new Diagnostic(r(3, 8, 3, 26),
						"cvc-type.3.1.3: The value '2017-11-30_INVALID' of element 'date' is not valid.",
						DiagnosticSeverity.Error, "xml", XMLSchemaErrorCode.cvc_type_3_1_3.getCode())));
	}

}
