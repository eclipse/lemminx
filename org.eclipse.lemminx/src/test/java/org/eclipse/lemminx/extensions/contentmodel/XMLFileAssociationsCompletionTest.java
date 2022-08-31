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

import static org.eclipse.lemminx.XMLAssert.c;
import static org.eclipse.lemminx.XMLAssert.te;

import java.util.function.Consumer;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLFileAssociation;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

/**
 * XML file associations completion tests.
 */
public class XMLFileAssociationsCompletionTest extends AbstractCacheBasedTest {

	// ------- XML file association with XSD xs:noNamespaceShemaLocation like

	@Test
	public void completionOnRootWithXSD() throws BadLocationException {
		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			contentModelManager
					.setFileAssociations(createXSDAssociationsNoNamespaceSchemaLocationLike("src/test/resources/xsd/"));
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
	public void completionAfterRootWithXSD() throws BadLocationException {
		Consumer<XMLLanguageService> configuration = ls -> {
			// Configure language service with file asociations
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			contentModelManager
					.setFileAssociations(createXSDAssociationsNoNamespaceSchemaLocationLike("src/test/resources/xsd/"));
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
	public void rootURIEndsWithSlashWithXSD() throws BadLocationException {
		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			// Use root URI which ends with slash
			contentModelManager.setRootURI("src/test/resources/xsd/");
			contentModelManager.setFileAssociations(createXSDAssociationsNoNamespaceSchemaLocationLike(""));
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
	public void rootURIEndsWithNoSlashWithXSD() throws BadLocationException {
		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			// Use root URI which ends with no slash
			contentModelManager.setRootURI("src/test/resources/xsd");
			contentModelManager.setFileAssociations(createXSDAssociationsNoNamespaceSchemaLocationLike(""));
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
	public void completionOnRootWithXSDAndNS() throws BadLocationException {
		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			contentModelManager.setFileAssociations(createXSDAssociationsSchemaLocationLike("src/test/resources/xsd/"));
		};
		// completion on <|
		String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\r\n>\r\n" + //
				"	<|" + //
				"</project>";
		testCompletionFor(xml, "file:///test/pom.xml", configuration, //
				c("modelVersion", te(2, 1, 2, 2, "<modelVersion></modelVersion>"), "<modelVersion"), //
				c("parent", "<parent></parent>", "<parent"));
	}

	@Test
	public void completionWithXSDAndDocType() throws BadLocationException {
		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			contentModelManager.setFileAssociations(createXSDAssociationsSchemaLocationLike("src/test/resources/xsd/"));
		};
		// completion on <|
		String xml = "<!DOCTYPE opt [\r\n" + //
				"  <!ENTITY size \"short\">\r\n" + //
				"]>\r\n" + //
				"<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\r\n>\r\n" + //
				"	<|" + //
				"</project>";
		testCompletionFor(xml, "file:///test/pom.xml", configuration, //
				c("modelVersion", te(5, 1, 5, 2, "<modelVersion></modelVersion>"), "<modelVersion"), //
				c("parent", "<parent></parent>", "<parent"));
	}

	private static XMLFileAssociation[] createXSDAssociationsSchemaLocationLike(String baseSystemId) {
		XMLFileAssociation maven = new XMLFileAssociation();
		maven.setPattern("**/pom.xml");
		maven.setSystemId(baseSystemId + "maven-4.0.0.xsd");
		return new XMLFileAssociation[] { maven };
	}

	// ------- XML file association with DTD

	@Test
	public void completionOnRootWithDTD() throws BadLocationException {
		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			contentModelManager.setFileAssociations(createDTDAssociations("src/test/resources/dtd/"));
		};
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"  <|";
		testCompletionFor(xml, "file:///test/web.xml", configuration, //
				c("web-app", "<web-app></web-app>"));
		xml = "|";
		testCompletionFor(xml, "file:///test/web.xml", configuration, //
				c("web-app", "<web-app></web-app>"));
		xml = " |";
		testCompletionFor(xml, "file:///test/web.xml", configuration, //
				c("web-app", "<web-app></web-app>"));
	}

	private static XMLFileAssociation[] createDTDAssociations(String baseSystemId) {
		XMLFileAssociation webApp = new XMLFileAssociation();
		webApp.setPattern("web.xml");
		webApp.setSystemId(baseSystemId + "web-app_2_3.dtd");
		return new XMLFileAssociation[] { webApp };
	}

	private static void testCompletionFor(String xml, String fileURI, Consumer<XMLLanguageService> configuration,
			CompletionItem... expectedItems) throws BadLocationException {
		XMLAssert.testCompletionFor(new XMLLanguageService(), xml, null, configuration, fileURI, null, true,
				expectedItems);
	}

}
