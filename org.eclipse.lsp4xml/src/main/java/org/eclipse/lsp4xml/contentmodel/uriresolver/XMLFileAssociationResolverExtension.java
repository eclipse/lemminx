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
package org.eclipse.lsp4xml.contentmodel.uriresolver;

import java.net.URI;
import java.util.Map;

import org.eclipse.lsp4xml.contentmodel.settings.XMLFileAssociation;
import org.eclipse.lsp4xml.uriresolver.IExternalSchemaLocationProvider;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtension;

/**
 * XML file association URI resolver.
 *
 */
public class XMLFileAssociationResolverExtension implements URIResolverExtension, IExternalSchemaLocationProvider {

	private XMLFileAssociation[] fileAssociations;

	public void setFileAssociations(XMLFileAssociation[] fileAssociations) {
		this.fileAssociations = fileAssociations;
	}

	@Override
	public String resolve(String baseLocation, String publicId, String systemId) {
		if (fileAssociations != null) {
			for (XMLFileAssociation fileAssociation : fileAssociations) {
				if (fileAssociation.matches(baseLocation)) {
					return fileAssociation.getSystemId();
				}
			}
		}
		return null;
	}

	@Override
	public Map<String, String> getExternalSchemaLocation(URI fileURI) {
		if (fileAssociations != null) {
			for (XMLFileAssociation fileAssociation : fileAssociations) {
				if (fileAssociation.matches(fileURI)) {
					return fileAssociation.getExternalSchemaLocation();
				}
			}
		}
		return null;
	}

}
