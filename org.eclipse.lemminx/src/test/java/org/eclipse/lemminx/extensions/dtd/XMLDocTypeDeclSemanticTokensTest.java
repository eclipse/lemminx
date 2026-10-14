/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.dtd;

import static org.eclipse.lemminx.XMLAssert.st;
import static org.eclipse.lemminx.XMLAssert.testSemanticTokensFor;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.settings.capabilities.ServerCapabilitiesConstants;
import org.eclipse.lsp4j.SemanticTokens;
import org.junit.jupiter.api.Test;

/**
 * Tests for semantic tokens in XML documents with inline DOCTYPE declarations.
 */
public class XMLDocTypeDeclSemanticTokensTest extends AbstractCacheBasedTest {

	private static final int TYPE = ServerCapabilitiesConstants.TOKEN_TYPE_TYPE;
	private static final int VARIABLE = ServerCapabilitiesConstants.TOKEN_TYPE_VARIABLE;
	private static final int PROPERTY = ServerCapabilitiesConstants.TOKEN_TYPE_PROPERTY;
	private static final int DECL = ServerCapabilitiesConstants.TOKEN_MODIFIER_DECLARATION;
	private static final int READONLY = ServerCapabilitiesConstants.TOKEN_MODIFIER_READONLY;

	// ------------------- DTD declaration tokens -------------------

	@Test
	public void entityNameInInlineDTD() {
		String xml = "<!DOCTYPE root [\n" + //
				"  <!ENTITY myEntity \"some value\">\n" + //
				"]>\n" + //
				"<root />";
		testSemanticTokensFor(xml, //
				st(1, 11, 8, VARIABLE, DECL) // 'myEntity'
		);
	}

	@Test
	public void multipleEntities() {
		String xml = "<!DOCTYPE root [\n" + //
				"  <!ENTITY first \"value1\">\n" + //
				"  <!ENTITY second \"value2\">\n" + //
				"  <!ENTITY third \"value3\">\n" + //
				"]>\n" + //
				"<root />";
		testSemanticTokensFor(xml, //
				st(1, 11, 5, VARIABLE, DECL), // 'first'
				st(2, 11, 6, VARIABLE, DECL), // 'second'
				st(3, 11, 5, VARIABLE, DECL) // 'third'
		);
	}

	@Test
	public void parameterEntity() {
		String xml = "<!DOCTYPE root [\n" + //
				"  <!ENTITY % common \"#PCDATA\">\n" + //
				"]>\n" + //
				"<root />";
		testSemanticTokensFor(xml, //
				st(1, 13, 6, VARIABLE, DECL) // 'common'
		);
	}

	@Test
	public void systemEntity() {
		String xml = "<!DOCTYPE root [\n" + //
				"  <!ENTITY external SYSTEM \"external.xml\">\n" + //
				"]>\n" + //
				"<root />";
		testSemanticTokensFor(xml, //
				st(1, 11, 8, VARIABLE, DECL) // 'external'
		);
	}

	@Test
	public void publicEntity() {
		String xml = "<!DOCTYPE root [\n" + //
				"  <!ENTITY pub PUBLIC \"-//OASIS//DTD\" \"oasis.dtd\">\n" + //
				"]>\n" + //
				"<root />";
		testSemanticTokensFor(xml, //
				st(1, 11, 3, VARIABLE, DECL) // 'pub'
		);
	}

	@Test
	public void elementDeclaration() {
		String xml = "<!DOCTYPE root [\n" + //
				"  <!ELEMENT root (#PCDATA)>\n" + //
				"]>\n" + //
				"<root />";
		testSemanticTokensFor(xml, //
				st(1, 12, 4, TYPE, DECL), // 'root'
				st(3, 1, 4, TYPE, 0) // 'root' tag name
		);
	}

	@Test
	public void multipleElementDeclarations() {
		String xml = "<!DOCTYPE root [\n" + //
				"  <!ELEMENT root (child)>\n" + //
				"  <!ELEMENT child (#PCDATA)>\n" + //
				"]>\n" + //
				"<root><child /></root>";
		testSemanticTokensFor(xml, //
				st(1, 12, 4, TYPE, DECL), // 'root'
				st(2, 12, 5, TYPE, DECL), // 'child'
				st(4, 1, 4, TYPE, 0), // <root> tag name
				st(4, 7, 5, TYPE, 0), // <child /> tag name
				st(4, 17, 4, TYPE, 0) // </root> tag name
		);
	}

