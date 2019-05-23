package org.eclipse.lsp4xml.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMParser;
import org.junit.Before;
import org.junit.Test;

/**
 * XML symbols test with {@link SymbolInformation}.
 */
public class XMLSymbolInformationsTest {
	private static final String testURI = "test:URI";
	private XMLLanguageService languageService;
	private DOMDocument xmlDocument;
	private List<SymbolInformation> actualSymbolInfos;
	private List<SymbolInformation> expectedSymbolInfos;
	private Location currentLocation;
	private SymbolInformation currentSymbolInfo;

	@Before
	public void initializeLanguageService() {
		languageService = new XMLLanguageService();
	}

	@Test
	public void testSingleSymbol() {
		String xmlText = "<project></project>";
		initializeTestObjects(xmlText);

		currentLocation = createLocation(testURI, 0, 19, xmlDocument);
		currentSymbolInfo = createSymbolInformation("project", SymbolKind.Field, currentLocation, "");
		expectedSymbolInfos.add(currentSymbolInfo);

		assertSymbols(expectedSymbolInfos, actualSymbolInfos);
	}

	@Test
	public void testNestedSymbol() {
		String xmlText = "<project><inside></inside></project>";
		initializeTestObjects(xmlText);

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
		initializeTestObjects(xmlText);

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
		initializeTestObjects(xmlText);

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
		initializeTestObjects(xmlText);

		currentLocation = createLocation(testURI, 0, 4, xmlDocument);
		currentSymbolInfo = createSymbolInformation("a", SymbolKind.Field, currentLocation, "");
		expectedSymbolInfos.add(currentSymbolInfo);

		assertSymbols(expectedSymbolInfos, actualSymbolInfos);
	}

	@Test
	public void testNestedSelfClosingTag() {
		String xmlText = "<a><b/></a>";
		initializeTestObjects(xmlText);

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
		initializeTestObjects(xmlText);

		currentLocation = createLocation(testURI, 0, 3, xmlDocument);
		currentSymbolInfo = createSymbolInformation("a", SymbolKind.Field, currentLocation, "");
		expectedSymbolInfos.add(currentSymbolInfo);

		assertSymbols(expectedSymbolInfos, actualSymbolInfos);
	}

	@Test
	public void testNestedUnclosedTag() {
		String xmlText = "<a><b></a>";
		initializeTestObjects(xmlText);

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
		initializeTestObjects(xmlText);

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
		initializeTestObjects(xmlText);

		currentLocation = createLocation(testURI, 0, 7, xmlDocument);
		currentSymbolInfo = createSymbolInformation("meta", SymbolKind.Field, currentLocation, "");
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
	// -------------------Tools------------------------------

	private void initializeTestObjects(String xmlText) {
		initializeTestObjects(xmlText, testURI);
	}

	private void initializeTestObjects(String xmlText, String uri) {
		xmlDocument = DOMParser.getInstance().parse(xmlText, uri, null);
		actualSymbolInfos = languageService.findSymbolInformations(xmlDocument);
		expectedSymbolInfos = new ArrayList<SymbolInformation>();
	}

	private void assertSymbols(List<SymbolInformation> expectedSymbolList, List<SymbolInformation> actualSymbolList) {
		assertEquals(expectedSymbolList.size(), actualSymbolList.size());

		SymbolInformation currentExpectedSymbol;
		SymbolInformation currentActualSymbol;

		for (int i = 0; i < expectedSymbolList.size(); i++) {
			currentExpectedSymbol = expectedSymbolList.get(i);
			currentActualSymbol = actualSymbolList.get(i);
			assertEquals("Symbol index " + i, currentExpectedSymbol.getName(), currentActualSymbol.getName());
			assertEquals("Symbol index " + i, currentExpectedSymbol.getKind(), currentActualSymbol.getKind());
			assertEquals("Symbol index " + i, currentExpectedSymbol.getContainerName(),
					currentActualSymbol.getContainerName());
			assertEquals("Symbol index " + i, currentExpectedSymbol.getLocation(), currentActualSymbol.getLocation());
			assertEquals("Symbol index " + i, currentExpectedSymbol.getDeprecated(),
					currentActualSymbol.getDeprecated());
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