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
package org.eclipse.lsp4xml.extensions.references.participants;

import java.util.List;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.Node;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.extensions.references.XMLReferencesManager;
import org.eclipse.lsp4xml.services.extensions.IDefinitionParticipant;
import org.eclipse.lsp4xml.utils.XMLPositionUtility;

public class XMLReferencesDefinitionParticipant implements IDefinitionParticipant {

	@Override
	public void findDefinition(XMLDocument document, Position position, List<Location> locations) {
		try {
			int offset = document.offsetAt(position);
			Node node = document.findNodeAt(offset);
			if (node != null) {
				XMLReferencesManager.getInstance().collect(node, n -> {
					XMLDocument doc = n.getOwnerDocument();
					Range range = XMLPositionUtility.createRange(n.getStart(), n.getEnd(), doc);
					locations.add(new Location(doc.getDocumentURI(), range));
				});
			}
		} catch (BadLocationException e) {

		}
	}

}
