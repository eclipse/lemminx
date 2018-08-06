package org.eclipse.lsp4xml.extensions;

import org.eclipse.lsp4j.DidChangeConfigurationParams;

public class XMLExtensionAdapter implements IXMLExtension {

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params) {
		
	}

	@Override
	public ICompletionParticipant getCompletionParticipant() {
		return null;
	}

	@Override
	public IHoverParticipant getHoverParticipant() {
		return null;
	}

	@Override
	public IDiagnosticsParticipant getDiagnosticsParticipant() {
		return null;
	}

}
