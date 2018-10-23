package org.eclipse.lsp4xml.extensions.web;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4xml.extensions.web.participants.WebDefinitionParticipant;
import org.eclipse.lsp4xml.services.extensions.IDefinitionParticipant;
import org.eclipse.lsp4xml.services.extensions.IXMLExtension;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;

public class WebPlugin implements IXMLExtension {

	private final IDefinitionParticipant definitionParticipant;

	public WebPlugin() {
		definitionParticipant = new WebDefinitionParticipant();
	}

	@Override
	public void updateSettings(Object settings) {

	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		registry.registerDefinitionParticipant(definitionParticipant);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		registry.unregisterDefinitionParticipant(definitionParticipant);
	}
}
