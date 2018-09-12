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

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.services.extensions.IDiagnosticsParticipant;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;

/**
 * XML diagnostics support.
 *
 */
class XMLDiagnostics {

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLDiagnostics(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public List<Diagnostic> doDiagnostics(XMLDocument xmlDocument, CancelChecker monitor) {
		List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
		try {
			doBasicDiagnostics(xmlDocument, diagnostics, monitor);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		doExtensionsDiagnostics(xmlDocument, diagnostics, monitor);
		return diagnostics;
	}

	/**
	 * Do basic validation to check the no XML valid.
	 * 
	 * @param xmlDocument
	 * @param diagnostics
	 * @param monitor
	 * @throws BadLocationException
	 */
	private void doBasicDiagnostics(XMLDocument xmlDocument, List<Diagnostic> diagnostics, CancelChecker monitor)
			throws BadLocationException {
		/*
		 * Scanner scanner = XMLScanner.createScanner(document.getText()); TokenType
		 * token = scanner.scan(); while (token != TokenType.EOS) {
		 * monitor.checkCanceled(); // TODO check tokens... token = scanner.scan(); }
		 */
	}

	/**
	 * Do validation with extension (XML Schema, etc)
	 * 
	 * @param xmlDocument
	 * @param diagnostics
	 * @param monitor
	 */
	private void doExtensionsDiagnostics(XMLDocument xmlDocument, List<Diagnostic> diagnostics, CancelChecker monitor) {
		for (IDiagnosticsParticipant diagnosticsParticipant : extensionsRegistry.getDiagnosticsParticipants()) {
			monitor.checkCanceled();
			diagnosticsParticipant.doDiagnostics(xmlDocument, diagnostics, monitor);
		}
	}

}
