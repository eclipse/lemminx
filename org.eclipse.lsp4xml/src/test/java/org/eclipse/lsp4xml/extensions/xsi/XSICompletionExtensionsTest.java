/**
 *  Copyright (c) 2019 Red Hat, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.xsi;

import static org.eclipse.lsp4xml.XMLAssert.c;
import static org.eclipse.lsp4xml.XMLAssert.te;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.junit.Test;

/**
 * XSL completion tests which test the {@link XSLURIResolverExtension}.
 *
 */
public class XSICompletionExtensionsTest {

	@Test
	public void completion() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=|>";
		testCompletionFor(xml,
				c("true", te(1, 71, 1, 71, "\"true\""), "\"true\""), // <-- coming from substition group of xsl:declaration
				c("false", te(1, 71, 1, 71, "\"false\""), "\"false\"")); // coming from stylesheet children
	}

	@Test
	public void completion2() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"|\">";
		testCompletionFor(xml,
				c("true", te(1, 71, 1, 73, "\"true\""), "\"true\""), // <-- coming from substition group of xsl:declaration
				c("false", te(1, 71, 1, 73, "\"false\""), "\"false\"")); // coming from stylesheet children
	}

	@Test
	public void completion3() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >\r\n" +
				"  <a xsi:nil=|> </a> ";
		testCompletionFor(xml,
				c("true", te(2, 13, 2, 13, "\"true\""), "\"true\""), // <-- coming from substition group of xsl:declaration
				c("false", te(2, 13, 2, 13, "\"false\""), "\"false\"")); // coming from stylesheet children
	}

	private void testCompletionFor(String xml, CompletionItem... expectedItems) throws BadLocationException {
		XMLAssert.testCompletionFor(xml, null, expectedItems);
	}
}
