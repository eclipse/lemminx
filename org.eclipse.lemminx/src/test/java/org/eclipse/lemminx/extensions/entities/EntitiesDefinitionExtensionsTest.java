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
import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.commons.BadLocationException;
import org.junit.jupiter.api.Test;

/**
 * Test for entities definition used in a text node.
 *
 */
public class EntitiesDefinitionExtensionsTest extends AbstractCacheBasedTest {

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
	public void localWithSYSTEM() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" standalone=\"no\" ?>\r\n" + //
				"	<!DOCTYPE copyright [\r\n" + //
				"	  <!ELEMENT copyright (#PCDATA)>\r\n" + //
				"	  <!ENTITY c SYSTEM \"http://www.xmlwriter.net/copyright.xml\">\r\n" + //
				"	]>\r\n" + //
				"	<copyright>&|c;</copyright>";
		testDefinitionFor(xml, "test.xml", ll("test.xml", r(5, 12, 5, 15), r(3, 12, 3, 13)));
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
		String dtdFileURI = getDTDFileURI("src/test/resources/dtd/entities/base.dtd");
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<!DOCTYPE root-element SYSTEM \"src/test/resources/dtd/entities/base.dtd\" [\r\n" + //
				"	<!ENTITY mdash \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root-element>\r\n" + //
				"\r\n &f|oo;" + //
				"</root-element>";
		testDefinitionFor(xml, "test.xml", ll(dtdFileURI, r(6, 1, 6, 6), r(2, 9, 2, 12)));
	}

	@Test
	public void externalWithDTDIndent() throws BadLocationException, MalformedURIException {
		String dtdFileURI = getDTDFileURI("src/test/resources/dtd/entities/base-indent.dtd");

		// &external3;
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<!DOCTYPE root-element SYSTEM \"src/test/resources/dtd/entities/base-indent.dtd\" [\r\n" + //
				"	<!ENTITY mdash \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root-element>\r\n" + //
				"\r\n &externa|l3;" + //
				"</root-element>";
		testDefinitionFor(xml, "test.xml", ll(dtdFileURI, r(6, 1, 6, 12), r(0, 9, 0, 18)));

		// &external5;
		xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<!DOCTYPE root-element SYSTEM \"src/test/resources/dtd/entities/base-indent.dtd\" [\r\n" + //
				"	<!ENTITY mdash \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root-element>\r\n" + //
				"\r\n &externa|l5;" + //
				"</root-element>";
		testDefinitionFor(xml, "test.xml", ll(dtdFileURI, r(6, 1, 6, 12), r(0, 34, 0, 43)));

		// &external;
		xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<!DOCTYPE root-element SYSTEM \"src/test/resources/dtd/entities/base-indent.dtd\" [\r\n" + //
				"	<!ENTITY mdash \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root-element>\r\n" + //
				"\r\n &externa|l;" + //
				"</root-element>";
		testDefinitionFor(xml, "test.xml", ll(dtdFileURI, r(6, 1, 6, 11), r(3, 15, 3, 23)));

		// &external2;
		xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<!DOCTYPE root-element SYSTEM \"src/test/resources/dtd/entities/base-indent.dtd\" [\r\n" + //
				"	<!ENTITY mdash \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root-element>\r\n" + //
				"\r\n &externa|l2;" + //
				"</root-element>";
		testDefinitionFor(xml, "test.xml", ll(dtdFileURI, r(6, 1, 6, 12), r(4, 9, 4, 18)));
	}

	@Test
	public void externalWithDTDNoIndent() throws BadLocationException, MalformedURIException {
		String dtdFileURI = getDTDFileURI("src/test/resources/dtd/entities/base-no-indent.dtd");

		// &external3;
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<!DOCTYPE root-element SYSTEM \"src/test/resources/dtd/entities/base-no-indent.dtd\" [\r\n" + //
				"	<!ENTITY mdash \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root-element>\r\n" + //
				"\r\n &externa|l3;" + //
				"</root-element>";
		testDefinitionFor(xml, "test.xml", ll(dtdFileURI, r(6, 1, 6, 12), r(0, 23, 0, 32)));

		// &external5;
		xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<!DOCTYPE root-element SYSTEM \"src/test/resources/dtd/entities/base-no-indent.dtd\" [\r\n" + //
				"	<!ENTITY mdash \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root-element>\r\n" + //
				"\r\n &externa|l5;" + //
				"</root-element>";
		testDefinitionFor(xml, "test.xml", ll(dtdFileURI, r(6, 1, 6, 12), r(0, 48, 0, 57)));

		// &external;
		xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<!DOCTYPE root-element SYSTEM \"src/test/resources/dtd/entities/base-no-indent.dtd\" [\r\n" + //
				"	<!ENTITY mdash \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root-element>\r\n" + //
				"\r\n &externa|l;" + //
				"</root-element>";
		testDefinitionFor(xml, "test.xml", ll(dtdFileURI, r(6, 1, 6, 11), r(3, 15, 3, 23)));

		// &external2;
		xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<!DOCTYPE root-element SYSTEM \"src/test/resources/dtd/entities/base-no-indent.dtd\" [\r\n" + //
				"	<!ENTITY mdash \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root-element>\r\n" + //
				"\r\n &externa|l2;" + //
				"</root-element>";
		testDefinitionFor(xml, "test.xml", ll(dtdFileURI, r(6, 1, 6, 12), r(4, 9, 4, 18)));
	}

	@Test
	public void externalWithSYSTEM() throws BadLocationException, MalformedURIException {
		String dtdFileURI = getDTDFileURI("src/test/resources/dtd/entities/base-system.dtd");

		// &writer;
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE author SYSTEM \"src/test/resources/dtd/entities/base-system.dtd\">\r\n" + //
				"<author>&wr|iter;&copyright;</author>";
		testDefinitionFor(xml, "test.xml", ll(dtdFileURI, r(2, 8, 2, 16), r(1, 9, 1, 15)));

		// &copyright;
		xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE author SYSTEM \"src/test/resources/dtd/entities/base-system.dtd\">\r\n" + //
				"<author>&writer;&cop|yright;</author>";
		testDefinitionFor(xml, "test.xml", ll(dtdFileURI, r(2, 16, 2, 27), r(2, 9, 2, 18)));

	}

	@Test
	public void parameterEntity() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE root [\r\n" + //
				"  <!ENTITY % document SYSTEM \"document.ent\">\r\n" + //
				"  %docu|ment;" + // <- here definition for mdash parameter entity
				"]>\r\n" + //
				"<root>\r\n" + //
				"</root>";
		testDefinitionFor(xml, "test.xml", ll("test.xml", r(3, 2, 3, 12), r(2, 13, 2, 21)));
	}

	private static String getDTDFileURI(String dtdURI) throws MalformedURIException {
		return XMLEntityManager.expandSystemId(dtdURI, "test.xml", true).replace("///", "/");
	}
}
