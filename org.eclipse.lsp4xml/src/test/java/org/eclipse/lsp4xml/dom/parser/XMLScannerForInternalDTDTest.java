/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.dom.parser;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * XML scanner test to parse internal DTD (declared inside XML  with <!DOCTYPE)
 *
 */
public class XMLScannerForInternalDTDTest {

	public Scanner scanner;

	@Test
	public void testDocumentTypeInternalOnly() {
		String xml = 
		"<!DOCTYPE note [\n" +
		"  <!ENTITY nbsp \"&#xA0;\"> \n" +
		"  <!ENTITY writer \"Writer: Donald Duck.\">\n" +
		"]>";
		scanner = XMLScanner.createScanner(xml);
		assertOffsetAndToken(0, TokenType.DTDStartDoctypeTag);
		assertOffsetAndToken(9, TokenType.Whitespace);
		assertOffsetAndToken(10, TokenType.DTDDoctypeName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDStartInternalSubset);
		assertOffsetAndToken(16, TokenType.Whitespace);
		assertOffsetAndToken(19, TokenType.DTDStartEntity);
		assertOffsetAndToken(27, TokenType.Whitespace);
		assertOffsetAndToken(28, TokenType.DTDEntityName);
		assertOffsetAndToken(32, TokenType.Whitespace);
		assertOffsetAndToken(33, TokenType.DTDEntityValue);
		assertOffsetAndToken(41, TokenType.DTDEndTag);
		assertOffsetAndToken(42, TokenType.Whitespace);
		assertOffsetAndToken(46, TokenType.DTDStartEntity);
		assertOffsetAndToken(54, TokenType.Whitespace);
		assertOffsetAndToken(55, TokenType.DTDEntityName);
		assertOffsetAndToken(61, TokenType.Whitespace);
		assertOffsetAndToken(62, TokenType.DTDEntityValue);
		assertOffsetAndToken(84, TokenType.DTDEndTag);
		assertOffsetAndToken(85, TokenType.Whitespace);
		assertOffsetAndToken(86, TokenType.DTDEndInternalSubset);
		assertOffsetAndToken(87, TokenType.DTDEndDoctypeTag);
	
	}

	@Test
	public void testDocumentTypePublicAndInternal() {
		
		String xml = 
		"<!DOCTYPE html PUBLIC\n" +
		"  \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n" +
		"  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"\n" +
		"  [\n" +
		"    <!ENTITY nbsp \"&#xA0;\"> \n" +
		"  ]\n" +
		">";
		scanner = XMLScanner.createScanner(xml);
		assertOffsetAndToken(0, TokenType.DTDStartDoctypeTag);
		assertOffsetAndToken(9, TokenType.Whitespace);
		assertOffsetAndToken(10, TokenType.DTDDoctypeName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDDocTypeKindPUBLIC);
		assertOffsetAndToken(21, TokenType.Whitespace);
		assertOffsetAndToken(24, TokenType.DTDDoctypePublicId);
		assertOffsetAndToken(64, TokenType.Whitespace);
		assertOffsetAndToken(67, TokenType.DTDDoctypeSystemId);
		assertOffsetAndToken(124, TokenType.Whitespace);
		assertOffsetAndToken(127, TokenType.DTDStartInternalSubset);
		assertOffsetAndToken(128, TokenType.Whitespace);
		assertOffsetAndToken(133, TokenType.DTDStartEntity);
		assertOffsetAndToken(141, TokenType.Whitespace);
		assertOffsetAndToken(142, TokenType.DTDEntityName);
		assertOffsetAndToken(146, TokenType.Whitespace);
		assertOffsetAndToken(147, TokenType.DTDEntityValue);
		assertOffsetAndToken(155, TokenType.DTDEndTag);
		assertOffsetAndToken(156, TokenType.Whitespace);
		assertOffsetAndToken(160, TokenType.DTDEndInternalSubset);
		assertOffsetAndToken(161, TokenType.Whitespace);
		assertOffsetAndToken(162, TokenType.DTDEndDoctypeTag);
		assertOffsetAndToken(163, TokenType.EOS);
	}

