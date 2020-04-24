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

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.entities.EntitiesDocumentationUtils.PredefinedEntity;
import org.junit.jupiter.api.Test;

/**
 * Test for entities completion used in a text node.
 *
 */
public class EntitiesCompletionExtensionsTest {

	@Test
	public void afterAmp() throws BadLocationException {
		// &|
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE article [\r\n" + //
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
				"<!DOCTYPE article [\r\n" + //
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
				"<!DOCTYPE article [\r\n" + //
				"  <!ENTITY mdash \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root>\r\n" + //
				"  &m|dblablabla\r\n" + // <- here completion shows mdash entity
				"</root>";
		testCompletionFor(xml, 1 + //
				2 /* CDATA and Comments */ + //
				PredefinedEntity.values().length /* predefined entities */,
				c("&mdash;", "&mdash;", r(5, 2, 5, 14), "&mdash;"));
	}

	@Test
	public void insideWithAmp() throws BadLocationException {
		// &m|d;blablabla
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE article [\r\n" + //
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
	public void nonep() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE article [\r\n" + //
				"  <!ENTITY mdash \"&#x2014;\">\r\n" + //
				"]>\r\n" + //
				"<root>\r\n" + //
				"  |\r\n" + // <- no entity completion
				"</root>";
		testCompletionFor(xml, 2 + 2 /* CDATA and Comments */);
	}
}
