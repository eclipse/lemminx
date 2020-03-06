/**
 *  Copyright (c) 2018 Angelo ZERR
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
package org.eclipse.lemminx.settings;

import static java.io.File.separator;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.eclipse.lemminx.XMLLanguageServer;
import org.eclipse.lemminx.XMLTextDocumentService;
import org.eclipse.lemminx.client.CodeLensKind;
import org.eclipse.lemminx.client.ExtendedClientCapabilities;
import org.eclipse.lemminx.extensions.contentmodel.settings.ContentModelSettings;
import org.eclipse.lemminx.settings.capabilities.InitializationOptionsExtendedClientCapabilities;
import org.eclipse.lemminx.utils.FilesUtils;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for settings.
 */
public class SettingsTest {

	private static String testFolder = "TestXMLCacheFolder";
	private static String targetTestFolder = "target" + separator + "generated-test-sources";

	@After
	public void cleanup() {
		String path = System.getProperty("user.dir") + separator + targetTestFolder + separator + testFolder;

		File f = new File(path);
		if (f.exists()) {
			f.delete();
		}
	}
	// @formatter:off
	private final String json = 
	"{\r\n" +
	"	\"settings\": {\r\n" + //
	// Content model settings
	"		\"xml\": {\r\n" + 
	"			\"fileAssociations\": [\r\n" + //
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
	"			],\r\n" + //
	"			\"validation\": {\r\n" + //
	"				\"enabled\": true,\r\n" + //
	"				\"schema\": false\r\n" + //
	"			},\r\n" + //
	// Client (commons) settings
	"			\"format\": {\r\n" + //
	"				\"tabSize\": 10,\r\n" + //
	"				\"insertSpaces\": false,\r\n" + //
	"				\"splitAttributes\": true,\r\n" + //
	"				\"joinCDATALines\": true,\r\n" + //
	"				\"formatComments\": true,\r\n" + //
	"				\"joinCommentLines\": true,\r\n" + //
	"				\"quotations\": " + XMLFormattingOptions.DOUBLE_QUOTES_VALUE + "\r\n" + //
	"			},\r\n" + 
	"			\"server\": {\r\n" + //
	"				\"workDir\": \"~/" + testFolder + "/Nested\"\r\n" + //
	"			},\r\n" + 
	"			\"symbols\": {\r\n" + //
	"				\"enabled\": true,\r\n" + //
	"				\"excluded\": [\"**\\\\*.xsd\", \"**\\\\*.xml\"]\r\n" + //
	"			}\r\n" + 
	"		}\r\n" + 
	"	}\r\n" +
	"	,\"extendedClientCapabilities\": {\r\n" + 
	"      \"codeLens\": {\r\n" + 
	"        \"codeLensKind\": {\r\n" + 
	"          \"valueSet\": [\r\n" + 
	"            \"references\"\r\n" + 
	"          ]\r\n" + 
	"        }\r\n" + 
	"      }\r\n" + 
	"   }" +	
	"}";
	// @formatter:on

	@Test
	public void initializationOptionsSettings() {

		// Emulate InitializeParams#getInitializationOptions() object created as
		// JSONObject when XMLLanguageServer#initialize(InitializeParams params) is
		// called
		InitializeParams params = createInitializeParams(json);
		Object initializationOptionsSettings = InitializationOptionsSettings.getSettings(params);

		// Test client commons settings
		initializationOptionsSettings = AllXMLSettings.getAllXMLSettings(initializationOptionsSettings);
		XMLGeneralClientSettings settings = XMLGeneralClientSettings.getGeneralXMLSettings(initializationOptionsSettings);
		assertNotNull(settings);
		// Server
		assertEquals("~/" + testFolder + "/Nested", settings.getServer().getWorkDir());

		// Test content model extension settings
		ContentModelSettings cmSettings = ContentModelSettings.getContentModelXMLSettings(initializationOptionsSettings);
		assertNotNull(cmSettings);
		// Catalog
		assertNotNull(cmSettings.getCatalogs());
		assertEquals(1, cmSettings.getCatalogs().length);
		assertEquals("src\\test\\resources\\catalogs\\catalog.xml", cmSettings.getCatalogs()[0]);
		// File associations
		assertNotNull(cmSettings.getFileAssociations());
		assertEquals(2, cmSettings.getFileAssociations().length);
		assertEquals("src\\test\\resources\\xsd\\spring-beans-3.0.xsd", cmSettings.getFileAssociations()[0].getSystemId());
		assertEquals("**/test*.xml", cmSettings.getFileAssociations()[0].getPattern());
		// Diagnostics
		assertNotNull(cmSettings.getValidation());
		assertEquals(true, cmSettings.getValidation().isEnabled());
		assertEquals(false, cmSettings.getValidation().isSchema());
		// Symbols
		assertNotNull(settings.getSymbols());
		assertEquals(true, settings.getSymbols().isEnabled());
		assertArrayEquals(new String[]{"**\\*.xsd", "**\\*.xml"}, settings.getSymbols().getExcluded());

	}

	private static InitializeParams createInitializeParams(String json) {
		InitializeParams initializeParams = new InitializeParams();
		Object initializationOptions = new Gson().fromJson(json, JsonObject.class);
		initializeParams.setInitializationOptions(initializationOptions);
		return initializeParams;
	}

