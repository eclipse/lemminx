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

import static org.eclipse.lemminx.XMLAssert.assertHover;
import static org.eclipse.lemminx.XMLAssert.r;

import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.util.URI.MalformedURIException;
import org.eclipse.lemminx.commons.BadLocationException;
import org.junit.jupiter.api.Test;

/**
 * Test for entities hover used in a text node.
 *
 */
public class EntitiesHoverExtensionsTest {

	// Test for local entities

	@Test
	public void local() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE root [\r\n" + //
				"  <!ENTITY mdash \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root>\r\n" + //
				"  &mdas|h;\r\n" + // <- here definition for mdash
				"</root>";
		assertHover(xml, "src/test/resources/test.xml", "**Entity mdash**" + //
				System.lineSeparator() + //
				" * Value: `&#x2014;`" + //
				System.lineSeparator() + //
				" * Type: `Local`" + //
				System.lineSeparator() + //
				" * Source: [test.xml](src/test/resources/test.xml)", //
				r(5, 2, 5, 9));
	}

	@Test
	public void localWithSYSTEM() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" standalone=\"no\" ?>\r\n" + //
				"	<!DOCTYPE copyright [\r\n" + //
				"	  <!ELEMENT copyright (#PCDATA)>\r\n" + //
				"	  <!ENTITY c SYSTEM \"http://www.xmlwriter.net/copyright.xml\">\r\n" + //
				"	]>\r\n" + //
				"	<copyright>&|c;</copyright>";
		assertHover(xml, "src/test/resources/test.xml", "**Entity c**" + //
				System.lineSeparator() + //
				" * Type: `Local`" + //
				System.lineSeparator() + //
				" * System ID: `http://www.xmlwriter.net/copyright.xml`" + //
				System.lineSeparator() + //
				" * Source: [test.xml](src/test/resources/test.xml)", //
				r(5, 12, 5, 15));
	}

	// Test for external entities

	@Test
	public void external() throws Exception {
		String baseDTDURI = getDTDFileURI("base.dtd");
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<!DOCTYPE root-element SYSTEM \"dtd/entities/base.dtd\" [\r\n" + //
				"<!ENTITY mdash \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root-element>\r\n" + //
				"\r\n &f|oo;" + //
				"</root-element>";
		assertHover(xml, "src/test/resources/test.xml", "**Entity foo**" + //
				System.lineSeparator() + //
				" * Value: `bar`" + //
				System.lineSeparator() + //
				" * Type: `External`" + //
				System.lineSeparator() + //
				" * Source: [base.dtd](" + baseDTDURI + ")", //
				r(6, 1, 6, 6));
	}

	@Test
	public void externalWithSYSTEM() throws Exception {
		String baseDTDURI = getDTDFileURI("base-system.dtd");

		// &writer;
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE author SYSTEM \"dtd/entities/base-system.dtd\">\r\n" + //
				"<author>&wr|iter;&copyright;</author>";
		assertHover(xml, "src/test/resources/test.xml", "**Entity writer**" + //
				System.lineSeparator() + //
				" * Type: `External`" + //
				System.lineSeparator() + //
				" * Public ID: `public id`" + //
				System.lineSeparator() + //
				" * System ID: `entity uri`" + //
				System.lineSeparator() + //
				" * Source: [base-system.dtd](" + baseDTDURI + ")", //
				r(2, 8, 2, 16));

		// &copyright;
		xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE author SYSTEM \"dtd/entities/base-system.dtd\">\r\n" + //
				"<author>&writer;&cop|yright;</author>";
		assertHover(xml, "src/test/resources/test.xml", "**Entity copyright**" + //
				System.lineSeparator() + //
				" * Type: `External`" + //
				System.lineSeparator() + //
				" * System ID: `uri`" + //
				System.lineSeparator() + //
				" * Source: [base-system.dtd](" + baseDTDURI + ")", //
				r(2, 16, 2, 27));
	}

	// Test for predefined entities

	@Test
	public void predefined() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<root>\r\n" + //
				"  &am|p;\r\n" + "</root>";
		assertHover(xml, "**Entity amp**" + //
				System.lineSeparator() + //
				" * Value: `&#38;`" + //
				System.lineSeparator() + //
				" * Type: `Predefined`", //
				r(2, 2, 2, 7));
	}

	private static String getDTDFileURI(String dtdURI) throws MalformedURIException {
		return XMLEntityManager.expandSystemId("dtd/entities/" + dtdURI, "src/test/resources/test.xml", true);
	}
}
