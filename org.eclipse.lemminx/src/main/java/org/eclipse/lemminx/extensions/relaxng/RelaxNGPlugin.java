/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.relaxng;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelProvider;
import org.eclipse.lemminx.extensions.relaxng.grammar.RelaxNGURIResolverExtension;
import org.eclipse.lemminx.extensions.relaxng.xml.contentmodel.CMRelaxNGContentModelProvider;
import org.eclipse.lemminx.services.extensions.IXMLExtension;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lemminx.services.extensions.save.ISaveContext;
import org.eclipse.lemminx.uriresolver.URIResolverExtension;
import org.eclipse.lemminx.utils.DOMUtils;
import org.eclipse.lsp4j.InitializeParams;

/**
 * RelaxNG plugin.
 */
public class RelaxNGPlugin implements IXMLExtension {

	private ContentModelManager contentModelManager;
	private URIResolverExtension uiResolver;

	public RelaxNGPlugin() {
	}

	@Override
	public void doSave(ISaveContext context) {
		String documentURI = context.getUri();
		DOMDocument document = context.getDocument(documentURI);
		if (DOMUtils.isRelaxNG(document)) {
			context.collectDocumentToValidate(d -> {
				DOMDocument xml = context.getDocument(d.getDocumentURI());
				return contentModelManager.dependsOnGrammar(xml, context.getUri());
			});
		}
	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		uiResolver = new RelaxNGURIResolverExtension();
		registry.getResolverExtensionManager().registerResolver(uiResolver);
		// register RelaxNG content model provider
		ContentModelProvider modelProvider = new CMRelaxNGContentModelProvider(registry.getResolverExtensionManager());
		this.contentModelManager = registry.getComponent(ContentModelManager.class);
		this.contentModelManager.registerModelProvider(modelProvider);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		registry.getResolverExtensionManager().unregisterResolver(uiResolver);
	}

	public ContentModelManager getContentModelManager() {
		return contentModelManager;
	}
}
