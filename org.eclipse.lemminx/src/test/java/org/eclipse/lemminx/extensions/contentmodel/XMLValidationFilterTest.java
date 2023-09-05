/**
 *  Copyright (c) 2022 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.extensions.contentmodel;

import static org.eclipse.lemminx.XMLAssert.pd;
import static org.eclipse.lemminx.XMLAssert.r;

import java.util.Arrays;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.extensions.contentmodel.participants.XMLSyntaxErrorCode;
import org.eclipse.lemminx.extensions.contentmodel.settings.SchemaEnabled;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLSchemaSettings;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationFilter;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationRootSettings;
import org.eclipse.lemminx.extensions.xsd.participants.XSDErrorCode;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

/**
 * XML validation filter tests.
 *
 */
public class XMLValidationFilterTest extends AbstractCacheBasedTest {

	@Test
	public void disableValidationFilter() throws Exception {

		String xml = "<foo";

		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		XMLValidationFilter filter = new XMLValidationFilter();
		filter.setEnabled(false);
		filter.setPattern("**.myxml");
		validation.setFilters(Arrays.asList(filter).toArray(new XMLValidationFilter[0]));
		XMLLanguageService ls = new XMLLanguageService();

		String fileURI = "file:///home/test.xml";
		// test.xml doesn't matches the validation filter
		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, validation, ls, pd(fileURI,
				new Diagnostic(r(0, 1, 0, 4), "No grammar constraints (DTD or XML Schema).", DiagnosticSeverity.Hint,
						"xml", XMLSyntaxErrorCode.NoGrammarConstraints.getCode()), //
				new Diagnostic(r(0, 1, 0, 4), "XML document structures must start and end within the same entity.",
						DiagnosticSeverity.Error, "xml", XMLSyntaxErrorCode.MarkupEntityMismatch.getCode())));

		// test.exsd matches the validation filter
		fileURI = "file:///home/test.myxml";
		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, validation, ls, pd(fileURI));
	}

	@Test
	public void noGrammarFilter() throws Exception {

		String xml = "<foo />";

		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		XMLValidationFilter filter = new XMLValidationFilter();
		filter.setEnabled(true);
		filter.setNoGrammar("ignore");
		filter.setPattern("foo.xml");
		validation.setFilters(Arrays.asList(filter).toArray(new XMLValidationFilter[0]));
		XMLLanguageService ls = new XMLLanguageService();

		String fileURI = "file:///home/test.xml";
		// test.xml doesn't matches the validation filter
		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, validation, ls,
				pd(fileURI, new Diagnostic(r(0, 1, 0, 4), "No grammar constraints (DTD or XML Schema).",
						DiagnosticSeverity.Hint, "xml", XMLSyntaxErrorCode.NoGrammarConstraints.getCode())));

		// test.exsd matches the validation filter
		fileURI = "file:///home/foo.xml";
		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, validation, ls, pd(fileURI));
	}

	@Test
	public void defaultDisableNoGrammarFilter() throws Exception {

		String xml = "<foo />";
		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		XMLLanguageService ls = new XMLLanguageService();

		String fileURI = "file:///home/test.xml";
		// test.xml doesn't matches the validation filter
		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, validation, ls,
				pd(fileURI, new Diagnostic(r(0, 1, 0, 4), "No grammar constraints (DTD or XML Schema).",
						DiagnosticSeverity.Hint, "xml", XMLSyntaxErrorCode.NoGrammarConstraints.getCode())));

		// plugin.xml matches the validation filter
		fileURI = "file:///home/.project";
		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, validation, ls, pd(fileURI));
		fileURI = "file:///home/.classpath";
		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, validation, ls, pd(fileURI));
		fileURI = "file:///home/plugin.xml";
		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, validation, ls, pd(fileURI));
		fileURI = "file:///home/feature.xml";
		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, validation, ls, pd(fileURI));
		fileURI = "file:///home/category.xml";
		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, validation, ls, pd(fileURI));
		fileURI = "file:///home/foo.target";
		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, validation, ls, pd(fileURI));
		fileURI = "file:///home/foo.product";
		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, validation, ls, pd(fileURI));
	}

	@Test
	public void disableXMLSchemaValidationFilter() throws Exception {
		String xml = "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\">\r\n" + //
				"    <annotation>\r\n" + //
				"      <appInfo>\r\n" + // <-- XSD error here, it should be appinfo
				"      </appInfo>\r\n" + //
				"   </annotation>\r\n" + //
				"</schema>";

		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		XMLValidationFilter filter = new XMLValidationFilter();
		filter.setEnabled(true);
		filter.setNoGrammar("ignore");
		XMLSchemaSettings schemaSettings = new XMLSchemaSettings();
		schemaSettings.setEnabled(SchemaEnabled.never);
		filter.setSchema(schemaSettings);
		filter.setPattern("**.myxsd");
		validation.setFilters(Arrays.asList(filter).toArray(new XMLValidationFilter[0]));
		XMLLanguageService ls = new XMLLanguageService();

		String fileURI = "file:///home/test.xsd";
		// test.xsd doesn't matches the validation filter
		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, validation, ls, pd(fileURI, //
				// XML schema error code
				new Diagnostic(r(2, 7, 2, 14),
						"src-annotation: <annotation> elements can only contain <appinfo> and <documentation> elements, but 'appInfo' was found.",
						DiagnosticSeverity.Error, "xsd", XSDErrorCode.src_annotation.getCode())));

		// test.exsd matches the validation filter
		// XSD error should not reported
		fileURI = "file:///home/test.myxsd";
		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, validation, ls, pd(fileURI));

		// Only XML syntax error must be reported
		xml = "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\">\r\n" + //
				"    <annotation>\r\n" + // <-- annotation is not closed
				"</schema>";
		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, validation, ls, pd(fileURI, //
				new Diagnostic(r(1, 5, 1, 15),
						"The element type \"annotation\" must be terminated by the matching end-tag \"</annotation>\".",
						DiagnosticSeverity.Error, "xml", XMLSyntaxErrorCode.ETagRequired.getCode())));
	}

	@Test
	public void defaultDisableXMLSchemaValidationFilter() throws Exception {
		String xml = "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\">\r\n" + //
				"    <annotation>\r\n" + //
				"      <appInfo>\r\n" + // <-- XSD error here, it should be appinfo
				"      </appInfo>\r\n" + //
				"   </annotation>\r\n" + //
				"</schema>";
		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		XMLLanguageService ls = new XMLLanguageService();

		String fileURI = "file:///home/test.xsd";
		// test.xsd doesn't matches the validation filter
		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, validation, ls, pd(fileURI, //
				// XML schema error code
				new Diagnostic(r(2, 7, 2, 14),
						"src-annotation: <annotation> elements can only contain <appinfo> and <documentation> elements, but 'appInfo' was found.",
						DiagnosticSeverity.Error, "xsd", XSDErrorCode.src_annotation.getCode())));

		// test.exsd matches the validation filter
		// XSD error should not reported for *.exsd files
		fileURI = "file:///home/test.exsd";
		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, validation, ls, pd(fileURI));

		// Only XML syntax error must be reported for *.exsd files
		xml = "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\">\r\n" + //
				"    <annotation>\r\n" + // <-- annotation is not closed
				"</schema>";
		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, validation, ls, pd(fileURI, //
				new Diagnostic(r(1, 5, 1, 15),
						"The element type \"annotation\" must be terminated by the matching end-tag \"</annotation>\".",
						DiagnosticSeverity.Error, "xml", XMLSyntaxErrorCode.ETagRequired.getCode())));
	}
}
