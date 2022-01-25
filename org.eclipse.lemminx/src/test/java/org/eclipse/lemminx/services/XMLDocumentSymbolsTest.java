/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services;

import static org.eclipse.lemminx.XMLAssert.ds;
import static org.eclipse.lemminx.XMLAssert.r;
import static org.eclipse.lemminx.XMLAssert.testDocumentSymbolsFor;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.lemminx.XMLAssert.SettingsSaveContext;
import org.eclipse.lemminx.commons.TextDocument;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMParser;
import org.eclipse.lemminx.extensions.contentmodel.settings.ContentModelSettings;
import org.eclipse.lemminx.settings.XMLSymbolExpressionFilter;
import org.eclipse.lemminx.settings.XMLSymbolFilter;
import org.eclipse.lemminx.settings.XMLSymbolSettings;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.SymbolKind;
import org.junit.jupiter.api.Test;

/**
 * XML symbols test with {@link DocumentSymbol} (with hierarchies).
 */
public class XMLDocumentSymbolsTest {

	@Test
	public void externalDTD() {
		String dtd = "<!ELEMENT br EMPTY>\n" + //
				"<!ATTLIST br\n" + //
				"	%all;>";
		testDocumentSymbolsFor(dtd, "test.dtd", //
				ds("br", SymbolKind.Property, r(0, 0, 0, 19), r(0, 0, 0, 19), null, //
						Arrays.asList(
								ds("%all;", SymbolKind.Key, r(2, 1, 2, 6), r(2, 1, 2, 6), null, Arrays.asList()))));

	}

	@Test
	public void multipleAttlistValues() {
		String dtd = "<!ELEMENT target EMPTY>\r\n" + //
				" \r\n" + //
				"<!ATTLIST target\r\n" + //
				"          tid ID #IMPLIED\r\n" + //
				"\r\n" + //
				"<!ATTLIST target\r\n" + //
				"          bee ID #IMPLIED>\r\n" + //
				"        \r\n" + //
				"         \r\n" + //
				"<!ATTLIST extension-point\r\n" + //
				"          ep CDATA #IMPLIED\r\n" + //
				"          ep2 CDATA #IMPLIED>\r\n";

		testDocumentSymbolsFor(dtd, "test.dtd", ds("target", SymbolKind.Property, r(0, 0, 0, 23), r(0, 0, 0, 23), null,
				Arrays.asList(ds("tid", SymbolKind.Key, r(3, 10, 3, 13), r(3, 10, 3, 13), null, Arrays.asList()),
						ds("bee", SymbolKind.Key, r(6, 10, 6, 13), r(6, 10, 6, 13), null, Arrays.asList()))),
				ds("extension-point", SymbolKind.Key, r(9, 0, 11, 29), r(9, 0, 11, 29), null, Arrays.asList(
						ds("ep", SymbolKind.Key, r(10, 10, 10, 12), r(10, 10, 10, 12), null, Arrays.asList()),
						ds("ep2", SymbolKind.Key, r(11, 10, 11, 13), r(11, 10, 11, 13), null, Arrays.asList()))));

	}