	@Test
	public void attlistDeclaration() {
		String xml = "<!DOCTYPE root [\n" + //
				"  <!ELEMENT root (#PCDATA)>\n" + //
				"  <!ATTLIST root id ID #REQUIRED>\n" + //
				"]>\n" + //
				"<root id=\"r1\" />";
		testSemanticTokensFor(xml, //
				st(1, 12, 4, TYPE, DECL), // ELEMENT 'root'
				st(2, 12, 4, TYPE, 0), // ATTLIST 'root' (element name reference)
				st(2, 17, 2, PROPERTY, DECL), // 'id' (attribute name declaration)
				st(4, 1, 4, TYPE, 0), // 'root' tag name
				st(4, 6, 2, PROPERTY, READONLY) // 'id' (required attribute in XML content)
		);
	}

	@Test
	public void attlistMultipleAttributes() {
		String xml = "<!DOCTYPE root [\n" + //
				"  <!ELEMENT root (#PCDATA)>\n" + //
				"  <!ATTLIST root\n" + //
				"            id ID #REQUIRED\n" + //
				"            name CDATA #IMPLIED\n" + //
				"            type CDATA #IMPLIED>\n" + //
				"]>\n" + //
				"<root id=\"r1\" />";
		testSemanticTokensFor(xml, //
				st(1, 12, 4, TYPE, DECL), // ELEMENT 'root'
				st(2, 12, 4, TYPE, 0), // ATTLIST 'root' (element name ref)
				st(3, 12, 2, PROPERTY, DECL), // 'id' (attr decl)
				st(4, 12, 4, PROPERTY, DECL), // 'name' (attr decl)
				st(5, 12, 4, PROPERTY, DECL), // 'type' (attr decl)
				st(7, 1, 4, TYPE, 0), // 'root' tag name
				st(7, 6, 2, PROPERTY, READONLY) // required 'id' in XML content
		);
	}

	@Test
	public void attlistWithoutElementDeclaration() {
		String xml = "<!DOCTYPE root [\n" + //
				"  <!ATTLIST root id ID #REQUIRED>\n" + //
				"]>\n" + //
				"<root id=\"r1\" />";
		testSemanticTokensFor(xml, //
				st(1, 12, 4, TYPE, 0), // 'root' (type reference, no DECL)
				st(1, 17, 2, PROPERTY, DECL) // 'id' (property declaration)
				// 'root' tag NOT highlighted (no <!ELEMENT root>)
				// 'id' attr NOT highlighted (no element declaration found)
		);
	}

	@Test
	public void notationDeclaration() {
		String xml = "<!DOCTYPE root [\n" + //
				"  <!NOTATION gif SYSTEM \"image/gif\">\n" + //
				"]>\n" + //
				"<root />";
		testSemanticTokensFor(xml, //
				st(1, 13, 3, TYPE, DECL) // 'gif'
		);
	}

	@Test
	public void notationPublic() {
		String xml = "<!DOCTYPE root [\n" + //
				"  <!NOTATION html PUBLIC \"-//W3C//DTD HTML 4.0//EN\">\n" + //
				"]>\n" + //
				"<root />";
		testSemanticTokensFor(xml, //
				st(1, 13, 4, TYPE, DECL) // 'html'
		);
	}

	@Test
	public void allDeclarationTypes() {
		String xml = "<!DOCTYPE root [\n" + //
				"  <!ELEMENT root (#PCDATA)>\n" + //
				"  <!ATTLIST root id ID #REQUIRED>\n" + //
				"  <!ENTITY greeting \"Hello\">\n" + //
				"  <!NOTATION gif SYSTEM \"image/gif\">\n" + //
				"]>\n" + //
				"<root id=\"r1\" />";
		testSemanticTokensFor(xml, //
				st(1, 12, 4, TYPE, DECL), // ELEMENT 'root'
				st(2, 12, 4, TYPE, 0), // ATTLIST 'root' (element ref)
				st(2, 17, 2, PROPERTY, DECL), // ATTLIST 'id' (attr decl)
				st(3, 11, 8, VARIABLE, DECL), // ENTITY 'greeting'
				st(4, 13, 3, TYPE, DECL), // NOTATION 'gif'
				st(6, 1, 4, TYPE, 0), // 'root' tag name
				st(6, 6, 2, PROPERTY, READONLY) // required 'id' in XML content
		);
	}

