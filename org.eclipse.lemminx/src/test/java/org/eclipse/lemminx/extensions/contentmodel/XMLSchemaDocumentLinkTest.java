/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.extensions.contentmodel;

import static org.eclipse.lemminx.XMLAssert.dl;
import static org.eclipse.lemminx.XMLAssert.r;

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.junit.Test;

/**
 * XML Schema document links tests
 *
 */
public class XMLSchemaDocumentLinkTest {

	@Test
	public void noNamespaceSchemaLocation() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + //
				"<Configuration xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"xsd/Format.xsd\">\r\n"
				+ //
				"  <ViewDefinitions>\r\n" + //
				"    <View>";
		XMLAssert.testDocumentLinkFor(xml, "src/test/resources/Format.xml",
				dl(r(1, 100, 1, 114), "src/test/resources/xsd/Format.xsd"));
	}
}
