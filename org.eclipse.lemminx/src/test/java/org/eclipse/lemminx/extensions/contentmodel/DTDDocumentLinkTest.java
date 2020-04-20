/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.contentmodel;

import static org.eclipse.lemminx.XMLAssert.dl;
import static org.eclipse.lemminx.XMLAssert.r;

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.junit.jupiter.api.Test;

/**
 * DTD document links tests
 *
 */
public class DTDDocumentLinkTest {

	@Test
	public void docTypeSYSTEM() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<!DOCTYPE root-element SYSTEM \"../dtd/base.dtd\" [\r\n" + //
				"\r\n" + //
				"]>\r\n" + //
				"<root-element />";
		XMLAssert.testDocumentLinkFor(xml, "src/test/resources/xml/base.xml",
				dl(r(1, 31, 1, 46), "src/test/resources/dtd/base.dtd"));
	}

	@Test
	public void docTypePUBLIC() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<!DOCTYPE root-element PUBLIC \"ABCD\" \"../dtd/base.dtd\" [\r\n" + //
				"\r\n" + //
				"]>\r\n" + //
				"<root-element />";
		XMLAssert.testDocumentLinkFor(xml, "src/test/resources/xml/base.xml",
				dl(r(1, 38, 1, 53), "src/test/resources/dtd/base.dtd"));
	}
	
	@Test
	public void noLinks() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<!DOCTYPE root-element \"ABCD\" \"../dtd/base.dtd\" [\r\n" + // here it misses PUBLIC
				"\r\n" + //
				"]>\r\n" + //
				"<root-element />";
		XMLAssert.testDocumentLinkFor(xml, "src/test/resources/xml/base.xml");
	}
}