	@Test
	public void entityAndRequiredAttributeTogether() {
		String xml = "<!DOCTYPE root [\n" + //
				"  <!ENTITY greeting \"Hello\">\n" + //
				"  <!ELEMENT root (#PCDATA)>\n" + //
				"  <!ATTLIST root name CDATA #REQUIRED>\n" + //
				"]>\n" + //
				"<root name=\"World\">&greeting;</root>";
		testSemanticTokensFor(xml, //
				st(1, 11, 8, VARIABLE, DECL), // ENTITY 'greeting'
				st(2, 12, 4, TYPE, DECL), // ELEMENT 'root'
				st(3, 12, 4, TYPE, 0), // ATTLIST 'root' (element ref)
				st(3, 17, 4, PROPERTY, DECL), // ATTLIST 'name' (attr decl)
				st(5, 1, 4, TYPE, 0), // <root> tag name
				st(5, 6, 4, PROPERTY, READONLY), // required 'name' in XML content
				st(5, 31, 4, TYPE, 0) // </root> tag name
		);
	}

	// ------------------- Required attributes -------------------

	@Test
	public void requiredAttribute() {
		String xml = "<!DOCTYPE note [\n" + //
				"  <!ELEMENT note (body)>\n" + //
				"  <!ATTLIST note id ID #REQUIRED>\n" + //
				"  <!ELEMENT body (#PCDATA)>\n" + //
				"]>\n" + //
				"<note id=\"n1\">\n" + //
				"  <body>Hello</body>\n" + //
				"</note>";
		testSemanticTokensFor(xml, //
				st(1, 12, 4, TYPE, DECL), // ELEMENT 'note'
				st(2, 12, 4, TYPE, 0), // ATTLIST 'note' (element ref)
				st(2, 17, 2, PROPERTY, DECL), // ATTLIST 'id' (attr decl)
				st(3, 12, 4, TYPE, DECL), // ELEMENT 'body'
				st(5, 1, 4, TYPE, 0), // 'note' open tag
				st(5, 6, 2, PROPERTY, READONLY), // required 'id' on <note>
				st(6, 3, 4, TYPE, 0), // 'body' open tag
				st(6, 15, 4, TYPE, 0), // 'body' close tag
				st(7, 2, 4, TYPE, 0) // 'note' close tag
		);
	}

	@Test
	public void mixOfRequiredAndImplied() {
		String xml = "<!DOCTYPE person [\n" + //
				"  <!ELEMENT person EMPTY>\n" + //
				"  <!ATTLIST person\n" + //
				"    name CDATA #REQUIRED\n" + //
				"    age CDATA #REQUIRED\n" + //
				"    email CDATA #IMPLIED\n" + //
				"  >\n" + //
				"]>\n" + //
				"<person name=\"John\" age=\"30\" email=\"john@example.com\" />";
		testSemanticTokensFor(xml, //
				st(1, 12, 6, TYPE, DECL), // ELEMENT 'person'
				st(2, 12, 6, TYPE, 0), // ATTLIST 'person' (element ref)
				st(3, 4, 4, PROPERTY, DECL), // ATTLIST 'name' (attr decl)
				st(4, 4, 3, PROPERTY, DECL), // ATTLIST 'age' (attr decl)
				st(5, 4, 5, PROPERTY, DECL), // ATTLIST 'email' (attr decl)
				st(8, 1, 6, TYPE, 0), // 'person' open tag
				st(8, 8, 4, PROPERTY, READONLY), // required 'name' on <person>
				st(8, 20, 3, PROPERTY, READONLY) // required 'age' on <person>
		);
	}

	@Test
	public void noRequiredAttributes() {
		String xml = "<!DOCTYPE item [\n" + //
				"  <!ELEMENT item EMPTY>\n" + //
				"  <!ATTLIST item\n" + //
				"    name CDATA #IMPLIED\n" + //
				"    value CDATA #IMPLIED\n" + //
				"  >\n" + //
				"]>\n" + //
				"<item name=\"test\" value=\"123\" />";
		testSemanticTokensFor(xml, //
				st(1, 12, 4, TYPE, DECL), // ELEMENT 'item'
				st(2, 12, 4, TYPE, 0), // ATTLIST 'item' (element ref)
				st(3, 4, 4, PROPERTY, DECL), // ATTLIST 'name' (attr decl)
				st(4, 4, 5, PROPERTY, DECL), // ATTLIST 'value' (attr decl)
				st(7, 1, 4, TYPE, 0) // 'item' open tag
		);
	}

