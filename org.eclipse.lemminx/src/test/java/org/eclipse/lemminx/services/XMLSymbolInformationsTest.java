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
package org.eclipse.lemminx.services;

import static org.eclipse.lemminx.XMLAssert.l;
import static org.eclipse.lemminx.XMLAssert.r;
import static org.eclipse.lemminx.XMLAssert.si;
import static org.eclipse.lemminx.XMLAssert.testSymbolInformationsFor;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.settings.XMLSymbolExpressionFilter;
import org.eclipse.lemminx.settings.XMLSymbolFilter;
import org.eclipse.lemminx.settings.XMLSymbolSettings;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.junit.jupiter.api.Test;

/**
 * XML symbols test with {@link SymbolInformation}.
 */
public class XMLSymbolInformationsTest {
	private static final String testURI = "test:URI";

	@Test
	public void testSingleSymbol() {
		String xml = "<project></project>";
		testSymbolInformationsFor(xml, testURI, //
				si("project", SymbolKind.Field, l(testURI, r(0, 0, 0, 19)), ""));
	}

	@Test
	public void testNestedSymbol() {
		String xml = "<project><inside></inside></project>";
		testSymbolInformationsFor(xml, testURI, //
				si("project", SymbolKind.Field, l(testURI, r(0, 0, 0, 36)), ""), //
				si("inside", SymbolKind.Field, l(testURI, r(0, 9, 0, 26)), "project"));
	}

	@Test
	public void testTwoNestedSymbols() {
		String xml = "<a><b></b><c></c></a>";
		testSymbolInformationsFor(xml, testURI, //
				si("a", SymbolKind.Field, l(testURI, r(0, 0, 0, 21)), ""), //
				si("b", SymbolKind.Field, l(testURI, r(0, 3, 0, 10)), "a"), //
				si("c", SymbolKind.Field, l(testURI, r(0, 10, 0, 17)), "a"));
	}

	@Test
	public void testNestedTwice() {
		String xml = "<a><b><c></c></b></a>";
		testSymbolInformationsFor(xml, testURI, //
				si("a", SymbolKind.Field, l(testURI, r(0, 0, 0, 21)), ""), //
				si("b", SymbolKind.Field, l(testURI, r(0, 3, 0, 17)), "a"), //
				si("c", SymbolKind.Field, l(testURI, r(0, 6, 0, 13)), "b"));
	}

	@Test
	public void testSelfClosingTag() {
		String xml = "<a/>";
		testSymbolInformationsFor(xml, testURI, //
				si("a", SymbolKind.Field, l(testURI, r(0, 0, 0, 4)), ""));
	}

	@Test
	public void testNestedSelfClosingTag() {
		String xml = "<a><b/></a>";
		testSymbolInformationsFor(xml, testURI, //
				si("a", SymbolKind.Field, l(testURI, r(0, 0, 0, 11)), ""), //
				si("b", SymbolKind.Field, l(testURI, r(0, 3, 0, 7)), "a"));
	}

	@Test
	public void testUnclosedTag() {
		String xml = "<a>";
		testSymbolInformationsFor(xml, testURI, //
				si("a", SymbolKind.Field, l(testURI, r(0, 0, 0, 3)), ""));
	}

	@Test
	public void testNestedUnclosedTag() {
		String xml = "<a><b></a>";
		testSymbolInformationsFor(xml, testURI, //
				si("a", SymbolKind.Field, l(testURI, r(0, 0, 0, 10)), ""), //
				si("b", SymbolKind.Field, l(testURI, r(0, 3, 0, 6)), "a"));
	}

	@Test
	public void testAllTagsUnclosed() {
		String xml = "<a><b>";
		testSymbolInformationsFor(xml, testURI, //
				si("a", SymbolKind.Field, l(testURI, r(0, 0, 0, 6)), ""), //
				si("b", SymbolKind.Field, l(testURI, r(0, 3, 0, 6)), "a"));
	}

	@Test
	public void singleEndTag() throws BadLocationException {
		String xml = "</meta>";
		testSymbolInformationsFor(xml, testURI, //
				si("meta", SymbolKind.Field, l(testURI, r(0, 0, 0, 7)), ""));
	}

	@Test
	public void invalidEndTag() {
		String xml = "</";
		testSymbolInformationsFor(xml, testURI, //
				si("?", SymbolKind.Field, l(testURI, r(0, 0, 0, 2)), ""));
	}

	@Test
	public void invalidEndTagAfterRoot() {
		String xml = "<a></";
		testSymbolInformationsFor(xml, testURI, //
				si("a", SymbolKind.Field, l(testURI, r(0, 0, 0, 5)), ""), //
				si("?", SymbolKind.Field, l(testURI, r(0, 3, 0, 5)), "a"));
	}

