/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.uriresolver;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;

/**
 * URI resolver manager.
 *
 */
public class URIResolverExtensionManager implements URIResolverExtension, IExternalSchemaLocationProvider {

	private final static URIResolverExtensionManager INSTANCE = new URIResolverExtensionManager();

	public static URIResolverExtensionManager getInstance() {
		return INSTANCE;
	}

	private final List<URIResolverExtension> resolvers;

	private URIResolverExtensionManager() {
		resolvers = new ArrayList<>();
	}

	/**
	 * Register an URI resolver.
	 * 
	 * @param resolver the URI resolver to register.
	 */
	public void registerResolver(URIResolverExtension resolver) {
		resolvers.add(resolver);
	}

	/**
	 * Unregister an URI resolver.
	 * 
	 * @param resolver the URI resolver to unregister.
	 */
	public void unregisterResolver(URIResolverExtension resolver) {
		resolvers.add(resolver);
	}

	@Override
	public String resolve(String baseLocation, String publicId, String systemId) {
		for (URIResolverExtension resolver : resolvers) {
			String resolved = resolver.resolve(baseLocation, publicId, systemId);
			if (resolved != null && !resolved.isEmpty()) {
				return resolved;
			}
		}
		return systemId;
	}

	@Override
	public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException {
		XMLInputSource is = null;
		for (URIResolverExtension resolver : resolvers) {
			is = resolver.resolveEntity(resourceIdentifier);
			if (is != null) {
				return is;
			}
		}
		return null;
	}

	@Override
	public Map getExternalSchemaLocation(URI fileURI) {
		for (URIResolverExtension resolver : resolvers) {
			if (resolver instanceof IExternalSchemaLocationProvider) {
				Map result = ((IExternalSchemaLocationProvider) resolver).getExternalSchemaLocation(fileURI);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}
}