	@Test
	public void fixedAttributeNotHighlighted() {
		String xml = "<!DOCTYPE config [\n" + //
				"  <!ELEMENT config EMPTY>\n" + //
				"  <!ATTLIST config\n" + //
				"    version CDATA #FIXED \"1.0\"\n" + //
				"    name CDATA #REQUIRED\n" + //
				"  >\n" + //
				"]>\n" + //
				"<config version=\"1.0\" name=\"test\" />";
		testSemanticTokensFor(xml, //
				st(1, 12, 6, TYPE, DECL), // ELEMENT 'config'
				st(2, 12, 6, TYPE, 0), // ATTLIST 'config' (element ref)
				st(3, 4, 7, PROPERTY, DECL), // ATTLIST 'version' (attr decl)
				st(4, 4, 4, PROPERTY, DECL), // ATTLIST 'name' (attr decl)
				st(7, 1, 6, TYPE, 0), // 'config' open tag
				st(7, 22, 4, PROPERTY, READONLY) // required 'name' on <config>
		);
	}

	@Test
	public void nestedElementsWithRequiredAttributes() {
		String xml = "<!DOCTYPE root [\n" + //
				"  <!ELEMENT root (child)>\n" + //
				"  <!ELEMENT child (grandchild)>\n" + //
				"  <!ATTLIST child id ID #REQUIRED>\n" + //
				"  <!ELEMENT grandchild EMPTY>\n" + //
				"  <!ATTLIST grandchild ref IDREF #REQUIRED>\n" + //
				"]>\n" + //
				"<root>\n" + //
				"  <child id=\"c1\">\n" + //
				"    <grandchild ref=\"c1\" />\n" + //
				"  </child>\n" + //
				"</root>";
		testSemanticTokensFor(xml, //
				st(1, 12, 4, TYPE, DECL), // ELEMENT 'root'
				st(2, 12, 5, TYPE, DECL), // ELEMENT 'child'
				st(3, 12, 5, TYPE, 0), // ATTLIST 'child' (element ref)
				st(3, 18, 2, PROPERTY, DECL), // ATTLIST 'id' (attr decl)
				st(4, 12, 10, TYPE, DECL), // ELEMENT 'grandchild'
				st(5, 12, 10, TYPE, 0), // ATTLIST 'grandchild' (element ref)
				st(5, 23, 3, PROPERTY, DECL), // ATTLIST 'ref' (attr decl)
				st(7, 1, 4, TYPE, 0), // 'root' open tag
				st(8, 3, 5, TYPE, 0), // 'child' open tag
				st(8, 9, 2, PROPERTY, READONLY), // required 'id' on <child>
				st(9, 5, 10, TYPE, 0), // 'grandchild' open tag
				st(9, 16, 3, PROPERTY, READONLY), // required 'ref' on <grandchild>
				st(10, 4, 5, TYPE, 0), // 'child' close tag
				st(11, 2, 4, TYPE, 0) // 'root' close tag
		);
	}

