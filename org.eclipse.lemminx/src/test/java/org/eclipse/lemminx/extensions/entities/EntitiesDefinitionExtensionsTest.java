/**
 *  Copyright (c) 2020 Red Hat, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lemminx.extensions.entities;

import static org.eclipse.lemminx.XMLAssert.ll;
import static org.eclipse.lemminx.XMLAssert.r;
import static org.eclipse.lemminx.XMLAssert.testDefinitionFor;

import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.util.URI.MalformedURIException;
import org.eclipse.lemminx.commons.BadLocationException;
import org.junit.jupiter.api.Test;

/**
 * Test for entities definition used in a text node.
 *
 */
public class EntitiesDefinitionExtensionsTest {

	@Test
	public void local() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE root [\r\n" + //
				"  <!ENTITY mdash \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root>\r\n" + //
				"  &mdas|h;\r\n" + // <- here definition for mdash
				"</root>";
		testDefinitionFor(xml, "test.xml", ll("test.xml", r(5, 2, 5, 9), r(2, 11, 2, 16)));
	}

	@Test
	public void beforeAmp() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE root [\r\n" + //
				"  <!ENTITY mdash \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root>\r\n" + //
				"  |&mdash\r\n" + // <- here definition before &mdash
				"</root>";
		testDefinitionFor(xml, "test.xml");
	}

	@Test
	public void unknownEntity() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE root [\r\n" + //
				"  <!ENTITY mdash \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root>\r\n" + //
				"  &BAD_ENT|ITY\r\n" + // <- here definition for BAD_ENTITY
				"</root>";
		testDefinitionFor(xml, "test.xml");
	}

	@Test
	public void insideText() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE root [\r\n" + //
				"  <!ENTITY mdash \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root>\r\n" + //
				"  abcd&mdas|h;efgh\r\n" + // <- here definition for mdash
				"</root>";
		testDefinitionFor(xml, "test.xml", ll("test.xml", r(5, 6, 5, 13), r(2, 11, 2, 16)));
	}

	@Test
	public void underscoreEntityName() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE root [\r\n" + //
				"  <!ENTITY foo_bar \"&#x2014;\">\r\n" + //
				"  <!ENTITY foo_baz \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root>\r\n" + //
				" &foo_b|ar;efgh\r\n" + // <- here definition for foo_bar
				"</root>";
		testDefinitionFor(xml, "test.xml", ll("test.xml", r(6, 1, 6, 10), r(2, 11, 2, 18)));
	}

	// Test for external entities

	@Test
	public void externalWithIndent() throws BadLocationException, MalformedURIException {
		String dtdFileURI = getDTDFileURI("src/test/resources/dtd/base.dtd");
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<!DOCTYPE root-element SYSTEM \"src/test/resources/dtd/base.dtd\" [\r\n" + //
				"	<!ENTITY mdash \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root-element>\r\n" + //
				"\r\n &f|oo;" + //
				"</root-element>";
		testDefinitionFor(xml, "test.xml", ll(dtdFileURI, r(6, 1, 6, 6), r(2, 9, 2, 12)));
	}

	private static String getDTDFileURI(String dtdURI) throws MalformedURIException {
		return XMLEntityManager.expandSystemId(dtdURI, "test.xml", true).replace("///", "/");
	}
}
