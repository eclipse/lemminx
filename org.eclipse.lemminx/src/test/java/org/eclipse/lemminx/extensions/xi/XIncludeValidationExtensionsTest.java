/**
 *  Copyright (c) 2022 Red Hat Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lemminx.extensions.xi;

import static org.eclipse.lemminx.XMLAssert.d;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.contentmodel.participants.DTDErrorCode;
import org.eclipse.lemminx.extensions.contentmodel.participants.ExternalResourceErrorCode;
import org.eclipse.lemminx.extensions.contentmodel.settings.ContentModelSettings;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationRootSettings;
import org.eclipse.lemminx.extensions.xinclude.XIncludeErrorCode;
import org.eclipse.lsp4j.Diagnostic;
import org.junit.jupiter.api.Test;

/**
 * XInclude validation tests.
 *
 */
public class XIncludeValidationExtensionsTest extends AbstractCacheBasedTest {

	@Test
	public void xIncludeValidationTrueIssue175() throws BadLocationException {
		ContentModelSettings settings = new ContentModelSettings();
		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		validation.getXInclude().setEnabled(true);
		settings.setValidation(validation);
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE chapter PUBLIC \"-//OASIS//DTD DocBook XML V4.5//EN\" \"https://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd\">"
				+ //
				"    <chapter id=\"chapter_id\">\r\n" + //
				"    <xi:include xmlns:xi=\"http://www.w3.org/2001/XInclude\" href=\"reference.xml\"/>\r\n" + //
				"</schema>";
		testDiagnosticsFor(xml, settings, //
				d(1, 63, 1, 118, ExternalResourceErrorCode.DownloadingResource), //
				d(1, 125, 1, 132, DTDErrorCode.MSG_ELEMENT_NOT_DECLARED), //
				d(2, 5, 2, 15, DTDErrorCode.MSG_ELEMENT_NOT_DECLARED), //
				d(2, 64, 2, 79, XIncludeErrorCode.XMLResourceError), //
				d(2, 64, 2, 79, XIncludeErrorCode.NoFallback));
	}

	@Test
	public void xIncludeValidationTrueNoDTD() throws BadLocationException {
		ContentModelSettings settings = new ContentModelSettings();
		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		validation.getXInclude().setEnabled(true);
		validation.setNoGrammar("ignore");
		settings.setValidation(validation);
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<book xmlns:xi=\"http://www.w3.org/2001/XInclude\">\r\n" + //
				"    <title>The Wit and Wisdom of George W. Bush</title>\r\n" + //
				"    <title>Sample title</title>\r\n" + //
				"    <xi:include href=\"http://www.whitehouse.gov/malapropisms.xml\"/>\r\n" + //
				"</book>";
		testDiagnosticsFor(xml, settings, //
				d(4, 21, 4, 65, XIncludeErrorCode.XMLResourceError), //
				d(4, 21, 4, 65, XIncludeErrorCode.NoFallback));
	}

	@Test
	public void xIncludeValidationFalseNoDTD() throws BadLocationException {
		ContentModelSettings settings = new ContentModelSettings();
		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		validation.getXInclude().setEnabled(false);
		validation.setNoGrammar("ignore");
		settings.setValidation(validation);
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<book xmlns:xi=\"http://www.w3.org/2001/XInclude\">\r\n" + //
				"    <title>The Wit and Wisdom of George W. Bush</title>\r\n" + //
				"    <title>Sample title</title>\r\n" + //
				"    <xi:include href=\"http://www.whitehouse.gov/malapropisms.xml\"/>\r\n" + //
				"</book>";
		testDiagnosticsFor(xml, settings);
	}

	@Test
	public void xIncludeValidationTrueNoHrefAttr() throws BadLocationException {
		ContentModelSettings settings = new ContentModelSettings();
		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		validation.getXInclude().setEnabled(true);
		validation.setNoGrammar("ignore");
		settings.setValidation(validation);
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<book xmlns:xi=\"http://www.w3.org/2001/XInclude\">\r\n" + //
				"    <xi:include/>\r\n" + //
				"</book>";
		testDiagnosticsFor(xml, settings, //
				d(2, 5, 2, 15, XIncludeErrorCode.XpointerMissing));
	}

	@Test
	public void xIncludeValidationTrueMultiFallback() throws BadLocationException {
		ContentModelSettings settings = new ContentModelSettings();
		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		validation.getXInclude().setEnabled(true);
		validation.setNoGrammar("ignore");
		settings.setValidation(validation);
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<book xmlns:xi=\"http://www.w3.org/2001/XInclude\">\r\n" + //
				"  <xi:include href=\"src/test/resources/xml/reference.xml\">\r\n" + //
				"    <xi:fallback><a href=\"mailto:bob@example.org\">Report error</a></xi:fallback>\r\n" + //
				"    <xi:fallback><a href=\"mailto:bob@example.org\">Report error</a></xi:fallback>\r\n" + //
				"  </xi:include>" + //
				"</book>";
		testDiagnosticsFor(xml, settings, //
				d(4, 5, 4, 16, XIncludeErrorCode.MultipleFallbacks));
	}