	@Test
	public void multipleElementsWithRequiredAttributes() {
		String xml = "<!DOCTYPE library [\n" + //
				"  <!ELEMENT library (book+)>\n" + //
				"  <!ELEMENT book (author)>\n" + //
				"  <!ATTLIST book title CDATA #REQUIRED>\n" + //
				"  <!ELEMENT author (#PCDATA)>\n" + //
				"  <!ATTLIST author name CDATA #REQUIRED>\n" + //
				"]>\n" + //
				"<library>\n" + //
				"  <book title=\"XML Guide\">\n" + //
				"    <author name=\"John\" />\n" + //
				"  </book>\n" + //
				"  <book title=\"Java Basics\">\n" + //
				"    <author name=\"Jane\" />\n" + //
				"  </book>\n" + //
				"</library>";
		testSemanticTokensFor(xml, //
				st(1, 12, 7, TYPE, DECL), // ELEMENT 'library'
				st(2, 12, 4, TYPE, DECL), // ELEMENT 'book'
				st(3, 12, 4, TYPE, 0), // ATTLIST 'book' (element ref)
				st(3, 17, 5, PROPERTY, DECL), // ATTLIST 'title' (attr decl)
				st(4, 12, 6, TYPE, DECL), // ELEMENT 'author'
				st(5, 12, 6, TYPE, 0), // ATTLIST 'author' (element ref)
				st(5, 19, 4, PROPERTY, DECL), // ATTLIST 'name' (attr decl)
				st(7, 1, 7, TYPE, 0), // 'library' open tag
				st(8, 3, 4, TYPE, 0), // first 'book' open tag
				st(8, 8, 5, PROPERTY, READONLY), // required 'title' on <book>
				st(9, 5, 6, TYPE, 0), // first 'author' open tag
				st(9, 12, 4, PROPERTY, READONLY), // required 'name' on <author>
				st(10, 4, 4, TYPE, 0), // first 'book' close tag
				st(11, 3, 4, TYPE, 0), // second 'book' open tag
				st(11, 8, 5, PROPERTY, READONLY), // required 'title' on second <book>
				st(12, 5, 6, TYPE, 0), // second 'author' open tag
				st(12, 12, 4, PROPERTY, READONLY), // required 'name' on second <author>
				st(13, 4, 4, TYPE, 0), // second 'book' close tag
				st(14, 2, 7, TYPE, 0) // 'library' close tag
		);
	}

	// ------------------- Element optionality -------------------

	@Test
	public void optionalElementNotHighlighted() {
		String xml = "<!DOCTYPE book [\n" + //
				"  <!ELEMENT book (author, summary?)>\n" + //
				"  <!ELEMENT author (#PCDATA)>\n" + //
				"  <!ELEMENT summary (#PCDATA)>\n" + //
				"]>\n" + //
				"<book>\n" + //
				"  <author>John</author>\n" + //
				"  <summary>A guide</summary>\n" + //
				"</book>";
		testSemanticTokensFor(xml, //
				st(1, 12, 4, TYPE, DECL), // ELEMENT 'book'
				st(2, 12, 6, TYPE, DECL), // ELEMENT 'author'
				st(3, 12, 7, TYPE, DECL), // ELEMENT 'summary'
				st(5, 1, 4, TYPE, 0), // 'book' open tag
				st(6, 3, 6, TYPE, 0), // 'author' open tag (required)
				st(6, 16, 6, TYPE, 0), // 'author' close tag
				// 'summary' NOT highlighted (optional: ?)
				st(8, 2, 4, TYPE, 0) // 'book' close tag
		);
	}

	@Test
	public void zeroOrMoreNotHighlighted() {
		String xml = "<!DOCTYPE list [\n" + //
				"  <!ELEMENT list (item*)>\n" + //
				"  <!ELEMENT item (#PCDATA)>\n" + //
				"]>\n" + //
				"<list>\n" + //
				"  <item>one</item>\n" + //
				"  <item>two</item>\n" + //
				"</list>";
		testSemanticTokensFor(xml, //
				st(1, 12, 4, TYPE, DECL), // ELEMENT 'list'
				st(2, 12, 4, TYPE, DECL), // ELEMENT 'item'
				st(4, 1, 4, TYPE, 0), // 'list' open tag
				// 'item' NOT highlighted (optional: *)
				st(7, 2, 4, TYPE, 0) // 'list' close tag
		);
	}

	@Test
	public void oneOrMoreHighlighted() {
		String xml = "<!DOCTYPE list [\n" + //
				"  <!ELEMENT list (item+)>\n" + //
				"  <!ELEMENT item (#PCDATA)>\n" + //
				"]>\n" + //
				"<list>\n" + //
				"  <item>one</item>\n" + //
				"</list>";
		testSemanticTokensFor(xml, //
				st(1, 12, 4, TYPE, DECL), // ELEMENT 'list'
				st(2, 12, 4, TYPE, DECL), // ELEMENT 'item'
				st(4, 1, 4, TYPE, 0), // 'list' open tag
				st(5, 3, 4, TYPE, 0), // 'item' open tag (required: +)
				st(5, 13, 4, TYPE, 0), // 'item' close tag
				st(6, 2, 4, TYPE, 0) // 'list' close tag
		);
	}

