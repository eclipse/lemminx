/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.rng;

import static org.eclipse.lemminx.XMLAssert.pd;
import static org.eclipse.lemminx.XMLAssert.r;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.extensions.contentmodel.participants.XMLSchemaErrorCode;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationRootSettings;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

/**
 * Tests to make sure that *.rng associates with relaxng.xsd properly
 *
 * @author datho7561
 */
public class RNGUriResolverTest extends AbstractCacheBasedTest {

	@Test
	public void testValidatesRNGWithBadElement() throws Exception {

		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		validation.setResolveExternalEntities(true);

		XMLLanguageService ls = new XMLLanguageService();
		ls.initializeIfNeeded();
		// Disable download
		ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
		contentModelManager.setDownloadExternalResources(false);

		String xsd = "<asdf xmlns=\"http://relaxng.org/ns/structure/1.0\"></asdf>";

		String fileURI = "test.rng";
		Diagnostic d = new Diagnostic(r(0, 1, 0, 5), "cvc-elt.1.a: Cannot find the declaration of element 'asdf'.",
				DiagnosticSeverity.Error, "xml", XMLSchemaErrorCode.cvc_elt_1_a.getCode());

		XMLAssert.testPublishDiagnosticsFor(xsd, fileURI, validation, ls, pd(fileURI, d));
	}

	@Test
	public void testValidatesRNGWithMissingNamespace() throws Exception {

		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		validation.setResolveExternalEntities(true);

		XMLLanguageService ls = new XMLLanguageService();
		ls.initializeIfNeeded();
		// Disable download
		ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
		contentModelManager.setDownloadExternalResources(false);

		String xsd = "<grammar></grammar>";

		String fileURI = "test.rng";
		Diagnostic d1 = new Diagnostic(r(0, 1, 0, 8),
				"TargetNamespace.2: Expecting no namespace, but the schema document has a target namespace of 'http://relaxng.org/ns/structure/1.0'.",
				DiagnosticSeverity.Error, "xml", XMLSchemaErrorCode.TargetNamespace_2.getCode());

		Diagnostic d2 = new Diagnostic(r(0, 1, 0, 8), "cvc-elt.1.a: Cannot find the declaration of element 'grammar'.",
				DiagnosticSeverity.Error, "xml", XMLSchemaErrorCode.cvc_elt_1_a.getCode());

		XMLAssert.testPublishDiagnosticsFor(xsd, fileURI, validation, ls, pd(fileURI, d1, d2));
	}

	@Test
	public void testValidatesRNGThatsValid() throws Exception {

		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		validation.setResolveExternalEntities(true);

		XMLLanguageService ls = new XMLLanguageService();
		ls.initializeIfNeeded();
		// Disable download
		ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
		contentModelManager.setDownloadExternalResources(false);

		String xsd = "<grammar xmlns=\"http://relaxng.org/ns/structure/1.0\"></grammar>";

		String fileURI = "test.rng";

		XMLAssert.testPublishDiagnosticsFor(xsd, fileURI, validation, ls, pd(fileURI));
	}

	@Test
	public void testValidatesRNGThatsValidWithNamespaceInPrefix() throws Exception {

		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		validation.setResolveExternalEntities(true);

		XMLLanguageService ls = new XMLLanguageService();
		ls.initializeIfNeeded();
		// Disable download
		ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
		contentModelManager.setDownloadExternalResources(false);

		// FIXME: namespace is not handled correctly

		// https://relaxng.org/tutorial-20011203.html
		String xsd = "<rng:element name=\"addressBook\" xmlns:rng=\"http://relaxng.org/ns/structure/1.0\">\n" + //
				"<rng:zeroOrMore>" + //
				"<rng:element name=\"card\">\n" + //
				"<rng:element name=\"name\">\n" + //
				"<rng:text/>\n" + //
				"</rng:element>\n" + //
				"<rng:element name=\"email\">\n" + //
				"<rng:text/>\n" + //
				"</rng:element>\n" + //
				"</rng:element>\n" + //
				"</rng:zeroOrMore>\n" + //
				"</rng:element>\n";

		String fileURI = "test.rng";

		XMLAssert.testPublishDiagnosticsFor(xsd, fileURI, validation, ls, pd(fileURI));
	}

}
