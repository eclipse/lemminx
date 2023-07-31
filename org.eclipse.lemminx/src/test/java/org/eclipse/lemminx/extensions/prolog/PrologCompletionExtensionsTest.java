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
package org.eclipse.lemminx.extensions.prolog;

import static org.eclipse.lemminx.XMLAssert.c;
import static org.eclipse.lemminx.XMLAssert.r;
import static org.eclipse.lemminx.XMLAssert.te;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.xsl.XSLURIResolverExtension;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lemminx.settings.EnforceQuoteStyle;
import org.eclipse.lemminx.settings.QuoteStyle;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemCapabilities;
import org.junit.jupiter.api.Test;

/**
 * XSL completion tests which test the {@link XSLURIResolverExtension}.
 *
 */
public class PrologCompletionExtensionsTest extends AbstractCacheBasedTest {

	@Test
	public void completionVersionWithV() throws BadLocationException {
		// completion on |
		String xml = "<?xml v|?>\r\n" + //
				"<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
		testCompletionFor(xml, 1, c("version", te(0, 6, 0, 7, "version=\"1.0\""), "version"));
	}

	@Test
	public void completionVersion() throws BadLocationException {
		// completion on |
		String xml = "<?xml |?>\r\n" + //
				"<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
		testCompletionFor(xml, c("version", te(0, 6, 0, 6, "version=\"1.0\""), "version"));
	}

