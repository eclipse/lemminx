/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.contentmodel;

import static org.eclipse.lemminx.XMLAssert.d;
import static org.eclipse.lemminx.XMLAssert.l;
import static org.eclipse.lemminx.XMLAssert.r;

import java.util.Arrays;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.extensions.contentmodel.participants.XMLSyntaxErrorCode;
import org.eclipse.lemminx.extensions.contentmodel.settings.ContentModelSettings;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationRootSettings;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticRelatedInformation;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.PublishDiagnosticsCapabilities;
import org.junit.jupiter.api.Test;

public class XMLSyntaxRelatedInfoFinderTest extends AbstractCacheBasedTest {

	final String XML_FILE_PATH = "src/test/resources/test.xml";

	@Test
	public void eTagRequiredWithoutRelatedInfo() {
		String xml = //
				"<root>\n" + //
				"  <boot>\n" + //
				"</root>";

				Diagnostic diagnostic = d(1, 3, 1, 7,
				XMLSyntaxErrorCode.ETagRequired,
				"The element type \"boot\" must be terminated by the matching end-tag \"</boot>\".");
		diagnostic.setSeverity(DiagnosticSeverity.Error);
		diagnostic.setSource("xml");

		XMLValidationRootSettings validationSettings = new XMLValidationRootSettings();
		validationSettings.setNoGrammar("ignore");
		ContentModelSettings settings = new ContentModelSettings();
		settings.setValidation(validationSettings);
		XMLLanguageService xmlLanguageService = new XMLLanguageService();
		XMLAssert.testDiagnosticsFor(xmlLanguageService, xml, null, null, XML_FILE_PATH, false,
				settings, //
				diagnostic);
	}

	@Test
	public void eTagRequiredRelatedInfo() {
		String xml = //
				"<root>\n" + //
				"  <boot>\n" + //
				"</root>";

		Diagnostic diagnostic = d(1, 3, 1, 7,
				XMLSyntaxErrorCode.ETagRequired,
				"The element type \"boot\" must be terminated by the matching end-tag \"</boot>\".");
		diagnostic.setRelatedInformation(Arrays.asList(
			new DiagnosticRelatedInformation(l(XML_FILE_PATH, r(2, 0, 2, 0)), "")
		));
		diagnostic.setSeverity(DiagnosticSeverity.Error);
		diagnostic.setSource("xml");

		assertDiagnosticsWithRelatedInfo(xml, diagnostic);
	}

	@Test
	public void eTagRequiredIncompleteClosing1RelatedInfo() {
		String xml = //
				"<root>\n" + //
				"  <boot></\n" + //
				"</root>";

		Diagnostic diagnostic = d(1, 3, 1, 7,
				XMLSyntaxErrorCode.ETagRequired,
				"The element type \"boot\" must be terminated by the matching end-tag \"</boot>\".");
		diagnostic.setRelatedInformation(Arrays.asList(
			new DiagnosticRelatedInformation(l(XML_FILE_PATH, r(1, 10, 1, 10)), "")
		));
		diagnostic.setSeverity(DiagnosticSeverity.Error);
		diagnostic.setSource("xml");

		assertDiagnosticsWithRelatedInfo(xml, diagnostic);
	}

	@Test
	public void eTagRequiredIncompleteClosing2RelatedInfo() {
		String xml = //
				"<root>\n" + //
				"  <boot></boo\n" + //
				"</root>";

		Diagnostic diagnostic = d(1, 3, 1, 7,
				XMLSyntaxErrorCode.ETagRequired,
				"The element type \"boot\" must be terminated by the matching end-tag \"</boot>\".");
		diagnostic.setRelatedInformation(Arrays.asList(
			new DiagnosticRelatedInformation(l(XML_FILE_PATH, r(2, 0, 2, 0)), "")
		));
		diagnostic.setSeverity(DiagnosticSeverity.Error);
		diagnostic.setSource("xml");

		assertDiagnosticsWithRelatedInfo(xml, diagnostic);
	}

	@Test
	public void eTagUnterminatedNoRelatedInfo() {
		String xml = //
				"<root>\n" + //
				"  <boot></boot\n" + //
				"</root>";

		Diagnostic diagnostic = d(1, 10, 1, 14,
				XMLSyntaxErrorCode.ETagUnterminated,
				"The end-tag for element type \"boot\" must end with a '>' delimiter.");

		diagnostic.setSeverity(DiagnosticSeverity.Error);
		diagnostic.setSource("xml");

		assertDiagnosticsWithRelatedInfo(xml, diagnostic);
	}

	@Test
	public void markupEntityMismatchRelatedInfoWithNestedElement() {
		String xml = //
				"<root>\n" + //
				"  <boot>";

		Diagnostic diagnostic = d(0, 1, 0, 5,
				XMLSyntaxErrorCode.MarkupEntityMismatch,
				"XML document structures must start and end within the same entity.");
		diagnostic.setRelatedInformation(Arrays.asList(
			new DiagnosticRelatedInformation(l(XML_FILE_PATH, r(1, 8, 1, 8)), "")
		));
		diagnostic.setSeverity(DiagnosticSeverity.Error);
		diagnostic.setSource("xml");

		assertDiagnosticsWithRelatedInfo(xml, diagnostic);
	}

	@Test
	public void markupEntityMismatchRelatedInfoWithAttributes() {
		String xml = //
				"<root aaa='bbb' ccc='ddd'";

		Diagnostic diagnostic = d(0, 1, 0, 5,
				XMLSyntaxErrorCode.MarkupEntityMismatch,
				"XML document structures must start and end within the same entity.");
		diagnostic.setRelatedInformation(Arrays.asList(
			new DiagnosticRelatedInformation(l(XML_FILE_PATH, r(0, 25, 0, 25)), "")
		));
		diagnostic.setSeverity(DiagnosticSeverity.Error);
		diagnostic.setSource("xml");

		assertDiagnosticsWithRelatedInfo(xml, diagnostic);
	}

	@Test
	public void markupEntityMismatchRelatedInfoWithBrokenClosingTag() {
		String xml = //
				"<root>\n" + //
				"  \n" + //
				"</root";

		Diagnostic diagnostic = d(2, 2, 2, 6,
				XMLSyntaxErrorCode.MarkupEntityMismatch,
				"XML document structures must start and end within the same entity.");
		diagnostic.setSeverity(DiagnosticSeverity.Error);
		diagnostic.setSource("xml");

		assertDiagnosticsWithRelatedInfo(xml, diagnostic);
	}

	private void assertDiagnosticsWithRelatedInfo(String xml, Diagnostic diagnostic) {
		XMLValidationRootSettings validationSettings = new XMLValidationRootSettings();
		validationSettings.setNoGrammar("ignore");
		validationSettings.setCapabilities(new PublishDiagnosticsCapabilities(true));
		ContentModelSettings settings = new ContentModelSettings();
		settings.setValidation(validationSettings);
		XMLLanguageService xmlLanguageService = new XMLLanguageService();
		XMLAssert.testDiagnosticsFor(xmlLanguageService, xml, null, null, XML_FILE_PATH, false,
				settings, //
				diagnostic);
	}

}