	@Test
	public void externalDTD() {
		String xml = "<!ELEMENT br EMPTY>\n" + //
				"<!ATTLIST br\n" + //
				"	%all;>";
		String testURI = "test.dtd";
		testSymbolInformationsFor(xml, testURI, //
				si("br", SymbolKind.Property, l(testURI, r(0, 0, 0, 19)), ""), //
				si("%all;", SymbolKind.Key, l(testURI, r(2, 1, 2, 6)), ""));
	}

	@Test
	public void internalDTD() {
		String xml = "<!DOCTYPE br [\n" + //
				"  	<!ELEMENT br EMPTY>\n" + //
				"	<!ATTLIST br\n" + //
				"		%all;>\n" + //
				"]>\n" + //
				"<br />";
		String testURI = "test.xml";
		testSymbolInformationsFor(xml, testURI, //
				si("DOCTYPE:br", SymbolKind.Struct, l(testURI, r(0, 0, 4, 2)), ""), //
				si("br", SymbolKind.Property, l(testURI, r(1, 3, 1, 22)), "DOCTYPE:br"), //
				si("%all;", SymbolKind.Key, l(testURI, r(3, 2, 3, 7)), "DOCTYPE:br"), //
				si("br", SymbolKind.Field, l(testURI, r(5, 0, 5, 6)), ""));
	}

	@Test
	public void exceedSymbolLimit() {
		String xml = "<!DOCTYPE br [\n" + //
				"  	<!ELEMENT br EMPTY>\n" + //
				"	<!ATTLIST br\n" + //
				"		%all;>\n" + //
				"]>\n" + //
				"<br />";
		String testURI = "test.xml";
		XMLSymbolSettings settings = new XMLSymbolSettings();

		settings.setMaxItemsComputed(4);
		testSymbolInformationsFor(xml, testURI, settings, //
				si("DOCTYPE:br", SymbolKind.Struct, l(testURI, r(0, 0, 4, 2)), ""), //
				si("br", SymbolKind.Property, l(testURI, r(1, 3, 1, 22)), "DOCTYPE:br"), //
				si("%all;", SymbolKind.Key, l(testURI, r(3, 2, 3, 7)), "DOCTYPE:br"), //
				si("br", SymbolKind.Field, l(testURI, r(5, 0, 5, 6)), ""));

		settings.setMaxItemsComputed(10);
		testSymbolInformationsFor(xml, testURI, settings, //
				si("DOCTYPE:br", SymbolKind.Struct, l(testURI, r(0, 0, 4, 2)), ""), //
				si("br", SymbolKind.Property, l(testURI, r(1, 3, 1, 22)), "DOCTYPE:br"), //
				si("%all;", SymbolKind.Key, l(testURI, r(3, 2, 3, 7)), "DOCTYPE:br"), //
				si("br", SymbolKind.Field, l(testURI, r(5, 0, 5, 6)), ""));

		settings.setMaxItemsComputed(3);
		testSymbolInformationsFor(xml, testURI, settings, //
				si("DOCTYPE:br", SymbolKind.Struct, l(testURI, r(0, 0, 4, 2)), ""), //
				si("br", SymbolKind.Property, l(testURI, r(1, 3, 1, 22)), "DOCTYPE:br"), //
				si("%all;", SymbolKind.Key, l(testURI, r(3, 2, 3, 7)), "DOCTYPE:br"));
	}

	@Test
	public void zeroSymbolLimit() {
		String xml = "<root>\n" + //
				"  <content />\n" + //
				"</root>";
		String testURI = "test.xml";
		XMLSymbolSettings settings = new XMLSymbolSettings();
		settings.setMaxItemsComputed(0);

		testSymbolInformationsFor(xml, testURI, settings);
	}

	// Tests for Symbols filter

	@Test
	public void noSymbolsFilter() {
		String xml = "<foo>\r\n" + //
				"	<bar attr1=\"value1\" attr2=\"value2\">ABCD</bar>\r\n" + //
				"</foo>";
		String testURI = "file:///test/foo.xml";
		// null symbols filter
		XMLSymbolSettings symbolSettings = new XMLSymbolSettings();
		testSymbolInformationsFor(xml, testURI, symbolSettings, //
				si("foo", SymbolKind.Field, l(testURI, r(0, 0, 2, 6)), ""), //
				si("bar", SymbolKind.Field, l(testURI, r(1, 1, 1, 46)), "foo"));

		// empty array symbols filter
		symbolSettings = new XMLSymbolSettings();
		symbolSettings.setFilters(new XMLSymbolFilter[0]);
		testSymbolInformationsFor(xml, testURI, symbolSettings, //
				si("foo", SymbolKind.Field, l(testURI, r(0, 0, 2, 6)), ""), //
				si("bar", SymbolKind.Field, l(testURI, r(1, 1, 1, 46)), "foo"));
	}

