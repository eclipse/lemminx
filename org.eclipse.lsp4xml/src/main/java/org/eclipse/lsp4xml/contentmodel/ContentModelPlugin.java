package org.eclipse.lsp4xml.contentmodel;

import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
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

	private CatalogResolver catalogResolver;

	public ContentModelPlugin() {
		completionParticipant = new ContentModelCompletionParticipant(this);
		hoverParticipant = new ContentModelHoverParticipant();
		diagnosticsParticipant = new ContentModelDiagnosticsParticipant(this);
	}

	@Override
	public void updateSettings(Object settings) {
		ContentModelSettings cmSettings = JSONUtility.toModel(settings, ContentModelSettings.class);
		if (cmSettings != null) {
			updateSettings(cmSettings);
		}
	}

	private void updateSettings(ContentModelSettings settings) {
		String xmlCatalogFiles = "";

		if (settings.getCatalogs() != null) {
			xmlCatalogFiles = Stream.of(settings.getCatalogs()).map(Object::toString).collect(Collectors.joining(";"));
		}
		setupXMLCatalog(xmlCatalogFiles);
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

	private void setupXMLCatalog(String xmlCatalogFiles) {
		if (xmlCatalogFilesValid(xmlCatalogFiles)) {
			CatalogManager catalogManager = new CatalogManager();
			catalogManager.setUseStaticCatalog(false);
			catalogManager.setIgnoreMissingProperties(true);
			catalogManager.setCatalogFiles(xmlCatalogFiles);
			catalogResolver = new CatalogResolver(catalogManager);
		} else {
			// languageClient
			// .showMessage(new MessageParams(MessageType.Error,
			// XMLMessages.XMLDiagnostics_CatalogLoad_error));
			catalogResolver = null;
		}
	}

	private boolean xmlCatalogFilesValid(String xmlCatalogFiles) {
		String[] paths = xmlCatalogFiles.split(";");
		for (int i = 0; i < paths.length; i++) {
			String currentPath = paths[i];
			File file = new File(currentPath);
			if (!file.exists()) {
				return false;
			}
		}
		return true;
	}

	public CatalogResolver getCatalogResolver() {
		return catalogResolver;
	}
}
