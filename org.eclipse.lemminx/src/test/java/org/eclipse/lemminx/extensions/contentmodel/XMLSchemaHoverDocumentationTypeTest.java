/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.contentmodel;

import java.util.Arrays;

import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.util.URI.MalformedURIException;
import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lemminx.settings.SchemaDocumentationType;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.HoverCapabilities;
import org.eclipse.lsp4j.MarkupKind;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * XML hover tests with XML Schema.
 *
 */
public abstract class XMLSchemaHoverDocumentationTypeTest extends AbstractCacheBasedTest {

	private static final String schemaName = "docAppinfo.xsd";
	private static final String schemaPath = "src/test/resources/" + schemaName;
	private static final String docPrefix = "**xs:documentation**:" + System.lineSeparator() + System.lineSeparator();
	private static final String appinfoPrefix = "**xs:appinfo**:" + System.lineSeparator() + System.lineSeparator();
	private static String source;
	private static String plainTextDocPrefix = "xs:documentation:" + System.lineSeparator() + System.lineSeparator();
	private static String plainTextAppinfoPrefix = "xs:appinfo:" + System.lineSeparator() + System.lineSeparator();
	private static String plainTextSource;
	private String extraAttributes = "";

	@BeforeAll
	public static void setup() throws MalformedURIException {
		source = "Source: [" + schemaName + "](" + getXMLSchemaFileURI(schemaName) + ")";
		plainTextSource = "Source: " + schemaName;
	}

	@Test
	public void testHoverAttributeNameDoc() throws BadLocationException, MalformedURIException {
		assertAttributeNameDocHover("attribute name documentation", SchemaDocumentationType.documentation, true);
		assertAttributeNameDocHover(null, SchemaDocumentationType.appinfo, true);
		assertAttributeNameDocHover("attribute name documentation", SchemaDocumentationType.all, true);
		assertAttributeNameDocHover(null, SchemaDocumentationType.none, true);
	};

	@Test
	public void testHoverAttributeNameAppinfo() throws BadLocationException, MalformedURIException {
		assertAttributeNameAppinfoHover(null, SchemaDocumentationType.documentation, true);
		assertAttributeNameAppinfoHover("attribute name appinfo", SchemaDocumentationType.appinfo, true);
		assertAttributeNameAppinfoHover("attribute name appinfo", SchemaDocumentationType.all, true);
		assertAttributeNameAppinfoHover(null, SchemaDocumentationType.none, true);
	};

	@Test
	public void testHoverAttributeNameBoth() throws BadLocationException, MalformedURIException {
		assertAttributeNameBothHover("attribute name documentation", SchemaDocumentationType.documentation, true);
		assertAttributeNameBothHover("attribute name appinfo", SchemaDocumentationType.appinfo, true);
		assertAttributeNameBothHover(
				docPrefix + "attribute name documentation" +
				System.lineSeparator() + System.lineSeparator() +
				appinfoPrefix + "attribute name appinfo", SchemaDocumentationType.all, true);
		assertAttributeNameBothHover(null, SchemaDocumentationType.none, true);
	};

	@Test
	public void testHoverAttributeValueDoc() throws BadLocationException, MalformedURIException {
		assertAttributeValueDocHover("attribute value documentation", SchemaDocumentationType.documentation, true);
		assertAttributeValueDocHover(null, SchemaDocumentationType.appinfo, true);
		assertAttributeValueDocHover("attribute value documentation", SchemaDocumentationType.all, true);
		assertAttributeValueDocHover(null, SchemaDocumentationType.none, true);
	};

	@Test
	public void testHoverAttributeValueAppinfo() throws BadLocationException, MalformedURIException {
		assertAttributeValueAppinfoHover(null, SchemaDocumentationType.documentation, true);
		assertAttributeValueAppinfoHover("attribute value appinfo", SchemaDocumentationType.appinfo, true);
		assertAttributeValueAppinfoHover("attribute value appinfo", SchemaDocumentationType.all, true);
		assertAttributeValueAppinfoHover(null, SchemaDocumentationType.none, true);
	};

