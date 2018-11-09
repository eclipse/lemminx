package org.eclipse.lsp4xml.extensions.emmet;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4xml.extensions.emmet.participants.EmmetCompletionParticipant;
import org.eclipse.lsp4xml.services.extensions.ICompletionParticipant;
import org.eclipse.lsp4xml.services.extensions.IXMLExtension;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.services.extensions.save.ISaveContext;

public class EmmetPlugin implements IXMLExtension {

	private final ICompletionParticipant completionParticipant;

	public EmmetPlugin() {
		completionParticipant = new EmmetCompletionParticipant();
	}

	@Override
	public void doSave(ISaveContext context) {
	
	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		registry.registerCompletionParticipant(completionParticipant);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		registry.unregisterCompletionParticipant(completionParticipant);
	}
}