	@Test
	public void completionEncodingAndStandalone() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=\"1.0\" |?>\r\n" + //
				"<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
		testCompletionFor(xml, 2, c("encoding", te(0, 20, 0, 20, "encoding=\"UTF-8\""), "encoding"),
				c("standalone", te(0, 20, 0, 20, "standalone=\"yes\""), "standalone"));
	}

	@Test
	public void completionStandalone() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" |?>\r\n" + //
				"<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
		testCompletionFor(xml, 1, c("standalone", te(0, 37, 0, 37, "standalone=\"yes\""), "standalone"));
	}

	@Test
	public void noCompletionsAfterStandalone() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=\"1.0\" standalone=\"yes\" |?>\r\n" + //
				"<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
		testCompletionFor(xml, 0, (CompletionItem[]) null);
	}

	@Test
	public void completionEncodingBeforeStandalone() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=\"1.0\" | standalone=\"yes\" ?>\r\n" + //
				"<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
		testCompletionFor(xml, 1, c("encoding", te(0, 20, 0, 20, "encoding=\"UTF-8\""), "encoding"));
	}

	@Test
	public void completionVersionValue() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=| ?>\r\n" + //
				"<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
		testCompletionFor(xml,
				c(PrologModel.VERSION_1, te(0, 14, 0, 14, "\"" + PrologModel.VERSION_1 + "\""),
						"\"" + PrologModel.VERSION_1 + "\""),
				c(PrologModel.VERSION_1_1, te(0, 14, 0, 14, "\"" + PrologModel.VERSION_1_1 + "\""),
						"\"" + PrologModel.VERSION_1_1 + "\""));
	}

	@Test
	public void completionVersionValueItemDefaults() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=| ?>\r\n" + //
				"<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
		testCompletionFor(true, xml,
				c(PrologModel.VERSION_1, te(0, 14, 0, 14, "\"" + PrologModel.VERSION_1 + "\""),
						"\"" + PrologModel.VERSION_1 + "\""),
				c(PrologModel.VERSION_1_1, te(0, 14, 0, 14, "\"" + PrologModel.VERSION_1_1 + "\""),
						"\"" + PrologModel.VERSION_1_1 + "\""));
	}

	@Test
	public void completionVersionNoSpaceAfterEquals() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=|?>\r\n" + // <- no space after the '='
				"<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
		testCompletionFor(xml,
				c(PrologModel.VERSION_1, te(0, 14, 0, 14, "\"" + PrologModel.VERSION_1 + "\""),
						"\"" + PrologModel.VERSION_1 + "\""),
				c(PrologModel.VERSION_1_1, te(0, 14, 0, 14, "\"" + PrologModel.VERSION_1_1 + "\""),
						"\"" + PrologModel.VERSION_1_1 + "\""));
	}

	@Test
	public void completionEncodingValue() throws BadLocationException {
		// completion on |
		String xml = "<?xml encoding=| ?>\r\n" + //
				"<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
		testCompletionFor(xml,
				c(PrologModel.UTF_8, te(0, 15, 0, 15, "\"" + PrologModel.UTF_8 + "\""),
						"\"" + PrologModel.UTF_8 + "\""),
				c(PrologModel.EUC_KR, te(0, 15, 0, 15, "\"" + PrologModel.EUC_KR + "\""),
						"\"" + PrologModel.EUC_KR + "\""));
	}

	@Test
	public void completionStandaloneValue() throws BadLocationException {
		// completion on |
		String xml = "<?xml standalone=| ?>\r\n" + //
				"<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
		testCompletionFor(xml,
				c(PrologModel.YES, te(0, 17, 0, 17, "\"" + PrologModel.YES + "\""), "\"" + PrologModel.YES + "\""),
				c(PrologModel.NO, te(0, 17, 0, 17, "\"" + PrologModel.NO + "\""), "\"" + PrologModel.NO + "\""));
	}

	@Test
	public void completionVersionExists() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=\"1.0\" version=| ?>\r\n" + //
				"<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
		testCompletionFor(xml, c(PrologModel.VERSION_1_1, te(0, 28, 0, 28, "\"" + PrologModel.VERSION_1_1 + "\""),
				"\"" + PrologModel.VERSION_1_1 + "\""));
	}

	@Test
	public void completionEncodingExists() throws BadLocationException {
		// completion on |
		String xml = "<?xml encoding=\"UTF-8\" encoding=\"Win|\" ?>\r\n" + //
				"<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
		testCompletionFor(xml,
				c(PrologModel.WINDOWS_1251, te(0, 33, 0, 36, PrologModel.WINDOWS_1251), PrologModel.WINDOWS_1251),
				c(PrologModel.WINDOWS_1252, te(0, 33, 0, 36, PrologModel.WINDOWS_1252), PrologModel.WINDOWS_1252));
	}

	@Test
	public void completionVersionSingleQuotes() throws BadLocationException {
		// completion on |
		String xml = "<?xml version=| ?>\r\n" + //
				"<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
		SharedSettings settings = createSharedSettings(true, true);
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		testCompletionFor(xml, settings,
				c(PrologModel.VERSION_1, te(0, 14, 0, 14, "\'" + PrologModel.VERSION_1 + "\'"),
						"\'" + PrologModel.VERSION_1 + "\'"),
				c(PrologModel.VERSION_1_1, te(0, 14, 0, 14, "\'" + PrologModel.VERSION_1_1 + "\'"),
						"\'" + PrologModel.VERSION_1_1 + "\'"));
	}

	@Test
	public void completionEncodingSingleQuotes() throws BadLocationException {
		// completion on |
		String xml = "<?xml encoding=| ?>\r\n" + //
				"<project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
		SharedSettings settings = createSharedSettings(true, true);
		settings.getPreferences().setQuoteStyle(QuoteStyle.singleQuotes);
		settings.getFormattingSettings().setEnforceQuoteStyle(EnforceQuoteStyle.preferred);
		testCompletionFor(xml, settings,
				c(PrologModel.UTF_8, te(0, 15, 0, 15, "\'" + PrologModel.UTF_8 + "\'"),
						"\'" + PrologModel.UTF_8 + "\'"),
				c(PrologModel.SHIFT_JIS, te(0, 15, 0, 15, "\'" + PrologModel.SHIFT_JIS + "\'"),
						"\'" + PrologModel.SHIFT_JIS + "\'"));
	}

	@Test
	public void testAutoCompletionPrologWithXML() throws BadLocationException {
		// With 'xml' label
		testCompletionFor("<?xml|", true, //
				c("Insert XML Declaration", //
						"<?xml version=\"${1|1.0,1.1|}\" encoding=\"${2|UTF-8,ISO-8859-1,Windows-1251,Windows-1252,Shift JIS,GB2312,EUC-KR|}\"?>${0}", //
						r(0, 0, 0, 5), //
						"<?xml"));
		testCompletionFor("<?xml|>", true, //
				c("Insert XML Declaration", //
						"<?xml version=\"${1|1.0,1.1|}\" encoding=\"${2|UTF-8,ISO-8859-1,Windows-1251,Windows-1252,Shift JIS,GB2312,EUC-KR|}\"?>${0}", //
						r(0, 0, 0, 6), //
						"<?xml"));
		testCompletionFor("<?xml|?>", true, //
				c("Insert XML Declaration", //
						"<?xml version=\"${1|1.0,1.1|}\" encoding=\"${2|UTF-8,ISO-8859-1,Windows-1251,Windows-1252,Shift JIS,GB2312,EUC-KR|}\"?>${0}", //
						r(0, 0, 0, 7), //
						"<?xml"));
	}

	@Test
	public void testAutoCompletionPrologWithoutXML() throws BadLocationException {
		// No 'xml' label
		testCompletionFor("<?|", true, //
				c("Insert XML Declaration", //
						"<?xml version=\"${1|1.0,1.1|}\" encoding=\"${2|UTF-8,ISO-8859-1,Windows-1251,Windows-1252,Shift JIS,GB2312,EUC-KR|}\"?>${0}", //
						r(0, 0, 0, 2), //
						"<?xml"));
		testCompletionFor("<?|", false, //
				c("Insert XML Declaration", //
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
						r(0, 0, 0, 2), //
						"<?xml"));
		testCompletionFor("<?|>", true, //
				c("Insert XML Declaration", //
						"<?xml version=\"${1|1.0,1.1|}\" encoding=\"${2|UTF-8,ISO-8859-1,Windows-1251,Windows-1252,Shift JIS,GB2312,EUC-KR|}\"?>${0}", //
						r(0, 0, 0, 3), //
						"<?xml"));
		testCompletionFor("<?|?>", true, //
				c("Insert XML Declaration", //
						"<?xml version=\"${1|1.0,1.1|}\" encoding=\"${2|UTF-8,ISO-8859-1,Windows-1251,Windows-1252,Shift JIS,GB2312,EUC-KR|}\"?>${0}", //
						r(0, 0, 0, 4), //
						"<?xml"));
	}

	@Test
	public void testAutoCompletionPrologWithPartialXML() throws BadLocationException {
		testCompletionFor("<?x|", true, //
				c("Insert XML Declaration", //
						"<?xml version=\"${1|1.0,1.1|}\" encoding=\"${2|UTF-8,ISO-8859-1,Windows-1251,Windows-1252,Shift JIS,GB2312,EUC-KR|}\"?>${0}", //
						r(0, 0, 0, 3), //
						"<?xml"));
		testCompletionFor("<?xm|", true, //
				c("Insert XML Declaration", //
						"<?xml version=\"${1|1.0,1.1|}\" encoding=\"${2|UTF-8,ISO-8859-1,Windows-1251,Windows-1252,Shift JIS,GB2312,EUC-KR|}\"?>${0}", //
						r(0, 0, 0, 4), //
						"<?xml"));
		testCompletionFor("<?xml|", true, //
				c("Insert XML Declaration", //
						"<?xml version=\"${1|1.0,1.1|}\" encoding=\"${2|UTF-8,ISO-8859-1,Windows-1251,Windows-1252,Shift JIS,GB2312,EUC-KR|}\"?>${0}", //
						r(0, 0, 0, 5), //
						"<?xml"));
		testCompletionFor("<?xml|?", true, //
				c("Insert XML Declaration", //
						"<?xml version=\"${1|1.0,1.1|}\" encoding=\"${2|UTF-8,ISO-8859-1,Windows-1251,Windows-1252,Shift JIS,GB2312,EUC-KR|}\"?>${0}", //
						r(0, 0, 0, 6), //
						"<?xml"));
		testCompletionFor("<?xml|?>", true, //
				c("Insert XML Declaration", //
						"<?xml version=\"${1|1.0,1.1|}\" encoding=\"${2|UTF-8,ISO-8859-1,Windows-1251,Windows-1252,Shift JIS,GB2312,EUC-KR|}\"?>${0}", //
						r(0, 0, 0, 7), //
						"<?xml"));
	}

	@Test
	public void testAutoCompletionPrologDTDFileWithXML() throws BadLocationException {
		// With 'xml' label
		String dtdFileURI = "test://test/test.dtd";
		testCompletionFor("<?xml|", dtdFileURI, true, //
				c("Insert XML Declaration", //
						"<?xml version=\"${1|1.0,1.1|}\" encoding=\"${2|UTF-8,ISO-8859-1,Windows-1251,Windows-1252,Shift JIS,GB2312,EUC-KR|}\"?>${0}", //
						r(0, 0, 0, 5), //
						"<?xml"));
		testCompletionFor("<?xml|>", dtdFileURI, true, //
				c("Insert XML Declaration", //
						"<?xml version=\"${1|1.0,1.1|}\" encoding=\"${2|UTF-8,ISO-8859-1,Windows-1251,Windows-1252,Shift JIS,GB2312,EUC-KR|}\"?>${0}", //
						r(0, 0, 0, 6), //
						"<?xml"));
		testCompletionFor("<?xml|?>", dtdFileURI, true, //
				c("Insert XML Declaration", //
						"<?xml version=\"${1|1.0,1.1|}\" encoding=\"${2|UTF-8,ISO-8859-1,Windows-1251,Windows-1252,Shift JIS,GB2312,EUC-KR|}\"?>${0}", //
						r(0, 0, 0, 7), //
						"<?xml"));
	}

	@Test
	public void testAutoCompletionPrologDTDFileWithoutXML() throws BadLocationException {
		// No 'xml' label
		String dtdFileURI = "test://test/test.dtd";
		testCompletionFor("<?|", dtdFileURI, true, //
				c("Insert XML Declaration", //
						"<?xml version=\"${1|1.0,1.1|}\" encoding=\"${2|UTF-8,ISO-8859-1,Windows-1251,Windows-1252,Shift JIS,GB2312,EUC-KR|}\"?>${0}", //
						r(0, 0, 0, 2), //
						"<?xml"));
		testCompletionFor("<?|", dtdFileURI, false, //
				c("Insert XML Declaration", //
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
						r(0, 0, 0, 2), //
						"<?xml"));
		testCompletionFor("<?|>", dtdFileURI, true, //
				c("Insert XML Declaration", //
						"<?xml version=\"${1|1.0,1.1|}\" encoding=\"${2|UTF-8,ISO-8859-1,Windows-1251,Windows-1252,Shift JIS,GB2312,EUC-KR|}\"?>${0}", //
						r(0, 0, 0, 3), //
						"<?xml"));
		testCompletionFor("<?|?>", dtdFileURI, true, //
				c("Insert XML Declaration", //
						"<?xml version=\"${1|1.0,1.1|}\" encoding=\"${2|UTF-8,ISO-8859-1,Windows-1251,Windows-1252,Shift JIS,GB2312,EUC-KR|}\"?>${0}", //
						r(0, 0, 0, 4), //
						"<?xml"));
	}

	@Test
	public void testAutoCompletionPrologDTFFileWithPartialXML() throws BadLocationException {
		String dtdFileURI = "test://test/test.dtd";
		testCompletionFor("<?x|", dtdFileURI, true, //
				c("Insert XML Declaration", //
						"<?xml version=\"${1|1.0,1.1|}\" encoding=\"${2|UTF-8,ISO-8859-1,Windows-1251,Windows-1252,Shift JIS,GB2312,EUC-KR|}\"?>${0}", //
						r(0, 0, 0, 3), //
						"<?xml"));
		testCompletionFor("<?xm|", dtdFileURI, true, //
				c("Insert XML Declaration", //
						"<?xml version=\"${1|1.0,1.1|}\" encoding=\"${2|UTF-8,ISO-8859-1,Windows-1251,Windows-1252,Shift JIS,GB2312,EUC-KR|}\"?>${0}", //
						r(0, 0, 0, 4), //
						"<?xml"));
		testCompletionFor("<?xml|", dtdFileURI, true, //
				c("Insert XML Declaration", //
						"<?xml version=\"${1|1.0,1.1|}\" encoding=\"${2|UTF-8,ISO-8859-1,Windows-1251,Windows-1252,Shift JIS,GB2312,EUC-KR|}\"?>${0}", //
						r(0, 0, 0, 5), //
						"<?xml"));
		testCompletionFor("<?xml|?", dtdFileURI, true, //
				c("Insert XML Declaration", //
						"<?xml version=\"${1|1.0,1.1|}\" encoding=\"${2|UTF-8,ISO-8859-1,Windows-1251,Windows-1252,Shift JIS,GB2312,EUC-KR|}\"?>${0}", //
						r(0, 0, 0, 6), //
						"<?xml"));
		testCompletionFor("<?xml|?>", dtdFileURI, true, //
				c("Insert XML Declaration", //
						"<?xml version=\"${1|1.0,1.1|}\" encoding=\"${2|UTF-8,ISO-8859-1,Windows-1251,Windows-1252,Shift JIS,GB2312,EUC-KR|}\"?>${0}", //
						r(0, 0, 0, 7), //
						"<?xml"));
	}

	private void testCompletionFor(boolean enableItemDefaults, String xml, CompletionItem... expectedItems) throws BadLocationException {
		XMLAssert.testCompletionFor(xml, null, enableItemDefaults, expectedItems);
	}

	private void testCompletionFor(String xml, CompletionItem... expectedItems) throws BadLocationException {
		XMLAssert.testCompletionFor(xml, null, expectedItems);
	}

	private void testCompletionFor(String xml, int expectedCount, CompletionItem... expectedItems)
			throws BadLocationException {
		XMLAssert.testCompletionFor(xml, expectedCount, expectedItems);
	}

	private void testCompletionFor(String xml, String fileURI, boolean isSnippetsSupported,
			CompletionItem... expectedItems) throws BadLocationException {
		testCompletionFor(xml, fileURI, createSharedSettings(false, isSnippetsSupported), expectedItems);
	}

	private void testCompletionFor(String xml, String fileURI, SharedSettings settings, CompletionItem... expectedItems)
			throws BadLocationException {
		XMLAssert.testCompletionFor(new XMLLanguageService(), xml, null, null, fileURI, null, settings, expectedItems);
	}

	private void testCompletionFor(String xml, boolean isSnippetsSupported, CompletionItem... expectedItems)
			throws BadLocationException {
		testCompletionFor(xml, createSharedSettings(false, isSnippetsSupported), expectedItems);
	}

	private void testCompletionFor(String xml, SharedSettings settings, CompletionItem... expectedItems)
			throws BadLocationException {
		XMLAssert.testCompletionFor(new XMLLanguageService(), xml, null, null, null, null, settings, expectedItems);
	}

	private SharedSettings createSharedSettings(boolean autoCloseTags, boolean isSnippetsSupported) {
		SharedSettings sharedSettings = new SharedSettings();
		CompletionCapabilities capabilities = new CompletionCapabilities();
		CompletionItemCapabilities itemCapabilities = new CompletionItemCapabilities(isSnippetsSupported);
		capabilities.setCompletionItem(itemCapabilities);
		sharedSettings.getCompletionSettings().setCapabilities(capabilities);
		sharedSettings.getCompletionSettings().setAutoCloseTags(autoCloseTags);
		return sharedSettings;
	}
}
