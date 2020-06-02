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

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.lemminx.client.ExtendedClientCapabilities;
import org.eclipse.lemminx.client.PathWarnings;
import org.eclipse.lemminx.extensions.contentmodel.settings.ContentModelSettings;
import org.eclipse.lemminx.extensions.contentmodel.uriresolver.XMLCatalogResolverExtension;
import org.eclipse.lemminx.services.extensions.IXMLExtension;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lemminx.services.extensions.save.ISaveContext;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lemminx.settings.capabilities.InitializationOptionsExtendedClientCapabilities;
import org.eclipse.lsp4j.InitializeParams;

/**
 * XMl Catalog plugin.
 */
public class XMLCatalogPlugin implements IXMLExtension {

	private final String catalogSettingId = "xml.catalogs";

	private XMLCatalogURIResolverExtension uiResolver;

	private PathWarnings pathWarnings;

	@Override
	public void doSave(ISaveContext context) {
		Object initializationOptionsSettings = context.getSettings();
		ContentModelSettings cmSettings = ContentModelSettings.getContentModelXMLSettings(initializationOptionsSettings);
		if (cmSettings == null) {
			return;
		}
		validateCatalogPaths(cmSettings);
	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		uiResolver = new XMLCatalogURIResolverExtension(registry);
		registry.getResolverExtensionManager().registerResolver(uiResolver);
		ExtendedClientCapabilities extendedClientCapabilities = InitializationOptionsExtendedClientCapabilities
				.getExtendedClientCapabilities(params);
		initializePathWarnings(extendedClientCapabilities, registry);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		registry.getResolverExtensionManager().unregisterResolver(uiResolver);
	}

	private void initializePathWarnings(ExtendedClientCapabilities extendedCapabilities, XMLExtensionsRegistry registry) {
		SharedSettings sharedSettings = new SharedSettings();
		sharedSettings.setActionableNotificationSupport(extendedCapabilities.isActionableNotificationSupport());
		sharedSettings.setOpenSettingsCommandSupport(extendedCapabilities.isOpenSettingsCommandSupport());
		this.pathWarnings = new PathWarnings(registry.getLanguageClientAPIProvider().getLanguageClient(), sharedSettings);
	}

	private void validateCatalogPaths(ContentModelSettings cmSettings) {
		if (this.pathWarnings == null) {
			return; // should never happen
		}
		String[] catalogs = cmSettings.getCatalogs();
		Set<String> invalidCatalogs = Arrays.stream(catalogs).filter(c -> !XMLCatalogResolverExtension.isXMLCatalogFileValid(c)).collect(Collectors.toSet());
		
		if (invalidCatalogs.size() > 0) {
			this.pathWarnings.onInvalidFilePath(invalidCatalogs, this.catalogSettingId);
		}
	}
}
