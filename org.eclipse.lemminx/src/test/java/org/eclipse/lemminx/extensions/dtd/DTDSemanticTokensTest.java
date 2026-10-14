/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
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

import static org.eclipse.lemminx.XMLAssert.st;
import static org.eclipse.lemminx.XMLAssert.testSemanticTokensFor;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.settings.capabilities.ServerCapabilitiesConstants;
import org.junit.jupiter.api.Test;

/**
 * Tests for semantic tokens in standalone DTD files (.dtd).
 */
public class DTDSemanticTokensTest extends AbstractCacheBasedTest {

	private static final int TYPE = ServerCapabilitiesConstants.TOKEN_TYPE_TYPE;
	private static final int VARIABLE = ServerCapabilitiesConstants.TOKEN_TYPE_VARIABLE;
	private static final int PROPERTY = ServerCapabilitiesConstants.TOKEN_TYPE_PROPERTY;
	private static final int DECL = ServerCapabilitiesConstants.TOKEN_MODIFIER_DECLARATION;

	@Test
	public void standaloneDTDFile() {
		String dtd = "<!ENTITY myEntity \"value\">\n" + //
				"<!ENTITY otherEntity \"other\">\n" + //
				"<!ELEMENT root (#PCDATA)>";
		testSemanticTokensFor(dtd, "test://test/test.dtd", //
				st(0, 9, 8, VARIABLE, DECL), // 'myEntity'
				st(1, 9, 11, VARIABLE, DECL), // 'otherEntity'
				st(2, 10, 4, TYPE, DECL) // 'root'
		);
	}

	@Test
	public void standaloneDTDAllDeclarations() {
		String dtd = "<!ELEMENT note (to, from, body)>\n" + //
				"<!ATTLIST note priority CDATA #IMPLIED>\n" + //
				"<!ENTITY copyright \"2026 Red Hat\">\n" + //
				"<!NOTATION xml SYSTEM \"text/xml\">";
		testSemanticTokensFor(dtd, "test://test/test.dtd", //
				st(0, 10, 4, TYPE, DECL), // ELEMENT 'note'
				st(1, 10, 4, TYPE, 0), // ATTLIST 'note' (element ref)
				st(1, 15, 8, PROPERTY, DECL), // ATTLIST 'priority' (attr decl)
				st(2, 9, 9, VARIABLE, DECL), // ENTITY 'copyright'
				st(3, 11, 3, TYPE, DECL) // NOTATION 'xml'
		);
	}

	@Test
	public void standaloneDTDNoDeclarations() {
		String dtd = "<!-- just a comment -->";
		testSemanticTokensFor(dtd, "test://test/test.dtd" //
		);
	}
}
