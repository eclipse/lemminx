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
package org.eclipse.lemminx.dom.parser;

import static org.junit.Assert.assertEquals;

import org.eclipse.lemminx.dom.parser.Scanner;
import org.eclipse.lemminx.dom.parser.TokenType;
import org.eclipse.lemminx.dom.parser.XMLScanner;
import org.junit.Test;

/**
 * XML scanner test to parse external DTD.
 *
 */
public class XMLScannerForExternalDTDTest {

	public Scanner scanner;

	@Test
	public void testDocumentTypeExternalOnly() {
		String dtd = "                \n" + //
				"  <!ENTITY nbsp \"&#xA0;\"> \n" + //
				"  <!ENTITY writer \"Writer: Donald Duck.\">\n" + //
				"";
		scanner = XMLScanner.createScanner(dtd, true);
		// assertOffsetAndToken(0, TokenType.DTDStartDoctypeTag);
		assertOffsetAndToken(0, TokenType.Content);
		// assertOffsetAndToken(19, TokenType.DTDDoctypeName);
		// assertOffsetAndToken(14, TokenType.Whitespace);
		// assertOffsetAndToken(15, TokenType.DTDStartInternalSubset);
		// assertOffsetAndToken(16, TokenType.Whitespace);
		assertOffsetAndToken(19, TokenType.DTDStartEntity);
		assertOffsetAndToken(27, TokenType.Whitespace);
		assertOffsetAndToken(28, TokenType.DTDEntityName);
		assertOffsetAndToken(32, TokenType.Whitespace);
		assertOffsetAndToken(33, TokenType.DTDEntityValue);
		assertOffsetAndToken(41, TokenType.DTDEndTag);
		assertOffsetAndToken(42, TokenType.Content);
		assertOffsetAndToken(46, TokenType.DTDStartEntity);
		assertOffsetAndToken(54, TokenType.Whitespace);
		assertOffsetAndToken(55, TokenType.DTDEntityName);
		assertOffsetAndToken(61, TokenType.Whitespace);
		assertOffsetAndToken(62, TokenType.DTDEntityValue);
		assertOffsetAndToken(84, TokenType.DTDEndTag);
		assertOffsetAndToken(85, TokenType.Content);
		// assertOffsetAndToken(86, TokenType.DTDEndInternalSubset);
		// assertOffsetAndToken(87, TokenType.DTDEndDoctypeTag);

	}

	@Test
	public void elementDeclWithSeveralBraces() {
		String dtd = "<!ELEMENT error-page ((error-code | exception-type), location)>";
		scanner = XMLScanner.createScanner(dtd, true);
		// assertOffsetAndToken(0, TokenType.DTDStartDoctypeTag);
		
		// assertOffsetAndToken(19, TokenType.DTDDoctypeName);
		// assertOffsetAndToken(14, TokenType.Whitespace);
		// assertOffsetAndToken(15, TokenType.DTDStartInternalSubset);
		// assertOffsetAndToken(16, TokenType.Whitespace);
		assertOffsetAndToken(0, TokenType.DTDStartElement);
		assertOffsetAndToken(9, TokenType.Whitespace);
		assertOffsetAndToken(10, TokenType.DTDElementDeclName);
		assertOffsetAndToken(20, TokenType.Whitespace);
		assertOffsetAndToken(21, TokenType.DTDStartElementContent);
		assertOffsetAndToken(22, TokenType.DTDElementContent);
		assertOffsetAndToken(61, TokenType.DTDEndElementContent);
		assertOffsetAndToken(62, TokenType.DTDEndTag);
		assertOffsetAndToken(63, TokenType.EOS);

		// assertOffsetAndToken(86, TokenType.DTDEndInternalSubset);
		// assertOffsetAndToken(87, TokenType.DTDEndDoctypeTag);

	}

