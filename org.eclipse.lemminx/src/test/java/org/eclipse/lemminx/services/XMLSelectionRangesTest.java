/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services;

import static org.eclipse.lemminx.XMLAssert.r;
import static org.eclipse.lemminx.XMLAssert.sr;
import static org.eclipse.lemminx.XMLAssert.testSelectionRange;

import org.junit.jupiter.api.Test;

public class XMLSelectionRangesTest {

	@Test
	public void testSimple() {
		String xml = "<aaa>Te|xt</aaa>";
		testSelectionRange(xml, //
				sr(r(0, 5, 0, 9), r(0, 0, 0, 15)));
	}

	@Test
	public void testMoreNesting() {
		String xml = "<aaa><bbb>Te|xt</bbb></aaa>";
		testSelectionRange(xml, //
				sr(r(0, 10, 0, 14), r(0, 5, 0, 20), r(0, 0, 0, 26)));
	}

	@Test
	public void testElementBounds() {
		String xml = "<aaa>|<bbb>Text</bbb></aaa>";
		testSelectionRange(xml, //
				sr(r(0, 5, 0, 20), r(0, 0, 0, 26)));
	}

	@Test
	public void testElementBounds2() {
		String xml = "<aaa><bbb>Text</bbb>|</aaa>";
		testSelectionRange(xml, //
				sr(r(0, 5, 0, 20), r(0, 0, 0, 26)));
	}

	@Test
	public void testElementBounds3() {
		String xml = "<aaa>\n" + //
				"  <bbb>Text</bbb>|<bbb>More Text</bbb>\n" + //
				"</aaa>";
		testSelectionRange(xml, //
				sr(r(1, 2, 1, 17), r(0, 5, 2, 0), r(0, 0, 2, 6)));
	}

	@Test
	public void testSelfClose() {
		String xml = "<aa|a />";
		testSelectionRange(xml, //
				sr(r(0, 0, 0, 7)));
	}

	@Test
	public void testEmpty() {
		String xml = "|";
		testSelectionRange(xml);
	}

	@Test
	public void testNoCursors() {
		String xml = "<aaa><bbb>Text</bbb></aaa>";
		testSelectionRange(xml);
	}

	@Test
	public void testJustText() {
		String xml = "Te|xt Content";
		testSelectionRange(xml, //
				sr(r(0, 0, 0, 12)));
	}

	@Test
	public void testMultilineText() {
		String xml = "<aaa>\n" + //
				"  <bbb>\n" + //
				"    Text Con|tent\n" + //
				"  </bbb>\n" + //
				"</aaa>\n";
		testSelectionRange(xml, //
				sr(r(1, 7, 3, 2), r(1, 2, 3, 8), r(0, 5, 4, 0), r(0, 0, 4, 6), r(0, 0, 5, 0)));
	}

	@Test
	public void testMultipleCursors() {
		String xml = "<aaa>\n" + //
				"  <bbb>\n" + //
				"    Hel|lo, World!\n" + //
				"  </bbb>\n" + //
				"  <bbb>\n" + //
				"    Sal|u, Terre!\n" + //
				"  </bbb>\n" + //
				"</aaa>\n";
		testSelectionRange(xml, //
				// First cursor
				sr(r(1, 7, 3, 2), r(1, 2, 3, 8), r(0, 5, 7, 0), r(0, 0, 7, 6), r(0, 0, 8, 0)),
				// Second cursor
				sr(r(4, 7, 6, 2), r(4, 2, 6, 8), r(0, 5, 7, 0), r(0, 0, 7, 6), r(0, 0, 8, 0)));
	}

	@Test
	public void testMultipleCursorsDifferentLevels() {
		String xml = "<aaa>\n" + //
				"  <bbb>\n" + //
				"    Hel|lo, World!\n" + //
				"  </bbb>\n" + //
				"  <bbb>\n" + //
				"    <ccc>\n" + //
				"      Sal|u, Terre!\n" + //
				"    </ccc>\n" + //
				"  </bbb>\n" + //
				"</aaa>\n";
		testSelectionRange(xml, //
				// First cursor
				sr(r(1, 7, 3, 2), r(1, 2, 3, 8), r(0, 5, 9, 0), r(0, 0, 9, 6), r(0, 0, 10, 0)),
				// Second cursor
				sr(r(5, 9, 7, 4), r(5, 4, 7, 10), r(4, 7, 8, 2), r(4, 2, 8, 8), r(0, 5, 9, 0), r(0, 0, 9, 6), r(0, 0, 10, 0)));
	}

