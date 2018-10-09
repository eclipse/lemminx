/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.xsl;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4xml.services.extensions.IXMLExtension;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtensionManager;

/**
 * XSL plugin.
 * 
 * @author azerr
 *
 */
public class XSLPlugin implements IXMLExtension {

	private XSLURIResolverExtension uiResolver;

	@Override
	public void updateSettings(Object settings) {

	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		uiResolver = new XSLURIResolverExtension(registry.getDocumentProvider());
		URIResolverExtensionManager.getInstance().registerResolver(uiResolver);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		URIResolverExtensionManager.getInstance().unregisterResolver(uiResolver);
	}
}
