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
package org.eclipse.lemminx.extensions.relaxng.grammar.rng;

import java.io.IOException;

import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.eclipse.lemminx.uriresolver.CacheResourcesManager;
import org.eclipse.lemminx.uriresolver.CacheResourcesManager.ResourceToDeploy;
import org.eclipse.lemminx.uriresolver.URIResolverExtension;

/**
 * Resolve the RelaxNG for a RelaxNG grammar rng.
 *
 */
public class RelaxNGURIResolverExtension implements URIResolverExtension {

	private static final String RELAXNG_NAMESPACE_URI = "http://relaxng.org/ns/structure/1.0"; //$NON-NLS-1$

	private static final String RELAXNG_SYSTEM = "https://relaxng.org/relaxng.rng";

	private static final ResourceToDeploy RELAXNG_RESOURCE = new ResourceToDeploy(RELAXNG_SYSTEM,
			"schemas/relaxng/relaxng.rng");

	@Override
	public String getName() {
		return "embedded relaxng.rng";
	}

	@Override
	public String resolve(String baseLocation, String publicId, String systemId) {
		if (!RELAXNG_NAMESPACE_URI.equals(publicId)) {
			return null;
		}
		try {
			return CacheResourcesManager.getResourceCachePath(RELAXNG_RESOURCE).toFile().toURI().toString();
		} catch (Exception e) {
			// Do nothing?
		}
		return RELAXNG_SYSTEM;
	}

	@Override
	public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException {
		String publicId = resourceIdentifier.getNamespace();
		if (RELAXNG_NAMESPACE_URI.equals(publicId)) {
			String baseLocation = resourceIdentifier.getBaseSystemId();
			String relaxngFilePath = resolve(baseLocation, publicId, null);
			if (relaxngFilePath != null) {
				return new XMLInputSource(publicId, relaxngFilePath, relaxngFilePath);
			}
		}
		return null;
	}

}
