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

import java.util.function.Consumer;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.XMLFileAssociation;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.junit.Test;

/**
 * XML file associations completion tests.
 */
public class XMLFileAssociationsCompletionTest {

	@Test
	public void completionOnRoot() throws BadLocationException {
		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			contentModelManager.setFileAssociations(createAssociations("src/test/resources/xsd/"));
		};
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"  <|";
		testCompletionFor(xml, "file:///test/Test.Format.ps1xml", configuration,
				c("Configuration", "<Configuration></Configuration>"));
		xml = "|";
		testCompletionFor(xml, "file:///test/Test.Format.ps1xml", configuration,
				c("Configuration", "<Configuration></Configuration>"));
		xml = " |";
		testCompletionFor(xml, "file:///test/Test.Format.ps1xml", configuration,
				c("Configuration", "<Configuration></Configuration>"));
	}

	@Test
	public void completionAfterRoot() throws BadLocationException {
		Consumer<XMLLanguageService> configuration = ls -> {
			// Configure language service with file asociations
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			contentModelManager.setFileAssociations(createAssociations("src/test/resources/xsd/"));
		};

		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<Configuration>\r\n" + //
				"  <|";
		testCompletionFor(xml, "file:///test/Test.Format.ps1xml", configuration, c("Controls", "<Controls></Controls>"), //
				c("DefaultSettings", "<DefaultSettings></DefaultSettings>"), //
				c("SelectionSets", "<SelectionSets></SelectionSets>"),
				c("ViewDefinitions", "<ViewDefinitions></ViewDefinitions>"));

		xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<Configuration>\r\n" + //
				"  |";
		testCompletionFor(xml, "file:///test/Test.Format.ps1xml", configuration, c("Controls", "<Controls></Controls>"), //
				c("DefaultSettings", "<DefaultSettings></DefaultSettings>"), //
				c("SelectionSets", "<SelectionSets></SelectionSets>"),
				c("ViewDefinitions", "<ViewDefinitions></ViewDefinitions>"));
	}

	@Test
	public void rootURIEndsWithSlash() throws BadLocationException {
		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			// Use root URI which ends with slash
			contentModelManager.setRootURI("src/test/resources/xsd/");
			contentModelManager.setFileAssociations(createAssociations(""));
		};

		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"  <|";
		testCompletionFor(xml, "file:///test/Test.Format.ps1xml", configuration,
				c("Configuration", "<Configuration></Configuration>"));
		xml = "|";
		testCompletionFor(xml, "file:///test/Test.Format.ps1xml", configuration,
				c("Configuration", "<Configuration></Configuration>"));
		xml = " |";
		testCompletionFor(xml, "file:///test/Test.Format.ps1xml", configuration,
				c("Configuration", "<Configuration></Configuration>"));
	}

	@Test
	public void rootURIEndsWithNoSlash() throws BadLocationException {
		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			// Use root URI which ends with no slash
			contentModelManager.setRootURI("src/test/resources/xsd");
			contentModelManager.setFileAssociations(createAssociations(""));
		};

		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"  <|";
		testCompletionFor(xml, "file:///test/Test.Format.ps1xml", configuration,
				c("Configuration", "<Configuration></Configuration>"));
		xml = "|";
		testCompletionFor(xml, "file:///test/Test.Format.ps1xml", configuration,
				c("Configuration", "<Configuration></Configuration>"));
		xml = " |";
		testCompletionFor(xml, "file:///test/Test.Format.ps1xml", configuration,
				c("Configuration", "<Configuration></Configuration>"));
	}

	private void testCompletionFor(String xml, String fileURI, Consumer<XMLLanguageService> configuration,
			CompletionItem... expectedItems) throws BadLocationException {
		XMLAssert.testCompletionFor(new XMLLanguageService(), xml, null, configuration, fileURI, null, true,
				expectedItems);
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