	@Test
	public void testHoverAttributeValueBoth() throws BadLocationException, MalformedURIException {
		assertAttributeValueBothHover("attribute value documentation", SchemaDocumentationType.documentation, true);
		assertAttributeValueBothHover("attribute value appinfo", SchemaDocumentationType.appinfo, true);
		assertAttributeValueBothHover(
				docPrefix + "attribute value documentation" +
				System.lineSeparator() + System.lineSeparator() +
				appinfoPrefix + "attribute value appinfo", SchemaDocumentationType.all, true);
		assertAttributeValueBothHover(null, SchemaDocumentationType.none, true);
	};

	@Test
	public void testHoverElementDoc() throws BadLocationException, MalformedURIException {
		assertElementDocHover("element documentation", SchemaDocumentationType.documentation, true);
		assertElementDocHover(null, SchemaDocumentationType.appinfo, true);
		assertElementDocHover("element documentation", SchemaDocumentationType.all, true);
		assertElementDocHover(null, SchemaDocumentationType.none, true);
	};

	@Test
	public void testHoverElementAppinfo() throws BadLocationException, MalformedURIException {
		assertElementAppinfoHover(null, SchemaDocumentationType.documentation, true);
		assertElementAppinfoHover("element appinfo", SchemaDocumentationType.appinfo, true);
		assertElementAppinfoHover("element appinfo", SchemaDocumentationType.all, true);
		assertElementAppinfoHover(null, SchemaDocumentationType.none, true);
	};

	@Test
	public void testHoverElementBoth() throws BadLocationException, MalformedURIException {
		assertElementBothHover("element documentation", SchemaDocumentationType.documentation, true);
		assertElementBothHover("element appinfo", SchemaDocumentationType.appinfo, true);
		assertElementBothHover(
				docPrefix + "element documentation" +
				System.lineSeparator() + System.lineSeparator() +
				appinfoPrefix + "element appinfo", SchemaDocumentationType.all, true);
		assertElementBothHover(null, SchemaDocumentationType.none, true);
	};

	@Test
	public void testHoverElementBothPlainText() throws BadLocationException, MalformedURIException {
		assertElementBothHover("element documentation", SchemaDocumentationType.documentation, false);
		assertElementBothHover("element appinfo", SchemaDocumentationType.appinfo, false);
		assertElementBothHover(
				plainTextDocPrefix + "element documentation" +
				System.lineSeparator() + System.lineSeparator() +
				plainTextAppinfoPrefix + "element appinfo", SchemaDocumentationType.all, false);
		assertElementBothHover(null, SchemaDocumentationType.none, false);
	};


	@Test
	public void testHoverMultipleBoth() throws BadLocationException, MalformedURIException {
		String documentation =
				"first element documentation" + System.lineSeparator() + System.lineSeparator() +
				"second element documentation" + System.lineSeparator() + System.lineSeparator() +
				"third element documentation";

		String appinfo =
				"first element appinfo" + System.lineSeparator() + System.lineSeparator() +
				"second element appinfo" + System.lineSeparator() + System.lineSeparator() +
				"third element appinfo";

		assertElementMultipleBothHover(documentation, SchemaDocumentationType.documentation, true);
		assertElementMultipleBothHover(appinfo, SchemaDocumentationType.appinfo, true);
		assertElementMultipleBothHover(
				docPrefix + documentation +
				System.lineSeparator() + System.lineSeparator() +
				appinfoPrefix + appinfo, SchemaDocumentationType.all, true);
		assertElementMultipleBothHover(null, SchemaDocumentationType.none, true);
	};

	@Test
	public void testHoverMultipleBothPlainText() throws BadLocationException, MalformedURIException {
		String documentation =
				"first element documentation" + System.lineSeparator() +System.lineSeparator() +
				"second element documentation" + System.lineSeparator() +System.lineSeparator() +
				"third element documentation";

		String appinfo =
				"first element appinfo" + System.lineSeparator() +System.lineSeparator() +
				"second element appinfo" + System.lineSeparator() +System.lineSeparator() +
				"third element appinfo";

		assertElementMultipleBothHover(documentation, SchemaDocumentationType.documentation, false);
		assertElementMultipleBothHover(appinfo, SchemaDocumentationType.appinfo, false);
		assertElementMultipleBothHover(
				plainTextDocPrefix + documentation +
				System.lineSeparator() + System.lineSeparator() +
				plainTextAppinfoPrefix + appinfo, SchemaDocumentationType.all, false);
		assertElementMultipleBothHover(null, SchemaDocumentationType.none, false);
	};

