/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.dom;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

/**
 * DOMParsre test with internal DTD.
 *
 */
public class DOMParserForInternalDTDTest {

	@Test
	public void onlyDocTypeNotClosed() {
		String xml = "<!DOCTYPE ";

		DOMDocument actual = createDOMDocument(xml);
		Assert.assertEquals(1, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(10, documentType.getEnd());
		Assert.assertNull(documentType.getName());
		Assert.assertFalse(documentType.isClosed());
		Assert.assertEquals(0, documentType.getChildren().size());
	}

	@Test
	public void docTypeWithNameNotClosed() {
		String xml = "<!DOCTYPE foo";

		DOMDocument actual = createDOMDocument(xml);
		Assert.assertEquals(1, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(13, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertFalse(documentType.isClosed());
		Assert.assertEquals(0, documentType.getChildren().size());
	}

	@Test
	public void docTypeWithStartInternalSubsetNotClosed() {
		String xml = "<!DOCTYPE foo [";

		DOMDocument actual = createDOMDocument(xml);
		Assert.assertEquals(1, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(15, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertFalse(documentType.isClosed());
		Assert.assertEquals(0, documentType.getChildren().size());
	}

	@Test
	public void docTypeWithStartInternalSubsetTextNotClosed() {
		String xml = "<!DOCTYPE foo [aaa";

		DOMDocument actual = createDOMDocument(xml);
		Assert.assertEquals(1, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(18, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertFalse(documentType.isClosed());

		Assert.assertEquals(1, documentType.getChildren().size());
		Assert.assertTrue(documentType.getChild(0).isText());
		DOMText text = (DOMText) documentType.getChild(0);
		Assert.assertEquals("aaa", text.getTextContent());
	}

	@Test
	public void docTypeNotClosedAndElement() {
		String xml = "<!DOCTYPE foo []<foo/>";

		DOMDocument actual = createDOMDocument(xml);
		Assert.assertEquals(2, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(16, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(actual.getChild(1).isClosed()); // here close comes from the '>' of <foo />
	}

	@Test
	public void docTypeClosedEmptySubsetContentAndElement() {
		String xml = "<!DOCTYPE foo []><foo/>";

		DOMDocument actual = createDOMDocument(xml);
		Assert.assertEquals(2, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(17, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <foo/> element
		Assert.assertTrue(actual.getChild(1).isElement());

	}

	@Test
	public void docTypeClosedNoSubsetContentAndElement() {
		String xml = "<!DOCTYPE foo ><foo/>";

		DOMDocument actual = createDOMDocument(xml);
		Assert.assertEquals(2, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(15, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <foo/> element
		Assert.assertTrue(actual.getChild(1).isElement());

	}

	@Test
	public void docTypeClosedWithBadContentAndElement() {
		String xml = "<!DOCTYPE foo [aaaa]><foo/>";

		DOMDocument actual = createDOMDocument(xml);
		Assert.assertEquals(2, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(21, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <foo />element
		Assert.assertTrue(actual.getChild(1).isElement());

	}

	@Test
	public void elementDeclNotClosed() {
		String xml = "<!DOCTYPE foo [<!ELEMENT]><foo/>";

		DOMDocument actual = createDOMDocument(xml);
		Assert.assertEquals(2, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(26, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <!ELEMENT
		Assert.assertEquals(1, documentType.getChildren().size());
		Assert.assertTrue(documentType.getChild(0).isDTDElementDecl());
		DTDElementDecl elementDecl = (DTDElementDecl) documentType.getChild(0);
		Assert.assertEquals(15, elementDecl.getStart());
		Assert.assertEquals(23, elementDecl.getEnd());
		Assert.assertFalse(elementDecl.isClosed());

		// <foo />element
		Assert.assertTrue(actual.getChild(1).isElement());
	}

	@Test
	public void elementDeclWithNameNotClosed() {
		String xml = "<!DOCTYPE foo [<!ELEMENT a]><foo/>";

		DOMDocument actual = createDOMDocument(xml);
		Assert.assertEquals(2, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(28, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <!ELEMENT
		Assert.assertEquals(1, documentType.getChildren().size());
		Assert.assertTrue(documentType.getChild(0).isDTDElementDecl());
		DTDElementDecl elementDecl = (DTDElementDecl) documentType.getChild(0);
		Assert.assertEquals(15, elementDecl.getStart());
		Assert.assertEquals(25, elementDecl.getEnd());
		Assert.assertFalse(elementDecl.isClosed());

		// <foo />element
		Assert.assertTrue(actual.getChild(1).isElement());
	}

	@Test
	public void elementDeclClosed() {
		String xml = "<!DOCTYPE foo [<!ELEMENT >]><foo/>";

		DOMDocument actual = createDOMDocument(xml);
		Assert.assertEquals(2, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(28, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <!ELEMENT
		Assert.assertEquals(1, documentType.getChildren().size());
		Assert.assertTrue(documentType.getChild(0).isDTDElementDecl());
		DTDElementDecl elementDecl = (DTDElementDecl) documentType.getChild(0);
		Assert.assertEquals(15, elementDecl.getStart());
		Assert.assertEquals(26, elementDecl.getEnd());
		Assert.assertTrue(elementDecl.isClosed());

		// <foo />element
		Assert.assertTrue(actual.getChild(1).isElement());
	}

	@Test
	public void elementDeclWithNameClosed() {
		String xml = "<!DOCTYPE foo [<!ELEMENT a >]><foo/>";

		DOMDocument actual = createDOMDocument(xml);
		Assert.assertEquals(2, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(30, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <!ELEMENT
		Assert.assertEquals(1, documentType.getChildren().size());
		Assert.assertTrue(documentType.getChild(0).isDTDElementDecl());
		DTDElementDecl elementDecl = (DTDElementDecl) documentType.getChild(0);
		Assert.assertEquals(15, elementDecl.getStart());
		Assert.assertEquals(28, elementDecl.getEnd());
		Assert.assertTrue(elementDecl.isClosed());

		// <foo />element
		Assert.assertTrue(actual.getChild(1).isElement());
	}

	@Test
	public void elementDeclClosedWithBraces() {
		String xml = "<!DOCTYPE error-page [<!ELEMENT error-page ((error-code | exception-type), location)>]><error-page/>";

		DOMDocument actual = createDOMDocument(xml);
		Assert.assertEquals(2, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(87, documentType.getEnd());
		Assert.assertEquals("error-page", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <!ELEMENT
		Assert.assertEquals(1, documentType.getChildren().size());
		Assert.assertTrue(documentType.getChild(0).isDTDElementDecl());
		DTDElementDecl elementDecl = (DTDElementDecl) documentType.getChild(0);
		Assert.assertEquals(22, elementDecl.getStart());
		Assert.assertEquals(85, elementDecl.getEnd());
		Assert.assertTrue(elementDecl.isClosed());
		Assert.assertEquals("error-page", elementDecl.getName());
		Assert.assertEquals("((error-code | exception-type), location)", elementDecl.getContent());

		// <error-page />element
		Assert.assertTrue(actual.getChild(1).isElement());
	}
	
	@Test
	public void elementDeclNotClosedWithBraces() {
		String xml = "<!DOCTYPE error-page [<!ELEMENT error-page ((error-code | exception-type), location)]><error-page/>";

		DOMDocument actual = createDOMDocument(xml);
		Assert.assertEquals(2, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(86, documentType.getEnd());
		Assert.assertEquals("error-page", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <!ELEMENT
		Assert.assertEquals(1, documentType.getChildren().size());
		Assert.assertTrue(documentType.getChild(0).isDTDElementDecl());
		DTDElementDecl elementDecl = (DTDElementDecl) documentType.getChild(0);
		Assert.assertEquals(22, elementDecl.getStart());
		Assert.assertEquals(83, elementDecl.getEnd());
		Assert.assertEquals("error-page", elementDecl.getName());
		Assert.assertEquals("((error-code | exception-type), location)", elementDecl.getContent());
		Assert.assertFalse(elementDecl.isClosed());

		// <error-page />element
		Assert.assertTrue(actual.getChild(1).isElement());
	}
	
	@Test
	public void attListDeclNotClosed() {
		String xml = "<!DOCTYPE foo [<!ATTLIST]><foo/>";

		DOMDocument actual = createDOMDocument(xml);
		Assert.assertEquals(2, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(26, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <!ATTLIST
		Assert.assertEquals(1, documentType.getChildren().size());
		Assert.assertTrue(documentType.getChild(0).isDTDAttListDecl());
		DTDAttlistDecl attListDecl = (DTDAttlistDecl) documentType.getChild(0);
		Assert.assertEquals(15, attListDecl.getStart());
		Assert.assertEquals(23, attListDecl.getEnd());
		Assert.assertFalse(attListDecl.isClosed());

		// <foo />element
		Assert.assertTrue(actual.getChild(1).isElement());
	}

	@Test
	public void attListDeclClosed() {
		String xml = "<!DOCTYPE foo [<!ATTLIST >]><foo/>";

		DOMDocument actual = createDOMDocument(xml);
		Assert.assertEquals(2, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(28, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <!ATTLIST
		Assert.assertEquals(1, documentType.getChildren().size());
		Assert.assertTrue(documentType.getChild(0).isDTDAttListDecl());
		DTDAttlistDecl attListDecl = (DTDAttlistDecl) documentType.getChild(0);
		Assert.assertEquals(15, attListDecl.getStart());
		Assert.assertEquals(26, attListDecl.getEnd());
		Assert.assertTrue(attListDecl.isClosed());

		// <foo />element
		Assert.assertTrue(actual.getChild(1).isElement());
	}

	@Test
	public void attListDeclWithNameClosed() {
		String xml = "<!DOCTYPE foo [<!ATTLIST a >]><foo/>";

		DOMDocument actual = createDOMDocument(xml);
		Assert.assertEquals(2, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(30, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <!ATTLIST
		Assert.assertEquals(1, documentType.getChildren().size());
		Assert.assertTrue(documentType.getChild(0).isDTDAttListDecl());
		DTDAttlistDecl attListDecl = (DTDAttlistDecl) documentType.getChild(0);
		Assert.assertEquals(15, attListDecl.getStart());
		Assert.assertEquals(28, attListDecl.getEnd());
		Assert.assertTrue(attListDecl.isClosed());

		// <foo />element
		Assert.assertTrue(actual.getChild(1).isElement());
	}

	@Test
	public void entityDeclNotClosed() {
		String xml = "<!DOCTYPE foo [<!ENTITY]><foo/>";

		DOMDocument actual = createDOMDocument(xml);
		Assert.assertEquals(2, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(25, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <!ENTITY
		Assert.assertEquals(1, documentType.getChildren().size());
		Assert.assertTrue(documentType.getChild(0).isDTDEntityDecl());
		DTDEntityDecl entityDecl = (DTDEntityDecl) documentType.getChild(0);
		Assert.assertEquals(15, entityDecl.getStart());
		Assert.assertEquals(22, entityDecl.getEnd());
		Assert.assertFalse(entityDecl.isClosed());
		Assert.assertNull(entityDecl.getNodeName());

		// <foo />element
		Assert.assertTrue(actual.getChild(1).isElement());
	}

	@Test
	public void entityDeclWithNameNotClosed() {
		String xml = "<!DOCTYPE foo [<!ENTITY a]><foo/>";

		DOMDocument actual = createDOMDocument(xml);
		Assert.assertEquals(2, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(27, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <!ENTITY
		Assert.assertEquals(1, documentType.getChildren().size());
		Assert.assertTrue(documentType.getChild(0).isDTDEntityDecl());
		DTDEntityDecl entityDecl = (DTDEntityDecl) documentType.getChild(0);
		Assert.assertEquals(15, entityDecl.getStart());
		Assert.assertEquals(24, entityDecl.getEnd());
		Assert.assertFalse(entityDecl.isClosed());
		Assert.assertEquals("a", entityDecl.getNodeName());

		// <foo />element
		Assert.assertTrue(actual.getChild(1).isElement());
	}

	@Test
	public void entityDeclClosed() {
		String xml = "<!DOCTYPE foo [<!ENTITY >]><foo/>";

		DOMDocument actual = createDOMDocument(xml);
		Assert.assertEquals(2, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(27, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <!ENTITY
		Assert.assertEquals(1, documentType.getChildren().size());
		Assert.assertTrue(documentType.getChild(0).isDTDEntityDecl());
		DTDEntityDecl entityDecl = (DTDEntityDecl) documentType.getChild(0);
		Assert.assertEquals(15, entityDecl.getStart());
		Assert.assertEquals(25, entityDecl.getEnd());
		Assert.assertTrue(entityDecl.isClosed());
		Assert.assertNull(entityDecl.getNodeName());

		// <foo />element
		Assert.assertTrue(actual.getChild(1).isElement());
	}

	@Test
	public void entityDeclWithNameClosed() {
		String xml = "<!DOCTYPE foo [<!ENTITY a >]><foo/>";

		DOMDocument actual = createDOMDocument(xml);
		Assert.assertEquals(2, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(29, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <!ENTITY
		Assert.assertEquals(1, documentType.getChildren().size());
		Assert.assertTrue(documentType.getChild(0).isDTDEntityDecl());
		DTDEntityDecl entityDecl = (DTDEntityDecl) documentType.getChild(0);
		Assert.assertEquals(15, entityDecl.getStart());
		Assert.assertEquals(27, entityDecl.getEnd());
		Assert.assertTrue(entityDecl.isClosed());
		Assert.assertEquals("a", entityDecl.getNodeName());

		// <foo />element
		Assert.assertTrue(actual.getChild(1).isElement());
	}

	@Test
	public void entityDeclWithNameClosedAndParameters() {
		String xml = "<!DOCTYPE foo [<!ENTITY % eName PUBLIC \"publicId\" \"systemId\" >]><foo/>";

		DOMDocument actual = createDOMDocument(xml);
		Assert.assertEquals(2, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(64, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <!ENTITY
		Assert.assertEquals(1, documentType.getChildren().size());
		Assert.assertTrue(documentType.getChild(0).isDTDEntityDecl());
		DTDEntityDecl entityDecl = (DTDEntityDecl) documentType.getChild(0);
		Assert.assertEquals(15, entityDecl.getStart());
		Assert.assertEquals(62, entityDecl.getEnd());
		Assert.assertTrue(entityDecl.isClosed());
		Assert.assertEquals("%", entityDecl.getPercent());
		Assert.assertEquals("eName", entityDecl.getNodeName());
		Assert.assertEquals("PUBLIC", entityDecl.getKind());
		Assert.assertEquals("\"publicId\"", entityDecl.getPublicId());
		Assert.assertEquals("\"systemId\"", entityDecl.getSystemId());

		// <foo />element
		Assert.assertTrue(actual.getChild(1).isElement());
	}

	@Test
	public void entityDeclWithNameClosedAndSomeParameters() {
		String xml = "<!DOCTYPE foo [<!ENTITY % eName PUBLIC \"publicId\" >]><foo/>";

		DOMDocument actual = createDOMDocument(xml);
		Assert.assertEquals(2, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(53, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <!ENTITY
		Assert.assertEquals(1, documentType.getChildren().size());
		Assert.assertTrue(documentType.getChild(0).isDTDEntityDecl());
		DTDEntityDecl entityDecl = (DTDEntityDecl) documentType.getChild(0);
		Assert.assertEquals(15, entityDecl.getStart());
		Assert.assertEquals(51, entityDecl.getEnd());
		Assert.assertTrue(entityDecl.isClosed());
		Assert.assertEquals("%", entityDecl.getPercent());
		Assert.assertEquals("eName", entityDecl.getNodeName());
		Assert.assertEquals("PUBLIC", entityDecl.getKind());
		Assert.assertEquals("\"publicId\"", entityDecl.getPublicId());
		Assert.assertEquals(null, entityDecl.getSystemId());

		// <foo />element
		Assert.assertTrue(actual.getChild(1).isElement());
	}

	
	@Test
	public void attlistWithMultipleInternalDeclarations() {
		
		String dtd = 
		"<!DOCTYPE foo [<!ATTLIST Institution\n" +
		"    to CDATA #REQUIRED\n" +
		"    from CDATA #REQUIRED>]\n" +
		">";

		DOMDocument actual = createDOMDocument(dtd);
		Assert.assertEquals(1, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(88, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <!ATTLIST
		Assert.assertEquals(1, documentType.getChildren().size());
		Assert.assertTrue(documentType.getChild(0).isDTDAttListDecl());
		DTDAttlistDecl attlistDecl = (DTDAttlistDecl) documentType.getChild(0);
		
		Assert.assertEquals(15, attlistDecl.getStart());
		Assert.assertEquals(85, attlistDecl.getEnd());
		Assert.assertEquals("Institution", attlistDecl.getElementName());
		Assert.assertEquals("to", attlistDecl.getAttributeName());
		Assert.assertEquals("CDATA", attlistDecl.getAttributeType());
		Assert.assertEquals("#REQUIRED", attlistDecl.getAttributeValue());

		Assert.assertNotNull(attlistDecl.getInternalChildren());
		Assert.assertEquals(1, attlistDecl.getInternalChildren().size());

		DTDAttlistDecl internalDecl = (DTDAttlistDecl) attlistDecl.getInternalChildren().get(0);

		Assert.assertEquals("from", internalDecl.getAttributeName());
		Assert.assertEquals("CDATA", internalDecl.getAttributeType());
		Assert.assertEquals("#REQUIRED", internalDecl.getAttributeValue());

	}

	@Test
	public void attlistWithMultipleInternalDeclarationsMissingInformation() {
		
		String dtd = 
		"<!DOCTYPE foo [<!ATTLIST Institution\n" +
		"    to CDATA \n" +
		"    from CDATA #REQUIRED>]\n" +
		">";

		DOMDocument actual = createDOMDocument(dtd);
		Assert.assertEquals(1, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(79, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <!ATTLIST
		Assert.assertEquals(1, documentType.getChildren().size());
		Assert.assertTrue(documentType.getChild(0).isDTDAttListDecl());
		DTDAttlistDecl attlistDecl = (DTDAttlistDecl) documentType.getChild(0);
		
		Assert.assertEquals(15, attlistDecl.getStart());
		Assert.assertEquals(76, attlistDecl.getEnd());
		Assert.assertEquals("Institution", attlistDecl.getElementName());
		Assert.assertEquals("to", attlistDecl.getAttributeName());
		Assert.assertEquals("CDATA", attlistDecl.getAttributeType());
		Assert.assertEquals(null, attlistDecl.getAttributeValue());

		Assert.assertEquals(55, attlistDecl.unrecognized.start);
		Assert.assertEquals(75, attlistDecl.unrecognized.end);

	}

	@Test
	public void elementWithContent() {
		
		String dtd = 
		"<!DOCTYPE foo [\n" +
		"    <!ELEMENT elName (aa1,bb2,cc3) \n" +
		"    ]\n" +
		">";

		DOMDocument actual = createDOMDocument(dtd);
		Assert.assertEquals(1, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(59, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <!ELEMENT
		Assert.assertEquals(1, documentType.getChildren().size());
		Assert.assertTrue(documentType.getChild(0).isDTDElementDecl());
		DTDElementDecl elementDecl = (DTDElementDecl) documentType.getChild(0);
		
		Assert.assertEquals(20, elementDecl.getStart());
		Assert.assertEquals(55, elementDecl.getEnd());
		Assert.assertEquals("elName", elementDecl.getName());
		Assert.assertEquals("(aa1,bb2,cc3)", elementDecl.getContent());
		Assert.assertEquals(null, elementDecl.getCategory());
		Assert.assertFalse(elementDecl.isClosed());
	}

	@Test
	public void allDeclsUnclosed() {
		
		String dtd = 
		"<!DOCTYPE foo [ <!ELEMENT   <!ATTLIST elName <!ENTITY garbage   <!NOTATION garbage  ]>";

		DOMDocument actual = createDOMDocument(dtd);
		Assert.assertEquals(1, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(86, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <!ELEMENT
		Assert.assertEquals(4, documentType.getChildren().size());
		Assert.assertTrue(documentType.getChild(0).isDTDElementDecl());
		Assert.assertTrue(documentType.getChild(1).isDTDAttListDecl());
		Assert.assertTrue(documentType.getChild(2).isDTDEntityDecl());
		Assert.assertTrue(documentType.getChild(3).isDTDNotationDecl());

		DTDElementDecl elementDecl = (DTDElementDecl) documentType.getChild(0);
		
		Assert.assertEquals(16, elementDecl.getStart());
		Assert.assertEquals(28, elementDecl.getEnd());
		Assert.assertEquals(null, elementDecl.getName());
		Assert.assertEquals(null, elementDecl.getContent());
		Assert.assertEquals(null, elementDecl.getCategory());
		Assert.assertFalse(elementDecl.isClosed());

		DTDAttlistDecl attlistDecl = (DTDAttlistDecl) documentType.getChild(1);
		
		Assert.assertEquals(28, attlistDecl.getStart());
		Assert.assertEquals(45, attlistDecl.getEnd());
		Assert.assertEquals("elName", attlistDecl.getElementName());
		Assert.assertFalse(attlistDecl.isClosed());

		DTDEntityDecl entityDecl = (DTDEntityDecl) documentType.getChild(2);
		
		Assert.assertEquals(45, entityDecl.getStart());
		Assert.assertEquals(64, entityDecl.getEnd());
		Assert.assertEquals("garbage", entityDecl.getNodeName());
		Assert.assertFalse(entityDecl.isClosed());

		DTDNotationDecl notationDecl = (DTDNotationDecl) documentType.getChild(3);
		
		Assert.assertEquals(64, notationDecl.getStart());
		Assert.assertEquals(83, notationDecl.getEnd());
		Assert.assertEquals("garbage", notationDecl.getName());
		Assert.assertFalse(notationDecl.isClosed());
	}

	@Test
	public void dtdUnrecognizedContent() {
		
		String dtd = 
		"<!DOCTYPE foo [\n" +
		"    <!ELEMENT elName (aa1,bb2,cc3) BAD UNRECOGNIZED CONTENT> \n" +
		"    ]\n" +
		">";

		DOMDocument actual = createDOMDocument(dtd);
		Assert.assertEquals(1, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(85, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <!ELEMENT
		Assert.assertEquals(1, documentType.getChildren().size());
		Assert.assertTrue(documentType.getChild(0).isDTDElementDecl());
		DTDElementDecl elementDecl = (DTDElementDecl) documentType.getChild(0);
		
		Assert.assertEquals(20, elementDecl.getStart());
		Assert.assertEquals(76, elementDecl.getEnd());
		Assert.assertEquals("elName", elementDecl.getName());
		Assert.assertEquals("(aa1,bb2,cc3)", elementDecl.getContent());
		Assert.assertEquals(null, elementDecl.getCategory());
		Assert.assertEquals(51, elementDecl.unrecognized.start);
		Assert.assertEquals(75, elementDecl.unrecognized.end);
		Assert.assertTrue(elementDecl.isClosed());
	}

	@Test
	public void dtdNotationComplete() {
		
		String dtd = 
		"<!DOCTYPE foo [\n" +
		"    <!NOTATION Name PUBLIC \"PublicID\" \"SystemID\"> \n" +
		"    ]\n" +
		">";

		DOMDocument actual = createDOMDocument(dtd);
		Assert.assertEquals(1, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(74, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <!NOTATION
		Assert.assertEquals(1, documentType.getChildren().size());
		Assert.assertTrue(documentType.getChild(0).isDTDNotationDecl());
		DTDNotationDecl elementDecl = (DTDNotationDecl) documentType.getChild(0);
		
		Assert.assertEquals(20, elementDecl.getStart());
		Assert.assertEquals(65, elementDecl.getEnd());
		Assert.assertEquals("Name", elementDecl.getName());
		Assert.assertEquals("PUBLIC", elementDecl.getKind());
		Assert.assertEquals("\"PublicID\"", elementDecl.getPublicId());
		Assert.assertEquals("\"SystemID\"", elementDecl.getSystemId());
		
		Assert.assertTrue(elementDecl.isClosed());
	}

	@Test
	public void dtdNotationSYSTEMUnrecognizedParameter() {
		
		String dtd = 
		"<!DOCTYPE foo [\n" +
		"    <!NOTATION Name SYSTEM \"PublicID\" \"SystemID\"> \n" +
		"    ]\n" +
		">";

		DOMDocument actual = createDOMDocument(dtd);
		Assert.assertEquals(1, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(74, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(documentType.isClosed());

		// <!NOTATION
		Assert.assertEquals(1, documentType.getChildren().size());
		Assert.assertTrue(documentType.getChild(0).isDTDNotationDecl());
		DTDNotationDecl elementDecl = (DTDNotationDecl) documentType.getChild(0);
		
		Assert.assertEquals(20, elementDecl.getStart());
		Assert.assertEquals(65, elementDecl.getEnd());
		Assert.assertEquals("Name", elementDecl.getName());
		Assert.assertEquals("SYSTEM", elementDecl.getKind());
		Assert.assertEquals("\"PublicID\"", elementDecl.getSystemId());
		Assert.assertEquals("\"SystemID\"", elementDecl.getUnrecognized());
		
		Assert.assertTrue(elementDecl.isClosed());
	}

	@Test
	public void testDoctypeUnrecognizedContent() {
		//Ensure unrecognized content goes all the way till the end
		String dtd = 
		"<!DOCTYPE foo BAD_VALUE [\n" +
		"    <!NOTATION Name SYSTEM \"PublicID\" \"SystemID\"> \n" +
		"    ]\n" +
		">";

		DOMDocument actual = createDOMDocument(dtd);
		Assert.assertEquals(1, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(84, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertEquals(14, documentType.unrecognized.start);
		Assert.assertEquals(82, documentType.unrecognized.end);
		Assert.assertTrue(documentType.isClosed());	
	}

	@Test
	public void testAttlistUncompletedInternalDecl() {
		//Ensure unrecognized content goes all the way till the end
		String dtd = 
		"<!DOCTYPE foo [\n" +
		"    <!ATTLIST payment type CDATA \"check\" BAD \n" +
		"    <!NOTATION Name SYSTEM \"PublicID\" \"SystemID\"> \n" +
		"    ]\n" +
		">";

		DOMDocument actual = createDOMDocument(dtd);
		Assert.assertEquals(1, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(2, documentType.getChildren().size());
		assertTrue(documentType.getChild(0) instanceof DTDAttlistDecl);
		assertTrue(documentType.getChild(1) instanceof DTDNotationDecl);
	}


	private static DOMDocument createDOMDocument(String xml) {
		return DOMParser.getInstance().parse(xml, "uri", null);
	}
}
