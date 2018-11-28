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
		Assert.assertEquals(1, actual.getChildren().size());
		Assert.assertTrue(actual.getChild(0).isDoctype());
		DOMDocumentType documentType = (DOMDocumentType) actual.getChild(0);
		Assert.assertEquals(0, documentType.getStart());
		Assert.assertEquals(32, documentType.getEnd());
		Assert.assertEquals("foo", documentType.getName());
		Assert.assertFalse(documentType.isClosed());

		// <!ELEMENT
		Assert.assertEquals(1, documentType.getChildren().size());
		Assert.assertTrue(documentType.getChild(0).isDTDElementDecl());
		DTDElementDecl elementDecl = (DTDElementDecl) documentType.getChild(0);
		Assert.assertEquals(15, elementDecl.getStart());
		Assert.assertEquals(32, elementDecl.getEnd());
		Assert.assertFalse(elementDecl.isClosed());
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
	
	private static DOMDocument createDOMDocument(String xml) {
		return DOMParser.getInstance().parse(xml, "uri", null);
	}
}
