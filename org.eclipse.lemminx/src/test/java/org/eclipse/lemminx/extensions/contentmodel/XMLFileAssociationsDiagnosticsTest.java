/**
 *  Copyright (c) 2018 Angelo ZERR.
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

import static org.eclipse.lemminx.XMLAssert.d;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.XMLAssert.SettingsSaveContext;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.extensions.contentmodel.participants.DTDErrorCode;
import org.eclipse.lemminx.extensions.contentmodel.participants.XMLSchemaErrorCode;
import org.eclipse.lemminx.extensions.contentmodel.settings.ContentModelSettings;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLFileAssociation;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lemminx.settings.AllXMLSettings;
import org.eclipse.lemminx.settings.InitializationOptionsSettings;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.InitializeParams;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * XML file associations diagnostics tests.
 */
public class XMLFileAssociationsDiagnosticsTest extends AbstractCacheBasedTest {

	// ------- XML file association with XSD xs:noNamespaceShemaLocation like

	@Test
	public void validationOnRoot() throws BadLocationException {
		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			// Use root URI which ends with slash
			contentModelManager.setRootURI("src/test/resources/xsd/");
			contentModelManager.setFileAssociations(createXSDAssociationsNoNamespaceSchemaLocationLike(""));
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
	public void testNoValidationOnFileAssociationNotChanged() throws BadLocationException {
		String json1 = "{\r\n" + //
				"	\"settings\": {\r\n" + //
				// Content model settings
				"		\"xml\": {\r\n" + "			\"fileAssociations\": [\r\n" + //
				"				{\r\n" + //
				"					\"systemId\": \"src\\\\test\\\\resources\\\\xsd\\\\spring-beans-3.0.xsd\",\r\n" + //
				"					\"pattern\": \"**/test*.xml\"\r\n" + //
				"				},\r\n" + //
				"				{\r\n" + //
				"					\"systemId\": \"src\\\\test\\\\resources\\\\xsd\\\\projectDescription.xsd\",\r\n" + //
				"					\"pattern\": \"projectDescription.xml\"\r\n" + //
				"				}\r\n" + //
				"			],\r\n" + //
				"			\"catalogs\": [\r\n" + //
				"				\"src\\\\test\\\\resources\\\\catalogs\\\\catalog.xml\"\r\n" + //
				"			]\r\n" + //
				"		}\r\n" + "	}\r\n" + "}";

		// Emulate InitializeParams#getInitializationOptions() object created as
		// JSONObject when XMLLanguageServer#initialize(InitializeParams params) is
		// called
		InitializeParams params = createInitializeParams(json1);
		Object initializationOptionsSettings = InitializationOptionsSettings.getSettings(params);
		Object settings = AllXMLSettings.getAllXMLSettings(initializationOptionsSettings);

		// Create content model settings, which include fileAssociations
		ContentModelSettings cmSettings = ContentModelSettings.getContentModelXMLSettings(settings);

		SettingsSaveContext context = new SettingsSaveContext(settings);
		ContentModelPlugin cmPlugin = new ContentModelPlugin();
		// Initializes values in cmPlugin
		cmPlugin.start(null, new XMLExtensionsRegistry());
		// Set initial fileAssociations
		cmPlugin.contentModelManager.setFileAssociations(cmSettings.getFileAssociations());
		// Simulate an update of settings
		cmPlugin.doSave(context);
		// Try to set associations, should be false since they are the same
		boolean last = cmPlugin.contentModelManager.setFileAssociations(cmSettings.getFileAssociations());
		assertFalse(last);

	}

	@Test
	public void testValidationOnFileAssociationUpdate() throws BadLocationException {
		String json1 = "{\r\n" + //
				"	\"settings\": {\r\n" + //
				// Content model settings
				"		\"xml\": {\r\n" + "			\"fileAssociations\": [\r\n" + //
				"				{\r\n" + //
				"					\"systemId\": \"src\\\\test\\\\resources\\\\xsd\\\\spring-beans-3.0.xsd\",\r\n" + //
				"					\"pattern\": \"**/test*.xml\"\r\n" + //
				"				},\r\n" + //
				"				{\r\n" + //
				"					\"systemId\": \"src\\\\test\\\\resources\\\\xsd\\\\projectDescription.xsd\",\r\n" + //
				"					\"pattern\": \"projectDescription.xml\"\r\n" + //
				"				}\r\n" + //
				"			],\r\n" + //
				"			\"catalogs\": [\r\n" + //
				"				\"src\\\\test\\\\resources\\\\catalogs\\\\catalog.xml\"\r\n" + //
				"			]\r\n" + //
				"		}\r\n" + "	}\r\n" + "}";

		String json2 = "{\r\n" + //
				"	\"settings\": {\r\n" + //
				// Content model settings
				"		\"xml\": {\r\n" + "			\"fileAssociations\": [\r\n" + //
				"				{\r\n" + //
				"					\"systemId\": \"src\\\\test\\\\resources\\\\xsd\\\\spring-beans-6000.0.xsd\",\r\n" + // <-
																															// Changed
				"					\"pattern\": \"**/test*.xml\"\r\n" + //
				"				}\r\n" + //

				"			],\r\n" + //
				"			\"catalogs\": [\r\n" + //
				"				\"src\\\\test\\\\resources\\\\catalogs\\\\catalog.xml\"\r\n" + //
				"			]\r\n" + //
				"		}\r\n" + "	}\r\n" + "}";
		// Emulate InitializeParams#getInitializationOptions() object created as
		// JSONObject when XMLLanguageServer#initialize(InitializeParams params) is
		// called
		InitializeParams params = createInitializeParams(json1);
		Object initializationOptionsSettings = InitializationOptionsSettings.getSettings(params);
		Object settings = AllXMLSettings.getAllXMLSettings(initializationOptionsSettings);

		// Create content model settings, which include fileAssociations
		ContentModelSettings cmSettings = ContentModelSettings.getContentModelXMLSettings(settings);

		SettingsSaveContext context = new SettingsSaveContext(settings);
		ContentModelPlugin cmPlugin = new ContentModelPlugin();
		// Initalize values in cmPlugin
		cmPlugin.start(null, new XMLExtensionsRegistry());
		// Set initial fileAssociations
		cmPlugin.contentModelManager.setFileAssociations(cmSettings.getFileAssociations());
		// Simulate an update of settings
		cmPlugin.doSave(context);

		// Create cmSettings with new fileAssociations settings
		params = createInitializeParams(json2);
		initializationOptionsSettings = InitializationOptionsSettings.getSettings(params);
		settings = AllXMLSettings.getAllXMLSettings(initializationOptionsSettings);
		cmSettings = ContentModelSettings.getContentModelXMLSettings(settings);
		// Try to set associations, should be true since new fileAssociations were
		// detected
		boolean last = cmPlugin.contentModelManager.setFileAssociations(cmSettings.getFileAssociations());
		assertTrue(last);

	}

	@Test
	public void validationOnRootWithRequiredAttr() throws BadLocationException {
		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			// Use root URI which ends with slash
			contentModelManager.setRootURI("src/test/resources/xsd/");
			contentModelManager.setFileAssociations(createXSDAssociationsNoNamespaceSchemaLocationLike(""));
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
			contentModelManager.setFileAssociations(createXSDAssociationsNoNamespaceSchemaLocationLike(""));
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

	private static XMLFileAssociation[] createXSDAssociationsNoNamespaceSchemaLocationLike(String baseSystemId) {
		XMLFileAssociation format = new XMLFileAssociation();
		format.setPattern("**/*.Format.ps1xml");
		format.setSystemId(baseSystemId + "Format.xsd");
		XMLFileAssociation resources = new XMLFileAssociation();
		resources.setPattern("**/*resources*.xml");
		resources.setSystemId(baseSystemId + "resources.xsd");
		return new XMLFileAssociation[] { format, resources };
	}

	// ------- XML file association with XSD xs:schemaLocation like

	@Test
	public void validationWithExternalXSDAndNS() throws BadLocationException {
		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			contentModelManager.setFileAssociations(createXSDAssociationsSchemaLocationLike("src/test/resources/xsd/"));
		};
		String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\r\n" + //
				"	<XXX></XXX>\r\n" + // <- error
				"</project>";

		testDiagnosticsFor(xml, "file:///test/pom.xml", configuration,
				d(1, 2, 1, 5, XMLSchemaErrorCode.cvc_complex_type_2_4_a));
	}

	private static XMLFileAssociation[] createXSDAssociationsSchemaLocationLike(String baseSystemId) {
		XMLFileAssociation maven = new XMLFileAssociation();
		maven.setPattern("**/pom.xml");
		maven.setSystemId(baseSystemId + "maven-4.0.0.xsd");
		return new XMLFileAssociation[] { maven };
	}

	@Test
	public void invalidXSD() throws BadLocationException {
		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			contentModelManager.setFileAssociations(createXSDAssociationsSchemaLocationLike("invalid/"));
		};
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				+ "<foo>\r\n"
				+ "    <bar>\r\n"
				+ "        <meh></meh>\r\n"
				+ "    </bar>\r\n"
				+ "</foo>";
		testDiagnosticsFor(xml, "file:///test/pom.xml", configuration,
				d(1, 1, 1, 4, XMLSchemaErrorCode.schema_reference_4),
				d(1, 1, 1, 4, XMLSchemaErrorCode.cvc_elt_1_a));
	}