	@Test
	public void testDocumentTypeSystemAndInternal() {
		
		String xml = 
		"<!DOCTYPE html SYSTEM\n" +
		"  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"\n" +
		"  [\n" +
		"    <!ELEMENT test (a)> \n" +
		"  ]\n" +
		">";
		scanner = XMLScanner.createScanner(xml);
		assertOffsetAndToken(0, TokenType.DTDStartDoctypeTag);
		assertOffsetAndToken(9, TokenType.Whitespace);
		assertOffsetAndToken(10, TokenType.DTDDoctypeName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDDocTypeKindSYSTEM);
		assertOffsetAndToken(21, TokenType.Whitespace);
		assertOffsetAndToken(24, TokenType.DTDDoctypeSystemId);
		assertOffsetAndToken(81, TokenType.Whitespace);
		assertOffsetAndToken(84, TokenType.DTDStartInternalSubset);
		assertOffsetAndToken(85, TokenType.Whitespace);
		assertOffsetAndToken(90, TokenType.DTDStartElementDecl);
		assertOffsetAndToken(99, TokenType.Whitespace);
		assertOffsetAndToken(100, TokenType.DTDElementDeclName);
		assertOffsetAndToken(104, TokenType.Whitespace);
		assertOffsetAndToken(105, TokenType.DTDStartElementContent);
		assertOffsetAndToken(106, TokenType.DTDElementContent);
		assertOffsetAndToken(107, TokenType.DTDEndElementContent);
		assertOffsetAndToken(108, TokenType.DTDEndTag);
		assertOffsetAndToken(109, TokenType.Whitespace);
		assertOffsetAndToken(113, TokenType.DTDEndInternalSubset);
		assertOffsetAndToken(114, TokenType.Whitespace);
		assertOffsetAndToken(115, TokenType.DTDEndDoctypeTag);
		assertOffsetAndToken(116, TokenType.EOS);
	}

	@Test
	public void testDocumentTypeSystemAndInternalAttlist() {
		
		String xml = 
		"<!DOCTYPE html SYSTEM\n" +
		"  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"\n" +
		"  [\n" +
		"    <!ATTLIST payment type CDATA \"cheque\"> \n" +
		"  ]\n" +
		">";
		scanner = XMLScanner.createScanner(xml);
		assertOffsetAndToken(0, TokenType.DTDStartDoctypeTag);
		assertOffsetAndToken(9, TokenType.Whitespace);
		assertOffsetAndToken(10, TokenType.DTDDoctypeName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDDocTypeKindSYSTEM);
		assertOffsetAndToken(21, TokenType.Whitespace);
		assertOffsetAndToken(24, TokenType.DTDDoctypeSystemId);
		assertOffsetAndToken(81, TokenType.Whitespace);
		assertOffsetAndToken(84, TokenType.DTDStartInternalSubset);
		assertOffsetAndToken(85, TokenType.Whitespace);
		assertOffsetAndToken(90, TokenType.DTDStartAttlistDecl);
		assertOffsetAndToken(99, TokenType.Whitespace);
		assertOffsetAndToken(100, TokenType.DTDAttlistAttributeName);
		assertOffsetAndToken(107, TokenType.Whitespace);
		assertOffsetAndToken(108, TokenType.DTDAttlistAttributeName);
		assertOffsetAndToken(112, TokenType.Whitespace);
		assertOffsetAndToken(113, TokenType.DTDAttlistType);
		assertOffsetAndToken(118, TokenType.Whitespace);
		assertOffsetAndToken(119, TokenType.DTDAttlistAttributeValue);
		assertOffsetAndToken(127, TokenType.DTDEndTag);
		assertOffsetAndToken(128, TokenType.Whitespace);
		assertOffsetAndToken(132, TokenType.DTDEndInternalSubset);
		assertOffsetAndToken(133, TokenType.Whitespace);
		assertOffsetAndToken(134, TokenType.DTDEndDoctypeTag);
		assertOffsetAndToken(135, TokenType.EOS);
	}

