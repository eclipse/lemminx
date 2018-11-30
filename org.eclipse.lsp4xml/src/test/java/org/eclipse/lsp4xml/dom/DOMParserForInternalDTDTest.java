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
package org.eclipse.lsp4xml.dom;

import org.junit.Assert;
import org.junit.Ignore;
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
		Assert.assertEquals(1, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(22, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertTrue(documentType.isClosed()); // here close comes from the '>' of <foo />

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

	@Ignore
	@Test
	public void attListDeclWithNameNotClosed() {
		String xml = "<!DOCTYPE foo [<!ATTLIST a]><foo/>";

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
		Assert.assertEquals(25, attListDecl.getEnd());
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

	private static DOMDocument createDOMDocument(String xml) {
		return DOMParser.getInstance().parse(xml, "uri", null);
	}
}
