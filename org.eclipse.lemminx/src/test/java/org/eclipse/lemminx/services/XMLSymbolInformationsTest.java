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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMParser;
import org.eclipse.lemminx.settings.XMLSymbolSettings;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * XML symbols test with {@link SymbolInformation}.
 */
public class XMLSymbolInformationsTest {
	private static final String testURI = "test:URI";
	private XMLLanguageService languageService;
	private DOMDocument xmlDocument;
	private List<SymbolInformation> actualSymbolInfos;
	private Location currentLocation;
	private SymbolInformation currentSymbolInfo;

	@BeforeEach
	public void initializeLanguageService() {
		languageService = new XMLLanguageService();
	}

	@Test
	public void testSingleSymbol() {
		String xmlText = "<project></project>";
		initializeTestObjects(xmlText, testURI);

		List<SymbolInformation> expectedSymbolInfos = new ArrayList<SymbolInformation>();
		currentLocation = createLocation(testURI, 0, 19, xmlDocument);
		currentSymbolInfo = createSymbolInformation("project", SymbolKind.Field, currentLocation, "");
		expectedSymbolInfos.add(currentSymbolInfo);

		assertSymbols(expectedSymbolInfos, actualSymbolInfos);
	}

	@Test
	public void testNestedSymbol() {
		String xmlText = "<project><inside></inside></project>";
		initializeTestObjects(xmlText, testURI);

		List<SymbolInformation> expectedSymbolInfos = new ArrayList<SymbolInformation>();
		currentLocation = createLocation(testURI, 0, 36, xmlDocument);
		currentSymbolInfo = createSymbolInformation("project", SymbolKind.Field, currentLocation, "");
		expectedSymbolInfos.add(currentSymbolInfo);

		currentLocation = createLocation(testURI, 9, 26, xmlDocument);
		currentSymbolInfo = createSymbolInformation("inside", SymbolKind.Field, currentLocation, "project");
		expectedSymbolInfos.add(currentSymbolInfo);

		assertSymbols(expectedSymbolInfos, actualSymbolInfos);
	}

	@Test
	public void testTwoNestedSymbols() {
		String xmlText = "<a><b></b><c></c></a>";
		initializeTestObjects(xmlText, testURI);

		List<SymbolInformation> expectedSymbolInfos = new ArrayList<SymbolInformation>();
		currentLocation = createLocation(testURI, 0, 21, xmlDocument);
		currentSymbolInfo = createSymbolInformation("a", SymbolKind.Field, currentLocation, "");
		expectedSymbolInfos.add(currentSymbolInfo);

		currentLocation = createLocation(testURI, 3, 10, xmlDocument);
		currentSymbolInfo = createSymbolInformation("b", SymbolKind.Field, currentLocation, "a");
		expectedSymbolInfos.add(currentSymbolInfo);

		currentLocation = createLocation(testURI, 10, 17, xmlDocument);
		currentSymbolInfo = createSymbolInformation("c", SymbolKind.Field, currentLocation, "a");
		expectedSymbolInfos.add(currentSymbolInfo);

		assertSymbols(expectedSymbolInfos, actualSymbolInfos);
	}

	@Test
	public void testNestedTwice() {
		String xmlText = "<a><b><c></c></b></a>";
		initializeTestObjects(xmlText, testURI);

		List<SymbolInformation> expectedSymbolInfos = new ArrayList<SymbolInformation>();
		currentLocation = createLocation(testURI, 0, 21, xmlDocument);
		currentSymbolInfo = createSymbolInformation("a", SymbolKind.Field, currentLocation, "");
		expectedSymbolInfos.add(currentSymbolInfo);

		currentLocation = createLocation(testURI, 3, 17, xmlDocument);
		currentSymbolInfo = createSymbolInformation("b", SymbolKind.Field, currentLocation, "a");
		expectedSymbolInfos.add(currentSymbolInfo);

		currentLocation = createLocation(testURI, 6, 13, xmlDocument);
		currentSymbolInfo = createSymbolInformation("c", SymbolKind.Field, currentLocation, "b");
		expectedSymbolInfos.add(currentSymbolInfo);

		assertSymbols(expectedSymbolInfos, actualSymbolInfos);
	}

