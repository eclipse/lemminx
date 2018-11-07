/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.contentmodel.uriresolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.eclipse.lsp4xml.uriresolver.CacheResourcesManager;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtension;

/**
 * URI resolver which, on the first access, downloads the XML Schema or DTD 
 * from "http(s)" or "ftp" URIs to the file system. On subsequent calls, the 
 * locally cached file is used instead of being remotely accessed. 
 * This cache drastically improves the resolution performance of some XML Schemas 
 * (ex: xml.xsd)
 */
public class XMLCacheResolverExtension implements URIResolverExtension {

	@Override
	public String resolve(String baseLocation, String publicId, String systemId) {
		// Don't resolve the URI
		return null;
	}

	@Override
	public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException {
		String url = resourceIdentifier.getExpandedSystemId();
		// Cache is used only for resource coming from "http(s)" or "ftp".
		if (CacheResourcesManager.getInstance().canUseCache(url)) {
			// Try to get the downloaded resource. In the case where the resource is
			// downloading but takes too long, a CacheResourceDownloadingException is thrown.
			Path file = CacheResourcesManager.getInstance().getResource(url);
			if (file != null) {
				// The resource was downloaded locally, use it.
				XMLInputSource source = new XMLInputSource(resourceIdentifier);
				source.setByteStream(Files.newInputStream(file));
				return source;
			}
		}
		return null;
	}

	/**
	 * Set <code>true</code> if cache must be used, <code>false</code> otherwise.
	 * 
	 * @param useCache <code>true</code> if cache must be used, <code>false</code> otherwise.
	 */
	public void setUseCache(boolean useCache) {
		CacheResourcesManager.getInstance().setUseCache(useCache);
	}

}
