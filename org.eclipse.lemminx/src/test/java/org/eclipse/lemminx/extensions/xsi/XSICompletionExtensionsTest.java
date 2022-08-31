/**
 *  Copyright (c) 2019 Red Hat, Inc. and others.
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
package org.eclipse.lemminx.extensions.xsi;

import static org.eclipse.lemminx.XMLAssert.c;
import static org.eclipse.lemminx.XMLAssert.te;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.xsl.XSLURIResolverExtension;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lemminx.settings.EnforceQuoteStyle;
import org.eclipse.lemminx.settings.QuoteStyle;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

/**
 * XSL completion tests which test the {@link XSLURIResolverExtension}.
 *
 */
public class XSICompletionExtensionsTest extends AbstractCacheBasedTest {

	@Test
	public void completion() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=|>";
		testCompletionFor(xml,
				c("true", te(1, 71, 1, 71, "\"true\""), "\"true\""),
				c("false", te(1, 71, 1, 71, "\"false\""), "\"false\""));
	}

	@Test
	public void completion2() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"|\">";
		testCompletionFor(xml,
				c("true", te(1, 72, 1, 72, "true"), "true"),
				c("false", te(1, 72, 1, 72, "false"), "false"));
	}

	@Test
	public void completion3() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >\r\n" +
				"  <a xsi:nil=|> </a> ";
		testCompletionFor(xml,
				c("true", te(2, 13, 2, 13, "\"true\""), "\"true\""),
				c("false", te(2, 13, 2, 13, "\"false\""), "\"false\""));
	}

	@Test
	public void completion3NNamespace() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"<project >\r\n" +
				"  <a xsi:nil=|> </a> ";
		testCompletionFor(xml);
	}

	@Test
	public void completion4() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >\r\n" +
				"  <a xsi:nil=|> </a> ";

		testCompletionFor(xml, singleQuotesSharedSettings(),
				c("true", te(2, 13, 2, 13, "\'true\'"), "\'true\'"),
				c("false", te(2, 13, 2, 13, "\'false\'"), "\'false\'"));
	}

	@Test
	public void completionXMLNSXSIValue() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"<project xmlns:xsi=| >\r\n" +
				"  <a> </a> \r\n"+
				"</project>";
		testCompletionFor(xml,
				c("http://www.w3.org/2001/XMLSchema-instance", te(1, 19, 1, 19, "\"http://www.w3.org/2001/XMLSchema-instance\""), "\"http://www.w3.org/2001/XMLSchema-instance\"")
				); // coming from stylesheet children
	}

	@Test
	public void completionXMLNSXSIValueSingleQuotes() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"<project xmlns:xsi=| >\r\n" +
				"  <a> </a> \r\n"+
				"</project>";
		testCompletionFor(xml, singleQuotesSharedSettings(),
				c("http://www.w3.org/2001/XMLSchema-instance", te(1, 19, 1, 19, "\'http://www.w3.org/2001/XMLSchema-instance\'"), "\'http://www.w3.org/2001/XMLSchema-instance\'")
				); // coming from stylesheet children
	}

	@Test
	public void completionXMLNSXSIWhole() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"<project xmlns:x| >\r\n" +
				"  <a> </a> \r\n"+
				"</project>";
		testCompletionFor(xml,
				c("xmlns:xsi", te(1, 9, 1, 16, "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""), "xmlns:xsi")
				); // coming from stylesheet children
	}

	@Test
	public void completionXMLNS() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"<project x| >\r\n" +
				"  <a> </a> \r\n"+
				"</project>";
		testCompletionFor(xml,
				c("xmlns", te(1, 9, 1, 10, "xmlns=\"\""), "xmlns")
				); // coming from stylesheet children
	}

	@Test
	public void completionXMLNSOnlyInRoot() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"<project>\r\n" +
				"  <a x|> </a> \r\n"+
				"</project>";
		testCompletionFor(xml
				); // coming from stylesheet children
	}

	private SharedSettings singleQuotesSharedSettings() {
		SharedSettings settings = new SharedSettings();
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		return settings;
	}

	private void testCompletionFor(String xml, CompletionItem... expectedItems) throws BadLocationException {
		XMLAssert.testCompletionFor(xml, null, expectedItems);
	}

	private void testCompletionFor(String xml, SharedSettings sharedSettings, CompletionItem... expectedItems) throws BadLocationException {
		XMLAssert.testCompletionFor(new XMLLanguageService(), xml, null, null, null, null, sharedSettings, expectedItems);
	}
}
