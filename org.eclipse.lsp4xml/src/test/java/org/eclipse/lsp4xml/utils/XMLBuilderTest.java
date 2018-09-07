/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.utils;

import org.eclipse.lsp4xml.settings.XMLFormattingOptions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XML builder test.
 *
 */
public class XMLBuilderTest {

	XMLFormattingOptions settings;

	@Before
	public void startup() {
		settings = new XMLFormattingOptions(4, false);
	}

	@Test
	public void simple() throws Exception {
		String xml = new XMLBuilder(settings, "", "\n").startElement("html", true).endElement("html").toString();
		Assert.assertEquals("<html></html>", xml);
	}

	@Test
	public void simpleLinefeed() throws Exception {
		String xml = new XMLBuilder(settings, "", "\n").startElement("html", true).linefeed().endElement("html")
				.toString();
		Assert.assertEquals("<html>\n</html>", xml);
	}

	@Test
	public void elementWithChild() throws Exception {
		String xml = new XMLBuilder(settings, "", "\n").startElement("html", true).linefeed().indent(1)
				.startElement("head", false).endElement().linefeed().endElement("html").toString();
		Assert.assertEquals("<html>\n\t<head />\n</html>", xml);
	}
}
