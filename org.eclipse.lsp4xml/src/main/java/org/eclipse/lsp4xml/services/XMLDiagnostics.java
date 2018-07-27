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

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.extensions.IDiagnosticsParticipant;
import org.eclipse.lsp4xml.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.internal.parser.Scanner;
import org.eclipse.lsp4xml.internal.parser.TokenType;
import org.eclipse.lsp4xml.internal.parser.XMLScanner;

/**
 * XML diagnostics support.
 *
 */
class XMLDiagnostics {

	private final XMLExtensionsRegistry extensionsRegistry;

	private SAXParserFactory factory;

	public XMLDiagnostics(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
		factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(true);
	}

	public List<Diagnostic> doDiagnostics(TextDocument document, String xmlSchemaFile, CancelChecker monitor) {
		List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
		// Basic XML validation
		doBasicDiagnostics(document, diagnostics, monitor);
		// Validation with extension (XML Schema, etc)
		for (IDiagnosticsParticipant diagnosticsParticipant : extensionsRegistry.getDiagnosticsParticipants()) {
			diagnosticsParticipant.doDiagnostics(document, diagnostics, monitor);
		}
		return diagnostics;
	}

	private void doBasicDiagnostics(TextDocument document, List<Diagnostic> diagnostics, CancelChecker monitor) {
		// TODO: implement basic validation with token...
		Scanner scanner = XMLScanner.createScanner(document.getText());
		TokenType token = scanner.scan();
		while (token != TokenType.EOS) {
//			System.err.println(token);
			switch (token) {
			}
			token = scanner.scan();
		}
	}

}
