/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.xsl;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4xml.services.extensions.IXMLExtension;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.services.extensions.save.ISaveContext;

/**
 * XSL plugin.
 */
public class XSLPlugin implements IXMLExtension {

	private XSLURIResolverExtension uiResolver;

	@Override
	public void doSave(ISaveContext context) {

	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		uiResolver = new XSLURIResolverExtension(registry.getDocumentProvider());
		registry.getResolverExtensionManager().registerResolver(uiResolver);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		registry.getResolverExtensionManager().unregisterResolver(uiResolver);
	}
}
