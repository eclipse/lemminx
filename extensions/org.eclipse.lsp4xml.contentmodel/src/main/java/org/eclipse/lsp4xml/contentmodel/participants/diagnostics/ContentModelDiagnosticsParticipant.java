/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.contentmodel.participants.diagnostics;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.contentmodel.config.ContentModelDiagnosticsConfiguration;
import org.eclipse.lsp4xml.services.extensions.IDiagnosticsParticipant;

/**
 * Validate XML file with Xerces for basic validation an dXML Schema, DTD.
 *
 */
public class ContentModelDiagnosticsParticipant implements IDiagnosticsParticipant {

	private CatalogResolver catalogResolver;

	public void setConfiguration(ContentModelDiagnosticsConfiguration config) {
		String xmlCatalogFiles = "";
		if (config != null) {
			if (config.getCatalogs() != null) {
				xmlCatalogFiles = Stream.of(config.getCatalogs()).map(Object::toString)
						.collect(Collectors.joining(";"));
			}
		}
		setupXMLCatalog(xmlCatalogFiles);
	}

	@Override
	public void doDiagnostics(TextDocument document, List<Diagnostic> diagnostics, CancelChecker monitor) {
		XMLValidator.doDiagnostics(document, catalogResolver, diagnostics, monitor);
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

}