	@Test
	public void testAttribute() {
		String xml = "<aaa key=\"val|ue\" otherKey=\"otherValue\" />";
		testSelectionRange(xml, //
				sr(r(0, 5, 0, 16), r(0, 0, 0, 41)));
	}

	@Test
	public void testAttribute2() {
		String xml = "<aaa ke|y=\"value\" otherKey=\"otherValue\" />";
		testSelectionRange(xml, //
				sr(r(0, 5, 0, 16), r(0, 0, 0, 41)));
	}

	@Test
	public void testAttribute3() {
		String xml = "<aaa |key=\"value\" otherKey=\"otherValue\" />";
		testSelectionRange(xml, //
				sr(r(0, 5, 0, 16), r(0, 0, 0, 41)));
	}

	@Test
	public void testAttributeInNestedElement() {
		String xml = "<aaa>\n" + //
				"  <bbb at|tr=\"value\">\n" + //
				"    Text\n" + //
				"  </bbb>\n" + //
				"</aaa>";
		testSelectionRange(xml, //
				sr(r(1, 7, 1, 19), r(1, 2, 3, 8), r(0, 5, 4, 0), r(0, 0, 4, 6)));
	}

	@Test
	public void testElementName() {
		String xml = "<aaa>\n" + //
				"  <b|bb>\n" + //
				"    Text Content\n" + //
				"  </bbb>\n" + //
				"</aaa>";
		testSelectionRange(xml, //
				sr(r(1, 3, 1, 6), r(1, 2, 3, 8), r(0, 5, 4, 0), r(0, 0, 4, 6)));
	}

	@Test
	public void testElementNameBounds() {
		String xml = "<aaa>\n" + //
				"  <|bbb>\n" + //
				"    Text Content\n" + //
				"  </bbb>\n" + //
				"</aaa>";
		testSelectionRange(xml, //
				sr(r(1, 3, 1, 6), r(1, 2, 3, 8), r(0, 5, 4, 0), r(0, 0, 4, 6)));
	}

	@Test
	public void testElementNameBounds2() {
		String xml = "<aaa>\n" + //
				"  <bbb|>\n" + //
				"    Text Content\n" + //
				"  </bbb>\n" + //
				"</aaa>";
		testSelectionRange(xml, //
				sr(r(1, 3, 1, 6), r(1, 2, 3, 8), r(0, 5, 4, 0), r(0, 0, 4, 6)));
	}

	@Test
	public void testElementNameBounds3() {
		String xml = "<aaa>\n" + //
				"  <bbb>\n" + //
				"    Text Content\n" + //
				"  </|bbb>\n" + //
				"</aaa>";
		testSelectionRange(xml, //
				sr(r(3, 4, 3, 7), r(1, 2, 3, 8), r(0, 5, 4, 0), r(0, 0, 4, 6)));
	}

	@Test
	public void testElementNameBounds4() {
		String xml = "<aaa>\n" + //
				"  <bbb>\n" + //
				"    Text Content\n" + //
				"  <|/bbb>\n" + //
				"</aaa>";
		testSelectionRange(xml, //
				sr(r(3, 4, 3, 7), r(1, 2, 3, 8), r(0, 5, 4, 0), r(0, 0, 4, 6)));
	}

	@Test
	public void testElementNameBounds5() {
		String xml = "<aaa>\n" + //
				"  <bbb>\n" + //
				"    Text Content\n" + //
				"  </bbb|>\n" + //
				"</aaa>";
		testSelectionRange(xml, //
				sr(r(3, 4, 3, 7), r(1, 2, 3, 8), r(0, 5, 4, 0), r(0, 0, 4, 6)));
	}

	@Test
	public void testElementNameBounds6() {
		String xml = "<aaa>\n" + //
				"  <bbb>\n" + //
				"    Text Content\n" + //
				"  |</bbb>\n" + //
				"</aaa>";
		testSelectionRange(xml, //
				sr(r(1, 7, 3, 2), r(1, 2, 3, 8), r(0, 5, 4, 0), r(0, 0, 4, 6)));
	}

	@Test
	public void testBrokenElement() {
		String xml = "<aaa>\n" + //
				"  <b|bb\n" + //
				"</aaa>";
		testSelectionRange(xml, //
				sr(r(1, 2, 2, 0), r(0, 5, 2, 0), r(0, 0, 2, 6)));
	}

	@Test
	public void testBrokenElement2() {
		String xml = "<aaa>\n" + //
				"  <b|bb>\n" + //
				"</aaa>";
		testSelectionRange(xml, //
				sr(r(1, 3, 1, 6), r(1, 2, 2, 0), r(0, 5, 2, 0), r(0, 0, 2, 6)));
	}