	@Test
	public void testHoverNoAnnotation() throws BadLocationException, MalformedURIException {
		assertElementHoverNoAnnotation(SchemaDocumentationType.all, true);
		assertElementHoverNoAnnotation(SchemaDocumentationType.appinfo, true);
		assertElementHoverNoAnnotation(SchemaDocumentationType.documentation, true);
		assertElementHoverNoAnnotation(SchemaDocumentationType.none, true);
		assertElementHoverNoAnnotation(SchemaDocumentationType.all, false);
		assertElementHoverNoAnnotation(SchemaDocumentationType.appinfo, false);
		assertElementHoverNoAnnotation(SchemaDocumentationType.documentation, false);
		assertElementHoverNoAnnotation(SchemaDocumentationType.none, false);
	}

	@Test
	public void testHoverWhitespaceAnnotation() throws BadLocationException, MalformedURIException {
		assertElementHoverWhitespaceAnnotation(SchemaDocumentationType.all, true);
		assertElementHoverWhitespaceAnnotation(SchemaDocumentationType.appinfo, true);
		assertElementHoverWhitespaceAnnotation(SchemaDocumentationType.documentation, true);
		assertElementHoverWhitespaceAnnotation(SchemaDocumentationType.none, true);
		assertElementHoverWhitespaceAnnotation(SchemaDocumentationType.all, false);
		assertElementHoverWhitespaceAnnotation(SchemaDocumentationType.appinfo, false);
		assertElementHoverWhitespaceAnnotation(SchemaDocumentationType.documentation, false);
		assertElementHoverWhitespaceAnnotation(SchemaDocumentationType.none, false);
	}

	private void assertAttributeNameDocHover(String expected, SchemaDocumentationType docSource,
			boolean markdownSupported) throws BadLocationException {
		String xml =
				"<root\n" +
				"	xmlns=\"http://docAppinfo\"\n" +
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"	xsi:schemaLocation=\"http://docAppinfo xsd/" + schemaName + "\"\n" +
				extraAttributes + //
				"	attribu|teNameOnlyDocumentation=\"onlyDocumentation\">\n" +
				"</root>\n";
		assertHover(xml, expected, docSource, markdownSupported);
	}

	private void assertAttributeValueDocHover(String expected, SchemaDocumentationType docSource,
			boolean markdownSupported) throws BadLocationException {
		String xml =
				"<root\n" +
				"	xmlns=\"http://docAppinfo\"\n" +
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"	xsi:schemaLocation=\"http://docAppinfo xsd/" + schemaName + "\"\n" +
				extraAttributes + //
				"	attributeNameOnlyDocumentation=\"o|nlyDocumentation\">\n" +
				"</root>\n";
		assertHover(xml, expected, docSource, markdownSupported);
	}

	private void assertAttributeNameAppinfoHover(String expected, SchemaDocumentationType docSource,
			boolean markdownSupported) throws BadLocationException {
		String xml =
				"<root\n" +
				"	xmlns=\"http://docAppinfo\"\n" +
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"	xsi:schemaLocation=\"http://docAppinfo xsd/" + schemaName + "\"\n" +
				extraAttributes + //
				"	a|ttributeNameOnlyAppinfo=\"onlyAppinfo\">\n" +
				"</root>\n";
		assertHover(xml, expected, docSource, markdownSupported);
	}

	private void assertAttributeValueAppinfoHover(String expected, SchemaDocumentationType docSource,
			boolean markdownSupported) throws BadLocationException {
		String xml =
				"<root\n" +
				"	xmlns=\"http://docAppinfo\"\n" +
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"	xsi:schemaLocation=\"http://docAppinfo xsd/" + schemaName + "\"\n" +
				extraAttributes + //
				"	attributeNameOnlyAppinfo=\"o|nlyAppinfo\">\n" +
				"</root>\n";
		assertHover(xml, expected, docSource, markdownSupported);
	}

