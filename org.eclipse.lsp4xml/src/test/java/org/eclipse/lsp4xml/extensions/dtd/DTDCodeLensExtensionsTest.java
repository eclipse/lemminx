/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.extensions.dtd;

import static org.eclipse.lsp4xml.XMLAssert.cl;
import static org.eclipse.lsp4xml.XMLAssert.r;
import static org.eclipse.lsp4xml.client.ClientCommands.SHOW_REFERENCES;

import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.junit.Test;

/**
 * DTD codelens tests
 *
 */
public class DTDCodeLensExtensionsTest {

	@Test
	public void codeLensOnDTDElementInDOCTYPE() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<!DOCTYPE note [\r\n" + //
				"	<!ELEMENT note (to,from,heading,body, note?)>\r\n" + //
				"	<!ELEMENT from (#PCDATA)>\r\n" + //
				"	<!ATTLIST note version CDATA #REQUIRED>\r\n" + //
				"]>";
		XMLAssert.testCodeLensFor(xml, "test.xml", cl(r(2, 11, 2, 15), "2 references", SHOW_REFERENCES),
				cl(r(3, 11, 3, 15), "1 reference", SHOW_REFERENCES));
	}

	@Test
	public void codeLensOnDTDElementInDTD() throws BadLocationException {
		String xml = "<!ELEMENT note (to,from,heading,body, note?)>\r\n" + //
				"	<!ELEMENT from (#PCDATA)>\r\n" + //
				"	<!ATTLIST note version CDATA #REQUIRED>";
		XMLAssert.testCodeLensFor(xml, "test.dtd", cl(r(0, 10, 0, 14), "2 references", SHOW_REFERENCES),
				cl(r(1, 11, 1, 15), "1 reference", SHOW_REFERENCES));
	}

}
