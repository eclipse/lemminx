/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package org.eclipse.lemminx.extensions.catalog;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.util.URI.MalformedURIException;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.IDocumentLinkParticipant;
import org.eclipse.lemminx.utils.FilesUtils;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.DocumentLink;

/**
 * Document links that are specific to catalogs
 */
public class XMLCatalogDocumentLinkParticipant implements IDocumentLinkParticipant {

	private static Logger LOGGER = Logger.getLogger(XMLCatalogDocumentLinkParticipant.class.getName());

	@Override
	public void findDocumentLinks(DOMDocument document, List<DocumentLink> links) {
		for (DOMElement entry : CatalogUtils.getCatalogEntries(document)) {
			DOMAttr catalogReference = CatalogUtils.getCatalogEntryURI(entry);
			DOMNode valueLocation = catalogReference.getNodeAttrValue();
			try {
				String path = getResolvedLocation(FilesUtils.removeFileScheme(document.getDocumentURI()),
						catalogReference.getValue());
				links.add(XMLPositionUtility.createDocumentLink(valueLocation, path, true));
			} catch (BadLocationException e) {
				LOGGER.log(Level.SEVERE, "Creation of document link failed", e);
			}
		}
	}

	/**
	 * Returns the expanded system location
	 *
	 * @return the expanded system location
	 */
	private static String getResolvedLocation(String documentURI, String location) {
		if (location == null) {
			return null;
		}
		try {
			return XMLEntityManager.expandSystemId(location, documentURI, false);
		} catch (MalformedURIException e) {
			return location;
		}
	}

}