	@Test
	public void testSelfClosingTag() {
		String xmlText = "<a/>";
		initializeTestObjects(xmlText, testURI);

		List<SymbolInformation> expectedSymbolInfos = new ArrayList<SymbolInformation>();
		currentLocation = createLocation(testURI, 0, 4, xmlDocument);
		currentSymbolInfo = createSymbolInformation("a", SymbolKind.Field, currentLocation, "");
		expectedSymbolInfos.add(currentSymbolInfo);

		assertSymbols(expectedSymbolInfos, actualSymbolInfos);
	}

	@Test
	public void testNestedSelfClosingTag() {
		String xmlText = "<a><b/></a>";
		initializeTestObjects(xmlText, testURI);

		List<SymbolInformation> expectedSymbolInfos = new ArrayList<SymbolInformation>();
		currentLocation = createLocation(testURI, 0, 11, xmlDocument);
		currentSymbolInfo = createSymbolInformation("a", SymbolKind.Field, currentLocation, "");
		expectedSymbolInfos.add(currentSymbolInfo);

		currentLocation = createLocation(testURI, 3, 7, xmlDocument);
		currentSymbolInfo = createSymbolInformation("b", SymbolKind.Field, currentLocation, "a");
		expectedSymbolInfos.add(currentSymbolInfo);

		assertSymbols(expectedSymbolInfos, actualSymbolInfos);
	}

	@Test
	public void testUnclosedTag() {
		String xmlText = "<a>";
		initializeTestObjects(xmlText, testURI);

		List<SymbolInformation> expectedSymbolInfos = new ArrayList<SymbolInformation>();
		currentLocation = createLocation(testURI, 0, 3, xmlDocument);
		currentSymbolInfo = createSymbolInformation("a", SymbolKind.Field, currentLocation, "");
		expectedSymbolInfos.add(currentSymbolInfo);

		assertSymbols(expectedSymbolInfos, actualSymbolInfos);
	}

	@Test
	public void testNestedUnclosedTag() {
		String xmlText = "<a><b></a>";
		initializeTestObjects(xmlText, testURI);

		List<SymbolInformation> expectedSymbolInfos = new ArrayList<SymbolInformation>();
		currentLocation = createLocation(testURI, 0, 10, xmlDocument);
		currentSymbolInfo = createSymbolInformation("a", SymbolKind.Field, currentLocation, "");
		expectedSymbolInfos.add(currentSymbolInfo);

		currentLocation = createLocation(testURI, 3, 6, xmlDocument);
		currentSymbolInfo = createSymbolInformation("b", SymbolKind.Field, currentLocation, "a");
		expectedSymbolInfos.add(currentSymbolInfo);

		assertSymbols(expectedSymbolInfos, actualSymbolInfos);
	}

	@Test
	public void testAllTagsUnclosed() {
		String xmlText = "<a><b>";
		initializeTestObjects(xmlText, testURI);

		List<SymbolInformation> expectedSymbolInfos = new ArrayList<SymbolInformation>();
		currentLocation = createLocation(testURI, 0, 6, xmlDocument);
		currentSymbolInfo = createSymbolInformation("a", SymbolKind.Field, currentLocation, "");
		expectedSymbolInfos.add(currentSymbolInfo);

		currentLocation = createLocation(testURI, 3, 6, xmlDocument);
		currentSymbolInfo = createSymbolInformation("b", SymbolKind.Field, currentLocation, "a");
		expectedSymbolInfos.add(currentSymbolInfo);

		assertSymbols(expectedSymbolInfos, actualSymbolInfos);
	}

	@Test
	public void singleEndTag() throws BadLocationException {
		String xmlText = "</meta>";
		initializeTestObjects(xmlText, testURI);

		List<SymbolInformation> expectedSymbolInfos = new ArrayList<SymbolInformation>();
		currentLocation = createLocation(testURI, 0, 7, xmlDocument);
		currentSymbolInfo = createSymbolInformation("meta", SymbolKind.Field, currentLocation, "");
		expectedSymbolInfos.add(currentSymbolInfo);

		assertSymbols(expectedSymbolInfos, actualSymbolInfos);

	}

	@Test
	public void invalidEndTag() {
		String xmlText = "</";
		initializeTestObjects(xmlText, testURI);

		List<SymbolInformation> expectedSymbolInfos = new ArrayList<SymbolInformation>();
		currentLocation = createLocation(testURI, 0, 2, xmlDocument);
		currentSymbolInfo = createSymbolInformation("?", SymbolKind.Field, currentLocation, "");
		expectedSymbolInfos.add(currentSymbolInfo);

		assertSymbols(expectedSymbolInfos, actualSymbolInfos);
	}