	@Test
	public void symbolsFilterWithAllAttr() {
		String xml = "<foo>\r\n" + //
				"	<bar attr1=\"value1\" attr2=\"value2\">ABCD</bar>\r\n" + //
				"	<baz attr1=\"baz-value1\" attr2=\"baz-value2\">EFGH</baz>\r\n" + //
				"</foo>";
		String testURI = "file:///test/foo.xml";
		XMLSymbolSettings symbolSettings = new XMLSymbolSettings();
		XMLSymbolFilter filter = new XMLSymbolFilter();
		filter.setPattern("foo.xml");
		XMLSymbolExpressionFilter expressionFilter = new XMLSymbolExpressionFilter();

		filter.setExpressions(new XMLSymbolExpressionFilter[] { expressionFilter });
		symbolSettings.setFilters(new XMLSymbolFilter[] { filter });

		// Test with //@*
		expressionFilter.setXpath("//@*");
		testSymbolInformationsFor(xml, testURI, symbolSettings, //
				si("foo", SymbolKind.Field, l(testURI, r(0, 0, 3, 6)), ""), //
				si("bar", SymbolKind.Field, l(testURI, r(1, 1, 1, 46)), "foo"), //
				si("@attr1: value1", SymbolKind.Constant, l(testURI, r(1, 6, 1, 20)), "bar"), //
				si("@attr2: value2", SymbolKind.Constant, l(testURI, r(1, 21, 1, 35)), "bar"), //
				si("baz", SymbolKind.Field, l(testURI, r(2, 1, 2, 54)), "foo"), //
				si("@attr1: baz-value1", SymbolKind.Constant, l(testURI, r(2, 6, 2, 24)), "baz"), //
				si("@attr2: baz-value2", SymbolKind.Constant, l(testURI, r(2, 25, 2, 43)), "baz"));

		// Test with //bar/@*
		expressionFilter.setXpath("//bar/@*");
		testSymbolInformationsFor(xml, testURI, symbolSettings, //
				si("foo", SymbolKind.Field, l(testURI, r(0, 0, 3, 6)), ""), //
				si("bar", SymbolKind.Field, l(testURI, r(1, 1, 1, 46)), "foo"), //
				si("@attr1: value1", SymbolKind.Constant, l(testURI, r(1, 6, 1, 20)), "bar"), //
				si("@attr2: value2", SymbolKind.Constant, l(testURI, r(1, 21, 1, 35)), "bar"), //
				si("baz", SymbolKind.Field, l(testURI, r(2, 1, 2, 54)), "foo"));

		// Test with /foo/bar/@*
		expressionFilter.setXpath("/foo/bar/@*");
		testSymbolInformationsFor(xml, testURI, symbolSettings, //
				si("foo", SymbolKind.Field, l(testURI, r(0, 0, 3, 6)), ""), //
				si("bar", SymbolKind.Field, l(testURI, r(1, 1, 1, 46)), "foo"), //
				si("@attr1: value1", SymbolKind.Constant, l(testURI, r(1, 6, 1, 20)), "bar"), //
				si("@attr2: value2", SymbolKind.Constant, l(testURI, r(1, 21, 1, 35)), "bar"), //
				si("baz", SymbolKind.Field, l(testURI, r(2, 1, 2, 54)), "foo"));

	}

	@Test
	public void symbolsFilterWithOneAttr() {
		String xml = "<foo>\r\n" + //
				"	<bar attr1=\"value1\" attr2=\"value2\">ABCD</bar>\r\n" + //
				"	<baz attr1=\"baz-value1\" attr2=\"baz-value2\">EFGH</baz>\r\n" + //
				"</foo>";
		String testURI = "file:///test/foo.xml";
		XMLSymbolSettings symbolSettings = new XMLSymbolSettings();
		XMLSymbolFilter filter = new XMLSymbolFilter();
		filter.setPattern("foo.xml");
		XMLSymbolExpressionFilter expressionFilter = new XMLSymbolExpressionFilter();
		filter.setExpressions(new XMLSymbolExpressionFilter[] { expressionFilter });
		symbolSettings.setFilters(new XMLSymbolFilter[] { filter });

		// Test with //@attr2
		expressionFilter.setXpath("//@attr2");
		testSymbolInformationsFor(xml, testURI, symbolSettings, //
				si("foo", SymbolKind.Field, l(testURI, r(0, 0, 3, 6)), ""), //
				si("bar", SymbolKind.Field, l(testURI, r(1, 1, 1, 46)), "foo"), //
				si("@attr2: value2", SymbolKind.Constant, l(testURI, r(1, 21, 1, 35)), "bar"), //
				si("baz", SymbolKind.Field, l(testURI, r(2, 1, 2, 54)), "foo"), //
				si("@attr2: baz-value2", SymbolKind.Constant, l(testURI, r(2, 25, 2, 43)), "baz"));

		// Test with //bar/@attr2
		expressionFilter.setXpath("//bar/@attr2");
		testSymbolInformationsFor(xml, testURI, symbolSettings, //
				si("foo", SymbolKind.Field, l(testURI, r(0, 0, 3, 6)), ""), //
				si("bar", SymbolKind.Field, l(testURI, r(1, 1, 1, 46)), "foo"), //
				si("@attr2: value2", SymbolKind.Constant, l(testURI, r(1, 21, 1, 35)), "bar"), //
				si("baz", SymbolKind.Field, l(testURI, r(2, 1, 2, 54)), "foo"));

		// Test with /foo/bar/@attr2
		expressionFilter.setXpath("/foo/bar/@attr2");
		testSymbolInformationsFor(xml, testURI, symbolSettings, //
				si("foo", SymbolKind.Field, l(testURI, r(0, 0, 3, 6)), ""), //
				si("bar", SymbolKind.Field, l(testURI, r(1, 1, 1, 46)), "foo"), //
				si("@attr2: value2", SymbolKind.Constant, l(testURI, r(1, 21, 1, 35)), "bar"), //
				si("baz", SymbolKind.Field, l(testURI, r(2, 1, 2, 54)), "foo"));
	}

