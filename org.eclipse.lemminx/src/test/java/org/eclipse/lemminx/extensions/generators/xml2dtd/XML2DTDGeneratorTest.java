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
package org.eclipse.lemminx.extensions.generators.xml2dtd;

import static java.lang.System.lineSeparator;
import static org.eclipse.lemminx.XMLAssert.assertGrammarGenerator;

import java.io.IOException;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.junit.jupiter.api.Test;

/**
 * Tests for generating DTD from XML source.
 *
 */
public class XML2DTDGeneratorTest extends AbstractCacheBasedTest {

	@Test
	public void dtd() throws IOException {
		String xml = "<note>\r\n" + //
				"	<to>Tove</to>\r\n" + //
				"	<from>Jani</from>\r\n" + //
				"	<heading>Reminder</heading>\r\n" + //
				"	<body>Don't forget me this weekend!</body>\r\n" + //
				"</note>";
		String dtd = "<!ELEMENT note (to,from,heading,body)>" + lineSeparator() + //
				"<!ELEMENT to (#PCDATA)>" + lineSeparator() + //
				"<!ELEMENT from (#PCDATA)>" + lineSeparator() + //
				"<!ELEMENT heading (#PCDATA)>" + lineSeparator() + //
				"<!ELEMENT body (#PCDATA)>";
		assertGrammarGenerator(xml, new DTDGeneratorSettings(), dtd);
	}

	@Test
	public void dtdWithAttr() throws IOException {
		String xml = "<note version=\"1.2\" >";
		String dtd = "<!ELEMENT note EMPTY>" + lineSeparator() + //
				"<!ATTLIST note version NMTOKEN #REQUIRED>";
		assertGrammarGenerator(xml, new DTDGeneratorSettings(), dtd);
	}

	@Test
	public void dtdWithAttrs() throws IOException {
		String xml = "<note version=\"1.2\" >\r\n" + //
				"	<to attr1=\"abcd\" attr2=\"efgh\">Tove</to>\r\n" + //
				"	<from>Jani</from>\r\n" + //
				"	<heading>Reminder</heading>\r\n" + //
				"	<body>Don't forget me this weekend!</body>\r\n" + //
				"</note>";
		String dtd = "<!ELEMENT note (to,from,heading,body)>" + lineSeparator() + //
				"<!ATTLIST note version NMTOKEN #REQUIRED>" + lineSeparator() + //
				"<!ELEMENT to (#PCDATA)>" + lineSeparator() + //
				"<!ATTLIST to attr1 NMTOKEN #REQUIRED>" + lineSeparator() + //
				"<!ATTLIST to attr2 NMTOKEN #REQUIRED>" + lineSeparator() + //
				"<!ELEMENT from (#PCDATA)>" + lineSeparator() + //
				"<!ELEMENT heading (#PCDATA)>" + lineSeparator() + //
				"<!ELEMENT body (#PCDATA)>";
		assertGrammarGenerator(xml, new DTDGeneratorSettings(), dtd);
	}

	@Test
	public void empty() throws IOException {
		String xml = "<note />";
		String dtd = "<!ELEMENT note EMPTY>";
		assertGrammarGenerator(xml, new DTDGeneratorSettings(), dtd);
	}

	@Test
	public void onlyCharacterContent() throws IOException {
		String xml = "<note>ABCD</note>";
		String dtd = "<!ELEMENT note (#PCDATA)>";
		assertGrammarGenerator(xml, new DTDGeneratorSettings(), dtd);
	}

	@Test
	public void onlyElementChildren() throws IOException {
		String xml = "<note><from>ABCD</from></note>";
		String dtd = "<!ELEMENT note (from)>" + lineSeparator() + //
				"<!ELEMENT from (#PCDATA)>";
		assertGrammarGenerator(xml, new DTDGeneratorSettings(), dtd);
	}

	@Test
	public void characterContentAndElementChildren() throws IOException {
		String xml = "<note>ABCD<from>ABCD</from></note>";
		String dtd = "<!ELEMENT note (#PCDATA|from)*>" + lineSeparator() + //
				"<!ELEMENT from (#PCDATA)>";
		assertGrammarGenerator(xml, new DTDGeneratorSettings(), dtd);
	}

	@Test
	public void characterContentAndSeveralElementChildren() throws IOException {
		String xml = "<note><from>ABCD</from><to></to></note>";
		String dtd = "<!ELEMENT note (from,to)>" + lineSeparator() + //
				"<!ELEMENT from (#PCDATA)>" + lineSeparator() + //
				"<!ELEMENT to EMPTY>";
		assertGrammarGenerator(xml, new DTDGeneratorSettings(), dtd);
	}

	@Test
	public void threeLevel() {
		String xml = "<a>\r\n" + //
				"	<b>\r\n" + //
				"		<c/>\r\n" + //
				"	</b>\r\n" + //
				"</a>";
		String dtd = "<!ELEMENT a (b)>" + lineSeparator() + //
				"<!ELEMENT b (c)>" + lineSeparator() + //
				"<!ELEMENT c EMPTY>";
		assertGrammarGenerator(xml, new DTDGeneratorSettings(), dtd);
	}

