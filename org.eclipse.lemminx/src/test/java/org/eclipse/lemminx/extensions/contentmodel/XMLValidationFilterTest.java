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
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationFilter;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationRootSettings;
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
	public void defaultDisableValidationFilter() throws Exception {

		String xml = "<foo";
		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		XMLLanguageService ls = new XMLLanguageService();

		String fileURI = "file:///home/test.xml";
		// test.xml doesn't matches the validation filter
		XMLAssert.testPublishDiagnosticsFor(xml, fileURI, validation, ls, pd(fileURI,
				new Diagnostic(r(0, 1, 0, 4), "No grammar constraints (DTD or XML Schema).", DiagnosticSeverity.Hint,
						"xml", XMLSyntaxErrorCode.NoGrammarConstraints.getCode()), //
				new Diagnostic(r(0, 1, 0, 4), "XML document structures must start and end within the same entity.",
						DiagnosticSeverity.Error, "xml", XMLSyntaxErrorCode.MarkupEntityMismatch.getCode())));

		// test.exsd matches the validation filter
		fileURI = "file:///home/test.exsd";
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
}