	@Test
	public void testBrokenElement3() {
		String xml = "<aaa>\n" + //
				"  </b|bb>\n" + //
				"</aaa>";
		testSelectionRange(xml, //
				sr(r(1, 4, 1, 7), r(1, 2, 1, 8), r(0, 5, 2, 0), r(0, 0, 2, 6)));
	}

	@Test
	public void testBrokenElement4() {
		String xml = "<aaa>\n" + //
				"  </b|bb\n" + //
				"</aaa>";
		testSelectionRange(xml, //
				sr(r(1, 2, 2, 0), r(0, 5, 2, 0), r(0, 0, 2, 6)));
	}

	@Test
	public void testMixedContent() {
		String xml = "<aaa>\n" + //
				"  Mixed Cont|ent\n" + //
				"  <bbb>\n" + //
				"    Text Content\n" + //
				"  </bbb>\n" + //
				"</aaa>";
		testSelectionRange(xml, //
				sr(r(0, 5, 2, 2), r(0, 5, 5, 0), r(0, 0, 5, 6)));
	}

	@Test
	public void testMixedContent2() {
		String xml = "<aaa>\n" + //
				"  Mixed Content\n" + //
				"  <bbb>\n" + //
				"    Text Con|tent\n" + //
				"  </bbb>\n" + //
				"</aaa>";
		testSelectionRange(xml, //
				sr(r(2, 7, 4, 2), r(2, 2, 4, 8), r(0, 5, 5, 0), r(0, 0, 5, 6)));
	}

	@Test
	public void testXMLProlog() {
		// The model represents the content of the xml prolog the same way as the content of an element
		String xml= "<?xml version=\"1.0\" encodi|ng=\"UTF-8\"?>\n" + //
				"<aaa>Content</aaa>";
		testSelectionRange(xml, //
				sr(r(0, 20, 0, 36), r(0, 0, 0, 38), r(0, 0, 1, 18)));
	}

	@Test
	public void testProcessingInstruction() {
		// The model represents the content of a processing instructions as plain text
		// and doesn't build more detail into the DOM.
		String xml= "<?xml-model hr|ef=\"file.dtd\" type=\"application/xml-dtd\"?>\n" + //
				"<aaa>Content</aaa>";
		testSelectionRange(xml, //
				sr(r(0, 0, 0, 56), r(0, 0, 1, 18)));
	}


	@Test
	public void testDoctypeDeclaration() {
		String xml = "<!DOCTYPE aaa [\n" + //
				"  <!ELEM|ENT aaa (bbb)+>\n" + //
				"  <!ELEMENT bbb (#PCDATA)>\n" + //
				"]>\n" + //
				"<aaa>\n" + //
				"  <bbb>Text Content</bbb>\n" + //
				"</aaa>\n";
		testSelectionRange(xml, //
				sr(r(1, 2, 1, 23), r(0, 15, 3, 0), r(0, 0, 3, 2), r(0, 0, 7, 0)));
	}

	@Test
	public void testDoctypeDeclaration2() {
		String xml = "<!DOCTYPE aaa [\n" + //
				"  <!ELEMENT aa|a (bbb)+>\n" + //
				"  <!ELEMENT bbb (#PCDATA)>\n" + //
				"]>\n" + //
				"<aaa>\n" + //
				"  <bbb>Text Content</bbb>\n" + //
				"</aaa>\n";
		testSelectionRange(xml, //
				sr(r(1, 2, 1, 23), r(0, 15, 3, 0), r(0, 0, 3, 2), r(0, 0, 7, 0)));
	}

	@Test
	public void testDoctypeDeclaration3() {
		String xml = "<!DOCTYPE aaa |[\n" + //
				"  <!ELEMENT aaa (bbb)+>\n" + //
				"  <!ELEMENT bbb (#PCDATA)>\n" + //
				"]>\n" + //
				"<aaa>\n" + //
				"  <bbb>Text Content</bbb>\n" + //
				"</aaa>\n";
		testSelectionRange(xml, //
				sr(r(0, 0, 3, 2), r(0, 0, 7, 0)));
	}

	@Test
	public void testDoctypeDeclaration4() {
		String xml = "<!DOCTYPE aaa [\n" + //
				"  <!ELEMENT aaa (bbb)+>\n" + //
				"  <!ELEMENT bbb (#PCDATA)>\n" + //
				"]|>\n" + //
				"<aaa>\n" + //
				"  <bbb>Text Content</bbb>\n" + //
				"</aaa>\n";
		testSelectionRange(xml, //
				sr(r(0, 0, 3, 2), r(0, 0, 7, 0)));
	}

