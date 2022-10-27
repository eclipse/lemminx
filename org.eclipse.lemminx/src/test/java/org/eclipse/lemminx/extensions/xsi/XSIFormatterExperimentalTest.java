/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
*  are made available under the terms of the Eclipse Public License v2.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.xsi;

import static org.eclipse.lemminx.XMLAssert.te;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.xsi.settings.XSISchemaLocationSplit;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.TextEdit;
import org.junit.jupiter.api.Test;

/**
 * XSI xsi:schemaLocation formatter tests
 *
 */
public class XSIFormatterExperimentalTest extends AbstractCacheBasedTest {

	@Test
	public void xsiSchemaLocationSplitNone() throws BadLocationException {
		// Default
		SharedSettings settings = createSettings();
		XSISchemaLocationSplit.setSplit(XSISchemaLocationSplit.none, settings.getFormattingSettings());
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" " + //
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + //
				"xmlns:util=\"http://www.springframework.org/schema/util\" " + //
				"xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"        http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd\"> </beans>";
		String expected = content;
		assertFormat(content, expected, settings);

		// None
		settings = createSettings();
		XSISchemaLocationSplit.setSplit(XSISchemaLocationSplit.none, settings.getFormattingSettings());
		assertFormat(content, expected, settings);
	}

	@Test
	public void xsiSchemaLocationSplitOnElement() throws BadLocationException {
		SharedSettings settings = createSettings();
		settings.getFormattingSettings().setSplitAttributes(false);
		settings.getFormattingSettings().setPreserveAttributeLineBreaks(false);
		XSISchemaLocationSplit.setSplit(XSISchemaLocationSplit.onElement, settings.getFormattingSettings());
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"    xsi:schemaLocation=\"\r\n" + //
				"        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"        http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd\"> </beans>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans\r\n"
				+ //
				"                                                                                 http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"                                                                                 http://www.springframework.org/schema/util\r\n"
				+ //
				"                                                                                 http://www.springframework.org/schema/util/spring-util.xsd\"> </beans>";
		assertFormat(content, expected, settings,
				te(1, 6, 2, 4, " "), //
				te(2, 57, 3, 4, " "), //
				te(3, 24, 4, 8, ""), //
				te(4, 51, 4, 52,
						"\r\n                                                                                 "), //
				te(4, 112, 5, 8,
						"\r\n                                                                                 "), //
				te(5, 50, 5, 51,
						"\r\n                                                                                 "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void xsiSchemaLocationSplitOnElementWithPreserveLineBreaks() throws BadLocationException {
		SharedSettings settings = createSettings();
		settings.getFormattingSettings().setInsertSpaces(true);
		settings.getFormattingSettings().setPreserveAttributeLineBreaks(true);
		XSISchemaLocationSplit.setSplit(XSISchemaLocationSplit.onElement, settings.getFormattingSettings());
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"    xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
				"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"    xmlns:util=\"http://www.springframework.org/schema/util\"\r\n" + //
				"    xsi:schemaLocation=\"\r\n" + //
				"        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"        http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd\"> </beans>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"  xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
				"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"  xmlns:util=\"http://www.springframework.org/schema/util\"\r\n" + //
				"  xsi:schemaLocation=\"http://www.springframework.org/schema/beans\r\n" + //
				"                      http://www.springframework.org/schema/beans/spring-beans.xsd\r\n" + //
				"                      http://www.springframework.org/schema/util\r\n" + //
				"                      http://www.springframework.org/schema/util/spring-util.xsd\"> </beans>";
		assertFormat(content, expected, settings,
				te(1, 6, 2, 4, "\r\n  "), //
				te(2, 55, 3, 4, "\r\n  "), //
				te(3, 57, 4, 4, "\r\n  "), //
				te(4, 59, 5, 4, "\r\n  "), //
				te(5, 24, 6, 8, ""), //
				te(6, 51, 6, 52,
						"\r\n                      "), //
				te(6, 112, 7, 8,
						"\r\n                      "), //
				te(7, 50, 7, 51,
						"\r\n                      "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void xsiSchemaLocationSplitOnElementWithSplitAttribute() throws BadLocationException {
		SharedSettings settings = createSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		XSISchemaLocationSplit.setSplit(XSISchemaLocationSplit.onElement, settings.getFormattingSettings());
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"    xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
				"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"    xmlns:util=\"http://www.springframework.org/schema/util\"\r\n" + //
				"    xsi:schemaLocation=\"\r\n" + //
				"        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"        http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd\"> </beans>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"    xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
				"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"    xmlns:util=\"http://www.springframework.org/schema/util\"\r\n" + //
				"    xsi:schemaLocation=\"http://www.springframework.org/schema/beans\r\n" + //
				"                        http://www.springframework.org/schema/beans/spring-beans.xsd\r\n" + //
				"                        http://www.springframework.org/schema/util\r\n" + //
				"                        http://www.springframework.org/schema/util/spring-util.xsd\"> </beans>";
		assertFormat(content, expected, settings,
				te(5, 24, 6, 8, ""), //
				te(6, 51, 6, 52, "\r\n                        "), //
				te(6, 112, 7, 8, "\r\n                        "), //
				te(7, 50, 7, 51, "\r\n                        "));
		assertFormat(expected, expected, settings);
	}

	@Override
	public String toString() {
		return "XSIFormatterExperimentalTest []";
	}

	@Test
	public void xsiSchemaLocationSplitOnPair() throws BadLocationException {
		SharedSettings settings = createSettings();
		settings.getFormattingSettings().setSplitAttributes(false);
		settings.getFormattingSettings().setPreserveAttributeLineBreaks(false);
		XSISchemaLocationSplit.setSplit(XSISchemaLocationSplit.onPair, settings.getFormattingSettings());
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"\r\n" + //
				"        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"        http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd\"> </beans>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"                                                                                 http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd\"> </beans>";
		assertFormat(content, expected, settings,
				te(1, 6, 2, 4, " "), //
				te(2, 78, 3, 8, ""), //
				te(3, 112, 4, 8,
						"\r\n                                                                                 "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void xsiSchemaLocationSplitOnPairWithSplitAttribute() throws BadLocationException {
		SharedSettings settings = createSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		XSISchemaLocationSplit.setSplit(XSISchemaLocationSplit.onPair, settings.getFormattingSettings());
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"    xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
				"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"    xmlns:util=\"http://www.springframework.org/schema/util\"\r\n" + //
				"    xsi:schemaLocation=\"\r\n" + //
				"        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"        http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd\">\r\n"
				+ //
				"\r\n" + //
				"</beans>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"    xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
				"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"    xmlns:util=\"http://www.springframework.org/schema/util\"\r\n" + //
				"    xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"                        http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd\">\r\n"
				+ //
				"\r\n" + //
				"</beans>";
		assertFormat(content, expected, settings,
				te(5, 24, 6, 8, ""), //
				te(6, 112, 7, 8, "\r\n                        "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void xsiSchemaLocationSplitOnPairWithPreserveLineBreaks() throws BadLocationException {
		SharedSettings settings = createSettings();
		settings.getFormattingSettings().setInsertSpaces(true);
		settings.getFormattingSettings().setPreserveAttributeLineBreaks(true);
		XSISchemaLocationSplit.setSplit(XSISchemaLocationSplit.onPair, settings.getFormattingSettings());
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"   xsi:schemaLocation=\"\r\n" + //
				"        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"        http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd\"> </beans>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"                                                                            http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd\"> </beans>";
		assertFormat(content, expected, settings,
				te(1, 6, 2, 4, "\r\n  "), //
				te(2, 57, 2, 60, " "), //
				te(2, 80, 3, 8, ""), //
				te(3, 112, 4, 8,
						"\r\n                                                                            "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void xsiSchemaLocationSplitOnPairWasElement() throws BadLocationException {
		SharedSettings settings = createSettings();
		settings.getFormattingSettings().setSplitAttributes(true);
		XSISchemaLocationSplit.setSplit(XSISchemaLocationSplit.onPair, settings.getFormattingSettings());
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"    xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
				"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"    xmlns:util=\"http://www.springframework.org/schema/util\"\r\n" + //
				"    xsi:schemaLocation=\"http://www.springframework.org/schema/beans\r\n" + //
				"                        http://www.springframework.org/schema/beans/spring-beans.xsd\r\n" + //
				"                        http://www.springframework.org/schema/util\r\n" + //
				"                        http://www.springframework.org/schema/util/spring-util.xsd\"> </beans>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"    xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
				"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"    xmlns:util=\"http://www.springframework.org/schema/util\"\r\n" + //
				"    xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"                        http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd\"> </beans>";
		assertFormat(content, expected, settings,
				te(5, 67, 6, 24, " "), //
				te(7, 66, 8, 24, " "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void xsiSchemaLocationEmpty() throws BadLocationException {
		SharedSettings settings = createSettings();
		XSISchemaLocationSplit.setSplit(XSISchemaLocationSplit.onElement, settings.getFormattingSettings());
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans " + //
				"xmlns=\"http://www.springframework.org/schema/beans\" " + //
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + //
				"xmlns:util=\"http://www.springframework.org/schema/util\" " + //
				"xsi:schemaLocation=" + //
				"</beans>";
		assertFormat(content, content, settings);

		content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans " + //
				"xmlns=\"http://www.springframework.org/schema/beans\" " + //
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + //
				"xmlns:util=\"http://www.springframework.org/schema/util\" " + //
				"xsi:schemaLocation=\"" + //
				"</beans>";
		assertFormat(content, content, settings);

		content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans " + //
				"xmlns=\"http://www.springframework.org/schema/beans\" " + //
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + //
				"xmlns:util=\"http://www.springframework.org/schema/util\" " + //
				"xsi:schemaLocation=\"\"" + //
				"</beans>";
		assertFormat(content, content, settings);

		content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans " + //
				"xmlns=\"http://www.springframework.org/schema/beans\" " + //
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + //
				"xmlns:util=\"http://www.springframework.org/schema/util\" " + //
				"xsi:schemaLocation=";
		assertFormat(content, content, settings);

		content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans " + //
				"xmlns=\"http://www.springframework.org/schema/beans\" " + //
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + //
				"xmlns:util=\"http://www.springframework.org/schema/util\" " + //
				"xsi:schemaLocation=\"";
		assertFormat(content, content, settings);

		content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans " + //
				"xmlns=\"http://www.springframework.org/schema/beans\" " + //
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + //
				"xmlns:util=\"http://www.springframework.org/schema/util\" " + //
				"xsi:schemaLocation=\"\"";
		assertFormat(content, content, settings);
	}

	@Test
	public void xsiSchemaLocationSplitOnElementWithTabs() throws BadLocationException {
		SharedSettings settings = createSettings();
		settings.getFormattingSettings().setTabSize(4);
		settings.getFormattingSettings().setInsertSpaces(false);
		settings.getFormattingSettings().setPreserveAttributeLineBreaks(false);
		XSISchemaLocationSplit.setSplit(XSISchemaLocationSplit.onElement, settings.getFormattingSettings());
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"    xsi:schemaLocation=\"\r\n" + //
				"        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"        http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd\">\r\n"
				+ //
				"\r\n" + //
				"</beans>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans\r\n"
				+ //
				"																				 http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"																				 http://www.springframework.org/schema/util\r\n"
				+ //
				"																				 http://www.springframework.org/schema/util/spring-util.xsd\">\r\n"
				+ //
				"\r\n" + //
				"</beans>";
		assertFormat(content, expected, settings,
				te(1, 6, 2, 4, " "), //
				te(2, 57, 3, 4, " "), //
				te(3, 24, 4, 8, ""), //
				te(4, 51, 4, 52, "\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t "), //
				te(4, 112, 5, 8, "\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t "), //
				te(5, 50, 5, 51, "\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t "));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void xsiSchemaLocationSplitOnElementWithTabsWithSplitAttribute() throws BadLocationException {
		SharedSettings settings = createSettings();
		settings.getFormattingSettings().setTabSize(4);
		settings.getFormattingSettings().setInsertSpaces(false);
		settings.getFormattingSettings().setSplitAttributes(true);
		XSISchemaLocationSplit.setSplit(XSISchemaLocationSplit.onElement, settings.getFormattingSettings());
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"    xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
				"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"    xmlns:util=\"http://www.springframework.org/schema/util\"\r\n" + //
				"    xsi:schemaLocation=\"\r\n" + //
				"        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"        http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd\">\r\n"
				+ //
				"\r\n" + //
				"</beans>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"		xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
				"		xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"		xmlns:util=\"http://www.springframework.org/schema/util\"\r\n" + //
				"		xsi:schemaLocation=\"http://www.springframework.org/schema/beans\r\n" + //
				"							http://www.springframework.org/schema/beans/spring-beans.xsd\r\n" + //
				"							http://www.springframework.org/schema/util\r\n" + //
				"							http://www.springframework.org/schema/util/spring-util.xsd\">\r\n" + //
				"\r\n" + //
				"</beans>";
		assertFormat(content, expected, settings,
				te(1, 6, 2, 4, "\r\n\t\t"), //
				te(2, 55, 3, 4, "\r\n\t\t"), //
				te(3, 57, 4, 4, "\r\n\t\t"), //
				te(4, 59, 5, 4, "\r\n\t\t"), //
				te(5, 24, 6, 8, ""), //
				te(6, 51, 6, 52, "\r\n\t\t\t\t\t\t\t"), //
				te(6, 112, 7, 8, "\r\n\t\t\t\t\t\t\t"), //
				te(7, 50, 7, 51, "\r\n\t\t\t\t\t\t\t"));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void xsiSchemaLocationSplitOnElementWithTabsWithPreserveLineBreaksOnSome() throws BadLocationException {
		SharedSettings settings = createSettings();
		settings.getFormattingSettings().setTabSize(4);
		settings.getFormattingSettings().setInsertSpaces(false);
		settings.getFormattingSettings().setPreserveAttributeLineBreaks(true);
		XSISchemaLocationSplit.setSplit(XSISchemaLocationSplit.onElement, settings.getFormattingSettings());
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"    xmlns=\"http://www.springframework.org/schema/beans\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ //
				"    xmlns:util=\"http://www.springframework.org/schema/util\" xsi:schemaLocation=\"\r\n" + //
				"        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"        http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd\">\r\n"
				+ //
				"\r\n" + //
				"</beans>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"	xmlns=\"http://www.springframework.org/schema/beans\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ //
				"	xmlns:util=\"http://www.springframework.org/schema/util\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans\r\n"
				+ //
				"																				http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"																				http://www.springframework.org/schema/util\r\n"
				+ //
				"																				http://www.springframework.org/schema/util/spring-util.xsd\">\r\n"
				+ //
				"\r\n" + //
				"</beans>";
		assertFormat(content, expected, settings,
				te(1, 6, 2, 4, "\r\n\t"), //
				te(2, 109, 3, 4, "\r\n\t"), //
				te(3, 80, 4, 8, ""), //
				te(4, 51, 4, 52, "\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"), //
				te(4, 112, 5, 8, "\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"), //
				te(5, 50, 5, 51, "\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void xsiSchemaLocationSplitOnElementWithTabsWithPreserveLineBreaksWithDiffIndent()
			throws BadLocationException {
		SharedSettings settings = createSettings();
		settings.getFormattingSettings().setTabSize(4);
		settings.getFormattingSettings().setInsertSpaces(false);
		settings.getFormattingSettings().setPreserveAttributeLineBreaks(true);
		XSISchemaLocationSplit.setSplit(XSISchemaLocationSplit.onElement, settings.getFormattingSettings());
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"      xmlns=\"http://www.springframework.org/schema/beans\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ //
				"      xmlns:util=\"http://www.springframework.org/schema/util\" xsi:schemaLocation=\"\r\n" + //
				"        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"        http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd\">\r\n"
				+ //
				"\r\n" + //
				"</beans>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"	xmlns=\"http://www.springframework.org/schema/beans\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ //
				"	xmlns:util=\"http://www.springframework.org/schema/util\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans\r\n"
				+ //
				"																				http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"																				http://www.springframework.org/schema/util\r\n"
				+ //
				"																				http://www.springframework.org/schema/util/spring-util.xsd\">\r\n"
				+ //
				"\r\n" + //
				"</beans>";
		assertFormat(content, expected, settings,
				te(1, 6, 2, 6, "\r\n\t"), //
				te(2, 111, 3, 6, "\r\n\t"), //
				te(3, 82, 4, 8, ""), //
				te(4, 51, 4, 52, "\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"), //
				te(4, 112, 5, 8, "\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"), //
				te(5, 50, 5, 51, "\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void xsiSchemaLocationSplitOnElementWithTabsWithPreserveLineBreaksWithMultiTabIndent()
			throws BadLocationException {
		SharedSettings settings = createSettings();
		settings.getFormattingSettings().setTabSize(4);
		settings.getFormattingSettings().setInsertSpaces(false);
		settings.getFormattingSettings().setPreserveAttributeLineBreaks(true);
		XSISchemaLocationSplit.setSplit(XSISchemaLocationSplit.onElement, settings.getFormattingSettings());
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"        xmlns=\"http://www.springframework.org/schema/beans\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ //
				"        xmlns:util=\"http://www.springframework.org/schema/util\" xsi:schemaLocation=\"\r\n" + //
				"        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"        http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd\">\r\n"
				+ //
				"\r\n" + //
				"</beans>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"	xmlns=\"http://www.springframework.org/schema/beans\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
				+ //
				"	xmlns:util=\"http://www.springframework.org/schema/util\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans\r\n"
				+ //
				"																				http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"																				http://www.springframework.org/schema/util\r\n"
				+ //
				"																				http://www.springframework.org/schema/util/spring-util.xsd\">\r\n"
				+ //
				"\r\n" + //
				"</beans>";
		assertFormat(content, expected, settings,
				te(1, 6, 2, 8, "\r\n\t"), //
				te(2, 113, 3, 8, "\r\n\t"), //
				te(3, 84, 4, 8, ""), //
				te(4, 51, 4, 52, "\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"), //
				te(4, 112, 5, 8, "\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"), //
				te(5, 50, 5, 51, "\r\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"));
		assertFormat(expected, expected, settings);
	}

	@Test
	public void xsiSchemaLocationSplitOnElementWithTabsWithPreserveLineBreaks() throws BadLocationException {
		SharedSettings settings = createSettings();
		settings.getFormattingSettings().setTabSize(4);
		settings.getFormattingSettings().setInsertSpaces(false);
		settings.getFormattingSettings().setPreserveAttributeLineBreaks(true);
		XSISchemaLocationSplit.setSplit(XSISchemaLocationSplit.onElement, settings.getFormattingSettings());
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"    xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
				"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"    xmlns:util=\"http://www.springframework.org/schema/util\"\r\n" + //
				"    xsi:schemaLocation=\"\r\n" + //
				"        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\r\n"
				+ //
				"        http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd\">\r\n"
				+ //
				"\r\n" + //
				"</beans>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"	xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"	xmlns:util=\"http://www.springframework.org/schema/util\"\r\n" + //
				"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans\r\n" + //
				"						http://www.springframework.org/schema/beans/spring-beans.xsd\r\n" + //
				"						http://www.springframework.org/schema/util\r\n" + //
				"						http://www.springframework.org/schema/util/spring-util.xsd\">\r\n" + //
				"\r\n" + //
				"</beans>";
		assertFormat(content, expected, settings,
				te(1, 6, 2, 4, "\r\n\t"), //
				te(2, 55, 3, 4, "\r\n\t"), //
				te(3, 57, 4, 4, "\r\n\t"), //
				te(4, 59, 5, 4, "\r\n\t"), //
				te(5, 24, 6, 8, ""), //
				te(6, 51, 6, 52, "\r\n\t\t\t\t\t\t"), //
				te(6, 112, 7, 8, "\r\n\t\t\t\t\t\t"), //
				te(7, 50, 7, 51, "\r\n\t\t\t\t\t\t"));
		assertFormat(expected, expected, settings);
	}

	private static void assertFormat(String unformatted, String expected, SharedSettings sharedSettings,
			TextEdit... expectedEdits) throws BadLocationException {
		assertFormat(unformatted, expected, sharedSettings, "test://test.html", expectedEdits);
	}

	private static void assertFormat(String unformatted, String expected, SharedSettings sharedSettings, String uri,
			TextEdit... expectedEdits) throws BadLocationException {
		assertFormat(unformatted, expected, sharedSettings, uri, true, expectedEdits);
	}

	private static void assertFormat(String unformatted, String expected, SharedSettings sharedSettings, String uri,
			Boolean considerRangeFormat, TextEdit... expectedEdits) throws BadLocationException {
		// Force to "experimental" formatter
		sharedSettings.getFormattingSettings().setExperimental(true);
		XMLAssert.assertFormat(null, unformatted, expected, sharedSettings, uri, considerRangeFormat, expectedEdits);
	}

	private static SharedSettings createSettings() {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setInsertSpaces(true);
		settings.getFormattingSettings().setTabSize(2);
		settings.getFormattingSettings().setPreserveAttributeLineBreaks(true);
		// settings.getFormattingSettings().setPreserveEmptyContent(true);
		// Force to "experimental" formatter
		settings.getFormattingSettings().setExperimental(true);
		return settings;
	}
}
