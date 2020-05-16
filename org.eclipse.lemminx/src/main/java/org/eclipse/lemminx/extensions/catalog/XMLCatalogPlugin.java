/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.extensions.catalog;

import org.eclipse.lemminx.services.extensions.IXMLExtension;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4j.InitializeParams;

/**
 * XMl Catalog plugin.
 */
public class XMLCatalogPlugin implements IXMLExtension {

	private XMLCatalogURIResolverExtension uiResolver;

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		uiResolver = new XMLCatalogURIResolverExtension(registry);
		registry.getResolverExtensionManager().registerResolver(uiResolver);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		registry.getResolverExtensionManager().unregisterResolver(uiResolver);
	}
}
