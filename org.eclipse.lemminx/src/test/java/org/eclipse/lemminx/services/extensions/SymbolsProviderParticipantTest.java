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
package org.eclipse.lemminx.services.extensions;

import static org.eclipse.lemminx.XMLAssert.ds;
import static org.eclipse.lemminx.XMLAssert.r;

import java.util.Arrays;

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.DocumentSymbolsResult;
import org.eclipse.lemminx.services.SymbolInformationResult;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.eclipse.lemminx.settings.XMLSymbolSettings;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ISymbolsProviderParticipant} API.
 * 
 */
public class SymbolsProviderParticipantTest {

	private static class ExtendedSymbolLanguageService extends XMLLanguageService {

		public ExtendedSymbolLanguageService() {
			super.registerSymbolsProviderParticipant(new ISymbolsProviderParticipant() {

				@Override
				public void findSymbolInformations(DOMDocument document, SymbolInformationResult symbols,
						CancelChecker cancelChecker) {

				}

				@Override
				public void findDocumentSymbols(DOMDocument document, DocumentSymbolsResult symbols,
						CancelChecker cancelChecker) {
					symbols.add(
							new DocumentSymbol("Custom-Symbol", SymbolKind.Namespace, r(0, 0, 0, 1), r(0, 0, 0, 1)));
				}

				@Override
				public SymbolStrategy applyFor(DOMDocument document) {
					String uri = document.getDocumentURI();
					if ("replace.xml".equals(uri)) {
						return SymbolStrategy.REPLACE;
					} else if ("insert.xml".equals(uri)) {
						return SymbolStrategy.INSERT;
					}
					return SymbolStrategy.UNADAPTABLE;
				}
			});
		}
	}

	@Test
	public void insertCustomSymbols() {
		String xml = "<root />";
		testDocumentSymbolsFor(xml, "insert.xml", //
				// Custom symbols
				ds("Custom-Symbol", SymbolKind.Namespace, r(0, 0, 0, 1), r(0, 0, 0, 1), null, null), //
				// XML symbols
				ds("root", SymbolKind.Field, r(0, 0, 0, 8), r(0, 0, 0, 8), null, Arrays.asList()));
	}

	@Test
	public void replaceCustomSymbols() {
		String xml = "<root />";
		testDocumentSymbolsFor(xml, "replace.xml", //
				ds("Custom-Symbol", SymbolKind.Namespace, r(0, 0, 0, 1), r(0, 0, 0, 1), null, null));
	}

	@Test
	public void noCustomSymbols() {
		String xml = "<root />";
		testDocumentSymbolsFor(xml, "test.xml", //
				ds("root", SymbolKind.Field, r(0, 0, 0, 8), r(0, 0, 0, 8), null, Arrays.asList()));
	}

	private static void testDocumentSymbolsFor(String xml, String fileURI, DocumentSymbol... expected) {
		ExtendedSymbolLanguageService languageService = new ExtendedSymbolLanguageService();
		XMLAssert.testDocumentSymbolsFor(languageService, xml, fileURI, new XMLSymbolSettings(), null, expected);
	}
}