	@Test
	public void internalDTD() {
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE Folks [\r\n" + //
				"	<!ELEMENT Folks (Person*)>\r\n" + //
				"	<!ELEMENT Person (Name,Email?)>\r\n" + //
				"	<!ATTLIST Person Pin ID #REQUIRED>\r\n" + //
				"	<!ATTLIST Person Friend IDREF #IMPLIED>\r\n" + //
				"	<!ATTLIST Person Likes IDREFS #IMPLIED>\r\n" + //
				"	<!ELEMENT Name (#PCDATA)>\r\n" + //
				"	<!ELEMENT Email (#PCDATA)>\r\n" + //
				"	]>\r\n" + //
				"<Folks>\r\n" + //
				"	\r\n" + //
				"</Folks>";
		testDocumentSymbolsFor(xml, "test.xml", //
				ds("xml", SymbolKind.Property, r(0, 0, 0, 23), r(0, 0, 0, 23), null, //
						Collections.emptyList()), //
				ds("DOCTYPE:Folks", SymbolKind.Struct, r(1, 0, 9, 3), r(1, 0, 9, 3), null, Arrays.asList(
						ds("Folks", SymbolKind.Property, r(2, 1, 2, 27), r(2, 1, 2, 27), null, Collections.emptyList()), //
						ds("Person", SymbolKind.Property, r(3, 1, 3, 32), r(3, 1, 3, 32), null, //
								Arrays.asList( //
										ds("Pin", SymbolKind.Key, r(4, 18, 4, 21), r(4, 18, 4, 21), null,
												Collections.emptyList()), //
										ds("Friend", SymbolKind.Key, r(5, 18, 5, 24), r(5, 18, 5, 24), null,
												Collections.emptyList()), //
										ds("Likes", SymbolKind.Key, r(6, 18, 6, 23), r(6, 18, 6, 23), null,
												Collections.emptyList()))), //
						ds("Name", SymbolKind.Property, r(7, 1, 7, 26), r(7, 1, 7, 26), null, Collections.emptyList()), //
						ds("Email", SymbolKind.Property, r(8, 1, 8, 27), r(8, 1, 8, 27), null, Collections.emptyList()) //
				)), //
				ds("Folks", SymbolKind.Field, r(10, 0, 12, 8), r(10, 0, 12, 8), null, Collections.emptyList()));
	}

	@Test
	public void exceedSymbolLimit() {
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE Folks [\r\n" + //
				"	<!ELEMENT Folks (Person*)>\r\n" + //
				"	<!ELEMENT Person (Name,Email?)>\r\n" + //
				"	<!ATTLIST Person Pin ID #REQUIRED>\r\n" + //
				"	<!ATTLIST Person Friend IDREF #IMPLIED>\r\n" + //
				"	<!ATTLIST Person Likes IDREFS #IMPLIED>\r\n" + //
				"	<!ELEMENT Name (#PCDATA)>\r\n" + //
				"	<!ELEMENT Email (#PCDATA)>\r\n" + //
				"	]>\r\n" + //
				"<Folks>\r\n" + //
				"	\r\n" + //
				"</Folks>";

		DocumentSymbol symbol1 = ds("xml", SymbolKind.Property, r(0, 0, 0, 23), r(0, 0, 0, 23), null, //
				Collections.emptyList());
		DocumentSymbol symbol2 = ds("DOCTYPE:Folks", SymbolKind.Struct, r(1, 0, 9, 3), r(1, 0, 9, 3), null,
				Arrays.asList(
						ds("Folks", SymbolKind.Property, r(2, 1, 2, 27), r(2, 1, 2, 27), null, Collections.emptyList()), //
						ds("Person", SymbolKind.Property, r(3, 1, 3, 32), r(3, 1, 3, 32), null, //
								Arrays.asList( //
										ds("Pin", SymbolKind.Key, r(4, 18, 4, 21), r(4, 18, 4, 21), null,
												Collections.emptyList()), //
										ds("Friend", SymbolKind.Key, r(5, 18, 5, 24), r(5, 18, 5, 24), null,
												Collections.emptyList()), //
										ds("Likes", SymbolKind.Key, r(6, 18, 6, 23), r(6, 18, 6, 23), null,
												Collections.emptyList()))), //
						ds("Name", SymbolKind.Property, r(7, 1, 7, 26), r(7, 1, 7, 26), null, Collections.emptyList()), //
						ds("Email", SymbolKind.Property, r(8, 1, 8, 27), r(8, 1, 8, 27), null,
								Collections.emptyList())));
		DocumentSymbol symbol3 = ds("Folks", SymbolKind.Field, r(10, 0, 12, 8), r(10, 0, 12, 8), null,
				Collections.emptyList());

		XMLSymbolSettings settings = new XMLSymbolSettings();
		settings.setMaxItemsComputed(10);
		testDocumentSymbolsFor(xml, "test.xml", settings, symbol1, symbol2, symbol3);

		settings.setMaxItemsComputed(15);
		testDocumentSymbolsFor(xml, "test.xml", settings, symbol1, symbol2, symbol3);

		settings.setMaxItemsComputed(9);
		testDocumentSymbolsFor(xml, "test.xml", settings, symbol1, symbol2);
	}

	@Test
	public void symbolLimitOfZero() {
		String xml = "<a>\n" + //
				"  <b>\n" + //
				"    <c>\n" + //
				"      <d />\n" + //
				"    </c>\n" + //
				"  </b>" + //
				"</a>";

		XMLSymbolSettings settings = new XMLSymbolSettings();
		settings.setMaxItemsComputed(0);

		testDocumentSymbolsNumber(xml, "test.xml", settings, 0);
	}

	private static void testDocumentSymbolsNumber(String xml, String fileURI, XMLSymbolSettings symbolSettings,
			int expectedNumber) {
		testDocumentSymbolsNumber(new XMLLanguageService(), xml, fileURI, symbolSettings, null, expectedNumber);
	}

	private static void testDocumentSymbolsNumber(XMLLanguageService xmlLanguageService, String xml, String fileURI,
			XMLSymbolSettings symbolSettings, Consumer<XMLLanguageService> customConfiguration, int expectedNumber) {
		TextDocument document = new TextDocument(xml, fileURI != null ? fileURI : "test.xml");

		ContentModelSettings settings = new ContentModelSettings();
		settings.setUseCache(false);
		xmlLanguageService.doSave(new SettingsSaveContext(settings));
		xmlLanguageService.initializeIfNeeded();

		if (customConfiguration != null) {
			customConfiguration.accept(xmlLanguageService);
		}

		DOMDocument xmlDocument = DOMParser.getInstance().parse(document,
				xmlLanguageService.getResolverExtensionManager());
		xmlLanguageService.setDocumentProvider((uri) -> xmlDocument);

		List<DocumentSymbol> actual = xmlLanguageService.findDocumentSymbols(xmlDocument, symbolSettings);
		assertEquals(expectedNumber, actual.size());
	}

	// Tests for Symbols filter

	@Test
	public void noSymbolsFilter() {
		String xml = "<foo>\r\n" + //
				"	<bar attr1=\"value1\" attr2=\"value2\">ABCD</bar>\r\n" + //
				"</foo>";
		// null symbols filter
		XMLSymbolSettings symbolSettings = new XMLSymbolSettings();
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				ds("foo", SymbolKind.Field, r(0, 0, 2, 6), r(0, 0, 2, 6), null, //
						Arrays.asList(
								ds("bar", SymbolKind.Field, r(1, 1, 1, 46), r(1, 1, 1, 46), null, Arrays.asList()))));

		// empty array symbols filter
		symbolSettings = new XMLSymbolSettings();
		symbolSettings.setFilters(new XMLSymbolFilter[0]);
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				ds("foo", SymbolKind.Field, r(0, 0, 2, 6), r(0, 0, 2, 6), null, //
						Arrays.asList(
								ds("bar", SymbolKind.Field, r(1, 1, 1, 46), r(1, 1, 1, 46), null, Arrays.asList()))));

	}

