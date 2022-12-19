/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.relaxng.xml.diagnostics;

import static org.eclipse.lemminx.XMLAssert.d;

import java.io.File;
import java.util.function.Consumer;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.extensions.contentmodel.settings.ContentModelSettings;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLFileAssociation;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationRootSettings;
import org.eclipse.lemminx.extensions.relaxng.xml.validator.RelaxNGErrorCode;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lsp4j.Diagnostic;
import org.junit.jupiter.api.Test;

/**
 * XML Validation tests with RelaxNG XML syntax by using XML file
 * association.
 *
 */
public class XMLFileAssociationRNGDiagnosticsTest extends AbstractCacheBasedTest {

	@Test
	public void valid() throws Exception {
		String xml = "<addressBook>\r\n" + //
				"  <card>\r\n" + //
				"    <name>John Smith</name>\r\n" + //
				"    <email>js@example.com</email>\r\n" + //
				"  </card>\r\n" + //
				"  <card>\r\n" + //
				"    <name>Fred Bloggs</name>\r\n" + //
				"    <email>fb@example.net</email>\r\n" + //
				"  </card>\r\n" + //
				"</addressBook>";
		testDiagnosticsFor(xml, "file:///test/addressBook.xml");
	}

	@Test
	public void unkwown_element() throws Exception {
		String xml = "<addressBook>\r\n" + //
				"  <card>\r\n" + //
				"    <nameXXX>John Smith</nameXXX>\r\n" + // unknown_element -> element "nameXXX" not allowed anywhere;
															// expected element "name"
				"    <email>js@example.com</email>\r\n" + // unexpected_element_required_element_missing -> "element
															// "email" not allowed yet; missing required element "name""
				"  </card>\r\n" + //
				"  <card>\r\n" + //
				"    <name>Fred Bloggs</name>\r\n" + //
				"    <email>fb@example.net</email>\r\n" + //
				"  </card>\r\n" + //
				"</addressBook>";
		testDiagnosticsFor(xml, "file:///test/addressBook.xml", //
				d(2, 5, 12, RelaxNGErrorCode.unknown_element), //
				d(3, 5, 10, RelaxNGErrorCode.unexpected_element_required_element_missing));
	}

	@Test
	public void xincludeInXMLEnabled() throws Exception {
		ContentModelSettings settings = new ContentModelSettings();
		XMLValidationRootSettings validation = new XMLValidationRootSettings();
		validation.getXInclude().setEnabled(true);
		settings.setValidation(validation);

		String xml = "<foo2 xmlns:xi=\"http://www.w3.org/2001/XInclude\" >\r\n"
				+ "	<xi:include href=\"bar2.xml\" />\r\n"
				+ "</foo2>";
		testDiagnosticsFor(xml, new File("src/test/resources/relaxng/xinclude/foo.xml").toURI().toString(), settings);

		xml = "<foo2 xmlns:xi=\"http://www.w3.org/2001/XInclude\" >\r\n"
				+ "	<xi:include href=\"bar.xml\" />\r\n"
				+ "</foo2>";
		testDiagnosticsFor(xml, new File("src/test/resources/relaxng/xinclude/foo.xml").toURI().toString(), settings, //
				d(0, 1, 5, null),
				d(0, 1, 5, RelaxNGErrorCode.incomplete_element_required_element_missing));

		
		xml = "<foo xmlns:xi=\"http://www.w3.org/2001/XInclude\" >\r\n"
				// + " <xi:include href=\"bar.xml\" />\r\n"
				+ "</foo>";
		testDiagnosticsFor(xml, new File("src/test/resources/relaxng/xinclude/foo.xml").toURI().toString(), settings, //
				d(0, 1, 4, RelaxNGErrorCode.incomplete_element_required_element_missing));
	}

	private static void testDiagnosticsFor(String xml, String fileURI, Diagnostic... expected) {
		ContentModelSettings settings = new ContentModelSettings();
		settings.setValidation(new XMLValidationRootSettings());
		testDiagnosticsFor(xml, fileURI, settings, expected);
	}

	private static void testDiagnosticsFor(String xml, String fileURI, ContentModelSettings settings,
			Diagnostic... expected) {
		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			contentModelManager
					.setFileAssociations(createXMLFileAssociation("src/test/resources/relaxng/"));
		};

		XMLAssert.testDiagnosticsFor(xml, null, configuration, fileURI, true, settings,
				expected);
	}

	private static XMLFileAssociation[] createXMLFileAssociation(String baseSystemId) {
		XMLFileAssociation addressBook = new XMLFileAssociation();
		addressBook.setPattern("**/addressBook.xml");
		addressBook.setSystemId(baseSystemId + "addressBook_v3.rng");
		XMLFileAssociation foo = new XMLFileAssociation();
		foo.setPattern("**/foo.xml");
		foo.setSystemId(baseSystemId + "/xinclude/foo.rng");
		return new XMLFileAssociation[] { addressBook, foo };
	}

}