	// ------- XML file association with DTD

	@Test
	public void validationWithExternalDTD() throws BadLocationException {
		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			contentModelManager.setFileAssociations(createDTDAssociations("src/test/resources/dtd/"));
		};
		String xml = "<root />";
		testDiagnosticsFor(xml, "file:///test/web.xml", configuration,
				d(0, 1, 0, 5, DTDErrorCode.MSG_ELEMENT_NOT_DECLARED));

		xml = "<web-app><bad-element /></web-app>";
		testDiagnosticsFor(xml, "file:///test/web.xml", configuration, //
				d(0, 10, 0, 21, DTDErrorCode.MSG_ELEMENT_NOT_DECLARED), //
				d(0, 1, 0, 8, DTDErrorCode.MSG_CONTENT_INVALID));
	}

	private static XMLFileAssociation[] createDTDAssociations(String baseSystemId) {
		XMLFileAssociation webApp = new XMLFileAssociation();
		webApp.setPattern("web.xml");
		webApp.setSystemId(baseSystemId + "web-app_2_3.dtd");
		return new XMLFileAssociation[] { webApp };
	}

	private static void testDiagnosticsFor(String xml, String fileURI, Consumer<XMLLanguageService> configuration,
			Diagnostic... expected) {
		XMLAssert.testDiagnosticsFor(xml, null, configuration, fileURI, expected);
	}

	private static InitializeParams createInitializeParams(String json) {
		InitializeParams initializeParams = new InitializeParams();
		Object initializationOptions = new Gson().fromJson(json, JsonObject.class);
		initializeParams.setInitializationOptions(initializationOptions);
		return initializeParams;
	}
}