	@Test
	public void choiceElementsNotHighlighted() {
		String xml = "<!DOCTYPE root [\n" + //
				"  <!ELEMENT root (a | b)>\n" + //
				"  <!ELEMENT a (#PCDATA)>\n" + //
				"  <!ELEMENT b (#PCDATA)>\n" + //
				"]>\n" + //
				"<root>\n" + //
				"  <a>value</a>\n" + //
				"</root>";
		testSemanticTokensFor(xml, //
				st(1, 12, 4, TYPE, DECL), // ELEMENT 'root'
				st(2, 12, 1, TYPE, DECL), // ELEMENT 'a'
				st(3, 12, 1, TYPE, DECL), // ELEMENT 'b'
				st(5, 1, 4, TYPE, 0), // 'root' open tag
				// 'a' NOT highlighted (inside choice)
				st(7, 2, 4, TYPE, 0) // 'root' close tag
		);
	}

	@Test
	public void mixedRequiredOptionalAndChoice() {
		String xml = "<!DOCTYPE doc [\n" + //
				"  <!ELEMENT doc (title, subtitle?, (appendixA | appendixB))>\n" + //
				"  <!ELEMENT title (#PCDATA)>\n" + //
				"  <!ELEMENT subtitle (#PCDATA)>\n" + //
				"  <!ELEMENT appendixA (#PCDATA)>\n" + //
				"  <!ELEMENT appendixB (#PCDATA)>\n" + //
				"]>\n" + //
				"<doc>\n" + //
				"  <title>My Doc</title>\n" + //
				"  <subtitle>Optional</subtitle>\n" + //
				"  <appendixA>A</appendixA>\n" + //
				"</doc>";
		testSemanticTokensFor(xml, //
				st(1, 12, 3, TYPE, DECL), // ELEMENT 'doc'
				st(2, 12, 5, TYPE, DECL), // ELEMENT 'title'
				st(3, 12, 8, TYPE, DECL), // ELEMENT 'subtitle'
				st(4, 12, 9, TYPE, DECL), // ELEMENT 'appendixA'
				st(5, 12, 9, TYPE, DECL), // ELEMENT 'appendixB'
				st(7, 1, 3, TYPE, 0), // 'doc' open tag
				st(8, 3, 5, TYPE, 0), // 'title' open tag (required)
				st(8, 17, 5, TYPE, 0), // 'title' close tag
				// 'subtitle' NOT highlighted (optional: ?)
				// 'appendixA' NOT highlighted (inside choice)
				st(11, 2, 3, TYPE, 0) // 'doc' close tag
		);
	}

	@Test
	public void optionalElementWithRequiredAttribute() {
		String xml = "<!DOCTYPE root [\n" + //
				"  <!ELEMENT root (child?)>\n" + //
				"  <!ELEMENT child EMPTY>\n" + //
				"  <!ATTLIST child id ID #REQUIRED>\n" + //
				"]>\n" + //
				"<root>\n" + //
				"  <child id=\"c1\" />\n" + //
				"</root>";
		testSemanticTokensFor(xml, //
				st(1, 12, 4, TYPE, DECL), // ELEMENT 'root'
				st(2, 12, 5, TYPE, DECL), // ELEMENT 'child'
				st(3, 12, 5, TYPE, 0), // ATTLIST 'child' (element ref)
				st(3, 18, 2, PROPERTY, DECL), // ATTLIST 'id' (attr decl)
				st(5, 1, 4, TYPE, 0), // 'root' open tag
				// 'child' tag NOT highlighted (optional: ?)
				st(6, 9, 2, PROPERTY, READONLY), // required 'id' on <child> (still highlighted)
				st(7, 2, 4, TYPE, 0) // 'root' close tag
		);
	}

	// ------------------- Comprehensive -------------------

