/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.extensions.contentmodel;

import static org.eclipse.lsp4xml.XMLAssert.c;
import static org.eclipse.lsp4xml.XMLAssert.te;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.ContentModelSettings;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.XMLValidationSettings.SchemaVersion;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.junit.Test;

/**
 * XSD completion tests.
 *
 */
public class XMLSchema11CompletionExtensionsTest {

	@Test
	public void xsall11() throws BadLocationException {
		// test with xs:all and xs:element maxOccurs="unbound" all.xsd 1.1
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<t:testDoc xmlns:t=\"test\"        \r\n" + //
				" xmlns:vc=\"http://www.w3.org/2007/XMLSchema-versioning\" \r\n" + //
				" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \r\n" + //
				" xsi:schemaLocation=\"test xsd/1.1/all.xsd\">  \r\n" + //
				"    <t:testContainer> \r\n" + //
				"\r\n" + //
				"|";
		//testCompletionFor(xml, c("t:testEle", te(7, 0, 7, 0, "<t:testEle></t:testEle>"), "t:testEle"), //
			//	c("t:testEleTwo", te(7, 0, 7, 0, "<t:testEleTwo></t:testEleTwo>"), "t:testEleTwo"));

		xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<t:testDoc xmlns:t=\"test\"        \r\n" + //
				" xmlns:vc=\"http://www.w3.org/2007/XMLSchema-versioning\" \r\n" + //
				" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \r\n" + //
				" xsi:schemaLocation=\"test xsd/1.1/all.xsd\">  \r\n" + //
				"    <t:testContainer> \r\n" + //
				"\r\n" + //
				"        <t:testEle></t:testEle>" + //
				"        <t:testEleTwo></t:testEleTwo>" + //
				"        |";
		testCompletionFor(xml, c("t:testEle", te(7, 76, 7, 76, "<t:testEle></t:testEle>"), "t:testEle"));
	}

	private void testCompletionFor(String xml, CompletionItem... expectedItems) throws BadLocationException {
		XMLLanguageService xmlLanguageService = new XMLLanguageService();
		XMLAssert.testCompletionFor(xmlLanguageService, xml, null, ls -> {
			ContentModelSettings settings = new ContentModelSettings();
			settings.setCatalogs(new String[] { "src/test/resources/catalogs/catalog.xml" });
			XMLValidationSettings problems = new XMLValidationSettings();
			problems.setSchemaVersion(SchemaVersion.V11.getVersion());
			settings.setValidation(problems);
			ls.doSave(new XMLAssert.SettingsSaveContext(settings));
		}, "src/test/resources/test.xml", null, true, expectedItems);
	}

}