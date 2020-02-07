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

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lemminx.settings.XMLCompletionSettings;
import org.eclipse.lemminx.settings.XMLFormattingOptions;
import org.eclipse.lsp4j.CompletionItem;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * XSL completion tests which test the {@link XSLURIResolverExtension}.
 *
 */
public class XSICompletionExtensionsTest {
	
	public static XMLFormattingOptions formattingSettingsSingleQuotes = new XMLFormattingOptions(true);
	
	@BeforeClass
	public static void runOnceBeforeClass() {
		formattingSettingsSingleQuotes.setQuotations(XMLFormattingOptions.SINGLE_QUOTES_VALUE);
	}

	
	

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
				
		testCompletionFor(xml, formattingSettingsSingleQuotes,
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
		testCompletionFor(xml, formattingSettingsSingleQuotes,
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

	private void testCompletionFor(String xml, CompletionItem... expectedItems) throws BadLocationException {
		XMLAssert.testCompletionFor(xml, null, expectedItems);
	}

	private void testCompletionFor(String xml, XMLFormattingOptions formattingSettings, CompletionItem... expectedItems) throws BadLocationException {
		XMLAssert.testCompletionFor(new XMLLanguageService(), xml, null, null, null, null, new XMLCompletionSettings(true), formattingSettings, expectedItems);
	}
}
