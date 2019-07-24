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

import static org.eclipse.lsp4xml.XMLAssert.l;
import static org.eclipse.lsp4xml.XMLAssert.r;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.junit.Test;

/**
 * DTD references tests
 *
 */
public class DTDReferenceExtensionsTest {

	@Test
	public void referencesOnDTDElementName() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<!DOCTYPE note [\r\n" + //
				"	<!ELEMENT no|te (to,from,heading,body, note?)>\r\n" + // <-- references on <!ELEMENT note
				"	<!ATTLIST note version CDATA #REQUIRED>\r\n" + //
				"]>";
		testReferencesFor(xml, l("test.xml", r(2, 39, 2, 43)), l("test.xml", r(3, 11, 3, 15)));
	}

	@Test
	public void noReferencesAfterElementName() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<!DOCTYPE note [\r\n" + //
				"	<!ELEMENT note (to,from,heading,body, note?|)>\r\n" + // <--after ?, no references
				"	<!ATTLIST note version CDATA #REQUIRED>\r\n" + //
				"]>";
		testReferencesFor(xml);
	}

	@Test
	public void noReferencesAfterDTDAttrListName() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<!DOCTYPE note [\r\n" + //
				"	<!ELEMENT note (to,from,heading,body, note?)>\r\n"
				+ "	<!ATTLIST note version CD|ATA #REQUIRED>\r\n" + // // <-- in CDATA, no references
				"]>";
		testReferencesFor(xml);
	}

	@Test
	public void noReferencesOnDTDAttListName() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<!DOCTYPE note [\r\n" + //
				"	<!ELEMENT note (to,from,heading,body, note?)>\r\n" + //
				"	<!ATTLIST no|te version CDATA #REQUIRED>\r\n" + // <-- no references on <!ATTLIST no|te
				"]>";
		testReferencesFor(xml);
	}

	@Test
	public void noReferencesOnDTDElementChildName() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<!DOCTYPE note [\r\n" + //
				"	<!ELEMENT note (to,from,heading,body, no|te?)>\r\n" + // <-- no references on child (... no|te?
				"	<!ATTLIST note version CDATA #REQUIRED>\r\n" + //
				"]>";
		testReferencesFor(xml);
	}

	private void testReferencesFor(String xml, Location... expectedItems) throws BadLocationException {
		XMLAssert.testReferencesFor(xml, "test.xml", expectedItems);
	}
}
