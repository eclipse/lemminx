package org.eclipse.lsp4xml.services.extensions;

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.commons.TextDocument;

public interface IDiagnosticsParticipant {

	void doDiagnostics(TextDocument document, List<Diagnostic> diagnostics, CancelChecker monitor);

}
