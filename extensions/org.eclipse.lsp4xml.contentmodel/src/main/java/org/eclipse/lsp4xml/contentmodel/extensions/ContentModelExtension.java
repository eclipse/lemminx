package org.eclipse.lsp4xml.contentmodel.extensions;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4xml.extensions.ICompletionParticipant;
import org.eclipse.lsp4xml.extensions.IDiagnosticsParticipant;
import org.eclipse.lsp4xml.extensions.IHoverParticipant;
import org.eclipse.lsp4xml.extensions.XMLExtensionAdapter;

public class ContentModelExtension extends XMLExtensionAdapter {

	private final ICompletionParticipant completionParticipant;

	private final IHoverParticipant hoverParticipant;

	private final ContentModelDiagnosticsParticipant diagnosticsParticipant;

	public ContentModelExtension() {
		completionParticipant = new ContentModelCompletionParticipant();
		hoverParticipant = new ContentModelHoverParticipant();
		diagnosticsParticipant = new ContentModelDiagnosticsParticipant();
	}

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params) {
		diagnosticsParticipant.didChangeConfiguration(params);
	}

	@Override
	public ICompletionParticipant getCompletionParticipant() {
		return completionParticipant;
	}

	@Override
	public IHoverParticipant getHoverParticipant() {
		return hoverParticipant;
	}

	@Override
	public IDiagnosticsParticipant getDiagnosticsParticipant() {
		return diagnosticsParticipant;
	}

}