	@Test
	public void symbolsFilterWithAllAttr() {
		String xml = "<foo>\r\n" + //
				"	<bar attr1=\"value1\" attr2=\"value2\">ABCD</bar>\r\n" + //
				"	<baz attr1=\"baz-value1\" attr2=\"baz-value2\">EFGH</baz>\r\n" + //
				"</foo>";
		XMLSymbolSettings symbolSettings = new XMLSymbolSettings();
		XMLSymbolFilter filter = new XMLSymbolFilter();
		filter.setPattern("foo.xml");
		XMLSymbolExpressionFilter expressionFilter = new XMLSymbolExpressionFilter();

		filter.setExpressions(new XMLSymbolExpressionFilter[] { expressionFilter });
		symbolSettings.setFilters(new XMLSymbolFilter[] { filter });

		DocumentSymbol symbolsWithAllAttr = ds("foo", SymbolKind.Field, r(0, 0, 3, 6), r(0, 0, 3, 6), null, //
				Arrays.asList(//
						ds("bar", SymbolKind.Field, r(1, 1, 1, 46), r(1, 1, 1, 46), null, //
								Arrays.asList( //
										ds("@attr1: value1", SymbolKind.Constant, r(1, 6, 1, 20), r(1, 6, 1, 20), null, //
												Arrays.asList()), //
										ds("@attr2: value2", SymbolKind.Constant, r(1, 21, 1, 35), r(1, 21, 1, 35),
												null, //
												Arrays.asList()))), //
						ds("baz", SymbolKind.Field, r(2, 1, 2, 54), r(2, 1, 2, 54), null, //
								Arrays.asList( //
										ds("@attr1: baz-value1", SymbolKind.Constant, r(2, 6, 2, 24), r(2, 6, 2, 24),
												null, //
												Arrays.asList()), //
										ds("@attr2: baz-value2", SymbolKind.Constant, r(2, 25, 2, 43), r(2, 25, 2, 43),
												null, //
												Arrays.asList())))

				));

		// Test with //@*
		expressionFilter.setXpath("//@*");
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, symbolsWithAllAttr);

