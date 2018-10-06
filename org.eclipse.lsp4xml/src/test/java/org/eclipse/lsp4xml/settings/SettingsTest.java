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
package org.eclipse.lsp4xml.settings;

import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.ContentModelSettings;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Tests for settings.
 */
public class SettingsTest {

	@Test
	public void initializationOptionsSettings() {
		String json = "{\r\n" + //
				"	\"settings\": {\r\n" + //
				// Content model settings
				"		\"fileAssociations\": [\r\n" + //
				"			{\r\n" + //
				"				\"systemId\": \"src\\\\test\\\\resources\\\\xsd\\\\spring-beans-3.0.xsd\",\r\n" + //
				"				\"pattern\": \"**/test*.xml\"\r\n" + //
				"			},\r\n" + //
				"			{\r\n" + //
				"				\"systemId\": \"src\\\\test\\\\resources\\\\xsd\\\\projectDescription.xsd\",\r\n" + //
				"				\"pattern\": \"projectDescription.xml\"\r\n" + //
				"			}\r\n" + //
				"		],\r\n" + //
				"		\"catalogs\": [\r\n" + //
				"			\"src\\\\test\\\\resources\\\\catalogs\\\\catalog.xml\"\r\n" + //
				"		],\r\n" + //
				// Client (commons) settings				
				"		\"format\": {\r\n" + //
				"			\"tabSize\": 10,\r\n" + //
				"			\"insertSpaces\": false,\r\n" + //
				"			\"splitAttributes\": true,\r\n" + //
				"			\"joinCDATALines\": true,\r\n" + //
				"			\"formatComments\": true,\r\n" + //
				"			\"joinCommentLines\": true\r\n" + //
				"		}\r\n" + "	}\r\n" + "}";
		// Emulate InitializeParams#getInitializationOptions() object created as
		// JSONObject when XMLLanguageServer#initialize(InitializeParams params) is
		// called
		InitializeParams params = createInitializeParams(json);
		Object initializationOptionsSettings = InitializationOptionsSettings.getSettings(params);

		// Test client commons settings
		XMLClientSettings settings = XMLClientSettings.getSettings(initializationOptionsSettings);
		Assert.assertNotNull(settings);

		// Test content model extension settings
		ContentModelSettings cmSettings = ContentModelSettings.getSettings(initializationOptionsSettings);
		Assert.assertNotNull(cmSettings);
		// Catalog
		Assert.assertNotNull(cmSettings.getCatalogs());
		Assert.assertEquals(1, cmSettings.getCatalogs().length);
		Assert.assertEquals("src\\test\\resources\\catalogs\\catalog.xml", cmSettings.getCatalogs()[0]);
		// File associations
		Assert.assertNotNull(cmSettings.getFileAssociations());
		Assert.assertEquals(2, cmSettings.getFileAssociations().length);
		Assert.assertEquals("src\\test\\resources\\xsd\\spring-beans-3.0.xsd",
				cmSettings.getFileAssociations()[0].getSystemId());
		Assert.assertEquals("**/test*.xml", cmSettings.getFileAssociations()[0].getPattern());
	}

	private static InitializeParams createInitializeParams(String json) {
		InitializeParams initializeParams = new InitializeParams();
		Object initializationOptions = new Gson().fromJson(json, JsonObject.class);
		initializeParams.setInitializationOptions(initializationOptions);
		return initializeParams;
	}

	@Test
	public void formatSettings() {
		XMLFormattingOptions sharedXMLFormattingOptions = new XMLFormattingOptions();
		sharedXMLFormattingOptions.setTabSize(10);
		sharedXMLFormattingOptions.setInsertSpaces(true);
		sharedXMLFormattingOptions.setJoinCommentLines(true);

		// formatting options coming from request
		FormattingOptions formattingOptions = new FormattingOptions();
		formattingOptions.setTabSize(5);

		XMLFormattingOptions xmlFormattingOptions = new XMLFormattingOptions(formattingOptions);
		Assert.assertEquals(5, xmlFormattingOptions.getTabSize()); // value coming from the request formattingOptions
		Assert.assertFalse(xmlFormattingOptions.isInsertSpaces()); // formattingOptions doesn't defines insert spaces
																	// flag

		Assert.assertFalse(xmlFormattingOptions.isJoinCommentLines());

		// merge with shared sharedXMLFormattingOptions (formatting settings created in
		// the InitializeParams
		xmlFormattingOptions.merge(sharedXMLFormattingOptions);
		Assert.assertEquals(5, xmlFormattingOptions.getTabSize()); // tab size is kept to 5 (and not updated with
																	// shared value 10), because request
																	// formattingOptions defines it.
		Assert.assertTrue(xmlFormattingOptions.isInsertSpaces()); // insert spaces is to true (shared value), because
																	// request formatting options doesn't define it.
		Assert.assertTrue(xmlFormattingOptions.isJoinCommentLines());
	}
}
