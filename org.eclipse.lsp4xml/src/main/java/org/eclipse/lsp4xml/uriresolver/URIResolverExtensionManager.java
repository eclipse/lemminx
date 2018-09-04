package org.eclipse.lsp4xml.uriresolver;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;

public class URIResolverExtensionManager implements URIResolverExtension, IExternalSchemaLocationProvider {

	private final static URIResolverExtensionManager INSTANCE = new URIResolverExtensionManager();

	public static URIResolverExtensionManager getInstance() {
		return INSTANCE;
	}

	private final List<URIResolverExtension> resolvers;

	private URIResolverExtensionManager() {
		resolvers = new ArrayList<>();
	}

	public void registerResolver(URIResolverExtension resolver) {
		resolvers.add(resolver);
	}

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
		return null;
	}

	@Override
	public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException {
		XMLInputSource is = null;
		long start =System.currentTimeMillis();
		for (URIResolverExtension resolver : resolvers) {
			is = resolver.resolveEntity(resourceIdentifier);
			if (is != null) {
				System.err.println(System.currentTimeMillis() - start);
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
