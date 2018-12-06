/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.contentmodel;

import static org.eclipse.lsp4xml.XMLAssert.r;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.ContentModelSettings;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.XMLValidationSettings;
import org.junit.Test;

/**
 * XML problems like noGrammar.
 *
 */
public class XMLProblemsTest {

	@Test
	public void noGrammarIgnore() throws BadLocationException {
		String xml = "<root /> ";
		XMLAssert.testDiagnosticsFor(xml);
	}

	@Test
	public void noGrammarHint() throws BadLocationException {
		String xml = "<root /> ";
		// Set noGrammar has 'hint'
		XMLAssert.testDiagnosticsFor(xml, null, ls -> {
			ContentModelSettings settings = new ContentModelSettings();
			XMLValidationSettings problems = new XMLValidationSettings();
			problems.setNoGrammar("hint");
			settings.setValidation(problems);
			ls.doSave(new XMLAssert.SettingsSaveContext(settings));
		}, null, false, new Diagnostic(r(0, 1, 0, 5), "No grammar constraints (DTD or XML Schema).",
				DiagnosticSeverity.Hint, "test.xml", "XML"));
	}
}