		DocumentSymbol symbolsWithBarAttr = ds("foo", SymbolKind.Field, r(0, 0, 3, 6), r(0, 0, 3, 6), null, //
				Arrays.asList(//
						ds("bar", SymbolKind.Field, r(1, 1, 1, 46), r(1, 1, 1, 46), null, //
								Arrays.asList( //
										ds("@attr1: value1", SymbolKind.Constant, r(1, 6, 1, 20), r(1, 6, 1, 20), null, //
												Arrays.asList()), //
										ds("@attr2: value2", SymbolKind.Constant, r(1, 21, 1, 35), r(1, 21, 1, 35),
												null, //
												Arrays.asList()))), //
						ds("baz", SymbolKind.Field, r(2, 1, 2, 54), r(2, 1, 2, 54), null, //
								Arrays.asList())

				));
		// Test with //bar/@*
		expressionFilter.setXpath("//bar/@*");
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, symbolsWithBarAttr);

		// Test with /foo/bar/@*
		expressionFilter.setXpath("/foo/bar/@*");
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, symbolsWithBarAttr);

	}

	@Test
	public void symbolsFilterWithOneAttr() {
		String xml = "<foo>\r\n" + //
				"	<bar attr1=\"value1\" attr2=\"value2\">ABCD</bar>\r\n" + //
				"	<baz attr1=\"baz-value1\" attr2=\"baz-value2\">EFGH</baz>\r\n" + //
				"</foo>";
		XMLSymbolSettings symbolSettings = new XMLSymbolSettings();
		XMLSymbolFilter filter = new XMLSymbolFilter();
		filter.setPattern("foo.xml");
		XMLSymbolExpressionFilter expressionFilter = new XMLSymbolExpressionFilter();
		filter.setExpressions(new XMLSymbolExpressionFilter[] { expressionFilter });
		symbolSettings.setFilters(new XMLSymbolFilter[] { filter });

		DocumentSymbol symbolsWithAllAttr2 = ds("foo", SymbolKind.Field, r(0, 0, 3, 6), r(0, 0, 3, 6), null, //
				Arrays.asList( //
						ds("bar", SymbolKind.Field, r(1, 1, 1, 46), r(1, 1, 1, 46), null, //
								Arrays.asList( //
										ds("@attr2: value2", SymbolKind.Constant, r(1, 21, 1, 35), r(1, 21, 1, 35),
												null, //
												Arrays.asList()))), //
						ds("baz", SymbolKind.Field, r(2, 1, 2, 54), r(2, 1, 2, 54), null, //
								Arrays.asList( //
										ds("@attr2: baz-value2", SymbolKind.Constant, r(2, 25, 2, 43), r(2, 25, 2, 43),
												null, //
												Arrays.asList())))));

		// Test with //@attr2
		expressionFilter.setXpath("//@attr2");
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				symbolsWithAllAttr2);

		DocumentSymbol symbolsWithBarAttr2 = ds("foo", SymbolKind.Field, r(0, 0, 3, 6), r(0, 0, 3, 6), null, //
				Arrays.asList( //
						ds("bar", SymbolKind.Field, r(1, 1, 1, 46), r(1, 1, 1, 46), null, //
								Arrays.asList( //
										ds("@attr2: value2", SymbolKind.Constant, r(1, 21, 1, 35), r(1, 21, 1, 35),
												null, //
												Arrays.asList()))), //
						ds("baz", SymbolKind.Field, r(2, 1, 2, 54), r(2, 1, 2, 54), null, Arrays.asList())));

		// Test with //bar/@attr2
		expressionFilter.setXpath("//bar/@attr2");
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				symbolsWithBarAttr2);

		// Test with /foo/bar/@attr2
		expressionFilter.setXpath("/foo/bar/@attr2");
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				symbolsWithBarAttr2);
	}

	@Test
	public void symbolsFilterWithText() {
		String xml = "<foo>\r\n" + //
				"	<bar attr1=\"value1\" attr2=\"value2\">ABCD</bar>\r\n" + //
				"	<baz attr1=\"baz-value1\" attr2=\"baz-value2\">EFGH</baz>\r\n" + //
				"</foo>";
		XMLSymbolSettings symbolSettings = new XMLSymbolSettings();
		XMLSymbolFilter filter = new XMLSymbolFilter();
		filter.setPattern("foo.xml");
		XMLSymbolExpressionFilter expressionFilter = new XMLSymbolExpressionFilter();

		filter.setExpressions(new XMLSymbolExpressionFilter[] { expressionFilter });
		symbolSettings.setFilters(new XMLSymbolFilter[] { filter });

		DocumentSymbol symbolWithAllText = ds("foo", SymbolKind.Field, r(0, 0, 3, 6), r(0, 0, 3, 6), null, //
				Arrays.asList(//
						ds("bar: ABCD", SymbolKind.Field, r(1, 1, 1, 46), r(1, 1, 1, 46), null, Arrays.asList()),
						ds("baz: EFGH", SymbolKind.Field, r(2, 1, 2, 54), r(2, 1, 2, 54), null, Arrays.asList())));

		// Test with //text()
		expressionFilter.setXpath("//text()");
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				symbolWithAllText);

		DocumentSymbol symbolWithBarText = ds("foo", SymbolKind.Field, r(0, 0, 3, 6), r(0, 0, 3, 6), null, //
				Arrays.asList(//
						ds("bar: ABCD", SymbolKind.Field, r(1, 1, 1, 46), r(1, 1, 1, 46), null, Arrays.asList()),
						ds("baz", SymbolKind.Field, r(2, 1, 2, 54), r(2, 1, 2, 54), null, Arrays.asList())));

		// Test with //bar/text()
		expressionFilter.setXpath("//bar/text()");
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				symbolWithBarText);

		// Test with /foo/bar/text()
		expressionFilter.setXpath("/foo/bar/text()");
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				symbolWithBarText);

	}

	@Test
	public void symbolsFilterExcludeElement() {
		String xml = "<foo>\r\n" + //
				"	<bar attr1=\"value1\" attr2=\"value2\">ABCD</bar>\r\n" + //
				"	<baz attr1=\"baz-value1\" attr2=\"baz-value2\">EFGH</baz>\r\n" + //
				"</foo>";
		XMLSymbolSettings symbolSettings = new XMLSymbolSettings();
		XMLSymbolFilter filter = new XMLSymbolFilter();
		filter.setPattern("foo.xml");
		XMLSymbolExpressionFilter expressionFilter = new XMLSymbolExpressionFilter();
		filter.setExpressions(new XMLSymbolExpressionFilter[] { expressionFilter });
		symbolSettings.setFilters(new XMLSymbolFilter[] { filter });

		// Exclude baz
		expressionFilter.setXpath("//baz");
		expressionFilter.setExcluded(true);
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				ds("foo", SymbolKind.Field, r(0, 0, 3, 6), r(0, 0, 3, 6), null, //
						Arrays.asList(//
								ds("bar", SymbolKind.Field, r(1, 1, 1, 46), r(1, 1, 1, 46), null, Arrays.asList()))));
	}

	@Test
	public void symbolsFilterWithNonInlineAttributeShowAttributeName(){
		String xml = "<foo>\r\n" + //
				"	<bar attr1=\"value1\" attr2=\"value2\">ABCD</bar>\r\n" + //
				"	<baz attr1=\"baz-value1\" attr2=\"baz-value2\">EFGH</baz>\r\n" + //
				"</foo>";
		XMLSymbolSettings symbolSettings = new XMLSymbolSettings();
		XMLSymbolFilter filter = new XMLSymbolFilter();
		filter.setPattern("foo.xml");
		XMLSymbolExpressionFilter expressionFilter = new XMLSymbolExpressionFilter();
		filter.setExpressions(new XMLSymbolExpressionFilter[] { expressionFilter });
		symbolSettings.setFilters(new XMLSymbolFilter[] { filter });

		DocumentSymbol symbolsWithAllAttr2NoInlineAttr = ds("foo", SymbolKind.Field, r(0, 0, 3, 6), r(0, 0, 3, 6), null, //
				Arrays.asList( //
						ds("bar", SymbolKind.Field, r(1, 1, 1, 46), r(1, 1, 1, 46), null, //
								Arrays.asList( //
										ds("@attr2: value2", SymbolKind.Constant, r(1, 21, 1, 35), r(1, 21, 1, 35),
												null, //
												Arrays.asList()))), //
						ds("baz", SymbolKind.Field, r(2, 1, 2, 54), r(2, 1, 2, 54), null, //
								Arrays.asList( //
										ds("@attr2: baz-value2", SymbolKind.Constant, r(2, 25, 2, 43), r(2, 25, 2, 43),
												null, //
												Arrays.asList())))));

		// Test with //@attr2
		// and showAttributeName=false
		// verify showAttributeName has no effect for non-inline (nested) attributes
		expressionFilter.setXpath("//@attr2");
		expressionFilter.setShowAttributeName(false);
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				symbolsWithAllAttr2NoInlineAttr);

		DocumentSymbol symbolsWithBarAttr2NoInlineAttr = ds("foo", SymbolKind.Field, r(0, 0, 3, 6), r(0, 0, 3, 6), null, //
				Arrays.asList( //
						ds("bar", SymbolKind.Field, r(1, 1, 1, 46), r(1, 1, 1, 46), null, //
								Arrays.asList( //
										ds("@attr2: value2", SymbolKind.Constant, r(1, 21, 1, 35), r(1, 21, 1, 35),
												null, //
												Arrays.asList()))), //
						ds("baz", SymbolKind.Field, r(2, 1, 2, 54), r(2, 1, 2, 54), null, Arrays.asList())));

		// Test with //bar/@attr2
		// and showAttributeName=false
		// verify showAttributeName has no effect for non-inline (nested) attributes
		// when specific elements are targeted
		expressionFilter.setXpath("//bar/@attr2");
		expressionFilter.setShowAttributeName(false);
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				symbolsWithBarAttr2NoInlineAttr);

		// Test with /foo/bar/@attr2
		// and showAttributeName=false
		// verify showAttributeName has no effect for non-inline (nested) attributes
		// when specific elements are targeted
		expressionFilter.setXpath("/foo/bar/@attr2");
		expressionFilter.setShowAttributeName(false);
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				symbolsWithBarAttr2NoInlineAttr);
	}

	@Test
	public void symbolsFilterWithInlineAttributeNoNestedAttributes(){
		String xml = "<foo>\r\n" + //
				"	<bar attr1=\"value1\" attr2=\"value2\">ABCD</bar>\r\n" + //
				"	<baz attr1=\"baz-value1\" attr2=\"baz-value2\">EFGH</baz>\r\n" + //
				"</foo>";
		XMLSymbolSettings symbolSettings = new XMLSymbolSettings();
		XMLSymbolFilter filter = new XMLSymbolFilter();
		filter.setPattern("foo.xml");
		XMLSymbolExpressionFilter expressionFilter = new XMLSymbolExpressionFilter();
		filter.setExpressions(new XMLSymbolExpressionFilter[] { expressionFilter });
		symbolSettings.setFilters(new XMLSymbolFilter[] { filter });

		DocumentSymbol symbolsWithAllAttr2InlineAttr = ds("foo", SymbolKind.Field, r(0, 0, 3, 6), r(0, 0, 3, 6), null, //
				Arrays.asList( //
						ds("bar: @attr2: value2", SymbolKind.Field, r(1, 1, 1, 46), r(1, 1, 1, 46), null, //
								Arrays.asList()), //
						ds("baz: @attr2: baz-value2", SymbolKind.Field, r(2, 1, 2, 54), r(2, 1, 2, 54), null, //
								Arrays.asList())));

		// Test with //@attr2
		// and showAttributeName=true, inlineAttribute=true
		expressionFilter.setXpath("//@attr2");
		expressionFilter.setInlineAttribute(true);
		expressionFilter.setShowAttributeName(true);
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				symbolsWithAllAttr2InlineAttr);

		DocumentSymbol symbolsWithAllAttr2InlineAttrNoName = ds("foo", SymbolKind.Field, r(0, 0, 3, 6), r(0, 0, 3, 6), null, //
				Arrays.asList( //
						ds("bar: value2", SymbolKind.Field, r(1, 1, 1, 46), r(1, 1, 1, 46), null, //
								Arrays.asList()), //
						ds("baz: baz-value2", SymbolKind.Field, r(2, 1, 2, 54), r(2, 1, 2, 54), null, //
								Arrays.asList())));

		// Test with //@attr2
		// and showAttributeName=false, inlineAttribute=true
		expressionFilter.setXpath("//@attr2");
		expressionFilter.setInlineAttribute(true);
		expressionFilter.setShowAttributeName(false);
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				symbolsWithAllAttr2InlineAttrNoName);

		DocumentSymbol symbolsWithBarAttr2InlineAttr = ds("foo", SymbolKind.Field, r(0, 0, 3, 6), r(0, 0, 3, 6), null, //
				Arrays.asList( //
						ds("bar: @attr2: value2", SymbolKind.Field, r(1, 1, 1, 46), r(1, 1, 1, 46), null, //
								Arrays.asList()), //
						ds("baz", SymbolKind.Field, r(2, 1, 2, 54), r(2, 1, 2, 54), null, Arrays.asList())));

		// Test with //bar/@attr2
		// and showAttributeName=true, inlineAttribute=true
		// verify when specific elements are targeted
		expressionFilter.setXpath("//bar/@attr2");
		expressionFilter.setInlineAttribute(true);
		expressionFilter.setShowAttributeName(true);
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				symbolsWithBarAttr2InlineAttr);

		// Test with /foo/bar/@attr2
		// and showAttributeName=true, inlineAttribute=true
		// verify when specific elements are targeted
		expressionFilter.setXpath("/foo/bar/@attr2");
		expressionFilter.setInlineAttribute(true);
		expressionFilter.setShowAttributeName(true);
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				symbolsWithBarAttr2InlineAttr);

		DocumentSymbol symbolsWithBarAttr2InlineAttrNoName = ds("foo", SymbolKind.Field, r(0, 0, 3, 6), r(0, 0, 3, 6), null, //
				Arrays.asList( //
						ds("bar: value2", SymbolKind.Field, r(1, 1, 1, 46), r(1, 1, 1, 46), null, //
								Arrays.asList()), //
						ds("baz", SymbolKind.Field, r(2, 1, 2, 54), r(2, 1, 2, 54), null, Arrays.asList())));

		// Test with //bar/@attr2
		// and showAttributeName=false, inlineAttribute=true
		// verify when specific elements are targeted
		expressionFilter.setXpath("//bar/@attr2");
		expressionFilter.setInlineAttribute(true);
		expressionFilter.setShowAttributeName(false);
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				symbolsWithBarAttr2InlineAttrNoName);

		// Test with /foo/bar/@attr2
		// and showAttributeName=false, inlineAttribute=true
		// verify when specific elements are targeted
		expressionFilter.setXpath("/foo/bar/@attr2");
		expressionFilter.setInlineAttribute(true);
		expressionFilter.setShowAttributeName(false);
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				symbolsWithBarAttr2InlineAttrNoName);
	}

	@Test
	public void symbolsFilterWithInlineAttributeWithNestedAttributes(){
		String xml = "<foo>\r\n" + //
				"	<bar attr1=\"value1\" attr2=\"value2\">ABCD</bar>\r\n" + //
				"	<baz attr1=\"baz-value1\" attr2=\"baz-value2\">EFGH</baz>\r\n" + //
				"</foo>";
		XMLSymbolSettings symbolSettings = new XMLSymbolSettings();
		XMLSymbolFilter filter = new XMLSymbolFilter();
		filter.setPattern("foo.xml");
		XMLSymbolExpressionFilter expressionFilter = new XMLSymbolExpressionFilter();
		XMLSymbolExpressionFilter expressionFilter2 = new XMLSymbolExpressionFilter();
		filter.setExpressions(new XMLSymbolExpressionFilter[] { expressionFilter, expressionFilter2 });
		symbolSettings.setFilters(new XMLSymbolFilter[] { filter });

		DocumentSymbol symbolsWithAllAttr2InlineAttrAndNestedAttr1 = ds("foo", SymbolKind.Field, r(0, 0, 3, 6), r(0, 0, 3, 6), null, //
				Arrays.asList( //
						ds("bar: @attr2: value2", SymbolKind.Field, r(1, 1, 1, 46), r(1, 1, 1, 46), null, //
								Arrays.asList( //
										ds("@attr1: value1", SymbolKind.Constant, r(1, 6, 1, 20), r(1, 6, 1, 20),
												null, //
												Arrays.asList()))), //
						ds("baz: @attr2: baz-value2", SymbolKind.Field, r(2, 1, 2, 54), r(2, 1, 2, 54), null, //
								Arrays.asList( //
										ds("@attr1: baz-value1", SymbolKind.Constant, r(2, 6, 2, 24), r(2, 6, 2, 24),
												null, //
												Arrays.asList()))))); //

		// Test with //@attr2
		// and showAttributeName=true, inlineAttribute=true
		// Test with nested //@attr1
		expressionFilter.setXpath("//@attr2");
		expressionFilter.setInlineAttribute(true);
		expressionFilter.setShowAttributeName(true);
		expressionFilter2.setXpath("//@attr1");
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				symbolsWithAllAttr2InlineAttrAndNestedAttr1);

		DocumentSymbol symbolsWithAllAttr2InlineAttrNoNameAndNestedAttr1 = ds("foo", SymbolKind.Field, r(0, 0, 3, 6), r(0, 0, 3, 6), null, //
				Arrays.asList( //
						ds("bar: value2", SymbolKind.Field, r(1, 1, 1, 46), r(1, 1, 1, 46), null, //
								Arrays.asList( //
										ds("@attr1: value1", SymbolKind.Constant, r(1, 6, 1, 20), r(1, 6, 1, 20),
												null, //
												Arrays.asList()))), //
						ds("baz: baz-value2", SymbolKind.Field, r(2, 1, 2, 54), r(2, 1, 2, 54), null, //
								Arrays.asList( //
										ds("@attr1: baz-value1", SymbolKind.Constant, r(2, 6, 2, 24), r(2, 6, 2, 24),
												null, //
												Arrays.asList()))))); //

		// Test with //@attr2
		// and showAttributeName=false, inlineAttribute=true
		// Test with nested //@attr1
		expressionFilter.setXpath("//@attr2");
		expressionFilter.setInlineAttribute(true);
		expressionFilter.setShowAttributeName(false);
		expressionFilter2.setXpath("//@attr1");
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				symbolsWithAllAttr2InlineAttrNoNameAndNestedAttr1);

		DocumentSymbol symbolsWithBarAttr2InlineAttrAndNestedAttr1 = ds("foo", SymbolKind.Field, r(0, 0, 3, 6), r(0, 0, 3, 6), null, //
				Arrays.asList( //
						ds("bar: @attr2: value2", SymbolKind.Field, r(1, 1, 1, 46), r(1, 1, 1, 46), null, //
								Arrays.asList( //
										ds("@attr1: value1", SymbolKind.Constant, r(1, 6, 1, 20), r(1, 6, 1, 20),
												null, //
												Arrays.asList()))), //
						ds("baz", SymbolKind.Field, r(2, 1, 2, 54), r(2, 1, 2, 54), null, Arrays.asList())));

		// Test with //bar/@attr2
		// and showAttributeName=true, inlineAttribute=true
		// Test with nested //bar/@attr1
		// verify when specific elements are targeted
		expressionFilter.setXpath("//bar/@attr2");
		expressionFilter.setInlineAttribute(true);
		expressionFilter.setShowAttributeName(true);
		expressionFilter2.setXpath("//bar/@attr1");
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				symbolsWithBarAttr2InlineAttrAndNestedAttr1);

		// Test with /foo/bar/@attr2
		// and showAttributeName=true, inlineAttribute=true
		// Test with nested /foo/bar/@attr1
		// verify when specific elements are targeted
		expressionFilter.setXpath("/foo/bar/@attr2");
		expressionFilter.setInlineAttribute(true);
		expressionFilter.setShowAttributeName(true);
		expressionFilter2.setXpath("/foo/bar/@attr1");
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				symbolsWithBarAttr2InlineAttrAndNestedAttr1);

		DocumentSymbol symbolsWithBarAttr2InlineAttrNoNameAndNestedAttr1 = ds("foo", SymbolKind.Field, r(0, 0, 3, 6), r(0, 0, 3, 6), null, //
				Arrays.asList( //
						ds("bar: value2", SymbolKind.Field, r(1, 1, 1, 46), r(1, 1, 1, 46), null, //
								Arrays.asList( //
										ds("@attr1: value1", SymbolKind.Constant, r(1, 6, 1, 20), r(1, 6, 1, 20),
												null, //
												Arrays.asList()))), //
						ds("baz", SymbolKind.Field, r(2, 1, 2, 54), r(2, 1, 2, 54), null, Arrays.asList())));

		// Test with //bar/@attr2
		// and showAttributeName=false, inlineAttribute=true
		// Test with nested //bar/@attr1
		// verify when specific elements are targeted
		expressionFilter.setXpath("//bar/@attr2");
		expressionFilter.setInlineAttribute(true);
		expressionFilter.setShowAttributeName(false);
		expressionFilter2.setXpath("/foo/bar/@attr1");
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				symbolsWithBarAttr2InlineAttrNoNameAndNestedAttr1);

		// Test with /foo/bar/@attr2
		// and showAttributeName=false, inlineAttribute=true
		// Test with nested /foo/bar/@attr1
		// verify when specific elements are targeted
		expressionFilter.setXpath("/foo/bar/@attr2");
		expressionFilter.setInlineAttribute(true);
		expressionFilter.setShowAttributeName(false);
		expressionFilter2.setXpath("/foo/bar/@attr1");
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				symbolsWithBarAttr2InlineAttrNoNameAndNestedAttr1);
	}

	@Test
	public void symbolsFilterWithInlineAttributeMultipleInlineAttributes(){
		String xml = "<foo>\r\n" + //
				"	<bar attr1=\"value1\" attr2=\"value2\">ABCD</bar>\r\n" + //
				"	<baz attr1=\"baz-value1\" attr2=\"baz-value2\">EFGH</baz>\r\n" + //
				"	<bar attr3=\"value3\" attr2=\"value2\">IJKL</bar>\r\n" + //
				"</foo>";
		XMLSymbolSettings symbolSettings = new XMLSymbolSettings();
		XMLSymbolFilter filter = new XMLSymbolFilter();
		filter.setPattern("foo.xml");
		XMLSymbolExpressionFilter expressionFilter = new XMLSymbolExpressionFilter();
		XMLSymbolExpressionFilter expressionFilter2 = new XMLSymbolExpressionFilter();
		filter.setExpressions(new XMLSymbolExpressionFilter[] { expressionFilter, expressionFilter2 });
		symbolSettings.setFilters(new XMLSymbolFilter[] { filter });

		DocumentSymbol symbolsWithAllAttr1AndAttr3Inline = ds("foo", SymbolKind.Field, r(0, 0, 4, 6), r(0, 0, 4, 6), null, //
				Arrays.asList( //
						ds("bar: value1", SymbolKind.Field, r(1, 1, 1, 46), r(1, 1, 1, 46), null, //
								Arrays.asList()), //
						ds("baz: baz-value1", SymbolKind.Field, r(2, 1, 2, 54), r(2, 1, 2, 54), null, //
								Arrays.asList()), //
						ds("bar: @attr3: value3", SymbolKind.Field, r(3, 1, 3, 46), r(3, 1, 3, 46), null, //
								Arrays.asList()))); //

		// Test with //@attr1
		// and showAttributeName=false, inlineAttribute=true
		// Test with second inline filter //@attr3
		// and showAttributeName=true, inlineAttribute=true
		expressionFilter.setXpath("//@attr1");
		expressionFilter.setInlineAttribute(true);
		expressionFilter.setShowAttributeName(false);
		expressionFilter2.setXpath("//@attr3");
		expressionFilter2.setInlineAttribute(true);
		expressionFilter2.setShowAttributeName(true);
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				symbolsWithAllAttr1AndAttr3Inline);

		DocumentSymbol symbolsWithBarAttr1AndAttr3Inline = ds("foo", SymbolKind.Field, r(0, 0, 4, 6), r(0, 0, 4, 6), null, //
				Arrays.asList( //
						ds("bar: value1", SymbolKind.Field, r(1, 1, 1, 46), r(1, 1, 1, 46), null, //
								Arrays.asList()), //
						ds("baz", SymbolKind.Field, r(2, 1, 2, 54), r(2, 1, 2, 54), null, //
								Arrays.asList()), //
						ds("bar: @attr3: value3", SymbolKind.Field, r(3, 1, 3, 46), r(3, 1, 3, 46), null, //
								Arrays.asList()))); //

		// Test with //bar/@attr1
		// and showAttributeName=false, inlineAttribute=true
		// Test with second inline filter //bar/@attr3
		// and showAttributeName=true, inlineAttribute=true
		// verify when specific elements are targeted
		expressionFilter.setXpath("//bar/@attr1");
		expressionFilter.setInlineAttribute(true);
		expressionFilter.setShowAttributeName(false);
		expressionFilter2.setXpath("//bar/@attr3");
		expressionFilter2.setInlineAttribute(true);
		expressionFilter2.setShowAttributeName(true);
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				symbolsWithBarAttr1AndAttr3Inline);

		// Test with /foo/bar/@attr1
		// and showAttributeName=false, inlineAttribute=true
		// Test with second inline filter /foo/bar/@attr3
		// and showAttributeName=true, inlineAttribute=true
		// verify when specific elements are targeted
		expressionFilter.setXpath("/foo/bar/@attr1");
		expressionFilter.setInlineAttribute(true);
		expressionFilter.setShowAttributeName(false);
		expressionFilter2.setXpath("/foo/bar/@attr3");
		expressionFilter2.setInlineAttribute(true);
		expressionFilter2.setShowAttributeName(true);
		testDocumentSymbolsFor(xml, "file:///test/foo.xml", symbolSettings, //
				symbolsWithBarAttr1AndAttr3Inline);
	}


}
