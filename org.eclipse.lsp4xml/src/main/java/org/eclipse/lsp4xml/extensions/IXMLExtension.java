package org.eclipse.lsp4xml.extensions;

import org.eclipse.lsp4j.DidChangeConfigurationParams;

public interface IXMLExtension {

	void didChangeConfiguration(DidChangeConfigurationParams params);

	ICompletionParticipant getCompletionParticipant();

	IHoverParticipant getHoverParticipant();

	IDiagnosticsParticipant getDiagnosticsParticipant();
}
