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

import static org.eclipse.lemminx.XMLAssert.c;
import static org.eclipse.lemminx.XMLAssert.r;
import static org.eclipse.lemminx.XMLAssert.testCompletionFor;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.entities.EntitiesDocumentationUtils.PredefinedEntity;
import org.junit.jupiter.api.Test;

/**
 * Test for entities completion used in a text node.
 *
 */
public class EntitiesCompletionExtensionsTest extends AbstractCacheBasedTest {

	// Test for local entities

	@Test
	public void localWithSYSTEM() throws BadLocationException {
		// &|
		String xml = "<?xml version=\"1.0\" standalone=\"no\" ?>\r\n" + //
				"	<!DOCTYPE copyright [\r\n" + //
				"	  <!ELEMENT copyright (#PCDATA)>\r\n" + //
				"	  <!ENTITY c SYSTEM \"http://www.xmlwriter.net/copyright.xml\">\r\n" + //
				"	]>\r\n" + //
				"	<copyright>&|</copyright>";
		testCompletionFor(xml, 1 + //
				2 /* CDATA and Comments */ + //
				PredefinedEntity.values().length /* predefined entities */, //
				c("&c;", "&c;", r(5, 12, 5, 13), "&c;"));
	}

	@Test
	public void afterAmp() throws BadLocationException {
		// &|
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE root [\r\n" + //
				"  <!ENTITY mdash \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root>\r\n" + //
				"  &|\r\n" + // <- here completion shows mdash entity
				"</root>";
		testCompletionFor(xml, 1 + //
				2 /* CDATA and Comments */ + //
				PredefinedEntity.values().length /* predefined entities */,
				c("&mdash;", "&mdash;", r(5, 2, 5, 3), "&mdash;"));

	}

	@Test
	public void afterCharacter() throws BadLocationException {
		// &m|
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE root [\r\n" + //
				"  <!ENTITY mdash \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root>\r\n" + //
				"  &m|\r\n" + // <- here completion shows mdash entity
				"</root>";
		testCompletionFor(xml, 1 + //
				2 /* CDATA and Comments */ + //
				PredefinedEntity.values().length /* predefined entities */,
				c("&mdash;", "&mdash;", r(5, 2, 5, 4), "&mdash;"));

	}

	@Test
	public void inside() throws BadLocationException {
		// &m|dblablabla
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE root [\r\n" + //
				"  <!ENTITY mdash \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root>\r\n" + //
				"  &m|dblablabla\r\n" + // <- here completion shows mdash entity
				"</root>";
		testCompletionFor(xml, 1 + //
				2 /* CDATA and Comments */ + //
				PredefinedEntity.values().length /* predefined entities */,
				c("&mdash;", "&mdash;", r(5, 2, 5, 4), "&mdash;"));
	}

	@Test
	public void underscoreEntityName() throws BadLocationException {
		// &foo_b|
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE root [\r\n" + //
				"  <!ENTITY foo_bar \"&#x2014;\">\r\n" + //
				"  <!ENTITY foo_baz \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root>\r\n" + //
				"  &foo_b|\r\n" + // <- here completion shows mdash entity
				"</root>";
		testCompletionFor(xml, 2 + //
				2 /* CDATA and Comments */ + //
				PredefinedEntity.values().length /* predefined entities */,
				c("&foo_bar;", "&foo_bar;", r(6, 2, 6, 8), "&foo_bar;"), //
				c("&foo_baz;", "&foo_baz;", r(6, 2, 6, 8), "&foo_baz;"));
	}

	@Test
	public void underscoreEntityNameItemDefaults() throws BadLocationException {
		// &foo_b|
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE root [\r\n" + //
				"  <!ENTITY foo_bar \"&#x2014;\">\r\n" + //
				"  <!ENTITY foo_baz \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root>\r\n" + //
				"  &foo_b|\r\n" + // <- here completion shows mdash entity
				"</root>";
		testCompletionFor(xml, 2 + //
				2 /* CDATA and Comments */ + //
				PredefinedEntity.values().length /* predefined entities */, true,
				c("&foo_bar;", "&foo_bar;", r(6, 2, 6, 8), "&foo_bar;"), //
				c("&foo_baz;", "&foo_baz;", r(6, 2, 6, 8), "&foo_baz;"));
	}

	@Test
	public void insideWithAmp() throws BadLocationException {
		// &m|d;blablabla
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE root [\r\n" + //
				"  <!ENTITY mdash \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root>\r\n" + //
				"  &m|d;blablabla\r\n" + // <- here completion shows mdash entity
				"</root>";
		testCompletionFor(xml, 1 + //
				2 /* CDATA and Comments */ + //
				PredefinedEntity.values().length /* predefined entities */,
				c("&mdash;", "&mdash;", r(5, 2, 5, 6), "&mdash;"));
	}

	@Test
	public void none() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE root [\r\n" + //
				"  <!ENTITY mdash \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root>\r\n" + //
				"  |\r\n" + // <- no entity completion
				"</root>";
		testCompletionFor(xml, 2 + 2 /* CDATA and Comments */);
	}

	// Test for external entities

	@Test
	public void external() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<!DOCTYPE root-element SYSTEM \"src/test/resources/dtd/entities/base.dtd\" [\r\n" + //
				"	<!ENTITY mdash \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root-element>\r\n" + //
				"\r\n &|" + //
				"</root-element>";
		testCompletionFor(xml, null, "test.xml", 2 + //
				2 /* CDATA and Comments */ + //
				PredefinedEntity.values().length /* predefined entities */,
				c("&mdash;", "&mdash;", r(6, 1, 6, 2), "&mdash;"), //
				c("&foo;", "&foo;", r(6, 1, 6, 2), "&foo;"));
	}

	@Test
	public void externalWithSYSTEM() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE author SYSTEM \"src/test/resources/dtd/entities/base-system.dtd\">\r\n" + //
				"<author>&|</author>";
		testCompletionFor(xml, null, "test.xml", 2 + //
				2 /* CDATA and Comments */ + //
				PredefinedEntity.values().length /* predefined entities */,
				c("&writer;", "&writer;", r(2, 8, 2, 9), "&writer;"), //
				c("&copyright;", "&copyright;", r(2, 8, 2, 9), "&copyright;"));
	}

	@Test
	public void bug_vscode_xml_262() throws BadLocationException {
		// See
		// https://github.com/redhat-developer/vscode-xml/issues/262#issuecomment-634716408
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<!DOCTYPE alex-update-sequence SYSTEM \"src/test/resources/dtd/entities/bug_vscode-xml_262.dtd\" [<!-- {{{ -->\r\n"
				+ //
				"]>\r\n" + //
				"<!-- }}} -->\r\n" + //
				"<root>\r\n" + //
				"&|\r\n" + //
				"</root>";
		testCompletionFor(xml, null, "test.xml", 29 + //
				2 /* CDATA and Comments */ + //
				PredefinedEntity.values().length /* predefined entities */, //
				c("&fdcuf_hide_actions_column;", "&fdcuf_hide_actions_column;", r(5, 0, 5, 1),
						"&fdcuf_hide_actions_column;"));
	}
}