	@Test
	public void invalidEndTagAfterRoot() {
		String xmlText = "<a></";
		initializeTestObjects(xmlText, testURI);

		List<SymbolInformation> expectedSymbolInfos = new ArrayList<SymbolInformation>();
		currentLocation = createLocation(testURI, 0, 5, xmlDocument);
		currentSymbolInfo = createSymbolInformation("a", SymbolKind.Field, currentLocation, "");
		expectedSymbolInfos.add(currentSymbolInfo);

		currentLocation = createLocation(testURI, 3, 5, xmlDocument);
		currentSymbolInfo = createSymbolInformation("?", SymbolKind.Field, currentLocation, "a");
		expectedSymbolInfos.add(currentSymbolInfo);

		assertSymbols(expectedSymbolInfos, actualSymbolInfos);
	}

	@Test
	public void insideEndTag() throws BadLocationException {
		// assertRename("<html|></meta></html>", "newText", edits("newText", r(0, 1, 5),
		// r(0, 15, 19)));
	}

	@Test
	public void externalDTD() {
		String xmlText = "<!ELEMENT br EMPTY>\n" + //
				"<!ATTLIST br\n" + //
				"	%all;>";
		String testURI = "test.dtd";
		initializeTestObjects(xmlText, testURI);

		List<SymbolInformation> expectedSymbolInfos = new ArrayList<SymbolInformation>();
		currentLocation = createLocation(testURI, 0, 19, xmlDocument);
		currentSymbolInfo = createSymbolInformation("br", SymbolKind.Property, currentLocation, "");
		expectedSymbolInfos.add(currentSymbolInfo);

		currentLocation = createLocation(testURI, 34, 39, xmlDocument);
		currentSymbolInfo = createSymbolInformation("%all;", SymbolKind.Key, currentLocation, "");
		expectedSymbolInfos.add(currentSymbolInfo);

		assertSymbols(expectedSymbolInfos, actualSymbolInfos);
	}

	@Test
	public void internalDTD() {
		String xmlText = "<!DOCTYPE br [\n" + //
				"  	<!ELEMENT br EMPTY>\n" + //
				"	<!ATTLIST br\n" + //
				"		%all;>\n" + //
				"]>\n" + //
				"<br />";
		String testURI = "test.xml";
		initializeTestObjects(xmlText, testURI);

		List<SymbolInformation> expectedSymbolInfos = new ArrayList<SymbolInformation>();
		currentLocation = createLocation(testURI, 0, 63, xmlDocument);
		currentSymbolInfo = createSymbolInformation("DOCTYPE:br", SymbolKind.Struct, currentLocation, "");
		expectedSymbolInfos.add(currentSymbolInfo);

		currentLocation = createLocation(testURI, 18, 37, xmlDocument);
		currentSymbolInfo = createSymbolInformation("br", SymbolKind.Property, currentLocation, "DOCTYPE:br");
		expectedSymbolInfos.add(currentSymbolInfo);

		currentLocation = createLocation(testURI, 54, 59, xmlDocument);
		currentSymbolInfo = createSymbolInformation("%all;", SymbolKind.Key, currentLocation, "DOCTYPE:br");
		expectedSymbolInfos.add(currentSymbolInfo);

		currentLocation = createLocation(testURI, 64, 70, xmlDocument);
		currentSymbolInfo = createSymbolInformation("br", SymbolKind.Field, currentLocation, "");
		expectedSymbolInfos.add(currentSymbolInfo);

		assertSymbols(expectedSymbolInfos, actualSymbolInfos);
	}

