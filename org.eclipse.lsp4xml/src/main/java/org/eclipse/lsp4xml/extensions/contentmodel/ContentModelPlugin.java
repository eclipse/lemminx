package org.eclipse.lsp4xml.extensions.contentmodel;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.ContentModelCodeActionParticipant;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.ContentModelCompletionParticipant;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.ContentModelDocumentLinkParticipant;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.ContentModelHoverParticipant;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.diagnostics.ContentModelDiagnosticsParticipant;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.ContentModelSettings;
import org.eclipse.lsp4xml.services.extensions.ICompletionParticipant;
import org.eclipse.lsp4xml.services.extensions.IHoverParticipant;
import org.eclipse.lsp4xml.services.extensions.IXMLExtension;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.services.extensions.save.ISaveContext;
import org.eclipse.lsp4xml.services.extensions.save.ISaveParticipant;
import org.eclipse.lsp4xml.utils.DOMUtils;

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

	private final ContentModelCodeActionParticipant codeActionParticipant;

	private final ContentModelDocumentLinkParticipant documentLinkParticipant;

	private final ISaveParticipant saveParticipant;

	public ContentModelPlugin() {
		completionParticipant = new ContentModelCompletionParticipant();
		hoverParticipant = new ContentModelHoverParticipant();
		diagnosticsParticipant = new ContentModelDiagnosticsParticipant();
		codeActionParticipant = new ContentModelCodeActionParticipant();
		documentLinkParticipant = new ContentModelDocumentLinkParticipant();
		saveParticipant = (context) -> {
			if (context.getType() == ISaveContext.SaveContextType.DOCUMENT) {
				// The save is done for a given XML file
				String documentURI = context.getUri();
				XMLDocument document = context.getDocument(documentURI);
				if (DOMUtils.isCatalog(document)) {
					// the XML document which has changed is a XML catalog.
					// 1) refresh catalogs
					ContentModelManager.getInstance().refreshCatalogs();
					// 2) Validate all opened XML files except the catalog which have changed
					context.collectDocumentToValidate(d -> {
						XMLDocument xml = context.getDocument(d.getDocumentURI());
						xml.resetGrammar();
						return !documentURI.equals(d.getDocumentURI());
					});
				}
			}
		};
	}

	@Override
	public void updateSettings(Object initializationOptionsSettings) {
		ContentModelSettings cmSettings = ContentModelSettings.getSettings(initializationOptionsSettings);
		if (cmSettings != null) {
			updateSettings(cmSettings);
		}
	}

	private void updateSettings(ContentModelSettings settings) {
		if (settings.getCatalogs() != null) {
			// Update XML catalog settings
			ContentModelManager.getInstance().setCatalogs(settings.getCatalogs());
		}
		if (settings.getFileAssociations() != null) {
			// Update XML file associations
			ContentModelManager.getInstance().setFileAssociations(settings.getFileAssociations());
		}
		// Update use cache, only if it is set in the settings.
		Boolean useCache = settings.isUseCache();
		if (useCache != null) {
			ContentModelManager.getInstance().setUseCache(useCache);
		}
	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		if (params != null) {
			ContentModelManager.getInstance().setRootURI(params.getRootUri());
		}
		registry.registerCompletionParticipant(completionParticipant);
		registry.registerHoverParticipant(hoverParticipant);
		registry.registerDiagnosticsParticipant(diagnosticsParticipant);
		registry.registerCodeActionParticipant(codeActionParticipant);
		registry.registerDocumentLinkParticipant(documentLinkParticipant);
		registry.registerSaveParticipant(saveParticipant);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		registry.unregisterCompletionParticipant(completionParticipant);
		registry.unregisterHoverParticipant(hoverParticipant);
		registry.unregisterDiagnosticsParticipant(diagnosticsParticipant);
		registry.unregisterCodeActionParticipant(codeActionParticipant);
		registry.unregisterDocumentLinkParticipant(documentLinkParticipant);
		registry.unregisterSaveParticipant(saveParticipant);
	}

}
