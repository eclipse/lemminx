/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.services.extensions;

import java.util.List;

import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4xml.dom.XMLDocument;

/**
 * Document link participant API.
 *
 */
public interface IDocumentLinkParticipant {

	/**
	 * Find document links of the given XML document.
	 * 
	 * @param document
	 * @param links
	 */
	void findDocumentLinks(XMLDocument document, List<DocumentLink> links);

}