	@Test
	public void xIncludeValidationTrueFallbackNoParent() throws BadLocationException {
		ContentModelSettings settings = new ContentModelSettings();
		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		validation.getXInclude().setEnabled(true);
		validation.setNoGrammar("ignore");
		settings.setValidation(validation);
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<book xmlns:xi=\"http://www.w3.org/2001/XInclude\">\r\n" + //
				"  <xi:fallback><a href=\"mailto:bob@example.org\">Report error</a></xi:fallback>\r\n" + //
				"</book>";
		testDiagnosticsFor(xml, settings, //
				d(2, 3, 2, 14, XIncludeErrorCode.FallbackParent));
	}

	@Test
	public void xIncludeValidationTrueInvalidIncludeChild() throws BadLocationException {
		ContentModelSettings settings = new ContentModelSettings();
		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		validation.getXInclude().setEnabled(true);
		validation.setNoGrammar("ignore");
		settings.setValidation(validation);
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<book xmlns:xi=\"http://www.w3.org/2001/XInclude\">\r\n" + //
				"  <xi:include href=\"src/test/resources/xml/reference.xml\">\r\n" + //
				"    <xi:include></include>\r\n" + //
				"  </xi:include>" + //
				"</book>";
		testDiagnosticsFor(xml, settings, //
				d(3, 5, 3, 15, XIncludeErrorCode.IncludeChild));
	}

	@Test
	public void xIncludeValidationTrueInvalidParseValue() throws BadLocationException {
		ContentModelSettings settings = new ContentModelSettings();
		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		validation.getXInclude().setEnabled(true);
		validation.setNoGrammar("ignore");
		settings.setValidation(validation);
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<book xmlns:xi=\"http://www.w3.org/2001/XInclude\">\r\n" + //
				"  <xi:include href=\"src/test/resources/xml/reference.xml\" parse=\"invalid\"/>\r\n" + //
				"</book>";
		testDiagnosticsFor(xml, settings, //
				d(2, 64, 2, 73, XIncludeErrorCode.InvalidParseValue));
	}

	@Test
	public void xIncludeValidationTrueAcceptMalformed() throws BadLocationException {
		ContentModelSettings settings = new ContentModelSettings();
		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		validation.getXInclude().setEnabled(true);
		validation.setNoGrammar("ignore");
		settings.setValidation(validation);
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<book xmlns:xi=\"http://www.w3.org/2001/XInclude\">\r\n" + //
				"  <xi:include href=\"src/test/resources/xml/reference.xml\" accept=\"�\"/>\r\n" + //
				"</book>";
		testDiagnosticsFor(xml, settings, //
				d(2, 65, 2, 68, XIncludeErrorCode.AcceptMalformed));
	}

	@Test
	public void xIncludeValidationTrueAcceptLanguageMalformed() throws BadLocationException {
		ContentModelSettings settings = new ContentModelSettings();
		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		validation.getXInclude().setEnabled(true);
		validation.setNoGrammar("ignore");
		settings.setValidation(validation);
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<book xmlns:xi=\"http://www.w3.org/2001/XInclude\">\r\n" + //
				"  <xi:include href=\"src/test/resources/xml/reference.xml\" accept-language=\"�\"/>\r\n" + //
				"</book>";
		testDiagnosticsFor(xml, settings, //
				d(2, 74, 2, 77, XIncludeErrorCode.AcceptLanguageMalformed));
	}

	@Test
	public void xIncludeValidationTrueNoRootElement() throws BadLocationException {
		ContentModelSettings settings = new ContentModelSettings();
		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		validation.getXInclude().setEnabled(true);
		validation.setNoGrammar("ignore");
		settings.setValidation(validation);
		String xml = "   \r\n";
		testDiagnosticsFor(xml, settings, //
				d(0, 1, 0, 1, XIncludeErrorCode.RootElementRequired));
	}

	@Test
	public void xIncludeValidationTrueHrefFragmentIdentifier() throws BadLocationException {
		ContentModelSettings settings = new ContentModelSettings();
		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		validation.getXInclude().setEnabled(true);
		validation.setNoGrammar("ignore");
		settings.setValidation(validation);
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<book xmlns:xi=\"http://www.w3.org/2001/XInclude\">\r\n" + //
				"    <xi:include href=\"http://www.whitehouse.gov/#invalid/malapropisms.xml\"/>\r\n" + //
				"</book>";
		testDiagnosticsFor(xml, settings, //
				d(2, 21, 2, 74, XIncludeErrorCode.HrefFragmentIdentifierIllegal));
	}

	@Test
	public void xIncludeValidationTrueHrefInvalidSyntax() throws BadLocationException {
		ContentModelSettings settings = new ContentModelSettings();
		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		validation.getXInclude().setEnabled(true);
		validation.setNoGrammar("ignore");
		settings.setValidation(validation);
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<book xmlns:xi=\"http://www.w3.org/2001/XInclude\">\r\n" + //
				"    <xi:include href=\"�\"/>\r\n" + //
				"</book>";
		testDiagnosticsFor(xml, settings, //
				d(2, 21, 2, 24, XIncludeErrorCode.HrefSyntacticallyInvalid));
	}

	public static void testDiagnosticsFor(String xml, ContentModelSettings settings,
			Diagnostic... expected) {
		XMLAssert.testDiagnosticsFor(xml, null, null, null, true, settings, expected);
	}
}
