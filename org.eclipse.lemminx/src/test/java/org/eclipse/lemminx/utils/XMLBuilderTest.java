/**
 *  Copyright (c) 2018 Angelo ZERR.
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
package org.eclipse.lemminx.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.lemminx.settings.XMLFormattingOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * XML builder test.
 *
 */
public class XMLBuilderTest {

	XMLFormattingOptions settings;

	@BeforeEach
	public void startup() {
		settings = new XMLFormattingOptions(4, false);
	}

	@Test
	public void simple() throws Exception {
		String xml = new XMLBuilder(settings, "", "\n").startElement("html", true).endElement("html").toString();
		assertEquals("<html></html>", xml);
	}

	@Test
	public void simpleLinefeed() throws Exception {
		String xml = new XMLBuilder(settings, "", "\n").startElement("html", true).linefeed().endElement("html")
				.toString();
		assertEquals("<html>\n</html>", xml);
	}

	@Test
	public void elementWithChild() throws Exception {
		String xml = new XMLBuilder(settings, "", "\n").startElement("html", true).linefeed().indent(1)
				.startElement("head", false).selfCloseElement().linefeed().endElement("html").toString();
		assertEquals("<html>\n\t<head />\n</html>", xml);
	}
}
