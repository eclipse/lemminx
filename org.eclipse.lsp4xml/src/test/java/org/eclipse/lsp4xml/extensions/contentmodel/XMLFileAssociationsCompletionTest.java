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

import static org.eclipse.lsp4xml.XMLAssert.c;

import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.XMLFileAssociation;
import org.eclipse.lsp4xml.extensions.contentmodel.uriresolver.XMLFileAssociationResolverExtension;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtensionManager;
import org.junit.Before;
import org.junit.Test;

/**
 * XML file associations tests.
 */
public class XMLFileAssociationsCompletionTest {

	@Before
	public void initializeFileAssociations() {
		XMLFileAssociation xmlSchemaFormatAssociation = new XMLFileAssociation();
		xmlSchemaFormatAssociation.setPattern("**/*.Format.ps1xml");
		xmlSchemaFormatAssociation.setSystemId("src/test/resources/xsd/Format.xsd");
		XMLFileAssociationResolverExtension resolver = new XMLFileAssociationResolverExtension();
		XMLFileAssociation[] fileAssociations = new XMLFileAssociation[] { xmlSchemaFormatAssociation };
		resolver.setFileAssociations(fileAssociations);
		URIResolverExtensionManager.getInstance().registerResolver(resolver);
	}

	@Test
	public void completionOnRoot() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"  <|";
		XMLAssert.testCompletionFor(xml, null, "file:///test/Test.Format.ps1xml", null,
				c("Configuration", "<Configuration></Configuration>"));
		xml = "|";
		XMLAssert.testCompletionFor(xml, null, "file:///test/Test.Format.ps1xml", null,
				c("Configuration", "<Configuration></Configuration>"));
		xml = " |";
		XMLAssert.testCompletionFor(xml, null, "file:///test/Test.Format.ps1xml", null,
				c("Configuration", "<Configuration></Configuration>"));
	}

	@Test
	public void completionAfterRoot() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<Configuration>\r\n" + //
				"  <|";
		XMLAssert.testCompletionFor(xml, null, "file:///test/Test.Format.ps1xml", null,
				c("Controls", "<Controls></Controls>"), //
				c("DefaultSettings", "<DefaultSettings></DefaultSettings>"), //
				c("SelectionSets", "<SelectionSets></SelectionSets>"),
				c("ViewDefinitions", "<ViewDefinitions></ViewDefinitions>"));
		
		xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<Configuration>\r\n" + //
				"  |";
		XMLAssert.testCompletionFor(xml, null, "file:///test/Test.Format.ps1xml", null,
				c("Controls", "<Controls></Controls>"), //
				c("DefaultSettings", "<DefaultSettings></DefaultSettings>"), //
				c("SelectionSets", "<SelectionSets></SelectionSets>"),
				c("ViewDefinitions", "<ViewDefinitions></ViewDefinitions>"));
	}
}
