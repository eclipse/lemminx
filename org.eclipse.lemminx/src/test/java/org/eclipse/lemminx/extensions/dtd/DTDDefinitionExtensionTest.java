/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.dtd;

import static org.eclipse.lemminx.XMLAssert.ll;
import static org.eclipse.lemminx.XMLAssert.r;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.junit.jupiter.api.Test;

/**
 * DTD definition tests.
 *
 * @author Angelo ZERR
 */
public class DTDDefinitionExtensionTest extends AbstractCacheBasedTest {

	@Test
	public void noDefinition() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<!DOCTYPE note [\r\n" + //
				"	<!EL|EMENT note (to,from,heading,body)>\r\n" + // <-- no definition for <!ELEMENT
				"	<!ELEMENT to (#PCDATA)>\r\n" + //
				"	<!ELEMENT from (#PCDATA)>\r\n" + //
				"	<!ELEMENT heading (#PCDATA)>\r\n" + //
				"	<!ELEMENT body (#PCDATA)>\r\n" + //
				"	<!ATTLIST note version CDATA #REQUIRED>\r\n" + //
				"]>\r\n" + //
				"<note version=\"0.9.0\">";
		XMLAssert.testDefinitionFor(xml, "test.xml");
	}

	@Test
	public void noDefinition2() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<!DOCTYPE note [\r\n" + //
				"	<!ELEMENT note (to,from,heading,body)|>\r\n" + // <-- no definition for )>
				"	<!ELEMENT to (#PCDATA)>\r\n" + //
				"	<!ELEMENT from (#PCDATA)>\r\n" + //
				"	<!ELEMENT heading (#PCDATA)>\r\n" + //
				"	<!ELEMENT body (#PCDATA)>\r\n" + //
				"	<!ATTLIST note version CDATA #REQUIRED>\r\n" + //
				"]>\r\n" + //
				"<note version=\"0.9.0\">";
		XMLAssert.testDefinitionFor(xml, "test.xml");
	}

	@Test
	public void fromElementParameterToElementInsideDOCTYPE() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<!DOCTYPE note [\r\n" + //
				"	<!ELEMENT note (to,from,hea|ding,body)>\r\n" + // <-- definition for heading
				"	<!ELEMENT to (#PCDATA)>\r\n" + //
				"	<!ELEMENT from (#PCDATA)>\r\n" + //
				"	<!ELEMENT heading (#PCDATA)>\r\n" + //
				"	<!ELEMENT body (#PCDATA)>\r\n" + //
				"	<!ATTLIST note version CDATA #REQUIRED>\r\n" + //
				"]>\r\n" + //
				"<note version=\"0.9.0\">";
		XMLAssert.testDefinitionFor(xml, "test.xml", ll("test.xml", r(2, 25, 2, 32), r(5, 11, 5, 18)));
	}

	@Test
	public void specialCharacter() throws BadLocationException {
		// an element name can be have '-'
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<!DOCTYPE note [\r\n" + //
				"	<!ELEMENT note (to,from,fo|nt-face,body)>\r\n" + // <-- definition for font-face
				"	<!ELEMENT to (#PCDATA)>\r\n" + //
				"	<!ELEMENT from (#PCDATA)>\r\n" + //
				"	<!ELEMENT font-face (#PCDATA)>\r\n" + //
				"	<!ELEMENT body (#PCDATA)>\r\n" + //
				"	<!ATTLIST note version CDATA #REQUIRED>\r\n" + //
				"]>\r\n" + //
				"<note version=\"0.9.0\">";
		XMLAssert.testDefinitionFor(xml, "test.xml", ll("test.xml", r(2, 25, 2, 34), r(5, 11, 5, 20)));
	}

	@Test
	public void fromAttrToElementInsideDOCTYPE() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<!DOCTYPE note [\r\n" + //
				"	<!ELEMENT note (to,from,heading,body)>\r\n" + //
				"	<!ELEMENT to (#PCDATA)>\r\n" + //
				"	<!ELEMENT from (#PCDATA)>\r\n" + //
				"	<!ELEMENT heading (#PCDATA)>\r\n" + //
				"	<!ELEMENT body (#PCDATA)>\r\n" + //
				"	<!ATTLIST n|ote version CDATA #REQUIRED>\r\n" + // <-- definition for note
				"]>\r\n" + //
				"<note version=\"0.9.0\">";
		XMLAssert.testDefinitionFor(xml, "test.xml", ll("test.xml", r(7, 11, 7, 15), r(2, 11, 2, 15)));
	}

	@Test
	public void fromElementParameterToElementInsideDTD() throws BadLocationException {
		String xml = "<!ELEMENT note (to,from,hea|ding,body)>\r\n" + // <-- definition for heading
				"<!ELEMENT to (#PCDATA)>\r\n" + //
				"<!ELEMENT from (#PCDATA)>\r\n" + //
				"<!ELEMENT heading (#PCDATA)>\r\n" + //
				"<!ELEMENT body (#PCDATA)>\r\n" + //
				"<!ATTLIST note version CDATA #REQUIRED>";
		XMLAssert.testDefinitionFor(xml, "test.dtd", ll("test.dtd", r(0, 24, 0, 31), r(3, 10, 3, 17)));
	}

	@Test
	public void fromAttrToElementInsideDTD() throws BadLocationException {
		String xml = "<!ELEMENT note (to,from,heading,body)>\r\n" + //
				"<!ELEMENT to (#PCDATA)>\r\n" + //
				"<!ELEMENT from (#PCDATA)>\r\n" + //
				"<!ELEMENT heading (#PCDATA)>\r\n" + //
				"<!ELEMENT body (#PCDATA)>\r\n" + //
				"<!ATTLIST n|ote version CDATA #REQUIRED>"; // <-- definition for note
		XMLAssert.testDefinitionFor(xml, "test.dtd", ll("test.dtd", r(5, 10, 5, 14), r(0, 10, 0, 14)));
	}
}
