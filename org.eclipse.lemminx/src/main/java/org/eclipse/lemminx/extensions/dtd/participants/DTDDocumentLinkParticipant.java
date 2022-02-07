/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.dtd.participants;

import static org.eclipse.lemminx.utils.XMLPositionUtility.createDocumentLink;

import java.util.List;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMDocumentType;
import org.eclipse.lemminx.dom.DOMRange;
import org.eclipse.lemminx.dom.DTDEntityDecl;
import org.eclipse.lemminx.services.extensions.IDocumentLinkParticipant;
import org.eclipse.lemminx.uriresolver.URIResolverExtensionManager;
import org.eclipse.lsp4j.DocumentLink;
import org.w3c.dom.NamedNodeMap;

/**
 * Document link for DTD entities system inside a DTD:
 * 
 * <code>
 * 	<!ENTITY % xinclude SYSTEM "http://www.docbook.org/xml/4.4/xinclude.mod">
 * </code>
 * 
 * @author Angelo ZERR
 *
 */
public class DTDDocumentLinkParticipant implements IDocumentLinkParticipant {

	private final URIResolverExtensionManager resolverManager;

	public DTDDocumentLinkParticipant(URIResolverExtensionManager resolverManager) {
		this.resolverManager = resolverManager;
	}

	@Override
	public void findDocumentLinks(DOMDocument document, List<DocumentLink> links) {
		// Document link for DTD
		DOMDocumentType docType = document.getDoctype();
		if (docType != null) {
			// Loop for each entities declaration
			NamedNodeMap entities = docType.getEntities();
			for (int i = 0; i < entities.getLength(); i++) {
				DTDEntityDecl entity = (DTDEntityDecl) entities.item(i);
				String location = resolverManager.resolve(document.getDocumentURI(), entity.getPublicId(),
						entity.getSystemId());
				if (location != null) {
					try {
						// The entity declares a SYSTEM like
						// <!ENTITY % xinclude SYSTEM "http://www.docbook.org/xml/4.4/xinclude.mod">
						DOMRange systemIdRange = entity.getSystemIdNode();
						if (systemIdRange != null) {
							links.add(createDocumentLink(systemIdRange, location, true));
						}
					} catch (BadLocationException e) {
						// Do nothing
					}
				}
			}
		}
	}

}