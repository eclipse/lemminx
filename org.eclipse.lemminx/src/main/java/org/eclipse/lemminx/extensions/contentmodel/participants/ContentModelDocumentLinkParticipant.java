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
package org.eclipse.lemminx.extensions.contentmodel.participants;

import java.util.List;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.dom.NoNamespaceSchemaLocation;
import org.eclipse.lemminx.services.extensions.IDocumentLinkParticipant;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

public class ContentModelDocumentLinkParticipant implements IDocumentLinkParticipant {

	@Override
	public void findDocumentLinks(DOMDocument document, List<DocumentLink> links) {
		NoNamespaceSchemaLocation noNamespaceSchemaLocation = document.getNoNamespaceSchemaLocation();
		if (noNamespaceSchemaLocation != null) {
			try {
				String location = noNamespaceSchemaLocation.getResolvedLocation();
				DOMNode attrValue = noNamespaceSchemaLocation.getAttr().getNodeAttrValue();
				Position start = document.positionAt(attrValue.getStart() + 1);
				Position end = document.positionAt(attrValue.getEnd() - 1);
				links.add(new DocumentLink(new Range(start, end), location));
			} catch (BadLocationException e) {
				// Do nothing
			}
		}
	}

}