	@Test
	public void allDeclarationsWithOptionalElements() {
		String xml = "<!DOCTYPE library [\n" + //
				"  <!ELEMENT library (book+)>\n" + //
				"  <!ELEMENT book (author, summary?)>\n" + //
				"  <!ATTLIST book\n" + //
				"    title CDATA #REQUIRED\n" + //
				"    isbn  CDATA #REQUIRED\n" + //
				"    year  CDATA #IMPLIED\n" + //
				"  >\n" + //
				"  <!ELEMENT author (#PCDATA)>\n" + //
				"  <!ATTLIST author name CDATA #REQUIRED>\n" + //
				"  <!ELEMENT summary (#PCDATA)>\n" + //
				"  <!ENTITY copyright \"2026 Red Hat Inc.\">\n" + //
				"  <!ENTITY license SYSTEM \"license.txt\">\n" + //
				"  <!NOTATION gif SYSTEM \"image/gif\">\n" + //
				"  <!NOTATION png PUBLIC \"-//W3C//DTD PNG//EN\">\n" + //
				"]>\n" + //
				"<library>\n" + //
				"  <book title=\"XML Guide\" isbn=\"978-0-123\" year=\"2026\">\n" + //
				"    <author name=\"John\">John Doe</author>\n" + //
				"    <summary>A comprehensive guide to XML.</summary>\n" + //
				"  </book>\n" + //
				"  <book title=\"Java Basics\" isbn=\"978-0-456\">\n" + //
				"    <author name=\"Jane\">Jane Smith</author>\n" + //
				"  </book>\n" + //
				"</library>";
		testSemanticTokensFor(xml, //
				// DTD declarations
				st(1, 12, 7, TYPE, DECL), // ELEMENT 'library'
				st(2, 12, 4, TYPE, DECL), // ELEMENT 'book'
				st(3, 12, 4, TYPE, 0), // ATTLIST 'book' (element ref)
				st(4, 4, 5, PROPERTY, DECL), // ATTLIST 'title' (attr decl)
				st(5, 4, 4, PROPERTY, DECL), // ATTLIST 'isbn' (attr decl)
				st(6, 4, 4, PROPERTY, DECL), // ATTLIST 'year' (attr decl)
				st(8, 12, 6, TYPE, DECL), // ELEMENT 'author'
				st(9, 12, 6, TYPE, 0), // ATTLIST 'author' (element ref)
				st(9, 19, 4, PROPERTY, DECL), // ATTLIST 'name' (attr decl)
				st(10, 12, 7, TYPE, DECL), // ELEMENT 'summary'
				st(11, 11, 9, VARIABLE, DECL), // ENTITY 'copyright'
				st(12, 11, 7, VARIABLE, DECL), // ENTITY 'license'
				st(13, 13, 3, TYPE, DECL), // NOTATION 'gif'
				st(14, 13, 3, TYPE, DECL), // NOTATION 'png'
				// XML content
				st(16, 1, 7, TYPE, 0), // 'library' open tag
				st(17, 3, 4, TYPE, 0), // first 'book' open tag (required: +)
				st(17, 8, 5, PROPERTY, READONLY), // required 'title'
				st(17, 26, 4, PROPERTY, READONLY), // required 'isbn'
				// 'year' NOT highlighted (implied)
				st(18, 5, 6, TYPE, 0), // 'author' open tag (required)
				st(18, 12, 4, PROPERTY, READONLY), // required 'name'
				st(18, 34, 6, TYPE, 0), // 'author' close tag
				// 'summary' NOT highlighted (optional: ?)
				st(20, 4, 4, TYPE, 0), // first 'book' close tag
				st(21, 3, 4, TYPE, 0), // second 'book' open tag
				st(21, 8, 5, PROPERTY, READONLY), // required 'title'
				st(21, 28, 4, PROPERTY, READONLY), // required 'isbn'
				st(22, 5, 6, TYPE, 0), // 'author' open tag
				st(22, 12, 4, PROPERTY, READONLY), // required 'name'
				st(22, 36, 6, TYPE, 0), // 'author' close tag
				st(23, 4, 4, TYPE, 0), // second 'book' close tag
				st(24, 2, 7, TYPE, 0) // 'library' close tag
		);
	}

	// ------------------- Edge cases -------------------

	@Test
	public void entitiesOnlyNoElementTokens() {
		String xml = "<!DOCTYPE root [\n" + //
				"  <!ENTITY pub PUBLIC \"-//OASIS//DTD\" \"oasis.dtd\">\n" + //
				"]>\n" + //
				"<root />";
		testSemanticTokensFor(xml, //
				st(1, 11, 3, VARIABLE, DECL) // ENTITY 'pub'
				// 'root' NOT highlighted (no <!ELEMENT root>)
		);
	}

