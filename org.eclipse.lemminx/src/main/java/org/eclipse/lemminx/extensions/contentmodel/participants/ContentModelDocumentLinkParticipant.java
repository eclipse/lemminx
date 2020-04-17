/**
 *  Copyright (c) 2018-2020 Angelo ZERR
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
package org.eclipse.lemminx.extensions.contentmodel.participants;

import java.util.List;

import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.util.URI.MalformedURIException;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMDocumentType;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.dom.DOMRange;
import org.eclipse.lemminx.dom.NoNamespaceSchemaLocation;
import org.eclipse.lemminx.services.extensions.IDocumentLinkParticipant;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

/**
 * Document link for :
 * 
 * <ul>
 * <li>XML Schema xsi:noNamespaceSchemaLocation</li>
 * <li>DTD SYSTEM (ex : <!DOCTYPE root-element SYSTEM "./extended.dtd" )</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class ContentModelDocumentLinkParticipant implements IDocumentLinkParticipant {

	@Override
	public void findDocumentLinks(DOMDocument document, List<DocumentLink> links) {
		// Document link for xsi:noNamespaceSchemaLocation
		NoNamespaceSchemaLocation noNamespaceSchemaLocation = document.getNoNamespaceSchemaLocation();
		if (noNamespaceSchemaLocation != null) {
			try {
				String location = getResolvedLocation(document.getDocumentURI(),
						noNamespaceSchemaLocation.getLocation());
				if (location != null) {
					DOMNode attrValue = noNamespaceSchemaLocation.getAttr().getNodeAttrValue();
					Position start = document.positionAt(attrValue.getStart() + 1);
					Position end = document.positionAt(attrValue.getEnd() - 1);
					links.add(new DocumentLink(new Range(start, end), location));
				}
			} catch (BadLocationException e) {
				// Do nothing
			}
		}
		// Document link for DTD
		DOMDocumentType docType = document.getDoctype();
		if (docType != null) {
			String location = getResolvedLocation(document.getDocumentURI(), docType.getSystemIdWithoutQuotes());
			if (location != null) {
				try {
					DOMRange sytemIdRange = docType.getSystemIdNode();
					Position start = document.positionAt(sytemIdRange.getStart() + 1);
					Position end = document.positionAt(sytemIdRange.getEnd() - 1);
					links.add(new DocumentLink(new Range(start, end), location));
				} catch (BadLocationException e) {
					// Do nothing
				}
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
