package org.eclipse.lsp4xml.extensions.contentmodel.uriresolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.eclipse.lsp4xml.uriresolver.CacheResourcesManager;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtension;

public class XMLCacheResolverExtension implements URIResolverExtension {

	@Override
	public String resolve(String baseLocation, String publicId, String systemId) {
		return null;
	}

	@Override
	public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException {
		String url = resourceIdentifier.getExpandedSystemId();
		if (CacheResourcesManager.getInstance().canUseCache(url)) {
			Path file = CacheResourcesManager.getInstance().getResources(url);
			if (file != null) {
				XMLInputSource source = new XMLInputSource(resourceIdentifier);
				source.setByteStream(Files.newInputStream(file));
				return source;
			}
		}
		return null;
	}

	public void setUseCache(boolean useCache) {
		CacheResourcesManager.getInstance().setUseCache(useCache);
	}

}
