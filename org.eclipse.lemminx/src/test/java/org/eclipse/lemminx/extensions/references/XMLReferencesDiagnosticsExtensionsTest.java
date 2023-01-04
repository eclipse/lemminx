/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.references;

import static org.eclipse.lemminx.XMLAssert.d;

import java.util.function.Consumer;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.XMLAssert.SettingsSaveContext;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.references.participants.XMLReferencesErrorCode;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

/**
 * XML references validation tests.
 *
 */
public class XMLReferencesDiagnosticsExtensionsTest extends AbstractCacheBasedTest {

	@Test
	public void undefinedReferences() throws BadLocationException {
		String xml = "<aaa ref=\"child1 badChild1 child2 badChild2 child3 badChild3\">\r\n"
				+ "  <bbb>child1</bbb>\r\n"
				+ "  <bbb>child2</bbb>\r\n"
				+ "  <bbb>child3</bbb>\r\n"
				+ "</aaa>";
		testDiagnosticsFor(xml, "file:///test/attr-to-text.xml", //
				d(0, 17, 0, 26, XMLReferencesErrorCode.UndefinedReference,
						"Undefined reference 'badChild1': nothing that matches the expression 'bbb/text()' defines 'badChild1'.",
						"xml", DiagnosticSeverity.Warning),
				d(0, 34, 0, 43, XMLReferencesErrorCode.UndefinedReference,
						"Undefined reference 'badChild2': nothing that matches the expression 'bbb/text()' defines 'badChild2'.",
						"xml", DiagnosticSeverity.Warning),
				d(0, 51, 0, 60, XMLReferencesErrorCode.UndefinedReference,
						"Undefined reference 'badChild3': nothing that matches the expression 'bbb/text()' defines 'badChild3'.",
						"xml", DiagnosticSeverity.Warning));
	}

	@Test
	public void invalidPrefix() throws BadLocationException {
		String xml = "<link target=\"#A B #C\" />";
		testDiagnosticsFor(xml, "file:///test/tei.xml", //
				d(0, 14, 0, 16, XMLReferencesErrorCode.UndefinedReference,
						"Undefined reference '#A': nothing that matches the expression '@xml:id' defines 'A'.",
						"xml", DiagnosticSeverity.Warning),
				d(0, 17, 0, 18, XMLReferencesErrorCode.InvalidPrefix,
						"Invalid reference 'B': references to declarations that match the expression '@xml:id' require the '#' prefix.",
						"xml", DiagnosticSeverity.Warning),
				d(0, 19, 0, 21, XMLReferencesErrorCode.UndefinedReference,
						"Undefined reference '#C': nothing that matches the expression '@xml:id' defines 'C'.",
						"xml", DiagnosticSeverity.Warning));
	}
	

	@Test
	public void noUndefinedReferences() throws BadLocationException {
		String xml = "<aaa ref=\"chi|ld1 badChild1 child2 badChild2 child3 badChild3\">\r\n"
				+ "  <bbb>child1</bbb>\r\n"
				+ "  <bbb>child2</bbb>\r\n"
				+ "  <bbb>child3</bbb>\r\n"
				+ "</aaa>";
		testDiagnosticsFor(xml, "file:///test/foo.xml");
	}

	public static void testDiagnosticsFor(String xml, String fileURI,
			Diagnostic... expected) {
		Consumer<XMLLanguageService> config = ls -> {
			ls.doSave(new SettingsSaveContext(XMLReferencesSettingsForTest.createXMLReferencesSettings()));
		};
		XMLAssert.testDiagnosticsFor(xml, null, config, fileURI, false, expected);
	}
}
