/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
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

import static org.eclipse.lemminx.XMLAssert.cl;
import static org.eclipse.lemminx.XMLAssert.r;
import static org.eclipse.lemminx.XMLAssert.testCodeLensFor;
import static org.eclipse.lemminx.client.ClientCommands.OPEN_BINDING_WIZARD;

import java.util.Collections;

import org.eclipse.lemminx.client.CodeLensKind;
import org.eclipse.lemminx.commons.BadLocationException;
import org.junit.jupiter.api.Test;

/**
 * Associate grammar codelens tests
 *
 */
public class AssociateGrammarCodeLensExtensionsTest {

	@Test
	public void noGrammarWithAssociationSupportInXML() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<foo />";
		testCodeLensFor(xml, "test.xml", //
				Collections.singletonList(CodeLensKind.Association), //
				cl(r(1, 1, 1, 4), "Bind to grammar/schema...", OPEN_BINDING_WIZARD));
	}

	@Test
	public void noGrammarWithAssociationSupportInXSD() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<schema />";
		testCodeLensFor(xml, "test.xsd", //
				Collections.singletonList(CodeLensKind.Association));
	}

	@Test
	public void noGrammarWithAssociationSupportInDTD() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<foo />";
		testCodeLensFor(xml, "test.dtd", //
				Collections.singletonList(CodeLensKind.Association));
	}

	@Test
	public void noGrammarWithoutAssociationSupport() throws BadLocationException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<foo />";
		testCodeLensFor(xml, "test.xml", //
				Collections.emptyList());
	}

	@Test
	public void withGrammar() throws BadLocationException {
		String xml = "<?xml-model href='test.dtd' >\r\n" + //
				"<foo />";
		testCodeLensFor(xml, "test.xml");
	}

}
