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
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.XMLFileAssociation;
import org.junit.Before;
import org.junit.Test;

/**
 * XML file associations completion tests.
 */
public class XMLFileAssociationsCompletionTest {

	@Before
	public void initializeFileAssociations() {
		ContentModelManager.getInstance().setRootURI(null);
	}

	@Test
	public void completionOnRoot() throws BadLocationException {
		ContentModelManager.getInstance().setFileAssociations(createAssociations("src/test/resources/xsd/"));

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
		ContentModelManager.getInstance().setFileAssociations(createAssociations("src/test/resources/xsd/"));

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

	@Test
	public void rootURIEndsWithSlash() throws BadLocationException {
		// Use root URI which ends with slash
		ContentModelManager.getInstance().setRootURI("src/test/resources/xsd/");
		ContentModelManager.getInstance().setFileAssociations(createAssociations(""));

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
		ContentModelManager.getInstance().setRootURI("src/test/resources/xsd");
		ContentModelManager.getInstance().setFileAssociations(createAssociations(""));

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

	private static XMLFileAssociation[] createAssociations(String baseSystemId) {
		XMLFileAssociation format = new XMLFileAssociation();
		format.setPattern("**/*.Format.ps1xml");
		format.setSystemId(baseSystemId + "Format.xsd");
		XMLFileAssociation resources = new XMLFileAssociation();
		resources.setPattern("**/*resources*.xml");
		resources.setSystemId(baseSystemId + "resources.xsd");
		return new XMLFileAssociation[] { format, resources };
	}
}
