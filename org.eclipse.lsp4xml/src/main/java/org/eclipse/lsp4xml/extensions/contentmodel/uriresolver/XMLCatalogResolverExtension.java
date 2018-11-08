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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.util.URI.MalformedURIException;
import org.apache.xerces.util.XMLCatalogResolver;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtension;

/**
 * XML catalog URI resolver.
 *
 */
public class XMLCatalogResolverExtension implements URIResolverExtension {

	private XMLCatalogResolver catalogResolver;
	private String rootUri;

	@Override
	public String resolve(String baseLocation, String publicId, String systemId) {
		if (catalogResolver != null) {
			try {
				if (publicId != null && systemId != null) {
					return catalogResolver.resolvePublic(publicId, systemId);
				} else if (systemId != null) {
					return catalogResolver.resolveSystem(systemId);
				} else if (publicId != null) {
					return catalogResolver.resolvePublic(publicId, null);
				}
			} catch (Exception e) {

			}
		}
		return null;
	}

	@Override
	public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException {
		if (catalogResolver != null) {
			return catalogResolver.resolveEntity(resourceIdentifier);
		}
		return null;
	}

	/**
	 * Set the root URI
	 * 
	 * @param rootUri the root URI
	 */
	public void setRootUri(String rootUri) {
		this.rootUri = rootUri;
	}

	/**
	 * Initialize catalogs path.
	 * 
	 * @param catalogs the catalog path array.
	 */
	public void setCatalogs(String[] catalogs) {
		if (catalogs != null) {
			String[] xmlCatalogFiles = Stream.of(catalogs) //
					.map(path -> expandSystemId(path)) // resolve catalog file path with root uri
					.filter(XMLCatalogResolverExtension::isXMLCatalogFileValid) // check if XML catalog path is valid
					.collect(Collectors.toList()).toArray(new String[0]);
			if (xmlCatalogFiles.length > 0) {
				XMLCatalogResolver catalogResolver = new XMLCatalogResolver(xmlCatalogFiles);
				setCatalogResolver(catalogResolver);
			} else {
				setCatalogResolver(null);
			}
		} else {
			setCatalogResolver(null);
		}
	}

	private String expandSystemId(String path) {
		try {
			return XMLEntityManager.expandSystemId(path, rootUri, false);
		} catch (MalformedURIException e) {
			return path;
		}
	}

	/**
	 * Returns true if the XML catalog file exists and false otherwise.
	 * 
	 * @param catalogFile catalog file to check.
	 * @return true if the XML catalog file exists and false otherwise.
	 */
	private static boolean isXMLCatalogFileValid(String catalogFile) {
		try {
			return new File(new URI(catalogFile).getPath()).exists();
		} catch (URISyntaxException e) {
			return new File(catalogFile).exists();
		}
	}

	private void setCatalogResolver(XMLCatalogResolver catalogResolver) {
		this.catalogResolver = catalogResolver;
	}

}
