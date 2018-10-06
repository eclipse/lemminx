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

	public void setCatalogResolver(XMLCatalogResolver catalogResolver) {
		this.catalogResolver = catalogResolver;
	}

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

}
