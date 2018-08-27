package org.eclipse.lsp4xml.contentmodel;

import org.eclipse.lsp4xml.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.contentmodel.participants.ContentModelCompletionParticipant;
import org.eclipse.lsp4xml.contentmodel.participants.ContentModelHoverParticipant;
import org.eclipse.lsp4xml.contentmodel.participants.diagnostics.ContentModelDiagnosticsParticipant;
import org.eclipse.lsp4xml.contentmodel.settings.ContentModelSettings;
import org.eclipse.lsp4xml.services.extensions.ICompletionParticipant;
import org.eclipse.lsp4xml.services.extensions.IHoverParticipant;
import org.eclipse.lsp4xml.services.extensions.IXMLExtension;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.utils.JSONUtility;

/**
 * Content model plugin extension to provide:
 * 
 * <ul>
 * <li>completion based on XML Schema, DTD...</li>
 * <li>hover based on XML Schema</li>
 * <li>diagnostics based on on XML Schema, DTD...</li>
 * </ul>
 */
public class ContentModelPlugin implements IXMLExtension {

	private final ICompletionParticipant completionParticipant;

	private final IHoverParticipant hoverParticipant;

	private final ContentModelDiagnosticsParticipant diagnosticsParticipant;

	public ContentModelPlugin() {
		completionParticipant = new ContentModelCompletionParticipant();
		hoverParticipant = new ContentModelHoverParticipant();
		diagnosticsParticipant = new ContentModelDiagnosticsParticipant();
	}

	@Override
	public void updateSettings(Object settings) {
		ContentModelSettings cmSettings = JSONUtility.toModel(settings, ContentModelSettings.class);
		if (cmSettings != null) {
			updateSettings(cmSettings);
		}
	}

	private void updateSettings(ContentModelSettings settings) {
		if (settings.getCatalogs() != null) {
			// Update XML catalog settings
			ContentModelManager.getInstance().setCatalogs(settings.getCatalogs());
		}
	}

	@Override
	public void start(XMLExtensionsRegistry registry) {
		registry.registerCompletionParticipant(completionParticipant);
		registry.registerHoverParticipant(hoverParticipant);
		registry.registerDiagnosticsParticipant(diagnosticsParticipant);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		registry.unregisterCompletionParticipant(completionParticipant);
		registry.unregisterHoverParticipant(hoverParticipant);
		registry.unregisterDiagnosticsParticipant(diagnosticsParticipant);
	}

}
