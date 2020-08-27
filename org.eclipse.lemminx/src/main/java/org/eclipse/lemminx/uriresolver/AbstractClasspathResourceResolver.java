/*******************************************************************************
* Copyright (c) 2020 IBM Corporation and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.uriresolver;

import java.nio.file.Path;

import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.eclipse.lemminx.uriresolver.CacheResourcesManager.ResourceToDeploy;

/**
 * Abstract class to deploy a resource to the lemminx cache from classpath. And
 * resolve it depending on the shouldResolveResource method.
 */
public abstract class AbstractClasspathResourceResolver implements URIResolverExtension {

	/**
	 * Finds if there is a resource that should be deployed. Null if no resource
	 * 
	 * @param baseLocation - the location of the resource that contains the uri
	 * @param publicId     - an optional public identifier (i.e. namespace name), or
	 *                     null if none
	 * @param systemId     - an absolute or relative URI, or null if none
	 * @return The resource to deploy if there is one. Null otherwise
	 */
	public abstract ResourceToDeploy resourceToResolve(String baseLocation, String publicId, String systemId);

	@Override
	public String resolve(String baseLocation, String publicId, String systemId) {
		ResourceToDeploy resource = this.resourceToResolve(baseLocation, publicId, systemId);
		if (resource != null) {
			try {
				Path resourceCachePath = CacheResourcesManager.getResourceCachePath(resource);
				return resourceCachePath.toFile().toURI().toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	@Override
	public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) {
		String baseLocation = resourceIdentifier.getBaseSystemId();
		String publicId = resourceIdentifier.getNamespace();
		ResourceToDeploy resource = this.resourceToResolve(baseLocation, publicId, null);
		if (resource != null) {
			String resourceFilePath = resolve(baseLocation, publicId, null);
			if (resourceFilePath != null) {
				return new XMLInputSource(publicId, resourceFilePath, resourceFilePath);
			}
		}

		return null;
	}
}
