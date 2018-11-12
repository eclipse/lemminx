/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.contentmodel;

import static org.eclipse.lsp4xml.XMLAssert.d;

import java.util.function.Consumer;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.XMLSchemaErrorCode;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.XMLFileAssociation;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.junit.Test;

/**
 * XML file associations diagnostics tests.
 */
public class XMLFileAssociationsDiagnosticsTest {

	@Test
	public void validationOnRoot() throws BadLocationException {
		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			// Use root URI which ends with slash
			contentModelManager.setRootURI("src/test/resources/xsd/");
			contentModelManager.setFileAssociations(createAssociations(""));
		};

		// Use Format.xsd which defines Configuration as root element
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"  <Bad-Root></Bad-Root>";
		testDiagnosticsFor(xml, "file:///test/Test.Format.ps1xml", configuration,
				d(1, 3, 1, 11, XMLSchemaErrorCode.cvc_elt_1_a));

		xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"  <Configuration></Configuration>";
		testDiagnosticsFor(xml, "file:///test/Test.Format.ps1xml", configuration);

	}

	@Test
	public void validationOnRootWithRequiredAttr() throws BadLocationException {
		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			// Use root URI which ends with slash
			contentModelManager.setRootURI("src/test/resources/xsd/");
			contentModelManager.setFileAssociations(createAssociations(""));
		};

		// Use resources.xsd which defines resources as root element and @variant as
		// required attribute
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"  <Bad-Root></Bad-Root>";
		testDiagnosticsFor(xml, "file:///test/resources.xml", configuration,
				d(1, 3, 1, 11, XMLSchemaErrorCode.cvc_elt_1_a));

		xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"  <resources></resources>"; // <- error @variant is required
		testDiagnosticsFor(xml, "file:///test/resources.xml", configuration,
				d(1, 3, 1, 12, XMLSchemaErrorCode.cvc_complex_type_4));

		xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"  <resources variant=\"\" ></resources>";
		testDiagnosticsFor(xml, "file:///test/resources.xml", configuration);

	}

	@Test
	public void validationAfterRoot() throws BadLocationException {
		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			// Use root URI which ends with slash
			contentModelManager.setRootURI("src/test/resources/xsd/");
			contentModelManager.setFileAssociations(createAssociations(""));
		};

		// Use resources.xsd which defines resources as root element and @variant as
		// required attribute
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<resources variant=\"\" >\r\n" + //
				"  <resource>\r\n" + // <-- error @name is required
				"  </resource>\r\n" + //
				"</resources>";
		testDiagnosticsFor(xml, "file:///test/resources.xml", configuration,
				d(2, 3, 2, 11, XMLSchemaErrorCode.cvc_complex_type_4));

	}

	private static XMLFileAssociation[] createAssociations(String baseSystemId) {
		XMLFileAssociation format = new XMLFileAssociation();
		format.setPattern("**/*.Format.ps1xml");
		format.setSystemId(baseSystemId + "Format.xsd");
		XMLFileAssociation resources = new XMLFileAssociation();
		resources.setPattern("**/*resources*.xml");
		resources.setSystemId(baseSystemId + "resources.xsd");
		return new XMLFileAssociation[] { format, resources };
	}

	private static void testDiagnosticsFor(String xml, String fileURI, Consumer<XMLLanguageService> configuration,
			Diagnostic... expected) {
		XMLAssert.testDiagnosticsFor(xml, null, configuration, fileURI, expected);
	}
}
