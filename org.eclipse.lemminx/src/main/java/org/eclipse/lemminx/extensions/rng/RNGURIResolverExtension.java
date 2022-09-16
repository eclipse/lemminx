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

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.lemminx.services.IXMLDocumentProvider;
import org.eclipse.lemminx.uriresolver.CacheResourcesManager;
import org.eclipse.lemminx.uriresolver.CacheResourcesManager.ResourceToDeploy;
import org.eclipse.lemminx.uriresolver.IExternalGrammarLocationProvider;
import org.eclipse.lemminx.uriresolver.URIResolverExtension;
import org.eclipse.lemminx.utils.DOMUtils;

/**
 * Resolves all *.rng files to use the relaxng.xsd schema
 *
 * @author datho7561
 */
public class RNGURIResolverExtension implements URIResolverExtension, IExternalGrammarLocationProvider {

	private static final ResourceToDeploy RNG_RNG = new ResourceToDeploy("http://relaxng.org/relaxng.xsd",
			"schemas/rng/relaxng.xsd");

	public RNGURIResolverExtension(IXMLDocumentProvider documentProvider) {

	}

	public String getName() {
		return "embedded relaxng.xsd";
	}

	@Override
	public Map<String, String> getExternalGrammarLocation(URI fileURI) {
		return resolve(fileURI.toString());
	}

	@Override
	public String resolve(String baseLocation, String publicId, String systemId) {
		return resolve(baseLocation).get(IExternalGrammarLocationProvider.NO_NAMESPACE_SCHEMA_LOCATION);
	}

	public Map<String, String> resolve(String uri) {
		if (DOMUtils.isRNG(uri)) {
			try {
				// XXX: I think this triggers the download, but confirm if this is needed
				CacheResourcesManager.getResourceCachePath(RNG_RNG);
				Map<String, String> schema  = new HashMap<>();
				schema.put(IExternalGrammarLocationProvider.NO_NAMESPACE_SCHEMA_LOCATION,
					RNG_RNG.getDeployedPath().toFile().toURI().toString());
				return schema;
			} catch (IOException e) {
				// do nothing
			}
		}
		return null;
	}

}
