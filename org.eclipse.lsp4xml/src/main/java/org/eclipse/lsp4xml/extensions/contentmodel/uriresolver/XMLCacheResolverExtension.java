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
 * URI resolver which download the first time the XML Schema, DTD from "http" or
 * "ftp" in the file system. The second time, teh downloaded file is used
 * instead of accessing from "http" or "ftp". This cache improves drastically
 * the performance of some XML Schema (ex: xml.xsd)
 *
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
		// Cache is used only for resource coming from "http" or "ftp".
		if (CacheResourcesManager.getInstance().canUseCache(url)) {
			// Try to get the downloaded resource. In the case of the resource is
			// downloading and takes so many time,
			// the exception CacheResourceDownloadingException is thrown.
			Path file = CacheResourcesManager.getInstance().getResource(url);
			if (file != null) {
				// The resource is downloaded in the file system, use it.
				XMLInputSource source = new XMLInputSource(resourceIdentifier);
				source.setByteStream(Files.newInputStream(file));
				return source;
			}
		}
		return null;
	}

	/**
	 * Set true if cache must be used and false otherwise.
	 * 
	 * @param useCache true if cache must be used and false otherwise.
	 */
	public void setUseCache(boolean useCache) {
		CacheResourcesManager.getInstance().setUseCache(useCache);
	}

}
