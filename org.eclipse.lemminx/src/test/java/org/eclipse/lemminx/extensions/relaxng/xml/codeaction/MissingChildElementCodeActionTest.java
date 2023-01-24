/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.relaxng.xml.codeaction;

import static org.eclipse.lemminx.XMLAssert.ca;
import static org.eclipse.lemminx.XMLAssert.d;
import static org.eclipse.lemminx.XMLAssert.te;
import static org.eclipse.lemminx.XMLAssert.testCodeActionsFor;
import static org.eclipse.lemminx.XMLAssert.testDiagnosticsFor;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.extensions.relaxng.xml.validator.RelaxNGErrorCode;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.Diagnostic;
import org.junit.jupiter.api.Test;

/**
 * Test for generating missing child elements for RELAX NG
 *
 */
public class MissingChildElementCodeActionTest extends AbstractCacheBasedTest {

	@Test
	public void incomplete_element_required_element_missing_only_root() throws Exception {
		String xml = "<?xml-model href=\"src/test/resources/relaxng/addressBook_v1.rng\" ?>\r\n" + //
				"<addressBook>\r\n" + //
				"</addressBook>";
		Diagnostic d = d(1, 1, 1, 12, RelaxNGErrorCode.incomplete_element_required_element_missing);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d,
				ca(d, te(1, 13, 2, 0, //
						"\r\n" + //
								"\t<card>\r\n" + //
								"\t\t<name></name>\r\n" + //
								"\t\t<email></email>\r\n" + //
								"\t</card>\r\n")),
				ca(d, te(1, 13, 2, 0, //
						"\r\n" + //
								"\t<card>\r\n" + //
								"\t\t<name></name>\r\n" + //
								"\t\t<email></email>\r\n" + //
								"\t</card>\r\n")));
	}

	@Test
	public void incomplete_element_required_element_missing_simple() throws Exception {
		String xml = "<?xml-model href=\"src/test/resources/relaxng/addressBook_v1.rng\" ?>\r\n" + //
				"<addressBook>\r\n" + //
				"	<card>\r\n" + //
				"	</card>\r\n" + //
				"</addressBook>";
		Diagnostic d = d(2, 2, 2, 6, RelaxNGErrorCode.incomplete_element_required_element_missing);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d,
				ca(d, te(2, 7, 3, 1, //
						"\r\n" + //
								"\t\t<name></name>\r\n" + //
								"\t\t<email></email>\r\n\t")),
				ca(d, te(2, 7, 3, 1, //
						"\r\n" + //
								"\t\t<name></name>\r\n" + //
								"\t\t<email></email>\r\n\t")));
	}

	@Test
	public void incomplete_element_required_element_missing_newlines() throws Exception {
		String xml = "<?xml-model href=\"src/test/resources/relaxng/addressBook_v1.rng\" ?>\r\n" + //
				"<addressBook>\r\n" + //
				"	<card>\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"	</card>\r\n" + //
				"</addressBook>";
		Diagnostic d = d(2, 2, 2, 6, RelaxNGErrorCode.incomplete_element_required_element_missing);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d,
				ca(d, te(2, 7, 7, 1, //
						"\r\n" + //
								"\t\t<name></name>\r\n" + //
								"\t\t<email></email>\r\n\t")),
				ca(d, te(2, 7, 7, 1, //
						"\r\n" + //
								"\t\t<name></name>\r\n" + //
								"\t\t<email></email>\r\n\t")));
	}

	@Test
	public void incomplete_element_required_element_missing_with_existing() throws Exception {
		String xml = "<?xml-model href=\"src/test/resources/relaxng/addressBook_v1.rng\" ?>\r\n" + //
				"<addressBook>\r\n" + //
				"	<card>\r\n" + //
				"		<name></name>\r\n" + //
				"	</card>\r\n" + //
				"</addressBook>";
		Diagnostic d = d(2, 2, 2, 6, RelaxNGErrorCode.incomplete_element_required_element_missing);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d,
				ca(d, te(2, 7, 4, 1, //
						"\r\n" + //
								"\t\t<name></name>\r\n" + //
								"\t\t<email></email>\r\n\t")),
				ca(d, te(2, 7, 4, 1, //
						"\r\n" + //
								"\t\t<name></name>\r\n" + //
								"\t\t<email></email>\r\n\t")));
	}

	@Test
	public void incomplete_element_required_element_missing_with_ref() throws Exception {
		String xml = "<?xml-model href=\"src/test/resources/relaxng/addressBook_v2.rng\" ?>\r\n" + //
				"<addressBook>\r\n" + //
				"	<card>\r\n" + //
				"	</card>\r\n" + //
				"</addressBook>";
		Diagnostic d = d(2, 2, 2, 6, RelaxNGErrorCode.incomplete_element_required_element_missing);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d,
				ca(d, te(2, 7, 3, 1, //
						"\r\n" + //
								"\t\t<name></name>\r\n" + //
								"\t\t<email></email>\r\n\t")),
				ca(d, te(2, 7, 3, 1, //
						"\r\n" + //
								"\t\t<name></name>\r\n" + //
								"\t\t<email></email>\r\n\t")));
	}

	@Test
	public void incomplete_element_required_element_missing_TEI() throws Exception {
		String xml = "<?xml-model href=\"src/test/resources/relaxng/tei_all.rng\" ?>\r\n" + //
				"<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">\r\n" + //
				"</TEI>";
		Diagnostic d = d(1, 1, 1, 4, RelaxNGErrorCode.incomplete_element_required_element_missing);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, 0,
				ca(d, te(1, 41, 2, 0, //
						"\r\n" + //
								"\t<teiHeader>\r\n" + //
								"\t\t<fileDesc>\r\n" + //
								"\t\t\t<titleStmt>\r\n" + //
								"\t\t\t\t<title></title>\r\n" + //
								"\t\t\t</titleStmt>\r\n" + //
								"\t\t\t<publicationStmt>\r\n" + //
								"\t\t\t\t<publisher></publisher>\r\n" + //
								"\t\t\t</publicationStmt>\r\n" + //
								"\t\t\t<sourceDesc>\r\n" + //
								"\t\t\t\t<p></p>\r\n" + //
								"\t\t\t</sourceDesc>\r\n" + //
								"\t\t</fileDesc>\r\n" + //
								"\t</teiHeader>\r\n" + //
								"\t<text>\r\n" + //
								"\t\t<body>\r\n" + //
								"\t\t\t<div></div>\r\n" + //
								"\t\t</body>\r\n" + //
								"\t</text>\r\n")));
	}

	@Test
	public void incomplete_element_required_element_missing_optional_element() throws Exception {
		String xml = "<?xml-model href=\"src/test/resources/relaxng/addressBook_v4.rng\" ?>\r\n" + //
				"<addressBook>\r\n" + //
				"	<card>\r\n" + //
				"	</card>\r\n" + //
				"</addressBook>";
		Diagnostic d = d(2, 2, 2, 6, RelaxNGErrorCode.incomplete_element_required_element_missing);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d,
				// insert only required - emailContent is optional in schema - indicated by
				// <optional>
				ca(d, te(2, 7, 3, 1, //
						"\r\n" + //
								"\t\t<name></name>\r\n" + //
								"\t\t<email>\r\n" + //
								"\t\t\t<emailContent></emailContent>\r\n" + //
								"\t\t</email>\r\n\t")),
				ca(d, te(2, 7, 3, 1, //
						"\r\n" + //
								"\t\t<name></name>\r\n" + //
								"\t\t<email>\r\n" + //
								"\t\t\t<emailContent></emailContent>\r\n" + //
								"\t\t\t<emailOptional></emailOptional>\r\n" + //
								"\t\t</email>\r\n\t")));
	}

	@Test
	public void incomplete_element_required_element_missing_optional_element_zeroOrMore() throws Exception {
		String xml = "<?xml-model href=\"src/test/resources/relaxng/addressBook_v5.rng\" ?>\r\n" + //
				"<addressBook>\r\n" + //
				"	<card>\r\n" + //
				"	</card>\r\n" + //
				"</addressBook>";
		Diagnostic d = d(2, 2, 2, 6, RelaxNGErrorCode.incomplete_element_required_element_missing);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d,
				// insert only required - emailContent is optional in schema - indicated by
				// <zeroOrMore>
				ca(d, te(2, 7, 3, 1, //
						"\r\n" + //
								"\t\t<name></name>\r\n" + //
								"\t\t<email>\r\n" + //
								"\t\t\t<emailContent></emailContent>\r\n" + //
								"\t\t</email>\r\n\t")),
				ca(d, te(2, 7, 3, 1, //
						"\r\n" + //
								"\t\t<name></name>\r\n" + //
								"\t\t<email>\r\n" + //
								"\t\t\t<emailContent></emailContent>\r\n" + //
								"\t\t\t<emailOptional></emailOptional>\r\n" + //
								"\t\t</email>\r\n\t")));
	}

	//https://github.com/eclipse/lemminx/issues/1458
	@Test
	public void incomplete_element_required_element_missing_optional_element_zeroOrMore_autoCloseTags()
			throws Exception {
		String xml = "<?xml-model href=\"src/test/resources/relaxng/addressBook_v5.rng\" ?>\r\n" + //
				"<addressBook>\r\n" + //
				"	<card>\r\n" + //
				"	</card>\r\n" + //
				"</addressBook>";
		Diagnostic d = d(2, 2, 2, 6, RelaxNGErrorCode.incomplete_element_required_element_missing);
		SharedSettings settings = new SharedSettings();
		settings.getCompletionSettings().setAutoCloseTags(false);
		settings.getFormattingSettings().setTabSize(4);
		settings.getFormattingSettings().setInsertSpaces(false);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, settings,
				// insert only required - emailContent is optional in schema - indicated by
				// <zeroOrMore>
				ca(d, te(2, 7, 3, 1, //
						"\r\n" + //
								"\t\t<name></name>\r\n" + //
								"\t\t<email>\r\n" + //
								"\t\t\t<emailContent></emailContent>\r\n" + //
								"\t\t</email>\r\n\t")),
				ca(d, te(2, 7, 3, 1, //
						"\r\n" + //
								"\t\t<name></name>\r\n" + //
								"\t\t<email>\r\n" + //
								"\t\t\t<emailContent></emailContent>\r\n" + //
								"\t\t\t<emailOptional></emailOptional>\r\n" + //
								"\t\t</email>\r\n\t")));
	}

	@Test
	public void incomplete_element_required_element_missing_choice() throws Exception {
		String xml = "<?xml-model href=\"src/test/resources/relaxng/article.rng\" ?>\r\n" + //
				"<article>\r\n" + //
				"</article>";
		Diagnostic d = d(1, 1, 1, 8, RelaxNGErrorCode.incomplete_element_required_element_missing);
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d,
				ca(d, te(1, 9, 2, 0, //
						"\r\n" + //
								"\t<title></title>\r\n")),
				// Generate all elements will generate duplicate elemenst in this case. Needs
				// fix in future.
				ca(d, te(1, 9, 2, 0, //
						"\r\n" + //
								"\t<title></title>\r\n" + //
								"\t<title>\r\n" + //
								"\t\t<line></line>\r\n" + //
								"\t</title>\r\n")));
	}
}