	private void assertAttributeNameBothHover(String expected, SchemaDocumentationType docSource,
			boolean markdownSupported) throws BadLocationException {
		String xml =
				"<root\n" +
				"	xmlns=\"http://docAppinfo\"\n" +
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"	xsi:schemaLocation=\"http://docAppinfo xsd/" + schemaName + "\"\n" +
				extraAttributes + //
				"	a|ttributeNameBoth=\"bothDocumentationAndAppinfo\">\n" +
				"</root>\n";
		assertHover(xml, expected, docSource, markdownSupported);
	}

	private void assertAttributeValueBothHover(String expected, SchemaDocumentationType docSource,
			boolean markdownSupported) throws BadLocationException {
		String xml =
				"<root\n" +
				"	xmlns=\"http://docAppinfo\"\n" +
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"	xsi:schemaLocation=\"http://docAppinfo xsd/" + schemaName + "\"\n" +
				extraAttributes + //
				"	attributeNameBoth=\"b|othDocumentationAndAppinfo\">\n" +
				"</root>\n";
		assertHover(xml, expected, docSource, markdownSupported);
	}

	private void assertElementDocHover(String expected, SchemaDocumentationType docSource,
			boolean markdownSupported) throws BadLocationException {
		String xml =
				"<root\n" +
				"	xmlns=\"http://docAppinfo\"\n" +
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"	xsi:schemaLocation=\"http://docAppinfo xsd/" + schemaName + "\"\n" +
				extraAttributes + //
				"	<e|lementOnlyDocumentation></elementOnlyDocumentation>\n" +
				"</root>\n";
		assertHover(xml, expected, docSource, markdownSupported);
	}

	private void assertElementAppinfoHover(String expected, SchemaDocumentationType docSource,
			boolean markdownSupported) throws BadLocationException {
		String xml =
				"<root\n" +
				"	xmlns=\"http://docAppinfo\"\n" +
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"	xsi:schemaLocation=\"http://docAppinfo xsd/" + schemaName + "\"\n" +
				extraAttributes + //
				"	<e|lementOnlyAppinfo></elementOnlyAppinfo>\n" +
				"</root>\n";
		assertHover(xml, expected, docSource, markdownSupported);
	}

	private void assertElementBothHover(String expected, SchemaDocumentationType docSource,
			boolean markdownSupported) throws BadLocationException {
		String xml =
				"<root\n" +
				"	xmlns=\"http://docAppinfo\"\n" +
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"	xsi:schemaLocation=\"http://docAppinfo xsd/" + schemaName + "\"\n" +
				extraAttributes + //
				"	<e|lementBoth></elementBoth>\n" +
				"</root>\n";
		assertHover(xml, expected, docSource, markdownSupported);
	}

	private void assertElementMultipleBothHover(String expected, SchemaDocumentationType docSource,
			boolean markdownSupported) throws BadLocationException {
		String xml =
				"<root\n" +
				"	xmlns=\"http://docAppinfo\"\n" +
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"	xsi:schemaLocation=\"http://docAppinfo xsd/" + schemaName + "\"\n" +
				extraAttributes + //
				"	<e|lementMultipleBoth></elementMultipleBoth>\n" +
				"</root>\n";
		assertHover(xml, expected, docSource, markdownSupported);
	}

	private void assertElementHoverNoAnnotation(SchemaDocumentationType docSource, boolean markdownSupported) throws BadLocationException {
		String xml =
				"<root\n" + //
				"	xmlns=\"http://docAppinfo\"\n" + //
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + //
				"	xsi:schemaLocation=\"http://docAppinfo xsd/" + schemaName + "\"\n" + //
				extraAttributes + //
				"	<e|lementNoAnnotation></elementNoAnnotation>\n" + //
				"</root>\n";
		assertHover(xml, null, docSource, markdownSupported);
	}

