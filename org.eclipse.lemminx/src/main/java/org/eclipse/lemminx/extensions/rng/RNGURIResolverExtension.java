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

import org.eclipse.lemminx.services.IXMLDocumentProvider;
import org.eclipse.lemminx.uriresolver.CacheResourcesManager;
import org.eclipse.lemminx.uriresolver.CacheResourcesManager.ResourceToDeploy;
import org.eclipse.lemminx.uriresolver.URIResolverExtension;
import org.eclipse.lemminx.utils.DOMUtils;

/**
 * Resolves all *.rng files to use the relaxng.rng schema
 *
 * @author datho7561
 */
public class RNGURIResolverExtension implements URIResolverExtension {

	private static final ResourceToDeploy RNG_RNG = new ResourceToDeploy("http://relaxng.org/relaxng.rng", "schemas/rng/relaxng.rng");


	public RNGURIResolverExtension(IXMLDocumentProvider documentProvider) {

	}

	public String getName() {
		return "embedded relaxng.rng";
	}

	@Override
	public String resolve(String baseLocation, String publicId, String systemId) {
		if (DOMUtils.isRNG(baseLocation)) {
			try {
				CacheResourcesManager.getResourceCachePath(RNG_RNG);
				return RNG_RNG.getDeployedPath().toFile().toURI().toString();
			} catch (IOException e) {
				// do nothing
			}
		}
		return null;
	}

}