	@Test
	public void severalSameElement() {
		String xml = "<a>\r\n" + //
				"	<b>\r\n" + //
				"		<c />\r\n" + //
				"		<c />\r\n" + //
				"	</b>\r\n" + //
				"	<b />\r\n" + //
				"	<b>\r\n" + //
				"		<c />\r\n" + //
				"	</b>\r\n" + //
				"</a>";
		String dtd = "<!ELEMENT a (b+)>" + lineSeparator() + //
				"<!ELEMENT b (c*)>" + lineSeparator() + //
				"<!ELEMENT c EMPTY>";
		assertGrammarGenerator(xml, new DTDGeneratorSettings(), dtd);
	}

	@Test
	public void cardinalityPlus() {
		String xml = "<a>\r\n" + //
				"	<b>\r\n" + //
				"		<c />\r\n" + //
				"		<d />\r\n" + //
				"	</b>\r\n" + //
				"	<b>\r\n" + //
				"		<c />\r\n" + //
				"		<d />\r\n" + //
				"	</b>\r\n" + //
				"</a>";
		String dtd = "<!ELEMENT a (b+)>" + lineSeparator() + //
				"<!ELEMENT b (c,d)>" + lineSeparator() + //
				"<!ELEMENT c EMPTY>" + lineSeparator() + //
				"<!ELEMENT d EMPTY>";
		assertGrammarGenerator(xml, new DTDGeneratorSettings(), dtd);
	}

	@Test
	public void complexLevel() {
		String xml = "<a>\r\n" + //
				"	<b>\r\n" + //
				"		<b>\r\n" + //
				"			<d>X</d>\r\n" + //
				"		</b>\r\n" + //
				"	</b>\r\n" + //
				"	<b>\r\n" + //
				"		<c />\r\n" + //
				"	</b>\r\n" + //
				"</a>";
		String dtd = "<!ELEMENT a (b+)>" + lineSeparator() + //
				"<!ELEMENT b (b?,d?,c?)>" + lineSeparator() + //
				"<!ELEMENT d (#PCDATA)>" + lineSeparator() + //
				"<!ELEMENT c EMPTY>";
		assertGrammarGenerator(xml, new DTDGeneratorSettings(), dtd);
	}

	@Test
	public void mixedContent() {
		String xml = "<root>\r\n" + //
				"    <a>\r\n" + //
				"        <b>hello</b>\r\n" + //
				"        text\r\n" + //
				"    </a>\r\n" + //
				"</root>";
		String dtd = "<!ELEMENT root (a)>" + lineSeparator() + //
				"<!ELEMENT a (#PCDATA|b)*>" + lineSeparator() + //
				"<!ELEMENT b (#PCDATA)>";
		assertGrammarGenerator(xml, new DTDGeneratorSettings(), dtd);
	}

	@Test
	public void mixedContentWith2SameElements() {
		String xml = "<root>\r\n" + //
				"	<a>\r\n" + //
				"		<b>hello</b>\r\n" + //
				"		text\r\n" + //
				"	</a>\r\n" + //
				"	<a>\r\n" + //
				"		text\r\n" + //
				"	</a>\r\n" + //
				"</root>";
		String dtd = "<!ELEMENT root (a+)>" + lineSeparator() + //
				"<!ELEMENT a (#PCDATA|b)*>" + lineSeparator() + //
				"<!ELEMENT b (#PCDATA)>";
		assertGrammarGenerator(xml, new DTDGeneratorSettings(), dtd);
	}

	@Test
	public void choice() {
		String xml = "<root>\r\n" + //
				"	<a>\r\n" + //
				"		<c />\r\n" + //
				"		<b />\r\n" + //
				"	</a>\r\n" + //
				"	<a>\r\n" + //
				"		<b />\r\n" + //
				"		<c />\r\n" + //
				"	</a>\r\n" + //
				"</root>";
		String dtd = "<!ELEMENT root (a+)>" + lineSeparator() + //
				"<!ELEMENT a (c|b)*>" + lineSeparator() + //
				"<!ELEMENT c EMPTY>" + lineSeparator() + //
				"<!ELEMENT b EMPTY>";
		assertGrammarGenerator(xml, new DTDGeneratorSettings(), dtd);
	}

	@Test
	public void oneZero() {
		String xml = "<root>\r\n" + //
				"    <a>\r\n" + //
				"        <b />\r\n" + //
				"    </a>\r\n" + //
				"    <a></a>\r\n" + //
				"</root>";
		String dtd = "<!ELEMENT root (a+)>" + lineSeparator() + //
				"<!ELEMENT a (b?)>" + lineSeparator() + //
				"<!ELEMENT b EMPTY>";
		assertGrammarGenerator(xml, new DTDGeneratorSettings(), dtd);
	}