	private void assertElementHoverWhitespaceAnnotation(SchemaDocumentationType docSource, boolean markdownSupported) throws BadLocationException {
		String xml =
				"<root\n" + //
				"	xmlns=\"http://docAppinfo\"\n" + //
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + //
				"	xsi:schemaLocation=\"http://docAppinfo xsd/" + schemaName + "\"\n" + //
				extraAttributes + //
				"	<e|lementWhitespaceAnnotation></elementWhitespaceAnnotation>\n" + //
				"</root>\n";
		assertHover(xml, null, docSource, markdownSupported);
	}

	private void assertHover(String xml, String expected, SchemaDocumentationType docSource, boolean markdownSupported) throws BadLocationException {

		if (expected != null) {
			String currSource = markdownSupported ? source : plainTextSource;
			StringBuilder content = new StringBuilder(expected);
			content.append(System.lineSeparator());
			content.append(System.lineSeparator());
			content.append(currSource);
			expected = content.toString();
		}

		XMLAssert.assertHover(new XMLLanguageService(), xml, null, schemaPath,
				expected, null, createSharedSettings(docSource, markdownSupported));
	}

	private SharedSettings createSharedSettings(SchemaDocumentationType docSource, boolean markdownSupported) {
		SharedSettings settings = new SharedSettings();
		if (markdownSupported) {
			HoverCapabilities capabilities = new HoverCapabilities(Arrays.asList(MarkupKind.MARKDOWN), false);
			settings.getHoverSettings().setCapabilities(capabilities);
		}
		settings.getPreferences()
				.setShowSchemaDocumentationType(docSource);
		return settings;
	}

	private static String getXMLSchemaFileURI(String schemaURI) throws MalformedURIException {
		return XMLEntityManager.expandSystemId("xsd/" + schemaURI, "src/test/resources/test.xml", true).replace("///",
				"/");
	}

	@Nested
	public static class BaseTypeTest extends XMLSchemaHoverDocumentationTypeTest {
	}

	@Nested
	public static class DerivedTypeTest extends XMLSchemaHoverDocumentationTypeTest {
		public DerivedTypeTest() {
			super.extraAttributes = "  xsi:type=\"Derived\"";
		}

		@Test
		public void testHoverDerivedAttributeNameDoc() throws BadLocationException, MalformedURIException {
			assertDerivedAttributeNameDocHover("derived attribute name documentation", SchemaDocumentationType.documentation, true);
			assertDerivedAttributeNameDocHover(null, SchemaDocumentationType.appinfo, true);
			assertDerivedAttributeNameDocHover("derived attribute name documentation", SchemaDocumentationType.all, true);
			assertDerivedAttributeNameDocHover(null, SchemaDocumentationType.none, true);
		};

		@Test
		public void testHoverDerivedElementDoc() throws BadLocationException, MalformedURIException {
			assertDerivedElementDocHover("derived element documentation", SchemaDocumentationType.documentation, true);
			assertDerivedElementDocHover(null, SchemaDocumentationType.appinfo, true);
			assertDerivedElementDocHover("derived element documentation", SchemaDocumentationType.all, true);
			assertDerivedElementDocHover(null, SchemaDocumentationType.none, true);
		};

		private void assertDerivedAttributeNameDocHover(String expected, SchemaDocumentationType docSource,
												 boolean markdownSupported) throws BadLocationException {
			String xml =
					"<root\n" +
							"	xmlns=\"http://docAppinfo\"\n" +
							"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
							"	xsi:schemaLocation=\"http://docAppinfo xsd/" + schemaName + "\"\n" +
							super.extraAttributes + //
							"	derivedAttribu|teNameOnlyDocumentation=\"onlyDocumentation\">\n" +
							"</root>\n";
			super.assertHover(xml, expected, docSource, markdownSupported);
		}

		private void assertDerivedElementDocHover(String expected, SchemaDocumentationType docSource,
										   boolean markdownSupported) throws BadLocationException {
			String xml =
					"<root\n" +
							"	xmlns=\"http://docAppinfo\"\n" +
							"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
							"	xsi:schemaLocation=\"http://docAppinfo xsd/" + schemaName + "\"\n" +
							super.extraAttributes + //
							"	<derivedE|lementOnlyDocumentation></derivedElementOnlyDocumentation>\n" +
							"</root>\n";
			super.assertHover(xml, expected, docSource, markdownSupported);
		}
	}
}
