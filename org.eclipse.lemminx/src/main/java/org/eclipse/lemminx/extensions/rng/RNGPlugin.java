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
package org.eclipse.lemminx.extensions.rng;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.services.extensions.IXMLExtension;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lemminx.services.extensions.save.ISaveContext;
import org.eclipse.lemminx.utils.DOMUtils;
import org.eclipse.lsp4j.InitializeParams;

/**
 * Plugin to help with editing RelaxNG grammars (in XML form).
 *
 * @author datho7561
 */
public class RNGPlugin implements IXMLExtension {

	private RNGURIResolverExtension uriResolver;
	private ContentModelManager contentModelManager;

	public RNGPlugin() {

	}

	@Override
	public void doSave(ISaveContext context) {
		String documentURI = context.getUri();
		if (DOMUtils.isRNG(documentURI)) {
			context.collectDocumentToValidate(d -> {
				DOMDocument xml = context.getDocument(d.getDocumentURI());
				return contentModelManager.dependsOnGrammar(xml, context.getUri());
			});
		}
	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		uriResolver = new RNGURIResolverExtension(registry.getDocumentProvider());
		registry.getResolverExtensionManager().registerResolver(uriResolver);
		contentModelManager = registry.getComponent(ContentModelManager.class);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		registry.getResolverExtensionManager().unregisterResolver(uriResolver);
	}

}