	@Test
	public void oneZero2() {
		String xml = "<root>\r\n" + //
				"    <a>\r\n" + //
				"        <b />\r\n" + //
				"    </a>\r\n" + //
				"    <a>\r\n" + //
				"        <c />\r\n" + //
				"    </a>\r\n" + //
				"    <a>\r\n" + //
				"        <b />\r\n" + //
				"        <c />\r\n" + //
				"    </a>\r\n" + //
				"    <a></a>\r\n" + //
				"</root>";
		String dtd = "<!ELEMENT root (a+)>" + lineSeparator() + //
				"<!ELEMENT a (b?,c?)>" + lineSeparator() + //
				"<!ELEMENT b EMPTY>" + lineSeparator() + //
				"<!ELEMENT c EMPTY>";
		assertGrammarGenerator(xml, new DTDGeneratorSettings(), dtd);
	}

	@Test
	public void optionalAttribute() {
		String xml = "<root>\r\n" + //
				"	<item attr1=\"A\" attr2=\"B\"/>\r\n" + //
				"	<item attr1=\"A\" />\r\n" + //
				"</root>";
		String dtd = "<!ELEMENT root (item+)>" + lineSeparator() + //
				"<!ELEMENT item EMPTY>" + lineSeparator() + //
				"<!ATTLIST item attr1 NMTOKEN #REQUIRED>" + lineSeparator() + //
				"<!ATTLIST item attr2 NMTOKEN #IMPLIED>";
		assertGrammarGenerator(xml, new DTDGeneratorSettings(), dtd);
	}

	@Test
	public void attrIDsAndFixed() {
		String xml = "<root>\r\n" + //
				"	<item attr1=\"id1\" attr2=\"A\" />\r\n" + //
				"	<item attr1=\"id2\" attr2=\"A\" />\r\n" + //
				"	<item attr1=\"id3\" attr2=\"A\" />\r\n" + //
				"	<item attr1=\"id4\" attr2=\"A\" />\r\n" + //
				"	<item attr1=\"id5\" attr2=\"A\" />\r\n" + //
				"	<item attr1=\"id6\" attr2=\"A\" />\r\n" + //
				"	<item attr1=\"id7\" attr2=\"A\" />\r\n" + //
				"	<item attr1=\"id8\" attr2=\"A\" />\r\n" + //
				"	<item attr1=\"id9\" attr2=\"A\" />\r\n" + //
				"	<item attr1=\"id10\" attr2=\"A\" />\r\n" + //
				"</root>";
		String dtd = "<!ELEMENT root (item+)>" + lineSeparator() + //
				"<!ELEMENT item EMPTY>" + lineSeparator() + //
				"<!ATTLIST item attr1 ID #REQUIRED>" + lineSeparator() + //
				"<!ATTLIST item attr2 NMTOKEN #FIXED \"A\">";
		assertGrammarGenerator(xml, new DTDGeneratorSettings(), dtd);
	}

	@Test
	public void attrIDsAndEnums() {
		String xml = "<root>\r\n" + //
				"	<item attr1=\"id1\" attr2=\"A\" />\r\n" + //
				"	<item attr1=\"id2\" attr2=\"A\" />\r\n" + //
				"	<item attr1=\"id3\" attr2=\"A\" />\r\n" + //
				"	<item attr1=\"id4\" attr2=\"A\" />\r\n" + //
				"	<item attr1=\"id5\" attr2=\"A\" />\r\n" + //
				"	<item attr1=\"id6\" attr2=\"B\" />\r\n" + //
				"	<item attr1=\"id7\" attr2=\"B\" />\r\n" + //
				"	<item attr1=\"id8\" attr2=\"B\" />\r\n" + //
				"	<item attr1=\"id9\" attr2=\"B\" />\r\n" + //
				"	<item attr1=\"id10\" attr2=\"B\" />\r\n" + //
				"</root>";
		String dtd = "<!ELEMENT root (item+)>" + lineSeparator() + //
				"<!ELEMENT item EMPTY>" + lineSeparator() + //
				"<!ATTLIST item attr1 ID #REQUIRED>" + lineSeparator() + //
				"<!ATTLIST item attr2 (A|B) #REQUIRED>";
		assertGrammarGenerator(xml, new DTDGeneratorSettings(), dtd);
	}

	@Test
	public void invalidStartTag() throws IOException {
		String xml = "<a><</a>";
		String dtd = "<!ELEMENT a EMPTY>";
		assertGrammarGenerator(xml, new DTDGeneratorSettings(), dtd);

		xml = "<a>bcd   <   </a>";
		dtd = "<!ELEMENT a (#PCDATA)>";
		assertGrammarGenerator(xml, new DTDGeneratorSettings(), dtd);
	}

	@Test
	public void invalidEndTag() throws IOException {
		String xml = "<a></</a>";
		String dtd = "<!ELEMENT a EMPTY>";
		assertGrammarGenerator(xml, new DTDGeneratorSettings(), dtd);

		xml = "<a>bcd   </   </a>";
		dtd = "<!ELEMENT a (#PCDATA)>";
		assertGrammarGenerator(xml, new DTDGeneratorSettings(), dtd);
	}
}