	@Test
	public void testDoctypeDeclaration5() {
		// This test is written so that the behaviour matches that of starting tags
		String xml = "<!DOCTYPE aaa [\n" + //
				"  |<!ELEMENT aaa (bbb)+>\n" + //
				"  <!ELEMENT bbb (#PCDATA)>\n" + //
				"]>\n" + //
				"<aaa>\n" + //
				"  <bbb>Text Content</bbb>\n" + //
				"</aaa>\n";
		testSelectionRange(xml, //
				sr(r(1, 2, 1, 23), r(0, 15, 3, 0), r(0, 0, 3, 2), r(0, 0, 7, 0)));
	}

	@Test
	public void testDoctypeDeclaration6() {
		String xml = "<!DOCTYPE aaa [\n" + //
				"  <!ELEMENT aaa (bbb)+>|\n" + //
				"  <!ELEMENT bbb (#PCDATA)>\n" + //
				"]>\n" + //
				"<aaa>\n" + //
				"  <bbb>Text Content</bbb>\n" + //
				"</aaa>\n";
		testSelectionRange(xml, //
				sr(r(1, 2, 1, 23), r(0, 15, 3, 0), r(0, 0, 3, 2), r(0, 0, 7, 0)));
	}

	@Test
	public void testDoctypeDeclaration7() {
		String xml = "<!DOCTYPE aaa [\n" + //
				"  <!ELEMENT aaa (bbb)+>\n" + //
				"  <!ATTLIST aaa key CDATA \"value\">\n" + //
				"  <!ELEMENT b|bb (#PCDATA)>\n" + //
				"]>\n" + //
				"<aaa>\n" + //
				"  <bbb>Text Content</bbb>\n" + //
				"</aaa>\n";
		testSelectionRange(xml, //
				sr(r(3, 2, 3, 26), r(0, 15, 4, 0), r(0, 0, 4, 2), r(0, 0, 8, 0)));
	}

	@Test
	public void testDoctypeDeclaration8() {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + //
				"<!DOCTYPE aaa [\n" + //
				"  <!ELEMENT aaa (bbb)+>\n" + //
				"  <!ATTLIST aaa key CDATA \"value\">\n" + //
				"  <!ELEMENT b|bb (#PCDATA)>\n" + //
				"]>\n" + //
				"<aaa>\n" + //
				"  <bbb>Text Content</bbb>\n" + //
				"</aaa>\n";
		testSelectionRange(xml, //
				sr(r(4, 2, 4, 26), r(1, 15, 5, 0), r(1, 0, 5, 2), r(0, 0, 9, 0)));
	}

	@Test
	public void testDoctypeDeclaration9() {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + //
				"<!DOCTYPE aaa [\n" + //
				"  <!ELEMENT aaa (bbb)+>|<!ELEMENT bbb (#PCDATA)>\n" + //
				"]>\n" + //
				"<aaa>\n" + //
				"  <bbb>Text Content</bbb>\n" + //
				"</aaa>\n";
		testSelectionRange(xml, //
				sr(r(2, 2, 2, 23), r(1, 15, 3, 0), r(1, 0, 3, 2), r(0, 0, 7, 0)));
	}

	@Test
	public void testEmptyDoctypeDeclaration() {
		String xml = "<!DOCTYPE aaa [\n" + //
				"|]>\n" + //
				"<aaa>\n" + //
				"  <bbb>Text Content</bbb>\n" + //
				"</aaa>\n";
		testSelectionRange(xml, //
				sr(r(0, 15, 1, 0), r(0, 0, 1, 2), r(0, 0, 5, 0)));
	}

	@Test
	public void testEmptyDoctypeDeclaration2() {
		String xml = "<!DOCTYPE aaa [|\n" + //
				"]>\n" + //
				"<aaa>\n" + //
				"  <bbb>Text Content</bbb>\n" + //
				"</aaa>\n";
		testSelectionRange(xml, //
				sr(r(0, 15, 1, 0), r(0, 0, 1, 2), r(0, 0, 5, 0)));
	}

	@Test
	public void testEmptyDoctypeDeclaration3() {
		String xml = "<!DOCTYPE aa|a>\n" + //
				"<aaa>\n" + //
				"  <bbb>Text Content</bbb>\n" + //
				"</aaa>\n";
		testSelectionRange(xml, //
				sr(r(0, 0, 0, 14), r(0, 0, 4, 0)));
	}

}