	@Test
	public void attlistAndNotationOnlyNoElementTokens() {
		String xml = "<!DOCTYPE root [\n" + //
				"  <!ATTLIST root id ID #REQUIRED>\n" + //
				"  <!NOTATION gif SYSTEM \"image/gif\">\n" + //
				"]>\n" + //
				"<root id=\"r1\" />";
		testSemanticTokensFor(xml, //
				st(1, 12, 4, TYPE, 0), // ATTLIST 'root' (type ref)
				st(1, 17, 2, PROPERTY, DECL), // ATTLIST 'id' (attr decl)
				st(2, 13, 3, TYPE, DECL) // NOTATION 'gif'
				// 'root' tag NOT highlighted (no <!ELEMENT root>)
				// 'id' attr NOT highlighted (no element declaration found)
		);
	}

	@Test
	public void emptyDoctype() {
		String xml = "<!DOCTYPE root []>\n<root />";
		testSemanticTokensFor(xml //
		);
	}

	@Test
	public void noGrammarNoTokens() {
		String xml = "<root />";
		testSemanticTokensFor(xml //
		);
	}

	@Test
	public void malformedXMLStillWorks() {
		String xml = "<!DOCTYPE item [\n" + //
				"  <!ELEMENT item EMPTY>\n" + //
				"  <!ATTLIST item name CDATA #REQUIRED>\n" + //
				"]>\n" + //
				"<item name=\"test\">";
		testSemanticTokensFor(xml, //
				st(1, 12, 4, TYPE, DECL), // ELEMENT 'item'
				st(2, 12, 4, TYPE, 0), // ATTLIST 'item' (element ref)
				st(2, 17, 4, PROPERTY, DECL), // ATTLIST 'name' (attr decl)
				st(4, 1, 4, TYPE, 0), // 'item' open tag
				st(4, 6, 4, PROPERTY, READONLY) // required 'name' on <item>
		);
	}

	@Test
	public void singleAttributeOnSingleLine() {
		String xml = "<!DOCTYPE item [\n" + //
				"  <!ELEMENT item EMPTY>\n" + //
				"  <!ATTLIST item id ID #REQUIRED>\n" + //
				"]>\n" + //
				"<item id=\"x\" />";
		testSemanticTokensFor(xml, //
				st(1, 12, 4, TYPE, DECL), // ELEMENT 'item'
				st(2, 12, 4, TYPE, 0), // ATTLIST 'item' (element ref)
				st(2, 17, 2, PROPERTY, DECL), // ATTLIST 'id' (attr decl)
				st(4, 1, 4, TYPE, 0), // 'item' open tag
				st(4, 6, 2, PROPERTY, READONLY) // required 'id' on <item>
		);
	}

	@Test
	public void defaultAttributeNotHighlighted() {
		String xml = "<!DOCTYPE item [\n" + //
				"  <!ELEMENT item EMPTY>\n" + //
				"  <!ATTLIST item\n" + //
				"    type CDATA \"default\"\n" + //
				"    name CDATA #REQUIRED\n" + //
				"  >\n" + //
				"]>\n" + //
				"<item type=\"custom\" name=\"test\" />";
		testSemanticTokensFor(xml, //
				st(1, 12, 4, TYPE, DECL), // ELEMENT 'item'
				st(2, 12, 4, TYPE, 0), // ATTLIST 'item' (element ref)
				st(3, 4, 4, PROPERTY, DECL), // ATTLIST 'type' (attr decl)
				st(4, 4, 4, PROPERTY, DECL), // ATTLIST 'name' (attr decl)
				st(7, 1, 4, TYPE, 0), // 'item' open tag
				st(7, 20, 4, PROPERTY, READONLY) // required 'name' on <item>
		);
	}

	@Test
	public void resultHasNoResultId() {
		String xml = "<!DOCTYPE item [\n" + //
				"  <!ELEMENT item EMPTY>\n" + //
				"  <!ATTLIST item id ID #REQUIRED>\n" + //
				"]>\n" + //
				"<item id=\"x\" />";

		SemanticTokens tokens = testSemanticTokensFor(xml, //
				st(1, 12, 4, TYPE, DECL), //
				st(2, 12, 4, TYPE, 0), //
				st(2, 17, 2, PROPERTY, DECL), //
				st(4, 1, 4, TYPE, 0), // 'item' open tag
				st(4, 6, 2, PROPERTY, READONLY) //
		);
		assertTrue(tokens.getResultId() == null || tokens.getResultId().isEmpty(),
				"Full semantic tokens should not have a resultId");
	}
}
