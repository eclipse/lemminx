package org.eclipse.lsp4xml.contentmodel.uriresolver;

import java.io.IOException;

import org.apache.xerces.util.XMLCatalogResolver;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtension;

public class XMLCatalogResolverExtension implements URIResolverExtension {

	private XMLCatalogResolver catalogResolver;

	public void setCatalogResolver(XMLCatalogResolver catalogResolver) {
		this.catalogResolver = catalogResolver;
	}

	@Override
	public String resolve(String baseLocation, String publicId, String systemId) {
		if (catalogResolver != null) {
			try {
				String resolvedId = null;
				if (publicId != null && systemId != null) {
					resolvedId = catalogResolver.resolvePublic(publicId, systemId);
				} else if (systemId != null) {
					resolvedId = catalogResolver.resolveSystem(systemId);
				}
				return resolvedId;
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
