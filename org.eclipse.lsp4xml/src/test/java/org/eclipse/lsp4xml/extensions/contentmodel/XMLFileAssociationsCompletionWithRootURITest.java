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
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * XML file associations tests.
 */
public class XMLFileAssociationsCompletionWithRootURITest {

	private static XMLFileAssociationResolverExtension resolver;

	@BeforeClass
	public static void initializeFileAssociations() {
		resolver = new XMLFileAssociationResolverExtension();
		URIResolverExtensionManager.getInstance().registerResolver(resolver);
	}

	@Test
	public void rootURIEndsWithSlash() throws BadLocationException {
		// Use root URI which ends with slash
		resolver.setRootUri("src/test/resources/xsd/");
		resolver.setFileAssociations(createAssociations());

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
	public void rootURIEndsWithNoSlash() throws BadLocationException {
		// Use root URI which ends with slash
		resolver.setRootUri("src/test/resources/xsd");
		resolver.setFileAssociations(createAssociations());

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

	private static XMLFileAssociation[] createAssociations() {
		XMLFileAssociation xmlSchemaFormatAssociation = new XMLFileAssociation();
		xmlSchemaFormatAssociation.setPattern("**/*.Format.ps1xml");
		xmlSchemaFormatAssociation.setSystemId("Format.xsd");
		return new XMLFileAssociation[] { xmlSchemaFormatAssociation };
	}
}
