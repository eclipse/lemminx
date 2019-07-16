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

import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMParser;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelProvider;
import org.eclipse.lsp4xml.extensions.dtd.contentmodel.CMDTDContentModelProvider;
import org.eclipse.lsp4xml.extensions.xsd.contentmodel.CMXSDContentModelProvider;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtensionManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test with {@link ContentModelManager#dependsOnGrammar(DOMDocument, String)}
 * 
 * @author Angelo ZERR
 *
 */
public class ContentModelManagerDependsOnGrammarTest {

	private ContentModelManager modelManager;

	@Before
	public void setup() {
		URIResolverExtensionManager resolverExtensionManager = new URIResolverExtensionManager();
		modelManager = new ContentModelManager(resolverExtensionManager);

		// DTD -> See DTDPlugin
		ContentModelProvider modelProvider = new CMDTDContentModelProvider(resolverExtensionManager);
		modelManager.registerModelProvider(modelProvider);

		// XSD -> See XSDPlugin
		modelProvider = new CMXSDContentModelProvider(resolverExtensionManager);
		modelManager.registerModelProvider(modelProvider);
	}

	@Test
	public void dependsOnGrammarTrue1WithNamespace() {
		String text = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" \r\n" + //
				"		 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \r\n" + //
				"		 xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 /home/nikolas/testXSD.xsd\">\r\n" + //
				"";
		TextDocument textDocument = new TextDocument(text, "/home/nikolas/testXML.xml");
		DOMDocument d = DOMParser.getInstance().parse(text, textDocument.getUri(), null);
		Assert.assertTrue(d.hasSchemaInstancePrefix());
		Assert.assertTrue(modelManager.dependsOnGrammar(d, "file:///home/nikolas/testXSD.xsd"));
	}

	@Test
	public void dependsOnGrammarTrue2WithNamespace() {
		String text = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" \r\n" + //
				"		 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \r\n" + //
				"		 xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 nested/testXSD.xsd\">\r\n" + //
				"";
		TextDocument textDocument = new TextDocument(text, "/home/nikolas/testXML.xml");
		DOMDocument d = DOMParser.getInstance().parse(text, textDocument.getUri(), null);
		Assert.assertTrue(d.hasSchemaInstancePrefix());
		Assert.assertTrue(modelManager.dependsOnGrammar(d, "file:///home/nikolas/nested/testXSD.xsd"));
	}

	@Test
	public void dependsOnGrammarTrue3WithNamespace() {
		String text = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" \r\n" + //
				"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + //
				"  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 file:///home/nikolas/nested/testXSD.xsd\">";
		TextDocument textDocument = new TextDocument(text, "/home/nikolas/testXML.xml");
		DOMDocument d = DOMParser.getInstance().parse(text, textDocument.getUri(), null);
		Assert.assertTrue(d.hasSchemaInstancePrefix());
		Assert.assertTrue(modelManager.dependsOnGrammar(d, "file:///home/nikolas/nested/testXSD.xsd"));
	}

	@Test
	public void dependsOnGrammarTrue1NoNamespace() {
		String text = "<project \r\n" + //
				"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + //
				"  xsi:noNamespaceSchemaLocation=\"testXSD.xsd\"> ";
		TextDocument textDocument = new TextDocument(text, "/home/nikolas/testXML.xml");
		DOMDocument d = DOMParser.getInstance().parse(text, textDocument.getUri(), null);
		Assert.assertTrue(d.hasSchemaInstancePrefix());
		Assert.assertTrue(modelManager.dependsOnGrammar(d, "file:///home/nikolas/testXSD.xsd"));
	}

	@Test
	public void dependsOnGrammarTrue2NoNamespace() {
		String text = "<project \r\n" + //
				"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + //
				"  xsi:noNamespaceSchemaLocation=\"nested/testXSD.xsd\">";
		TextDocument textDocument = new TextDocument(text, "/home/nikolas/testXML.xml");
		DOMDocument d = DOMParser.getInstance().parse(text, textDocument.getUri(), null);
		Assert.assertTrue(d.hasSchemaInstancePrefix());
		Assert.assertTrue(modelManager.dependsOnGrammar(d, "file:///home/nikolas/nested/testXSD.xsd"));
	}

	@Test
	public void dependsOnGrammarTrue3NoNamespace() {
		String text = "<project \r\n" + //
				"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + //
				"  xsi:noNamespaceSchemaLocation=\"file:///home/nikolas/nested/testXSD.xsd\">";
		TextDocument textDocument = new TextDocument(text, "/home/nikolas/testXML.xml");
		DOMDocument d = DOMParser.getInstance().parse(text, textDocument.getUri(), null);
		Assert.assertTrue(d.hasSchemaInstancePrefix());
		Assert.assertTrue(modelManager.dependsOnGrammar(d, "file:///home/nikolas/nested/testXSD.xsd"));
	}

	@Test
	public void dependsOnGrammarFalseWithNamespace() {
		String text = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" \r\n" + //
				"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + //
				"  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 testXSD.xsd\">";
		TextDocument textDocument = new TextDocument(text, "/home/nikolas/testXML.xml");
		DOMDocument d = DOMParser.getInstance().parse(text, textDocument.getUri(), null);
		Assert.assertTrue(d.hasSchemaInstancePrefix());
		Assert.assertFalse(modelManager.dependsOnGrammar(d, "file:///home/NOT_NIKOLAS/testXSD.xsd")); // bad path
	}

	@Test
	public void dependsOnGrammarFalseNoNamespace() {
		String text = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" \r\n" + //
				"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + //
				"  xsi:noNamespaceSchemaLocation=\"nested/testXSD.xsd\">";
		TextDocument textDocument = new TextDocument(text, "/home/nikolas/testXML.xml");
		DOMDocument d = DOMParser.getInstance().parse(text, textDocument.getUri(), null);
		Assert.assertTrue(d.hasSchemaInstancePrefix());
		Assert.assertFalse(modelManager.dependsOnGrammar(d, "file:///home/NOT_NIKOLAS/nested/testXSD.xsd")); // bad path
	}

	@Test
	public void dependsOnGrammarTrueAbsolutePath() {
		String text = "<project \r\n" + //
				"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + //
				"  xsi:noNamespaceSchemaLocation=\"/home/nikolas/nested/testXSD.xsd\">";
		TextDocument textDocument = new TextDocument(text, "/home/nikolas/testXML.xml");
		DOMDocument d = DOMParser.getInstance().parse(text, textDocument.getUri(), null);
		Assert.assertTrue(d.hasSchemaInstancePrefix());
		Assert.assertTrue(modelManager.dependsOnGrammar(d, "file:///home/nikolas/nested/testXSD.xsd")); // bad path
	}

	@Test
	public void testNoNamespaceSchemaLocationAndShemaLocationBoth() {
		String text = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" \r\n" + //
				"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + //
				"  xsi:noNamespaceSchemaLocation=\"/home/nikolas/nested/testXSD.xsd\"" + // //
				" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 testXSD.xsd\"" + // //
				">";
		TextDocument textDocument = new TextDocument(text, "/home/test.xml");
		DOMDocument d = DOMParser.getInstance().parse(text, textDocument.getUri(), null);
		Assert.assertNotNull(d.getNoNamespaceSchemaLocation());
		Assert.assertNotNull(d.getSchemaLocation());
	}

}
