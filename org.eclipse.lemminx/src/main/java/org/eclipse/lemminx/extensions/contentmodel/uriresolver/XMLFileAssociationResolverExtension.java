/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.extensions.contentmodel.uriresolver;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.util.URI.MalformedURIException;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLFileAssociation;
import org.eclipse.lemminx.uriresolver.IExternalGrammarLocationProvider;
import org.eclipse.lemminx.uriresolver.URIResolverExtension;

/**
 * XML file association URI resolver.
 *
 */
public class XMLFileAssociationResolverExtension implements URIResolverExtension, IExternalGrammarLocationProvider {

	private static Logger LOGGER = Logger.getLogger(XMLFileAssociationResolverExtension.class.getName());

	private String rootUri;

	private XMLFileAssociation[] fileAssociations;

	private List<String> workspaceFolderUris;

	@Override
	public String getName() {
		return "file association";
	}

	/**
	 *
	 * @param fileAssociations
	 * @return true if file associations changed and false otherwise
	 */
	public boolean setFileAssociations(XMLFileAssociation[] fileAssociations) {
		XMLFileAssociation[] oldFileAssociations = this.fileAssociations;
		this.fileAssociations = fileAssociations;
		if (fileAssociations != null && workspaceFolderUris != null) {
			for (XMLFileAssociation fa : fileAssociations) {
				fa.setWorkspaceFolderUris(workspaceFolderUris);
			}
		}
		expandSystemId();
		return !Arrays.equals(oldFileAssociations, fileAssociations);
	}

	@Override
	public String resolve(String baseLocation, String publicId, String systemId) {
		if (systemId != null) {
			// system id is defined in the XML root element (ex : systemId=Types.xsd for
			// <Types
			// xsi:noNamespaceSchemaLocation="Types.xsd">
			// ignore XML file association
			return null;
		}
		if (fileAssociations != null) {
			for (XMLFileAssociation fileAssociation : fileAssociations) {
				if (fileAssociation.matches(baseLocation)) {
					if (fileAssociation.hasSystemIdVariables()) {
						try {
							URI documentUri = new URI(baseLocation);
							String resolvedSystemId = fileAssociation.getResolvedSystemId(documentUri);
							return expandSystemId(resolvedSystemId);
						} catch (Exception e) {
							return fileAssociation.getSystemId();
						}
					}
					return fileAssociation.getSystemId();
				}
			}
		}
		return null;
	}

	@Override
	public Map<String, String> getExternalGrammarLocation(URI fileURI) {
		if (fileAssociations != null) {
			for (XMLFileAssociation fileAssociation : fileAssociations) {
				if (fileAssociation.matches(fileURI)) {
					return fileAssociation.getExternalSchemaLocation(fileURI);
				}
			}
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
	 * Set the workspace folder URIs used for resolving variables like
	 * {@code ${workspaceFolder}}.
	 *
	 * @param workspaceFolderUris the workspace folder URIs.
	 */
	public void setWorkspaceFolderUris(List<String> workspaceFolderUris) {
		this.workspaceFolderUris = workspaceFolderUris;
		if (fileAssociations != null) {
			for (XMLFileAssociation fa : fileAssociations) {
				fa.setWorkspaceFolderUris(workspaceFolderUris);
			}
		}
	}

	private void expandSystemId() {
		if (fileAssociations != null) {
			for (XMLFileAssociation fileAssociation : fileAssociations) {
				if (fileAssociation.hasSystemIdVariables()) {
					// Don't expand systemIds with variables; they are resolved per-document
					continue;
				}
				// Expand original system id, if rootUri is null then it uses just the systemId.
				try {
					String expandedSystemId = expandSystemId(fileAssociation.getSystemId());
					if (expandedSystemId != null) {
						fileAssociation.setSystemId(expandedSystemId);
					}
				} catch (Exception e) {
					LOGGER.log(Level.WARNING, "Issue expanding the provided systemId from the file associations.", e);
				}
			}
		}

	}

	private String expandSystemId(String systemId) {
		try {
			return XMLEntityManager.expandSystemId(systemId, rootUri, false);
		} catch (MalformedURIException e) {
			LOGGER.log(Level.WARNING, "Issue expanding systemId '" + systemId + "'.", e);
			return systemId;
		}
	}
}