	@Test
	public void formatSettings() {
		XMLFormattingOptions sharedXMLFormattingOptions = new XMLFormattingOptions(true);
		sharedXMLFormattingOptions.setTabSize(10);
		sharedXMLFormattingOptions.setInsertSpaces(true);
		sharedXMLFormattingOptions.setJoinCommentLines(true);
		sharedXMLFormattingOptions.setQuotations(XMLFormattingOptions.SINGLE_QUOTES_VALUE);

		// formatting options coming from request
		FormattingOptions formattingOptions = new FormattingOptions();
		formattingOptions.setTabSize(5);
		formattingOptions.setInsertSpaces(false);

		XMLFormattingOptions xmlFormattingOptions = new XMLFormattingOptions(formattingOptions);

		xmlFormattingOptions.setQuotations("InvalidValue"); // set a value that is not recognized

		assertEquals(5, xmlFormattingOptions.getTabSize()); // value coming from the request formattingOptions
		assertFalse(xmlFormattingOptions.isInsertSpaces()); // formattingOptions doesn't defines insert spaces
		// flag

		assertFalse(xmlFormattingOptions.isJoinCommentLines());// Since default for JoinCommentLines is False

		// Assumes the "InvalidValue" will be overridden
		assertTrue(xmlFormattingOptions.getQuotations().equals(XMLFormattingOptions.DOUBLE_QUOTES_VALUE));

		// merge with shared sharedXMLFormattingOptions (formatting settings created in
		// the InitializeParams
		xmlFormattingOptions.merge(sharedXMLFormattingOptions);
		assertEquals(5, xmlFormattingOptions.getTabSize()); // tab size is kept as 5 (and not updated with
		// shared value 10), because only the request's
		// formattingOptions object is allowed to define it.
		assertFalse(xmlFormattingOptions.isInsertSpaces()); // insert spaces is kept as false because only the
																												// request's
		// formattingOptions object is allowed to define it.
		assertTrue(xmlFormattingOptions.isJoinCommentLines());

		assertTrue(xmlFormattingOptions.getQuotations().equals(XMLFormattingOptions.SINGLE_QUOTES_VALUE));
	}

	@Test
	public void cachePathSettings() {
		// Emulate InitializeParams#getInitializationOptions() object created as
		// JSONObject when XMLLanguageServer#initialize(InitializeParams params) is
		// called

		InitializeParams params = createInitializeParams(json);
		Object initializationOptionsSettings = InitializationOptionsSettings.getSettings(params);
		XMLLanguageServer languageServer = new XMLLanguageServer();

		String originalUserHome = System.getProperty("user.home");
		String userDir = System.getProperty("user.dir");
		try {
			System.setProperty("user.home", userDir + separator + targetTestFolder); // .../org.eclipse.lemminx/target/generated-test-sources/

			languageServer.updateSettings(initializationOptionsSettings);

			// Ensure the expanded absolute path is being used.
			assertEquals(System.getProperty("user.home") + separator + testFolder + separator + "Nested",
					FilesUtils.getCachePathSetting());
		} catch (Exception e) {
			fail();
		} finally {
			//Reset static cache path
			FilesUtils.setCachePathSetting(null);
			System.setProperty("user.home", originalUserHome);
		}
	}

	@Test
	public void symbolSettingsTest() {
		//Tests that when the settings are updated the shared settings are also updated correctly

		InitializeParams params = createInitializeParams(json);
		Object initializationOptionsSettings = InitializationOptionsSettings.getSettings(params);
		XMLLanguageServer languageServer = new XMLLanguageServer();
		languageServer.updateSettings(initializationOptionsSettings); // This should set/update the sharedSettings

		XMLExcludedSymbolFile xsdFile = new XMLExcludedSymbolFile("**\\*.xsd"); 
		XMLExcludedSymbolFile xmlFile = new XMLExcludedSymbolFile("**\\*.xml");
		XMLExcludedSymbolFile[] expectedExcludedFiles = new XMLExcludedSymbolFile[] {xsdFile, xmlFile};

		XMLExcludedSymbolFile[] actualExpectedFiles = languageServer.getSettings().getSymbolSettings().getExcludedFiles();
		assertArrayEquals(expectedExcludedFiles, actualExpectedFiles);

		XMLTextDocumentService textDocumentService = (XMLTextDocumentService) languageServer.getTextDocumentService();
	}

	@Test
	public void extendedClientCapabilitiesTest() {
		InitializeParams params = createInitializeParams(json);
		ExtendedClientCapabilities clientCapabilities = InitializationOptionsExtendedClientCapabilities
				.getExtendedClientCapabilities(params);
		Assert.assertNotNull(clientCapabilities);
		Assert.assertNotNull(clientCapabilities.getCodeLens());
		Assert.assertNotNull(clientCapabilities.getCodeLens().getCodeLensKind());
		Assert.assertNotNull(clientCapabilities.getCodeLens().getCodeLensKind().getValueSet());
		Assert.assertEquals(1, clientCapabilities.getCodeLens().getCodeLensKind().getValueSet().size());
		Assert.assertEquals(CodeLensKind.References,
				clientCapabilities.getCodeLens().getCodeLensKind().getValueSet().get(0));
	}
}
