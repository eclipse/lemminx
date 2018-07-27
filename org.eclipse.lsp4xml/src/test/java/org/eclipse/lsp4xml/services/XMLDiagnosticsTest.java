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
package org.eclipse.lsp4xml.services;

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.junit.Test;

/**
 * XML diagnostics services tests
 *
 */
public class XMLDiagnosticsTest {

	private static CancelChecker NULL_MONITOR = new CancelChecker() {

		@Override
		public void checkCanceled() {
			// Do nothing
		}
	};

	@Test
	public void testName() throws Exception {
		XMLLanguageService languageService = new XMLLanguageService();
		TextDocument document = new TextDocument("<a", "test.xml");
		List<Diagnostic> diagnostics = languageService.doDiagnostics(document, NULL_MONITOR);
		System.err.println(diagnostics);
	}

}