	@Test
	public void exceedSymbolLimit() {
		String xmlText = "<!DOCTYPE br [\n" + //
				"  	<!ELEMENT br EMPTY>\n" + //
				"	<!ATTLIST br\n" + //
				"		%all;>\n" + //
				"]>\n" + //
				"<br />";
		String testURI = "test.xml";
		XMLSymbolSettings settings = new XMLSymbolSettings();
		settings.setMaxItemsComputed(4);
		initializeTestObjects(xmlText, testURI, settings);

		List<SymbolInformation> expectedSymbolInfos = new ArrayList<SymbolInformation>();
		currentLocation = createLocation(testURI, 0, 63, xmlDocument);
		currentSymbolInfo = createSymbolInformation("DOCTYPE:br", SymbolKind.Struct, currentLocation, "");
		expectedSymbolInfos.add(currentSymbolInfo);

		currentLocation = createLocation(testURI, 18, 37, xmlDocument);
		currentSymbolInfo = createSymbolInformation("br", SymbolKind.Property, currentLocation, "DOCTYPE:br");
		expectedSymbolInfos.add(currentSymbolInfo);

		currentLocation = createLocation(testURI, 54, 59, xmlDocument);
		currentSymbolInfo = createSymbolInformation("%all;", SymbolKind.Key, currentLocation, "DOCTYPE:br");
		expectedSymbolInfos.add(currentSymbolInfo);

		currentLocation = createLocation(testURI, 64, 70, xmlDocument);
		currentSymbolInfo = createSymbolInformation("br", SymbolKind.Field, currentLocation, "");
		expectedSymbolInfos.add(currentSymbolInfo);

		assertSymbols(expectedSymbolInfos, actualSymbolInfos);

		settings.setMaxItemsComputed(10);
		initializeTestObjects(xmlText, testURI, settings);
		assertSymbols(expectedSymbolInfos, actualSymbolInfos);

		settings.setMaxItemsComputed(3);
		initializeTestObjects(xmlText, testURI, settings);
		assertSymbols(expectedSymbolInfos.stream().limit(3).collect(Collectors.toList()), actualSymbolInfos);
	}

	@Test
	public void zeroSymbolLimit() {
		String xmlText = "<root>\n" + //
				"  <content />\n" + //
				"</root>";
		String testURI = "test.xml";
		XMLSymbolSettings settings = new XMLSymbolSettings();
		settings.setMaxItemsComputed(0);
		initializeTestObjects(xmlText, testURI, settings);
		assertSymbols(Collections.emptyList(), actualSymbolInfos);
	}

	// -------------------Tools------------------------------

	private void initializeTestObjects(String xmlText, String uri) {
		initializeTestObjects(xmlText, uri, new XMLSymbolSettings());
	}

	private void initializeTestObjects(String xmlText, String uri, XMLSymbolSettings settings) {
		xmlDocument = DOMParser.getInstance().parse(xmlText, uri, null);
		actualSymbolInfos = languageService.findSymbolInformations(xmlDocument, settings);
	}

	private void assertSymbols(List<SymbolInformation> expectedSymbolList, List<SymbolInformation> actualSymbolList) {
		assertEquals(expectedSymbolList.size(), actualSymbolList.size());

		SymbolInformation currentExpectedSymbol;
		SymbolInformation currentActualSymbol;

		for (int i = 0; i < expectedSymbolList.size(); i++) {
			currentExpectedSymbol = expectedSymbolList.get(i);
			currentActualSymbol = actualSymbolList.get(i);
			assertEquals(currentExpectedSymbol.getName(), currentActualSymbol.getName(),"Symbol index " + i);
			assertEquals(currentExpectedSymbol.getKind(), currentActualSymbol.getKind(),"Symbol index " + i);
			assertEquals(currentExpectedSymbol.getContainerName(),
					currentActualSymbol.getContainerName(),"Symbol index " + i);
			assertEquals(currentExpectedSymbol.getLocation(), currentActualSymbol.getLocation(),"Symbol index " + i);
			assertEquals(currentExpectedSymbol.getDeprecated(),
					currentActualSymbol.getDeprecated(),"Symbol index " + i);
		}
	}

	private SymbolInformation createSymbolInformation(String name, SymbolKind kind, Location location,
			String containerName) {
		SymbolInformation temp = new SymbolInformation(name, kind, location, containerName);
		return temp;
	}

	private Range createRange(int startOffset, int endOffset, DOMDocument xmlDocument) {
		Position start = null;
		try {
			start = xmlDocument.positionAt(startOffset);
		} catch (BadLocationException e) {
			fail("Could not create position at startOffset");
		}
		Position end = null;
		try {
			start = xmlDocument.positionAt(startOffset);
			end = xmlDocument.positionAt(endOffset);
		} catch (BadLocationException e) {
			fail("Could not create position at endOffset");
		}
		return new Range(start, end);
	}

	private Location createLocation(String uri, int startOffset, int endOffset, DOMDocument xmlDocument) {
		Range range = createRange(startOffset, endOffset, xmlDocument);
		return new Location(uri, range);
	}
}