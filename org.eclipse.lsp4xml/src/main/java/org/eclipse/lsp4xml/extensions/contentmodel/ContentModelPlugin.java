package org.eclipse.lsp4xml.extensions.contentmodel;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4xml.dom.DOMDocument;
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
import org.eclipse.lsp4xml.uriresolver.URIResolverExtensionManager;
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

	private ContentModelManager contentModelManager;

	private ContentModelSettings cmSettings;
	
	public ContentModelPlugin() {
		completionParticipant = new ContentModelCompletionParticipant();
		hoverParticipant = new ContentModelHoverParticipant();
		diagnosticsParticipant = new ContentModelDiagnosticsParticipant(this);
		codeActionParticipant = new ContentModelCodeActionParticipant();
		documentLinkParticipant = new ContentModelDocumentLinkParticipant();
	}

	@Override
	public void doSave(ISaveContext context) {
		if (context.getType() == ISaveContext.SaveContextType.DOCUMENT) {
			// The save is done for a given XML file
			String documentURI = context.getUri();
			DOMDocument document = context.getDocument(documentURI);
			if (DOMUtils.isCatalog(document)) {
				// the XML document which has changed is a XML catalog.
				// 1) refresh catalogs
				contentModelManager.refreshCatalogs();
				// 2) Validate all opened XML files except the catalog which have changed
				context.collectDocumentToValidate(d -> {
					DOMDocument xml = context.getDocument(d.getDocumentURI());
					xml.resetGrammar();
					return !documentURI.equals(d.getDocumentURI());
				});
			}
		} else {
			// Settings
			updateSettings(context);
		}
	}

	private void updateSettings(ISaveContext saveContext) {
		Object initializationOptionsSettings = saveContext.getSettings();
		cmSettings = ContentModelSettings.getContentModelXMLSettings(initializationOptionsSettings);
		if (cmSettings != null) {
			updateSettings(cmSettings, saveContext);
		}
	}

	private void updateSettings(ContentModelSettings settings, ISaveContext context) {
		if (settings.getCatalogs() != null) {
			// Update XML catalog settings
			boolean catalogPathsChanged = contentModelManager.setCatalogs(settings.getCatalogs());
			if (catalogPathsChanged) {
				// Validate all opened XML files
				context.collectDocumentToValidate(d -> {
					DOMDocument xml = context.getDocument(d.getDocumentURI());
					xml.resetGrammar();
					return true;
				});
			}
		}
		if (settings.getFileAssociations() != null) {
			// Update XML file associations
			boolean fileAssociationsChanged = contentModelManager
					.setFileAssociations(settings.getFileAssociations());
			if (fileAssociationsChanged) {
				// Validate all opened XML files
				context.collectDocumentToValidate(d -> {
					DOMDocument xml = context.getDocument(d.getDocumentURI());
					xml.resetGrammar();
					return true;
				});
			}
		}
		// Update use cache, only if it is set in the settings.
		Boolean useCache = settings.isUseCache();
		if (useCache != null) {
			contentModelManager.setUseCache(useCache);
		}
	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		URIResolverExtensionManager resolverManager = registry.getComponent(URIResolverExtensionManager.class);
		contentModelManager = new ContentModelManager(resolverManager);
		registry.registerComponent(contentModelManager);
		if (params != null) {
			contentModelManager.setRootURI(params.getRootUri());
		}
		registry.registerCompletionParticipant(completionParticipant);
		registry.registerHoverParticipant(hoverParticipant);
		registry.registerDiagnosticsParticipant(diagnosticsParticipant);
		registry.registerCodeActionParticipant(codeActionParticipant);
		registry.registerDocumentLinkParticipant(documentLinkParticipant);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		registry.unregisterCompletionParticipant(completionParticipant);
		registry.unregisterHoverParticipant(hoverParticipant);
		registry.unregisterDiagnosticsParticipant(diagnosticsParticipant);
		registry.unregisterCodeActionParticipant(codeActionParticipant);
		registry.unregisterDocumentLinkParticipant(documentLinkParticipant);
	}

	public ContentModelSettings getContentModelSettings() {
		return cmSettings;
	}
}