	@Test
	public void symbolsFilterWithText() {
		String xml = "<foo>\r\n" + //
				"	<bar attr1=\"value1\" attr2=\"value2\">ABCD</bar>\r\n" + //
				"	<baz attr1=\"baz-value1\" attr2=\"baz-value2\">EFGH</baz>\r\n" + //
				"</foo>";
		String testURI = "file:///test/foo.xml";
		XMLSymbolSettings symbolSettings = new XMLSymbolSettings();
		XMLSymbolFilter filter = new XMLSymbolFilter();
		filter.setPattern("foo.xml");
		XMLSymbolExpressionFilter expressionFilter = new XMLSymbolExpressionFilter();

		filter.setExpressions(new XMLSymbolExpressionFilter[] { expressionFilter });
		symbolSettings.setFilters(new XMLSymbolFilter[] { filter });

		// Test with //text()
		expressionFilter.setXpath("//text()");
		testSymbolInformationsFor(xml, testURI, symbolSettings, //
				si("foo", SymbolKind.Field, l(testURI, r(0, 0, 3, 6)), ""), //
				si("bar: ABCD", SymbolKind.Field, l(testURI, r(1, 1, 1, 46)), "foo"), //
				si("baz: EFGH", SymbolKind.Field, l(testURI, r(2, 1, 2, 54)), "foo"));

		// Test with //bar/text()
		expressionFilter.setXpath("//bar/text()");
		testSymbolInformationsFor(xml, testURI, symbolSettings, //
				si("foo", SymbolKind.Field, l(testURI, r(0, 0, 3, 6)), ""), //
				si("bar: ABCD", SymbolKind.Field, l(testURI, r(1, 1, 1, 46)), "foo"), //
				si("baz", SymbolKind.Field, l(testURI, r(2, 1, 2, 54)), "foo"));

		// Test with /foo/bar/text()
		expressionFilter.setXpath("/foo/bar/text()");
		testSymbolInformationsFor(xml, testURI, symbolSettings, //
				si("foo", SymbolKind.Field, l(testURI, r(0, 0, 3, 6)), ""), //
				si("bar: ABCD", SymbolKind.Field, l(testURI, r(1, 1, 1, 46)), "foo"), //
				si("baz", SymbolKind.Field, l(testURI, r(2, 1, 2, 54)), "foo"));

	}

	@Test
	public void symbolsFilterExcludeElement() {
		String xml = "<foo>\r\n" + //
				"	<bar attr1=\"value1\" attr2=\"value2\">ABCD</bar>\r\n" + //
				"	<baz attr1=\"baz-value1\" attr2=\"baz-value2\">EFGH</baz>\r\n" + //
				"</foo>";
		String testURI = "file:///test/foo.xml";
		XMLSymbolSettings symbolSettings = new XMLSymbolSettings();
		XMLSymbolFilter filter = new XMLSymbolFilter();
		filter.setPattern("foo.xml");
		XMLSymbolExpressionFilter expressionFilter = new XMLSymbolExpressionFilter();
		filter.setExpressions(new XMLSymbolExpressionFilter[] { expressionFilter });
		symbolSettings.setFilters(new XMLSymbolFilter[] { filter });

		// Exclude baz
		expressionFilter.setXpath("//baz");
		expressionFilter.setExcluded(true);
		testSymbolInformationsFor(xml, testURI, symbolSettings, //
				si("foo", SymbolKind.Field, l(testURI, r(0, 0, 3, 6)), ""), //
				si("bar", SymbolKind.Field, l(testURI, r(1, 1, 1, 46)), "foo"));
	}
}