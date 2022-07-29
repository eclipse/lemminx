/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
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

import static org.eclipse.lemminx.XMLAssert.assertFormat;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.xsi.settings.XSISchemaLocationSplit;
import org.eclipse.lemminx.settings.SharedSettings;
import org.junit.jupiter.api.Test;

/**
 * XSI xsi:schemaLocation formatter tests
 *
 */
public class XSIFormatterTest {

	@Test
	public void xsiSchemaLocationSplitNone() throws BadLocationException {
		// Default
		SharedSettings settings = createSettings();
		XSISchemaLocationSplit.setSplit(XSISchemaLocationSplit.none, settings.getFormattingSettings());
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
				"    xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
				"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"    xmlns:util=\"http://www.springframework.org/schema/util\"\r\n" + //
				"    xsi:schemaLocation=\"http://www.springframework.org/schema/beans\r\n" + //
				"                        http://www.springframework.org/schema/beans/spring-beans.xsd\r\n" + //
				"                        http://www.springframework.org/schema/util\r\n" + //
				"                        http://www.springframework.org/schema/util/spring-util.xsd\">\r\n" + //
				"\r\n" + //
				"</beans>";
		assertFormat(content, expected, settings);
	}

	@Test
	public void xsiSchemaLocationSplitOnPair() throws BadLocationException {
		SharedSettings settings = createSettings();
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
		assertFormat(content, expected, settings);
	}

	@Test
	public void xsiSchemaLocationEmpty() throws BadLocationException {
		SharedSettings settings = createSettings();
		XSISchemaLocationSplit.setSplit(XSISchemaLocationSplit.onElement, settings.getFormattingSettings());
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"    xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
				"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"    xmlns:util=\"http://www.springframework.org/schema/util\"\r\n" + //
				"    xsi:schemaLocation=" + //
				"</beans>";
		assertFormat(content, content, settings);

		content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"    xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
				"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"    xmlns:util=\"http://www.springframework.org/schema/util\"\r\n" + //
				"    xsi:schemaLocation=\"" + //
				"</beans>";
		assertFormat(content, content, settings);
		
		content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans\r\n" + //
				"    xmlns=\"http://www.springframework.org/schema/beans\"\r\n" + //
				"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"    xmlns:util=\"http://www.springframework.org/schema/util\"\r\n" + //
				"    xsi:schemaLocation=\"\"" + //
				"</beans>";
		assertFormat(content, content, settings);
	}

	@Test
	public void xsiSchemaLocationSplitOnElementWithTabs() throws BadLocationException {
		SharedSettings settings = createSettings();
		settings.getFormattingSettings().setTabSize(4);
		settings.getFormattingSettings().setInsertSpaces(false);
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
		assertFormat(content, expected, settings);
	}
	
	private static SharedSettings createSettings() {
		SharedSettings settings = new SharedSettings();
		settings.getFormattingSettings().setInsertSpaces(true);
		settings.getFormattingSettings().setTabSize(2);
		settings.getFormattingSettings().setSplitAttributes(true);
		settings.getFormattingSettings().setPreserveEmptyContent(true);
		return settings;
	}
}