	@Test
	public void notationDeclNormal() {
		String dtd = "<!NOTATION png PUBLIC \"JPG 1.0\" \"image/gif\">";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartNotation);
		assertOffsetAndToken(10, TokenType.Whitespace);
		assertOffsetAndToken(11, TokenType.DTDNotationName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDNotationKindPUBLIC);
		assertOffsetAndToken(21, TokenType.Whitespace);
		assertOffsetAndToken(22, TokenType.DTDNotationPublicId);
		assertOffsetAndToken(31, TokenType.Whitespace);
		assertOffsetAndToken(32, TokenType.DTDNotationSystemId);
		assertOffsetAndToken(43, TokenType.DTDEndTag);
		assertOffsetAndToken(44, TokenType.EOS);
	}

	@Test
	public void notationDeclMissingSystemId() {
		String dtd = "<!NOTATION png PUBLIC \"JPG 1.0\">";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartNotation);
		assertOffsetAndToken(10, TokenType.Whitespace);
		assertOffsetAndToken(11, TokenType.DTDNotationName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDNotationKindPUBLIC);
		assertOffsetAndToken(21, TokenType.Whitespace);
		assertOffsetAndToken(22, TokenType.DTDNotationPublicId);
		assertOffsetAndToken(31, TokenType.DTDEndTag);
		assertOffsetAndToken(32, TokenType.EOS);
	}

	@Test
	public void notationDeclMissingKind() {
		String dtd = "<!NOTATION png \"JPG 1.0\" \"image/gif\">";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartNotation);
		assertOffsetAndToken(10, TokenType.Whitespace);
		assertOffsetAndToken(11, TokenType.DTDNotationName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDUnrecognizedParameters);
		assertOffsetAndToken(36, TokenType.DTDEndTag);
		assertOffsetAndToken(37, TokenType.EOS);
	}

	@Test
	public void notationDeclIncorrectURLParameter() {
		String dtd = "<!NOTATION png PUBLIC bad_val >";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartNotation);
		assertOffsetAndToken(10, TokenType.Whitespace);
		assertOffsetAndToken(11, TokenType.DTDNotationName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDNotationKindPUBLIC);
		assertOffsetAndToken(21, TokenType.Whitespace);
		assertOffsetAndToken(22, TokenType.DTDUnrecognizedParameters);
		assertOffsetAndToken(30, TokenType.DTDEndTag);
		assertOffsetAndToken(31, TokenType.EOS);
	}

	@Test
	public void notationDeclUnclosed() {
		String dtd = "<!NOTATION png PUBLIC bad_val <!ELEMENT>";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartNotation);
		assertOffsetAndToken(10, TokenType.Whitespace);
		assertOffsetAndToken(11, TokenType.DTDNotationName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDNotationKindPUBLIC);
		assertOffsetAndToken(21, TokenType.Whitespace);
		assertOffsetAndToken(22, TokenType.DTDUnrecognizedParameters);
		assertOffsetAndToken(30, TokenType.DTDStartElement);
		assertOffsetAndToken(39, TokenType.DTDEndTag);
		assertOffsetAndToken(40, TokenType.EOS);
	}

	@Test
	public void notationDeclUnclosed2() {
		String dtd = "<!NOTATION png PUBLIC <!ELEMENT>";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartNotation);
		assertOffsetAndToken(10, TokenType.Whitespace);
		assertOffsetAndToken(11, TokenType.DTDNotationName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDNotationKindPUBLIC);
		assertOffsetAndToken(21, TokenType.Whitespace);
		assertOffsetAndToken(22, TokenType.DTDStartElement);
		assertOffsetAndToken(31, TokenType.DTDEndTag);
		assertOffsetAndToken(32, TokenType.EOS);
	}

	@Test
	public void notationDeclUnclosed3() {
		String dtd = "<!NOTATION png <!ELEMENT>";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartNotation);
		assertOffsetAndToken(10, TokenType.Whitespace);
		assertOffsetAndToken(11, TokenType.DTDNotationName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDStartElement);
		assertOffsetAndToken(24, TokenType.DTDEndTag);
		assertOffsetAndToken(25, TokenType.EOS);
	}

	@Test
	public void notationDeclUnclosed4() {
		String dtd = "<!NOTATION png PUBLIC \"JPG 1.0\" \"image/gif\"<!ELEMENT>";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartNotation);
		assertOffsetAndToken(10, TokenType.Whitespace);
		assertOffsetAndToken(11, TokenType.DTDNotationName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDNotationKindPUBLIC);
		assertOffsetAndToken(21, TokenType.Whitespace);
		assertOffsetAndToken(22, TokenType.DTDNotationPublicId);
		assertOffsetAndToken(31, TokenType.Whitespace);
		assertOffsetAndToken(32, TokenType.DTDNotationSystemId);
		assertOffsetAndToken(43, TokenType.DTDStartElement);
		assertOffsetAndToken(52, TokenType.DTDEndTag);
		assertOffsetAndToken(53, TokenType.EOS);
	}

	@Test
	public void notationDeclNormalMoreSpaces() {
		String dtd = "<!NOTATION png   PUBLIC \"JPG 1.0\"   \"image/gif\" >";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartNotation);
		assertOffsetAndToken(10, TokenType.Whitespace);
		assertOffsetAndToken(11, TokenType.DTDNotationName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(17, TokenType.DTDNotationKindPUBLIC);
		assertOffsetAndToken(23, TokenType.Whitespace);
		assertOffsetAndToken(24, TokenType.DTDNotationPublicId);
		assertOffsetAndToken(33, TokenType.Whitespace);
		assertOffsetAndToken(36, TokenType.DTDNotationSystemId);
		assertOffsetAndToken(47, TokenType.Whitespace);
		assertOffsetAndToken(48, TokenType.DTDEndTag);
		assertOffsetAndToken(49, TokenType.EOS);
	}

	@Test
	public void notationDeclUnrecognizedContent() {
		String dtd = "<!NOTATION png   PUBLIC \"JPG 1.0\"   zz>";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartNotation);
		assertOffsetAndToken(10, TokenType.Whitespace);
		assertOffsetAndToken(11, TokenType.DTDNotationName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(17, TokenType.DTDNotationKindPUBLIC);
		assertOffsetAndToken(23, TokenType.Whitespace);
		assertOffsetAndToken(24, TokenType.DTDNotationPublicId);
		assertOffsetAndToken(33, TokenType.Whitespace);
		assertOffsetAndToken(36, TokenType.DTDUnrecognizedParameters);
		assertOffsetAndToken(38, TokenType.DTDEndTag);
		assertOffsetAndToken(39, TokenType.EOS);
	}

	@Test
	public void notationNoParameters() {
		String dtd = "<!NOTATION <!ELEMENT >";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartNotation);
		assertOffsetAndToken(10, TokenType.Whitespace);
		assertOffsetAndToken(11, TokenType.DTDStartElement);
		assertOffsetAndToken(20, TokenType.Whitespace);
		assertOffsetAndToken(21, TokenType.DTDEndTag);
		assertOffsetAndToken(22, TokenType.EOS);
	}

	@Test
	public void elementDeclContent() {
		String dtd = "<!ELEMENT note (to,from,heading,body)>";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartElement);
		assertOffsetAndToken(9, TokenType.Whitespace);
		assertOffsetAndToken(10, TokenType.DTDElementDeclName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDStartElementContent);
		assertOffsetAndToken(16, TokenType.DTDElementContent);
		assertOffsetAndToken(36, TokenType.DTDEndElementContent);
		assertOffsetAndToken(37, TokenType.DTDEndTag);
		assertOffsetAndToken(38, TokenType.EOS);
	}

	@Test
	public void elementDeclContentWithProlog() {
		String dtd =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<!ELEMENT note (to,from,heading,body)>";

		scanner = XMLScanner.createScanner(dtd, true);

		assertOffsetAndToken(0, TokenType.StartPrologOrPI);
		assertOffsetAndToken(2, TokenType.PrologName, "xml");
		assertOffsetAndToken(5, TokenType.Whitespace);
		assertOffsetAndToken(6, TokenType.AttributeName);
		assertOffsetAndToken(13, TokenType.DelimiterAssign);
		assertOffsetAndToken(14, TokenType.AttributeValue);
		assertOffsetAndToken(19, TokenType.Whitespace);
		assertOffsetAndToken(20, TokenType.AttributeName);
		assertOffsetAndToken(28, TokenType.DelimiterAssign);
		assertOffsetAndToken(29, TokenType.AttributeValue);
		assertOffsetAndToken(36, TokenType.PrologEnd);
		assertOffsetAndToken(38, TokenType.Content);
		assertOffsetAndToken(39, TokenType.DTDStartElement);
		assertOffsetAndToken(48, TokenType.Whitespace);
		assertOffsetAndToken(49, TokenType.DTDElementDeclName);
		assertOffsetAndToken(53, TokenType.Whitespace);
		assertOffsetAndToken(54, TokenType.DTDStartElementContent);
		assertOffsetAndToken(55, TokenType.DTDElementContent);
		assertOffsetAndToken(75, TokenType.DTDEndElementContent);
		assertOffsetAndToken(76, TokenType.DTDEndTag);
		assertOffsetAndToken(77, TokenType.EOS);
	}

	@Test
	public void elementOnlyName() {
		String dtd = "<!ELEMENT note > <!ENTITY>";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartElement);
		assertOffsetAndToken(9, TokenType.Whitespace);
		assertOffsetAndToken(10, TokenType.DTDElementDeclName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDEndTag);
		assertOffsetAndToken(16, TokenType.Content);
		assertOffsetAndToken(17, TokenType.DTDStartEntity);
		assertOffsetAndToken(25, TokenType.DTDEndTag);
		assertOffsetAndToken(26, TokenType.EOS);
	}

	@Test
	public void elementUnclosed() {
		String dtd = "<!ELEMENT note EMPTY> <!ENTITY>";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartElement);
		assertOffsetAndToken(9, TokenType.Whitespace);
		assertOffsetAndToken(10, TokenType.DTDElementDeclName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDElementCategory);
		assertOffsetAndToken(20, TokenType.DTDEndTag);
		assertOffsetAndToken(21, TokenType.Content);
		assertOffsetAndToken(22, TokenType.DTDStartEntity);
		assertOffsetAndToken(30, TokenType.DTDEndTag);
		assertOffsetAndToken(31, TokenType.EOS);
	}

	@Test
	public void elementDeclCategory() {
		String dtd = "<!ELEMENT note ANY>";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartElement);
		assertOffsetAndToken(9, TokenType.Whitespace);
		assertOffsetAndToken(10, TokenType.DTDElementDeclName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDElementCategory);
		assertOffsetAndToken(18, TokenType.DTDEndTag);
		assertOffsetAndToken(19, TokenType.EOS);
	}

	@Test
	public void elementDeclContentIncomplete() {
		String dtd = "<!ELEMENT note (to,from,heading >";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartElement);
		assertOffsetAndToken(9, TokenType.Whitespace);
		assertOffsetAndToken(10, TokenType.DTDElementDeclName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDStartElementContent);
		assertOffsetAndToken(16, TokenType.DTDElementContent);
		assertOffsetAndToken(32, TokenType.DTDEndTag);
		assertOffsetAndToken(33, TokenType.EOS);
	}

	@Test
	public void elementDeclContentIncompleteAndUnclosed() {
		String dtd = "<!ELEMENT note (to,from,heading <!ATTLIST >";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartElement);
		assertOffsetAndToken(9, TokenType.Whitespace);
		assertOffsetAndToken(10, TokenType.DTDElementDeclName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDStartElementContent);
		assertOffsetAndToken(16, TokenType.DTDElementContent);
		assertOffsetAndToken(32, TokenType.DTDStartAttlist);
		assertOffsetAndToken(41, TokenType.Whitespace);
		assertOffsetAndToken(42, TokenType.DTDEndTag);
		assertOffsetAndToken(43, TokenType.EOS);
	}

	@Test
	public void elementNoParameters() {
		String dtd = "<!ELEMENT   <!ATTLIST >";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartElement);
		assertOffsetAndToken(9, TokenType.Whitespace);
		assertOffsetAndToken(12, TokenType.DTDStartAttlist);
		assertOffsetAndToken(21, TokenType.Whitespace);
		assertOffsetAndToken(22, TokenType.DTDEndTag);
		assertOffsetAndToken(23, TokenType.EOS);
	}

	@Test
	public void attlistDecl() {
		String dtd = "<!ATTLIST elName attName CDATA \"defaultVal\">";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartAttlist);
		assertOffsetAndToken(9, TokenType.Whitespace);
		assertOffsetAndToken(10, TokenType.DTDAttlistElementName);
		assertOffsetAndToken(16, TokenType.Whitespace);
		assertOffsetAndToken(17, TokenType.DTDAttlistAttributeName);
		assertOffsetAndToken(24, TokenType.Whitespace);
		assertOffsetAndToken(25, TokenType.DTDAttlistAttributeType);
		assertOffsetAndToken(30, TokenType.Whitespace);
		assertOffsetAndToken(31, TokenType.DTDAttlistAttributeValue);
		assertOffsetAndToken(43, TokenType.DTDEndTag);
		assertOffsetAndToken(44, TokenType.EOS);
	}

	@Test
	public void attlistDeclWithProlog() {
		String dtd =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<!ATTLIST elName attName CDATA \"defaultVal\">";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.StartPrologOrPI);
		assertOffsetAndToken(2, TokenType.PrologName, "xml");
		assertOffsetAndToken(5, TokenType.Whitespace);
		assertOffsetAndToken(6, TokenType.AttributeName);
		assertOffsetAndToken(13, TokenType.DelimiterAssign);
		assertOffsetAndToken(14, TokenType.AttributeValue);
		assertOffsetAndToken(19, TokenType.Whitespace);
		assertOffsetAndToken(20, TokenType.AttributeName);
		assertOffsetAndToken(28, TokenType.DelimiterAssign);
		assertOffsetAndToken(29, TokenType.AttributeValue);
		assertOffsetAndToken(36, TokenType.PrologEnd);
		assertOffsetAndToken(38, TokenType.Content);
		assertOffsetAndToken(39, TokenType.DTDStartAttlist);
		assertOffsetAndToken(48, TokenType.Whitespace);
		assertOffsetAndToken(49, TokenType.DTDAttlistElementName);
		assertOffsetAndToken(55, TokenType.Whitespace);
		assertOffsetAndToken(56, TokenType.DTDAttlistAttributeName);
		assertOffsetAndToken(63, TokenType.Whitespace);
		assertOffsetAndToken(64, TokenType.DTDAttlistAttributeType);
		assertOffsetAndToken(69, TokenType.Whitespace);
		assertOffsetAndToken(70, TokenType.DTDAttlistAttributeValue);
		assertOffsetAndToken(82, TokenType.DTDEndTag);
		assertOffsetAndToken(83, TokenType.EOS);
	}

	@Test
	public void attlistMultipleDecls() {
		String dtd = 
		"<!ATTLIST elName \n" + 
		"    attName1 CDATA \"defaultVal1\"\n" +
		"    attName2 CDATA \"defaultVal2\">";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartAttlist);
		assertOffsetAndToken(9, TokenType.Whitespace);
		assertOffsetAndToken(10, TokenType.DTDAttlistElementName);
		assertOffsetAndToken(16, TokenType.Whitespace);
		assertOffsetAndToken(22, TokenType.DTDAttlistAttributeName);
		assertOffsetAndToken(30, TokenType.Whitespace);
		assertOffsetAndToken(31, TokenType.DTDAttlistAttributeType);
		assertOffsetAndToken(36, TokenType.Whitespace);
		assertOffsetAndToken(37, TokenType.DTDAttlistAttributeValue);
		assertOffsetAndToken(50, TokenType.Whitespace);
		assertOffsetAndToken(55, TokenType.DTDAttlistAttributeName);
		assertOffsetAndToken(63, TokenType.Whitespace);
		assertOffsetAndToken(64, TokenType.DTDAttlistAttributeType);
		assertOffsetAndToken(69, TokenType.Whitespace);
		assertOffsetAndToken(70, TokenType.DTDAttlistAttributeValue);
		assertOffsetAndToken(83, TokenType.DTDEndTag);
		assertOffsetAndToken(84, TokenType.EOS);
	}

	@Test
	public void attlistIncompleteDecl() {
		String dtd = 
		"<!ATTLIST elName \n" + 
		"    attName1 CDATA \n" +
		"    attName2 CDATA \"defaultVal2\">";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartAttlist);
		assertOffsetAndToken(9, TokenType.Whitespace);
		assertOffsetAndToken(10, TokenType.DTDAttlistElementName);
		assertOffsetAndToken(16, TokenType.Whitespace);
		assertOffsetAndToken(22, TokenType.DTDAttlistAttributeName);
		assertOffsetAndToken(30, TokenType.Whitespace);
		assertOffsetAndToken(31, TokenType.DTDAttlistAttributeType);
		assertOffsetAndToken(36, TokenType.Whitespace);
		assertOffsetAndToken(42, TokenType.DTDUnrecognizedParameters);
		assertOffsetAndToken(70, TokenType.DTDEndTag);
		assertOffsetAndToken(71, TokenType.EOS);
	}

	@Test
	public void attlistMissingAttributeName() {
		String dtd = 
		"<!ATTLIST elName CDATA \"value\" >";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartAttlist);
		assertOffsetAndToken(9, TokenType.Whitespace);
		assertOffsetAndToken(10, TokenType.DTDAttlistElementName);
		assertOffsetAndToken(16, TokenType.Whitespace);
		assertOffsetAndToken(17, TokenType.DTDAttlistAttributeName);
		assertOffsetAndToken(22, TokenType.Whitespace);
		assertOffsetAndToken(23, TokenType.DTDUnrecognizedParameters);
		assertOffsetAndToken(31, TokenType.DTDEndTag);
		assertOffsetAndToken(32, TokenType.EOS);
	}

	@Test
	public void attlistMissingAttributeType() {
		String dtd = 
		"<!ATTLIST elName attName \"value\" >";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartAttlist);
		assertOffsetAndToken(9, TokenType.Whitespace);
		assertOffsetAndToken(10, TokenType.DTDAttlistElementName);
		assertOffsetAndToken(16, TokenType.Whitespace);
		assertOffsetAndToken(17, TokenType.DTDAttlistAttributeName);
		assertOffsetAndToken(24, TokenType.Whitespace);
		assertOffsetAndToken(25, TokenType.DTDUnrecognizedParameters);
		assertOffsetAndToken(33, TokenType.DTDEndTag);
		assertOffsetAndToken(34, TokenType.EOS);
	}

	@Test
	public void attlistMissingAttributeValue() {
		String dtd = 
		"<!ATTLIST elName attName CDATA >";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartAttlist);
		assertOffsetAndToken(9, TokenType.Whitespace);
		assertOffsetAndToken(10, TokenType.DTDAttlistElementName);
		assertOffsetAndToken(16, TokenType.Whitespace);
		assertOffsetAndToken(17, TokenType.DTDAttlistAttributeName);
		assertOffsetAndToken(24, TokenType.Whitespace);
		assertOffsetAndToken(25, TokenType.DTDAttlistAttributeType);
		assertOffsetAndToken(30, TokenType.Whitespace);
		assertOffsetAndToken(31, TokenType.DTDEndTag);
		assertOffsetAndToken(32, TokenType.EOS);
	}

	@Test
	public void attlistNoParameters() {
		String dtd = "<!ATTLIST <!ELEMENT >";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartAttlist);
		assertOffsetAndToken(9, TokenType.Whitespace);
		assertOffsetAndToken(10, TokenType.DTDStartElement);
		assertOffsetAndToken(19, TokenType.Whitespace);
		assertOffsetAndToken(20, TokenType.DTDEndTag);
		assertOffsetAndToken(21, TokenType.EOS);
	}

	@Test
	public void attlistMissingAttributeNameAndEndBracket() {
		String dtd = 
		"<!ATTLIST elName CDATA \"value\"";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartAttlist);
		assertOffsetAndToken(9, TokenType.Whitespace);
		assertOffsetAndToken(10, TokenType.DTDAttlistElementName);
		assertOffsetAndToken(16, TokenType.Whitespace);
		assertOffsetAndToken(17, TokenType.DTDAttlistAttributeName);
		assertOffsetAndToken(22, TokenType.Whitespace);
		assertOffsetAndToken(23, TokenType.DTDUnrecognizedParameters);
		assertOffsetAndToken(30, TokenType.EOS);
	}

	@Test
	public void attlistDeclMissingEndBracket() {
		String dtd = "<!ATTLIST elName attName CDATA \"defaultVal\" <!ELEMENT>";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartAttlist);
		assertOffsetAndToken(9, TokenType.Whitespace);
		assertOffsetAndToken(10, TokenType.DTDAttlistElementName);
		assertOffsetAndToken(16, TokenType.Whitespace);
		assertOffsetAndToken(17, TokenType.DTDAttlistAttributeName);
		assertOffsetAndToken(24, TokenType.Whitespace);
		assertOffsetAndToken(25, TokenType.DTDAttlistAttributeType);
		assertOffsetAndToken(30, TokenType.Whitespace);
		assertOffsetAndToken(31, TokenType.DTDAttlistAttributeValue);
		assertOffsetAndToken(43, TokenType.Whitespace);
		assertOffsetAndToken(44, TokenType.DTDStartElement);
		assertOffsetAndToken(53, TokenType.DTDEndTag);
		assertOffsetAndToken(54, TokenType.EOS);
	}

	@Test
	public void entityPUBLIC() {
		String dtd = 
		"<!ENTITY eName PUBLIC \"public/Id\" \"system/Id\" >";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartEntity);
		assertOffsetAndToken(8, TokenType.Whitespace);
		assertOffsetAndToken(9, TokenType.DTDEntityName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDEntityKindPUBLIC);
		assertOffsetAndToken(21, TokenType.Whitespace);
		assertOffsetAndToken(22, TokenType.DTDEntityPublicId);
		assertOffsetAndToken(33, TokenType.Whitespace);
		assertOffsetAndToken(34, TokenType.DTDEntitySystemId);
		assertOffsetAndToken(45, TokenType.Whitespace);
		assertOffsetAndToken(46, TokenType.DTDEndTag);
		assertOffsetAndToken(47, TokenType.EOS);
	}

	@Test
	public void entitySYSTEM() {
		String dtd = 
		"<!ENTITY eName SYSTEM   \"system/Id\" >";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartEntity);
		assertOffsetAndToken(8, TokenType.Whitespace);
		assertOffsetAndToken(9, TokenType.DTDEntityName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDEntityKindSYSTEM);
		assertOffsetAndToken(21, TokenType.Whitespace);
		assertOffsetAndToken(24, TokenType.DTDEntitySystemId);
		assertOffsetAndToken(35, TokenType.Whitespace);
		assertOffsetAndToken(36, TokenType.DTDEndTag);
		assertOffsetAndToken(37, TokenType.EOS);
	}

	@Test
	public void entityCorrect() {
		String dtd = 
		"<!ENTITY eName \"eValue\" >";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartEntity);
		assertOffsetAndToken(8, TokenType.Whitespace);
		assertOffsetAndToken(9, TokenType.DTDEntityName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDEntityValue);
		assertOffsetAndToken(23, TokenType.Whitespace);
		assertOffsetAndToken(24, TokenType.DTDEndTag);
		assertOffsetAndToken(25, TokenType.EOS);
	}

	@Test
	public void entityCorrectWithPercent() {
		String dtd = 
		"<!ENTITY % eName \"eValue\" >";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartEntity);
		assertOffsetAndToken(8, TokenType.Whitespace);
		assertOffsetAndToken(9, TokenType.DTDEntityPercent);
		assertOffsetAndToken(10, TokenType.Whitespace);
		assertOffsetAndToken(11, TokenType.DTDEntityName);
		assertOffsetAndToken(16, TokenType.Whitespace);
		assertOffsetAndToken(17, TokenType.DTDEntityValue);
		assertOffsetAndToken(25, TokenType.Whitespace);
		assertOffsetAndToken(26, TokenType.DTDEndTag);
		assertOffsetAndToken(27, TokenType.EOS);
	}

	@Test
	public void entityCorrectWithPercentAndPublic() {
		String dtd = 
		"<!ENTITY % eName PUBLIC \"publicId\" \"systemId\" >";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartEntity);
		assertOffsetAndToken(8, TokenType.Whitespace);
		assertOffsetAndToken(9, TokenType.DTDEntityPercent);
		assertOffsetAndToken(10, TokenType.Whitespace);
		assertOffsetAndToken(11, TokenType.DTDEntityName);
		assertOffsetAndToken(16, TokenType.Whitespace);
		assertOffsetAndToken(17, TokenType.DTDEntityKindPUBLIC);
		assertOffsetAndToken(23, TokenType.Whitespace);
		assertOffsetAndToken(24, TokenType.DTDEntityPublicId);
		assertOffsetAndToken(34, TokenType.Whitespace);
		assertOffsetAndToken(35, TokenType.DTDEntitySystemId);
		assertOffsetAndToken(45, TokenType.Whitespace);
		assertOffsetAndToken(46, TokenType.DTDEndTag);
		assertOffsetAndToken(47, TokenType.EOS);
	}

	@Test
	public void entityIncorrectWithPercentAndPublic() {
		String dtd = 
		"<!ENTITY % eName PUBLIC \"publicId\" \"systemId\" garbage>";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartEntity);
		assertOffsetAndToken(8, TokenType.Whitespace);
		assertOffsetAndToken(9, TokenType.DTDEntityPercent);
		assertOffsetAndToken(10, TokenType.Whitespace);
		assertOffsetAndToken(11, TokenType.DTDEntityName);
		assertOffsetAndToken(16, TokenType.Whitespace);
		assertOffsetAndToken(17, TokenType.DTDEntityKindPUBLIC);
		assertOffsetAndToken(23, TokenType.Whitespace);
		assertOffsetAndToken(24, TokenType.DTDEntityPublicId);
		assertOffsetAndToken(34, TokenType.Whitespace);
		assertOffsetAndToken(35, TokenType.DTDEntitySystemId);
		assertOffsetAndToken(45, TokenType.Whitespace);
		assertOffsetAndToken(46, TokenType.DTDUnrecognizedParameters);
		assertOffsetAndToken(53, TokenType.DTDEndTag);
		assertOffsetAndToken(54, TokenType.EOS);
	}

	@Test
	public void entityIncorrectMissingSystemId() {
		String dtd = 
		"<!ENTITY eName PUBLIC \"publicId\" aa>";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartEntity);
		assertOffsetAndToken(8, TokenType.Whitespace);
		assertOffsetAndToken(9, TokenType.DTDEntityName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDEntityKindPUBLIC);
		assertOffsetAndToken(21, TokenType.Whitespace);
		assertOffsetAndToken(22, TokenType.DTDEntityPublicId);
		assertOffsetAndToken(32, TokenType.Whitespace);
		assertOffsetAndToken(33, TokenType.DTDUnrecognizedParameters);
		assertOffsetAndToken(35, TokenType.DTDEndTag);
		assertOffsetAndToken(36, TokenType.EOS);
	}

	@Test
	public void entityIncorrectMissingSystemIdUnclosed() {
		String dtd = 
		"<!ENTITY eName PUBLIC \"publicId\" <!ELEMENT>";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartEntity);
		assertOffsetAndToken(8, TokenType.Whitespace);
		assertOffsetAndToken(9, TokenType.DTDEntityName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDEntityKindPUBLIC);
		assertOffsetAndToken(21, TokenType.Whitespace);
		assertOffsetAndToken(22, TokenType.DTDEntityPublicId);
		assertOffsetAndToken(32, TokenType.Whitespace);
		assertOffsetAndToken(33, TokenType.DTDStartElement);
		assertOffsetAndToken(42, TokenType.DTDEndTag);
		assertOffsetAndToken(43, TokenType.EOS);
	}

	@Test
	public void entityIncorrectMissingIDs() {
		String dtd = 
		"<!ENTITY eName PUBLIC aa>";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartEntity);
		assertOffsetAndToken(8, TokenType.Whitespace);
		assertOffsetAndToken(9, TokenType.DTDEntityName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDEntityKindPUBLIC);
		assertOffsetAndToken(21, TokenType.Whitespace);
		assertOffsetAndToken(22, TokenType.DTDUnrecognizedParameters);
		assertOffsetAndToken(24, TokenType.DTDEndTag);
		assertOffsetAndToken(25, TokenType.EOS);
	}

	@Test
	public void entityIncorrect() {
		String dtd = 
		"<!ENTITY eName PUBLIC aa <!ATTLIST>";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.DTDStartEntity);
		assertOffsetAndToken(8, TokenType.Whitespace);
		assertOffsetAndToken(9, TokenType.DTDEntityName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDEntityKindPUBLIC);
		assertOffsetAndToken(21, TokenType.Whitespace);
		assertOffsetAndToken(22, TokenType.DTDUnrecognizedParameters);
		assertOffsetAndToken(25, TokenType.DTDStartAttlist);
		assertOffsetAndToken(34, TokenType.DTDEndTag);
		assertOffsetAndToken(35, TokenType.EOS);
	}


	@Test
	public void dtdUnrecognizedTagName() {
		String dtd = 
		"<!BAD eName aaa \"asdasd\">";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.Content);
		assertOffsetAndToken(25, TokenType.EOS);
	}

	@Test
	public void dtdUnrecognizedTagName2() {
		String dtd =
		"<!BAD eName aaa \"asdasd\"> <!ELEMENT >";
		scanner = XMLScanner.createScanner(dtd, true);
		assertOffsetAndToken(0, TokenType.Content);
		assertOffsetAndToken(25, TokenType.Content);
		assertOffsetAndToken(26, TokenType.DTDStartElement);
		assertOffsetAndToken(35, TokenType.Whitespace);
		assertOffsetAndToken(36, TokenType.DTDEndTag);
		assertOffsetAndToken(37, TokenType.EOS);
	}

	@Test
	public void dtdUnrecognizedContent() {
		String dtd = "<!ELEMENT>    aaa gf  <!ATTLIST>";

		scanner = XMLScanner.createScanner(dtd, true);
	
		assertOffsetAndToken(0, TokenType.DTDStartElement);
		assertOffsetAndToken(9, TokenType.DTDEndTag);
		assertOffsetAndToken(10, TokenType.Content);
		assertOffsetAndToken(22, TokenType.DTDStartAttlist);
		assertOffsetAndToken(31, TokenType.DTDEndTag);
		assertOffsetAndToken(32, TokenType.EOS);
	}
	
	@Test
	public void dtdUnrecognizedContent2() {
		String dtd = 
				"<![ %HTML.Reserved; [\r\n" + 
				"<!ENTITY % reserved\r\n" + 
				" \"datasrc     %URI;          #IMPLIED  -- \"\r\n" + 
				"  >\r\n" + 
				"]]>\r\n" + 
				"\r\n" + 
				"<!--=================== Text Markup ======================================-->";

		scanner = XMLScanner.createScanner(dtd, true);
	
		assertOffsetAndToken(0, TokenType.Content);

		assertOffsetAndToken(23, TokenType.DTDStartEntity);
		assertOffsetAndToken(31, TokenType.Whitespace);
		assertOffsetAndToken(32, TokenType.DTDEntityPercent);
		assertOffsetAndToken(33, TokenType.Whitespace);
		assertOffsetAndToken(34, TokenType.DTDEntityName);
		assertOffsetAndToken(42, TokenType.Whitespace);
		assertOffsetAndToken(45, TokenType.DTDEntityValue);
		assertOffsetAndToken(87, TokenType.Whitespace);
		assertOffsetAndToken(91, TokenType.DTDEndTag);

		assertOffsetAndToken(92, TokenType.Content); // " ... ]]> ..."

		assertOffsetAndToken(101, TokenType.StartCommentTag);
		assertOffsetAndToken(105, TokenType.Comment);
		assertOffsetAndToken(175, TokenType.EndCommentTag);
		assertOffsetAndToken(178, TokenType.EOS);
	}

	public void assertOffsetAndToken(int tokenOffset, TokenType tokenType) {
		TokenType token = scanner.scan();
		// System.err.println("assertOffsetAndToken(" + scanner.getTokenOffset() +  ", TokenType." + scanner.getTokenType() + ");");
		assertEquals(tokenOffset, scanner.getTokenOffset());
		assertEquals(tokenType, token);
	}

	public void assertOffsetAndToken(int tokenOffset, TokenType tokenType, String tokenText) {
		TokenType token = scanner.scan();
		assertEquals(tokenOffset, scanner.getTokenOffset());
		assertEquals(tokenType, token);
		assertEquals(tokenText, scanner.getTokenText());
	}
}