	@Test
	public void testDocumentTypeSystemAndInternalAttlist2() {
		
		String xml = 
		"<!DOCTYPE html SYSTEM\n" +
		"  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"\n" +
		"  [\n" +
		"    <!ATTLIST payment type (first|second) \"first\"> \n" +
		"  ]\n" +
		">";
		scanner = XMLScanner.createScanner(xml);
		assertOffsetAndToken(0, TokenType.DTDStartDoctypeTag);
		assertOffsetAndToken(9, TokenType.Whitespace);
		assertOffsetAndToken(10, TokenType.DTDDoctypeName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDDocTypeKindSYSTEM);
		assertOffsetAndToken(21, TokenType.Whitespace);
		assertOffsetAndToken(24, TokenType.DTDDoctypeSystemId);
		assertOffsetAndToken(81, TokenType.Whitespace);
		assertOffsetAndToken(84, TokenType.DTDStartInternalSubset);
		assertOffsetAndToken(85, TokenType.Whitespace);
		assertOffsetAndToken(90, TokenType.DTDStartAttlistDecl);
		assertOffsetAndToken(99, TokenType.Whitespace);
		assertOffsetAndToken(100, TokenType.DTDAttlistAttributeName);
		assertOffsetAndToken(107, TokenType.Whitespace);
		assertOffsetAndToken(108, TokenType.DTDAttlistAttributeName);
		assertOffsetAndToken(112, TokenType.Whitespace);
		assertOffsetAndToken(113, TokenType.DTDAttlistType);
		assertOffsetAndToken(127, TokenType.Whitespace);
		assertOffsetAndToken(128, TokenType.DTDAttlistAttributeValue);
		assertOffsetAndToken(135, TokenType.DTDEndTag);
		assertOffsetAndToken(136, TokenType.Whitespace);
		assertOffsetAndToken(140, TokenType.DTDEndInternalSubset);
		assertOffsetAndToken(141, TokenType.Whitespace);
		assertOffsetAndToken(142, TokenType.DTDEndDoctypeTag);
		assertOffsetAndToken(143, TokenType.EOS);
	}

	@Test
	public void testDocumentTypeSystemAndEmptyInternalDTD() {
		
		String xml = 
		"<!DOCTYPE html SYSTEM\n" +
		"  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"\n" +
		"  [\n" +
		"  ]\n" +
		">";
		scanner = XMLScanner.createScanner(xml);
		assertOffsetAndToken(0, TokenType.DTDStartDoctypeTag);
		assertOffsetAndToken(9, TokenType.Whitespace);
		assertOffsetAndToken(10, TokenType.DTDDoctypeName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDDocTypeKindSYSTEM);
		assertOffsetAndToken(21, TokenType.Whitespace);
		assertOffsetAndToken(24, TokenType.DTDDoctypeSystemId);
		assertOffsetAndToken(81, TokenType.Whitespace);
		assertOffsetAndToken(84, TokenType.DTDStartInternalSubset);
		assertOffsetAndToken(85, TokenType.Whitespace);
		assertOffsetAndToken(88, TokenType.DTDEndInternalSubset);
		assertOffsetAndToken(89, TokenType.Whitespace);
		assertOffsetAndToken(90, TokenType.DTDEndDoctypeTag);
		assertOffsetAndToken(91, TokenType.EOS);
	}

	@Test
	public void testDocumentTypeSystem() {
		
		String xml = 
		"<!DOCTYPE html SYSTEM\n" +
		"  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"\n" +
		">";
		scanner = XMLScanner.createScanner(xml);
		assertOffsetAndToken(0, TokenType.DTDStartDoctypeTag);
		assertOffsetAndToken(9, TokenType.Whitespace);
		assertOffsetAndToken(10, TokenType.DTDDoctypeName);
		assertOffsetAndToken(14, TokenType.Whitespace);
		assertOffsetAndToken(15, TokenType.DTDDocTypeKindSYSTEM);
		assertOffsetAndToken(21, TokenType.Whitespace);
		assertOffsetAndToken(24, TokenType.DTDDoctypeSystemId);
		assertOffsetAndToken(81, TokenType.Whitespace);
		assertOffsetAndToken(82, TokenType.DTDEndDoctypeTag);
	}
	
	public void assertOffsetAndToken(int tokenOffset, TokenType tokenType) {
		TokenType token = scanner.scan();
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
