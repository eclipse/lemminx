package org.eclipse.lsp4xml.services.extensions;

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.dom.XMLDocument;

public interface IDiagnosticsParticipant {

	void doDiagnostics(XMLDocument xmlDocument, List<Diagnostic> diagnostics, CancelChecker monitor);

}
