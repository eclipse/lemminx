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

import static org.eclipse.lemminx.extensions.relaxng.RelaxNGConstants.RELAXNG_XSD_URI;

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
public class RelaxNGURIResolverExtension implements URIResolverExtension, IExternalGrammarLocationProvider {

	private static final ResourceToDeploy RELAXNG_XSD = new ResourceToDeploy(RELAXNG_XSD_URI,
			"schemas/relaxng/relaxng.xsd");

	public RelaxNGURIResolverExtension(IXMLDocumentProvider documentProvider) {

	}

	public String getName() {
		return "embedded relaxng.xsd";
	}

	@Override
	public Map<String, String> getExternalGrammarLocation(URI fileURI) {
		String schemaUri = resolve(fileURI.toString());
		if (schemaUri == null) {
			return null;
		}

		Map<String, String> schemaMap = new HashMap<>();
		schemaMap.put(IExternalGrammarLocationProvider.NO_NAMESPACE_SCHEMA_LOCATION,
				schemaUri);
		return schemaMap;
	}

	@Override
	public String resolve(String baseLocation, String publicId, String systemId) {
		return resolve(baseLocation);
	}

	private String resolve(String uri) {
		if (DOMUtils.isRelaxNG(uri)) {
			try {
				CacheResourcesManager.getResourceCachePath(RELAXNG_XSD);
				return RELAXNG_XSD.getDeployedPath().toFile().toURI().toString();
			} catch (IOException e) {
				// do nothing
			}
		}
		return null;
	}

}
