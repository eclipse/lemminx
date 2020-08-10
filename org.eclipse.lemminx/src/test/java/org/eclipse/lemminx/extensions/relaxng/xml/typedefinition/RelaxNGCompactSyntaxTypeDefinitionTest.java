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
package org.eclipse.lemminx.extensions.relaxng.xml.typedefinition;

import static org.eclipse.lemminx.XMLAssert.ll;
import static org.eclipse.lemminx.XMLAssert.r;
import static org.eclipse.lemminx.XMLAssert.testTypeDefinitionFor;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.junit.jupiter.api.Test;

/**
 * RelaxNG type definition tests with compact syntax.
 *
 */
public class RelaxNGCompactSyntaxTypeDefinitionTest extends AbstractCacheBasedTest {

	@Test
	public void toRootElementDeclaration() throws BadLocationException {
		String xmlFile = "src/test/resources/relaxng/addressBook.xml";
		String xsdFile = "addressBook.rnc";

		String xml = "<?xml-model href=\"addressBook.rnc\" ?>\r\n" + //
				"<addre|ssBook>\r\n" + //
				"\r\n" + //
				"</addressBook>";
		XMLLanguageService xmlLanguageService = new XMLLanguageService();
		String targetSchemaURI = xmlLanguageService.getResolverExtensionManager().resolve(xmlFile, null, xsdFile);
		testTypeDefinitionFor(xmlLanguageService, xml, xmlFile, //
				ll(targetSchemaURI, r(1, 1, 1, 12), r(0, 0, 0, 1)));
	}

	@Test
	public void toElementDeclaration() throws BadLocationException {
		String xmlFile = "src/test/resources/relaxng/addressBook.xml";
		String xsdFile = "addressBook.rnc";

		String xml = "<?xml-model href=\"addressBook.rnc\" ?>\r\n" + //
				"<addressBook>\r\n" + //
				"  <car|d></card>\r\n" + //
				"</addressBook>";
		XMLLanguageService xmlLanguageService = new XMLLanguageService();
		String targetSchemaURI = xmlLanguageService.getResolverExtensionManager().resolve(xmlFile, null, xsdFile);
		testTypeDefinitionFor(xmlLanguageService, xml, xmlFile, //
				ll(targetSchemaURI, r(2, 3, 2, 7), r(1, 2, 1, 3)));
	}

	@Test
	public void toAttributeDeclaration() throws BadLocationException {
		String xmlFile = "src/test/resources/relaxng/addressBook.xml";
		String xsdFile = "addressBook.rnc";

		String xml = "<?xml-model href=\"addressBook.rnc\" ?>\r\n" + //
				"<addressBook>\r\n" + //
				"  <card i|d=\"\"></card>\r\n" + //
				"</addressBook>";
		XMLLanguageService xmlLanguageService = new XMLLanguageService();
		String targetSchemaURI = xmlLanguageService.getResolverExtensionManager().resolve(xmlFile, null, xsdFile);
		testTypeDefinitionFor(xmlLanguageService, xml, xmlFile, //
				ll(targetSchemaURI, r(2, 8, 2, 13), r(5, 4, 5, 5)));
	}
}
