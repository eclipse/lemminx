/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.services.extensions.diagnostics;

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.dom.DOMDocument;

/**
 * Diagnostics participant API.
 *
 */
public interface IDiagnosticsParticipant {

	/**
	 * Validate the given XML document.
	 * 
	 * @param xmlDocument XML document to validate.
	 * @param diagnostics list to populate with errors, warnings, etc
	 * @param monitor     used to stop the validation when XML document changed.
	 */
	void doDiagnostics(DOMDocument xmlDocument, List<Diagnostic> diagnostics, CancelChecker monitor);

}
